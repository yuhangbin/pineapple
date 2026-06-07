# ADR 0001: JSON Schema Validation for Tool Call Arguments

## Status

Accepted

## Context

The agent loop receives tool call arguments from LLM responses as `Map<String, Object>`. These arguments must be validated against the tool's declared JSON Schema before execution, because:

- LLM output is unreliable — may omit required fields, return wrong types, or include unknown fields
- Tool implementations expect correctly typed parameters
- Early validation provides clear error messages back to the LLM for retry

The TypeScript implementation uses TypeBox (`@sinclair/typebox`) with `Value.Convert` for type coercion and schema validation. We need a Java equivalent.

## Decision

Use **Jackson `JsonNode`** as the tree model with **`networknt/json-schema-validator`** for JSON Schema validation.

### Why `networknt/json-schema-validator`

| Library | Draft Support | Active Maintenance | Ecosystem |
|---|---|---|---|
| `networknt/json-schema-validator` | 4, 6, 7, 2019-09, **2020-12** | Active | Jackson-based, standalone |
| `everit-org/json-schema` | 4, 6, 7 | Stale | No 2020-12 |
| Vert.x `schema-json-validator` | 4, 7 | Active | Tied to Vert.x |

OpenAI and Anthropic tool schemas may use JSON Schema 2020-12 features (e.g. `prefixItems`), so 2020-12 support is required.

### Why `JsonNode` (Jackson)

The validator API requires `JsonNode` as input — it traverses both schema and instance as structured JSON nodes. The conversion flow is:

```
Map<String, Object> args → JsonUtils.valueToTree() → JsonNode → validate → Map<String, Object>
```

This means a serialization/deserialization round-trip per validation call. This cost is acceptable because:

- Tool call frequency is low relative to LLM API latency (ms vs seconds)
- Schema parsing can be cached per tool (the `JsonSchema` object is reusable)

### Why not manual validation

Manual type/required-field checks would avoid the Jackson dependency but would mean reimplementing JSON Schema validation — complex, error-prone, and incomplete. Jackson is already a de facto standard in Java projects.

## Consequences

- **Positive:** Full JSON Schema compliance with minimal code, supports all draft versions, clear error messages
- **Negative:** Additional dependency on Jackson and networknt; double serialization cost per validation (negligible in context)
- **Mitigation:** `JsonUtils` centralizes all Jackson usage so the rest of the codebase never touches `ObjectMapper` directly

### TODO

- Cache `JsonSchema` instances per tool to avoid re-parsing the schema string on every call
- Add type coercion (equivalent to TS `Value.Convert`) if needed for LLM output tolerance
