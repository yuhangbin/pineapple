package com.cboy.pineapple.ai.utils;

import com.cboy.pineapple.ai.types.content.ToolCall;
import com.cboy.pineapple.ai.types.tool.Tool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates tool call arguments against JSON Schema, with type coercion
 * for plain JSON Schema primitives.
 */
public final class ValidationService {

    private static final JsonSchemaFactory SCHEMA_FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    private ValidationService() {}

    /**
     * Validates tool call arguments against the tool's JSON Schema.
     * Applies type coercion for plain JSON schemas.
     *
     * @param tool     the tool definition with JSON Schema parameters
     * @param toolCall the tool call from the LLM
     * @return the validated (and potentially coerced) arguments
     * @throws IllegalArgumentException if validation fails, with formatted error message
     */
    public static Map<String, Object> validateToolArguments(Tool tool, ToolCall toolCall) {
        Map<String, Object> args = deepCopyArguments(toolCall.arguments());

        String schemaJson = tool.parameters();
        if (schemaJson == null || schemaJson.isBlank()) {
            return args;
        }

        JsonNode schemaNode = JsonUtils.readTree(schemaJson);
        JsonNode argsNode = JsonUtils.valueToTree(args);

        // Apply coercion before validation
        JsonNode coerced = coerceWithSchema(argsNode, schemaNode);

        // Validate the coerced result
        JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
        Set<ValidationMessage> errors = schema.validate(coerced);

        if (errors.isEmpty()) {
            return JsonUtils.treeToMap(coerced);
        }

        String formattedErrors = errors.stream()
                .map(e -> "  - " + e.getInstanceLocation() + ": " + e.getMessage())
                .collect(Collectors.joining("\n"));

        String originalArgsJson = JsonUtils.toPrettyJson(toolCall.arguments());
        throw new IllegalArgumentException(
                "Validation failed for tool \"" + toolCall.name() + "\":\n" + formattedErrors
                        + "\n\nReceived arguments:\n" + originalArgsJson);
    }

    /**
     * Looks up a tool by name and validates its arguments.
     *
     * @param tools    available tools
     * @param toolCall the tool call from the LLM
     * @return the validated (and potentially coerced) arguments
     * @throws IllegalArgumentException if tool not found or validation fails
     */
    public static Map<String, Object> validateToolCall(List<Tool> tools, ToolCall toolCall) {
        Tool tool = tools.stream()
                .filter(t -> t.name().equals(toolCall.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tool \"" + toolCall.name() + "\" not found"));
        return validateToolArguments(tool, toolCall);
    }

    // ── Coercion ────────────────────────────────────────────────────

    /**
     * Recursively walks the schema tree and coerces leaf values by declared type.
     * Mirrors the TypeScript {@code coerceWithJsonSchema} function.
     */
    static JsonNode coerceWithSchema(JsonNode value, JsonNode schema) {
        if (value == null || schema == null) {
            return value;
        }

        // Handle union types (type: ["number", "string"])
        JsonNode typeNode = schema.get("type");
        if (typeNode != null && typeNode.isArray()) {
            return coerceUnionType(value, (ArrayNode) typeNode, schema);
        }

        // Handle single type
        if (typeNode != null && typeNode.isTextual()) {
            String type = typeNode.asText();
            return switch (type) {
                case "number" -> coerceToNumber(value);
                case "integer" -> coerceToInteger(value);
                case "boolean" -> coerceToBoolean(value);
                case "string" -> coerceToString(value);
                case "null" -> coerceToNull(value);
                case "object" -> coerceObject(value, schema);
                case "array" -> coerceArray(value, schema);
                default -> value;
            };
        }

        // No explicit type — check composite keywords first, then properties
        if (schema.has("allOf")) {
            return coerceAllOf(value, schema.get("allOf"));
        }
        if (schema.has("anyOf")) {
            return coerceAnyOf(value, schema.get("anyOf"));
        }
        if (schema.has("oneOf")) {
            return coerceOneOf(value, schema.get("oneOf"));
        }

        // No explicit type — recurse into properties if present
        if (value.isObject() && schema.has("properties")) {
            return coerceObject(value, schema);
        }

        return value;
    }

