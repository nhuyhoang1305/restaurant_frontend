package com.uet.restaurant.Model;

import com.uet.restaurant.Common.Common;

public class Favorite {

    public String convert(String _image){
        String words[] = _image.split("/");
        return new StringBuilder().append(Common.API_RESTAURANT_ENDPOINT)
                .append(words[3]).toString();
    }

    private String fbid;

    private String restaurantName;

    private String foodName;

    private String foodImage;

    private int foodId;

    private int restaurantId;

    private Double price;

    public String getFbid() {
        return fbid;
    }

    public void setFbid(String fbid) {
        this.fbid = fbid;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return convert(foodImage);
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
