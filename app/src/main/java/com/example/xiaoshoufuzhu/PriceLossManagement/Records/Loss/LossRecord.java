package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Loss;

public class LossRecord {
    private int id;
    private int productId;
    private double quantity;
    private String lossReason;
    private String lossDate;
    private double lossAmount;
    private String productName;
    private String batchNumber;

    public LossRecord(int id, int productId, double quantity, String lossReason, String lossDate, double lossAmount, String productName, String batchNumber) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.lossReason = lossReason;
        this.lossDate = lossDate;
        this.lossAmount = lossAmount;
        this.productName = productName;
        this.batchNumber = batchNumber;
    }

    // Getter and setter methods

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getLossReason() {
        return lossReason;
    }

    public void setLossReason(String lossReason) {
        this.lossReason = lossReason;
    }

    public String getLossDate() {
        return lossDate;
    }

    public void setLossDate(String lossDate) {
        this.lossDate = lossDate;
    }

    public double getLossAmount() {
        return lossAmount;
    }

    public void setLossAmount(double lossAmount) {
        this.lossAmount = lossAmount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
}