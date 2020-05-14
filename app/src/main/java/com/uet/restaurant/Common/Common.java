package com.uet.restaurant.Common;

import com.uet.restaurant.Model.User;

public class Common {
    public static final String API_RESTAURANT_ENDPOINT = "http://192.168.0.102:3000/";

    // later, secure it with Firebase Remote Config
    public static String API_KEY = "";
    public static final String REMEMBER_FBID = "REMEBER_FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFIC_TITLE = "title";
    public static final String NOTIFIC_CONTENT = "content";
    public static User currentUser;

    public static String buildJWT(String apiKey){
        return new StringBuilder("Bearer")
                .append(" ")
                .append(apiKey).toString();
    }
}
