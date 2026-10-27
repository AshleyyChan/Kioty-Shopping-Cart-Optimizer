package com.kioty.algorithm;

import com.kioty.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 0/1 Knapsack Dynamic Programming Optimizer.
 * Optimizes selected items to maximize total value within a given budget.
 */
public class KnapsackOptimizer {

    public static class Result {
        private final List<Item> selectedItems;
        private final int totalPrice;
        private final int totalValue;

        public Result(List<Item> selectedItems, int totalPrice, int totalValue) {
            this.selectedItems = selectedItems;
            this.totalPrice = totalPrice;
            this.totalValue = totalValue;
        }

        public List<Item> getSelectedItems() {
            return selectedItems;
        }

        public int getTotalPrice() {
            return totalPrice;
        }

        public int getTotalValue() {
            return totalValue;
        }
    }

    /**
     * Solves the 0/1 knapsack problem for the given items and budget limit.
     *
     * @param items  List of available items with price and value
     * @param budget Maximum allowable total price (budget)
     * @return Result containing selected items, total price, and total value
     */
    public static Result optimize(List<Item> items, int budget) {
        if (items == null || items.isEmpty() || budget <= 0) {
            return new Result(Collections.emptyList(), 0, 0);
        }

        int n = items.size();
        int[][] dp = new int[n + 1][budget + 1];

        // Bottom-up DP table filling
        for (int i = 1; i <= n; i++) {
            Item item = items.get(i - 1);
            int itemPrice = item.getPrice();
            int itemValue = item.getValue();

            for (int b = 1; b <= budget; b++) {
                if (itemPrice <= b) {
                    dp[i][b] = Math.max(dp[i - 1][b], dp[i - 1][b - itemPrice] + itemValue);
                } else {
                    dp[i][b] = dp[i - 1][b];
                }
            }
        }

        // Backtracking to find selected items
        List<Item> selected = new ArrayList<>();
        int b = budget;
        for (int i = n; i > 0; i--) {
            if (dp[i][b] != dp[i - 1][b]) {
                Item item = items.get(i - 1);
                selected.add(item);
                b -= item.getPrice();
            }
        }

        // Reverse to maintain original order of appearance
        Collections.reverse(selected);

        int totalPrice = 0;
        for (Item item : selected) {
            totalPrice += item.getPrice();
        }
        int totalValue = dp[n][budget];

        return new Result(selected, totalPrice, totalValue);
    }
}
