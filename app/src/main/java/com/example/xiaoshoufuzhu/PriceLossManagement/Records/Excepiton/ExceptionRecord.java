package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Excepiton;

public class ExceptionRecord {
    private int id;
    private String name;
    private String num;
    private double quantity;
    private double price;
    private String date;
    private String reason;
    private int sid;
    private String supplierName; // 新增字段

    public ExceptionRecord(int id, String name, String num, double quantity, double price, String date, String reason, int sid, String supplierName) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
        this.reason = reason;
        this.sid = sid;
        this.supplierName = supplierName; // 新增字段
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getNum() { return num; }
    public double getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getDate() { return date; }
    public String getReason() { return reason; }
    public int getSid() { return sid; }
    public String getSupplierName() { return supplierName; } // 新增字段
}