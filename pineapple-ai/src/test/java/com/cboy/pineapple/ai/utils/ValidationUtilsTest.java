package com.cboy.pineapple.ai.utils;

import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.types.tool.Tool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    private static Tool tool(String schemaJson) {
        return tool("testTool", schemaJson);
    }

    private static Tool tool(String name, String schemaJson) {
        return Tool.of(name, "test description", schemaJson);
    }

    private static ToolCall call(String name, Map<String, Object> args) {
        return new ToolCall("call_1", name, args, Optional.empty());
    }

    private static Map<String, Object> args(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private Map<String, Object> validate(String schema, Map<String, Object> input) {
        return ValidationUtils.validateToolArguments(tool(schema), call("testTool", input));
    }

    private Map<String, Object> validate(String schema, String toolName, Map<String, Object> input) {
        return ValidationUtils.validateToolArguments(tool(toolName, schema), call(toolName, input));
    }

    // ══════════════════════════════════════════════════════════════════
    // 1. Happy Path — Valid Arguments Pass Through
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. Happy Path")
    class HappyPath {

        @Test @DisplayName("1.1 validStringArg")
        void validStringArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},\"required\":[\"name\"]}", args("name", "hello"));
            assertEquals("hello", r.get("name"));
        }

        @Test @DisplayName("1.2 validNumberArg")
        void validNumberArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"count\":{\"type\":\"number\"}},\"required\":[\"count\"]}", args("count", 42.0));
            assertEquals(42.0, ((Number) r.get("count")).doubleValue());
        }

        @Test @DisplayName("1.3 validIntegerArg")
        void validIntegerArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"count\":{\"type\":\"integer\"}},\"required\":[\"count\"]}", args("count", 42));
            assertEquals(42, r.get("count"));
            assertInstanceOf(Integer.class, r.get("count"));
        }

        @Test @DisplayName("1.4 validBooleanArg")
        void validBooleanArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"flag\":{\"type\":\"boolean\"}},\"required\":[\"flag\"]}", args("flag", true));
            assertEquals(true, r.get("flag"));
        }

        @Test @DisplayName("1.5 validNullArg")
        void validNullArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"null\"}},\"required\":[\"val\"]}", args("val", null));
            assertNull(r.get("val"));
        }

        @Test @DisplayName("1.6 validArrayArg")
        void validArrayArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}},\"required\":[\"items\"]}", args("items", List.of("a", "b")));
            assertEquals(List.of("a", "b"), r.get("items"));
        }

        @Test @DisplayName("1.7 validNestedObjectArg")
        void validNestedObjectArg() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"config\":{\"type\":\"object\",\"properties\":{\"debug\":{\"type\":\"boolean\"}}}},\"required\":[\"config\"]}", args("config", Map.of("debug", true)));
            assertEquals(Map.of("debug", true), r.get("config"));
        }

        @Test @DisplayName("1.8 validMultipleArgs")
        void validMultipleArgs() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"count\":{\"type\":\"integer\"}},\"required\":[\"name\",\"count\"]}", args("name", "test", "count", 5));
            assertEquals("test", r.get("name"));
            assertEquals(5, r.get("count"));
        }

        @Test @DisplayName("1.9 validOptionalArgOmitted")
        void validOptionalArgOmitted() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"opt\":{\"type\":\"string\"}}}", args("name", "test"));
            assertEquals("test", r.get("name"));
            assertFalse(r.containsKey("opt"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 2. Type Coercion — Plain JSON Schema Primitives
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. Type Coercion — Primitives")
    class CoercionPrimitives {

        private Map<String, Object> coerce(String type, Object input) {
            String schema = "{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"" + type + "\"}}}";
            return validate(schema, args("val", input));
        }

        @Test @DisplayName("2.1 coerceStringToNumber")
        void coerceStringToNumber() { assertEquals(42.0, ((Number) coerce("number", "42").get("val")).doubleValue()); }

        @Test @DisplayName("2.2 coerceBooleanTrueToNumber")
        void coerceBooleanTrueToNumber() { assertEquals(1.0, ((Number) coerce("number", true).get("val")).doubleValue()); }

        @Test @DisplayName("2.3 coerceBooleanFalseToNumber")
        void coerceBooleanFalseToNumber() { assertEquals(0.0, ((Number) coerce("number", false).get("val")).doubleValue()); }

        @Test @DisplayName("2.4 coerceNullToNumber")
        void coerceNullToNumber() { assertEquals(0.0, ((Number) coerce("number", null).get("val")).doubleValue()); }

        @Test @DisplayName("2.5 coerceStringToInteger")
        void coerceStringToInteger() { assertEquals(42, coerce("integer", "42").get("val")); }

        @Test @DisplayName("2.6 coerceBooleanTrueToInteger")
        void coerceBooleanTrueToInteger() { assertEquals(1, coerce("integer", true).get("val")); }

        @Test @DisplayName("2.7 coerceBooleanFalseToInteger")
        void coerceBooleanFalseToInteger() { assertEquals(0, coerce("integer", false).get("val")); }

        @Test @DisplayName("2.8 coerceNullToInteger")
        void coerceNullToInteger() { assertEquals(0, coerce("integer", null).get("val")); }

        @Test @DisplayName("2.9 coerceStringTrueToBoolean")
        void coerceStringTrueToBoolean() { assertEquals(true, coerce("boolean", "true").get("val")); }

        @Test @DisplayName("2.10 coerceStringFalseToBoolean")
        void coerceStringFalseToBoolean() { assertEquals(false, coerce("boolean", "false").get("val")); }

        @Test @DisplayName("2.11 coerceInt1ToBoolean")
        void coerceInt1ToBoolean() { assertEquals(true, coerce("boolean", 1).get("val")); }

        @Test @DisplayName("2.12 coerceInt0ToBoolean")
        void coerceInt0ToBoolean() { assertEquals(false, coerce("boolean", 0).get("val")); }

        @Test @DisplayName("2.13 coerceNullToBoolean")
        void coerceNullToBoolean() { assertEquals(false, coerce("boolean", null).get("val")); }

        @Test @DisplayName("2.14 coerceNullToString")
        void coerceNullToString() { assertEquals("", coerce("string", null).get("val")); }

        @Test @DisplayName("2.15 coerceBooleanToString")
        void coerceBooleanToString() { assertEquals("true", coerce("string", true).get("val")); }

        @Test @DisplayName("2.16 coerceNumberToString")
        void coerceNumberToString() { assertEquals("42", coerce("string", 42).get("val")); }

        @Test @DisplayName("2.17 coerceEmptyStringToNull")
        void coerceEmptyStringToNull() { assertNull(coerce("null", "").get("val")); }

        @Test @DisplayName("2.18 coerceZeroToNull")
        void coerceZeroToNull() { assertNull(coerce("null", 0).get("val")); }

        @Test @DisplayName("2.19 coerceFalseToNull")
        void coerceFalseToNull() { assertNull(coerce("null", false).get("val")); }
    }

    // ══════════════════════════════════════════════════════════════════
    // 3. Type Coercion — Union Types
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. Union Type Coercion")
    class UnionTypeCoercion {

        private Map<String, Object> union(String types, Object input) {
            return validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":" + types + "}}}", args("val", input));
        }

        @Test @DisplayName("3.1 unionNumberString_stringInput_staysString")
        void stringStaysString() { assertEquals("1", union("[\"number\",\"string\"]", "1").get("val")); }

        @Test @DisplayName("3.2 unionBooleanNumber_stringInput_coercesToNumber")
        void stringCoercesToNumber() { assertEquals(1.0, ((Number) union("[\"boolean\",\"number\"]", "1").get("val")).doubleValue()); }

        @Test @DisplayName("3.3 unionNumberString_numberInput_staysNumber")
        void numberStaysNumber() { assertEquals(42.0, ((Number) union("[\"number\",\"string\"]", 42).get("val")).doubleValue()); }

        @Test @DisplayName("3.4 unionBooleanString_trueString_nativelyMatchesString")
        void trueStringNativelyMatchesString() { assertEquals("true", union("[\"boolean\",\"string\"]", "true").get("val")); }
    }

    // ══════════════════════════════════════════════════════════════════
    // 4. Rejection — Invalid Coercion Fails
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. Rejection — Invalid Coercion")
    class Rejection {

        @Test @DisplayName("4.1 rejectStringOneForBoolean")
        void rejectStringOneForBoolean() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"boolean\"}}}", args("val", "1")));
        }

        @Test @DisplayName("4.2 rejectStringZeroForBoolean")
        void rejectStringZeroForBoolean() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"boolean\"}}}", args("val", "0")));
        }

        @Test @DisplayName("4.3 rejectStringNullForNull")
        void rejectStringNullForNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"null\"}}}", args("val", "null")));
        }

        @Test @DisplayName("4.4 rejectFloatStringForInteger")
        void rejectFloatStringForInteger() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"integer\"}}}", args("val", "42.1")));
        }

        @Test @DisplayName("4.5 rejectNonNumericStringForNumber")
        void rejectNonNumericStringForNumber() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"number\"}}}", args("val", "abc")));
        }

        @Test @DisplayName("4.6 rejectObjectForString")
        void rejectObjectForString() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"string\"}}}", args("val", Map.of())));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 5. Validation Error Formatting
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("5. Error Formatting")
    class ErrorFormatting {

        @Test @DisplayName("5.1 errorContainsToolName")
        void errorContainsToolName() {
            var ex = assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"required\":[\"name\"]}", "myTool", Map.of()));
            assertTrue(ex.getMessage().contains("myTool"));
        }

        @Test @DisplayName("5.2 errorContainsFieldName")
        void errorContainsFieldName() {
            var ex = assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"required\":[\"path\"]}", args()));
            assertTrue(ex.getMessage().contains("path"));
        }

        @Test @DisplayName("5.3 errorContainsReceivedArgs")
        void errorContainsReceivedArgs() {
            var ex = assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"required\":[\"missing\"]}", args("present", 42)));
            assertTrue(ex.getMessage().contains("Received arguments:"));
            assertTrue(ex.getMessage().contains("present"));
        }

        @Test @DisplayName("5.4 errorContainsValidationPrefix")
        void errorContainsValidationPrefix() {
            var ex = assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"required\":[\"x\"]}", args()));
            assertTrue(ex.getMessage().startsWith("Validation failed for tool"));
        }

        @Test @DisplayName("5.5 errorContainsMultipleErrors")
        void errorContainsMultipleErrors() {
            var ex = assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"a\":{\"type\":\"string\"},\"b\":{\"type\":\"integer\"}},\"required\":[\"a\",\"b\"]}", args("a", 123, "b", "not_int")));
            long errorCount = ex.getMessage().lines().filter(l -> l.startsWith("  -")).count();
            assertTrue(errorCount >= 1);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 6. Required Field Validation
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("6. Required Fields")
    class RequiredFields {

        @Test @DisplayName("6.1 missingRequiredField_throws")
        void missingRequiredField() {
            assertThrows(IllegalArgumentException.class, () -> validate("{\"type\":\"object\",\"required\":[\"name\"]}", args()));
        }

        @Test @DisplayName("6.2 missingOneOfTwoRequired_throws")
        void missingOneOfTwoRequired() {
            assertThrows(IllegalArgumentException.class, () -> validate("{\"type\":\"object\",\"required\":[\"name\",\"count\"]}", args("name", "x")));
        }

        @Test @DisplayName("6.3 allRequiredPresent_passes")
        void allRequiredPresent() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"count\":{\"type\":\"integer\"}},\"required\":[\"name\",\"count\"]}", args("name", "x", "count", 1));
            assertEquals("x", r.get("name"));
            assertEquals(1, r.get("count"));
        }

        @Test @DisplayName("6.4 nestedMissingRequired_throws")
        void nestedMissingRequired() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"outer\":{\"type\":\"object\",\"properties\":{\"field\":{\"type\":\"string\"}},\"required\":[\"field\"]}}}", args("outer", Map.of())));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 7. Nested Object and Array Coercion
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("7. Nested & Array Coercion")
    class NestedArrayCoercion {

        @Test @DisplayName("7.1 coerceNestedObjectProperty")
        void coerceNestedObjectProperty() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"config\":{\"type\":\"object\",\"properties\":{\"port\":{\"type\":\"number\"}}}}}", args("config", Map.of("port", "8080")));
            @SuppressWarnings("unchecked") Map<String,Object> config = (Map<String,Object>) r.get("config");
            assertEquals(8080.0, ((Number) config.get("port")).doubleValue());
        }

        @Test @DisplayName("7.2 coerceArrayItems")
        void coerceArrayItems() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"nums\":{\"type\":\"array\",\"items\":{\"type\":\"number\"}}}}", args("nums", List.of("1", "2", "3")));
            @SuppressWarnings("unchecked") List<Object> nums = (List<Object>) r.get("nums");
            assertEquals(3, nums.size());
            assertEquals(1.0, ((Number) nums.get(0)).doubleValue());
        }

        @Test @DisplayName("7.3 coerceTupleItems")
        void coerceTupleItems() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"pair\":{\"type\":\"array\",\"prefixItems\":[{\"type\":\"number\"},{\"type\":\"string\"}]}}}", args("pair", List.of("42", 100)));
            @SuppressWarnings("unchecked") List<Object> pair = (List<Object>) r.get("pair");
            assertEquals(42.0, ((Number) pair.get(0)).doubleValue());
            assertEquals("100", pair.get(1));
        }

        @Test @DisplayName("7.4 additionalPropertiesCoercion")
        void additionalPropertiesCoercion() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},\"additionalProperties\":{\"type\":\"number\"}}", args("name", "test", "extra", "42"));
            assertEquals("test", r.get("name"));
            assertEquals(42.0, ((Number) r.get("extra")).doubleValue());
        }

        @Test @DisplayName("7.5 additionalPropertiesFalse_rejectsExtra")
        void additionalPropertiesFalse_rejectsExtra() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},\"additionalProperties\":false}", args("name", "test", "extra", 42)));
        }

        @Test @DisplayName("7.6 deepNestedObjectCoercion")
        void deepNestedObjectCoercion() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"l1\":{\"type\":\"object\",\"properties\":{\"l2\":{\"type\":\"object\",\"properties\":{\"l3\":{\"type\":\"number\"}}}}}}}", args("l1", Map.of("l2", Map.of("l3", "99"))));
            @SuppressWarnings("unchecked") Map<String,Object> l1 = (Map<String,Object>) r.get("l1");
            @SuppressWarnings("unchecked") Map<String,Object> l2 = (Map<String,Object>) l1.get("l2");
            assertEquals(99.0, ((Number) l2.get("l3")).doubleValue());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 8. allOf / anyOf / oneOf Handling
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("8. allOf / anyOf / oneOf")
    class CompositeSchemas {

        @Test @DisplayName("8.1 allOf_mergesSchemas")
        void allOf_mergesSchemas() {
            var r = validate("{\"type\":\"object\",\"allOf\":[{\"properties\":{\"a\":{\"type\":\"string\"}}},{\"properties\":{\"b\":{\"type\":\"number\"}}}],\"required\":[\"a\",\"b\"]}", args("a", "hello", "b", "42"));
            assertEquals("hello", r.get("a"));
            assertEquals(42.0, ((Number) r.get("b")).doubleValue());
        }

        @Test @DisplayName("8.2 anyOf_matchesFirstValid")
        void anyOf_matchesFirstValid() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"val\":{\"anyOf\":[{\"type\":\"string\"},{\"type\":\"number\"}]}}}", args("val", "hello"));
            assertEquals("hello", r.get("val"));
        }

        @Test @DisplayName("8.3 anyOf_coercesToMatchingBranch")
        void anyOf_coercesToMatchingBranch() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"val\":{\"anyOf\":[{\"type\":\"number\"}]}}}", args("val", "42"));
            assertEquals(42.0, ((Number) r.get("val")).doubleValue());
        }

        @Test @DisplayName("8.4 oneOf_matchesExactlyOne")
        void oneOf_matchesExactlyOne() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"val\":{\"oneOf\":[{\"type\":\"string\"},{\"type\":\"number\"}]}}}", args("val", 42));
            assertEquals(42.0, ((Number) r.get("val")).doubleValue());
        }

        @Test @DisplayName("8.5 oneOf_matchesNone_throws")
        void oneOf_matchesNone_throws() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"val\":{\"oneOf\":[{\"type\":\"boolean\"}]}}}", args("val", Map.of())));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 9. Edge Cases
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("9. Edge Cases")
    class EdgeCases {

        @Test @DisplayName("9.1 emptyObjectSchema_acceptsAnyObject")
        void emptyObjectSchema_acceptsAnyObject() {
            var r = validate("{\"type\":\"object\"}", args("anything", "goes"));
            assertEquals("goes", r.get("anything"));
        }

        @Test @DisplayName("9.2 emptyArguments_noRequired_passes")
        void emptyArguments_noRequired_passes() {
            var r = validate("{\"type\":\"object\"}", Map.of());
            assertTrue(r.isEmpty());
        }

        @Test @DisplayName("9.3 nullSchema_skipsValidation")
        void nullSchema_skipsValidation() {
            Tool t = Tool.of("test", "desc", null);
            var r = ValidationUtils.validateToolArguments(t, call("test", args("a", 1)));
            assertEquals(1, r.get("a"));
        }

        @Test @DisplayName("9.4 originalArgsNotMutated")
        void originalArgsNotMutated() {
            Map<String, Object> original = new HashMap<>(Map.of("val", "42"));
            validate("{\"type\":\"object\",\"properties\":{\"val\":{\"type\":\"number\"}}}", original);
            assertEquals("42", original.get("val"));
        }

        @Test @DisplayName("9.5 toolCallWithExtraFields_stripsWhenNoAdditional")
        void toolCallWithExtraFields_rejectsWhenNoAdditional() {
            assertThrows(IllegalArgumentException.class, () ->
                    validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},\"additionalProperties\":false}", args("name", "test", "extra", 42)));
        }

        @Test @DisplayName("9.6 toolCallWithExtraFields_passesWithDefaultAdditional")
        void toolCallWithExtraFields_passesWithDefaultAdditional() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}", args("name", "test", "extra", 42));
            assertEquals("test", r.get("name"));
            assertEquals(42, r.get("extra"));
        }

        @Test @DisplayName("9.7 veryLargeStringArgument")
        void veryLargeStringArgument() {
            String large = "x".repeat(10_000);
            var r = validate("{\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"string\"}}}", args("data", large));
            assertEquals(large, r.get("data"));
        }

        @Test @DisplayName("9.8 unicodeInStringArgs")
        void unicodeInStringArgs() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"msg\":{\"type\":\"string\"}}}", args("msg", "你好世界 🍍"));
            assertEquals("你好世界 🍍", r.get("msg"));
        }

        @Test @DisplayName("9.9 schemaWithDefaultValues")
        void schemaWithDefaultValues() {
            var r = validate("{\"type\":\"object\",\"properties\":{\"count\":{\"type\":\"integer\",\"default\":42}}}", args());
            assertFalse(r.containsKey("count"));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // 10. Integration with validateToolCall
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("10. validateToolCall Integration")
    class ValidateToolCallIntegration {

        @Test @DisplayName("10.1 toolFound_validates")
        void toolFound_validates() {
            Tool t = tool("read", "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}},\"required\":[\"path\"]}");
            var r = ValidationUtils.validateToolCall(List.of(t), call("read", args("path", "/tmp")));
            assertEquals("/tmp", r.get("path"));
        }

        @Test @DisplayName("10.2 toolNotFound_throws")
        void toolNotFound_throws() {
            Tool t = tool("read", "{\"type\":\"object\"}");
            var ex = assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateToolCall(List.of(t), call("write", args())));
            assertTrue(ex.getMessage().contains("Tool \"write\" not found"));
        }

        @Test @DisplayName("10.3 multipleTools_selectsCorrect")
        void multipleTools_selectsCorrect() {
            Tool read = tool("read", "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}},\"required\":[\"path\"]}");
            Tool write = tool("write", "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"},\"content\":{\"type\":\"string\"}},\"required\":[\"path\",\"content\"]}");
            Tool delete = tool("delete", "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\"}},\"required\":[\"path\"]}");
            var r = ValidationUtils.validateToolCall(List.of(read, write, delete), call("write", args("path", "/tmp", "content", "hello")));
            assertEquals("/tmp", r.get("path"));
            assertEquals("hello", r.get("content"));
        }
    }
}
