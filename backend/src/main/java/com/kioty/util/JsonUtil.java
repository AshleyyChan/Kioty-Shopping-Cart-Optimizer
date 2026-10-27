package com.kioty.util;

import com.kioty.model.Item;
import com.kioty.model.OptimizeResult;

import java.util.*;

/**
 * Lightweight, zero-dependency JSON parser and serializer.
 * Supports Maps, Lists, primitives, strings, and KIOTY model classes.
 */
public class JsonUtil {

    // -------------------------------------------------------------
    // Parsing JSON -> Java Object (Map, List, String, Number, Boolean, null)
    // -------------------------------------------------------------

    public static Object parse(String json) {
        if (json == null) return null;
        JsonParser parser = new JsonParser(json);
        return parser.parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object result = parse(json);
        if (result instanceof Map) {
            return (Map<String, Object>) result;
        }
        throw new IllegalArgumentException("Expected JSON object, got: " + (result == null ? "null" : result.getClass().getSimpleName()));
    }

    private static class JsonParser {
        private final String src;
        private int pos = 0;
        private final int len;

        public JsonParser(String src) {
            this.src = src;
            this.len = src.length();
        }

        public Object parseValue() {
            skipWhitespace();
            if (pos >= len) return null;

            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"' || c == '\'') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();

            throw new IllegalArgumentException("Unexpected character at pos " + pos + ": " + c);
        }

        private void skipWhitespace() {
            while (pos < len) {
                char c = src.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();

            if (peek() == '}') {
                pos++;
                return map;
            }

            while (pos < len) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object val = parseValue();
                map.put(key, val);

                skipWhitespace();
                char c = peek();
                if (c == ',') {
                    pos++;
                } else if (c == '}') {
                    pos++;
                    break;
                } else {
                    throw new IllegalArgumentException("Expected ',' or '}' in object at pos " + pos);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWhitespace();

            if (peek() == ']') {
                pos++;
                return list;
            }

            while (pos < len) {
                Object val = parseValue();
                list.add(val);

                skipWhitespace();
                char c = peek();
                if (c == ',') {
                    pos++;
                } else if (c == ']') {
                    pos++;
                    break;
                } else {
                    throw new IllegalArgumentException("Expected ',' or ']' in array at pos " + pos);
                }
            }
            return list;
        }

        private String parseString() {
            char quote = src.charAt(pos);
            if (quote != '"' && quote != '\'') {
                throw new IllegalArgumentException("Expected string quote at pos " + pos);
            }
            pos++;
            StringBuilder sb = new StringBuilder();

            while (pos < len) {
                char c = src.charAt(pos++);
                if (c == quote) {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (pos >= len) break;
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\'': sb.append('\''); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 <= len) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            break;
                        default:
                            sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new IllegalArgumentException("Unterminated string starting around pos " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') pos++;
            while (pos < len && Character.isDigit(src.charAt(pos))) pos++;

            boolean isFloating = false;
            if (pos < len && src.charAt(pos) == '.') {
                isFloating = true;
                pos++;
                while (pos < len && Character.isDigit(src.charAt(pos))) pos++;
            }

            if (pos < len && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                isFloating = true;
                pos++;
                if (pos < len && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
                while (pos < len && Character.isDigit(src.charAt(pos))) pos++;
            }

            String numStr = src.substring(start, pos);
            if (isFloating) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean literal at pos " + pos);
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null literal at pos " + pos);
        }

        private void expect(char expected) {
            skipWhitespace();
            if (pos >= len || src.charAt(pos) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' at pos " + pos);
            }
            pos++;
        }

        private char peek() {
            return pos < len ? src.charAt(pos) : '\0';
        }
    }

    // -------------------------------------------------------------
    // Serialization Java Object -> JSON String
    // -------------------------------------------------------------

    public static String toJson(Object obj) {
        return toJson(obj, 0);
    }

    public static String toPrettyJson(Object obj) {
        return toJson(obj, 2);
    }

    private static String toJson(Object obj, int indentLevel) {
        StringBuilder sb = new StringBuilder();
        serializeValue(obj, sb, indentLevel, 0);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void serializeValue(Object obj, StringBuilder sb, int indentStep, int currentIndent) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            escapeString((String) obj, sb);
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Item) {
            Item item = (Item) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", item.getName());
            map.put("price", item.getPrice());
            map.put("value", item.getValue());
            serializeMap(map, sb, indentStep, currentIndent);
        } else if (obj instanceof OptimizeResult) {
            OptimizeResult res = (OptimizeResult) obj;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sessionId", res.getSessionId());
            map.put("timestamp", res.getTimestamp());
            map.put("budget", res.getBudget());
            map.put("selectedItems", res.getSelectedItems());
            map.put("totalPrice", res.getTotalPrice());
            map.put("totalValue", res.getTotalValue());
            map.put("count", res.getCount());
            serializeMap(map, sb, indentStep, currentIndent);
        } else if (obj instanceof Map) {
            serializeMap((Map<String, Object>) obj, sb, indentStep, currentIndent);
        } else if (obj instanceof Collection) {
            serializeCollection((Collection<?>) obj, sb, indentStep, currentIndent);
        } else if (obj.getClass().isArray()) {
            serializeCollection(Arrays.asList((Object[]) obj), sb, indentStep, currentIndent);
        } else {
            escapeString(obj.toString(), sb);
        }
    }

    private static void serializeMap(Map<String, ?> map, StringBuilder sb, int indentStep, int currentIndent) {
        if (map.isEmpty()) {
            sb.append("{}");
            return;
        }

        sb.append("{");
        boolean pretty = indentStep > 0;
        int nextIndent = currentIndent + indentStep;
        boolean first = true;

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            if (pretty) {
                sb.append("\n").append(" ".repeat(nextIndent));
            }
            escapeString(entry.getKey(), sb);
            sb.append(pretty ? ": " : ":");
            serializeValue(entry.getValue(), sb, indentStep, nextIndent);
            first = false;
        }

        if (pretty) {
            sb.append("\n").append(" ".repeat(currentIndent));
        }
        sb.append("}");
    }

    private static void serializeCollection(Collection<?> col, StringBuilder sb, int indentStep, int currentIndent) {
        if (col.isEmpty()) {
            sb.append("[]");
            return;
        }

        sb.append("[");
        boolean pretty = indentStep > 0;
        int nextIndent = currentIndent + indentStep;
        boolean first = true;

        for (Object item : col) {
            if (!first) {
                sb.append(",");
            }
            if (pretty) {
                sb.append("\n").append(" ".repeat(nextIndent));
            }
            serializeValue(item, sb, indentStep, nextIndent);
            first = false;
        }

        if (pretty) {
            sb.append("\n").append(" ".repeat(currentIndent));
        }
        sb.append("]");
    }

    private static void escapeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
