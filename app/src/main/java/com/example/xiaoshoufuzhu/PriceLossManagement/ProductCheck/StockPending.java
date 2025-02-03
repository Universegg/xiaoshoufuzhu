package com.example.xiaoshoufuzhu.PriceLossManagement.ProductCheck;

public class StockPending {
    private int id;
    private int sid;
    private String name;
    private String num;
    private double price;
    private double quantity;
    private String state;

    public StockPending(int id, int sid, String name, String num, double price, double quantity, String state) {
        this.id = id;
        this.sid = sid;
        this.name = name;
        this.num = num;
        this.price = price;
        this.quantity = quantity;
        this.state = state;
    }

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
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
}