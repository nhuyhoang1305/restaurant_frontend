package com.uet.restaurant.Retrofit;

import com.uet.restaurant.Model.AddonModel;
import com.uet.restaurant.Model.CreateOrderModel;
import com.uet.restaurant.Model.FavoriteModel;
import com.uet.restaurant.Model.FavoriteOnlyIdModel;
import com.uet.restaurant.Model.FoodModel;
import com.uet.restaurant.Model.GetKeyModel;
import com.uet.restaurant.Model.MaxOrderModel;
import com.uet.restaurant.Model.MenuModel;
import com.uet.restaurant.Model.OrderModel;
import com.uet.restaurant.Model.RestaurantModel;
import com.uet.restaurant.Model.SizeModel;
import com.uet.restaurant.Model.TokenModel;
import com.uet.restaurant.Model.UpdateOrderModel;
import com.uet.restaurant.Model.UpdateUserModel;
import com.uet.restaurant.Model.UserModel;

import java.util.Map;

import io.reactivex.Observable;

import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IRestaurantAPI {

    //============================GET=========================================

    @GET("getkey")
    Observable<GetKeyModel> getKey(@Query("fbid") String fbid);

    @GET("user")
    Observable<UserModel> getUser(@HeaderMap Map<String, String> headers);

    @GET("restaurant")
    Observable<RestaurantModel> getRestaurant(@HeaderMap Map<String, String> headers);

    @GET("restaurantById")
    Observable<RestaurantModel> getRestaurantById(@HeaderMap Map<String, String> headers,
                                                  @Query("restaurantId") int id);

    @GET("nearbyrestaurant")
    Observable<RestaurantModel> getNearByRestaurant(@HeaderMap Map<String, String> headers,
                                                    @Query("lat") double lat,
                                                    @Query("lng") double lng,
                                                    @Query("distance") int distance);
    @GET("menu")
    Observable<MenuModel> getCategories(@HeaderMap Map<String, String> headers,
                                        @Query("restaurantId") int restaurantId);

    @GET("food")
    Observable<FoodModel> getFoodOfMenu(@HeaderMap Map<String, String> headers,
                                              @Query("menuId") int menuId);

    @GET("foodById")
    Observable<FoodModel> getFoodById(@HeaderMap Map<String, String> headers,
                                      @Query("foodId") int foodId);

    @GET("searchFood")
    Observable<FoodModel> searchFood(@HeaderMap Map<String, String> headers,
                                            @Query("foodName") String foodName,
                                            @Query("menuId") int menuId);

    @GET("size")
    Observable<SizeModel> getSizeOfFood(@HeaderMap Map<String, String> headers,
                                        @Query("foodId") int foodId);

    @GET("addon")
    Observable<AddonModel> getAddonOfFood(@HeaderMap Map<String, String> headers,
                                          @Query("foodId") int foodId);

    @GET("favorite")
    Observable<FavoriteModel> getFavoriteByUser(@HeaderMap Map<String, String> headers);

    @GET("favoriteByRestaurant")
    Observable<FavoriteOnlyIdModel> getFavoriteByRestaurant(@HeaderMap Map<String, String> headers,
                                                            @Query("restaurantId") int restaurantId);

    @GET("order")
    Observable<OrderModel> getOrder(@HeaderMap Map<String, String> headers,
                                    @Query("orderFBID") String orderFBID,
                                    @Query("from") int from,
                                    @Query("to") int to);

    @GET("maxorder")
    Observable<MaxOrderModel> getMaxOrder(@HeaderMap Map<String, String> headers,
                                          @Query("orderFBID") String orderFBID);

    @GET("token")
    Observable<TokenModel> getToken(@HeaderMap Map<String, String> headers);

    //============================POST=========================================

    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel> updateUserInfo(@HeaderMap Map<String, String> headers,
                                               @Field("userPhone") String userPhone,
                                               @Field("userName") String userName,
                                               @Field("userAddress") String userAddress);
    @POST("favorite")
    @FormUrlEncoded
    Observable<FavoriteModel> insertFavorite(@HeaderMap Map<String, String> headers,
                                             @Field("foodId") int foodId,
                                             @Field("restaurantId") int restaurantId,
                                             @Field("restaurantName") String restaurantName,
                                             @Field("foodName") String foodName,
                                             @Field("foodImage") String foodImage,
                                             @Field("price") double price);
    @POST("createOrder")
    @FormUrlEncoded
    Observable<CreateOrderModel> createOrder(@HeaderMap Map<String, String> headers,
                                             @Field("orderPhone") String orderPhone,
                                             @Field("orderName") String orderName,
                                             @Field("orderAddress") String orderAddress,
                                             @Field("orderDate") String orderDate,
                                             @Field("restaurantId") int restaurantId,
                                             @Field("transactionId") String transactionId,
                                             @Field("cod") boolean cod,
                                             @Field("totalPrice") Double totalPrice,
                                             @Field("numOfItem") int numOfItem);

    @POST("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrder(@HeaderMap Map<String, String> headers,
                                             @Field("orderDetail") String orderDetail);

    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateTokenToServer(@HeaderMap Map<String, String> headers,
                                               @Field("token") String token);

    //============================DELETE=========================================
    @DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@HeaderMap Map<String, String> headers,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);

}
