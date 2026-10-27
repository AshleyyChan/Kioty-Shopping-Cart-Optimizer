package com.kioty.util;

import com.kioty.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates incoming request payloads for shopping cart optimization.
 */
public class Validator {

    /**
     * Validates raw parsed JSON data map.
     *
     * @param data Map representing the JSON payload
     * @return Error message string if invalid, or empty string "" if valid.
     */
    @SuppressWarnings("unchecked")
    public static String validateInput(Map<String, Object> data) {
        if (data == null || !data.containsKey("budget") || !isPositiveInteger(data.get("budget"))) {
            return "Budget must be a positive integer.";
        }

        if (!data.containsKey("items") || !(data.get("items") instanceof List)) {
            return "Items must be a non-empty list.";
        }

        List<?> itemsList = (List<?>) data.get("items");
        if (itemsList.isEmpty()) {
            return "Items must be a non-empty list.";
        }

        for (int i = 0; i < itemsList.size(); i++) {
            Object obj = itemsList.get(i);
            if (!(obj instanceof Map)) {
                return "Item " + (i + 1) + " is missing required fields (name, price, value).";
            }

            Map<String, Object> itemMap = (Map<String, Object>) obj;
            if (!itemMap.containsKey("name") || !itemMap.containsKey("price") || !itemMap.containsKey("value")) {
                return "Item " + (i + 1) + " is missing required fields (name, price, value).";
            }

            Object nameObj = itemMap.get("name");
            if (!(nameObj instanceof String) || ((String) nameObj).trim().isEmpty()) {
                return "Item " + (i + 1) + " has an invalid or empty name.";
            }

            String itemName = ((String) nameObj).trim();

            Object priceObj = itemMap.get("price");
            if (!isPositiveInteger(priceObj)) {
                return "Item '" + itemName + "' has an invalid price (must be a positive integer).";
            }

            Object valueObj = itemMap.get("value");
            if (!isPositiveInteger(valueObj)) {
                return "Item '" + itemName + "' has an invalid value (must be a positive integer).";
            }
        }

        return "";
    }

    /**
     * Checks if an object represents a positive whole integer (> 0).
     */
    public static boolean isPositiveInteger(Object val) {
        if (val instanceof Integer) {
            return (Integer) val > 0;
        }
        if (val instanceof Long) {
            return (Long) val > 0 && (Long) val <= Integer.MAX_VALUE;
        }
        if (val instanceof Double) {
            double d = (Double) val;
            return d > 0 && d == Math.floor(d) && d <= Integer.MAX_VALUE;
        }
        if (val instanceof Float) {
            float f = (Float) val;
            return f > 0 && f == Math.floor(f) && f <= Integer.MAX_VALUE;
        }
        if (val instanceof String) {
            try {
                int parsed = Integer.parseInt(((String) val).trim());
                return parsed > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Safely extracts an integer from a number object.
     */
    public static int toInt(Object val) {
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            return Integer.parseInt(((String) val).trim());
        }
        throw new IllegalArgumentException("Cannot convert " + val + " to integer.");
    }

    /**
     * Extracts a list of Item models from validated items array.
     */
    @SuppressWarnings("unchecked")
    public static List<Item> extractItems(List<?> itemsList) {
        List<Item> items = new ArrayList<>();
        for (Object obj : itemsList) {
            Map<String, Object> map = (Map<String, Object>) obj;
            String name = ((String) map.get("name")).trim();
            int price = toInt(map.get("price"));
            int value = toInt(map.get("value"));
            items.add(new Item(name, price, value));
        }
        return items;
    }
}
