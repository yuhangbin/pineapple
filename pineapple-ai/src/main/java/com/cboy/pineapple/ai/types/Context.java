package com.cboy.pineapple.ai.types;

import com.cboy.pineapple.ai.types.message.Message;
import com.cboy.pineapple.ai.types.tool.Tool;

import java.util.List;
import java.util.Optional;

public record Context(Optional<String> systemPrompt, List<Message> messages, List<Tool> tools) {

}
