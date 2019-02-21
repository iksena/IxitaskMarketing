package com.ixitask.ixitask.services;

import android.database.Observable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ixitask.ixitask.models.ResponseCode;
import com.ixitask.ixitask.models.ResponseHomepass;
import com.ixitask.ixitask.models.ResponseImage;
import com.ixitask.ixitask.models.ResponseInstall;
import com.ixitask.ixitask.models.ResponseLogin;
import com.ixitask.ixitask.models.ResponseLogs;
import com.ixitask.ixitask.models.ResponseProduct;
import com.ixitask.ixitask.models.ResponseSlot;
import com.ixitask.ixitask.models.ResponseSummary;
import com.ixitask.ixitask.models.ResponseSummaryRes;
import com.ixitask.ixitask.models.ResponseUpdate;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public class IxitaskService {

    public static final String BASE_URL = "http://www.ixitask.com/";

    /**
     * userid: 527
     * userkey: 58f02da8893e9dfc5cc45cf743bd31d1
     */
    public interface IxitaskApi {
        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/login")
        Call<ResponseLogin> userLogin(@Field("uname") String username,
                                      @Field("upass") String password);

        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/my_homepass")
        Call<ResponseHomepass> getHomepasses(@Field("userid") String id,
                                             @Field("userkey") String key);

        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/response_code")
        Call<ResponseCode> getResCodes(@Field("userid") String id,
                                       @Field("userkey") String key);

        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/update_homepass_activity")
        Call<ResponseUpdate> submitUpdate(@Field("userid") String userId,
                                          @Field("userkey") String userKey,
                                          @Field("hpid") String hpId,
                                          @Field("date") String date,
                                          @Field("contact_type") int contactType,
                                          @Field("owner_name") String ownerName,
                                          @Field("owner_phone") String phone,
                                          @Field("response_code") String resCode,
                                          @Field("note") String note,
                                          @Field("open") boolean isOpen,
                                          @Field("long") double lon,
                                          @Field("lat") double lat);

        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/activity_log")
        Call<ResponseLogs> getLogs(@Field("userid") String userId,
                                   @Field("userkey") String userKey,
                                   @Field("hpid") String hpId);

        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/track_user_location")
        Call<ResponseUpdate> updateLocation(@Field("userid") String userId,
                                            @Field("userkey") String userKey,
                                            @Field("lat") double lat,
                                            @Field("long") double lon);

        @GET("/mega/api/my_install")
        Call<ResponseInstall> getInstallation(@Query("userid") String userid,
                                              @Query("userkey") String userkey);

        @GET("/mega/api/activiy_summary")
        Call<ResponseSummary> getSummaries(@Query("userid") String userid,
                                           @Query("userkey") String userkey);

        @GET("/mega/api/activity_summary_response")
        Call<ResponseSummaryRes> getSummaryResponses(@Query("userid") String userid,
                                                     @Query("userkey") String userkey,
                                                     @Query("hpscid") int hpscId);

        @GET("/mega/api/slot")
        Call<ResponseSlot> getSlots(@Query("userid") String userid,
                                    @Query("userkey") String userkey,
                                    @Query("slotdate") String date);

        @GET("/mega/api/product")
        Call<ResponseProduct> getProducts(@Query("userid") String userid,
                                          @Query("userkey") String userkey);

        @GET("/mega/api/register_token")
        Call<ResponseUpdate> updateToken(@Query("userid") String userid,
                                    @Query("userkey") String userkey,
                                    @Query("devicetoken") String firebaseToken);

        @GET("/mega/api/register_install")
        Call<ResponseUpdate> registerInstall(@Query("userid") String userId,
                                             @Query("userkey") String userKey,
                                             @Query("hpid") String hpId,
                                             @Query("slotid") String slotId,
                                             @Query("cust_name") String custName,
                                             @Query("longitude") double lon,
                                             @Query("latitude") double lat,
                                             @QueryMap Map<String, Integer> products,
                                             @QueryMap Map<String, String> bundles);

//        @Multipart
//        @POST("/mega/api/upload_picture")
//        Observable<ResponseUpdate> uploadImage(@Part("userid") RequestBody userId,
//                                                 @Part("userkey") RequestBody userKey,
//                                                 @Part("hpid") RequestBody hpId,
//                                                 @Part MultipartBody.Part image);

        @Headers("Accept-Encoding: identity")
        @FormUrlEncoded
        @POST("/mega/api/upload_picture")
        Call<ResponseUpdate> uploadImage(@Field("userid") String userId,
                                               @Field("userkey") String userKey,
                                               @Field("hpid") String hpId,
                                               @Field("picture") String image);

        @GET("/mega/api/get_picture")
        Call<ResponseImage> getImage(@Query("userid") String userId,
                                     @Query("userkey") String userKey,
                                     @Query("hpid") String hpId);

    }

    public static IxitaskApi getApi(){
        Gson gson = new GsonBuilder().setLenient().create(); //accept malformed json
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(IxitaskApi.class);
    }

}
