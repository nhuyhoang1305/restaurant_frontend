package com.uet.restaurant.Model;

import com.uet.restaurant.Common.Common;

public class Category {
    private int id;
    private String name;
    private String description;
    private String image;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return convert(image);
    }

    public void setImage(String image) {
        this.image = image;
    }
}
