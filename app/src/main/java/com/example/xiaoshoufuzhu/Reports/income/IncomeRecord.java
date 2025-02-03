package com.example.xiaoshoufuzhu.Reports.income;

public class IncomeRecord {
    private String productName;
    private String productNum;
    private double totalIncome;

    public IncomeRecord(String productName, String productNum, double totalIncome) {
        this.productName = productName;
        this.productNum = productNum;
        this.totalIncome = totalIncome;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductNum() {
        return productNum;
    }

    public double getTotalIncome() {
        return totalIncome;
    }
}