package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Stock;

public class StockRecord {
    private int id;
    private int sid;
    private String name;
    private String num;
    private double price;
    private double quantity;
    private String state;
    private String updatedAt;
    private String supplierName; // 新增字段

    public StockRecord(int id, int sid, String name, String num, double price, double quantity, String state, String updatedAt, String supplierName) {
        this.id = id;
        this.sid = sid;
        this.name = name;
        this.num = num;
        this.price = price;
        this.quantity = quantity;
        this.state = state;
        this.updatedAt = updatedAt;
        this.supplierName = supplierName; // 初始化字段
    }

    // Getters and setters
    public int getId() { return id; }
    public int getSid() { return sid; }
    public String getName() { return name; }
    public String getNum() { return num; }
    public double getPrice() { return price; }
    public double getQuantity() { return quantity; }
    public String getState() { return state; }
    public String getUpdatedAt() { return updatedAt; }
    public String getSupplierName() { return supplierName; } // 新增字段
}