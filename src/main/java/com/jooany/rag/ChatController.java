package com.jooany.rag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    private record ChatRequest(String query) {
    }

    /**
     * http://localhost:8080/chat (POST)
     *
     * @param request {"query": "이 지원자의 경력은?"}
     * @return RAG 챗봇의 답변 (String)
     */
    @PostMapping("/chat")
    public String handleChat(@RequestBody ChatRequest request) {
        return chatService.chat(request.query());
    }
}
