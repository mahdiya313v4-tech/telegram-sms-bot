package com.mmt.telegramsmsbot.service;

import com.mmt.telegramsmsbot.model.SMSMessage;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SMSService {

    private final List<SMSMessage> sampleMessages;
    private long lastCheckTime;

    public SMSService() {
        this.sampleMessages = generateSampleMessages();
        this.lastCheckTime = System.currentTimeMillis();
    }

    public String readMessages(int count, String filter, String selectedUser) {
        try {
            StringBuilder messages = new StringBuilder();
            List<SMSMessage> filteredMessages = getFilteredMessages(filter);
            
            int messageCount = 0;
            for (SMSMessage msg : filteredMessages) {
                if (messageCount >= count) break;
                
                messages.append("📩 از: ").append(msg.getSender()).append("\n")
                       .append("📅 تاریخ: ").append(msg.getFormattedDate()).append("\n")
                       .append("💬 متن: ").append(msg.getBody()).append("\n")
                       .append("➖➖➖➖➖➖➖➖➖➖\n");
                messageCount++;
            }

            if (messages.length() == 0) {
                return "📭 پیامکی یافت نشد.";
            }

            return "📱 پیامک‌های کاربر `" + selectedUser + "`:\n\n" + messages.toString();
        } catch (Exception e) {
            return "❌ خطا: " + e.getMessage();
        }
    }

    private List<SMSMessage> getFilteredMessages(String filter) {
        List<SMSMessage> filtered = new ArrayList<>();
        
        for (SMSMessage msg : sampleMessages) {
            switch (filter.toLowerCase()) {
                case "bank":
                    if (isBankMessage(msg)) {
                        filtered.add(msg);
                    }
                    break;
                case "personal":
                    if (!isBankMessage(msg)) {
                        filtered.add(msg);
                    }
                    break;
                case "all":
                default:
                    filtered.add(msg);
                    break;
            }
        }
        
        return filtered;
    }

    private boolean isBankMessage(SMSMessage msg) {
        String bodyLower = msg.getBody().toLowerCase();
        String senderLower = msg.getSender().toLowerCase();

        String[] bankKeywords = {"bank", "بانک", "حساب", "کارت", "مانده", "انتقال", "پرداخت", "واریز", "برداشت"};
        String[] bankNumbers = {"98300", "9830", "1000", "2000", "3000"};

        for (String keyword : bankKeywords) {
            if (bodyLower.contains(keyword) || senderLower.contains(keyword)) {
                return true;
            }
        }
        for (String number : bankNumbers) {
            if (senderLower.contains(number)) {
                return true;
            }
        }
        return false;
    }

    public List<SMSMessage> getNewMessages() {
        List<SMSMessage> newMessages = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastCheckTime > 300000) {
            newMessages.add(new SMSMessage(
                "پیامک تست جدید - " + new Date(),
                "TestSender",
                currentTime
            ));
            lastCheckTime = currentTime;
        }
        
        return newMessages;
    }

    private List<SMSMessage> generateSampleMessages() {
        List<SMSMessage> messages = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        messages.add(new SMSMessage(
            "مبلغ 50000 تومان از حساب شما برداشت شد. مانده: 150000 تومان",
            "98309820",
            currentTime - 3600000
        ));
        
        messages.add(new SMSMessage(
            "واریز 200000 تومان به حساب شما انجام شد",
            "بانک ملی",
            currentTime - 7200000
        ));
        
        messages.add(new SMSMessage(
            "سلام، امروز میای خونه؟",
            "09123456789",
            currentTime - 1800000
        ));
        
        messages.add(new SMSMessage(
            "جلسه فردا ساعت 2 لغو شد",
            "دفتر کار",
            currentTime - 5400000
        ));
        
        return messages;
    }
}
