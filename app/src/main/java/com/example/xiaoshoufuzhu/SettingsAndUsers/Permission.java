package com.example.xiaoshoufuzhu.SettingsAndUsers;

import java.sql.SQLException;
import android.util.Log;

import java.sql.ResultSet;

public class Permission {
    private boolean priceMgr;
    private boolean saleMgr;
    private boolean purchaseMgr;
    private boolean tableMgr;
    private boolean anasys;

    // Getters
    public boolean isPriceMgr() { return priceMgr; }
    public boolean isSaleMgr() { return saleMgr; }
    public boolean isPurchaseMgr() { return purchaseMgr; }
    public boolean isTableMgr() { return tableMgr; }
    public boolean isAnasys() { return anasys; }

    // 从数据库结果集创建权限对象
    public static Permission fromResultSet(ResultSet rs) {
        Permission p = new Permission();
        try {
            p.priceMgr = rs.getBoolean("priceMgr");
            p.saleMgr = rs.getBoolean("saleMgr");
            p.purchaseMgr = rs.getBoolean("purchaseMgr");
            p.tableMgr = rs.getBoolean("tableMgr");
            p.anasys = rs.getBoolean("anasys");
        } catch (SQLException e) {
            Log.e("Permission", "解析权限时发生错误", e);
            // 返回默认权限或部分权限
            return new Permission(); // 返回空权限
        }
        return p;
    }
}
