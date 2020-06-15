package com.uet.restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.uet.restaurant.Adapter.RestaurantAdapter;
import com.uet.restaurant.Adapter.RestaurantSliderAdapter;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Database.CartDataSource;
import com.uet.restaurant.Database.CartItem;
import com.uet.restaurant.Model.EventBus.RestaurantLoadEvent;
import com.uet.restaurant.Model.Favorite;
import com.uet.restaurant.Model.FavoriteOnlyId;
import com.uet.restaurant.Model.Food;
import com.uet.restaurant.Model.Restaurant;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;
import com.uet.restaurant.Services.PicassoImageLoadingService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ss.com.bannerslider.Slider;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = HomeActivity.class.getSimpleName();
    private TextView txt_user_name, txt_user_phone;

    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_restaurant)
    RecyclerView recycler_restaurant;

    private IRestaurantAPI mIRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private android.app.AlertDialog mDialog;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: started!!");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        txt_user_name = headerView.findViewById(R.id.txt_user_name);
        txt_user_phone = headerView.findViewById(R.id.txt_user_phone);

        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getName()))
            txt_user_name.setText(Common.currentUser.getName());
        if (Common.currentUser != null && !TextUtils.isEmpty(Common.currentUser.getAddress()))
            txt_user_phone.setText(Common.currentUser.getAddress());


        //txt_user_name.setText(Common.currentUser.getName());
        //txt_user_phone.setText(Common.currentUser.getUserPhone());

        init();
        initView();

        loadRestaurant();
        loadFav();

    }

    private void loadFav() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        mCompositeDisposable.add(mIRestaurantAPI.getFavoriteByUser(headers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favoriteModel -> {
                    if (favoriteModel.isSuccess()){
                        Common.currentFavOfRestaurant = new ArrayList<>();
                        for (Favorite favorite : favoriteModel.getResult()){
                            Common.currentFavOfRestaurant.add(new FavoriteOnlyId(favorite.getFoodId()));
                        }
                    }
                        },
                        throwable -> {}));
    }

    private void loadRestaurant() {
        Log.d(TAG, "loadRestaurant: called!!");
        mDialog.show();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        mCompositeDisposable.add(mIRestaurantAPI.getRestaurant(headers)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restaurantModel -> {
                            // user EventBus to send local event set adapter and slider
                            EventBus.getDefault().post(new RestaurantLoadEvent(true, restaurantModel.getResult()));
                            mDialog.dismiss();
                            },
                            throwable -> {
                                mDialog.dismiss();
                                EventBus.getDefault().post(new RestaurantLoadEvent(false, throwable.getMessage()));
                                Toast.makeText(this, "[GET RESTAURANT] " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        })
        );
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_restaurant.setLayoutManager(layoutManager);
        recycler_restaurant.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IRestaurantAPI.class);
        Paper.init(this);
        Slider.init(new PicassoImageLoadingService());

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.

        int id =  menuItem.getItemId();
        if (id == R.id.nav_log_out){
            signOut();
        }
        else if (id == R.id.nav_nearby){
            startActivity(new Intent(HomeActivity.this, NearbyRestaurantActivity.class));
        }
        else if (id == R.id.nav_update_info){
            startActivity(new Intent(HomeActivity.this, UpdateInfoActivity.class));
        }
        else if (id == R.id.nav_order_history){
            startActivity(new Intent(HomeActivity.this, ViewOrderActivity.class));
        }
        else if (id == R.id.nav_fav){
            startActivity(new Intent(HomeActivity.this, FavoriteActivity.class));
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut(){
        Log.d(TAG, "signOut: called!!");
        //Alert dialog to confirm
        AlertDialog confimrDialog = new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có thực sự muốn đăng xuất?")
                .setNegativeButton("CANCLE", (dialog1, which) -> dialog1.dismiss())
                .setPositiveButton("OK", (dialog12, which) -> {
                    Common.currentUser = null;
                    Common.currentRestaurant = null;
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear all previous activity
                    startActivity(intent);
                    finish();
                }).create();
        confimrDialog.show();
    }

    //Register event bus
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // Listener Event Bus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processRestaurantLoadEvent(RestaurantLoadEvent event){
        Log.d(TAG, "processRestaurantLoadEvent: called!!");
        if (event.isSuccess()){
            displayBanner(event.getRestaurantList());
            displayRestaurant(event.getRestaurantList());
        }
        else {
            Toast.makeText(this, "[RESTAURANT LOAD]" + event.getMessage(), Toast.LENGTH_SHORT).show();
        }
        mDialog.dismiss();
    }

    private void displayRestaurant(List<Restaurant> restaurantList) {
        Log.d(TAG, "displayRestaurant: called!!");
        RestaurantAdapter adapter = new RestaurantAdapter(this, restaurantList);
        recycler_restaurant.setAdapter(adapter);
    }

    private void displayBanner(List<Restaurant> restaurantList) {
        Log.d(TAG, "displayBanner: called!!");
        Log.d(TAG, "displayBanner: size: " + restaurantList.size());
        banner_slider.setAdapter(new RestaurantSliderAdapter(restaurantList));
    }
}
