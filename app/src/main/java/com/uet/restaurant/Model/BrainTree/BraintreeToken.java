package com.uet.restaurant.Model.BrainTree;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BraintreeToken {

    @SerializedName("clientToken")
    @Expose
    private String clientToken;

    @SerializedName("success")
    @Expose
    private boolean success;

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
