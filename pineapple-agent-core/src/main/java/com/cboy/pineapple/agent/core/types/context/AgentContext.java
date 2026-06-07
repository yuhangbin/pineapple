package com.cboy.pineapple.agent.core.types.context;

import com.cboy.pineapple.ai.types.message.AgentMessage;
import com.cboy.pineapple.agent.core.types.AgentTool;

import java.util.List;

public class AgentContext {

    private String systemPrompt;
    private List<AgentMessage> messages;
    private List<AgentTool> tools;

    public AgentContext(String systemPrompt, List<AgentMessage> messages, List<AgentTool> tools) {
        this.systemPrompt = systemPrompt;
        this.messages = messages;
        this.tools = tools;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public List<AgentMessage> getMessages() {
        return messages;
    }

    public List<AgentTool> getTools() {
        return tools;
    }

    public AgentContext setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    public AgentContext setMessages(List<AgentMessage> messages) {
        this.messages = messages;
        return this;
    }

    public AgentContext setTools(List<AgentTool> tools) {
        this.tools = tools;
        return this;
    }
}
