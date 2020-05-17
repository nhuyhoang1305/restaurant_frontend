package com.uet.restaurant.Model;

import java.util.List;

public class MenuModel {
    private boolean success;
    private String message;
    private List<Category> result;

    public MenuModel(boolean success, String message, List<Category> result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Category> getResult() {
        return result;
    }

    public void setResult(List<Category> result) {
        this.result = result;
    }
}
