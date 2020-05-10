package com.uet.restaurant;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.accounts.Account;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;

import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/*
 * author: Huy Hoang
 */

public class SplashScreen extends AppCompatActivity {

    IRestaurantAPI restaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;
    FirebaseUser user;
    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

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
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        restaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IRestaurantAPI.class);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            compositeDisposable.add(restaurantAPI.getUser((String) Common.API_KEY, user.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userModel -> {
                        if (userModel.isSuccess()){ //nếu người dùng tồn tại trong db, chạy thẳng vào home

                            Common.currentUser = userModel.getResult().get(0);
                            Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else{ // nếu người dùng không tồn tại trong db, chạy vào UpdateInfo để đăng ký
                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        dialog.dismiss();
                    },
                        throwable -> {
                            dialog.dismiss();
                            Toast.makeText(SplashScreen.this, "[GET USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
        }
        else{
            Toast.makeText(SplashScreen.this, "Not Signed", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }
    }
}
