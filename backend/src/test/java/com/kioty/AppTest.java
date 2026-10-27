package com.kioty;

import com.kioty.algorithm.KnapsackOptimizer;
import com.kioty.model.Item;
import com.kioty.model.OptimizeResult;
import com.kioty.service.HistoryService;
import com.kioty.util.JsonUtil;
import com.kioty.util.Validator;

import java.util.*;

/**
 * Self-contained verification tests for KIOTY Java backend.
 */
public class AppTest {

    public static void main(String[] args) {
        System.out.println("Running KIOTY Java verification tests...\n");

        testKnapsack();
        testJsonUtil();
        testValidator();
        testHistoryService();

        System.out.println("\n🎉 ALL TESTS PASSED SUCCESSFULLY!");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected true, got false");
        }
    }

    private static void testKnapsack() {
        System.out.print("Testing KnapsackOptimizer... ");

        // Case 1: Empty items
        KnapsackOptimizer.Result r1 = KnapsackOptimizer.optimize(Collections.emptyList(), 100);
        assertEquals(0, r1.getSelectedItems().size(), "Empty items should return 0 items");
        assertEquals(0, r1.getTotalPrice(), "Total price should be 0");
        assertEquals(0, r1.getTotalValue(), "Total value should be 0");

        // Case 2: Zero or negative budget
        List<Item> items = Arrays.asList(
                new Item("Laptop", 500, 9),
                new Item("Mouse", 20, 4),
                new Item("Headphones", 80, 7)
        );
        KnapsackOptimizer.Result r2 = KnapsackOptimizer.optimize(items, 0);
        assertEquals(0, r2.getSelectedItems().size(), "Zero budget should return 0 items");

        // Case 3: Standard Knapsack
        // Budget = 100.
        // Laptop costs 500 -> cannot fit.
        // Mouse costs 20, value 4.
        // Headphones costs 80, value 7.
        // Both fit: 20 + 80 = 100 <= 100, total value = 11.
        KnapsackOptimizer.Result r3 = KnapsackOptimizer.optimize(items, 100);
        assertEquals(2, r3.getSelectedItems().size(), "Should select 2 items");
        assertEquals(100, r3.getTotalPrice(), "Total price should be 100");
        assertEquals(11, r3.getTotalValue(), "Total value should be 11");
        assertEquals("Mouse", r3.getSelectedItems().get(0).getName(), "First selected should be Mouse");
        assertEquals("Headphones", r3.getSelectedItems().get(1).getName(), "Second selected should be Headphones");

        // Case 4: Trade-off decision
        // Item A: price 60, value 10
        // Item B: price 50, value 7
        // Item C: price 50, value 7
        // Budget: 100 -> optimal is B + C (cost 100, value 14) rather than A (cost 60, value 10)
        List<Item> items2 = Arrays.asList(
                new Item("Item A", 60, 10),
                new Item("Item B", 50, 7),
                new Item("Item C", 50, 7)
        );
        KnapsackOptimizer.Result r4 = KnapsackOptimizer.optimize(items2, 100);
        assertEquals(2, r4.getSelectedItems().size(), "Should select Item B and Item C");
        assertEquals(100, r4.getTotalPrice(), "Total cost should be 100");
        assertEquals(14, r4.getTotalValue(), "Total value should be 14");

        System.out.println("PASSED ✅");
    }

    private static void testJsonUtil() {
        System.out.print("Testing JsonUtil... ");

        String json = "{\"budget\": 500, \"items\": [{\"name\": \"Bag\", \"price\": 200, \"value\": 8}], \"active\": true, \"note\": null}";
        Map<String, Object> map = JsonUtil.parseObject(json);

        assertEquals(500, map.get("budget"), "Parsed budget should be 500");
        assertEquals(true, map.get("active"), "Parsed active should be true");
        assertEquals(null, map.get("note"), "Parsed note should be null");

        List<?> list = (List<?>) map.get("items");
        assertEquals(1, list.size(), "Items list should have 1 item");

        // Test serialization
        Item item = new Item("Shoes", 150, 6);
        OptimizeResult res = new OptimizeResult("sess123", "2026-10-27T16:00:00", 500, Collections.singletonList(item), 150, 6);
        String serialized = JsonUtil.toJson(res);
        assertTrue(serialized.contains("\"sessionId\":\"sess123\""), "Serialized should contain sessionId");
        assertTrue(serialized.contains("\"totalPrice\":150"), "Serialized should contain totalPrice");
        assertTrue(serialized.contains("\"name\":\"Shoes\""), "Serialized should contain Shoes");

        System.out.println("PASSED ✅");
    }

    private static void testValidator() {
        System.out.print("Testing Validator... ");

        // Missing budget
        Map<String, Object> bad1 = new HashMap<>();
        bad1.put("items", Collections.singletonList(new HashMap<>()));
        assertEquals("Budget must be a positive integer.", Validator.validateInput(bad1), "Missing budget error");

        // Negative budget
        Map<String, Object> bad2 = new HashMap<>();
        bad2.put("budget", -50);
        bad2.put("items", Collections.singletonList(new HashMap<>()));
        assertEquals("Budget must be a positive integer.", Validator.validateInput(bad2), "Negative budget error");

        // Empty items
        Map<String, Object> bad3 = new HashMap<>();
        bad3.put("budget", 100);
        bad3.put("items", Collections.emptyList());
        assertEquals("Items must be a non-empty list.", Validator.validateInput(bad3), "Empty items error");

        // Missing fields in item
        Map<String, Object> badItem = new HashMap<>();
        badItem.put("name", "Watch");
        // missing price and value
        Map<String, Object> bad4 = new HashMap<>();
        bad4.put("budget", 100);
        bad4.put("items", Collections.singletonList(badItem));
        assertEquals("Item 1 is missing required fields (name, price, value).", Validator.validateInput(bad4), "Missing item field error");

        // Invalid price
        Map<String, Object> badItemPrice = new HashMap<>();
        badItemPrice.put("name", "Watch");
        badItemPrice.put("price", -10);
        badItemPrice.put("value", 5);
        Map<String, Object> bad5 = new HashMap<>();
        bad5.put("budget", 100);
        bad5.put("items", Collections.singletonList(badItemPrice));
        assertEquals("Item 'Watch' has an invalid price (must be a positive integer).", Validator.validateInput(bad5), "Negative price error");

        // Valid payload
        Map<String, Object> goodItem = new HashMap<>();
        goodItem.put("name", "Watch");
        goodItem.put("price", 50);
        goodItem.put("value", 8);
        Map<String, Object> good = new HashMap<>();
        good.put("budget", 100);
        good.put("items", Collections.singletonList(goodItem));
        assertEquals("", Validator.validateInput(good), "Valid input should return empty string");

        System.out.println("PASSED ✅");
    }

    private static void testHistoryService() {
        System.out.print("Testing HistoryService... ");

        String sid = HistoryService.generateSessionId();
        assertEquals(8, sid.length(), "Session ID should be 8 characters");

        OptimizeResult res = new OptimizeResult(
                sid,
                "2026-10-27T16:00:00",
                300,
                Collections.singletonList(new Item("Book", 50, 9)),
                50,
                9
        );

        HistoryService.saveSessionResult(sid, res);

        List<Object> history = HistoryService.getHistory("guest_user");
        assertTrue(!history.isEmpty(), "History should contain entries");

        System.out.println("PASSED ✅");
    }
}
