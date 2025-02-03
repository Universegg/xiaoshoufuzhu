package com.example.xiaoshoufuzhu.SupplierSalesManagement.model;

public class PurchaseRecord {
    private int id;
    private String productName; // 对应 name 字段
    private String batchNo;     // 对应 num 字段
    private int quantity;
    private double price;
    private String purchaseDate;
    private double totalPrice;

    // 修改构造函数（移除 productId 参数，新增 productName 和 batchNo）
    public PurchaseRecord(int id, String productName, String batchNo,
                          int quantity, double price, String purchaseDate,
                          double totalPrice) {
        this.id = id;
        this.productName = productName;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.totalPrice = totalPrice;
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
}