package com.cboy.pineapple.ai.types;

import com.cboy.pineapple.ai.types.message.Message;
import com.cboy.pineapple.ai.types.tool.Tool;

import java.util.List;

public record Context(String systemPrompt, List<Message> messages, List<? extends Tool> tools) {

}
