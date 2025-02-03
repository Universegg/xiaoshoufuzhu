package com.example.xiaoshoufuzhu.SalesManagement.model;

public class RetailSales {
    private int id;
    private int productId;
    private int quantity;
    private String saleDate;
    private double totalPrice;
    private String productName;
    private String batchNo;
    private double unitPrice;  // 新增字段

    public RetailSales(int id, int productId, int quantity, String saleDate, double totalPrice, String productName, String batchNo, double unitPrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.saleDate = saleDate;
        this.totalPrice = totalPrice;
        this.productName = productName;
        this.batchNo = batchNo;
        this.unitPrice = unitPrice;  // 设置单价
    }

    // Getters and setters for all fields
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}