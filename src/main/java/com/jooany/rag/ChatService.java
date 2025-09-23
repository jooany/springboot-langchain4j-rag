package com.jooany.rag;

import dev.langchain4j.service.UserMessage;

public interface ChatService {

    String chat(@UserMessage String userMessage);
}
