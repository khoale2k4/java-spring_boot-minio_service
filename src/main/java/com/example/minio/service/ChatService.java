package com.example.minio.service;

import org.springframework.stereotype.Service;

import com.example.minio.entity.ChatMessage;
import com.example.minio.entity.Conversation;

import java.util.*;

@Service
public class ChatService {
    private final Map<String, Conversation> conversations = new HashMap<>();

    public Conversation getOrCreateConversation(String id) {
        return conversations.computeIfAbsent(id, cid -> {
            Conversation c = new Conversation();
            c.setId(cid);
            return c;
        });
    }

    public void addMessage(String conversationId, ChatMessage message) {
        Conversation c = getOrCreateConversation(conversationId);
        c.getMessages().add(message);
    }

    public List<ChatMessage> getMessages(String conversationId) {
        return getOrCreateConversation(conversationId).getMessages();
    }

    public void addParticipant(String conversationId, String user) {
        getOrCreateConversation(conversationId).getParticipants().add(user);
    }

    public Set<String> getParticipants(String conversationId) {
        return getOrCreateConversation(conversationId).getParticipants();
    }
}
