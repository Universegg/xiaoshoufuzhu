package com.example.xiaoshoufuzhu.Inventory;

public class InventoryItem {
    private int id;
    private String name;
    private String batchNo;
    private int stock;
    private double price; // 添加单价字段

    public InventoryItem(int id, String name, String batchNo, int stock, double price) {
        this.id = id;
        this.name = name;
        this.batchNo = batchNo;
        this.stock = stock;
        this.price = price; // 初始化单价字段
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public int getStock() {
        return stock;
    }

    public double getPrice() {return price;}
}