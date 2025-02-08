package com.example.xiaoshoufuzhu.Reports.expense;

public class ExpenseRecord {
    private String productName;
    private String batchNumber;
    private double totalExpense;
    private double freight;

    public ExpenseRecord(String productName, String batchNumber,
                         double totalExpense, double freight) {
        this.productName = productName;
        this.batchNumber = batchNumber;
        this.totalExpense = totalExpense;
        this.freight = freight;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getBatchNumber() { return batchNumber; }
    public double getTotalExpense() { return totalExpense; }
    public double getFreight() { return freight; }
}