package com.raceauditor.model;

public class Category {
    private final String category;
    private final long count;
    private final double total;

    public Category(String category, long count, double total) {
        this.category = category;
        this.count = count;
        this.total = total;
    }

    public String getCategory() { return category; }
    public long getCount() { return count; }
    public double getTotal() { return total; }
}
