package com.example.xiaoshoufuzhu;

import android.app.Application;

import com.example.xiaoshoufuzhu.SettingsAndUsers.User;

public class MyApplication extends Application {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}