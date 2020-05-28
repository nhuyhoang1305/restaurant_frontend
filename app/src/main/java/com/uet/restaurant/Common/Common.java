package com.uet.restaurant.Common;

import com.uet.restaurant.Model.Addon;
import com.uet.restaurant.Model.FavoriteOnlyId;
import com.uet.restaurant.Model.Restaurant;
import com.uet.restaurant.Model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String API_RESTAURANT_ENDPOINT = "http://192.168.100.3:3000/";

    // later, secure it with Firebase Remote Config
    public static String API_KEY = "";
    public static final String REMEMBER_FBID = "REMEBER_FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFIC_TITLE = "title";
    public static final String NOTIFIC_CONTENT = "content";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static User currentUser;
    public static Restaurant currentRestaurant;
    public static Set<Addon> addonList = new HashSet<>();
    public static List<FavoriteOnlyId> currentFavOfRestaurant;

    public static String buildJWT(String apiKey){
        return new StringBuilder("Bearer")
                .append(" ")
                .append(apiKey).toString();
    }

    public static boolean checkFavorite(int id) {
        boolean result = false;
        for (FavoriteOnlyId item : currentFavOfRestaurant) {
            if (item.getFoodId() == id) {
                result = true;
            }
        }
        return result;
    }

    public static void removeFavorite(int id) {
        for (FavoriteOnlyId item : currentFavOfRestaurant) {
            if (item.getFoodId() == id) {
                currentFavOfRestaurant.remove(item);
            }
        }
    }
    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }
}
