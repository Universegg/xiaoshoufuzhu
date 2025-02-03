package com.example.xiaoshoufuzhu.PriceLossManagement.Records.Excepiton;

public class getSupplier {
    private int id;
    private String name;
    private String phone;
    private String address;

    public getSupplier(int id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}