package com.sis.clightapp.fragments.checkout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.Functions2;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.CheckOutMainActivity;
import com.sis.clightapp.activity.HomeActivity;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFundChannel;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFunds;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeersChannels;
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.currency.CurrentAllRate;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

public class CheckOutsFragment2 extends CheckOutBaseFragment implements View.OnClickListener {
    private CheckOutsFragment2 checkOutsFragment2;
    EditText amount, rcieptnum;
    TextView[] btn = new TextView[12];
    TextView btnAddItem, btnCheckOut;
    double CurrentRateInBTC;
    ApiPaths fApiPaths;
    private WebSocketClient webSocketClient;
    Functions2 functions;
    CustomSharedPreferences sharedPreferences;
    Context fContext;
    TextView btcRate;
    Items newManualItem;
    TextView setTextWithSpan;
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    TextView clearout;
    static boolean isReceivableGet = false;
    double mSatoshiReceivable = 0;
    double btcReceivable = 0;
    double usdReceivable = 0;
    double mSatoshiCapacity = 0;
    double btcCapacity = 0;
    double usdCapacity = 0;
    double usdRemainingCapacity = 0;
    double btcRemainingCapacity = 0;
    int INTENT_AUTHENTICATE = 1234;
    boolean isFundingInfoGetSuccefully = false;
    Dialog clearOutDialog;


    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().stopService(new Intent(getContext(), MyLogOutService.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_outs2, container, false);
        isFundingInfoGetSuccefully = false;
        setTextWithSpan = view.findViewById(R.id.footer);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
        fContext = getContext();
        clearout = view.findViewById(R.id.clearout);
        exitFromServerProgressDialog = new ProgressDialog(getContext());
        exitFromServerProgressDialog.setMessage("Exiting");
        btcRate = view.findViewById(R.id.btcRateTextview);
        btnAddItem = view.findViewById(R.id.button);
        btnCheckOut = view.findViewById(R.id.imageView5);
        amount = view.findViewById(R.id.et_amount);
        rcieptnum = view.findViewById(R.id.recieptno);
        amount.setShowSoftInputOnFocus(false);
        gdaxUrl = new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());
        sharedPreferences = new CustomSharedPreferences();
        btnAddItem.setOnClickListener(view12 -> {
            String amountValue = amount.getText().toString();
            String recieptNumValue = rcieptnum.getText().toString();
            if (amountValue.isEmpty() || recieptNumValue.isEmpty()) {
                showToast("Enter the Amount and Receipt Number");
            } else {
                Items newItem = new Items();
                newItem.setName(recieptNumValue);
                newItem.setPrice(amountValue);
                newItem.setUPC(recieptNumValue);
                newItem.setQuantity("100000000");
                newItem.setSelectQuatity(1);
                newItem.setIsManual("Yes");
                addItemToCartDialog(newItem);
            }
        });
        btnCheckOut.setOnClickListener(view1 -> {
            ((CheckOutMainActivity) requireActivity()).swipeToCheckOutFragment3(2);
        });
        if (CheckNetwork.isInternetAvailable(fContext)) {
            subscribeChannel();
            getFundingNodeInfo();
        } else {
            setcurrentrate("Not Found");
            setReceivableAndCapacity("0", "0", false);
        }

        btn[0] = (TextView) view.findViewById(R.id.num1);
        btn[1] = (TextView) view.findViewById(R.id.num2);
        btn[2] = (TextView) view.findViewById(R.id.num3);
        btn[3] = (TextView) view.findViewById(R.id.num4);
        btn[4] = (TextView) view.findViewById(R.id.num5);
        btn[5] = (TextView) view.findViewById(R.id.num6);
        btn[6] = (TextView) view.findViewById(R.id.num7);
        btn[7] = (TextView) view.findViewById(R.id.num8);
        btn[8] = (TextView) view.findViewById(R.id.num9);
        btn[9] = (TextView) view.findViewById(R.id.num0);
        btn[10] = (TextView) view.findViewById(R.id.numdot);
        btn[11] = (TextView) view.findViewById(R.id.numC);
        for (int i = 0; i < 12; i++) {
            btn[i].setOnClickListener(this);
        }

