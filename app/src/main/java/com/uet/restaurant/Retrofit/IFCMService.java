package com.uet.restaurant.Retrofit;

import com.uet.restaurant.Model.FCMResponse;
import com.uet.restaurant.Model.FCMSendData;


import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAANJ4Q-Ls:APA91bFCcGCz5CFt0yAmPuOg-zOaEtGc0bb0yKdJFnSBXSv4f_MLXuChTstocmtCrwPtVsd4gDFKmU4zw3_7mjbTHU8Ocf5a-blaHCpLj74jEUBLUQRtP-wsegVLPD4a_15aLFMp-yFx"
    })

    @POST("fcm/send")
    Observable<FCMResponse> sendNotificiaton(@Body FCMSendData body);
}
