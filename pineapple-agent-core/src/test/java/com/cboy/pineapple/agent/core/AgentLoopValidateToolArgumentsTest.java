package com.cboy.pineapple.agent.core;

import com.cboy.pineapple.agent.core.types.AgentTool;
import com.cboy.pineapple.agent.core.types.AgentToolCall;
import com.cboy.pineapple.agent.core.types.AgentToolResult;
import com.cboy.pineapple.ai.types.AbortSignal;
import com.cboy.pineapple.ai.types.content.TextContent;
import com.cboy.pineapple.ai.types.content.ToolCall;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AgentLoopValidateToolArgumentsTest {

    private AgentLoop agentLoop;

    @BeforeEach
    void setUp() {
        agentLoop = new AgentLoop();
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /** Create a stub AgentTool with the given JSON schema. */
    private AgentTool stubTool(String name, String schemaJson) {
        return new AgentTool() {
            @Override public String name() { return name; }
            @Override public String description() { return "test tool"; }
            @Override public String parameters() { return schemaJson; }
            @Override public String label() { return "Test"; }
            @Override public CompletableFuture<AgentToolResult> execute(
                    String toolCallId, Map<String, Object> params,
                    AbortSignal signal, Consumer<Object> onUpdate) {
                return CompletableFuture.completedFuture(
                        AgentToolResult.of(List.of(new TextContent("ok", Optional.empty()))));
            }
        };
    }

    /** Create an AgentToolCall with the given arguments. */
    private AgentToolCall toolCall(String name, Map<String, Object> arguments) {
        return new AgentToolCall(new ToolCall("call_1", name, arguments, Optional.empty()));
    }

    // ── Test cases ───────────────────────────────────────────────────

    @Nested
    @DisplayName("When schema is null or empty")
    class NoSchema {

        @Test
        @DisplayName("null schema — skip validation, return args as-is")
        void nullSchema_returnsArgsDirectly() {
            AgentTool tool = stubTool("read_file", null);
            AgentToolCall call = toolCall("read_file", Map.of("path", "/tmp/test.txt"));

            Map<String, Object> result = agentLoop.validateToolArguments(tool, call);

            assertEquals("/tmp/test.txt", result.get("path"));
        }

        @Test
        @DisplayName("blank schema — skip validation, return args as-is")
        void blankSchema_returnsArgsDirectly() {
            AgentTool tool = stubTool("read_file", "   ");
            AgentToolCall call = toolCall("read_file", Map.of("path", "/tmp/test.txt"));

            Map<String, Object> result = agentLoop.validateToolArguments(tool, call);

            assertEquals("/tmp/test.txt", result.get("path"));
        }
    }

    @Nested
    @DisplayName("When arguments match the schema")
    class ValidArgs {

        @Test
        @DisplayName("required string field present — passes")
        void requiredStringField() {
            String schema = """
                {
                  "type": "object",
                  "properties": {
                    "path": { "type": "string" }
                  },
                  "required": ["path"]
                }
                """;
            AgentTool tool = stubTool("read_file", schema);
            AgentToolCall call = toolCall("read_file", Map.of("path", "/etc/hosts"));

            Map<String, Object> result = agentLoop.validateToolArguments(tool, call);

            assertEquals("/etc/hosts", result.get("path"));
        }

        @Test
        @DisplayName("multiple typed fields — passes when all correct")
        void multipleTypedFields() {
            String schema = """
                {
                  "type": "object",
                  "properties": {
                    "name": { "type": "string" },
                    "count": { "type": "integer" },
                    "verbose": { "type": "boolean" }
                  },
                  "required": ["name", "count"]
                }
                """;
            AgentTool tool = stubTool("create_item", schema);
            AgentToolCall call = toolCall("create_item", Map.of(
                    "name", "widget",
                    "count", 5,
                    "verbose", true
            ));

            Map<String, Object> result = agentLoop.validateToolArguments(tool, call);

            assertEquals("widget", result.get("name"));
            assertEquals(5, result.get("count"));
            assertEquals(true, result.get("verbose"));
        }
    }

    @Nested
    @DisplayName("When arguments violate the schema")
    class InvalidArgs {

        @Test
        @DisplayName("missing required field — throws with error details")
        void missingRequiredField() {
            String schema = """
                {
                  "type": "object",
                  "properties": {
                    "path": { "type": "string" }
                  },
                  "required": ["path"]
                }
                """;
            AgentTool tool = stubTool("read_file", schema);
            AgentToolCall call = toolCall("read_file", Map.of());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> agentLoop.validateToolArguments(tool, call));

            assertTrue(ex.getMessage().contains("Validation failed for tool \"read_file\""));
            assertTrue(ex.getMessage().contains("path"));
        }

        @Test
        @DisplayName("wrong type — throws with error details")
        void wrongType() {
            String schema = """
                {
                  "type": "object",
                  "properties": {
                    "count": { "type": "integer" }
                  },
                  "required": ["count"]
                }
                """;
            AgentTool tool = stubTool("counter", schema);
            // LLM returned a string instead of integer
            AgentToolCall call = toolCall("counter", Map.of("count", "five"));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> agentLoop.validateToolArguments(tool, call));

            assertTrue(ex.getMessage().contains("Validation failed for tool \"counter\""));
            assertTrue(ex.getMessage().contains("count"));
        }

        @Test
        @DisplayName("error message includes original arguments as JSON")
        void errorMessageIncludesOriginalArgs() {
            String schema = """
                {
                  "type": "object",
                  "properties": {
                    "path": { "type": "string" }
                  },
                  "required": ["path"]
                }
                """;
            AgentTool tool = stubTool("read_file", schema);
            AgentToolCall call = toolCall("read_file", Map.of("unknown_field", 42));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> agentLoop.validateToolArguments(tool, call));

            assertTrue(ex.getMessage().contains("Received arguments:"));
            assertTrue(ex.getMessage().contains("unknown_field"));
        }
    }

    @Nested
    @DisplayName("Deep copy behavior")
    class DeepCopy {

        @Test
        @DisplayName("validation does not mutate original arguments")
        void doesNotMutateOriginal() {
            String schema = """
                {
                  "type": "object",
                  "properties": {
                    "nested": { "type": "object" }
                  }
                }
                """;
            AgentTool tool = stubTool("test", schema);
            Map<String, Object> originalNested = new java.util.HashMap<>(Map.of("key", "value"));
            Map<String, Object> originalArgs = new java.util.HashMap<>(Map.of("nested", originalNested));
            AgentToolCall call = toolCall("test", originalArgs);

            Map<String, Object> result = agentLoop.validateToolArguments(tool, call);

            // Result is a different object
            assertNotSame(originalArgs, result);
            // But equal in content
            assertEquals(originalArgs, result);
            // Nested map is also a copy
            assertNotSame(originalNested, result.get("nested"));
        }
    }
}
