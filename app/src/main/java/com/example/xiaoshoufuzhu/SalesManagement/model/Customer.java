package com.example.xiaoshoufuzhu.SalesManagement.model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String address;
    private double amount;

    public Customer(int id, String name, String phone, String address, double amount) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}