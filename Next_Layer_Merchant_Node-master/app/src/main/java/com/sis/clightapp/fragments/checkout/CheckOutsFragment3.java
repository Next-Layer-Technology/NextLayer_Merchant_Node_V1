package com.sis.clightapp.fragments.checkout;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.Acknowledgement;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.UrlConstants;
import com.sis.clightapp.Utills.Utils;
import com.sis.clightapp.activity.CheckOutMainActivity;
import com.sis.clightapp.adapter.CheckOutMainListAdapter;
import com.sis.clightapp.adapter.MerchantNodeAdapter;
import com.sis.clightapp.fragments.printing.PrintDialogFragment;
import com.sis.clightapp.fragments.shared.ExitDialogFragment;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.CreateInvoice;
import com.sis.clightapp.model.GsonModel.FirebaseNotificationModel;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.model.GsonModel.InvoiceForPrint;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeersChannels;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.REST.GetRouteResponse;
import com.sis.clightapp.model.REST.nearby_clients.NearbyClientResponse;
import com.sis.clightapp.model.REST.nearby_clients.NearbyClients;
import com.sis.clightapp.model.WebsocketResponse.MWSWebSocketResponse;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.model.rbs.Payload;
import com.sis.clightapp.model.rbs.RBSRequest;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

public class CheckOutsFragment3 extends CheckOutBaseFragment {

    private CheckOutsFragment3 checkOutFragment3;
    Button paywithclightbtn;
    ImageView btnFlashPay;
    double totalGrandfinal = 0;
    private WebSocketClient webSocketClient;
    ListView checkoutPayItemslistview;
    private Dialog dialog, invoiceDialog;
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    CustomSharedPreferences sharedPreferences;
    Context fContext;
    TextView btcRate, totalpay, taxpay, grandtotal;
    CheckOutMainListAdapter checkOutPayItemAdapter;
    double priceInBTC = 0;
    double priceInCurrency = 0;
    double taxpayInBTC = 1;
    double taxpayInCurrency = 1;
    double grandTotalInCurrency = 0;
    double getGrandTotalInBTC = 0;
    double btcRatePerDollar = 0;
    ImageView qRCodeImage;
    Button confirpaymentbtn;
    String currentTransactionLabel = "";
    double taxInBtcAmount = 0;
    double taxtInCurennccyAmount = 0;
    double taxBtcOnP3ToPopUp = 0;
    double taxUsdOnP3ToPopUp = 0;

    TextView setTextWithSpan;
    String labelGlobal = "sale123";
    BroadcastReceiver blReceiver;

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
    MerchantData merchantData;

    private Socket socket;
    ProgressDialog confirmingProgressDialog;
    private boolean isCreatingInvoice = false;
    private BroadcastReceiver broadcastReceiver = null;
    private final IntentFilter intentFilter = new IntentFilter(AppConstants.PAYMENT_RECEIVED_NOTIFICATION);
    Dialog distributeGetPaidDialog;
    ArrayList<Items> selectedItems = new ArrayList<>();


    public CheckOutsFragment3() {
    }

