package com.example.minio.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.minio.entity.ChatMessage;
import com.example.minio.service.ChatService;

import io.minio.StatObjectResponse;

@Controller
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        chatService.addMessage(message.getConversationId(), message);
        // gửi về topic conversation.{id}
        messagingTemplate.convertAndSend(
                "/topic/conversation." + message.getConversationId(),
                message);
    }

    @MessageMapping("/addUser")
    public void addUser(@Payload ChatMessage message) {
        chatService.addParticipant(message.getConversationId(), message.getSender());
        message.setType("JOIN");
        messagingTemplate.convertAndSend(
                "/topic/conversation." + message.getConversationId(),
                message);
    }

    @GetMapping("/{conId}")
    public ResponseEntity<Map<String, Object>> getMessages(@PathVariable String conId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ChatMessage> messages = chatService.getMessages(conId);

            response.put("success", true);
            response.put("data", messages);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Conversation not found: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
