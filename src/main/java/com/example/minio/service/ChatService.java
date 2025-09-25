package com.example.minio.service;

import org.springframework.stereotype.Service;

import com.example.minio.entity.ChatMessage;
import com.example.minio.entity.Conversation;

import java.lang.Thread;
import java.util.*;

@Service
public class ChatService {
    private final Map<String, Conversation> conversations = new HashMap<>();

    public Conversation getOrCreateConversation(String id) {
        if (id == null) {
            id = UUID.randomUUID().toString(); 
            Conversation newConversation = new Conversation();
            newConversation.setId(id);
            conversations.put(id, newConversation);

            return newConversation;
        }
        return conversations.computeIfAbsent(id, cid -> {
            Conversation c = new Conversation();
            c.setId(cid);
            return c;
        });
    }

    public void addMessage(String conversationId, ChatMessage message) {
        
        try {
            Thread.sleep(1000);
            Conversation c = getOrCreateConversation(conversationId);
            c.getMessages().add(message);
        } catch (Exception e) {
        }
    }

    public List<ChatMessage> getMessages(String conversationId) {
        return getOrCreateConversation(conversationId).getMessages();
    }

    public Map<String, Conversation> getCons() {
        return conversations;
    }

    public void addParticipant(String conversationId, String user) {
        getOrCreateConversation(conversationId).getParticipants().add(user);
    }

    public Set<String> getParticipants(String conversationId) {
        return getOrCreateConversation(conversationId).getParticipants();
    }

    public String getConversationByParticipants(String userId1, String userId2) {
        for (Conversation conversation : conversations.values()) {
            Set<String> participants = conversation.getParticipants();
            if (participants.size() == 2 &&
                participants.contains(userId1) &&
                participants.contains(userId2)) {
                return conversation.getId();
            }
        }

        String newId = UUID.randomUUID().toString();
        Conversation newConversation = new Conversation();
        newConversation.setId(newId);
        newConversation.getParticipants().add(userId1);
        newConversation.getParticipants().add(userId2);
        conversations.put(newId, newConversation);

        return newId;
    }
}
