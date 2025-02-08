package com.example.xiaoshoufuzhu.SupplierSalesManagement.model;

public class PurchaseRecord {
    private int id;
    private String productName; // 对应 name 字段
    private String batchNo;     // 对应 num 字段
    private int quantity;
    private double price;
    private String purchaseDate;
    private double totalPrice;
    private double freight; // 新增运费字段

    public PurchaseRecord(int id, String productName, String batchNo,
                          int quantity, double price, String purchaseDate,
                          double totalPrice, double freight) { // 修改构造方法
        this.id = id;
        this.productName = productName;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.totalPrice = totalPrice;
        this.freight = freight; // 初始化运费字段
    }

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getFreight() { // 新增运费字段的getter方法
        return freight;
    }
}