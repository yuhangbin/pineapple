package com.cboy.pineapple.ai.diagnostic;

import java.util.Map;
import java.util.Optional;

public record AssistantMessageDiagnostic(String type, long timestamp, Optional<DiagnosticErrorInfo> error, Map<String, Object> details) {}