        clearout.setOnClickListener(view13 -> {
            KeyguardManager km = (KeyguardManager) requireActivity().getSystemService(KEYGUARD_SERVICE);
            if (km.isKeyguardSecure()) {
                Intent authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "");
                startActivityForResult(authIntent, INTENT_AUTHENTICATE);
            } else {
                getListPeers();
            }
        });

        return view;
    }

    private void addItemToCartDialog(Items newItem) {
        final Items newItemToAdd = newItem;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.additem_title));
        builder.setMessage(getString(R.string.additem_subtitle));
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            newManualItem = newItemToAdd;
            amount.getText().clear();
            rcieptnum.getText().clear();
            if (newManualItem != null) {
                GlobalState.getInstance().addInmSeletedForPayDataSourceCheckOutInventory(newManualItem);
                ArrayList<Items> after = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
                int count = 0;
                for (Items items : after) {
                    count = count + items.getSelectQuatity();
                }
                ((CheckOutMainActivity) requireActivity()).updateCartIcon(count);

            }
            newManualItem = null;
            dialogInterface.dismiss();
        });

        // Actions if user selects 'no'
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            amount.getText().clear();
            rcieptnum.getText().clear();
            newManualItem = null;
            dialogInterface.dismiss();

        });

        // Create the alert dialog using alert dialog builder
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    public void onBackPressed() {
        ask_exit();
    }

    // Creating exit dialogue
    @SuppressLint("SetTextI18n")
    public void ask_exit() {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(getContext());
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        goAlertDialogwithOneBTnDialog.setCancelable(false);
        final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
        final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
        final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
        final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
        yesbtn.setText("Yes");
        nobtn.setText("No");
        alertTitle_tv.setText(getString(R.string.exit_title));
        alertMessage_tv.setText(getString(R.string.exit_subtitle));
        yesbtn.setOnClickListener(v -> {
            cleanAllDataSource();
            requireContext().stopService(new Intent(getContext(), MyLogOutService.class));
            Intent ii = new Intent(getContext(), HomeActivity.class);
            startActivity(ii);
        });
        nobtn.setOnClickListener(v -> goAlertDialogwithOneBTnDialog.dismiss());
        goAlertDialogwithOneBTnDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.num1:
                amount.append("1");
                break;
            case R.id.num2:
                amount.append("2");
                break;
            case R.id.num3:
                amount.append("3");
                break;
            case R.id.num4:
                amount.append("4");
                break;
            case R.id.num5:
                amount.append("5");
                break;
            case R.id.num6:
                amount.append("6");
                break;
            case R.id.num7:
                amount.append("7");
                break;
            case R.id.num8:
                amount.append("8");
                break;
            case R.id.num9:
                amount.append("9");
                break;
            case R.id.num0:
                amount.append("0");
                break;
            case R.id.numdot:
                amount.append(".");
                break;

            case R.id.numC:
                amount.setText("");
                break;
            default:

        }
    }

    public void refreshList() {

    }

    public CheckOutsFragment2() {
        // Required empty public constructor
    }

    public CheckOutsFragment2 getInstance() {
        if (checkOutsFragment2 == null) {
            checkOutsFragment2 = new CheckOutsFragment2();
        }
        return checkOutsFragment2;
    }


    @SuppressLint("SetTextI18n")
    private void setcurrentrate(String x) {
        btcRate.setText("$" + x);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {// HANDLE LockIng
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                getListPeers();
            }
        }
    }

    //Get Funding Node Infor
    private void getFundingNodeInfo() {
        Call<FundingNodeListResp> call = ApiClient.getRetrofit().create(ApiPaths.class).get_Funding_Node_List();
        call.enqueue(new Callback<FundingNodeListResp>() {
            @Override
            public void onResponse(@NonNull Call<FundingNodeListResp> call, @NonNull Response<FundingNodeListResp> response) {
                if (response.body() != null) {
                    if (response.body().getFundingNodesList() != null) {
                        if (response.body().getFundingNodesList().size() > 0) {
                            isFundingInfoGetSuccefully = true;
                            FundingNode fundingNode = response.body().getFundingNodesList().get(0);
                            GlobalState.getInstance().setFundingNode(fundingNode);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FundingNodeListResp> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    public void getListPeers() {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listpeers\"] }";

                try {
                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());

                    webSocket.send(String.valueOf(obj));
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                            requireActivity().runOnUiThread(() -> {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            });
                        } else {
                            parseJSONForListPeers(text);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, ByteString bytes) {
                Log.e("TAG", "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                //TODO: stuff
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                //TODO: stuff
                requireActivity().runOnUiThread(() -> showToast(String.valueOf(response)));
            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }


    private ListPeers parseJSONForListPeers(String jsonresponse) {
        Log.d("ListPeersParsingResponse", jsonresponse);
        ListPeers listFunds = null;
        boolean sta = false;
        JSONArray jsonArr = null;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonresponse);
            JSONArray ja_data = null;
            jsonArr = jsonObject.getJSONArray("peers");


            //jsonArr = new JSONArray(jsonresponse);
        } catch (Exception e) {
            Log.e("ListFundParsing1", e.getMessage());
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = jsonArr.getJSONObject(0);
            sta = true;
        } catch (Exception e) {
            //e.printStackTrace();
            sta = false;
            Log.e("ListFundParsing2", e.getMessage());
        }
        if (sta == true) {
            listFunds = new ListPeers();
            Gson gson = new Gson();
            boolean failed = false;
            try {
                listFunds = gson.fromJson(jsonObj.toString(), ListPeers.class);
                failed = false;
                if (listFunds != null) {
                    if (listFunds.getChannels() != null) {
                        if (listFunds.getChannels().size() > 0) {
                            isReceivableGet = true;
                            double msat = 0;
                            double mcap = 0;
                            for (ListPeersChannels tempListFundChanel : listFunds.getChannels()) {
                                if (listFunds.isConnected() && tempListFundChanel.state.equalsIgnoreCase("CHANNELD_NORMAL")) {
                                    String tempmsat = tempListFundChanel.getReceivable_msatoshi() + "";
                                    String tempmCap = tempListFundChanel.getSpendable_msatoshi() + "";
                                    double tmsat = 0;
                                    double tmcap = 0;
                                    try {
                                        tmsat = Double.parseDouble(tempmsat);
                                        tmcap = Double.parseDouble(tempmCap);
                                        BigDecimal value = new BigDecimal(tempmCap);
                                        double doubleValue = value.doubleValue();
                                        Log.e("StringToDouble:", String.valueOf(doubleValue));
                                    } catch (Exception e) {
                                        Log.e("StringToDouble:", e.getMessage());
                                    }
                                    msat = msat + tmsat;
                                    mcap = mcap + tmcap;
                                }
                            }
                            Log.e("Receivable", excatFigure2(msat));
                            Log.e("Capcaity", excatFigure2(mcap));

                            setReceivableAndCapacity(String.valueOf(msat), String.valueOf(mcap + msat), true);
                        }
                    }
                } else {
                    setReceivableAndCapacity("0", "0", false);
                }
            } catch (IllegalStateException | JsonSyntaxException exception) {
                Log.e("ListFundParsing3", exception.getMessage());
                failed = true;
            }
        } else {
            Log.e("Error", "Error");
            showToast("Wrong Response!!!");
        }
        return listFunds;
    }

    //Manipulate Receivable Amount
    private void setReceivableAndCapacity(String receivableMSat, String capcaityMSat, boolean sta) {
        mSatoshiReceivable = Double.parseDouble(receivableMSat);
        btcReceivable = mSatoshiReceivable / AppConstants.satoshiToMSathosi;
        btcReceivable = btcReceivable / AppConstants.btcToSathosi;
        usdReceivable = getUsdFromBtc(btcReceivable);
        mSatoshiCapacity = Double.parseDouble(capcaityMSat);
        btcCapacity = mSatoshiCapacity / AppConstants.satoshiToMSathosi;
        btcCapacity = btcCapacity / AppConstants.btcToSathosi;
        usdCapacity = getUsdFromBtc(btcCapacity);
        btcRemainingCapacity = btcCapacity /*- btcReceivable*/;
        usdRemainingCapacity = usdCapacity /*- usdReceivable*/;
        goToClearOutDialog(sta);
    }

    // TODO: Open The Clear Out Dialog
    private void goToClearOutDialog(final boolean isFetchData) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        clearOutDialog = new Dialog(getContext());
        clearOutDialog.setContentView(R.layout.clearout_dialog_layout);
        Objects.requireNonNull(clearOutDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //dialog.getWindow().setLayout(500, 500);
        clearOutDialog.setCancelable(false);
        TextView receivedVal = (TextView) clearOutDialog.findViewById(R.id.receivedVal);
        TextView capicityVal = (TextView) clearOutDialog.findViewById(R.id.capicityVal);
        TextView clearoutVal = (TextView) clearOutDialog.findViewById(R.id.clearoutVal);

        boolean isCanClearout = false;
        Log.e("BeforeDialogCap", String.valueOf(usdRemainingCapacity));
        Log.e("BeforeDialogRecv", String.valueOf(usdReceivable));
        if (isFetchData) {
            if (isReceivableGet) {
                capicityVal.setText(":$" + String.format("%.2f", round(usdRemainingCapacity, 2)));
                receivedVal.setText(":$" + String.format("%.2f", round(usdReceivable, 2)));
                clearoutVal.setText(":$" + String.format("%.2f", round(usdRemainingCapacity - usdReceivable, 2)));

                isCanClearout = true;
            } else {
                capicityVal.setText("N/A");
                receivedVal.setText("N/A");
                clearoutVal.setText("N/A");

                isCanClearout = false;
            }
        } else {
            capicityVal.setText("N/A");
            receivedVal.setText("N/A");
            clearoutVal.setText("N/A");

            isCanClearout = false;
        }
        final ImageView ivBack = clearOutDialog.findViewById(R.id.iv_back_invoice);
        Button noBtn = clearOutDialog.findViewById(R.id.noBtn);
        Button yesBtn = clearOutDialog.findViewById(R.id.yesBtn);
        ivBack.setOnClickListener(v -> clearOutDialog.dismiss());
        yesBtn.setOnClickListener(view -> {
            if (isFetchData) {
                sendReceivable();
            } else {
                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                builder.setMessage("Please Try Again!!")
                        .setCancelable(false)
                        .setPositiveButton("Retry!", (dialog, id) -> {
                            dialog.cancel();
                            clearOutDialog.dismiss();
                        }).show();
            }
            //  sendReceivable();
        });
        noBtn.setOnClickListener(view -> clearOutDialog.dismiss());

        clearOutDialog.show();

    }

    //Clear Out All Receivable Amount to Destination
    private void sendReceivable() {
        String routingNodeId = "";
        FundingNode fundingNode = GlobalState.getInstance().getFundingNode();
        if (fundingNode != null) {
            if (fundingNode.getNode_id() != null) {
                routingNodeId = fundingNode.getNode_id();
                String label = "clearout" + getUnixTimeStamp();
                long mSatoshiSpendableTotal = (long) (mSatoshiCapacity - mSatoshiReceivable);
                sendreceiveables(routingNodeId, mSatoshiSpendableTotal + "", label);

            } else {
                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                builder.setMessage("Funding Node Id is Missing")
                        .setCancelable(false)
                        .setPositiveButton("Retry!", (dialog, id) -> {
                            dialog.cancel();
                            getFundingNodeInfo();
                        }).show();
            }
        } else {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
            builder.setMessage("Funding Node Id is Missing")
                    .setCancelable(false)
                    .setPositiveButton("Retry!", (dialog, id) -> {
                        dialog.cancel();
                        getFundingNodeInfo();
                    }).show();
        }
    }

    private void subscribeChannel() {
        URI uri;
        try {
            uri = new URI("wss://ws.bitstamp.net/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {

                String json = "{\"event\":\"bts:subscribe\",\"data\":{\"channel\":\"live_trades_btcusd\"}}";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocketClient.send(String.valueOf(obj));


                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

                Log.i("WebSocket", "Session is starting");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;


                if (!s.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        final String subscription = jsonObject.getString("event");
                        final JSONObject objects = jsonObject.getJSONObject("data");
                        if (!subscription.equals("bts:subscription_succeeded")) {
                            requireActivity().runOnUiThread(() -> {
                                try {
                                    Channel_BTCResponseData channel_btcResponseData = new Channel_BTCResponseData();
                                    channel_btcResponseData.setId(objects.getInt("id"));
                                    channel_btcResponseData.setTimestamp((objects.getString("timestamp")));
                                    channel_btcResponseData.setAmount(objects.getDouble("amount"));
                                    channel_btcResponseData.setAmount_str((objects.getString("amount_str")));
                                    channel_btcResponseData.setPrice(objects.getDouble("price"));
                                    channel_btcResponseData.setPrice_str((objects.getString("price_str")));
                                    channel_btcResponseData.setType((objects.getInt("type")));
                                    channel_btcResponseData.setMicrotimestamp((objects.getString("microtimestamp")));
                                    channel_btcResponseData.setBuy_order_id(objects.getInt("buy_order_id"));
                                    channel_btcResponseData.setSell_order_id(objects.getInt("sell_order_id"));
//                                                    showToast(String.valueOf(channel_btcResponseData.getPrice()));
                                    CurrentSpecificRateData currentSpecificRateData = new CurrentSpecificRateData();
                                    currentSpecificRateData.setRateinbitcoin(channel_btcResponseData.getPrice());
                                    GlobalState.getInstance().setCurrentSpecificRateData(currentSpecificRateData);
                                    setcurrentrate(String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
                                    GlobalState.getInstance().setChannel_btcResponseData(channel_btcResponseData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });

                        }

                    } catch (JSONException ignored) {

                    }


                }


            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                requireActivity().runOnUiThread(() -> {
                });
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(100000);
        webSocketClient.setReadTimeout(600000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public void sendreceiveables(final String routingnode_id, final String mstoshiReceivable, final String lable) {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli keysend" + " " + routingnode_id + " " + mstoshiReceivable + "\"] }";
                try {
                    JSONObject obj = new JSONObject(json);
                    Log.d("My App", obj.toString());
                    webSocket.send(String.valueOf(obj));
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                requireActivity().runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                            webSocket.close(1000, null);
                            webSocket.cancel();
                            goTo2FaPasswordDialog();
                        } else {
                            Gson gson = new Gson();
                            Sendreceiveableresponse sendreceiveableresponse = gson.fromJson(text, Sendreceiveableresponse.class);
                            showToast(String.valueOf(sendreceiveableresponse.getMsatoshi()));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, ByteString bytes) {
                Log.e("TAG", "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                requireActivity().runOnUiThread(() -> showToast(String.valueOf(response)));

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

}
