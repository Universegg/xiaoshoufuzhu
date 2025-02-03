package com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck;

public class PendingInventory {
    private int id;
    private int productId;
    private String name;
    private String batchNumber;
    private double price;
    private double quantity;
    private String state;
    private String supplierName; // 新增的字段

    // 构造函数
    public PendingInventory(int id, int productId, String name, String batchNumber, double price, double quantity, String state, String supplierName) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.batchNumber = batchNumber;
        this.price = price;
        this.quantity = quantity;
        this.state = state;
        this.supplierName = supplierName; // 初始化新增的字段
    }

    // Getter 和 Setter 方法
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}