    public CheckOutsFragment3 getInstance() {
        if (checkOutFragment3 == null) {
            checkOutFragment3 = new CheckOutsFragment3();
        }
        return checkOutFragment3;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().stopService(new Intent(requireContext(), MyLogOutService.class));
        if (blReceiver != null) {
            requireActivity().unregisterReceiver(blReceiver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_outs3, container, false);

        Log.d(TAG, "onCreateView: CheckOutsFragment3");

        isFundingInfoGetSuccefully = false;
        setTextWithSpan = view.findViewById(R.id.footer);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
        confirmInvoicePamentProgressDialog = new ProgressDialog(requireContext());
        confirmInvoicePamentProgressDialog.setMessage("Confirming Payment");
        updatingInventoryProgressDialog = new ProgressDialog(requireContext());
        updatingInventoryProgressDialog.setMessage("Updating..");
        createInvoiceProgressDialog = new ProgressDialog(requireContext());
        createInvoiceProgressDialog.setMessage("Creating Invoice");
        exitFromServerProgressDialog = new ProgressDialog(requireContext());
        exitFromServerProgressDialog.setMessage("Exiting");
        getItemListprogressDialog = new ProgressDialog(requireContext());
        getItemListprogressDialog.setMessage("Loading...");
        btcRate = view.findViewById(R.id.btcRateTextview);
        totalpay = view.findViewById(R.id.totalpay);
        taxpay = view.findViewById(R.id.taxpay);
        grandtotal = view.findViewById(R.id.grandtotal);
        fContext = requireContext();
        clearout = view.findViewById(R.id.clearout);
        gdaxUrl = new CustomSharedPreferences().getvalueofMWSCommand("mws_command", requireContext());
        sharedPreferences = new CustomSharedPreferences();
        String json = new CustomSharedPreferences().getvalueofMerchantData("data", requireContext());
        Gson gson = new Gson();
        merchantData = gson.fromJson(json, MerchantData.class);

        if (GlobalState.getInstance().getTax() != null) {
            taxpayInBTC = GlobalState.getInstance().getTax().getTaxInBTC();
            taxpayInCurrency = GlobalState.getInstance().getTax().getTaxInUSD();
        }
        if (CheckNetwork.isInternetAvailable(fContext)) {
            subscribeChannel();
            getFundingNodeInfo();
        } else {
            setReceivableAndCapacity("0", "0", false);
            setcurrentrate("Not Found");
        }
        paywithclightbtn = view.findViewById(R.id.imageView5);
        checkoutPayItemslistview = view.findViewById(R.id.checkout2listview);
        paywithclightbtn.setOnClickListener(view12 -> createGrandTotalForInvoice(null));
        clearout.setOnClickListener(view1 -> getListPeers());
        setAdapter();
        btnFlashPay = view.findViewById(R.id.btnFlashPay);
        btnFlashPay.setOnClickListener(v -> {
            confirmingProgressDialog.show();
            getNearbyClients();
        });

        confirmingProgressDialog = new ProgressDialog(fContext);
        confirmingProgressDialog.setMessage("Confirming...");
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setCanceledOnTouchOutside(false);
        selectedItems = GlobalState.getInstance().selectedItems;
        return view;
    }

    private void createGrandTotalForInvoice(NearbyClients nearbyClients) {
        Log.d("TEST_DOUBLE_CHECKOUT", "createGrandTotalForInvoice: ");
        if (selectedItems != null && selectedItems.size() > 0) {
            priceInCurrency = 0;
            priceInBTC = 0;
            grandTotalInCurrency = 0;
            getGrandTotalInBTC = 0;
            for (int q = 0; q < selectedItems.size(); q++) {
                priceInCurrency = priceInCurrency + Double.parseDouble(selectedItems.get(q).getPrice());
                //format  ::   Total : 1.25 BTC /  $34.95 USD
                if (GlobalState.getInstance().getChannel_btcResponseData() != null) {
                    priceInBTC = 1 / GlobalState.getInstance().getChannel_btcResponseData().getPrice();
                    priceInBTC = priceInBTC * priceInCurrency;
                    priceInBTC = Utils.Companion.round(priceInBTC, 9);
                    grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount;
                    getGrandTotalInBTC = priceInBTC + taxInBtcAmount;
                    getGrandTotalInBTC = Utils.Companion.round(getGrandTotalInBTC, 9);

                    dialogBoxForInvoice(nearbyClients);
                } else {
                    showToast("No BTC Rate");
                }
            }

        } else {
            showToast("Cart is Empty");
        }
    }

    private void setcurrentrate(String x) {
        btcRate.setText("$" + x + "BTC/USD");
    }

    public void onBackPressed() {
        new ExitDialogFragment().show(getChildFragmentManager(), null);
    }

    public void refreshAdapter() {
        setAdapter();
    }

    @SuppressLint("SetTextI18n")
    public void setAdapter() {
        if (selectedItems != null && selectedItems.size() > 0) {
            int countitem = 0;
            for (Items items : selectedItems) {
                countitem = countitem + items.getSelectQuatity();
            }
            ((CheckOutMainActivity) requireActivity()).updateCartIcon(countitem);
            checkOutPayItemAdapter = new CheckOutMainListAdapter(requireContext(), selectedItems);
            checkoutPayItemslistview.setAdapter(checkOutPayItemAdapter);
            checkoutPayItemslistview.setOnItemLongClickListener((adapterView, view, i, l) -> {

                final int position = i;

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(getString(R.string.delete_title));
                builder.setMessage(getString(R.string.delete_subtitle));
                builder.setCancelable(true);

                // Action if user selects 'yes'
                builder.setPositiveButton("Yes", (dialogInterface, i1) -> {

                    Items tem = selectedItems.get(position);

                    if (tem.getIsManual() != null) {
                        checkOutPayItemAdapter.notifyDataSetChanged();
                        setAdapter();

                    } else {
                        checkOutPayItemAdapter.notifyDataSetChanged();
                        setAdapter();
                    }
                });

                // Actions if user selects 'no'
                builder.setNegativeButton("No", (dialogInterface, i12) -> {
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
            ((CheckOutMainActivity) requireActivity()).updateCartIcon(0);
        }

        if (selectedItems != null && selectedItems.size() > 0) {
            priceInCurrency = 0;
            priceInBTC = 0;

            for (int q = 0; q < selectedItems.size(); q++) {
                double total;
                if (selectedItems.get(q).getPrice() != null) {
                    total = selectedItems.get(q).getSelectQuatity() * Double.parseDouble(selectedItems.get(q).getPrice());

                } else {
                    total = selectedItems.get(q).getSelectQuatity() * Double.parseDouble("0");

                }
                priceInCurrency = priceInCurrency + total;
                //format  ::   Total : 1.25 BTC /  $34.95 USD

                if (GlobalState.getInstance().getChannel_btcResponseData() != null) {

                    priceInBTC = 1 / GlobalState.getInstance().getChannel_btcResponseData().getPrice();
                    Log.e("btcbefore", String.valueOf(priceInBTC));
                    priceInBTC = priceInBTC * priceInCurrency;
                    Log.e("btcafter", String.valueOf(btcRatePerDollar));
                    priceInBTC = Utils.Companion.round(priceInBTC, 9);
                    priceInCurrency = Utils.Companion.round(priceInCurrency, 2);
                    DecimalFormat precision = new DecimalFormat("0.00");
                    totalpay.setText("Total:" + exactFigure(priceInBTC) + " BTC/ $" + precision.format(priceInCurrency));
                    double percent = GlobalState.getInstance().getTax().getTaxpercent() / 100;

                    taxInBtcAmount = priceInBTC * percent;
                    taxBtcOnP3ToPopUp = taxInBtcAmount;
                    taxInBtcAmount = Utils.Companion.round(taxInBtcAmount, 9);
                    taxtInCurennccyAmount = priceInCurrency * percent;
                    taxUsdOnP3ToPopUp = taxtInCurennccyAmount;
                    taxtInCurennccyAmount = Utils.Companion.round(taxtInCurennccyAmount, 2);
                    taxpay.setText("Tax:" + exactFigure(taxInBtcAmount) + " BTC/ $" + precision.format(taxtInCurennccyAmount));
                    grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount;
                    getGrandTotalInBTC = priceInBTC + taxInBtcAmount;
                    getGrandTotalInBTC = Utils.Companion.round(getGrandTotalInBTC, 9);
                    grandTotalInCurrency = Utils.Companion.round(grandTotalInCurrency, 2);
                    grandtotal.setText(exactFigure(getGrandTotalInBTC) + " BTC/ $" + precision.format(grandTotalInCurrency));
                    totalGrandfinal = getGrandTotalInBTC;
                } else {
                    totalpay.setText("Total:" + "0.0 BTC" + " / " + "0.00 $");
                }

            }
        } else {
            //set default rates
            totalpay.setText("Total:" + "0.0 BTC" + " / " + "0.00 $");
            taxpay.setText("Tax:0.0 BTC/0.00 $");
            grandtotal.setText("0.0 BTC/ 0.00 $");
        }
    }


    private void dialogBoxForInvoice(NearbyClients nearbyClients) {

        if (invoiceDialog != null && invoiceDialog.isShowing()) {
            return;
        }

        Log.d("TEST_DOUBLE_CHECKOUT", "dialogBoxForInvoice: ");
        long tsLong = System.currentTimeMillis() / 1000;
        String uNixtimeStamp = Long.toString(tsLong);
        double dmSatoshi;
        double dSatoshi;
        dSatoshi = totalGrandfinal * AppConstants.btcToSathosi;
        dmSatoshi = dSatoshi * AppConstants.satoshiToMSathosi;
        NumberFormat formatter = new DecimalFormat("#0");
        String rMSatoshi = formatter.format(dmSatoshi);
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        invoiceDialog = new Dialog(requireContext());
        invoiceDialog.setContentView(R.layout.dialoglayoutinvoice);
        Objects.requireNonNull(invoiceDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        invoiceDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
        invoiceDialog.setCancelable(true);
        confirpaymentbtn = invoiceDialog.findViewById(R.id.confirpaymentbtn);
        final EditText et_msatoshi = invoiceDialog.findViewById(R.id.et_msatoshi);
        et_msatoshi.setInputType(InputType.TYPE_NULL);
        et_msatoshi.setText(rMSatoshi);
        final EditText et_label = invoiceDialog.findViewById(R.id.et_lable);
        et_label.setInputType(InputType.TYPE_NULL);
        String label = "sale" + uNixtimeStamp;
        et_label.setText(label);
        labelGlobal = "sale" + uNixtimeStamp;
        final EditText et_description = invoiceDialog.findViewById(R.id.et_description);
        final ImageView ivBack = invoiceDialog.findViewById(R.id.iv_back_invoice);
        qRCodeImage = invoiceDialog.findViewById(R.id.imgQR);
        Button btnCreatInvoice = invoiceDialog.findViewById(R.id.btn_createinvoice);
        qRCodeImage.setVisibility(View.GONE);
        ivBack.setOnClickListener(v -> invoiceDialog.dismiss());

        btnCreatInvoice.setOnClickListener(v -> {
            String msatoshi = et_msatoshi.getText().toString();
            String label1 = et_label.getText().toString();
            String descrption = et_description.getText().toString();
            if (msatoshi.isEmpty()) {
                showToast("MSATOSHI" + getString(R.string.empty));
                return;
            }
            if (label1.isEmpty()) {
                showToast("Label" + getString(R.string.empty));
                return;
            }
            if (descrption.isEmpty()) {
                showToast("Description" + getString(R.string.empty));
                return;
            }
            currentTransactionLabel = label1;
            isCreatingInvoice = false;
            createInvoice(msatoshi, label1, descrption, nearbyClients);

        });


        confirpaymentbtn.setOnClickListener(view -> ListInvoices(currentTransactionLabel));

        if (nearbyClients != null) {
            createInvoice(rMSatoshi, label, "Flashpay", nearbyClients);
        } else {
            invoiceDialog.show();
        }

    }

    private CreateInvoice parseJSONForCreatInvocie(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<CreateInvoice>() {
        }.getType();
        CreateInvoice createInvoice = gson.fromJson(jsonString, type);
        GlobalState.getInstance().setCreateInvoice(createInvoice);
        return createInvoice;
    }

    private MWSWebSocketResponse parseMWSWebSocketResponse(String jsonString) {
        return new Gson().fromJson(jsonString, new TypeToken<MWSWebSocketResponse>() {
        }.getType());
    }


    public Bitmap getBitMapImg(String hex, int widht, int height) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, widht, height);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        return barcodeEncoder.createBitmap(bitMatrix);

    }

    public String exactFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));
        return d.toPlainString();
    }

    public void onActivityResult(int mRequestCode, int mResultCode, Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);
        if (mRequestCode == 1234) {// HANDLE LockIng
            if (mResultCode == RESULT_OK) {
                sendReceivable();
            }
        }
    }

