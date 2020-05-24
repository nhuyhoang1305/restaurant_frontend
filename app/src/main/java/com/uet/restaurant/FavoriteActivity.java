package com.uet.restaurant;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uet.restaurant.Adapter.MyFavoriteAdapter;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FavoriteActivity extends AppCompatActivity {

    private static final String TAG = FavoriteActivity.class.getSimpleName();

    private IRestaurantAPI mIRestaurantAPI;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private android.app.AlertDialog mDialog;

    @BindView(R.id.recycler_fav)
    RecyclerView recycler_fav;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private MyFavoriteAdapter mAdapter;

    @Override
    protected void onDestroy() {
        mCompositeDisposable.clear();
        if (mAdapter != null) {
            mAdapter = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        Log.d(TAG, "onCreate: started!!");

        init();
        initView();

        loadFavoriteItems();
    }

    private void loadFavoriteItems() {
        Log.d(TAG, "loadFavoriteItems: called!!");
        mDialog.show();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        mCompositeDisposable.add(mIRestaurantAPI.getFavoriteByUser(headers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favoriteModel -> {

                    if (favoriteModel.isSuccess()) {
                        mAdapter = new MyFavoriteAdapter(FavoriteActivity.this, favoriteModel.getResult());
                        recycler_fav.setAdapter(mAdapter);
                    } else {
                        Toast.makeText(this, "[GET FAV RESULT]"+favoriteModel.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    mDialog.dismiss();

                }, throwable -> {
                    mDialog.dismiss();
                    Toast.makeText(this, "[GET FAV]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        Log.d(TAG, "initView: called!!");
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_fav.setLayoutManager(layoutManager);
        recycler_fav.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        toolbar.setTitle(getString(R.string.menu_fav));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void init() {
        Log.d(TAG, "init: called!!");
        mDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        mIRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT)
                .create(IRestaurantAPI.class);
    }
}
