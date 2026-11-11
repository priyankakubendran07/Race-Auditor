package com.raceauditor.model;

public class PriceListing {
    private int id;
    private int productId;
    private String marketplace;
    private double price;
    private String availability;
    private String productUrl;
    private boolean demo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getMarketplace() { return marketplace; }
    public void setMarketplace(String marketplace) { this.marketplace = marketplace; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public boolean isDemo() { return demo; }
    public void setDemo(boolean demo) { this.demo = demo; }
}
