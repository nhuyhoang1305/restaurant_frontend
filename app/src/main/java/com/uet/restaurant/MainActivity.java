package com.uet.restaurant;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int APP_REQUEST_CODE = 1305;

    IRestaurantAPI restaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void loginUser(){
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(), APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE){

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else{
                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
            }
            dialog.show();

        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        
        init();

    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        restaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IRestaurantAPI.class);
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();

        listener = firebaseAuth1 -> {
            FirebaseUser user = firebaseAuth1.getCurrentUser();

            if (user != null){ // user đã đăng nhập
                //Save FBID
                //Paper.book().write(Common.REMEMBER_FBID, user.getUid());
                dialog.show();
                compositeDisposable.add(
                        restaurantAPI.getUser((String) Common.API_KEY,
                                user.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(userModel -> {

                                        if (userModel.isSuccess()){

                                            if (userModel.isSuccess()){
                                                Common.currentUser = userModel.getResult().get(0);
                                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                finish();
                                            }
                                            else{ // if user not register
                                                startActivity(new Intent(MainActivity.this, UpdateInfoActivity.class));
                                                finish();
                                            }
                                            dialog.dismiss();
                                        }
                                        else{
                                            dialog.dismiss();
                                            Toast.makeText(MainActivity.this, "[UPDATE USER API RETURN]" + userModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    },
                                    throwable -> {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "[UPDATE USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                );
            }
            else{
                loginUser();
            }
        };

    }
}
