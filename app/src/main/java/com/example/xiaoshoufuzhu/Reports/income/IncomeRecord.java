package com.example.xiaoshoufuzhu.Reports.income;

public class IncomeRecord {
    private String productName;
    private String productNum;
    private double totalIncome;
    private double accountReceivable;

    public IncomeRecord(String productName, String productNum, double totalIncome, double accountReceivable) {
        this.productName = productName;
        this.productNum = productNum;
        this.totalIncome = totalIncome;
        this.accountReceivable = accountReceivable;
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

    public double getAccountReceivable() {
        return accountReceivable;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public void setAccountReceivable(double accountReceivable) {
        this.accountReceivable = accountReceivable;
    }
}