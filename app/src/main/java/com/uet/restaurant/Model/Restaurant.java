package com.uet.restaurant.Model;

import com.uet.restaurant.Common.Common;

public class Restaurant {

    // Nên nhớ, tất cả tên biến nên giống như thuộc tính trong JSON trả về từ API, nó sẽ giúp JSON parse đúng

    private int id;

    private String name;

    private String address;

    private String phone;

    private String image;

    private String paymentUrl;

    private Float lat;

    private Float lng; //kinh do, vi do

    private int userOwner;

    public String convert(String _image){
        String words[] = _image.split("/");
        return new StringBuilder().append(Common.API_RESTAURANT_ENDPOINT)
                .append(words[3]).toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return convert(image);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLng() {
        return lng;
    }

    public void setLng(Float lng) {
        this.lng = lng;
    }

    public int getUserOwner() {
        return userOwner;
    }

    public void setUserOwner(int userOwner) {
        this.userOwner = userOwner;
    }
}