    private static JsonNode coerceUnionType(JsonNode value, ArrayNode types, JsonNode schema) {
        // If value already matches any type natively, return as-is
        for (JsonNode typeNode : types) {
            if (isTypeMatch(value, typeNode.asText())) {
                return value;
            }
        }
        // Otherwise try coercing to each type in order; first successful match wins
        for (JsonNode typeNode : types) {
            String type = typeNode.asText();
            JsonNode coerced = switch (type) {
                case "number" -> coerceToNumber(value);
                case "integer" -> coerceToInteger(value);
                case "boolean" -> coerceToBoolean(value);
                case "string" -> coerceToString(value);
                case "null" -> coerceToNull(value);
                default -> value;
            };
            if (coerced != value && isTypeMatch(coerced, type)) {
                return coerced;
            }
        }
        return value;
    }

    private static boolean isTypeMatch(JsonNode value, String type) {
        return switch (type) {
            case "number" -> value.isNumber();
            case "integer" -> value.isInt();
            case "boolean" -> value.isBoolean();
            case "string" -> value.isTextual();
            case "null" -> value.isNull();
            case "object" -> value.isObject();
            case "array" -> value.isArray();
            default -> true;
        };
    }

    // ── Leaf coercion ───────────────────────────────────────────────

    static JsonNode coerceToNumber(JsonNode value) {
        if (value.isNumber()) return value;
        if (value.isBoolean()) return value.booleanValue() ? DoubleNode.valueOf(1.0) : DoubleNode.valueOf(0.0);
        if (value.isNull()) return DoubleNode.valueOf(0.0);
        if (value.isTextual()) {
            try {
                return DoubleNode.valueOf(Double.parseDouble(value.asText()));
            } catch (NumberFormatException e) {
                return value;
            }
        }
        return value;
    }

