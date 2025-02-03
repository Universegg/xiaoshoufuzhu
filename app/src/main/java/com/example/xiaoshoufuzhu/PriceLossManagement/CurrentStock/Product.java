package com.example.xiaoshoufuzhu.PriceLossManagement.CurrentStock;

public class Product {
    private int id;
    private String name;
    private double price;
    private double stock;
    private String num;

    public Product(int id, String name, double price, double stock, String num) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.num = num;
    }

    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}