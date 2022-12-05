package com.sis.clightapp.fragments.checkout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.CheckOutMainActivity;
import com.sis.clightapp.activity.HomeActivity;

import com.sis.clightapp.adapter.CheckOutMainListAdapter;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemLIstModel;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemsDataMerchant;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeersChannels;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp;
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse;
import com.sis.clightapp.model.ImageRelocation.GetItemImageReloc;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    CheckBox cbList, cbScan;
    private WebSocketClient webSocketClient;
    ListView checkOutListView;
    CheckOutMainListAdapter checkOutMainListAdapter;
    TextView btcRate;
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    CustomSharedPreferences sharedPreferences;
    Context fContext;
    ArrayList<Items> mScanedDataSourceItemList;

    ProgressDialog confirmingProgressDialog;

    TextView setTextWithSpan;

    static boolean isReceivableGet = false;
    double mSatoshiReceivable = 0;
    double btcReceivable = 0;
    double usdReceivable = 0;
    double mSatoshiCapacity = 0;
    double btcCapacity = 0;
    double usdCapacity = 0;
    double usdRemainingCapacity = 0;
    double btcRemainingCapacity = 0;
    ArrayList<Items> itemsList = new ArrayList<>();

    int INTENT_AUTHENTICATE = 1234;

    boolean isFundingInfoGetSuccefully = false;
    Dialog clearOutDialog;
    String TAG = "CheckOutFragment1";

    boolean isListMode = true;
    boolean isScanMode = false;

    public CheckOutFragment1() {
    }

    public CheckOutFragment1 getInstance() {
        if (checkOutFragment1 == null) {
            checkOutFragment1 = new CheckOutFragment1();
        }
        return checkOutFragment1;
    }

    @SuppressLint("SetTextI18n")
    private void setcurrentrate(String x) {
        btcRate.setText("$" + x + "BTC/USD");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().stopService(new Intent(getContext(), MyLogOutService.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_out1, container, false);
        setTextWithSpan = view.findViewById(R.id.footervtv);
        isFundingInfoGetSuccefully = false;
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
        checkOutbtn = view.findViewById(R.id.checkoutbtn);
        cbList = view.findViewById(R.id.cbList);
        cbScan = view.findViewById(R.id.cbScan);

        cbList.setOnClickListener((compoundButton) -> {
            isListMode = true;
            isScanMode = false;
            cbScan.setChecked(false);
            cbList.setChecked(true);
            checkOutMainListAdapter = new CheckOutMainListAdapter(requireContext(), itemsList);
            if (checkOutListView != null)
                checkOutListView.setAdapter(checkOutMainListAdapter);
        });
        cbScan.setOnClickListener((compoundButton) -> {
            isScanMode = true;
            isListMode = false;
            cbList.setChecked(false);
            cbScan.setChecked(true);
            checkOutMainListAdapter = new CheckOutMainListAdapter(requireContext(), GlobalState.getInstance().selectedItems);
            checkOutListView.setAdapter(checkOutMainListAdapter);
        });
        scanUPCbtn = view.findViewById(R.id.scanUPC);
        checkOutListView = view.findViewById(R.id.checkoutitemlist);
        confirmingProgressDialog = new ProgressDialog(getContext());
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setMessage("Loading ...");
        mScanedDataSourceItemList = new ArrayList<>();
        //create scan object
        IntentIntegrator qrScan = new IntentIntegrator(getActivity());
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
        subscrieChannel();
        if (CheckNetwork.isInternetAvailable(fContext)) {
            getFundingNodeInfo();
        } else {
            setReceivableAndCapacity("0", "0", true);
            setcurrentrate("Not Found");
        }
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht = width * 45;
        setwidht = setwidht / 100;
        setheight = height / 2;
        scanUPCbtn.setOnClickListener(view13 -> {
            //TODO:on scan upc
            IntentIntegrator.forSupportFragment(CheckOutFragment1.this).initiateScan();

        });
        checkOutbtn.setOnClickListener(view1 -> ((CheckOutMainActivity) requireActivity()).swipeToCheckOutFragment3(2));
        view.findViewById(R.id.clearout).setOnClickListener(view12 -> {
            KeyguardManager km = (KeyguardManager) requireActivity().getSystemService(KEYGUARD_SERVICE);
            if (km.isKeyguardSecure()) {
                Intent authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "");
                startActivityForResult(authIntent, INTENT_AUTHENTICATE);
            } else {
                getListPeers();
            }
        });

        MerchantData merchantData = GlobalState.getInstance().getMerchantData();
        if (merchantData != null) {
            getAllItems();
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
                if (resultCode == RESULT_OK) {
                    getListPeers();
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
                        if (itemsList.size() > 0) {
                            for (int itr = 0; itr < itemsList.size(); itr++) {
                                if (itemsList.get(itr).getUPC().equals(getUpc)) {
                                    if ( GlobalState.getInstance().selectedItems.contains(itemsList.get(itr))) {
                                        new AlertDialog.Builder(getContext())
                                                .setMessage("Item Already Add")
                                                .setPositiveButton("OK", null)
                                                .show();
                                        showToast("Item Already Add");
                                    } else {
                                        itemsList.get(itr).setSelectQuatity(1);
                                        GlobalState.getInstance().selectedItems.add(itemsList.get(itr));
                                        Log.d(TAG, "onActivityResult: 372");
                                        setAdapter();
                                        break;
                                    }
                                }
                            }
                            addItemprogressDialog.dismiss();
                        } else {
                            showToast("No Item In Inventory");
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
            goAlertDialogwithOneBTnDialog.dismiss();
            requireActivity().stopService(new Intent(getContext(), MyLogOutService.class));
            Intent ii = new Intent(getContext(), HomeActivity.class);
            startActivity(ii);
        });
        nobtn.setOnClickListener(v -> goAlertDialogwithOneBTnDialog.dismiss());
        goAlertDialogwithOneBTnDialog.show();


    }

    //Reloaded ALl Adapter
    public void setAdapter() {
        int countItem = 0;
        for (Items items : GlobalState.getInstance().selectedItems) {
            countItem = countItem + items.getSelectQuatity();
        }
        ((CheckOutMainActivity) requireActivity()).updateCartIcon(countItem);

        ArrayList<Items> dataSource;

        if (isListMode) {
            dataSource = itemsList;
        } else {
            dataSource = GlobalState.getInstance().selectedItems;
        }

        if (dataSource != null) {
            Log.d(TAG, "setAdapter: dataSource != null: " + Arrays.toString(Arrays.stream(dataSource.toArray()).toArray()));
            if (dataSource.size() > 0) {
                checkOutMainListAdapter = new CheckOutMainListAdapter(requireContext(), dataSource);
                checkOutListView.setAdapter(checkOutMainListAdapter);
                checkOutListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getString(R.string.delete_title));
                    builder.setMessage(getString(R.string.delete_subtitle));
                    builder.setCancelable(true);
                    // Action if user selects 'yes'
                    builder.setPositiveButton("Yes", (dialogInterface, i12) -> {
                        checkOutMainListAdapter.notifyDataSetChanged();
                    });
                    // Create the alert dialog using alert dialog builder
                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    // Finally, display the dialog when user press back button
                    dialog.show();
                    return true;
                });
                GlobalState.getInstance().setCheckoutBtnPress(false);
            } else {
                checkOutMainListAdapter = new CheckOutMainListAdapter(requireContext(), dataSource);
                checkOutListView.setAdapter(checkOutMainListAdapter);
                ((CheckOutMainActivity) requireActivity()).updateCartIcon(0);
            }
        } else {
            ((CheckOutMainActivity) requireActivity()).updateCartIcon(0);
        }
    }


    private void parseJSON() {
        ArrayList<GetItemImageReloc> itemImageRelocArrayList = GlobalState.getInstance().getCurrentItemImageRelocArrayList();
        itemsList.clear();
        itemsList.size();
        for (int j = 0; j < itemImageRelocArrayList.size(); j++) {
            Items items = new Items();
            items.setUPC(itemImageRelocArrayList.get(j).getUpc_number());
            items.setImageUrl(itemImageRelocArrayList.get(j).getImage());
            items.setName(itemImageRelocArrayList.get(j).getName());
            if (itemImageRelocArrayList.get(j).getQuantity() != null) {
                items.setQuantity(itemImageRelocArrayList.get(j).getQuantity());
            } else {
                items.setQuantity("1");
            }
            items.setPrice(itemImageRelocArrayList.get(j).getPrice());
            items.setTotalPrice(itemImageRelocArrayList.get(j).getTotal_price());
            items.setImageInHex(itemImageRelocArrayList.get(j).getImage_in_hex());
            items.setAdditionalInfo(itemImageRelocArrayList.get(j).getAdditional_info());
            itemsList.add(j, items);
        }

        for (Items items : itemsList) {
            Log.e("ItemsDetails", "Name:" + items.getName() + "-" + "Quantity:" + items.getQuantity() + "-" + "Price:" + items.getPrice() + "-" + "UPC:" + items.getUPC() + "-" + "ImageURl:" + items.getImageUrl());
        }

        setAdapter();
    }

    private void getAllItems() {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token = "Bearer" + " " + RefToken;
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", RefToken);

        Call<ItemsDataMerchant> call = ApiClient2.getRetrofit().create(ApiPaths2.class).getInventoryItems(token);
        call.enqueue(new Callback<ItemsDataMerchant>() {
            @Override
            public void onResponse(@NonNull Call<ItemsDataMerchant> call, @NonNull Response<ItemsDataMerchant> response) {
                if (response.body() != null) {
                    ItemsDataMerchant itemsDataMerchant = response.body();
                    ArrayList<GetItemImageReloc> itemImageRelocArrayList = new ArrayList<>();
                    if (itemsDataMerchant.getSuccess()) {
                        List<ItemLIstModel> lIstModelList = itemsDataMerchant.getList();
                        for (int i = 0; i < lIstModelList.size(); i++) {
                            ItemLIstModel itemLIstModel = lIstModelList.get(i);
                            GetItemImageReloc getItemImageReloc = new GetItemImageReloc(Integer.parseInt(itemLIstModel.getId()), 1, itemLIstModel.getUpc_code(), itemLIstModel.getImage_path(), itemLIstModel.getName(), itemLIstModel.getQuantity_left(), itemLIstModel.getUnit_price(), "i", "1", 0.0, "i", "i", "i");
                            itemImageRelocArrayList.add(getItemImageReloc);
                        }
                        if (itemImageRelocArrayList.size() > 0) {
                            GlobalState.getInstance().setCurrentItemImageRelocArrayList(itemImageRelocArrayList);
                            parseJSON();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemsDataMerchant> call, @NonNull Throwable t) {
                Log.e("get-funding-nodes:", Objects.requireNonNull(t.getMessage()));
            }
        });

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


    private void parseJSONForListPeers(String jsonresponse) {
        Log.d("ListPeersParsingResponse", jsonresponse);
        ListPeers listFunds;
        boolean sta = false;
        JSONArray jsonArr = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonresponse);
            jsonArr = jsonObject.getJSONArray("peers");

        } catch (Exception e) {
            Log.e("ListFundParsing1", Objects.requireNonNull(e.getMessage()));
        }
        JSONObject jsonObj = null;
        try {
            assert jsonArr != null;
            jsonObj = jsonArr.getJSONObject(0);
            sta = true;
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("ListFundParsing2", Objects.requireNonNull(e.getMessage()));
        }
        if (sta) {
            //String temp1 = jsonObj.toString();
            //Log.e("jsonObj",jsonObj.toString());
            Gson gson = new Gson();
            try {
                listFunds = gson.fromJson(jsonObj.toString(), ListPeers.class);
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
                                        Log.e("StringToDouble:", Objects.requireNonNull(e.getMessage()));
                                    }
                                    msat = msat + tmsat;
                                    mcap = mcap + tmcap;
                                }
                            }
                            setReceivableAndCapacity(String.valueOf(msat), String.valueOf(mcap + msat), true);
                        }
                    }
                } else {
                    setReceivableAndCapacity("0", "0", false);
                }
            } catch (IllegalStateException | JsonSyntaxException exception) {
                Log.e("ListFundParsing3", Objects.requireNonNull(exception.getMessage()));
            }
        } else {
            Log.e("Error", "Error");
            showToast("Wrong Response!!!");
        }
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void goToClearOutDialog(final boolean isFetchData) {
        clearOutDialog = new Dialog(getContext());
        clearOutDialog.setContentView(R.layout.clearout_dialog_layout);
        Objects.requireNonNull(clearOutDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //dialog.getWindow().setLayout(500, 500);
        clearOutDialog.setCancelable(false);
        TextView receivedVal = clearOutDialog.findViewById(R.id.receivedVal);
        TextView capicityVal = clearOutDialog.findViewById(R.id.capicityVal);
        TextView clearoutVal = clearOutDialog.findViewById(R.id.clearoutVal);
        if (isFetchData) {
            if (isReceivableGet) {
                capicityVal.setText(":$" + String.format("%.2f", round(usdRemainingCapacity, 2)));
                receivedVal.setText(":$" + String.format("%.2f", round(usdReceivable, 2)));
                clearoutVal.setText(":$" + String.format("%.2f", round(usdRemainingCapacity - usdReceivable, 2)));
            } else {
                capicityVal.setText("N/A");
                receivedVal.setText("N/A");
                clearoutVal.setText("N/A");
            }
        } else {
            capicityVal.setText("N/A");
            receivedVal.setText("N/A");
            clearoutVal.setText("N/A");

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
        });
        noBtn.setOnClickListener(view -> clearOutDialog.dismiss());
        clearOutDialog.show();
    }

    //Clear Out All Receivable Amount to Destination
    private void sendReceivable() {
        String routingNodeId;
        FundingNode fundingNode = GlobalState.getInstance().getFundingNode();
        if (fundingNode != null) {
            if (fundingNode.getNode_id() != null) {
                routingNodeId = fundingNode.getNode_id();
                long mSatoshiSpendableTotal = (long) (mSatoshiCapacity - mSatoshiReceivable);
                sendreceiveables(routingNodeId, mSatoshiSpendableTotal + "");
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

    private void findMerchant(final String id, final String pass) {
        confirmingProgressDialog.show();
        JsonObject paramObject = new JsonObject();
        paramObject.addProperty("user_id", id);
        paramObject.addProperty("password", pass);
        Call<MerchantLoginResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).merchant_Loging(paramObject);
        call.enqueue(new Callback<MerchantLoginResp>() {
            @Override
            public void onResponse(@NonNull Call<MerchantLoginResp> call, @NonNull Response<MerchantLoginResp> response) {
                confirmingProgressDialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getMessage().equals("successfully login")) {
                            MerchantData merchantData;
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
                            tax.setTaxpercent(Double.parseDouble(merchantData.getTax_rate()));
                            GlobalState.getInstance().setTax(tax);
                            sharedPreferences.setString(merchantData.getSsh_password(), "sshkeypass", getContext());
                            new CustomSharedPreferences().setvalueofMerchantname(id, "merchant_name", getContext());
                            new CustomSharedPreferences().setvalueofMerchantpassword(pass, "merchant_pass", getContext());
                            new CustomSharedPreferences().setvalueofMerchantId(merchantData.getId(), "merchant_id", getContext());

                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MerchantLoginResp> call, @NonNull Throwable t) {
                GlobalState.getInstance().setMerchantConfirm(false);
                confirmingProgressDialog.dismiss();

            }
        });

    }

    private void subscrieChannel() {
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
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                if (!s.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        final String subscription = jsonObject.getString("event");
                        final JSONObject objects = jsonObject.getJSONObject("data");
                        if (subscription.equals("bts:subscription_succeeded")) {
                            requireActivity().runOnUiThread(() -> {
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> {
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
                                        CurrentSpecificRateData currentSpecificRateData = new CurrentSpecificRateData();
                                        currentSpecificRateData.setRateinbitcoin(channel_btcResponseData.getPrice());
                                        GlobalState.getInstance().setCurrentSpecificRateData(currentSpecificRateData);
                                        setcurrentrate(String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
                                        GlobalState.getInstance().setChannel_btcResponseData(channel_btcResponseData);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
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
                            webSocket.close(1000, null);
                            webSocket.cancel();
                            goTo2FaPasswordDialog();
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

    public void sendreceiveables(final String routingnode_id, final String mstoshiReceivable) {
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
