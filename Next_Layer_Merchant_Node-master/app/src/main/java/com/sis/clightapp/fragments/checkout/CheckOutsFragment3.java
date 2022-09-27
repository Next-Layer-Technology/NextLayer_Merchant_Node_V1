package com.sis.clightapp.fragments.checkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.Functions2;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.Print.PrintPic;
import com.sis.clightapp.Utills.Print.PrinterCommands;
import com.sis.clightapp.activity.CheckOutMain11;
import com.sis.clightapp.activity.MainActivity;
import com.sis.clightapp.adapter.CheckOutPayItemAdapter;
import com.sis.clightapp.adapter.SelectClientList;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.ConfirmInvoice.ConfirmInvoiceResp;
import com.sis.clightapp.model.GsonModel.CreateInvoice;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.model.GsonModel.InvoiceForPrint;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFundChannel;
import com.sis.clightapp.model.GsonModel.ListFunds.ListFunds;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeers;
import com.sis.clightapp.model.GsonModel.ListPeers.ListPeersChannels;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Sendreceiveableresponse;
import com.sis.clightapp.model.REST.ClientListModel;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.REST.FundingNodeListResp;
import com.sis.clightapp.model.REST.StoreClients;
import com.sis.clightapp.model.REST.TransactionInfo;
import com.sis.clightapp.model.REST.TransactionResp;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.WebsocketResponse.WebSocketOTPresponse;
import com.sis.clightapp.model.currency.CurrentAllRate;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

public class CheckOutsFragment3 extends CheckOutBaseFragment {

    private CheckOutsFragment3 checkOutFragment3;
    Button paywithclightbtn,btnFlashPay;
    //RelativeLayout btnFlashPay;
    double totalGrandfinal = 0;
    private WebSocketClient webSocketClient;
    ListView checkoutPayItemslistview;
    ListView storeList;
    private ProgressBar progressBar, mainProgressBar;
    private Dialog dialog, invoiceDialog, confirmPaymentDialog;
    int intScreenWidth, intScreenHeight;
    double CurrentRateInBTC;
    //private final String gdaxUrl = "ws://98.226.215.246:8095/SendCommands";
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    ApiPaths fApiPaths;
    Functions2 functions;
    CustomSharedPreferences sharedPreferences;
    Context fContext;
    TextView btcRate, totalpay, taxpay, grandtotal;
    CheckOutPayItemAdapter checkOutPayItemAdapter;
    SelectClientList selectClientListAdapter;
    double priceInBTC = 0;
    double priceInCurrency = 0;
    double taxpayInBTC = 1;
    double taxpayInCurrency = 1;
    double grandTotalInCurrency = 0;
    double getGrandTotalInBTC = 0;
    String current_transaction_description = "";
    double btcToSathosi = 0;
    double satoshiToMSathosi = 0;
    double getGrandTotalInMSatoshi = 0;
    double btcRatePerDollar = 0;
    ImageView qRCodeImage;
    Button confirpaymentbtn;
    int transactionID = 0;
    String currentTransactionLabel = "";
    double taxInBtcAmount = 0;
    double taxtInCurennccyAmount = 0;
    double taxBtcOnP3ToPopUp = 0;
    double taxUsdOnP3ToPopUp = 0;
    //TODO:For Printing Purpose
    private static final int REQUEST_ENABLE_BT = 2;
    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    int printstat;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private static OutputStream btoutputstream;
    ProgressDialog printingProgressBar;
    Dialog blutoothDevicesDialog;
    TextView setTextWithSpan;
    String labelGlobal = "sale123";
    private double AMOUNT_BTC = 0;
    private double AMOUNT_USD = 0;
    private double CONVERSION_RATE = 0;
    private double MSATOSHI = 0;


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
    MerchantData merchantData;

