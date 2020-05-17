package com.uet.restaurant;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.picasso.Picasso;
import com.uet.restaurant.Adapter.MyCategoryAdapter;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Database.CartDataSource;
import com.uet.restaurant.Database.CartDatabase;
import com.uet.restaurant.Database.LocalCartDataSource;
import com.uet.restaurant.Model.EventBus.MenuItemEvent;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;
import com.uet.restaurant.Utils.SpaceItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MenuActivity extends AppCompatActivity {

    private final String TAG = MenuActivity.class.getSimpleName();

    @BindView(R.id.img_restaurant)
    KenBurnsView img_restaurant;
    @BindView(R.id.recycler_category)
    RecyclerView recycler_category;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton btn_cart;
    @BindView(R.id.badge)
    NotificationBadge badge;


    private IRestaurantAPI mIRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private AlertDialog mDialog;

    private MyCategoryAdapter myCategoryAdapter;
    private CartDataSource mCartDataSource;

    private LayoutAnimationController mLayoutAnimationController;


    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        coutCartByRestaurant();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();
        coutCartByRestaurant();

    }

    private void coutCartByRestaurant() {
        Log.d(TAG, "coutCartByRestaurant: calledd!!");
        mCartDataSource.countItemInCart(Common.currentUser.getFbid(),
        Common.currentRestaurant.getId())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new SingleObserver<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(Integer integer) {
                badge.setText(String.valueOf(integer));
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MenuActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        mLayoutAnimationController = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_item_from_left);

        btn_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Implement late
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        // This code will select item view type
        // If item is last, it will set full width on Grid layout
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (myCategoryAdapter != null){
                    switch (myCategoryAdapter.getItemViewType(position)){
                        case Common.DEFAULT_COLUMN_COUNT:
                            return 1;
                        case Common.FULL_WIDTH_COLUMN:
                            return 2;
                        default:
                            return -1;
                    }
                }
                else {
                    return -1;
                }
            }
        });
        recycler_category.setLayoutManager(layoutManager);
        recycler_category.addItemDecoration(new SpaceItemDecoration(8));
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IRestaurantAPI.class);
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
    }

    // Register event bus

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

    //Listen EventBus
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void loadMenuByRestaurant(MenuItemEvent event){
        if (event.isSuccess()){
            Log.d(TAG, event.getRestaurant().getImage());
            Picasso.get().load(event.getRestaurant().getImage()).into(img_restaurant); //load img restaurant
            toolbar.setTitle(event.getRestaurant().getName()); // load name restaurant

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // request category by restaurant id
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
            mCompositeDisposable.add(mIRestaurantAPI.getCategories(headers, event.getRestaurant().getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(menuModel -> {
                            myCategoryAdapter = new MyCategoryAdapter(MenuActivity.this, menuModel.getResult());
                            recycler_category.setAdapter(myCategoryAdapter);
                            recycler_category.setLayoutAnimation(mLayoutAnimationController);
                                },
                                throwable -> {
                                    Toast.makeText(this, "[Lấy thực đơn lỗi]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }));

        }
    }
}
