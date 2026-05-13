package com.cboy.pineapple.ai.diagnostic;

import java.util.Optional;

public record DiagnosticErrorInfo(Optional<String> name, String message, Optional<String> stack, Optional<String> code) {}