    static JsonNode coerceToInteger(JsonNode value) {
        if (value.isInt()) return value;
        if (value.isNumber()) return IntNode.valueOf(value.intValue());
        if (value.isBoolean()) return value.booleanValue() ? IntNode.valueOf(1) : IntNode.valueOf(0);
        if (value.isNull()) return IntNode.valueOf(0);
        if (value.isTextual()) {
            try {
                String text = value.asText();
                double d = Double.parseDouble(text);
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return IntNode.valueOf((int) d);
                }
                return value; // not a clean integer string
            } catch (NumberFormatException e) {
                return value;
            }
        }
        return value;
    }

    static JsonNode coerceToBoolean(JsonNode value) {
        if (value.isBoolean()) return value;
        if (value.isInt()) return BooleanNode.valueOf(value.intValue() == 1);
        if (value.isNull()) return BooleanNode.FALSE;
        if (value.isTextual()) {
            String text = value.asText();
            if ("true".equalsIgnoreCase(text)) return BooleanNode.TRUE;
            if ("false".equalsIgnoreCase(text)) return BooleanNode.FALSE;
            return value; // "1" and "0" are NOT valid boolean coercions
        }
        return value;
    }

    static JsonNode coerceToString(JsonNode value) {
        if (value.isTextual()) return value;
        if (value.isNull()) return TextNode.valueOf("");
        if (value.isBoolean()) return TextNode.valueOf(String.valueOf(value.booleanValue()));
        if (value.isNumber()) return TextNode.valueOf(String.valueOf(value.numberValue()));
        return value;
    }

    static JsonNode coerceToNull(JsonNode value) {
        if (value.isNull()) return NullNode.getInstance();
        if (value.isTextual() && value.asText().isEmpty()) return NullNode.getInstance();
        if (value.isInt() && value.intValue() == 0) return NullNode.getInstance();
        if (value.isBoolean() && !value.booleanValue()) return NullNode.getInstance();
        return value;
    }

    // ── Composite coercion ──────────────────────────────────────────

    private static JsonNode coerceObject(JsonNode value, JsonNode schema) {
        if (!value.isObject()) return value;

        ObjectNode result = (ObjectNode) value;

        // Coerce declared properties
        if (schema.has("properties")) {
            JsonNode properties = schema.get("properties");
            for (Map.Entry<String, JsonNode> prop : properties.properties()) {
                if (result.has(prop.getKey())) {
                    JsonNode coerced = coerceWithSchema(result.get(prop.getKey()), prop.getValue());
                    if (coerced != result.get(prop.getKey())) {
                        result.set(prop.getKey(), coerced);
                    }
                }
            }
        }

        // Handle allOf: merge properties from sub-schemas and coerce
        if (schema.has("allOf")) {
            for (JsonNode subSchema : schema.get("allOf")) {
                if (subSchema.has("properties")) {
                    JsonNode properties = subSchema.get("properties");
                    for (Map.Entry<String, JsonNode> prop : properties.properties()) {
                        if (result.has(prop.getKey())) {
                            JsonNode coerced = coerceWithSchema(result.get(prop.getKey()), prop.getValue());
                            if (coerced != result.get(prop.getKey())) {
                                result.set(prop.getKey(), coerced);
                            }
                        }
                    }
                }
            }
        }

        // Coerce additionalProperties
        if (schema.has("additionalProperties") && schema.get("additionalProperties").isObject()) {
            JsonNode addPropSchema = schema.get("additionalProperties");
            Set<String> declaredProps = schema.has("properties")
                    ? schema.get("properties").properties().stream().map(Map.Entry::getKey).collect(Collectors.toSet())
                    : Set.of();
            for (Map.Entry<String, JsonNode> field : result.properties()) {
                if (!declaredProps.contains(field.getKey())) {
                    JsonNode coerced = coerceWithSchema(field.getValue(), addPropSchema);
                    if (coerced != field.getValue()) {
                        result.set(field.getKey(), coerced);
                    }
                }
            }
        }

        return result;
    }

    private static JsonNode coerceArray(JsonNode value, JsonNode schema) {
        if (!value.isArray()) return value;

        ArrayNode result = (ArrayNode) value;
        boolean modified = false;

        // Draft 2020-12: prefixItems for tuple validation
        JsonNode prefixItems = schema.get("prefixItems");
        if (prefixItems != null && prefixItems.isArray()) {
            for (int i = 0; i < Math.min(result.size(), prefixItems.size()); i++) {
                JsonNode coerced = coerceWithSchema(result.get(i), prefixItems.get(i));
                if (coerced != result.get(i)) {
                    result.set(i, coerced);
                    modified = true;
                }
            }
        }

        // items applies to all remaining/individual elements
        JsonNode itemsSchema = schema.get("items");
        if (itemsSchema != null && itemsSchema.isObject()) {
            int start = prefixItems != null ? prefixItems.size() : 0;
            for (int i = start; i < result.size(); i++) {
                JsonNode coerced = coerceWithSchema(result.get(i), itemsSchema);
                if (coerced != result.get(i)) {
                    result.set(i, coerced);
                    modified = true;
                }
            }
        }

        return result;
    }

    // ── Composite schema keywords ───────────────────────────────────

    private static JsonNode coerceAllOf(JsonNode value, JsonNode allOfSchemas) {
        // Merge all sub-schemas' properties into one virtual schema, then coerce once
        ObjectNode merged = JsonNodeFactory.instance.objectNode();
        merged.put("type", "object");
        ObjectNode mergedProps = JsonNodeFactory.instance.objectNode();
        for (JsonNode sub : allOfSchemas) {
            if (sub.has("properties")) {
                for (Map.Entry<String, JsonNode> prop : sub.get("properties").properties()) {
                    mergedProps.set(prop.getKey(), prop.getValue());
                }
            }
        }
        merged.set("properties", mergedProps);
        return coerceWithSchema(value, merged);
    }

    private static JsonNode coerceAnyOf(JsonNode value, JsonNode anyOfSchemas) {
        for (JsonNode subSchema : anyOfSchemas) {
            JsonNode coerced = coerceWithSchema(value, subSchema);
            if (coerced == value || !coerced.equals(value)) {
                // Attempt: see if the coerced value validates against this branch
                return coerced;
            }
        }
        return value;
    }

    private static JsonNode coerceOneOf(JsonNode value, JsonNode oneOfSchemas) {
        // If value already matches a branch, return as-is
        for (JsonNode subSchema : oneOfSchemas) {
            JsonNode typeNode = subSchema.get("type");
            if (typeNode != null && typeNode.isTextual() && isTypeMatch(value, typeNode.asText())) {
                return value;
            }
        }
        // Try coercion to each branch; first successful match wins
        for (JsonNode subSchema : oneOfSchemas) {
            JsonNode coerced = coerceWithSchema(value, subSchema);
            if (coerced != value) {
                return coerced;
            }
        }
        return value;
    }

    // ── Deep copy ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static Map<String, Object> deepCopyArguments(Map<String, Object> args) {
        if (args == null) return Map.of();
        Map<String, Object> copy = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            copy.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return copy;
    }

    private static Object deepCopyValue(Object value) {
        if (value instanceof Map) {
            return deepCopyArguments((Map<String, Object>) value);
        } else if (value instanceof List) {
            List<Object> listCopy = new java.util.ArrayList<>();
            for (Object item : (List<?>) value) {
                listCopy.add(deepCopyValue(item));
            }
            return listCopy;
        }
        return value;
    }
}