    //Get Funding Node Info
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

    private void getNearbyClients() {
        String accessToken = new CustomSharedPreferences().getvalue("accessTokenLogin", requireContext());
        String token = "Bearer" + " " + accessToken;
        Call<NearbyClientResponse> call = ApiClient.getRetrofit().create(ApiPaths.class).getNearbyClients(token);
        call.enqueue(new Callback<NearbyClientResponse>() {
            @Override
            public void onResponse(@NonNull Call<NearbyClientResponse> call, @NonNull Response<NearbyClientResponse> response) {
                requireActivity().runOnUiThread(() -> confirmingProgressDialog.dismiss());
                if (response.body() != null) {
                    NearbyClientResponse clientListModel = response.body();
                    List<NearbyClients> list = clientListModel.getData();
                    if (list.size() > 0) {
                        //ArrayList<StoreClients> list1=list;
                        showDialogNearbyClients(list);
                    } else {
                        showToast("No client found");
                    }
                } else {
                    showToast(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<NearbyClientResponse> call, @NonNull Throwable t) {
                Log.e("merchant_nearby_clients:", Objects.requireNonNull(t.getMessage()));
                requireActivity().runOnUiThread(() -> confirmingProgressDialog.dismiss());
                showToast(t.getMessage());
            }
        });
    }

    private void showDialogNearbyClients(List<NearbyClients> list) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_merchant_node_scrollbar);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);

