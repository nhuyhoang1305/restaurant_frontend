package com.uet.restaurant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/*
 * author: Huy Hoang
 */

public class SplashScreen extends AppCompatActivity {

    private static final String TAG = SplashScreen.class.getSimpleName();

    private IRestaurantAPI mIRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started!!");
        super.onCreate(savedInstanceState);

        init();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        //Get token
                        FirebaseInstanceId.getInstance()
                                .getInstanceId()
                                .addOnFailureListener(e -> Toast.makeText(SplashScreen.this, "[GET TOKEN]" + e.getMessage(), Toast.LENGTH_SHORT).show()
                                )
                                .addOnCompleteListener(task -> {
                                   if (task.isSuccessful()){

                                       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                       if (user != null){
                                           Paper.book().write(Common.REMEMBER_FBID, user.getUid());

                                           mDialog.show();
                                           mCompositeDisposable.add(mIRestaurantAPI.getKey(user.getUid())
                                           .subscribeOn(Schedulers.io())
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .subscribe(getKeyModel -> {
                                               if (getKeyModel.isSuccess()){
                                                   //Write jwt to variable
                                                   Common.API_KEY = getKeyModel.getToken();
                                                   //Log.d(TAG, "API_KEY: " + Common.API_KEY);
                                                   //After we have account, we will get fbid and update token
                                                   Map<String, String> headers = new HashMap<>();
                                                   headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                                   Log.d(TAG, "TOKEN_TASK: " + task.getResult().getToken());
                                                   mCompositeDisposable.add(mIRestaurantAPI.updateTokenToServer(headers,
                                                           task.getResult().getToken())
                                                           .subscribeOn(Schedulers.io())
                                                           .observeOn(AndroidSchedulers.mainThread())
                                                           .subscribe(tokenModel -> {
                                                               if (!tokenModel.isSuccess()){
                                                                   Toast.makeText(SplashScreen.this, "[UPDATE TOKEN ERROR]" + tokenModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                                   return;
                                                               }
                                                               mCompositeDisposable.add(mIRestaurantAPI.getUser(headers)
                                                               .subscribeOn(Schedulers.io())
                                                               .observeOn(AndroidSchedulers.mainThread())
                                                               .subscribe(userModel -> {
                                                                        if (userModel.isSuccess()){ // if user avaiable in db
                                                                            Common.currentUser = userModel.getResult().get(0);
                                                                            Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                        else{
                                                                            Intent intent = new Intent(SplashScreen.this, UpdateInfoActivity.class);
                                                                            startActivity(intent);
                                                                            //startActivity(new Intent(SplashScreen.this, UpdateInfoActivity.class));
                                                                            finish();
                                                                        }
                                                                        mDialog.dismiss();
                                                                       },
                                                                       throwable -> {
                                                                        mDialog.dismiss();
                                                                        Toast.makeText(SplashScreen.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                       }));
                                                           }, throwable -> {
                                                               mDialog.dismiss();
                                                               Toast.makeText(SplashScreen.this, "[UPDATE TOKEN] " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                               Log.d(TAG, "[UPDATE TOKEN] " + throwable.getMessage());
                                                           }));

                                               }
                                               else{
                                                   mDialog.dismiss();
                                                   Toast.makeText(SplashScreen.this, getKeyModel.getMessage(), Toast.LENGTH_SHORT).show();
                                               }
                                           }, throwable -> {
                                               mDialog.dismiss();
                                               Toast.makeText(SplashScreen.this, "Can't get Json Web Token " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                               Log.d(TAG, "Can't get Json Web Token " + throwable.getMessage());
                                           }));
                                       }
                                       else {
                                           Toast.makeText(SplashScreen.this, "Chưa đăng nhập!!", Toast.LENGTH_SHORT).show();
                                           startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                           finish();
                                       }

                                   }
                                });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SplashScreen.this,"Bạn nên cấp quyền để truy cập vào app!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        Paper.init(this);
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IRestaurantAPI.class);

    }
}