    public CheckOutsFragment3() {
        // Required empty public constructor
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
        try {
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_outs3, container, false);
        isFundingInfoGetSuccefully = false;
        setTextWithSpan = view.findViewById(R.id.footer);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);
        printingProgressBar = new ProgressDialog(getContext());
        printingProgressBar.setMessage("Printing...");
        confirmInvoicePamentProgressDialog = new ProgressDialog(getContext());
        confirmInvoicePamentProgressDialog.setMessage("Confirming Payment");
        updatingInventoryProgressDialog = new ProgressDialog(getContext());
        updatingInventoryProgressDialog.setMessage("Updating..");
        createInvoiceProgressDialog = new ProgressDialog(getContext());
        createInvoiceProgressDialog.setMessage("Creating Invoice");
        exitFromServerProgressDialog = new ProgressDialog(getContext());
        exitFromServerProgressDialog.setMessage("Exiting");
        getItemListprogressDialog = new ProgressDialog(getContext());
        getItemListprogressDialog.setMessage("Loading...");
        btcRate = view.findViewById(R.id.btcRateTextview);
        totalpay = view.findViewById(R.id.totalpay);
        taxpay = view.findViewById(R.id.taxpay);
        grandtotal = view.findViewById(R.id.grandtotal);
        fContext = getContext();
        clearout = view.findViewById(R.id.clearout);
        gdaxUrl=new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());
        sharedPreferences = new CustomSharedPreferences();
        String json = new CustomSharedPreferences().getvalueofMerchantData("data", getContext());
        Gson gson = new Gson();
        merchantData = gson.fromJson(json, MerchantData.class);

        if (GlobalState.getInstance().getTax() != null) {
            taxpayInBTC = GlobalState.getInstance().getTax().getTaxInBTC();
            taxpayInCurrency = GlobalState.getInstance().getTax().getTaxInUSD();
        }
        if (CheckNetwork.isInternetAvailable(fContext)) {
            //getcurrentrate();
            SubscrieChannel();
            getFundingNodeInfo();
        } else {
            setReceivableAndCapacity("0", "0", false);
            setcurrentrate("Not Found");
        }
        paywithclightbtn = view.findViewById(R.id.imageView5);
        checkoutPayItemslistview = view.findViewById(R.id.checkout2listview);
        paywithclightbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Items> dataSource = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
                int totalSaleIncurrency = 0;
                int totalSaleInSatoshiBTC = 0;
                if (dataSource != null && dataSource.size() > 0) {
                    priceInCurrency = 0;
                    priceInBTC = 0;
                    grandTotalInCurrency = 0;
                    getGrandTotalInBTC = 0;
                    for (int q = 0; q < dataSource.size(); q++) {
                        priceInCurrency = priceInCurrency + Double.parseDouble(dataSource.get(q).getPrice());
                        //format  ::   Total : 1.25 BTC /  $34.95 USD
                        if (GlobalState.getInstance().getChannel_btcResponseData() != null) {
                            // ab e value nai use krra es ki jaga pe adapter se value utha ra direct btc ki
                             //priceInBTC = 1 / GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
                            priceInBTC=1 /GlobalState.getInstance().getChannel_btcResponseData().getPrice();
                            priceInBTC = priceInBTC * priceInCurrency;
                            priceInBTC = round(priceInBTC, 9);
                            grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount;
                            getGrandTotalInBTC = priceInBTC + taxInBtcAmount;
                            getGrandTotalInBTC = round(getGrandTotalInBTC, 9);
                            //  totalpay.setText("Total:"+priceInBTC+"BTC"+"/"+"$"+priceInCurrency);
                        } else {
                            showToast("No BTC Rate");
                        }
                    }
                    dialogBoxForInvoice(grandTotalInCurrency, getGrandTotalInBTC);
                } else {
                    showToast("Cart is Empty");
                }
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
//                        getReceivable();
                       // Listfunds();
                        getListPeers();
                    }
                }
            }
        });
        setAdapter();
        btnFlashPay=view.findViewById(R.id.btnFlashPay);
        btnFlashPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInStoreClients();
            }
        });
        return view;
    }
    private  void getInvoiceForFlashPay(String clientName,String clientID,String storeName){
        getItemListprogressDialog.show();
        ArrayList<Items> dataSource = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
        if (dataSource != null && dataSource.size() > 0) {
            priceInCurrency = 0;
            priceInBTC = 0;
            grandTotalInCurrency = 0;
            getGrandTotalInBTC = 0;
            for (int q = 0; q < dataSource.size(); q++) {
                priceInCurrency = priceInCurrency + Double.parseDouble(dataSource.get(q).getPrice());
                if (GlobalState.getInstance().getChannel_btcResponseData() != null) {
                    priceInBTC=1 /GlobalState.getInstance().getChannel_btcResponseData().getPrice();
                    priceInBTC = priceInBTC * priceInCurrency;
                    priceInBTC = round(priceInBTC, 9);
                    grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount;
                    getGrandTotalInBTC = priceInBTC + taxInBtcAmount;
                    getGrandTotalInBTC = round(getGrandTotalInBTC, 9);
                } else {
                    getItemListprogressDialog.dismiss();
                    showToast("No BTC Rate");
                }
            }
            invoiceForFLashPay(grandTotalInCurrency, getGrandTotalInBTC,clientName,clientID,storeName);
        } else {
            getItemListprogressDialog.dismiss();
            showToast("Cart is Empty");
        }

    }
    private void invoiceForFLashPay(double totalSaleCurrency, double totalSaleBTC,String name,String clientID,String storeName) {
        double dTotalSaleInCurrency = totalSaleCurrency;
        double dTotalSaleBTC = totalSaleBTC;
        AMOUNT_USD = totalSaleCurrency;
        AMOUNT_BTC = totalSaleBTC;
        CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC;
        Long tsLong = System.currentTimeMillis() / 1000;
        String uNixtimeStamp = tsLong.toString();
        double dmSatoshi = 0;
        double dSatoshi = 0;
        dSatoshi = totalGrandfinal * AppConstants.btcToSathosi;
        MSATOSHI = dSatoshi;
        dmSatoshi = dSatoshi * AppConstants.satoshiToMSathosi;
        NumberFormat formatter = new DecimalFormat("#0");
        String rMSatoshi = formatter.format(dmSatoshi);
        labelGlobal = "sale" + uNixtimeStamp;
        String label="sale"+uNixtimeStamp;
        String descrption="FlashPay"+name;
        currentTransactionLabel = label;
        CreateInvoice(rMSatoshi, label, descrption);
        CreateInvoice createInvoice=GlobalState.getInstance().getCreateInvoice();
        String invoice=createInvoice.getBolt11();
        makeFlashPayment(clientID,storeName,invoice);

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
                            Log.d("NetworkStatus", "succefully network call");
                            CurrentSpecificRateData cSRDtemp = new CurrentSpecificRateData();
                            cSRDtemp.setRateinbitcoin(temp.getUSD().getLast());
//                       luqman comment     GlobalState.getInstance().setCurrentSpecificRateData(cSRDtemp);
                            GlobalState.getInstance().setCurrentAllRate(response.body());
                            sharedPreferences.setCurrentSpecificRateData(cSRDtemp, "CurrentSpecificRateData", fContext);
//                luqman comment            setcurrentrate(String.valueOf(cSRDtemp.getRateinbitcoin()));
//                            Log.d("CurrentRate", String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
//                            Log.d("CurrentRate2", String.valueOf(cSRDtemp.getRateinbitcoin()));
                            btcRatePerDollar = 1 / GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
                            Log.e("btcRatePerDollar", String.valueOf(btcRatePerDollar));
                            Tax tax = new Tax();
                            tax.setTaxInUSD(AppConstants.TAXVALUEINDOLLAR);
                            double taxBtc = 1 / response.body().getUSD().get15m();
                            taxBtc = taxBtc * AppConstants.TAXVALUEINDOLLAR;


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
                    Log.d("2error", t.getMessage());

                }
            });

        } else {
            showToast("NEtwork Not Avaible");
            CurrentRateInBTC = 1;
        }
    }

    public void onBackPressed() {
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

    public void refreshAdapter() {
        setAdapter();
    }

    public void setAdapter() {
        final ArrayList<Items> dataSource = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
        if (dataSource != null && dataSource.size() > 0) {
            int countitem = 0;
            for (Items items : dataSource) {
                countitem = countitem + items.getSelectQuatity();
            }
            ((CheckOutMain11) getActivity()).updateCartIcon(countitem);
            checkOutPayItemAdapter = new CheckOutPayItemAdapter(getContext(), dataSource, CheckOutsFragment3.this);
            checkoutPayItemslistview.setAdapter(checkOutPayItemAdapter);
            checkoutPayItemslistview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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
                                checkOutPayItemAdapter.notifyDataSetChanged();
                                setAdapter();

                            } else {
                                GlobalState.getInstance().removeInMDataScannedForPage1(tem);
                                GlobalState.getInstance().removeInmSeletedForPayDataSourceCheckOutInventory(tem);
                                GlobalState.getInstance().setmDataScanedSourceCheckOutInventory(GlobalState.getInstance().getmDataScannedForPage1());
//                                dataSource.remove(position);
                                checkOutPayItemAdapter.notifyDataSetChanged();
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
            ((CheckOutMain11) getActivity()).updateCartIcon(0);
        }

        if (dataSource != null && dataSource.size() > 0) {
            priceInCurrency = 0;
            priceInBTC = 0;

            for (int q = 0; q < dataSource.size(); q++) {
                double total;
                if (dataSource.get(q).getPrice() != null) {
                    total = dataSource.get(q).getSelectQuatity() * Double.parseDouble(dataSource.get(q).getPrice());

                } else {
                    total = dataSource.get(q).getSelectQuatity() * Double.parseDouble("0");

                }
                priceInCurrency = priceInCurrency + total;
                //format  ::   Total : 1.25 BTC /  $34.95 USD

               // if (GlobalState.getInstance().getCurrentAllRate() != null) {
                    if(GlobalState.getInstance().getChannel_btcResponseData()!=null){

                   // priceInBTC = 1 / GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
                        priceInBTC = 1 /GlobalState.getInstance().getChannel_btcResponseData().getPrice();
                    Log.e("btcbefore", String.valueOf(priceInBTC));
                    priceInBTC = priceInBTC * priceInCurrency;
                    Log.e("btcafter", String.valueOf(btcRatePerDollar));
                    priceInBTC = round(priceInBTC, 9);
                    priceInCurrency = round(priceInCurrency, 2);
                    DecimalFormat precision = new DecimalFormat("0.00");
                    totalpay.setText("Total:" + excatFigure(priceInBTC) + " BTC/ $" + precision.format(priceInCurrency));
                    double percent = Double.valueOf(GlobalState.getInstance().getTax().getTaxpercent()) / 100;

                    taxInBtcAmount = priceInBTC * percent;
                    taxBtcOnP3ToPopUp = taxInBtcAmount;
                    taxInBtcAmount = round(taxInBtcAmount, 9);
                    taxtInCurennccyAmount = priceInCurrency * percent;
                    taxUsdOnP3ToPopUp = taxtInCurennccyAmount;
                    taxtInCurennccyAmount = round(taxtInCurennccyAmount, 2);
                    taxpay.setText("Tax:" + excatFigure(taxInBtcAmount) + " BTC/ $" + precision.format(taxtInCurennccyAmount));
                    grandTotalInCurrency = priceInCurrency + taxtInCurennccyAmount;
                    getGrandTotalInBTC = priceInBTC + taxInBtcAmount;
                    getGrandTotalInBTC = round(getGrandTotalInBTC, 9);
                    grandTotalInCurrency = round(grandTotalInCurrency, 2);
                    grandtotal.setText(excatFigure(getGrandTotalInBTC) + " BTC/ $" + precision.format(grandTotalInCurrency));
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

    private void setAdapterAfterUpdateDb() {
        ArrayList<Items> dataSource = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();
        if (dataSource != null) {
            int countitem = 0;
            for (Items items : dataSource) {
                countitem = countitem + items.getSelectQuatity();
            }
            ((CheckOutMain11) getActivity()).updateCartIcon(countitem);

            checkOutPayItemAdapter = new CheckOutPayItemAdapter(getContext(), dataSource, CheckOutsFragment3.this);
            checkoutPayItemslistview.setAdapter(checkOutPayItemAdapter);
            GlobalState.getInstance().setCheckoutBtnPress(false);
        } else {
            ((CheckOutMain11) getActivity()).updateCartIcon(0);
        }
        totalpay.setText("Total:" + "0.0 BTC" + " / " + "0.0 $");
        taxpay.setText("Tax:0.0 BTC/0.0 $");
        grandtotal.setText("0.0 BTC/ 0.0 $");
    }

    private void dialogBoxForInvoice(double totalSaleCurrency, double totalSaleBTC) {
        double dTotalSaleInCurrency = totalSaleCurrency;
        double dTotalSaleBTC = totalSaleBTC;
        AMOUNT_USD = totalSaleCurrency;
        AMOUNT_BTC = totalSaleBTC;
        CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC;
        Long tsLong = System.currentTimeMillis() / 1000;
        String uNixtimeStamp = tsLong.toString();
        double dmSatoshi = 0;
        double dSatoshi = 0;
        dSatoshi = totalGrandfinal * AppConstants.btcToSathosi;
        MSATOSHI = dSatoshi;
        dmSatoshi = dSatoshi * AppConstants.satoshiToMSathosi;
        NumberFormat formatter = new DecimalFormat("#0");
        String rMSatoshi = formatter.format(dmSatoshi);
        //showToast(totalSaleCurrency+":"+totalSaleBTC);
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        invoiceDialog = new Dialog(getContext());
        invoiceDialog.setContentView(R.layout.dialoglayoutinvoice);
        Objects.requireNonNull(invoiceDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        invoiceDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        invoiceDialog.setCancelable(false);
        confirpaymentbtn = invoiceDialog.findViewById(R.id.confirpaymentbtn);
        final EditText et_msatoshi = invoiceDialog.findViewById(R.id.et_msatoshi);
        et_msatoshi.setInputType(InputType.TYPE_NULL);
        et_msatoshi.setText(String.valueOf(rMSatoshi));
        final EditText et_label = invoiceDialog.findViewById(R.id.et_lable);
        et_label.setInputType(InputType.TYPE_NULL);
        et_label.setText("sale" + uNixtimeStamp);
        labelGlobal = "sale" + uNixtimeStamp;
        final EditText et_description = invoiceDialog.findViewById(R.id.et_description);
        final ImageView ivBack = invoiceDialog.findViewById(R.id.iv_back);
        qRCodeImage = invoiceDialog.findViewById(R.id.imgQR);
        Button btnCreatInvoice = invoiceDialog.findViewById(R.id.btn_createinvoice);
        qRCodeImage.setVisibility(View.GONE);
        // progressBar = dialog.findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invoiceDialog.dismiss();
            }
        });

        btnCreatInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:Real USe After TestCase
                double totalInBtc = getGrandTotalInBTC;
                String msatoshi = et_msatoshi.getText().toString();
                String label = et_label.getText().toString();
                String descrption = et_description.getText().toString();
                boolean status = true;
                if (msatoshi.isEmpty()) {
                    showToast("MSATOSHI" + getString(R.string.empty));
                    status = false;
                    return;
                }
                if (label.isEmpty()) {
                    showToast("Label" + getString(R.string.empty));
                    status = false;
                    return;
                }
                if (descrption.isEmpty()) {
                    showToast("Description" + getString(R.string.empty));
                    status = false;
                    return;
                }
                //      progressBar.setVisibility(View.VISIBLE);
                if (status) {
                    //TODO:when call cmd invoice :      createInvoiceProgressDialog.show();
                    currentTransactionLabel = label;

                    CreateInvoice(msatoshi, label, descrption);
                }

            }
        });


        confirpaymentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListInvoices(currentTransactionLabel);
            }
        });
        invoiceDialog.show();
    }

    private void saveTransactionInLog(Invoice invoice) {
        DecimalFormat precision = new DecimalFormat("0.00");
        String transaction_label = ((transaction_label = invoice.getLabel()) != null) ? transaction_label : labelGlobal;
        String status = ((status = invoice.getStatus()) != null) ? status : "paid";
        String transaction_amountBTC = excatFigure(round(AMOUNT_BTC, 9));
        String transaction_amountUSD = precision.format(AMOUNT_USD);
        String conversion_rate = precision.format(CONVERSION_RATE);
        String msatoshi = String.valueOf(MSATOSHI);
        String payment_preimage = ((payment_preimage = invoice.getPayment_preimage()) != null) ? payment_preimage : "test123";
        String payment_hash = ((payment_hash = invoice.getPayment_hash()) != null) ? payment_hash : "test123";
        String destination = ((destination = invoice.getBolt11()) != null) ? destination : "123";
        String merchant_id = ((merchant_id = GlobalState.getInstance().getMerchant_id()) != null) ? merchant_id : "mg123";
        String transaction_description1 = current_transaction_description;
        add_alpha_transaction(transaction_label, status, transaction_amountBTC, transaction_amountUSD, payment_preimage, payment_hash, conversion_rate, msatoshi, destination, merchant_id, transaction_description1);
    }

    public void add_alpha_transaction(String transaction_label, String status, String transaction_amountBTC, String transaction_amountUSD, String payment_preimage, String payment_hash, String conversion_rate, String msatoshi, String destination, String merchant_id, String transaction_description) {
        Call<TransactionResp> call = ApiClient.getRetrofit().create(ApiPaths.class).add_alpha_transction(transaction_label, status, transaction_amountBTC, transaction_amountUSD, payment_preimage, payment_hash, conversion_rate, msatoshi, destination, merchant_id, transaction_description);
        // Call<TransactionResp> call = ApiClient.getRetrofit().create(ApiPaths.class).add_alpha_transction2(hashMap);
        // Call<TransactionResp> call = ApiClient.getRetrofit().create(ApiPaths.class).add_alpha_transction3(params);
        call.enqueue(new Callback<TransactionResp>() {
            @Override
            public void onResponse(Call<TransactionResp> call, Response<TransactionResp> response) {
                if (response != null) {
                    if (response.body() != null) {
                        TransactionResp transactionResp = response.body();
                        if (transactionResp.getMessage().equals("successfully done") && transactionResp.getTransactionInfo() != null) {
                            TransactionInfo transactionInfo = new TransactionInfo();
                            transactionInfo = transactionResp.getTransactionInfo();
                            showToast("Save..");
                        } else {
                            showToast("Not Save!!");
                        }
                        Log.e("Test", "Test");
                    } else {
                        showToast("Not Save!!");
                    }
                } else {
                    showToast("Not Save!!");
                }

                Log.e("AddTransactionLog", response.message());
            }

            @Override
            public void onFailure(Call<TransactionResp> call, Throwable t) {
                Log.e("AddTransactionLog", t.getMessage().toString());
                showToast("Server Side Issue!!");
            }
        });
    }

    private CreateInvoice parseJSONForCreatInvocie(String jsonString) {
        String response = jsonString;
        Gson gson = new Gson();
        Type type = new TypeToken<CreateInvoice>() {
        }.getType();
        CreateInvoice createInvoice = gson.fromJson(jsonString, type);
        GlobalState.getInstance().setCreateInvoice(createInvoice);
        return createInvoice;
    }

    private Invoice parseJSONForConfirmPayment(String jsonString) {
        String response = jsonString;
        Invoice invoice = null;
        boolean sta = false;
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(jsonString);
        } catch (Exception e) {
            Log.e("ConfrimInvoice1", e.getMessage());
        }
        JSONObject jsonObj = null;
        try {
            jsonObj = jsonArr.getJSONObject(0);
            sta = true;
        } catch (Exception e) {
            //e.printStackTrace();
            sta = false;
            Log.e("ConfrimInvoice2", e.getMessage());
        }
        ConfirmInvoiceResp confirmInvoiceResp = null;
        if (sta) {
            Gson gson = new Gson();
            boolean failed = false;
            try {
                confirmInvoiceResp = gson.fromJson(jsonObj.toString(), ConfirmInvoiceResp.class);
                failed = true;
            } catch (Exception e) {
                Log.e("ConfirmInvoiceResp3", e.getMessage());
                failed = false;
            }
            if (confirmInvoiceResp != null) {
                if (confirmInvoiceResp.getInvoiceArrayList() != null) {
                    if (confirmInvoiceResp.getInvoiceArrayList().size() > 0) {
                        invoice = confirmInvoiceResp.getInvoiceArrayList().get(0);
                    }
                }
            }
        } else {
//do nothing
        }
        GlobalState.getInstance().setInvoice(invoice);
        return invoice;
    }

    public Bitmap getBitMapFromHex(String hex) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(hex, BarcodeFormat.QR_CODE, 600, 600);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return bitmap;

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
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return bitmap;

    }

    public String excatFigure(double value) {
        BigDecimal d = new BigDecimal(String.valueOf(value));
        return d.toPlainString();
    }

    private void parseJSON(String jsonString) {
        String temre = jsonString;
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Items>>() {
        }.getType();
        ArrayList<Items> itemsList = gson.fromJson(jsonString, type);
        GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsList);
        for (Items items : itemsList) {
            Log.i("Items Details", items.getID() + "-" + items.getName() + "-" + items.getQuantity() + "-" + items.getPrice() + "-" + items.getAdditionalInfo());
            items.setSelectQuatity(1);
        }
        getItemListprogressDialog.dismiss();
        //  setAdapter();
    }

    public String getDateFromUTCTimestamp(long mTimestamp, String mDateFormate) {
        String date = null;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(""));
            cal.setTimeInMillis(mTimestamp * 1000L);
            date = DateFormat.format(mDateFormate, cal.getTimeInMillis()).toString();

            SimpleDateFormat formatter = new SimpleDateFormat(mDateFormate);
            formatter.setTimeZone(TimeZone.getTimeZone("CST"));
            Date value = formatter.parse(date);

            SimpleDateFormat dateFormatter = new SimpleDateFormat(mDateFormate);
            dateFormatter.setTimeZone(TimeZone.getDefault());
            date = dateFormatter.format(value);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public double mSatoshoToBtc(double msatoshhi) {
        double msatoshiToSatoshi = msatoshhi / AppConstants.satoshiToMSathosi;
        double satoshiToBtc = msatoshiToSatoshi / AppConstants.btcToSathosi;
        return satoshiToBtc;
    }

    //TODO: Prinnting Purpose
    private void initials() {
        ProgressBar tv_prgbar = blutoothDevicesDialog.findViewById(R.id.printerProgress);
        tv_prgbar.setVisibility(View.VISIBLE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.device_name);

        ListView t_blueDeviceListView = blutoothDevicesDialog.findViewById(R.id.blueDeviceListView);
        t_blueDeviceListView.setAdapter(mPairedDevicesArrayAdapter);
        t_blueDeviceListView.setOnItemClickListener(mDeviceClickListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter.getBondedDevices();

        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                mPairedDevicesArrayAdapter.add(mDevice.getName() + "\n" + mDevice.getAddress());
            }
        } else {
            String mNoDevices = "None Paired";
            mPairedDevicesArrayAdapter.add(mNoDevices);
        }
        tv_prgbar.setVisibility(View.GONE);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> mAdapterView, View mView, int mPosition, long mLong) {
            TextView tv_status = blutoothDevicesDialog.findViewById(R.id.tv_status);
            ProgressBar tv_prgbar = blutoothDevicesDialog.findViewById(R.id.printerProgress);
            try {
                tv_prgbar.setVisibility(View.VISIBLE);
                tv_status.setText("Device Status:Connecting....");
                mBluetoothAdapter.cancelDiscovery();
                String mDeviceInfo = ((TextView) mView).getText().toString();
                String mDeviceAddress = mDeviceInfo.substring(mDeviceInfo.length() - 17);
                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Code here will run in UI thread
                        TextView tv_status = blutoothDevicesDialog.findViewById(R.id.tv_status);
                        ProgressBar tv_prgbar = blutoothDevicesDialog.findViewById(R.id.printerProgress);

                        try {

                            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(applicationUUID);
                            mBluetoothAdapter.cancelDiscovery();
                            mBluetoothSocket.connect();
                            tv_status.setText("Device Status:Connected");
                            //controlLay(1);
                            tv_prgbar.setVisibility(View.GONE);
                            blutoothDevicesDialog.dismiss();
                        } catch (IOException eConnectException) {
                            tv_status.setText("Device Status:Try Again");
                            tv_prgbar.setVisibility(View.GONE);
                            Log.e("ConnectError", eConnectException.toString());
                            closeSocket(mBluetoothSocket);
                            //controlLay(0);
                        }

                    }
                });


            } catch (Exception ex) {
                Log.e("ConnectError", ex.toString());
            }
        }
    };

    private void dialogBoxForConnecctingBTPrinter() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        blutoothDevicesDialog = new Dialog(getContext());
        blutoothDevicesDialog.setContentView(R.layout.blutoothdevicelistdialoglayout);
        Objects.requireNonNull(blutoothDevicesDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        blutoothDevicesDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        blutoothDevicesDialog.setCancelable(false);
        //init dialog views
        final ImageView ivBack = blutoothDevicesDialog.findViewById(R.id.iv_back);
        final Button scanDevices = blutoothDevicesDialog.findViewById(R.id.btn_scanDevices);
        TextView tv_status = blutoothDevicesDialog.findViewById(R.id.tv_status);
        ListView blueDeviceListView = blutoothDevicesDialog.findViewById(R.id.blueDeviceListView);
        initials();
        scanDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                initials();

            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blutoothDevicesDialog.dismiss();
            }
        });

        blutoothDevicesDialog.show();

    }

    private void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d("", "SocketClosed");
        } catch (IOException ex) {
            Log.d("", "CouldNotCloseSocket");
        }
    }

    private void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v("", "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }

    void sendData() throws IOException {
        try {
            btoutputstream = mBluetoothSocket.getOutputStream();
            // the text typed by the user
            InvoiceForPrint recInvoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();
            if (recInvoiceForPrint != null) {
                final String paidAt = getDateFromUTCTimestamp(recInvoiceForPrint.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE);
                DecimalFormat precision = new DecimalFormat("0.00");
                final String amount = excatFigure(round((mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 9)) + "BTC/$" + precision.format(round(getUsdFromBtc(mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 2)) + "USD";
                final String amountInBtc = excatFigure(round((mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 9)) + "BTC";
                final String amountInUsd = precision.format(round(getUsdFromBtc(mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 2)) + "USD";
                final String items = recInvoiceForPrint.getPurchasedItems();
                final String tax = recInvoiceForPrint.getTax();
                final String taxInBtc = recInvoiceForPrint.getTaxInBtc();
                final String taxInUsd = recInvoiceForPrint.getTaxInUsd();
                final Bitmap bitmap = getBitMapFromHex(recInvoiceForPrint.getPayment_preimage());
                final String titile = "Invoice";
                String[] taxarray = tax.split("\n");
                final String newTax = "\t\t\t" + taxarray[0] + " / " + taxarray[1];
                final String finalPurchasedItem = getFinalPurchasedItem(items);
                //   final String finalMsg = "\n"+"Amount: "+amount+"\n"+"Paid at:"+paidAt+"\n"+"Items Purchased:"+items+"\n"+"Tax:"+tax+"\n";
                // final String finalMsg = String.valueOf(datatoWrite());
                //Log.e("PrintCommand",finalMsg);
                printingProgressBar.show();
                printingProgressBar.setCancelable(false);
                printingProgressBar.setCanceledOnTouchOutside(false);
                Thread t = new Thread() {
                    public void run() {
                        try {
                            // This is printer specific code you can comment ==== > Start
                            btoutputstream.write(PrinterCommands.reset);
                            btoutputstream.write(PrinterCommands.INIT);
                            btoutputstream.write(PrinterCommands.FEED_LINE);
                            btoutputstream.write("\n\n".getBytes());
                            //Items title should Center
                            btoutputstream.write("\tCheckout".getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\t--------".getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write(current_transaction_description.getBytes());
                            btoutputstream.write("\n\n".getBytes());
                            btoutputstream.write("\tItems:".getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write(finalPurchasedItem.getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\tTax:".getBytes());
                            btoutputstream.write("\n\t".getBytes());
                            btoutputstream.write(taxInBtc.getBytes());
                            btoutputstream.write("\n\t".getBytes());
                            btoutputstream.write(taxInUsd.getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\tTotal: ".getBytes());
                            btoutputstream.write("\n\t".getBytes());
                            btoutputstream.write(amountInBtc.getBytes());
                            btoutputstream.write("\n\t".getBytes());
                            btoutputstream.write(amountInUsd.getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\n".getBytes());
                            btoutputstream.write("\tPaid at:".getBytes());
                            btoutputstream.write("\n  ".getBytes());
                            btoutputstream.write("  ".getBytes());
                            btoutputstream.write(paidAt.getBytes());
                            btoutputstream.write("\n\n".getBytes());
                            btoutputstream.write("\tPayment Hash:".getBytes());
                            printNewLine();
                            if (bitmap != null) {
                                Bitmap bMapScaled = Bitmap.createScaledBitmap(bitmap, 250, 250, true);
                                new ByteArrayOutputStream();
                                PrintPic printPic = PrintPic.getInstance();
                                printPic.init(bMapScaled);
                                byte[] bitmapdata = printPic.printDraw();
                                btoutputstream.write(PrinterCommands.print);
                                btoutputstream.write(bitmapdata);
                                btoutputstream.write(PrinterCommands.print);
                                btoutputstream.write("\n\n".getBytes());
                            }
                            btoutputstream.write("\n\n".getBytes());

                            Thread.sleep(1000);
                            printingProgressBar.dismiss();
                            //confirmPaymentDialog.dismiss();

                        } catch (Exception e) {
                            Log.e("PrintError", "Exe ", e);

                        }

                    }
                };
                t.start();

            } else {
                String paidAt = "\n\n\n\n\n\n\nNot Data Found\n\n\n\n\n\n\n";
                btoutputstream.write(paidAt.getBytes());

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void printNewLine() {
        try {
            btoutputstream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int mRequestCode, int mResultCode, Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);
        switch (mRequestCode) {
            case REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    initials();
                } else {
                    Toast.makeText(getContext(), "Message", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1234:
                // HANDLE LockIng
                if (mRequestCode == 1234) {
                    if (mResultCode == RESULT_OK) {
                        //do something you want when pass the security
                        // Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                        //getReceivable();
                       // Listfunds();
                        getListPeers();
                    }
                }
                break;
        }
    }


    private String getFinalPurchasedItem(String items) {
        String[] itemsarray = items.split("\n");
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < itemsarray.length; i++) {
            stringBuffer.append("\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t" + itemsarray[i] + "\n");
        }
        stringBuffer.append("\n\n");
        return String.valueOf(stringBuffer);
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
    private void getInStoreClients() {
        String accessToken= new CustomSharedPreferences().getvalue("accessTokenLogin", getContext());
        String token="Bearer"+" "+accessToken;
        Call<ClientListModel> call = ApiClient.getRetrofit().create(ApiPaths.class).getInStoreClients(token);
        call.enqueue(new Callback<ClientListModel>() {
            @Override
            public void onResponse(Call<ClientListModel> call, Response<ClientListModel> response) {
                if (response.body() != null) {
                    ClientListModel clientListModel=response.body();
                    List<StoreClients> list=clientListModel.getStoreClientsList();
                    if (list.size()>0){
                        //ArrayList<StoreClients> list1=list;
                        showDialog(list);
                    }else {
                        showToast("No client found");
                    }
                }else {
                    showToast(response.message());
                }
            }

            @Override
            public void onFailure(Call<ClientListModel> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
                showToast(t.getMessage());

            }
        });
    }
    private void showDialog(List<StoreClients> list){

        final Dialog dialog = new Dialog(getContext());

        View view = getLayoutInflater().inflate(R.layout.dialog_select_client, null);

        ListView lv = (ListView) view.findViewById(R.id.selectClient);

        // Change MyActivity.this and myListOfItems to your own values
        selectClientListAdapter = new SelectClientList(getContext(), list);

        lv.setAdapter(selectClientListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StoreClients tem = list.get(position);

                getInvoiceForFlashPay(tem.getClient_name(),tem.getClient_id(),merchantData.getStore_name());


            }
        });

        dialog.setContentView(view);

        dialog.show();

    }
    private void makeFlashPayment(String clientID,String storeName,String invoice) {
        ///admin/invoice-to-client
        /*Authorization: Bearer {ACCESS_TOKEN_HERE}
        {"client_id": "C1640282683975726","invoice": "asdfjalksdjflaksjdf","store_name": "Some big store"}*/
        String accessToken= new CustomSharedPreferences().getvalue("accessTokenLogin", getContext());
        String token="Bearer"+" "+accessToken;
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("client_id", clientID);
            jsonObject1.addProperty("invoice", invoice);
            jsonObject1.addProperty("store_name", storeName);

            Call<WebSocketOTPresponse> call = (Call<WebSocketOTPresponse>) ApiClient2.getRetrofit().create(ApiPaths2.class).flashPay(token,jsonObject1);
            call.enqueue(new Callback<WebSocketOTPresponse>() {
                @Override
                public void onResponse(Call<WebSocketOTPresponse> call, Response<WebSocketOTPresponse> response) {
                    if (response.body() != null) {
                        WebSocketOTPresponse webSocketOTPresponse = response.body();
                        getItemListprogressDialog.dismiss();

                        if (webSocketOTPresponse != null) {

                            if (webSocketOTPresponse.getCode() == 700) {


                            } else if (webSocketOTPresponse.getCode() == 701) {

                                showToast("Missing 2FA code when requesting an access token");
                            } else if (webSocketOTPresponse.getCode() == 702) {

                                showToast("2FA code is incorrect / has timed out (30s window)");
                            } else if (webSocketOTPresponse.getCode() == 703) {
                                showToast("refresh token missing when requesting access code");
                            } else if (webSocketOTPresponse.getCode() == 704) {

                                showToast("refresh token missing when requesting access code");
                            } else if (webSocketOTPresponse.getCode() == 711) {
                                showToast("error -> attempting to initialize 2FA with the admin refresh code in a client system");

                            } else if (webSocketOTPresponse.getCode() == 716) {
                                showToast("Refresh token has expired (6 months), a new one is being mailed to the user");

                            } else if (webSocketOTPresponse.getCode() == 721) {
                                showToast("SendCommands is missing a \"commands\" field");

                            } else if (webSocketOTPresponse.getCode() == 722) {
                                showToast("SendCommands is missing a \"token\" with the access token");

                            } else if (webSocketOTPresponse.getCode() == 723) {

                                showToast("SendCommands received a refresh token instead of an access token");
                            } else if (webSocketOTPresponse.getCode() == 724) {
                                showToast("Access token has expired (at this point request 2FA code and get a new access token from /Refresh");

                            } else if (webSocketOTPresponse.getCode() == 725) {
                                showToast("Misc websocket error, \"message\" field will include more data");
                            }
                        }
                    }else {
                        getItemListprogressDialog.dismiss();
                    }
                }
                @Override
                public void onFailure(Call<WebSocketOTPresponse> call, Throwable t) {
                    Log.e("get-funding-nodes:", t.getMessage());
                    getItemListprogressDialog.dismiss();
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
//            String temp1 = jsonObj.toString();
            // Log.e("jsonObj",jsonObj.toString());
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
        JSONObject jsonObject=null;

        /*
        * {
   "peers": [
      {
         "id": "03ce4d3edecdcacba271a18f6287a1acbcbc123ea16dde6ffe77d0ed312b2be568",
         "connected": true,
         "netaddr": [
            "73.36.65.41:45524"
         ],
         "features": "08026aa2",
         "channels": [
            {
               "state": "CHANNELD_NORMAL",
               "scratch_txid": "be18ba10150abb91ca33590b1fe8f84da3bbbe2a1d1dcfc243e4cc7ea2280546",
               "last_tx_fee": "184000msat",
               "last_tx_fee_msat": "184000msat",
               "feerate": {
                  "perkw": 253,
                  "perkb": 1012
               },
               "owner": "channeld",
               "short_channel_id": "651050x1258x0",
               "direction": 0,
               "channel_id": "40aceabf410c0b3fe41455a450d97a2a5442c2e7cc1035dea7463a0b7a21e0bf",
               "funding_txid": "bfe0217a0b3a46a7de3510cce7c242542a7ad950a45514e43f0b0c41bfeaac40",
               "close_to_addr": "bc1q0z6eyeth3vursklgmtxk4pf5a9zcn93x3djycc",
               "close_to": "001478b59265778b38385be8dacd6a8534e945899626",
               "private": false,
               "opener": "local",
               "closer": null,
               "features": [
                  "option_static_remotekey"
               ],
               "funding_allocation_msat": {
                  "03ce4d3edecdcacba271a18f6287a1acbcbc123ea16dde6ffe77d0ed312b2be568": 0,
                  "029ff6fb4a0dbc6ee6e32a07e8f9b7318cd0298bf8ecd0a9f6a94ef70690470924": 224978000
               },
               "funding_msat": {
                  "03ce4d3edecdcacba271a18f6287a1acbcbc123ea16dde6ffe77d0ed312b2be568": "0msat",
                  "029ff6fb4a0dbc6ee6e32a07e8f9b7318cd0298bf8ecd0a9f6a94ef70690470924": "224978000msat"
               },
               "funding": {
                  "local_msat": "224978000msat",
                  "remote_msat": "0msat"
               },
               "msatoshi_to_us": 2702001,
               "to_us_msat": "2702001msat",
               "msatoshi_to_us_min": 2702001,
               "min_to_us_msat": "2702001msat",
               "msatoshi_to_us_max": 224978000,
               "max_to_us_msat": "224978000msat",
               "msatoshi_total": 224978000,
               "total_msat": "224978000msat",
               "fee_base_msat": "1000msat",
               "fee_proportional_millionths": 10,
               "dust_limit_satoshis": 546,
               "dust_limit_msat": "546000msat",
               "max_htlc_value_in_flight_msat": 18446744073709551615,
               "max_total_htlc_in_msat": "18446744073709551615msat",
               "their_channel_reserve_satoshis": 2249,
               "their_reserve_msat": "2249000msat",
               "our_channel_reserve_satoshis": 2249,
               "our_reserve_msat": "2249000msat",
               "spendable_msatoshi": 1,
               "spendable_msat": "1msat",
               "receivable_msatoshi": 220026999,
               "receivable_msat": "220026999msat",
               "htlc_minimum_msat": 0,
               "minimum_htlc_in_msat": "0msat",
               "their_to_self_delay": 144,
               "our_to_self_delay": 144,
               "max_accepted_htlcs": 30,
               "state_changes": [],
               "status": [
                  "CHANNELD_NORMAL:Reconnected, and reestablished.",
                  "CHANNELD_NORMAL:Funding transaction locked. Channel announced."
               ],
               "in_payments_offered": 33,
               "in_msatoshi_offered": 12445107,
               "in_offered_msat": "12445107msat",
               "in_payments_fulfilled": 33,
               "in_msatoshi_fulfilled": 12445107,
               "in_fulfilled_msat": "12445107msat",
               "out_payments_offered": 29,
               "out_msatoshi_offered": 234721106,
               "out_offered_msat": "234721106msat",
               "out_payments_fulfilled": 29,
               "out_msatoshi_fulfilled": 234721106,
               "out_fulfilled_msat": "234721106msat",
               "htlcs": []
            }
         ]
      }
   ]
}*/


        try {
            jsonObject=new JSONObject(jsonresponse);
            JSONArray ja_data=null;
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
                                if (listFunds.isConnected()) {
                                    String tempmsat = tempListFundChanel.getSpendable_msat();
                                    String tempmCap = tempListFundChanel.getMax_to_us_msat();
                                    tempmsat = removeLastChars(tempmsat, 4);
                                    tempmCap = removeLastChars(tempmCap, 4);
                                    double tmsat = 0;
                                    double tmcap = 0;
                                    try {
                                        tmsat = Double.parseDouble(tempmsat);
                                        tmcap = Double.parseDouble(tempmCap);
                                        BigDecimal value = new BigDecimal(tempmCap);
                                        double  doubleValue = value.doubleValue();
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
        btcRemainingCapacity = btcCapacity - btcReceivable;
        usdRemainingCapacity = usdCapacity - usdReceivable;
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
//        dialog.getWindow().setLayout(500, 500);
        clearOutDialog.setCancelable(false);
        TextView receivedVal = (TextView) clearOutDialog.findViewById(R.id.receivedVal);
        TextView capicityVal = (TextView) clearOutDialog.findViewById(R.id.capicityVal);
        boolean isCanClearout = false;
        Log.e("BeforeDialogCap", String.valueOf(usdRemainingCapacity));
        Log.e("BeforeDialogRecv", String.valueOf(usdReceivable));
        if (isFetchData) {
            if (isReceivableGet) {
                capicityVal.setText("$" + String.format("%.2f", round(usdRemainingCapacity, 2)));
                receivedVal.setText("$" + String.format("%.2f", round(usdReceivable, 2)));
                isCanClearout = true;
            } else {
                capicityVal.setText("N/A");
                receivedVal.setText("N/A");
                isCanClearout = false;
            }
        } else {
            capicityVal.setText("N/A");
            receivedVal.setText("N/A");
            isCanClearout = false;
        }
        final ImageView ivBack = clearOutDialog.findViewById(R.id.iv_back);
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
                String tempDestinationId = "02dc8590dd675b5bf89c6bdf9eeed767290b3d6056465e5b013756f65616d3d372";
                String clearOutQuery = "rpc-cmd,cli-node," + mlattitude + "_" + mlongitude + "," + getUnixTimeStamp() + ",[ keysend  " + routingNodeId + " " + mSatoshiReceivable + " " + label + " null 10" + " ]";
                //   String clearOutQuery="rpc-cmd,cli-node,"+mlattitude+"_"+mlongitude+","+getUnixTimeStamp()+",[ keysend  "+tempDestinationId+" "+5700000+" "+label+" null 10"+" ]";
                sendreceiveables(routingNodeId, String.valueOf(mSatoshiReceivable), label);
//                GetClearOutMsatoshi getClearOutMsatoshi = new GetClearOutMsatoshi(getActivity());
//                if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//                    getClearOutMsatoshi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String(clearOutQuery)});
//                } else {
//                    getClearOutMsatoshi.execute(new String[]{new String(clearOutQuery)});
//                }
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
//                Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();

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

    public void CreateInvoice(final String rMSatoshi, final String label, final String descrption) {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());

                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli invoice" + " " + rMSatoshi + " " + label + " " + descrption + "\"] }";

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
                        parseJSONForCreatInvocie(text);

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
                Log.e("TAG", "FAIL: " + response);
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

    public void Listfunds() {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                //String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNjMzNjc4NDIzLCJleHAiOjE2MzM3MjE2MjN9.zRgKB5Usnc5H03z_XWFx7m4_6MV984g0X1Vw8HwegS4";
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
                if (text.equals("{\"code\":724,\"message\":\"Access token has expired, please request a new token\"}\n")) {

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseJSONForListFunds(text);
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
                        parseJSONForListPeers(text);
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

    public void ListInvoices(final String lable) {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
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
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                //parseJSONForListFunds(text);
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
                //String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"keysend" + routingnode_id +" " + mSatoshiReceivable + " " + lable + " null 10" + "\" ] }";
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli keysend" +" "+routingnode_id +" " + mstoshiReceivable+"\"] }";

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
//                        parseJSONForRefunds(text);
                        String response = text;
                        Gson gson = new Gson();
                        Sendreceiveableresponse sendreceiveableresponse = gson.fromJson(response, Sendreceiveableresponse.class);
                        showToast(String.valueOf(sendreceiveableresponse.getMsatoshi()));
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
