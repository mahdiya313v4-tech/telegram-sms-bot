package com.mmt.telegramsmsbot.controller;

import com.mmt.telegramsmsbot.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@RequestMapping("/bot")
public class BotController {

    @Autowired
    private TelegramBotService botService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> update) {
        botService.processUpdate(update);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(botService.getStatus());
    }

    @PostMapping("/setWebhook")
    public ResponseEntity<String> setWebhook(@RequestParam String url) {
        boolean success = botService.setWebhook(url);
        return ResponseEntity.ok(success ? "Webhook set successfully" : "Failed to set webhook");
    }
}
