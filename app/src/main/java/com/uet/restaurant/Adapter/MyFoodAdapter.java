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
import com.uet.restaurant.Database.CartDataSource;
import com.uet.restaurant.Database.CartDatabase;
import com.uet.restaurant.Database.CartItem;
import com.uet.restaurant.Database.LocalCartDataSource;
import com.uet.restaurant.FoodDetailActivity;
import com.uet.restaurant.Interface.IFoodDetailOrCartClickListener;
import com.uet.restaurant.Model.EventBus.FoodDetailEvent;
import com.uet.restaurant.Model.FavoriteOnlyId;
import com.uet.restaurant.Model.Food;
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

public class MyFoodAdapter extends RecyclerView.Adapter<MyFoodAdapter.MyViewHolder> {

    private Context mContext;
    private List<Food> mFoodList;
    private CompositeDisposable mCompositeDisposable;
    private CartDataSource mCartDataSource;
    private IRestaurantAPI mIRestaurantAPI;

    public void onStop() {
        mCompositeDisposable.clear();
    }

    public MyFoodAdapter(Context context, List<Food> foodList) {
        mContext = context;
        mFoodList = foodList;
        mCompositeDisposable = new CompositeDisposable();
        mCartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        mIRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IRestaurantAPI.class);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_food, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(mFoodList.get(position).getImage())
                .placeholder(R.drawable.app_icon).into(holder.img_food);

        holder.txt_food_name.setText(mFoodList.get(position).getName());
        holder.txt_food_price.setText(new StringBuilder(mContext.getString(R.string.money_sign))
                .append(mFoodList.get(position).getPrice()));

        // Check Favorite
        if (Common.currentFavOfRestaurant != null && Common.currentFavOfRestaurant.size() > 0) {
            if (Common.checkFavorite(mFoodList.get(position).getId())) {
                holder.img_fav.setImageResource(R.drawable.ic_favorite_button_color_24dp);
                holder.img_fav.setTag(true);
            }
            else {
                holder.img_fav.setImageResource(R.drawable.ic_favorite_border_button_color_24dp);
                holder.img_fav.setTag(false);
            }
        }
        else {
            // Default, all item is no favorite
            holder.img_fav.setTag(false);
        }

        // Event
        holder.img_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView fav = (ImageView)v;
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                if ((Boolean)fav.getTag()) {
                    // If tag = true -> Favorite item clicked
                    mCompositeDisposable.add(mIRestaurantAPI.removeFavorite(headers,
                            mFoodList.get(position).getId(),
                            Common.currentRestaurant.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(favoriteModel -> {

                                if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                    fav.setImageResource(R.mipmap.ic_heart_fav);
                                    fav.setTag(false);
                                    if (Common.currentFavOfRestaurant != null) {
                                        Common.removeFavorite(mFoodList.get(position).getId());
                                    }
                                }

                            }, throwable -> {
                                Toast.makeText(mContext, "[REMOVE FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
                }
                else {
                    mCompositeDisposable.add(mIRestaurantAPI.insertFavorite(headers,
                            mFoodList.get(position).getId(),
                            Common.currentRestaurant.getId(),
                            Common.currentRestaurant.getName(),
                            mFoodList.get(position).getName(),
                            mFoodList.get(position).getImage(),
                            mFoodList.get(position).getPrice())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(favoriteModel -> {

                                if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                    fav.setImageResource(R.mipmap.ic_heart);
                                    fav.setTag(true);
                                    if (Common.currentFavOfRestaurant != null) {
                                        Common.currentFavOfRestaurant.add(new FavoriteOnlyId(mFoodList.get(position).getId()));
                                    }
                                }

                            }, throwable -> {
                                Toast.makeText(mContext, "[ADD FAV]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
                }
            }
        });

        holder.setIFoodDetailOrCartClickListener((view, i, isDetail) -> {
            if (isDetail) {

                mContext.startActivity(new Intent(mContext, FoodDetailActivity.class));
                EventBus.getDefault().postSticky(new FoodDetailEvent(true, mFoodList.get(i)));

            } else {
                // Cart create
                CartItem cartItem = new CartItem();
                cartItem.setFoodId(mFoodList.get(i).getId());
                cartItem.setFoodName(mFoodList.get(i).getName());
                cartItem.setFoodPrice(mFoodList.get(i).getPrice());
                cartItem.setFoodImage(mFoodList.get(i).getImage());
                cartItem.setFoodQuantity(1);
                cartItem.setUserPhone(Common.currentUser.getUserPhone());
                cartItem.setRestaurantId(Common.currentRestaurant.getId());
                cartItem.setFoodAddon("NORMAL");
                cartItem.setFoodSize("NORMAL");
                cartItem.setFoodExtraPrice(0.0);
                cartItem.setFbid(Common.currentUser.getFbid());

                mCompositeDisposable.add(mCartDataSource.insertOrReplaceAll(cartItem)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Toast.makeText(mContext, "Added to Cart", Toast.LENGTH_SHORT).show();
                        }, throwable -> {

                        }));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFoodList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_food)
        ImageView img_food;
        @BindView(R.id.img_fav)
        ImageView img_fav;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.img_detail)
        ImageView img_detail;
        @BindView(R.id.img_cart)
        ImageView img_add_cart;

        IFoodDetailOrCartClickListener mIFoodDetailOrCartClickListener;

        public void setIFoodDetailOrCartClickListener(IFoodDetailOrCartClickListener IFoodDetailOrCartClickListener) {
            mIFoodDetailOrCartClickListener = IFoodDetailOrCartClickListener;
        }

        Unbinder mUnbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mUnbinder = ButterKnife.bind(this, itemView);

            img_detail.setOnClickListener(this);
            img_add_cart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.img_detail) {
                mIFoodDetailOrCartClickListener.onFoodItemClickListener(v, getAdapterPosition(), true);
            }
            else if (v.getId() == R.id.img_cart) {
                mIFoodDetailOrCartClickListener.onFoodItemClickListener(v, getAdapterPosition(), false);
            }
        }
    }
}