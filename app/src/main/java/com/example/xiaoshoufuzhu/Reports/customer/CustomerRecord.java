package com.example.xiaoshoufuzhu.Reports.customer;

public class CustomerRecord {
    private String productName;
    private String batchNum;
    private double price;
    private double quantity;
    private String saleDate;
    private String state;
    private double totalPrice;

    public CustomerRecord(String productName, String batchNum, double price,
                          double quantity, String saleDate, String state, double totalPrice) {
        this.productName = productName;
        this.batchNum = batchNum;
        this.price = price;
        this.quantity = quantity;
        this.saleDate = saleDate;
        this.state = state;
        this.totalPrice = totalPrice;
    }

    // 新增getter方法
    public String getBatchNum() { return batchNum; }
    public double getPrice() { return price; }
    public String getSaleDate() { return saleDate; }
    public String getState() { return state; }

    // 保留原有getter
    public String getProductName() { return productName; }
    public double getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
}