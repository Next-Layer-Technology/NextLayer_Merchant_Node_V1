package com.sis.clightapp.Interface;


import com.google.gson.JsonObject;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp;
import com.sis.clightapp.model.ImageRelocation.AddImageResp;
import com.sis.clightapp.model.ImageRelocation.GetItemImageRSP;
import com.sis.clightapp.model.REST.ClientListModel;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.REST.Loginresponse;
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp;
import com.sis.clightapp.model.REST.StoreClients;
import com.sis.clightapp.model.REST.TransactionResp;
import com.sis.clightapp.model.REST.get_session_response;
import com.sis.clightapp.model.REST.nearby_clients.NearbyClientResponse;
import com.sis.clightapp.model.WebsocketResponse.WebSocketResponse;
import com.sis.clightapp.model.currency.CurrentAllRate;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;

import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiPaths {
    @GET("ticker")
    Call<CurrentAllRate> getCurrentAllRate();

    //TODO: Trasacntion Add APi
    @FormUrlEncoded
    @POST("add-alpha-transction")
    //ok
    Call<TransactionResp> add_alpha_transction(
            @Field("transaction_label") String transaction_label,
            @Field("status") String status,
            @Field("transaction_amountBTC") String transaction_amountBTC,
            @Field("transaction_amountUSD") String transaction_amountUSD,
            @Field("payment_preimage") String payment_preimage,
            @Field("payment_hash") String payment_hash,
            @Field("conversion_rate") String conversion_rate,
            @Field("msatoshi") String msatoshi,
            @Field("destination") String destination,
            @Field("merchant_id") String merchant_id,
            @Field("transaction_description") String transaction_description
    );

    @Multipart
    @POST("reboot.php")
    Call<NodeResp> rebootServer(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );

    @Multipart
    @POST("upgrade.php")
    Call<NodeResp> upgradeServer(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );

    @Multipart
    @POST("update.php")
    Call<NodeResp> updateServer(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );
    @Multipart
    @POST("thor.php")
        //ok
    Call<NodeResp> startThorStopNodeServer3(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );
    @Multipart
    @POST("lightning.php")

         //Call<NodeResp> startLightningServer0(@Body JsonObject params);
        //ok
    Call<NodeResp> startLightningServer2(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );

    @Multipart
    @POST("lightning.php")
        //ok
    Call<NodeResp> stopLightningServer2(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );
    @Multipart
    @POST("bitcoin.php")
        //ok
    Call<NodeResp> startBitcoinServer2(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );
    @Multipart
    @POST("bitcoin.php")
        //ok
    Call<NodeResp> stopBitcoinServer2(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("type") RequestBody type,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key
    );

    @Multipart
    @POST("lightning-status.php")
        //ok
    Call<NodeResp> checkLightningNodeServerStatus2(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key,
            @Part("rpcusername") RequestBody rpc_username,
            @Part("rpcpassword") RequestBody rpc_password
    );

    @Multipart
    @POST("bitcoin-status.php")
        //ok
    Call<NodeResp> checkBitcoinNodeServerStatus2(
            @Part("sshkeypw") RequestBody sshkeypass,
            @Part("host") RequestBody host,
            @Part("port") RequestBody port,
            @Part("username") RequestBody username,
            @Part MultipartBody.Part key,
            @Part("rpcusername") RequestBody rpc_username,
            @Part("rpcpassword") RequestBody rpc_password
    );


    //TODO: Get  Funding Node List  APi
    @GET("get-funding-nodes")
    //ok
    Call<FundingNodeListResp> get_Funding_Node_List(
    );
    //@FormUrlEncoded
    @GET("clients") //ok
    Call<ClientListModel> getInStoreClients(
            @Header("Authorization")String token
    );
    //TODO: Merchant Login APi
    @Headers("Accept: application/json")
    @POST("merchants_login") //ok
    Call<MerchantLoginResp> merchant_Loging(
            @Body JsonObject body
    );
    //TODO: Merchant Login APi
   /* @FormUrlEncoded
    @POST("merchants_login")
    //ok
    Call<MerchantLoginResp> merchant_Loging(
            @Field("merchant_id") String merchant_id,
            //@Field("merchant_name") String merchant_id,
            @Field("merchant_password") String merchant_password
    );*/

    @GET("all_merchant_file/{merchant_id}")
        //ok
    Call<GetItemImageRSP> getAllItemImageMerchant(@Path("merchant_id") int merchant_id);

    // Update Profile
    @Multipart
    @POST("add_mercahnt_file")
    Call<AddImageResp> addItemImageToMerchant(
            @Part("merchant_id") RequestBody merchant_id,
            @Part("upc") RequestBody upc,
            @Part("name") RequestBody name,
            @Part("quantity") RequestBody quantity,
            @Part("price") RequestBody price,
            @Part MultipartBody.Part photoid);

    @Multipart
    @POST("delete_mercahnt_file")
    Call<AddImageResp> DeleteItemImageToMerchant(
            @Part("merchant_id") RequestBody merchant_id,
            @Part("merchant_item_upc") RequestBody merchant_item_upc);

    @Multipart
    @POST("update_merchant_file")
    Call<AddImageResp> UpdateItemImageToMerchant(
            @Part("merchant_id") RequestBody merchant_id,
            @Part("upc") RequestBody upc,
            @Part("name") RequestBody name,
            @Part("quantity") RequestBody quantity,
            @Part("price") RequestBody price,
            @Part MultipartBody.Part photoid);

    //Download SSk Key File

    //On your api interface
    @POST("path/to/your/resource")
    @Streaming
    Response apiRequest();

    // option 2: using a dynamic URL
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);


    @FormUrlEncoded
    @POST("merchantsuser_login")
        //ok
    Call<Loginresponse> merchantsuser_login(
            @Field("merchant_id") String merchant_id,
            @Field("sign_in_username") String sign_in_username,
            @Field("password") String password,
            @Field("user_type") String user_type

    );

    @FormUrlEncoded
    @POST("get-session")
    Call<get_session_response> get_session(
            @Field("type") String type,
            @Field("key") String key
    );

    @FormUrlEncoded
    @POST("Refresh")
    Call<get_session_response> refresh(
            @Field("refresh") String accessToken,
            @Field("twoFactor") String twoFactor,
            @Field("time") String time
    );

    @GET("merchant_nearby_clients")
    Call<NearbyClientResponse> getNearbyClients(
            @Header("Authorization") String token
    );

}
