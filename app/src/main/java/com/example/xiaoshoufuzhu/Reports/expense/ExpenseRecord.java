package com.example.xiaoshoufuzhu.Reports.expense;

public class ExpenseRecord {
    private String productName;
    private String productNum;
    private double totalExpense;

    public ExpenseRecord(String productName, String productNum, double totalExpense) {
        this.productName = productName;
        this.productNum = productNum;
        this.totalExpense = totalExpense;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductNum() {
        return productNum;
    }

    public double getTotalExpense() {
        return totalExpense;
    }
}