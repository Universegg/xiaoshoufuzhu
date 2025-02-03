package com.example.xiaoshoufuzhu.SalesManagement.model;

public class SalesRecord {
    private int id;
    private int productId;
    private int cid;  // 修改 customerId 为 cid
    private int quantity;
    private String saleDate;
    private double totalPrice;
    private String productName;
    private String batchNo;
    private double price;
    private String state;
    private double actualAmount; // 实收金额
    private double receivableAmount; // 应收金额

    public SalesRecord(int id, int productId, int cid, int quantity, String saleDate, double totalPrice,
                       String productName, String batchNo, double price, String state) {
        this.id = id;
        this.productId = productId;
        this.cid = cid;  // 初始化 cid
        this.quantity = quantity;
        this.saleDate = saleDate;
        this.totalPrice = totalPrice;
        this.productName = productName;
        this.batchNo = batchNo;
        this.price = price;
        this.state = state;
        this.actualAmount = totalPrice;
        this.receivableAmount = quantity * price;
    }

    public int getCid() {
        return cid;
    }

    public double getActualAmount() {
        return actualAmount;
    }

    public double getReceivableAmount() {
        return receivableAmount;
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getProductName() {
        return productName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public double getPrice() {
        return price;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}