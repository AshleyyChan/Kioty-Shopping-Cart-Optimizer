package com.kioty.model;

import java.util.Objects;

/**
 * Represents an item in the shopping cart with name, price, and value/importance.
 */
public class Item {
    private String name;
    private int price;
    private int value;

    public Item() {
    }

    public Item(String name, int price, int value) {
        this.name = name;
        this.price = price;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return price == item.price && value == item.value && Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, value);
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", value=" + value +
                '}';
    }
}