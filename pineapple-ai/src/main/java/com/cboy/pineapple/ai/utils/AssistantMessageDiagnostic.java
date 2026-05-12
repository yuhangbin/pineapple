package com.cboy.pineapple.ai.utils;

import java.util.Map;
import java.util.Optional;

record DiagnosticErrorInfo(Optional<String> name, String message, Optional<String> stack, Optional<String> code) {}

public record AssistantMessageDiagnostic(String type, double timestamp, Optional<DiagnosticErrorInfo> error, Map<String, Object> details) {}
