package com.uet.restaurant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateInfoActivity extends AppCompatActivity {

    IRestaurantAPI restaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    FirebaseUser user;

    @BindView(R.id.edit_user_name)
    EditText edit_user_name;
    @BindView(R.id.edit_user_address)
    EditText edit_user_address;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        ButterKnife.bind(this);

        init();
        initView();
    }


    //Override back arrow


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish(); // close this activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        toolbar.setTitle(getString(R.string.update_infomation));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                compositeDisposable.add(
                        restaurantAPI.updateUserInfo((String) Common.API_KEY,
                                user.getPhoneNumber().toString(),
                                edit_user_name.getText().toString(),
                                edit_user_address.getText().toString(),
                                user.getUid())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(updateUserModel -> {

                                            if (updateUserModel.isSuccess()){
                                                // If user has been update, just refesh again
                                                compositeDisposable.add(
                                                        restaurantAPI.getUser((String) Common.API_KEY, user.getUid())
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(userModel -> {

                                                                            if (userModel.isSuccess()){
                                                                                Common.currentUser = userModel.getResult().get(0);
                                                                                startActivity(new Intent(UpdateInfoActivity.this, HomeActivity.class));
                                                                                finish();
                                                                            }
                                                                            else{
                                                                                Toast.makeText(UpdateInfoActivity.this, "[GET USER RESULT]" + userModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            dialog.dismiss();
                                                                        },
                                                                        throwable -> {
                                                                            dialog.dismiss();
                                                                            Toast.makeText(UpdateInfoActivity.this, "[GET USER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        })
                                                );
                                            }
                                            else{
                                                dialog.dismiss();
                                                Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API RETURN]" + updateUserModel.getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        },
                                        throwable -> {
                                            dialog.dismiss();
                                            Toast.makeText(UpdateInfoActivity.this, "[UPDATE USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                );

            }
        });

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        restaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IRestaurantAPI.class);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }
}
