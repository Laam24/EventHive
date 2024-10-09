package org.example.ems;

public class LoggedInUser {
    private static String username;

    public static void setUsername(String username) {
        LoggedInUser.username = username;
    }

    public static String getUsername() {
        return username;
    }

    public static void clearSession() {
        username = null;
    }
}