package com.example.xiaoshoufuzhu.Reports.ProductLoss;

public class ProductLossRecord {
    private String productName;
    private String batchNumber;
    private double lossQuantity;
    private double lossAmount;

    public ProductLossRecord(String productName, String batchNumber, double lossQuantity, double lossAmount) {
        this.productName = productName;
        this.batchNumber = batchNumber;
        this.lossQuantity = lossQuantity;
        this.lossAmount = lossAmount;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getBatchNumber() { return batchNumber; }
    public double getLossQuantity() { return lossQuantity; }
    public double getLossAmount() { return lossAmount; }
}