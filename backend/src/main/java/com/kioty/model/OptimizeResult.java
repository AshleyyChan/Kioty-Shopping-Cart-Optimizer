package com.kioty.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the result of a shopping cart optimization session.
 */
public class OptimizeResult {
    private String sessionId;
    private String timestamp;
    private int budget;
    private List<Item> selectedItems = new ArrayList<>();
    private int totalPrice;
    private int totalValue;
    private int count;

    public OptimizeResult() {
    }

    public OptimizeResult(String sessionId, String timestamp, int budget, List<Item> selectedItems, int totalPrice, int totalValue) {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.budget = budget;
        this.selectedItems = selectedItems != null ? selectedItems : new ArrayList<>();
        this.totalPrice = totalPrice;
        this.totalValue = totalValue;
        this.count = this.selectedItems.size();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getBudget() {
        return budget;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public List<Item> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<Item> selectedItems) {
        this.selectedItems = selectedItems;
        this.count = selectedItems != null ? selectedItems.size() : 0;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(int totalValue) {
        this.totalValue = totalValue;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
