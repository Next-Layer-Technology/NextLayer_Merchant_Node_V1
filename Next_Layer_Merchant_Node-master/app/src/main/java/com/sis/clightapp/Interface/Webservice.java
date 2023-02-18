package com.sis.clightapp.Interface;


import com.google.gson.JsonObject;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp;
import com.sis.clightapp.model.ImageRelocation.AddImageResp;
import com.sis.clightapp.model.ImageRelocation.GetItemImageRSP;
import com.sis.clightapp.model.REST.ClientListModel;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.REST.Loginresponse;
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp;
import com.sis.clightapp.model.REST.TransactionResp;
import com.sis.clightapp.model.REST.get_session_response;
import com.sis.clightapp.model.REST.nearby_clients.NearbyClientResponse;
import com.sis.clightapp.model.currency.CurrentAllRate;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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

public interface Webservice {
    @GET("ticker")
    Call<CurrentAllRate> getCurrentAllRate();

    @FormUrlEncoded
    @POST("add-merchant-tx")
    Call<TransactionResp> add_merchant_transction(
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


    @GET("get-funding-nodes")
    Call<FundingNodeListResp> fundingNodeList();

    @GET("clients")
    Call<ClientListModel> getInStoreClients(
            @Header("Authorization") String token
    );

    @Headers("Accept: application/json")
    @POST("merchants_login")
    Call<MerchantLoginResp> merchant_Loging(@Body JsonObject body);

    @GET("all_merchant_file/{merchant_id}")
    Call<GetItemImageRSP> getAllItemImageMerchant(@Path("merchant_id") int merchant_id);

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



    @FormUrlEncoded
    @POST("merchantsuser_login")
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
