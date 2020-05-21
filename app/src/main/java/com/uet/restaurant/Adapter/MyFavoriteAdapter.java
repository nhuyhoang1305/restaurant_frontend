package com.uet.restaurant.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.uet.restaurant.Common.Common;
import com.uet.restaurant.FoodDetailActivity;
import com.uet.restaurant.Interface.IOnRecyclerViewClickListener;
import com.uet.restaurant.Model.EventBus.FoodDetailEvent;
import com.uet.restaurant.Model.Favorite;
import com.uet.restaurant.Model.Restaurant;
import com.uet.restaurant.R;
import com.uet.restaurant.Retrofit.IRestaurantAPI;
import com.uet.restaurant.Retrofit.RetrofitClient;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyFavoriteAdapter extends RecyclerView.Adapter<MyFavoriteAdapter.MyViewHolder> {

    private Context mContext;
    private List<Favorite> mFavoriteList;
    private CompositeDisposable mCompositeDisposable;
    private IRestaurantAPI mIRestaurantAPI;

    public void onStop() {
        mCompositeDisposable.clear();
    }

    public MyFavoriteAdapter(Context context, List<Favorite> favoriteList) {
        mContext = context;
        mFavoriteList = favoriteList;
        mCompositeDisposable = new CompositeDisposable();
        mIRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IRestaurantAPI.class);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_favorite_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mFavoriteList.get(position).getFoodImage()).into(holder.img_food);
        holder.txt_food_name.setText(mFavoriteList.get(position).getFoodName());
        holder.txt_food_price.setText(new StringBuilder(mContext.getString(R.string.money_sign))
                .append(mFavoriteList.get(position).getPrice()));
        holder.txt_restaurant_name.setText(mFavoriteList.get(position).getRestaurantName());

        // Event
        holder.setIOnRecyclerViewClickListener((view, i) -> {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", Common.buildJWT(Common.API_KEY));
            mCompositeDisposable.add(mIRestaurantAPI.getFoodById(headers, mFavoriteList.get(i).getFoodId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(foodModel -> {

                        if (foodModel.isSuccess()) {
                            // When user click to favorite item, just start FoodDetailActivity
                            mContext.startActivity(new Intent(mContext, FoodDetailActivity.class));
                            if (Common.currentRestaurant == null) {
                                Common.currentRestaurant = new Restaurant();
                            }
                            Common.currentRestaurant.setId(mFavoriteList.get(i).getRestaurantId());
                            Common.currentRestaurant.setName(mFavoriteList.get(i).getRestaurantName());
                            EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodModel.getResult().get(0)));

                        }
                        else {
                            Toast.makeText(mContext, "[GET FOOD BY RESULT]"+foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }, throwable -> {
                        Toast.makeText(mContext, "[GET FOOD BY ID]", Toast.LENGTH_SHORT).show();
                    }));
        });
    }

    @Override
    public int getItemCount() {
        return mFavoriteList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_restaurant_name)
        TextView txt_restaurant_name;

        Unbinder mUnbinder;

        IOnRecyclerViewClickListener mIOnRecyclerViewClickListener;

        public void setIOnRecyclerViewClickListener(IOnRecyclerViewClickListener IOnRecyclerViewClickListener) {
            mIOnRecyclerViewClickListener = IOnRecyclerViewClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mIOnRecyclerViewClickListener.onClick(v, getAdapterPosition());
        }
    }
}