        MerchantNodeAdapter adapter = new MerchantNodeAdapter(list, requireContext(), nearbyClients -> {
            dialog.dismiss();
            isCreatingInvoice = false;
            createGrandTotalForInvoice(nearbyClients);
        });
        recyclerView.setAdapter(adapter);

        dialog.show();
    }

    private void parseJSONForListPeers(String jsonresponse) {
        Log.d("ListPeersParsingResponse", jsonresponse);
        ListPeers listFunds;
        JSONArray jsonArr;
        JSONObject jsonObject;

        try {
            Gson gson = new Gson();
            jsonObject = new JSONObject(jsonresponse);
            jsonArr = jsonObject.getJSONArray("peers");
            JSONObject jsonObj;
            jsonObj = jsonArr.getJSONObject(0);
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
                        Log.e("Receivable", excatFigure2(msat));
                        Log.e("Capacity", excatFigure2(mcap));

                        setReceivableAndCapacity(String.valueOf(msat), String.valueOf(mcap + msat), true);
                    }
                }
            } else {
                setReceivableAndCapacity("0", "0", false);
            }
        } catch (IllegalStateException | JsonSyntaxException | JSONException exception) {
            Log.e("ListFundParsing3", Objects.requireNonNull(exception.getMessage()));
        }

    }

    //Manipulate Receivable Amount
    private void setReceivableAndCapacity(String receivableMSat, String capcaityMSat, boolean sta) {
        mSatoshiReceivable = Double.parseDouble(receivableMSat);
        btcReceivable = mSatoshiReceivable / AppConstants.satoshiToMSathosi;
        btcReceivable = btcReceivable / AppConstants.btcToSathosi;
        usdReceivable = Utils.Companion.btcToUsd(btcReceivable);
        mSatoshiCapacity = Double.parseDouble(capcaityMSat);
        btcCapacity = mSatoshiCapacity / AppConstants.satoshiToMSathosi;
        btcCapacity = btcCapacity / AppConstants.btcToSathosi;
        usdCapacity = Utils.Companion.btcToUsd(btcCapacity);
        btcRemainingCapacity = btcCapacity;
        usdRemainingCapacity = usdCapacity;
        goToClearOutDialog(sta);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void goToClearOutDialog(final boolean isFetchData) {
        clearOutDialog = new Dialog(requireContext());
        clearOutDialog.setContentView(R.layout.clearout_dialog_layout);
        Objects.requireNonNull(clearOutDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        clearOutDialog.setCancelable(false);
        TextView receivedVal = clearOutDialog.findViewById(R.id.receivedVal);
        TextView capicityVal = clearOutDialog.findViewById(R.id.capicityVal);
        TextView clearoutVal = clearOutDialog.findViewById(R.id.clearoutVal);

        Log.e("BeforeDialogCap", String.valueOf(usdRemainingCapacity));
        Log.e("BeforeDialogRecv", String.valueOf(usdReceivable));
        if (isFetchData) {
            if (isReceivableGet) {
                capicityVal.setText(":$" + String.format("%.2f", Utils.Companion.round(usdRemainingCapacity, 2)));
                receivedVal.setText(":$" + String.format("%.2f", Utils.Companion.round(usdReceivable, 2)));
                clearoutVal.setText(":$" + String.format("%.2f", Utils.Companion.round(usdRemainingCapacity - usdReceivable, 2)));
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
                KeyguardManager km = (KeyguardManager) requireActivity().getSystemService(KEYGUARD_SERVICE);
                if (km.isKeyguardSecure()) {
                    Intent authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "");
                    startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                } else {
                    sendReceivable();
                }
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
                getRoute(routingNodeId, mSatoshiSpendableTotal + "");
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

    CreateInvoice globalInvoice;
    String globalRMSatoshi, globalLabel, globalDescrption;

    public void createInvoice(final String rMSatoshi, final String label, final String descrption, NearbyClients nearbyClients) {

        globalRMSatoshi = rMSatoshi;
        globalLabel = label;
        globalDescrption = descrption;

        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        if (!isCreatingInvoice) {
            isCreatingInvoice = true;
            Log.i("TAG", "CreateInvoice: " + gdaxUrl);

            WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
                @Override
                public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

                    String token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext());

                    String json = UrlConstants.getInvoiceSendCommand(token, rMSatoshi, label, descrption);

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
                    Log.e("TAG_onMessage", "MESSAGE: " + text);

                    MWSWebSocketResponse response = parseMWSWebSocketResponse(text);
                    if (response.isError()) {
                        Log.e("TAG_onMessage", "Error: " + response.getMessage());
                        requireActivity().runOnUiThread(() -> showToast(response.getMessage()));
                    } else if (response.getCode() == 724) {
                        Log.e("TAG_onMessage", response.getCode() + "");
                        requireActivity().runOnUiThread(() -> goTo2FaPasswordDialog());
                    } else if (response.getPayment_hash() != null) {
                        Log.e("TAG_onMessage", "Hash: " + response.getPayment_hash());
                        requireActivity().runOnUiThread(() -> {
                            globalInvoice = null;
                            CreateInvoice invoice = parseJSONForCreatInvocie(text);
                            globalInvoice = invoice;
                            if (nearbyClients != null) {
                                confirmingProgressDialog.show();
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                subscribeRbs(nearbyClients.getClient_id(), invoice.getBolt11());
                            } else {
                                performPaymentCollectionFunction();
                            }
                        });
                    } else {
                        showToast(text);
                    }
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
                public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                    Log.e("TAG", "FAIL: " + response);
                }
            };

            clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
            clientCoinPrice.dispatcher().executorService().shutdown();
        }
    }

    public void getListPeers() {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext());
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
                requireActivity().runOnUiThread(() -> parseJSONForListPeers(text));
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
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                requireActivity().runOnUiThread(() -> showToast(String.valueOf(response)));
            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void ListInvoices(final String lable) {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listinvoices" + lable + "\"] }";

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
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {

                requireActivity().runOnUiThread(() -> showToast(String.valueOf(response)));
            }
        };
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void getRoute(final String routingnode_id, final String mstoshiReceivable) {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli getroute" + " " + routingnode_id + " " + mstoshiReceivable + " " + 1 + "\"] }";
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
                    Gson gson = new Gson();
                    GetRouteResponse getRouteResponse = gson.fromJson(text, GetRouteResponse.class);
                    String mstoshiReceivableRemoveFee = String.valueOf(Long.parseLong(mstoshiReceivable) - (getRouteResponse.routes.get(0).msatoshi - Long.parseLong(mstoshiReceivable)));
                    sendreceiveables(routingnode_id, mstoshiReceivableRemoveFee);

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
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
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

                String token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext());
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
                    Gson gson = new Gson();
                    Sendreceiveableresponse sendreceiveableresponse = gson.fromJson(text, Sendreceiveableresponse.class);
                    showToast(String.valueOf(sendreceiveableresponse.getMsatoshi()));
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
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                requireActivity().runOnUiThread(() -> showToast(String.valueOf(response)));

            }
        };
        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    /*This method's success is unable to success at the moment of implementation, so I(developer) just called the performPaymentCollectionFunction()  in "err" of
    socket instead of "msg" of socket*/
    public void subscribeRbs(String clientId, String bolt11String) {

        String token = new CustomSharedPreferences().getvalue("accessTokenLogin", requireContext());

        final IO.Options options = new IO.Options();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Authorization", Collections.singletonList("Bearer " + token));
        headers.put("Content-Type", Collections.singletonList("application/json"));

        options.extraHeaders = headers;

        try {
            socket = IO.socket("https://realtime.nextlayer.live", options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.connect();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.v("subscribeRbs", "EVENT_CONNECT");
                Log.v("subscribeRbs", Arrays.toString(args));

                RBSRequest rbsRequest = new RBSRequest(clientId, "fp_merch2client_invoice", new Payload(bolt11String));

                JSONObject payload = new JSONObject();
                try {
                    payload.accumulate("invoice", rbsRequest.getPayload().getInvoice());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("to", rbsRequest.getTo());
                    jsonObject.accumulate("type", rbsRequest.getType());
                    jsonObject.accumulate("payload", payload);

                    Log.v("subscribeRbs", jsonObject.toString());

                    socket.emit("msg", jsonObject, new Acknowledgement() {
                        @Override
                        public void call(Object... args) {
                            super.call(args);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.v("subscribeRbs", "EVENT_CONNECT_ERROR");
            Log.v("subscribeRbs", Arrays.toString(args));
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.v("subscribeRbs", "EVENT_DISCONNECT");
            Log.v("subscribeRbs", Arrays.toString(args));
        });

        socket.on("err", args -> {
            Log.v("subscribeRbs", "err");
            Log.v("subscribeRbs", Arrays.toString(args));
            requireActivity().runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    String message = jsonObject.getString("message");
                    Log.v("subscribeRbs", "message -> " + message);
                    showToast(message);
                    performPaymentCollectionFunction();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                confirmingProgressDialog.dismiss();
            });


            socket.disconnect();
        });

        socket.on("msg", args -> {
            Log.v("subscribeRbs", "msg");
            Log.v("subscribeRbs", Arrays.toString(args));
        });
    }

    private void performPaymentCollectionFunction() {
        dialogBoxQRCodePayment();
        listenToFcmBroadcast();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void listenToFcmBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    Log.d("MyFirebaseMsgService", "BroadcastReceiver");
                    if (intent != null) {
                        Log.d("MyFirebaseMsgService", "intent != null");
                        if (intent.getExtras() != null) {
                            Log.d("MyFirebaseMsgService", "intent.getExtras() != null");
                            requireActivity().runOnUiThread(() -> {
                                FirebaseNotificationModel notificationModel = new Gson().fromJson(intent.getStringExtra(AppConstants.PAYMENT_INVOICE),
                                        FirebaseNotificationModel.class);
                                Log.d("MyFirebaseMsgService", notificationModel.getInvoice_label());
                                fcmReceived();
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        requireContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    private void fcmReceived() {
        if (distributeGetPaidDialog.isShowing()) {
            distributeGetPaidDialog.dismiss();
        }
        unregisterBroadcastReceiver();

        confirmPayment();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterBroadcastReceiver();
    }

    private void unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            requireContext().unregisterReceiver(broadcastReceiver);
        }
    }

    @SuppressLint("SetTextI18n")
    private void dialogBoxQRCodePayment() {

        if (distributeGetPaidDialog != null && distributeGetPaidDialog.isShowing()) {
            return;
        }

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        distributeGetPaidDialog = new Dialog(requireContext());
        distributeGetPaidDialog.setContentView(R.layout.dialoglayoutgetpaiddistribute);
        Objects.requireNonNull(distributeGetPaidDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        distributeGetPaidDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
        distributeGetPaidDialog.setCancelable(false);

        TextView titile = distributeGetPaidDialog.findViewById(R.id.tv_title);
        Button btnCreatInvoice = distributeGetPaidDialog.findViewById(R.id.btn_createinvoice);
        final EditText et_msatoshi = distributeGetPaidDialog.findViewById(R.id.et_msatoshi);
        final EditText et_label = distributeGetPaidDialog.findViewById(R.id.et_lable);
        final EditText et_description = distributeGetPaidDialog.findViewById(R.id.et_description);
        final ImageView ivBack = distributeGetPaidDialog.findViewById(R.id.iv_back_invoice);
        qRCodeImage = distributeGetPaidDialog.findViewById(R.id.imgQR);

        confirpaymentbtn = distributeGetPaidDialog.findViewById(R.id.confirpaymentbtn);
        titile.setText("Get Paid");
        et_label.setInputType(InputType.TYPE_NULL);
        et_label.setText(globalLabel);
        et_msatoshi.setText(globalRMSatoshi);
        et_description.setText(globalDescrption);

        qRCodeImage.setVisibility(View.GONE);
        confirpaymentbtn.setVisibility(View.GONE);
        btnCreatInvoice.setVisibility(View.GONE);
        confirpaymentbtn.setVisibility(View.GONE);

        if (globalInvoice != null) {
            if (globalInvoice.getBolt11() != null) {

                String temHax = globalInvoice.getBolt11();
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(temHax, BarcodeFormat.QR_CODE, 600, 600);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qRCodeImage.setImageBitmap(bitmap);
                    qRCodeImage.setVisibility(View.VISIBLE);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        }

        confirpaymentbtn.setOnClickListener(v -> fcmReceived());

        ivBack.setOnClickListener(v -> distributeGetPaidDialog.dismiss());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    timer.cancel();
                    requireActivity().runOnUiThread(() -> distributeGetPaidDialog.dismiss());
                } catch (Exception e) {
                    Log.d("Timer Exception", Objects.requireNonNull(e.getMessage()));
                }
            }
        }, 1000 * 60 * 5);

        distributeGetPaidDialog.show();
    }

    public void confirmPayment() {
        confirmingProgressDialog.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", requireContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listinvoices" + " " + globalLabel + "\"] }";
                try {
                    JSONObject obj = new JSONObject(json);
                    Log.d("My App", obj.toString());
                    webSocket.send(String.valueOf(obj));
                } catch (Throwable t) {
                    requireActivity().runOnUiThread(() -> confirmingProgressDialog.dismiss());
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

                            confirmingProgressDialog.dismiss();

                            goTo2FaPasswordDialog();
                        } else {
                            parseJSONForConfirmPaymentNew(text);
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
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                requireActivity().runOnUiThread(() -> {
                    confirmingProgressDialog.dismiss();
                    showToast(String.valueOf(response));
                });
            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    private void parseJSONForConfirmPaymentNew(String jsonString) {
        Gson gson = new Gson();
        JSONArray jsonArray;
        String json = "";
        try {
            jsonArray = new JSONObject(jsonString).getJSONArray("invoices");
            json = jsonArray.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Invoice invoice = gson.fromJson(json, Invoice.class);
        GlobalState.getInstance().setInvoice(invoice);


        if (invoice != null) {
            if (invoice.getStatus().equals("paid")) {
                dialogBoxForConfirmPaymentInvoice(invoice);
                confirmInvoicePamentProgressDialog.dismiss();
                confirmingProgressDialog.dismiss();
            } else {
                confirmingProgressDialog.dismiss();
                distributeGetPaidDialog.dismiss();
                confirmInvoicePamentProgressDialog.dismiss();
                new AlertDialog.Builder(requireContext())
                        .setMessage("Payment Not Recieved")
                        .setPositiveButton("Retry", null)
                        .show();

            }

        } else {
            confirmingProgressDialog.dismiss();
            distributeGetPaidDialog.dismiss();
            confirmInvoicePamentProgressDialog.dismiss();
            new AlertDialog.Builder(requireContext())
                    .setMessage("Payment Not Recieved")
                    .setPositiveButton("Retry", null)
                    .show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void dialogBoxForConfirmPaymentInvoice(final Invoice invoice) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        distributeGetPaidDialog = new Dialog(requireContext());
        distributeGetPaidDialog.setContentView(R.layout.customlayoutofconfirmpaymentdialogformerchantadmin);
        Objects.requireNonNull(distributeGetPaidDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        distributeGetPaidDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        distributeGetPaidDialog.setCancelable(false);
        //init dialog views
        final ImageView ivBack = distributeGetPaidDialog.findViewById(R.id.iv_back_invoice);
        final TextView amount = distributeGetPaidDialog.findViewById(R.id.et_amount);
        final ImageView payment_preImage = distributeGetPaidDialog.findViewById(R.id.et_preimage);
        final TextView paid_at = distributeGetPaidDialog.findViewById(R.id.et_paidat);
        final TextView purchased_Items = distributeGetPaidDialog.findViewById(R.id.et_perchaseditems);
        //  final TextView tax=distributeGetPaidDialog.findViewById(R.id.et_tax);
        final Button printInvoice = distributeGetPaidDialog.findViewById(R.id.btn_printinvoice);
        amount.setVisibility(View.GONE);
        payment_preImage.setVisibility(View.GONE);
        paid_at.setVisibility(View.GONE);
        purchased_Items.setVisibility(View.GONE);
        //   tax.setVisibility(View.GONE);
        printInvoice.setVisibility(View.GONE);

        if (invoice != null) {
            InvoiceForPrint invoiceForPrint = new InvoiceForPrint();
            if (invoice.getStatus().equals("paid")) {

                invoiceForPrint.setMsatoshi(invoice.getMsatoshi());
                invoiceForPrint.setPayment_preimage(invoice.getPayment_preimage());
                invoiceForPrint.setPaid_at(invoice.getPaid_at());
                invoiceForPrint.setPurchasedItems(invoice.getDescription());
                invoiceForPrint.setDesscription(invoice.getDescription());
                invoiceForPrint.setMode("distributeGetPaid");
                GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
                amount.setVisibility(View.VISIBLE);
                payment_preImage.setVisibility(View.VISIBLE);
                paid_at.setVisibility(View.VISIBLE);
                purchased_Items.setVisibility(View.VISIBLE);
                //    tax.setVisibility(View.VISIBLE);
                printInvoice.setVisibility(View.VISIBLE);
                double amounttempusd = Utils.Companion.round(Utils.Companion.btcToUsd(Utils.Companion.satoshiToBtc(invoice.getMsatoshi())), 2);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(exactFigure(Utils.Companion.round((Utils.Companion.btcToUsd(invoice.getMsatoshi())), 9)) + "BTC\n$" + precision.format(Utils.Companion.round(amounttempusd, 2)) + "USD");

                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                paid_at.setText(Utils.Companion.dateStringUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                purchased_Items.setText(invoice.getDescription());

            } else {
                invoiceForPrint.setMsatoshi(0.0);
                invoiceForPrint.setPayment_preimage("N/A");
                invoiceForPrint.setPaid_at(0);
                invoiceForPrint.setMode("distributeGetPaid");
                GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
                amount.setVisibility(View.VISIBLE);
                payment_preImage.setVisibility(View.VISIBLE);
                paid_at.setVisibility(View.VISIBLE);
                purchased_Items.setVisibility(View.VISIBLE);
                //    tax.setVisibility(View.VISIBLE);
                printInvoice.setVisibility(View.VISIBLE);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(exactFigure(Utils.Companion.round((Utils.Companion.satoshiToBtc(invoice.getMsatoshi())), 9)) + "BTC\n$" + precision.format(Utils.Companion.round(Utils.Companion.btcToUsd(Utils.Companion.satoshiToBtc(invoice.getMsatoshi())), 2)) + "USD");
                paid_at.setText(Utils.Companion.dateStringUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                purchased_Items.setText("N/A");

            }
        }
        printInvoice.setOnClickListener(view -> {
            InvoiceForPrint invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();
            if (invoice != null && invoice.getStatus().equals("paid")) {
                if (invoiceForPrint != null) {
                    new PrintDialogFragment().show(getChildFragmentManager(), null);
                }
            }
        });

        ivBack.setOnClickListener(v -> distributeGetPaidDialog.dismiss());
        distributeGetPaidDialog.show();
    }

}
