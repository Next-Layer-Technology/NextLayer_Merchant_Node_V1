package com.sis.clightapp.fragments.checkout;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.Functions2;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.CheckOutMain11;
import com.sis.clightapp.activity.MainActivity;

import com.sis.clightapp.adapter.CheckOutMainListAdapter;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemLIstModel;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemsDataMerchant;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFundChannel;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFunds;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeersChannels;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp;
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse;
import com.sis.clightapp.model.GsonModel.StringImageOfUPCItem;
import com.sis.clightapp.model.GsonModel.UPCofImages;
import com.sis.clightapp.model.ImageRelocation.GetItemImageRSP;
import com.sis.clightapp.model.ImageRelocation.GetItemImageReloc;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.currency.CurrentAllRate;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public class CheckOutFragment1 extends CheckOutBaseFragment {

    CheckOutFragment1 checkOutFragment1;
    int setwidht, setheight;
    Button checkOutbtn, scanUPCbtn;
    private WebSocketClient webSocketClient;
    ListView checkOutListVieww;
    CheckOutMainListAdapter checkOutMainListAdapter;
    TextView btcRate;
    double CurrentRateInBTC;
    //private final String gdaxUrl = "ws://98.226.215.246:8095/SendCommands";
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    ApiPaths fApiPaths;
    Functions2 functions;
    CustomSharedPreferences sharedPreferences;
    Context fContext;
    CheckBox mCheckBox;
    ArrayList<Items> mScanedDataSourceItemList;
    ArrayList<StringImageOfUPCItem> mDataSourceImageString;
    int cursize = 0;
    int totalSize = 0;
    static Boolean ASYNC_TASK_FINISHED;
    static Boolean isAddorDelete;
    static boolean isImageGet = false;
    static boolean isbarGet = true;

    //create scan object
    private IntentIntegrator qrScan;
    ProgressDialog confirmingProgressDialog;
    //
    TextView setTextWithSpan;
    // ClearOut KeySend//
    TextView receivable_tv, clearout, capacity_tv, tv_receivable;
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
    String TAG = "CheckOutFragment1";

    public CheckOutFragment1() {
        // Required empty public constructor
    }

    public CheckOutFragment1 getInstance() {
        if (checkOutFragment1 == null) {
            checkOutFragment1 = new CheckOutFragment1();
        }
        return checkOutFragment1;
    }

    private void setcurrentrate(String x) {
        btcRate.setText("$" + x + "BTC/USD");
    }

    private void getcurrentrate() {
        functions = new Functions2();
        fApiPaths = functions.retrofitBuilder();
        sharedPreferences = new CustomSharedPreferences();
        if (CheckNetwork.isInternetAvailable(getContext())) {
            final Call<CurrentAllRate> responseCall = fApiPaths.getCurrentAllRate();
            responseCall.enqueue(new Callback<CurrentAllRate>() {
                @Override
                public void onResponse(@NonNull Call<CurrentAllRate> call, @NonNull Response<CurrentAllRate> response) {

                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            CurrentAllRate temp = response.body();
                            //Log.d("NetworkStatus","succefully network call");
                            Log.e(TAG, "Getting Current BTC Rate");
                            CurrentSpecificRateData cSRDtemp = new CurrentSpecificRateData();
                            cSRDtemp.setRateinbitcoin(temp.getUSD().getLast());
                            GlobalState.getInstance().setCurrentAllRate(response.body());
                            sharedPreferences.setCurrentSpecificRateData(cSRDtemp, "CurrentSpecificRateData", fContext);


                        }
                    } else {

                        showToast("Unkown Error Occured");
                        setcurrentrate("Not BTC Rate Getting");

                    }
                }

                @Override
                public void onFailure(@NonNull Call<CurrentAllRate> call, @NonNull Throwable t) {

                    showToast("Network Call Error");
                    setcurrentrate("Not BTC Rate Getting");
                    // Log.d("2error",t.getMessage());

                }
            });

        } else {
            showToast("NEtwork Not Avaible");
            CurrentRateInBTC = 1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_out1, container, false);
        //intializing scan object
        setTextWithSpan = view.findViewById(R.id.footervtv);
        isFundingInfoGetSuccefully = false;
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
//        tv_receivable=view.findViewById(R.id.tv_receivable);
        checkOutbtn = view.findViewById(R.id.checkoutbtn);
        scanUPCbtn = view.findViewById(R.id.scanUPC);
        checkOutListVieww = view.findViewById(R.id.checkoutitemlist);
        clearout = view.findViewById(R.id.clearout);
        confirmingProgressDialog = new ProgressDialog(getContext());
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setMessage("Loading ...");
//        capacity_tv=view.findViewById(R.id.capacity_tv);
//        receivable_tv=view.findViewById(R.id.receivable_tv);
        mScanedDataSourceItemList = new ArrayList<>();
        GlobalState.getInstance().setmDataScannedForPage1(new ArrayList<Items>());
        qrScan = new IntentIntegrator(getActivity());
        qrScan.setOrientationLocked(false);
        String prompt = getResources().getString(R.string.enter_upc_code_via_scanner);
        qrScan.setPrompt(prompt);
        addItemprogressDialog = new ProgressDialog(getContext());
        addItemprogressDialog.setMessage("Adding Item");
        exitFromServerProgressDialog = new ProgressDialog(getContext());
        exitFromServerProgressDialog.setMessage("Exiting");
        getItemListprogressDialog = new ProgressDialog(getContext());
        getItemListprogressDialog.setMessage("Loading...");
        btcRate = view.findViewById(R.id.btcRateTextview);
        gdaxUrl = new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());

        findMerchant(new CustomSharedPreferences().getvalueofMerchantname("merchant_name", getContext()), new CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", getContext()));
        fContext = getContext();
        sharedPreferences = new CustomSharedPreferences();
        SubscrieChannel();
        if (CheckNetwork.isInternetAvailable(fContext)) {
            //getcurrentrate();
            getFundingNodeInfo();
        } else {
//            setReceivableAndCapacity("0", "0", false);
            setReceivableAndCapacity("0", "0", true);

            setcurrentrate("Not Found");
        }
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht = width * 45;
        setwidht = setwidht / 100;
        setheight = height / 2;
        scanUPCbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:on scan upc
                qrScan.forSupportFragment(CheckOutFragment1.this).initiateScan();

            }
        });
        checkOutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((CheckOutMain11) getActivity()).swipeToCheckOutFragment3(2);

            }
        });
        clearout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);
                    if (km.isKeyguardSecure()) {
                        Intent authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "");
                        startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                    } else {
                        //getReceivable();
                        //getfundslist();
                        getListPeers();
                    }
                }
            }
        });

        MerchantData merchantData = GlobalState.getInstance().getMerchantData();
        if (merchantData != null) {
            getAllItems();
            //getAllItemsImageList(merchantData);
        }

        return view;
    }

    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1234:
                // HANDLE LockIng
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == 1234) {
                    if (resultCode == RESULT_OK) {
                        //getReceivable();
                        // getfundslist();
                        getListPeers();
                    }
                }
                break;
            case 49374:
                // HANDLE QRSCAN
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    //if qrcode has nothing in it
                    if (result.getContents() == null) {
                        Toast.makeText(getContext(), "Result Not Found", Toast.LENGTH_LONG).show();
                    } else {
                        addItemprogressDialog.show();
                        addItemprogressDialog.setCancelable(false);
                        addItemprogressDialog.setCanceledOnTouchOutside(false);
                        String getUpc = result.getContents();
                        showToast(getUpc);
                        ArrayList<Items> itemsArrayList = new ArrayList<>();
                        itemsArrayList = GlobalState.getInstance().getmDataSourceCheckOutInventory();
                        if (itemsArrayList != null) {
                            if (itemsArrayList.size() > 0) {
                                for (int itr = 0; itr < itemsArrayList.size(); itr++) {
                                    if (itemsArrayList.get(itr).getUPC().equals(getUpc)) {
                                        if (GlobalState.getInstance().getmDataScannedForPage1() != null) {

                                        }
                                        if (GlobalState.getInstance().getmDataScannedForPage1().contains(itemsArrayList.get(itr))) {
                                            new AlertDialog.Builder(getContext())
                                                    .setMessage("Item Already Add")
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            showToast("Item Already Add");
                                        } else {
                                            itemsArrayList.get(itr).setSelectQuatity(1);
                                            GlobalState.getInstance().addInmDataScannedForPage1(itemsArrayList.get(itr));
                                            GlobalState.getInstance().addInmSeletedForPayDataSourceCheckOutInventory(itemsArrayList.get(itr));
                                            GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(GlobalState.getInstance().getmDataScannedForPage1());
                                            Log.d(TAG, "onActivityResult: 372");
                                            setAdapter();
                                        }
                                    }
                                }
                            }
                            addItemprogressDialog.dismiss();
                        } else {
                            showToast("No Item In Invetory");
                            addItemprogressDialog.dismiss();
                        }
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }


    public void onBackPressed() {
        //  hideCheckBox();
        ask_exit();
    }

    // Creating exit dialogue
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
        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanAllDataSource();
                goAlertDialogwithOneBTnDialog.dismiss();
                getContext().stopService(new Intent(getContext(), MyLogOutService.class));
                Intent ii = new Intent(getContext(), MainActivity.class);
                startActivity(ii);
            }
        });
        nobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAlertDialogwithOneBTnDialog.dismiss();
            }
        });
        goAlertDialogwithOneBTnDialog.show();


    }

    //Reloaded ALl Adapter
    private void setAdapter() {
        Log.d(TAG, "setAdapter: ");
        int countitem = 0;
        ArrayList<Items> ttd = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
        if (ttd != null) {
            Log.d(TAG, "setAdapter: ttd != null: " + ttd.toString());
            for (Items items : ttd) {
                countitem = countitem + items.getSelectQuatity();
            }
            ((CheckOutMain11) getActivity()).updateCartIcon(countitem);
        }
        final ArrayList<Items> dataSource = GlobalState.getInstance().getmDataScanedSourceCheckOutInventory();
        if (dataSource != null) {
            Log.d(TAG, "setAdapter: dataSource != null: " + Arrays.toString(Arrays.stream(dataSource.toArray()).toArray()));
            if (dataSource.size() > 0) {
                checkOutMainListAdapter = new CheckOutMainListAdapter(getContext(), dataSource);
                checkOutListVieww.setAdapter(checkOutMainListAdapter);
                checkOutListVieww.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final int position = i;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(getString(R.string.delete_title));
                        builder.setMessage(getString(R.string.delete_subtitle));
                        builder.setCancelable(true);
                        // Action if user selects 'yes'
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Items tem = dataSource.get(position);
                                if (tem.getIsManual() != null) {
                                    GlobalState.getInstance().removeInmSeletedForPayDataSourceCheckOutInventory(tem);
//                                dataSource.remove(position);
                                    checkOutMainListAdapter.notifyDataSetChanged();
                                    Log.d(TAG, "onClick: 465");
                                    setAdapter();
                                } else {
                                    GlobalState.getInstance().removeInMDataScannedForPage1(tem);
                                    GlobalState.getInstance().removeInmSeletedForPayDataSourceCheckOutInventory(tem);
                                    GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(GlobalState.getInstance().getmDataScannedForPage1());
//                                  dataSource.remove(position);
                                    checkOutMainListAdapter.notifyDataSetChanged();
                                    Log.d(TAG, "onClick: 473");
                                    setAdapter();
                                }
                            }
                        });
                        // Actions if user selects 'no'
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        // Create the alert dialog using alert dialog builder
                        AlertDialog dialog = builder.create();
                        dialog.setCanceledOnTouchOutside(false);
                        // Finally, display the dialog when user press back button
                        dialog.show();
                        return true;
                    }
                });
                GlobalState.getInstance().setCheckoutBtnPress(false);
            } else {
                //TODO:....
                checkOutMainListAdapter = new CheckOutMainListAdapter(getContext(), dataSource);
                checkOutListVieww.setAdapter(checkOutMainListAdapter);
                ((CheckOutMain11) getActivity()).updateCartIcon(0);
            }
        } else {
            ((CheckOutMain11) getActivity()).updateCartIcon(0);
            //TODO:....
        }
    }

    public void refreshAdapter(boolean isFirstTime) {
        Log.d(TAG, "refreshAdapter: " + isFirstTime);
        setAdapter();
    }

    public void getItemList() {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"db,get-list,items\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());

                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

            }

            @Override
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            } else {
                                setItemList(text);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.e("TAG", "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                //TODO: stuff
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                //TODO: stuff

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void setItemList(String result) {
        String response = result;
        String[] resaray = response.split(",");
        if (resaray[0].contains("resp")) {
            if (resaray[1].equals("ok")) {
                //showToast("Get Item ListSuccessfully");
                Log.e(TAG, "Get Item List Successfully");
                Log.e(TAG, "Result:" + result);
                String[] splitresponse = response.split(",");
                String jsonresponse = "";
                for (int i = 4; i < splitresponse.length; i++) {
                    jsonresponse += "," + splitresponse[i];
                }
                parseJSON(jsonresponse.substring(1));

            } else {
                showToast("Get Item List Failed");
                Log.e(TAG, "Get Item List  Failed");
                Log.e(TAG, "ErrorResult:" + result);
            }
        } else {
            showToast("Get Item List Failed");
            Log.e(TAG, "Get Item List  Failed");
            Log.e(TAG, "ErrorResult:" + result);
        }
    }

    private void parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Items>>() {
        }.getType();
        ArrayList<Items> itemsList = new ArrayList<>();
        //itemsList = gson.fromJson(jsonString, type);

        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() == null) {

        } else {
            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
        }
        ArrayList<GetItemImageReloc> itemImageRelocArrayList = GlobalState.getInstance().getCurrentItemImageRelocArrayList();
        if (itemsList.size() == 0) {
            for (int j = 0; j < itemImageRelocArrayList.size(); j++) {
                Items items = new Items();
//                if (itemImageRelocArrayList.get(j).getUpc_number() != null) {
                items.setUPC(itemImageRelocArrayList.get(j).getUpc_number());
//                } else if (itemImageRelocArrayList.get(j).getImage() != null) {
                items.setImageUrl(itemImageRelocArrayList.get(j).getImage());
//                } else if (itemImageRelocArrayList.get(j).getName() != null) {
                items.setName(itemImageRelocArrayList.get(j).getName());
//                } else
                if (itemImageRelocArrayList.get(j).getQuantity() != null) {
                    items.setQuantity(itemImageRelocArrayList.get(j).getQuantity());
                } else {
                    items.setQuantity("1");

                }
//                else if (itemImageRelocArrayList.get(j).getPrice() != null) {
                items.setPrice(itemImageRelocArrayList.get(j).getPrice());
//                } else if (itemImageRelocArrayList.get(j).getTotal_price() != 0) {
                items.setTotalPrice(itemImageRelocArrayList.get(j).getTotal_price());
//                } else if (itemImageRelocArrayList.get(j).getImage_in_hex() != null) {
                items.setImageInHex(itemImageRelocArrayList.get(j).getImage_in_hex());
//                } else if (itemImageRelocArrayList.get(j).getAdditional_info() != null) {
                items.setAdditionalInfo(itemImageRelocArrayList.get(j).getAdditional_info());
//                }
                itemsList.add(j, items);
            }

        }

        GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsList);
        GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(itemsList);
        for (Items items : itemsList) {
            Log.e("ItemsDetails", "Name:" + items.getName() + "-" + "Quantity:" + items.getQuantity() + "-" + "Price:" + items.getPrice() + "-" + "UPC:" + items.getUPC() + "-" + "ImageURl:" + items.getImageUrl());
        }

        //withOutImageRealodAdapter();
        //  getImagesOfItems();
        refreshAdapter(false);
    }

    private void withOutImageRealodAdapter() {
        ArrayList<Items> itemsArrayList = GlobalState.getInstance().getmDataSourceCheckOutInventory();
    }

    private void getAllItems() {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token = "Bearer" + " " + RefToken;
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", RefToken);
        String merchantId = new CustomSharedPreferences().getvalueofMerchantname("merchant_name", getContext());

        Call<ItemsDataMerchant> call = (Call<ItemsDataMerchant>) ApiClient2.getRetrofit().create(ApiPaths2.class).getInventoryItems(token);
        call.enqueue(new Callback<ItemsDataMerchant>() {
            @Override
            public void onResponse(Call<ItemsDataMerchant> call, Response<ItemsDataMerchant> response) {
                if (response.body() != null) {
                    ItemsDataMerchant itemsDataMerchant = response.body();
                    ArrayList<GetItemImageReloc> itemImageRelocArrayList = new ArrayList<GetItemImageReloc>();
                    if (itemsDataMerchant.getSuccess()) {
//                        showToast(itemsDataMerchant.getMessage());
                        List<ItemLIstModel> lIstModelList = itemsDataMerchant.getList();
                        for (int i = 0; i < lIstModelList.size(); i++) {
                            ItemLIstModel itemLIstModel = lIstModelList.get(i);
                            GetItemImageReloc getItemImageReloc = new GetItemImageReloc(Integer.parseInt(itemLIstModel.getId()), 1, itemLIstModel.getUpc_code(), itemLIstModel.getImage_path(), itemLIstModel.getName(), itemLIstModel.getQuantity_left(), itemLIstModel.getUnit_price(), "i", "1", 0.0, "i", "i", "i");
                            itemImageRelocArrayList.add(getItemImageReloc);
                        }
                        if (itemImageRelocArrayList.size() > 0) {
                            GlobalState.getInstance().setCurrentItemImageRelocArrayList(itemImageRelocArrayList);
                            parseJSON("");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ItemsDataMerchant> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });

    }

    private void getAllItemsImageList(MerchantData merchantData) {
        //merchantData.getId()
        Call<GetItemImageRSP> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).getAllItemImageMerchant(merchantData.getId());
        call.enqueue(new Callback<GetItemImageRSP>() {
            @Override
            public void onResponse(Call<GetItemImageRSP> call, Response<GetItemImageRSP> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getStatus().equals("success")) {
                            if (response.body().getMessage().equals("All the merchant file record")) {
                                if (response.body().getItemImageRelocArrayList() != null) {
                                    ArrayList<GetItemImageReloc> itemImageRelocArrayList = response.body().getItemImageRelocArrayList();
                                    if (itemImageRelocArrayList != null) {
                                        if (itemImageRelocArrayList.size() > 0) {
                                            GlobalState.getInstance().setCurrentItemImageRelocArrayList(itemImageRelocArrayList);
                                            parseJSON("");
                                        } else {
                                        }
                                    } else {
                                    }
                                } else {
                                }
                            } else {
                            }
                        } else {
                        }
                    } else {

                    }
                } else {
                }
            }

            @Override
            public void onFailure(Call<GetItemImageRSP> call, Throwable t) {
                showToast("ok");
            }
        });
    }

    private void parseJSONForStringImage(String jsonString, String upccr) {
        cursize = cursize + 1;
        // ||!isbarGet
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<StringImageOfUPCItem>>() {
        }.getType();
        ArrayList<StringImageOfUPCItem> stringImagesofUpcList = gson.fromJson(jsonString, type);
        stringImagesofUpcList.get(0).setUPC(upccr);
        mDataSourceImageString.addAll(stringImagesofUpcList);
        GlobalState.getInstance().setmStringImageOfUPCItems(mDataSourceImageString);
        if (cursize == totalSize || !isbarGet) {
            reloadAdapter();
            //showToast("ALL...");
        }
    }

    private void reloadAdapter() {
        ArrayList<UPCofImages> upCofImagesArrayList = GlobalState.getInstance().getmUPCListOfImagesDataSorce();
        ArrayList<Items> itemsArrayList = GlobalState.getInstance().getmDataSourceCheckOutInventory();
        ArrayList<StringImageOfUPCItem> stringImageOfUPCItemArrayList = GlobalState.getInstance().getmStringImageOfUPCItems();
        Log.e("Tes", "tst1");
        if (upCofImagesArrayList != null && stringImageOfUPCItemArrayList != null) {
            Log.e("Tes", "tst1");
            if (stringImageOfUPCItemArrayList.size() > 0) {
                if (itemsArrayList != null) {
                    for (int s = 0; s < itemsArrayList.size(); s++) {
                        for (int q = 0; q < stringImageOfUPCItemArrayList.size(); q++) {
                            if (itemsArrayList.get(s).getUPC().equals(stringImageOfUPCItemArrayList.get(q).getUPC())) {
                                itemsArrayList.get(s).setImageInHex(stringImageOfUPCItemArrayList.get(q).getImage());
                            }
                        }
                    }
                }
            }
        }
        if (itemsArrayList != null) {
            GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsArrayList);
            ArrayList<Items> temp = GlobalState.getInstance().getmDataSourceCheckOutInventory();
            showToast("");
        }
    }

    //Get Funding Node Infor
    private void getFundingNodeInfo() {
        Call<FundingNodeListResp> call = ApiClient.getRetrofit().create(ApiPaths.class).get_Funding_Node_List();
        call.enqueue(new Callback<FundingNodeListResp>() {
            @Override
            public void onResponse(Call<FundingNodeListResp> call, Response<FundingNodeListResp> response) {
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
            public void onFailure(Call<FundingNodeListResp> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });
    }

    //Get Receivable Amount

    private ListFunds parseJSONForListFunds(String jsonresponse) {
        Log.d("ListFundParsingResponse", jsonresponse);
        ListFunds listFunds = null;
        boolean sta = false;
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(jsonresponse);
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
        if (sta == false) {
            //String temp1 = jsonObj.toString();
            //Log.e("jsonObj",jsonObj.toString());
            listFunds = new ListFunds();
            Gson gson = new Gson();
            boolean failed = false;
            try {
                listFunds = gson.fromJson(jsonresponse, ListFunds.class);
                failed = false;
                if (listFunds != null) {
                    if (listFunds.getChannels() != null) {
                        if (listFunds.getChannels().size() > 0) {
                            isReceivableGet = true;
                            double msat = 0;
                            double mcap = 0;
                            for (ListFundChannel tempListFundChanel : listFunds.getChannels()) {
                                if (tempListFundChanel.isConnected()) {
                                    String tempmsat = tempListFundChanel.getOur_amount_msat();
                                    String tempmCap = tempListFundChanel.getAmount_msat();
                                    tempmsat = removeLastChars(tempmsat, 4);
                                    tempmCap = removeLastChars(tempmCap, 4);
                                    double tmsat = 0;
                                    double tmcap = 0;
                                    try {
                                        tmsat = Double.parseDouble(tempmsat);
                                        tmcap = Double.parseDouble(tempmCap);
                                    } catch (Exception e) {
                                        Log.e("StringToDouble:", e.getMessage());
                                    }
                                    msat = msat + tmsat;
                                    mcap = mcap + tmcap;
                                }
                            }
                            Log.e("Receivable", excatFigure2(msat));
                            Log.e("Capcaity", excatFigure2(mcap));

                            setReceivableAndCapacity(String.valueOf(msat), String.valueOf(mcap), true);
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
            //String temp1 = jsonObj.toString();
            //Log.e("jsonObj",jsonObj.toString());
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
//                                    if(tempmsat.length() > 4) {
//                                        tempmsat = removeLastChars(tempmsat, 4);
//                                    }
//
//                                    if(tempmCap.length() > 4) {
//                                        tempmCap = removeLastChars(tempmCap, 4);
//                                    }
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
        mSatoshiReceivable = Double.valueOf(receivableMSat);
        btcReceivable = mSatoshiReceivable / AppConstants.satoshiToMSathosi;
        btcReceivable = btcReceivable / AppConstants.btcToSathosi;
        usdReceivable = getUsdFromBtc(btcReceivable);
        mSatoshiCapacity = Double.valueOf(capcaityMSat);
        btcCapacity = mSatoshiCapacity / AppConstants.satoshiToMSathosi;
        btcCapacity = btcCapacity / AppConstants.btcToSathosi;
        usdCapacity = getUsdFromBtc(btcCapacity);
        btcRemainingCapacity = btcCapacity /*- btcReceivable*/;
        usdRemainingCapacity = usdCapacity /*- usdReceivable*/;
        goToClearOutDialog(sta);
        //receivable_tv.setText("$"+String.format("%.2f",round(usdReceivable,2)));
        //capacity_tv.setText("$"+String.format("%.2f",round(remainingCapacity,2)));
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
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearOutDialog.dismiss();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFetchData) {
                    sendReceivable();
                } else {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                    builder.setMessage("Please Try Again!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry!", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                    clearOutDialog.dismiss();
                                }
                            }).show();
                }
                //  sendReceivable();
            }
        });
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearOutDialog.dismiss();
            }
        });
        clearOutDialog.show();
    }

    //Clear Out All Receivable Amount to Destination
    private void sendReceivable() {
        String routingNodeId = "";
        FundingNode fundingNode = GlobalState.getInstance().getFundingNode();
        if (fundingNode != null) {
            if (fundingNode.getNode_id() != null) {
                routingNodeId = fundingNode.getNode_id();
                String mlattitude = "0.0";
                if (GlobalState.getInstance().getLattitude() != null) {
                    mlattitude = GlobalState.getInstance().getLattitude();
                }
                String mlongitude = "0.0";
                if (GlobalState.getInstance().getLongitude() != null) {
                    mlongitude = GlobalState.getInstance().getLongitude();
                }

                // rpc-cmd,cli-node,32.2601463_75.1623775,[ lightning-cli listpays ]
                String label = "clearout" + getUnixTimeStamp();
//                mSatoshiReceivable=mSatoshiReceivable-1;

                long mSatoshiSpendableTotal = (long) (mSatoshiCapacity - mSatoshiReceivable);

                sendreceiveables(routingNodeId, mSatoshiSpendableTotal + "", label);

            } else {
                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                builder.setMessage("Funding Node Id is Missing")
                        .setCancelable(false)
                        .setPositiveButton("Retry!", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                                getFundingNodeInfo();
                            }
                        }).show();
            }
        } else {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
            builder.setMessage("Funding Node Id is Missing")
                    .setCancelable(false)
                    .setPositiveButton("Retry!", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                            getFundingNodeInfo();
                        }
                    }).show();
        }
    }

    private void findMerchant(final String id, final String pass) {
        confirmingProgressDialog.show();
        JsonObject paramObject = new JsonObject();
        paramObject.addProperty("user_id", id);
        paramObject.addProperty("password", pass);
        Call<MerchantLoginResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchant_Loging(paramObject);
        call.enqueue(new Callback<MerchantLoginResp>() {
            @Override
            public void onResponse(Call<MerchantLoginResp> call, Response<MerchantLoginResp> response) {
                confirmingProgressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getMessage().equals("successfully login")) {
                            MerchantData merchantData = new MerchantData();
                            merchantData = response.body().getMerchantData();
                            MerchantData myObject = response.body().getMerchantData();
                            Gson gson = new Gson();
                            String json = gson.toJson(myObject);
                            new CustomSharedPreferences().setvalueofMerchantData(json, "data", getContext());
                            GlobalState.getInstance().setMerchant_id(id);
                            GlobalState.getInstance().setMerchantData(merchantData);

                            Tax tax = new Tax();
                            tax.setTaxInUSD(1.0);
                            tax.setTaxInBTC(0.00001);
                            tax.setTaxpercent(Double.valueOf(merchantData.getTax_rate()));
                            GlobalState.getInstance().setTax(tax);
                            sharedPreferences.setString(merchantData.getSsh_password(), "sshkeypass", getContext());
                            new CustomSharedPreferences().setvalueofMerchantname(id, "merchant_name", getContext());
                            new CustomSharedPreferences().setvalueofMerchantpassword(pass, "merchant_pass", getContext());
                            new CustomSharedPreferences().setvalueofMerchantId(merchantData.getId(), "merchant_id", getContext());

                        } else {
                        }
                    } else {

                    }
                } else {
                }

            }

            @Override
            public void onFailure(Call<MerchantLoginResp> call, Throwable t) {
                GlobalState.getInstance().setMerchantConfirm(false);
                confirmingProgressDialog.dismiss();

            }
        });

    }

    private void SubscrieChannel() {
        URI uri;
        try {
            // Connect to local host
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
                //Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;


                if (s.equals("")) {

                } else {

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        final String subscription = jsonObject.getString("event");
                        final JSONObject objects = jsonObject.getJSONObject("data");
                        if (subscription.equals("bts:subscription_succeeded")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    showToast(subscription);
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (objects != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {

                                                    Channel_BTCResponseData channel_btcResponseData = new Channel_BTCResponseData();
                                                    channel_btcResponseData.setId(objects.getInt("id"));
                                                    channel_btcResponseData.setTimestamp((objects.getString("timestamp")));
                                                    channel_btcResponseData.setAmount(Double.valueOf(objects.getDouble("amount")));
                                                    channel_btcResponseData.setAmount_str((objects.getString("amount_str")));
                                                    channel_btcResponseData.setPrice(Double.valueOf(objects.getDouble("price")));
                                                    channel_btcResponseData.setPrice_str((objects.getString("price_str")));
                                                    channel_btcResponseData.setType((objects.getInt("type")));
                                                    channel_btcResponseData.setMicrotimestamp((objects.getString("microtimestamp")));
                                                    channel_btcResponseData.setBuy_order_id(objects.getInt("buy_order_id"));
                                                    channel_btcResponseData.setSell_order_id(objects.getInt("sell_order_id"));
//                                                    showToast(String.valueOf(channel_btcResponseData.getPrice()));
                                                    CurrentSpecificRateData currentSpecificRateData = new CurrentSpecificRateData();
                                                    currentSpecificRateData.setRateinbitcoin(Double.valueOf(channel_btcResponseData.getPrice()));
                                                    GlobalState.getInstance().setCurrentSpecificRateData(currentSpecificRateData);
                                                    setcurrentrate(String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
                                                    GlobalState.getInstance().setChannel_btcResponseData(channel_btcResponseData);

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    } catch (JSONException err) {
                    }
                }
            }

            @Override
            public void onBinaryReceived(byte[] data) {
//                showToast("binary" + data.toString());
            }

            @Override
            public void onPingReceived(byte[] data) {
//                showToast("ping" + data.toString());
            }

            @Override
            public void onPongReceived(byte[] data) {
//                showToast("ping2" + data.toString());
            }

            @Override
            public void onException(final Exception e) {
                System.out.println(e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
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

    public void getfundslist() {

        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listfunds\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());

                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            } else {
                                parseJSONForListFunds(text);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.e("TAG", "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                //TODO: stuff
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                //TODO: stuff

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void getListPeers() {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

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
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            } else {
                                parseJSONForListPeers(text);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.e("TAG", "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                //TODO: stuff
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                //TODO: stuff

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(String.valueOf(response));
                    }
                });
            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void sendreceiveables(final String routingnode_id, final String mstoshiReceivable, final String lable) {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());

                //String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli invoice" + " " + routingnode_id + " " + mstoshiReceivable + " " + lable + "null 10" + "\"] }";

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
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            } else {
                                String response = text;
                                Gson gson = new Gson();
                                Sendreceiveableresponse sendreceiveableresponse = gson.fromJson(response, Sendreceiveableresponse.class);
                                showToast(String.valueOf(sendreceiveableresponse.getMsatoshi()));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.e("TAG", "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                //TODO: stuff
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                //TODO: stuff

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

}
