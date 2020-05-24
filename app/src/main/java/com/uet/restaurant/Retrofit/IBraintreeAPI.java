package com.uet.restaurant.Retrofit;

import io.reactivex.Observable;

import com.uet.restaurant.Model.BrainTree.BraintreeToken;
import com.uet.restaurant.Model.BrainTree.BraintreeTransaction;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IBraintreeAPI {
    @GET("checkouts/new")
    Observable<BraintreeToken> getToken();

    @POST("checkouts")
    @FormUrlEncoded
    Observable<BraintreeTransaction> submitPayment(@Field("amount") String amount,
                                                   @Field("payment_method_nonce") String nonce);
}
