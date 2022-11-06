package com.sis.clightapp.fragments.merchant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.opencsv.CSVWriter;
import com.sis.clightapp.EmailSdk.GMailSender;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Network.CheckNetwork;

import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.Functions2;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.Print.PrintPic;
import com.sis.clightapp.Utills.Print.PrinterCommands;

import com.sis.clightapp.Utills.UrlConstants;
import com.sis.clightapp.activity.MainActivity;
import com.sis.clightapp.adapter.MerchantRefundsListAdapter;
import com.sis.clightapp.adapter.MerchantSalesListAdapter;

import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.CreateInvoice;
import com.sis.clightapp.model.GsonModel.DecodePayBolt11;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.model.GsonModel.InvoiceForPrint;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantLoginResp;
import com.sis.clightapp.model.GsonModel.Pay;
import com.sis.clightapp.model.GsonModel.Refund;
import com.sis.clightapp.model.GsonModel.Sale;

import com.sis.clightapp.model.Invoices.InvoicesResponse;
import com.sis.clightapp.model.REST.TransactionInfo;
import com.sis.clightapp.model.REST.TransactionResp;
import com.sis.clightapp.model.RefundsData.RefundResponse;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.UserInfo;
import com.sis.clightapp.model.currency.CurrentAllRate;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

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

public class MerchantFragment1 extends MerchantBaseFragment {
    private MerchantFragment1 merchantFragment1;
    SharedPreferences preferences;
    SharedPreferences.Editor edit;
    int INTENT_AUTHENTICATE = 1234;
    int setwidht, setheight;
    //private final String gdaxUrl = "ws://98.226.215.246:8095/SendCommands";
    private final String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    ProgressDialog confirmingProgressDialog, simpleloader;
    Button getpaidbutton, refundbutton;
    private WebSocketClient webSocketClient;
    CustomSharedPreferences sharedPreferences = new CustomSharedPreferences();
    ImageView salestextview, refundtextview;
    ListView saleslistview, refundslistview;
    MerchantSalesListAdapter merchantSalesListAdapter;
    MerchantRefundsListAdapter merchantRefundsListAdapter;
    String TAG = "CLighting App";
    double CurrentRateInBTC;
    ApiPaths fApiPaths;
    Functions2 functions;
    ProgressDialog createInvoiceProgressDialog, exitFromServerProgressDialog, getSalesListProgressDialog, getRefundsListProgressDialog, confirmInvoicePamentProgressDialog, payOtherProgressDialog, decodePayBolt11ProgressDialog;
    Dialog invoiceDialog, confirmPaymentDialog, commandeerRefundDialog, commandeerRefundDialogstep2, distributeGetPaidDialog;
    Button confirpaymentbtn;
    ImageView qRCodeImage;
    String currentTransactionLabel = "";
    String bolt11fromqr = "";
    private IntentIntegrator qrScan;
    String getPaidDescrition = "";
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
    public boolean isInAppMerchant1 = true;
    //Date Filter
    EditText fromDateSale, toDateSale, fromDateRefund, toDateRefund;
    DatePickerDialog picker;
    String fromDateVaue = "";
    String toDateValue = "";
    //Email Sale
    GMailSender gMailSender;
    static String senderEmail = null;
    public static final int REQUEST_WRITE_PERMISSION = 786;
    static File folderpath = null;
    ArrayList<Sale> mSaleDataSource;
    ArrayList<Refund> mRefundDataSource;
    TextView setTextWithSpan;
    String getPaidLABEL = "";
    String getRefubdLABEL = "";
    double AMOUNT_BTC = 0;
    double AMOUNT_USD = 0;
    double CONVERSION_RATE = 0;
    double MSATOSHI = 0;
    String current_transaction_description = "";

    String current_Refund_Bolt11 = "";
    String current_Refund_label = "";

    public MerchantFragment1() {
        // Required empty public constructor
    }

    public MerchantFragment1 getInstance() {
        if (merchantFragment1 == null) {
            merchantFragment1 = new MerchantFragment1();
        }
        return merchantFragment1;
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

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {

            try {
                getCSV();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isInAppMerchant1) {
            getSalesListFromMerchantServer();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant1, container, false);
        setTextWithSpan = view.findViewById(R.id.poweredbyimage);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan, getString(R.string.welcome_text), getString(R.string.welcome_text_bold), boldStyle);
        cLightsharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        fromDateSale = view.findViewById(R.id.et_from_date_sale);
        toDateSale = view.findViewById(R.id.et_to_date_sale);
        fromDateRefund = view.findViewById(R.id.et_from_date_refund);
        toDateRefund = view.findViewById(R.id.et_to_date_refund);

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        edit = preferences.edit();
        qrScan = new IntentIntegrator(getActivity());
        qrScan.setOrientationLocked(false);
        String prompt = getResources().getString(R.string.scanqrforbolt11);
        qrScan.setPrompt(prompt);
        printingProgressBar = new ProgressDialog(getContext());
        printingProgressBar.setMessage("Printing...");
        decodePayBolt11ProgressDialog = new ProgressDialog(getContext());
        decodePayBolt11ProgressDialog.setMessage("Loading...");
        payOtherProgressDialog = new ProgressDialog(getContext());
        payOtherProgressDialog.setMessage("Paying...");
        exitFromServerProgressDialog = new ProgressDialog(getContext());
        exitFromServerProgressDialog.setMessage("Exiting");
        getSalesListProgressDialog = new ProgressDialog(getContext());
        getSalesListProgressDialog.setMessage("Loading Sales");
        getRefundsListProgressDialog = new ProgressDialog(getContext());
        getRefundsListProgressDialog.setMessage("Loading Refunds");
        confirmInvoicePamentProgressDialog = new ProgressDialog(getContext());
        confirmInvoicePamentProgressDialog.setMessage("Confirming Payment");
        createInvoiceProgressDialog = new ProgressDialog(getContext());
        createInvoiceProgressDialog.setMessage("Creating Invoice");
        confirmingProgressDialog = new ProgressDialog(getContext());
        confirmingProgressDialog.setCancelable(false);
        confirmingProgressDialog.setMessage("Loading ...");
        simpleloader = new ProgressDialog(getContext());
        simpleloader.setCancelable(false);
        simpleloader.setMessage("Loading ...");
        sharedPreferences = new CustomSharedPreferences();
// new CustomSharedPreferences().setvalueofaccestoken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNjM0MTkyMzk5LCJleHAiOjE2MzQyMzU1OTl9.va8ixffbBQ14gMvjZDOTiDW-b0G2C4hSfGWW1gnmxV0", "accessToken", getContext());
//  new CustomSharedPreferences().setvalueofipaddress("98.226.215.246:8095", "ip", getContext());
//        CreateInvoice();
        getInvoicelist();
        getRefundslist();

//        sendpayslist();
//        getCoinPrice();
//        CreateInvoice("2282927 ", "sale1633085815", "pp");

        findMerchant(new CustomSharedPreferences().getvalueofMerchantname("merchant_name", getContext()), new CustomSharedPreferences().getvalueofMerchantpassword("merchant_pass", getContext()));

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht = width * 48;
        setwidht = setwidht / 100;
        setheight = height / 2;
        getpaidbutton = view.findViewById(R.id.getpaidbutton);
        getpaidbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:what ever on getPAid
                dialogBoxForGetPaidDistribute();

            }
        });
        refundbutton = view.findViewById(R.id.refundbutton);
        refundbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO:what ever on Refunds
                isInAppMerchant1 = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);

                    if (km.isKeyguardSecure()) {
                        Intent authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "");
                        startActivityForResult(authIntent, INTENT_AUTHENTICATE);
                    } else {
                        dialogBoxForRefundCommandeer();
                    }
                }
            }
        });
        saleslistview = view.findViewById(R.id.salesListview);
        refundslistview = view.findViewById(R.id.refendListview);
        saleslistview.setMinimumWidth(setwidht);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) saleslistview.getLayoutParams();
        lp.width = setwidht;
        saleslistview.setLayoutParams(lp);
        ViewGroup.LayoutParams lp2 = (ViewGroup.LayoutParams) refundslistview.getLayoutParams();
        lp2.width = setwidht;
        refundslistview.setLayoutParams(lp2);
        if (CheckNetwork.isInternetAvailable(getContext())) {
            //getcurrentrate();
            //getHeartBeat();
            SubscrieChannel();
        } else {
            setcurrentrate("Not Found");
        }
       /*final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                //call function
                if (CheckNetwork.isInternetAvailable(getContext())) {
                    getcurrentrate();
                    // getHeartBeat();
                } else {
                    setcurrentrate("Not Found");
                }
                ha.postDelayed(this, 180000);
            }
        }, 180000);*/
        // ((MerchnatMain11)getActivity()).updateCartIcon(0);
        fromDateSale.setInputType(InputType.TYPE_NULL);
        toDateSale.setInputType(InputType.TYPE_NULL);
        fromDateRefund.setInputType(InputType.TYPE_NULL);
        toDateRefund.setInputType(InputType.TYPE_NULL);
        fromDateSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth);
                                fromDateSale.setText(date);
                                fromDateRefund.setText("");
                                toDateRefund.setText("");
                                toDateSale.setText("");
                                setAdapterFromDateSale(date);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year

                picker.show();

            }
        });

        toDateSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog


                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth);
                                toDateSale.setText(date);
                                fromDateSale.setText("");
                                fromDateRefund.setText("");
                                toDateRefund.setText("");
                                setAdapterToDateSale(date);

                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
                picker.show();

            }
        });

        fromDateRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth);
                                fromDateRefund.setText(date);
                                toDateRefund.setText("");
                                fromDateSale.setText("");
                                toDateSale.setText("");
                                setAdapterFromDateRefund(date);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
                picker.show();

            }
        });

        toDateRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog


                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                String date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth);
                                toDateRefund.setText(date);
                                fromDateRefund.setText("");
                                fromDateSale.setText("");
                                toDateSale.setText("");
                                setAdapterToDateRefund(date);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
                picker.show();

            }
        });
        return view;
    }

    private void getcurrentrate() {
        functions = new Functions2();
        fApiPaths = functions.retrofitBuilder();

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
//                      luqman comment      GlobalState.getInstance().setCurrentSpecificRateData(cSRDtemp);
                            GlobalState.getInstance().setCurrentAllRate(response.body());
                            // sharedPreferences.setCurrentSpecificRateData(cSRDtemp,"CurrentSpecificRateData",getContext());
//                         luqman comment   setcurrentrate(String.valueOf(cSRDtemp.getRateinbitcoin()));


                            // Log.d("CurrentRate",String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
//                            Log.d("CurrentRate2",String.valueOf(cSRDtemp.getRateinbitcoin()));

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

    private void setcurrentrate(String rate) {
        //TODO:what need

    }

    private void setAdapterToDateRefund(String datex) {
        String dateval = datex;
        if (GlobalState.getInstance().getmMerchantRefundsLIstDataSource() != null) {
            ArrayList<Refund> merchantRefundList = GlobalState.getInstance().getmMerchantRefundsLIstDataSource();
            ArrayList<Refund> fromDateRefundList = new ArrayList<>();
            for (Refund refund : merchantRefundList) {
                if (refund.getStatus().equals("complete")) {
                    String[] sourceSplit = dateval.split("-");
                    int month = Integer.parseInt(sourceSplit[0]);
                    int day = Integer.parseInt(sourceSplit[1]);
                    int year = Integer.parseInt(sourceSplit[2]);
                    //  GregorianCalendar calendar = new GregorianCalendar();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                    cal.set(year, month - 1, day);
                    Date date = cal.getTime();
                    long curentTime = date.getTime();
                    long paidTime = refund.getCreated_at() * 1000;
                    Date date2 = new Date(paidTime);
                    if (date2.before(date))
//                    long dayDiff=getDayDiffDates(paidTime,curentTime);
//                    if(dayDiff<1)
                    {
                        fromDateRefundList.add(refund);
                    }

                } else {

                }
            }
            merchantRefundsListAdapter = new MerchantRefundsListAdapter(getContext(), fromDateRefundList);
            refundslistview.setAdapter(merchantRefundsListAdapter);
        }

    }

    private void setAdapterFromDateRefund(String datex) {
        String dateval = datex;
        if (GlobalState.getInstance().getmMerchantRefundsLIstDataSource() != null) {
            ArrayList<Refund> merchantRefundList = GlobalState.getInstance().getmMerchantRefundsLIstDataSource();
            ArrayList<Refund> fromDateRefundList = new ArrayList<>();
            for (Refund refund : merchantRefundList) {
                if (refund.getStatus().equals("complete")) {
                    String[] sourceSplit = dateval.split("-");
                    int month = Integer.parseInt(sourceSplit[0]);
                    int day = Integer.parseInt(sourceSplit[1]);
                    int year = Integer.parseInt(sourceSplit[2]);
                    //  GregorianCalendar calendar = new GregorianCalendar();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                    cal.set(year, month - 1, day);
                    Date date = cal.getTime();
                    long curentTime = date.getTime();
                    long paidTime = refund.getCreated_at() * 1000;
                    Date date2 = new Date(paidTime);
//                    if(date.after(date2))
                    int d1 = date.getDay();
                    int d2 = date2.getDay();
                    if (date2.after(date) || date.getDay() == date2.getDay())
//                    long dayDiff=getDayDiffDates(curentTime,paidTime);
//                    if(dayDiff<1)
                    {
                        fromDateRefundList.add(refund);
                    }

                } else {

                }
            }
            merchantRefundsListAdapter = new MerchantRefundsListAdapter(getContext(), fromDateRefundList);
            refundslistview.setAdapter(merchantRefundsListAdapter);
        }

    }

    private void setAdapterToDateSale(String toString) {
        String dateval = toString;
        if (GlobalState.getInstance().getmMerchantSalesListDataSource() != null) {
            ArrayList<Sale> merchantSaleList = GlobalState.getInstance().getmMerchantSalesListDataSource();
            ArrayList<Sale> fromDateSaleList = new ArrayList<>();
            for (Sale sale : merchantSaleList) {
                if (sale.getPayment_preimage() != null) {
                    String[] sourceSplit = dateval.split("-");
                    int month = Integer.parseInt(sourceSplit[0]);
                    int day = Integer.parseInt(sourceSplit[1]);
                    int year = Integer.parseInt(sourceSplit[2]);
                    //GregorianCalendar calendar = new GregorianCalendar();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                    cal.set(year, month - 1, day);
                    Date date = cal.getTime();
                    long curentTime = date.getTime();
                    long paidTime = sale.getPaid_at() * 1000;
                    int d1 = date.getDay();
                    int d2 = date.getDay();
                    Date date2 = new Date(paidTime);
                    if (date2.before(date))
//                    long dayDiff=getDayDiffDates(paidTime,curentTime);
//                    if(dayDiff<1)
                    {
                        fromDateSaleList.add(sale);
                    }

                } else {

                }
            }
            merchantSalesListAdapter = new MerchantSalesListAdapter(getContext(), fromDateSaleList);
            saleslistview.setAdapter(merchantSalesListAdapter);
        }
    }

    private void setAdapterFromDateSale(String fromDateSale) {
        String dateval = fromDateSale;
        if (GlobalState.getInstance().getmMerchantSalesListDataSource() != null) {
            ArrayList<Sale> merchantSaleList = GlobalState.getInstance().getmMerchantSalesListDataSource();
            ArrayList<Sale> fromDateSaleList = new ArrayList<>();
            for (Sale sale : merchantSaleList) {
                if (sale.getPayment_preimage() != null) {
                    String[] sourceSplit = dateval.split("-");
                    int month = Integer.parseInt(sourceSplit[0]);
                    int day = Integer.parseInt(sourceSplit[1]);
                    int year = Integer.parseInt(sourceSplit[2]);
                    //  GregorianCalendar calendar = new GregorianCalendar();
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                    cal.set(year, month - 1, day);
                    //calendar.set(year,month-1,day);
                    Date date = cal.getTime();
                    long curentTime = date.getTime();
                    long paidTime = sale.getPaid_at() * 1000;
                    Date date2 = new Date(paidTime);
                    if (date2.after(date) || date.getDay() == date2.getDay())
                    // long dayDiff=getDayDiffDates(curentTime,paidTime);
//                    if(dayDiff<1)
                    {
                        fromDateSaleList.add(sale);
                    }

                } else {

                }
            }
            merchantSalesListAdapter = new MerchantSalesListAdapter(getContext(), fromDateSaleList);
            saleslistview.setAdapter(merchantSalesListAdapter);
        }

    }

    //TODO:Start Get Sales and Refund List
    //TODO:Get Refund List
    private void getRefundsListFromMerchantServer() {
        //TODO:Get Refund List
        sendpayslist();
//        GetRefundsListFromMerchantServer getRefundsListFromMerchantServer = new GetRefundsListFromMerchantServer(getActivity());
//        if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//            getRefundsListFromMerchantServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String("listsendpays")});
//            getRefundsListProgressDialog.show();
//            getRefundsListProgressDialog.setCancelable(false);
//            getRefundsListProgressDialog.setCanceledOnTouchOutside(false);
//        } else {
//            getRefundsListFromMerchantServer.execute(new String[]{new String("listsendpays")});
//            getRefundsListProgressDialog.show();
//            getRefundsListProgressDialog.setCancelable(false);
//            getRefundsListProgressDialog.setCanceledOnTouchOutside(false);
//        }

    }

    //TODO:Get Sales List
    private void getSalesListFromMerchantServer() {
        //TODO:Get Sales List
        getInvoicelist();
//        GetSalesListFromMerchantServer getSalesListFromMerchantServer = new GetSalesListFromMerchantServer(getActivity());
//        if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//            getSalesListFromMerchantServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String("listinvoices")});
//
//        } else {
//            getSalesListFromMerchantServer.execute(new String[]{new String("listinvoices")});
//
//        }

    }

    private void parseJSONForSales(String jsonString) {
        String temre = jsonString;
        Gson gson = new Gson();
//        Type type = new TypeToken<ArrayList<Sale>>() {
//        }.getType();
//        ArrayList<Sale> saleArrayList = new ArrayList<>();
        ArrayList<Sale> saleArrayList = new ArrayList<>();
        InvoicesResponse invoicesResponse;
        try {
            invoicesResponse = gson.fromJson(jsonString, InvoicesResponse.class);
            saleArrayList = invoicesResponse.getInvoiceArrayList();

        } catch (Exception e) {
            simpleloader.dismiss();
            Log.e("GsonnParsingError_Sale", e.getMessage());
        }
//        Collections.reverse(saleArrayList);
        GlobalState.getInstance().setmMerchantSalesListDataSource(saleArrayList);
//        for (Sale sales : saleArrayList){
//           // Log.i("Sales Details", sales.getLabel()+"-"+sales.getBolt11() + "-"+sales.getStatus() + "-" + sales.getAmount_msat() + "-" + sales.getMsatoshi()+".......");
//        }


        // requestPermission();   for sending email and excel file
        setSalesAdapter();


    }

    private void parseJSONForRefunds(String jsonString) {
        String temre = jsonString;
        Gson gson = new Gson();
//        Type type = new TypeToken<ArrayList<Refund>>() {
//        }.getType();
        ArrayList<Refund> refundArrayList = new ArrayList<>();

        RefundResponse refundResponse;
        try {
            refundResponse = gson.fromJson(jsonString, RefundResponse.class);
            refundArrayList = refundResponse.getRefundArrayList();
        } catch (Exception e) {
            Log.e("GsonnParsingError_Rfd", e.getMessage());
        }


//        Collections.reverse(refundArrayList);
        GlobalState.getInstance().setmMerchantRefundsLIstDataSource(refundArrayList);
//        for (Refund refund : refundArrayList){
//           // Log.i("Refund Details", refund.getBolt11() + "-"+sales.getStatus() + "-" + sales.getPreimage() + "-" + sales.getAmount_sent_msat());
//        }

        setRefundsAdapter();
    }

    private void setSalesAdapter() {

        if (GlobalState.getInstance().getmMerchantSalesListDataSource() != null) {
            ArrayList<Sale> merchantSaleList = GlobalState.getInstance().getmMerchantSalesListDataSource();
            ArrayList<Sale> todaySaleList = new ArrayList<>();
            ArrayList<Sale> totalPaidSaleList = new ArrayList<>();
            ArrayList<Sale> totalUnPaidSaleList = new ArrayList<>();
            ArrayList<Sale> totalSaleList = new ArrayList<>();
            for (Sale sale : merchantSaleList) {
                //   luqman comment     if (sale.getPayment_preimage() != null) {
                if (sale.getLabel() != null) {
                    totalPaidSaleList.add(sale);
                    long curentTime = new Date().getTime();
//               String refundtyme=getDateFromUTCTimestamp(sale.getPaid_at(),AppConstants.OUTPUT_DATE_FORMATE);
//               String currnttyme=getDateFromUTCTimestamp(curentTime/1000,AppConstants.OUTPUT_DATE_FORMATE);
                    long paidTime = sale.getPaid_at() * 1000;
                    Date currentDate = new Date(curentTime);
                    Date paidDate = new Date(paidTime);
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(currentDate);
                    cal2.setTime(paidDate);
                    boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                    if (sameDay) {
                        todaySaleList.add(sale);
                    } else {
                        //TODO:If greater

                    }

                } else {
                    totalUnPaidSaleList.add(sale);
                }
            }

            totalSaleList = merchantSaleList;
            mSaleDataSource = totalPaidSaleList;
            GlobalState.getInstance().setmTodaySaleList(todaySaleList);
            GlobalState.getInstance().setmTotalPaidSaleList(totalPaidSaleList);
            GlobalState.getInstance().setmTotalSaleList(totalSaleList);
            GlobalState.getInstance().setmTotalUnPaidSaleList(totalUnPaidSaleList);
            merchantSalesListAdapter = new MerchantSalesListAdapter(getContext(), todaySaleList);
            saleslistview.setAdapter(merchantSalesListAdapter);
        }
    }

    private void setRefundsAdapter() {
        if (GlobalState.getInstance().getmMerchantRefundsLIstDataSource() != null) {
            ArrayList<Refund> merchantRefundList = GlobalState.getInstance().getmMerchantRefundsLIstDataSource();
            ArrayList<Refund> mTodayRefundList = new ArrayList<>();
            ArrayList<Refund> mTotalCompleteRefundList = new ArrayList<>();
            ArrayList<Refund> mTotalUnCompleteList = new ArrayList<>();
            ArrayList<Refund> mTotalRefundList = new ArrayList<>();
            for (Refund refund : merchantRefundList) {

                if (refund.getStatus() != null) {

                    if (refund.getStatus().equals("complete")) {
                        mTotalCompleteRefundList.add(refund);
                        long currentTime = new Date().getTime();
                        long refundtime = refund.getCreated_at() * 1000;
                        long dayDiff = getDayDiffDates(refundtime, currentTime);
                        Date currentDate = new Date(currentTime);
                        Date refundDate = new Date(refundtime);
                        int d1 = currentDate.getDay();
                        int d2 = refundDate.getDay();
                        Calendar cal1 = Calendar.getInstance();
                        Calendar cal2 = Calendar.getInstance();
                        cal1.setTime(currentDate);
                        cal2.setTime(refundDate);
                        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                        if (sameDay)
//                   if(dayDiff<1)
                        {
                            // under +/- 24hour , do the work
                            mTodayRefundList.add(refund);
                        } else {
                            // over 24 hour

                        }

                    } else {

                        mTotalUnCompleteList.add(refund);

                    }
                } else {
                    mTotalUnCompleteList.add(refund);
                }
            }
            mRefundDataSource = mTotalCompleteRefundList;
            mTotalRefundList = merchantRefundList;
            GlobalState.getInstance().setmTodayRefundList(mTodayRefundList);
            GlobalState.getInstance().setmTotalCompleteRefundList(mTotalCompleteRefundList);
            GlobalState.getInstance().setmTotalUnCompleteRefundList(mTotalUnCompleteList);
            GlobalState.getInstance().setmTotalRefundList(mTotalRefundList);
            merchantRefundsListAdapter = new MerchantRefundsListAdapter(getContext(), mTodayRefundList);
            refundslistview.setAdapter(merchantRefundsListAdapter);
        }
    }
    //TODO: End OF Sales and Refund List Fetvh


    //TOOD:GetPaid Or Distribute
    private void dialogBoxForGetPaidDistribute() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        distributeGetPaidDialog = new Dialog(getContext());
        distributeGetPaidDialog.setContentView(R.layout.dialoglayoutgetpaiddistribute);
        Objects.requireNonNull(distributeGetPaidDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        distributeGetPaidDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        distributeGetPaidDialog.setCancelable(false);
        TextView titile = (TextView) distributeGetPaidDialog.findViewById(R.id.tv_title);
        titile.setText("Get Paid");
        confirpaymentbtn = distributeGetPaidDialog.findViewById(R.id.confirpaymentbtn);
        final EditText et_msatoshi = distributeGetPaidDialog.findViewById(R.id.et_msatoshi);
        final EditText et_label = distributeGetPaidDialog.findViewById(R.id.et_lable);
        et_label.setInputType(InputType.TYPE_NULL);
        et_label.setText("sale" + getUnixTimeStamp());
        getPaidLABEL = (et_label.getText().toString());
        final EditText et_description = distributeGetPaidDialog.findViewById(R.id.et_description);
        final ImageView ivBack = distributeGetPaidDialog.findViewById(R.id.iv_back_invoice);
        qRCodeImage = distributeGetPaidDialog.findViewById(R.id.imgQR);
        Button btnCreatInvoice = distributeGetPaidDialog.findViewById(R.id.btn_createinvoice);
        qRCodeImage.setVisibility(View.GONE);
        // progressBar = dialog.findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distributeGetPaidDialog.dismiss();
            }
        });

        btnCreatInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msatoshi = et_msatoshi.getText().toString();
                String label = et_label.getText().toString();
                String descrption = et_description.getText().toString();
                boolean status = true;
                if (msatoshi.isEmpty()) {
                    showToast("Amount" + getString(R.string.empty));
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
                    AMOUNT_USD = Double.parseDouble(msatoshi);
                    double priceInBTC = 1 / GlobalState.getInstance().getChannel_btcResponseData().getPrice();

                    // double priceInBTC = 1 / GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
                    priceInBTC = priceInBTC * Double.parseDouble(msatoshi);
                    AMOUNT_BTC = priceInBTC;
                    double amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi;
                    MSATOSHI = amountInMsatoshi;
                    amountInMsatoshi = amountInMsatoshi * AppConstants.satoshiToMSathosi;
                    CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC;
                    //msatoshi=excatFigure(amountInMsatoshi);
                    NumberFormat formatter = new DecimalFormat("#0");
                    String rMSatoshi = formatter.format(amountInMsatoshi);
                    getPaidDescrition = descrption;
//                    creatInvoice(rMSatoshi, label, descrption);
                    CreateInvoice(rMSatoshi, label, descrption);
                }

            }
        });


        confirpaymentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Confirmpayment(currentTransactionLabel);
//                ConfirmInvoicePaymentFromServer confirmInvoicePaymentFromServer = new ConfirmInvoicePaymentFromServer(getActivity());
//                confirmInvoicePaymentFromServer.execute(new String[]{new String(currentTransactionLabel)});
//                confirmInvoicePamentProgressDialog.show();


            }
        });
        distributeGetPaidDialog.show();
    }

    private CreateInvoice parseJSONForCreatInvocie(String jsonString) {
        String response = jsonString;
        Gson gson = new Gson();
//        Type type = new TypeToken<CreateInvoice>() {
//        }.getType();
        CreateInvoice createInvoice = gson.fromJson(jsonString, CreateInvoice.class);
        GlobalState.getInstance().setCreateInvoice(createInvoice);
        showToast(createInvoice.getBolt11());

        CreateInvoice temInvoice = createInvoice;
        if (temInvoice != null) {
            if (temInvoice.getBolt11() != null) {

                String temHax = temInvoice.getBolt11();
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(temHax, BarcodeFormat.QR_CODE, 600, 600);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    qRCodeImage.setImageBitmap(bitmap);
                    qRCodeImage.setVisibility(View.VISIBLE);
                    confirpaymentbtn.setVisibility(View.VISIBLE);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }

        }
        return createInvoice;
    }

    private Invoice parseJSONForConfirmPayment(String jsonString) {
        String response = jsonString;
//        response="{\"label\":\"abc\",\"bolt11\":\"asasasadjadjkhadkajsdhjkahsdkjhasdjkh\",\"payment_hash\":\"aasasasas\",\"msatoshi\":102100201,\"amount_msat\":\"102100201msat\",\"status\":\"paid\",\"pay_index\":2,\"msatoshi_received\":1021002012,\"amount_received_msat\":\"1021002012msat\",\"paid_at\":120120102012,\"payment_preimage\":\"asasasasassasaksjkasklasjklsaj\",\"description\":\"asasas\",\"expires_at\":1201201020100}";
        Gson gson = new Gson();
//        Type type = new TypeToken<Invoice>() {
//        }.getType();
        JSONArray jsonArray = null;
        String json = "";
        try {
            jsonArray = new JSONObject(response).getJSONArray("invoices");
            json = jsonArray.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Invoice invoice = gson.fromJson(json, Invoice.class);
        GlobalState.getInstance().setInvoice(invoice);
        if (invoice != null) {
            if (invoice.getStatus().equals("paid")) {
                saveGetPaidTransactionInLog(invoice);
                distributeGetPaidDialog.dismiss();
                simpleloader.show();
                dialogBoxForConfirmPaymentInvoice(invoice);
                confirmInvoicePamentProgressDialog.dismiss();
            } else {
                simpleloader.show();
                distributeGetPaidDialog.dismiss();
                confirmInvoicePamentProgressDialog.dismiss();
                new AlertDialog.Builder(getContext())
                        .setMessage("Payment Not Recieved")
                        .setPositiveButton("Retry", null)
                        .show();
            }
        } else {
            simpleloader.show();
            distributeGetPaidDialog.dismiss();
            confirmInvoicePamentProgressDialog.dismiss();
            new AlertDialog.Builder(getContext())
                    .setMessage("Payment Not Recieved")
                    .setPositiveButton("Retry", null)
                    .show();
        }
        return invoice;
    }

    private void dialogBoxForConfirmPaymentInvoice(final Invoice invoice) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        confirmPaymentDialog = new Dialog(getContext());
        confirmPaymentDialog.setContentView(R.layout.customlayoutofconfirmpaymentdialogformerchantadmin);
        Objects.requireNonNull(confirmPaymentDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmPaymentDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        confirmPaymentDialog.setCancelable(false);
        //init dialog views

        final ImageView ivBack = confirmPaymentDialog.findViewById(R.id.iv_back_invoice);
        final TextView amount = confirmPaymentDialog.findViewById(R.id.et_amount);
        final ImageView payment_preImage = confirmPaymentDialog.findViewById(R.id.et_preimage);
        final TextView paid_at = confirmPaymentDialog.findViewById(R.id.et_paidat);
        final TextView purchased_Items = confirmPaymentDialog.findViewById(R.id.et_perchaseditems);
        //  final TextView tax=confirmPaymentDialog.findViewById(R.id.et_tax);
        final Button printInvoice = confirmPaymentDialog.findViewById(R.id.btn_printinvoice);
        amount.setVisibility(View.GONE);
        payment_preImage.setVisibility(View.GONE);
        paid_at.setVisibility(View.GONE);
        purchased_Items.setVisibility(View.GONE);
        //   tax.setVisibility(View.GONE);
        printInvoice.setVisibility(View.GONE);

        if (invoice != null) {
            if (invoice.getStatus().equals("paid")) {
                InvoiceForPrint invoiceForPrint = new InvoiceForPrint();
                //before // invoiceForPrint.setMsatoshi(invoice.getMsatoshi()/1000);
                //After
                invoiceForPrint.setMsatoshi(invoice.getMsatoshi());
                invoiceForPrint.setPayment_preimage(invoice.getPayment_preimage());
                invoiceForPrint.setPaid_at(invoice.getPaid_at());
                invoiceForPrint.setPurchasedItems(getPaidDescrition);
                invoiceForPrint.setTax(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)) + "BTC\n$" + round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2) + "USD");
                GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
                amount.setVisibility(View.VISIBLE);
                payment_preImage.setVisibility(View.VISIBLE);
                paid_at.setVisibility(View.VISIBLE);
                purchased_Items.setVisibility(View.VISIBLE);
                //  tax.setVisibility(View.VISIBLE);
                printInvoice.setVisibility(View.VISIBLE);
                String amountInBtc = excatFigure(mSatoshoToBtc(invoice.getMsatoshi()));
                double amounttempusd = round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)) + "BTC\n$" + precision.format(round(amounttempusd, 2)) + "USD");
                //payment_preImage.setText(invoice.getPayment_preimage());
                //  payment_preImage.setImageBitmap(getBitMapFromHex(invoice.getPayment_preimage()));
                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                paid_at.setText(getDateFromUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                purchased_Items.setText(getPaidDescrition);
                //   tax.setText(excatFigure(round(getTaxOfBTC(mSatoshoToBtc(invoice.getMsatoshi())),9))+" BTC\n"+round(getTaxOfUSD(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi()))),2)+"$");

            } else {
                InvoiceForPrint invoiceForPrint = new InvoiceForPrint();
                invoiceForPrint.setMsatoshi(0.0);
                invoiceForPrint.setPayment_preimage("N/A");
                invoiceForPrint.setPaid_at(0000);
                invoiceForPrint.setPurchasedItems(getPaidDescrition);
                invoiceForPrint.setDesscription(getPaidDescrition);
                invoiceForPrint.setTax(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)) + "BTC\n$" + round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2) + "USD");
                GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
                amount.setVisibility(View.VISIBLE);
                payment_preImage.setVisibility(View.VISIBLE);
                paid_at.setVisibility(View.VISIBLE);
                purchased_Items.setVisibility(View.VISIBLE);
                // tax.setVisibility(View.VISIBLE);
                printInvoice.setVisibility(View.VISIBLE);
                amount.setText("0.0");
                paid_at.setText("N/A");
                purchased_Items.setText(getPaidDescrition);
                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                double temmsat = invoice.getMsatoshi();
                temmsat = 240000000;
                // tax.setText("tem"+excatFigure(getTaxOfBTC(mSatoshoToBtc(temmsat)))+"BTC");
                //TODO: if payment not recived
            }
        }

        printInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InvoiceForPrint invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();

                if (invoice.getStatus().equals("paid")) {

                    getSalesListFromMerchantServer();
                    getRefundsListFromMerchantServer();
                    if (invoiceForPrint != null) {


                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (!mBluetoothAdapter.isEnabled()) {
                            dialogBoxForConnecctingBTPrinter();
                        } else {
                            if (mBluetoothSocket != null) {
                                Toast.makeText(getContext(), "Already Connected", Toast.LENGTH_LONG).show();
                                try {

                                    sendData("getPaidDistribute");
                                } catch (IOException e) {
                                    Log.e("SendDataError", e.toString());
                                    e.printStackTrace();
                                }


                            } else {
                                dialogBoxForConnecctingBTPrinter();
                            }
                        }


                        //:TODO : do what need to make print..
                    } else {
                        confirmPaymentDialog.dismiss();

                        //TODO: do when nothin to print
                    }

                } else {
                    getSalesListFromMerchantServer();
                    getRefundsListFromMerchantServer();
                    //TODO:When Unpaid
                    if (invoiceForPrint != null) {
                        //:TODO : do what need to make print..
                        confirmPaymentDialog.dismiss();
                    } else {
                        confirmPaymentDialog.dismiss();
                        //TODO: do when nothin to print
                    }
                }
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSalesListFromMerchantServer();
                getRefundsListFromMerchantServer();
                confirmPaymentDialog.dismiss();
            }
        });
        confirmPaymentDialog.show();
    }

    //TOOD:Refund  Or Commandeer
    private void dialogBoxForRefundCommandeer() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        commandeerRefundDialog = new Dialog(getContext());
        commandeerRefundDialog.setContentView(R.layout.dialoglayoutrefundcommandeer);

        Objects.requireNonNull(commandeerRefundDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        commandeerRefundDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        commandeerRefundDialog.setCancelable(false);
        final EditText bolt11 = commandeerRefundDialog.findViewById(R.id.bolt11val);
        final ImageView ivBack = commandeerRefundDialog.findViewById(R.id.iv_back_invoice);
        Button btnNext = commandeerRefundDialog.findViewById(R.id.btn_next);
        Button btnscanQr = commandeerRefundDialog.findViewById(R.id.btn_scanQR);
        // progressBar = dialog.findViewById(R.id.progress_bar);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandeerRefundDialog.dismiss();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bolt11value = bolt11.getText().toString();
                if (bolt11value.isEmpty()) {
                    showToast("Bolt11 " + getString(R.string.empty));
                    return;
                } else {
                    commandeerRefundDialog.dismiss();
                    bolt11fromqr = bolt11value;
//                    decodeBolt11(bolt11value);
                    RefundDecodePay(bolt11value);

                }

            }


        });
        btnscanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                qrScan.forSupportFragment(MerchantFragment1.this).initiateScan();
//                commandeerRefundDialog.dismiss();
//                String bolt11value="";
//                dialogBoxForRefundCommandeerStep2(bolt11value);

            }
        });


        commandeerRefundDialog.show();

    }

    private void dialogBoxForRefundCommandeerStep2(final String bolt11value, String msatoshi) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        commandeerRefundDialogstep2 = new Dialog(getContext());
        commandeerRefundDialogstep2.setContentView(R.layout.dialoglayoutrefundcommandeerstep2);
        Objects.requireNonNull(commandeerRefundDialogstep2.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        commandeerRefundDialogstep2.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        commandeerRefundDialogstep2.setCancelable(false);


        String mst = msatoshi;
        MSATOSHI = (Double.valueOf(msatoshi));
        double btc = mSatoshoToBtc(Double.valueOf(msatoshi));
        // double priceInBTC = GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
        double priceInBTC = GlobalState.getInstance().getChannel_btcResponseData().getPrice();
        double usd = priceInBTC * btc;
        AMOUNT_USD = usd;
        AMOUNT_BTC = btc;
        CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC;
        usd = round(usd, 2);
        mst = String.valueOf(usd);
        final TextView bolt11 = commandeerRefundDialogstep2.findViewById(R.id.bolt11valtxt);
        final TextView label = commandeerRefundDialogstep2.findViewById(R.id.labelvaltxt);
        final EditText amount = commandeerRefundDialogstep2.findViewById(R.id.amountval);
        amount.setText(mst);
        amount.setInputType(InputType.TYPE_NULL);
        final ImageView ivBack = commandeerRefundDialogstep2.findViewById(R.id.iv_back_invoice);
        Button excecute = commandeerRefundDialogstep2.findViewById(R.id.btn_next);
        bolt11.setText(bolt11value);
        label.setText("outgoing" + getUnixTimeStamp());
        getRefubdLABEL = label.getText().toString();

        if (msatoshi.equals("0.0")) {
            excecute.setVisibility(View.INVISIBLE);
        }
        // progressBar = dialog.findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandeerRefundDialogstep2.dismiss();
            }
        });
        excecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bolt11val = bolt11.getText().toString();
                String labelval = label.getText().toString();
                String amountval = amount.getText().toString();
                boolean status = true;
                if (bolt11val.isEmpty()) {
                    showToast("Bolt11 " + getString(R.string.empty));
                    status = false;
                    return;
                }
                if (labelval.isEmpty()) {
                    showToast("Label " + getString(R.string.empty));
                    status = false;
                    return;
                }

                if (status) {
                    executeCommandeerRefundApi(bolt11val, labelval, amountval);
                    commandeerRefundDialogstep2.dismiss();
                    //Confirmationn MSg
                }

            }


        });
        commandeerRefundDialogstep2.show();
    }

    private Pay parseJSONForPayOthers(String jsonString) {


        Pay pay = null;
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject jsonObj = null;
        if (jsonArr != null) {
            try {
                jsonObj = jsonArr.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (jsonObj != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<Pay>() {
                }.getType();
                pay = gson.fromJson(jsonObj.toString(), type);
                // GlobalState.getInstance().setInvoice(pay);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
        return pay;
    }

    private DecodePayBolt11 parseJSONForDecodePayBolt11(String jsonString) {
        DecodePayBolt11 decodePayBolt11 = null;
        JSONArray jsonArr = null;
        if (jsonString.equals("")) {

        } else {
            try {
                Gson gson = new Gson();
//                Type type = new TypeToken<DecodePayBolt11>() {
//                }.getType();
                decodePayBolt11 = gson.fromJson(jsonString, DecodePayBolt11.class);
                GlobalState.getInstance().setCurrentDecodePayBolt11(decodePayBolt11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            if (decodePayBolt11.getMsatoshi() != 0) {

                if (decodePayBolt11 != null) {
                    decodePayBolt11ProgressDialog.dismiss();
                    if (decodePayBolt11.getDescription() != null) {
                        current_transaction_description = decodePayBolt11.getDescription();
                    } else {
                        current_transaction_description = "receipt now";
                    }
                    dialogBoxForRefundCommandeerStep2(bolt11fromqr, String.valueOf(decodePayBolt11.getMsatoshi()));
                } else {
                    DecodePayBolt11 decode2PayBolt11 = new DecodePayBolt11();
                    decode2PayBolt11.setMsatoshi(0);
                    GlobalState.getInstance().setCurrentDecodePayBolt11(decode2PayBolt11);
                    decodePayBolt11ProgressDialog.dismiss();
                    dialogBoxForRefundCommandeerStep2(bolt11fromqr, String.valueOf(decode2PayBolt11.getMsatoshi()));
                }

            } else {
                decodePayBolt11 = new DecodePayBolt11();
                decodePayBolt11.setMsatoshi(0);
                GlobalState.getInstance().setCurrentDecodePayBolt11(decodePayBolt11);
                decodePayBolt11ProgressDialog.dismiss();
                dialogBoxForRefundCommandeerStep2(bolt11fromqr, String.valueOf(decodePayBolt11.getMsatoshi()));
            }
        }
        return decodePayBolt11;
    }

    private void executeCommandeerRefundApi(String bolt11value, String labelval, String amountusd) {
        //double priceInBTC = 1 / GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
        double priceInBTC = 1 / GlobalState.getInstance().getChannel_btcResponseData().getPrice();
        priceInBTC = priceInBTC * Double.parseDouble(amountusd);
        double amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi;
        amountInMsatoshi = amountInMsatoshi * AppConstants.satoshiToMSathosi;
        //msatoshi=excatFigure(amountInMsatoshi);
        NumberFormat formatter = new DecimalFormat("#0");
        String rMSatoshi = formatter.format(amountInMsatoshi);
        //showToast(bolt11value+"-"+labelval+"-"+rMSatoshi);
        Log.e("abcd", bolt11value + "-" + labelval + "-" + rMSatoshi);
        //luqman pending
        PayRequestToOther(bolt11value, rMSatoshi, labelval);
    }

    protected void paytoothersResponse(String result) {


        String response = result;
        Log.e(TAG, "PayOthers:" + result);
        if (response.contains("payment_hash")) {
            String[] split = response.split(",");
            String invoiceReponse = "";
            for (int i = 4; i < split.length; i++) {
                invoiceReponse += "," + split[i];
            }
            invoiceReponse = invoiceReponse.substring(1);
            Pay payresponse = parseJSONForPayOthers(invoiceReponse);
            if (payresponse != null) {
                if (payresponse.getStatus().equals("complete")) {
                    saveGetRefundTransactionInLog(payresponse);
                    //showToast("Succefully Pay");
                    showCofirmationDialog(payresponse);
                } else {
                    Pay payresponse2 = new Pay();
                    payresponse2.setStatus("Not complete");
                    showCofirmationDialog(payresponse2);
                }

            } else {
                Pay payresponse22 = new Pay();
                payresponse22.setStatus("Not complete");
                showCofirmationDialog(payresponse22);

            }

            payOtherProgressDialog.dismiss();
        } else {
            Pay payresponse22 = new Pay();
            payresponse22.setStatus("Not complete");
            showCofirmationDialog(payresponse22);
            payOtherProgressDialog.dismiss();
        }
    }

    private void showCofirmationDialog(final Pay payresponse) {
        Log.e("errorhe", "showCofirmationDialog me agya");
        InvoiceForPrint invoiceForPrint = new InvoiceForPrint();
        invoiceForPrint.setDestination(payresponse.getDestination());
        invoiceForPrint.setMsatoshi(payresponse.getMsatoshi());
        invoiceForPrint.setPayment_preimage(payresponse.getPayment_preimage());
        invoiceForPrint.setCreated_at(payresponse.getCreated_at());
        // invoiceForPrint.setPayment_hash(payresponse.getPayment_hash());
        invoiceForPrint.setPurchasedItems(current_transaction_description);
        invoiceForPrint.setDesscription(current_transaction_description);
        GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        commandeerRefundDialogstep2 = new Dialog(getContext());
        commandeerRefundDialogstep2.setContentView(R.layout.dialoglayoutrefundcommandeerlaststepconfirmedpay);
        Objects.requireNonNull(commandeerRefundDialogstep2.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final ImageView ivBack = commandeerRefundDialogstep2.findViewById(R.id.iv_back_invoice);
        final TextView textView = commandeerRefundDialogstep2.findViewById(R.id.textView2);
        final Button ok = commandeerRefundDialogstep2.findViewById(R.id.btn_ok);
        commandeerRefundDialogstep2.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        commandeerRefundDialogstep2.setCancelable(false);

        textView.setText("Payment Status:" + payresponse.getStatus());
        if (payresponse.getStatus().equals("complete")) {
            ok.setText("Print");
        }
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InvoiceForPrint invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();
                if (payresponse.getStatus().equals("complete")) {
                    getRefundsListFromMerchantServer();
                    getSalesListFromMerchantServer();
                    if (invoiceForPrint != null) {
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (!mBluetoothAdapter.isEnabled()) {
                            dialogBoxForConnecctingBTPrinter();
                        } else {
                            if (mBluetoothSocket != null) {
                                Toast.makeText(getContext(), "Already Connected", Toast.LENGTH_LONG).show();
                                try {
                                    sendData("commandeerRefund");
                                } catch (IOException e) {
                                    e.printStackTrace();

                                }
                                commandeerRefundDialogstep2.dismiss();
                            } else {
                                dialogBoxForConnecctingBTPrinter();
                            }
                        }
                        //:TODO : do what need to make print..
                    } else {
                        confirmPaymentDialog.dismiss();
                        //TODO: do when nothin to print
                    }

                } else {
                    getRefundsListFromMerchantServer();
                    getSalesListFromMerchantServer();
                    commandeerRefundDialogstep2.dismiss();
                }

            }
        });

        // progressBar = dialog.findViewById(R.id.progress_bar);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandeerRefundDialogstep2.dismiss();
                getRefundsListFromMerchantServer();
                getSalesListFromMerchantServer();

            }
        });
        commandeerRefundDialogstep2.show();


    }


    //Getting the scan results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // check whether storage permission granted or not.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // do what you want;
                        try {
                            getCSV();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1234:
                // HANDLE LockIng
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == 1234) {
                    if (resultCode == RESULT_OK) {
                        //do something you want when pass the security
                        // Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                        dialogBoxForRefundCommandeer();
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    ListPairedDevices();
                    initials();
                } else {
                    Toast.makeText(getContext(), "Message", Toast.LENGTH_SHORT).show();
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
                        commandeerRefundDialog.dismiss();
                        bolt11fromqr = result.getContents();
                        // showToast(bolt11fromqr);
//                        decodeBolt11(bolt11fromqr);
                        RefundDecodePay(bolt11fromqr);

                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }
    }

    //TODO: Printing Stuff Start
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
        final ImageView ivBack = blutoothDevicesDialog.findViewById(R.id.iv_back_invoice);
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

    void sendData(String getPaidDistribute) throws IOException {

        try {
            switch (getPaidDistribute) {
                case "getPaidDistribute":
                    btoutputstream = mBluetoothSocket.getOutputStream();
                    // the text typed by the user
                    InvoiceForPrint recInvoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();
                    DecimalFormat precision = new DecimalFormat("0.00");
                    if (recInvoiceForPrint != null) {
                        final String paidAt = getDateFromUTCTimestamp(recInvoiceForPrint.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE);
                        final String amount = excatFigure(round((mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 9)) + "BTC/$" + precision.format(round(getUsdFromBtc(mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 2)) + "USD";
                        final String amountInBtc = excatFigure(round((mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 9)) + " BTC";
                        final String amountInUsd = precision.format(round(getUsdFromBtc(mSatoshoToBtc(recInvoiceForPrint.getMsatoshi())), 2)) + " USD";
                        final String des = recInvoiceForPrint.getPurchasedItems();
                        final Bitmap bitmap = getBitMapFromHex(recInvoiceForPrint.getPayment_preimage());
                        printingProgressBar.show();
                        printingProgressBar.setCancelable(false);
                        printingProgressBar.setCanceledOnTouchOutside(false);
                        Thread t = new Thread() {
                            public void run() {
                                try {
                                    // This is printer specific code you can comment ==== > Start
                                    btoutputstream.write(PrinterCommands.reset);
                                    btoutputstream.write(PrinterCommands.INIT);
                                    btoutputstream.write("\n\n".getBytes());
                                    btoutputstream.write("    Sale / Incoming Funds".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write("    ---------------------".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write(des.getBytes());
                                    btoutputstream.write("\n\n".getBytes());
                                    btoutputstream.write("\tAmount: ".getBytes());
                                    btoutputstream.write("\n\t".getBytes());
                                    //amountInBTC should right
                                    btoutputstream.write(amountInBtc.getBytes());
                                    btoutputstream.write("\n\t".getBytes());
                                    //amountInBTC should right
                                    btoutputstream.write(amountInUsd.getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    //Paid at title should center
                                    btoutputstream.write("\tReceived:".getBytes());
                                    btoutputstream.write("\n  ".getBytes());
                                    //Paid at   should center
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
                                    }
                                    btoutputstream.write("\n\n".getBytes());
                                    Thread.sleep(1000);
                                    printingProgressBar.dismiss();


                                } catch (Exception e) {
                                    Log.e("PrintError", "Exe ", e);

                                }

                            }
                        };
                        t.start();
                    } else {
                        btoutputstream.write(PrinterCommands.reset);
                        btoutputstream.write(PrinterCommands.INIT);
                        btoutputstream.write(PrinterCommands.FEED_LINE);
                        String paidAt = "\n\n\n\n\n\n\nNot Data Found\n\n\n\n\n\n\n";
                        btoutputstream.write(paidAt.getBytes());
                    }
                    break;
                case "commandeerRefund":
                    Log.e("errorhe", "commandeerRefund Case me");
                    btoutputstream = mBluetoothSocket.getOutputStream();
                    InvoiceForPrint recInvoiceForPrint2 = GlobalState.getInstance().getInvoiceForPrint();
                    DecimalFormat precision2 = new DecimalFormat("0.00");
                    if (recInvoiceForPrint2 != null) {
                        Log.e("errorhe", " recInvoiceForPrint2 not Null");
                        final String paidAt2 = getDateFromUTCTimestamp(recInvoiceForPrint2.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE);
                        final String amount2 = excatFigure(round((mSatoshoToBtc(recInvoiceForPrint2.getMsatoshi())), 9)) + "BTC/$" + precision2.format(round(getUsdFromBtc(mSatoshoToBtc(recInvoiceForPrint2.getMsatoshi())), 2)) + "USD";
                        final String amountInBtc2 = excatFigure(round((mSatoshoToBtc(recInvoiceForPrint2.getMsatoshi())), 9)) + " BTC";
                        final String amountInUsd2 = precision2.format(round(getUsdFromBtc(mSatoshoToBtc(recInvoiceForPrint2.getMsatoshi())), 2)) + " USD";
                        final String des2 = recInvoiceForPrint2.getPurchasedItems();
                        final Bitmap paymentHashBitmap = getBitMapFromHex(recInvoiceForPrint2.getPayment_preimage());
                        final Bitmap destinationBitmap = getBitMapFromHex(recInvoiceForPrint2.getDestination());
                        printingProgressBar.show();
                        printingProgressBar.setCancelable(false);
                        printingProgressBar.setCanceledOnTouchOutside(false);
                        Thread t2 = new Thread() {
                            public void run() {
                                try {
                                    // This is printer specific code you can comment ==== > Start
                                    btoutputstream.write(PrinterCommands.reset);
                                    btoutputstream.write(PrinterCommands.INIT);
                                    btoutputstream.write("\n\n".getBytes());
                                    btoutputstream.write("    Refund / Payout".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write("    ---------------".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write(des2.getBytes());
                                    btoutputstream.write("\n\n".getBytes());
                                    btoutputstream.write("\tAmount: ".getBytes());
                                    btoutputstream.write("\n\t".getBytes());
                                    //amountInBTC should right
                                    btoutputstream.write(amountInBtc2.getBytes());
                                    btoutputstream.write("\n\t".getBytes());
                                    //amountInBTC should right
                                    btoutputstream.write(amountInUsd2.getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    //Paid at title should center
                                    btoutputstream.write("\tReceived:".getBytes());
                                    btoutputstream.write("\n  ".getBytes());
                                    //Paid at   should center
                                    btoutputstream.write("  ".getBytes());
                                    btoutputstream.write(paidAt2.getBytes());
                                    btoutputstream.write("\n\n".getBytes());
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write("\tBolt 11:".getBytes());
                                    if (destinationBitmap != null) {
                                        Bitmap bMapScaled = Bitmap.createScaledBitmap(destinationBitmap, 250, 250, true);
                                        new ByteArrayOutputStream();
                                        PrintPic printPic = PrintPic.getInstance();
                                        printPic.init(bMapScaled);
                                        byte[] bitmapdata = printPic.printDraw();
                                        btoutputstream.write(PrinterCommands.print);
                                        btoutputstream.write(bitmapdata);
                                        btoutputstream.write(PrinterCommands.print);
                                    }
                                    btoutputstream.write("\n".getBytes());
                                    btoutputstream.write("\tPayment Hash:".getBytes());
                                    printNewLine();
                                    if (paymentHashBitmap != null) {
                                        Bitmap bMapScaled = Bitmap.createScaledBitmap(paymentHashBitmap, 250, 250, true);
                                        new ByteArrayOutputStream();
                                        PrintPic printPic = PrintPic.getInstance();
                                        printPic.init(bMapScaled);
                                        byte[] bitmapdata = printPic.printDraw();
                                        btoutputstream.write(PrinterCommands.print);
                                        btoutputstream.write(bitmapdata);
                                        btoutputstream.write(PrinterCommands.print);
                                    }
                                    btoutputstream.write("\n\n".getBytes());
                                    Thread.sleep(1000);
                                    printingProgressBar.dismiss();
                                } catch (Exception e) {
                                    Log.e("PrintError", "Exe ", e);
                                    Log.e("errorhe", "0");
                                }

                            }
                        };
                        t2.start();
                    } else {
                        Log.e("errorhe", "recInvoiceForPrint2 Is Null");
                        Log.e("errorhe", "1");
                        btoutputstream.write(PrinterCommands.reset);
                        btoutputstream.write(PrinterCommands.INIT);
                        btoutputstream.write(PrinterCommands.FEED_LINE);
                        String paidAt = "\n\n\n\n\n\n\nNot Data Found\n\n\n\n\n\n\n";
                        btoutputstream.write(paidAt.getBytes());
                    }
                    break;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.e("errorhe", "3");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("errorhe", "3");
        }
    }

    protected void printNewLine() {
        try {
            btoutputstream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: UPload Transaction to Web PAnnel
    private void saveGetPaidTransactionInLog(Invoice invoice) {
        DecimalFormat precision = new DecimalFormat("0.00");
        String transaction_label = ((transaction_label = invoice.getLabel()) != null) ? transaction_label : getPaidLABEL;
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
        add_alpha_transaction(transaction_label, status, transaction_amountBTC, transaction_amountUSD, conversion_rate, msatoshi, payment_preimage, payment_hash, destination, merchant_id, transaction_description1);
    }

    private void saveGetRefundTransactionInLog(Pay payresponse) {
        DecimalFormat precision = new DecimalFormat("0.00");
        String transaction_label = getRefubdLABEL;
        String status = ((status = payresponse.getStatus()) != null) ? status : "Complete";
        String transaction_amountBTC = excatFigure(round(AMOUNT_BTC, 9));
        String transaction_amountUSD = precision.format(AMOUNT_USD);
        String conversion_rate = precision.format(CONVERSION_RATE);
        String msatoshi = String.valueOf(MSATOSHI);
        String payment_preimage = ((payment_preimage = payresponse.getPayment_preimage()) != null) ? payment_preimage : "test123";
        String payment_hash = ((payment_hash = payresponse.getPayment_hash()) != null) ? payment_hash : "test123";
        String destination = ((destination = payresponse.getDestination()) != null) ? destination : "destination123";
        String merchant_id = ((merchant_id = GlobalState.getInstance().getMerchant_id()) != null) ? merchant_id : "mg123";
        String transaction_description1 = current_transaction_description;
        add_alpha_transaction(transaction_label, status, transaction_amountBTC, transaction_amountUSD, conversion_rate, msatoshi, payment_preimage, payment_hash, destination, merchant_id, transaction_description1);


    }

    public void add_alpha_transaction(String transaction_label, String status, String transaction_amountBTC, String transaction_amountUSD, String conversion_rate, String msatoshi, String payment_preimage, String payment_hash, String destination, String merchant_id, String transaction_description) {
        Call<TransactionResp> call = ApiClient.getRetrofit().create(ApiPaths.class).add_alpha_transction(transaction_label, status, transaction_amountBTC, transaction_amountUSD, payment_preimage, payment_hash, conversion_rate, msatoshi, destination, merchant_id, transaction_description);
        call.enqueue(new Callback<TransactionResp>() {
            @Override
            public void onResponse(Call<TransactionResp> call, Response<TransactionResp> response) {
                if (response != null) {
                    if (response.body() != null) {
                        TransactionResp transactionResp = response.body();
                        if (transactionResp.getMessage().equals("successfully done") && transactionResp.getTransactionInfo() != null) {
                            TransactionInfo transactionInfo = new TransactionInfo();
                            transactionInfo = transactionResp.getTransactionInfo();

                        } else {
                            showToast("Not Done!!");
                        }
                        Log.e("Test", "Test");
                    }
                }

                Log.e("AddTransactionLog", response.message());
            }

            @Override
            public void onFailure(Call<TransactionResp> call, Throwable t) {
                Log.e("AddTransactionLog", t.getMessage().toString());
            }
        });
    }

    //TODO:Make CSV and Email Sending
    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;
        folderpath = new File(Environment.getExternalStorageDirectory(), path);
        if (!folderpath.exists()) {
            if (!folderpath.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

    public void getCSV() throws FileNotFoundException {
        if (mSaleDataSource != null) {
            if (mSaleDataSource.size() > 0) {
                dailyReportSendingEmailSale(mSaleDataSource);
                weeklyReportSendingEmailSale(mSaleDataSource);
                monthlyReportSendingEmailSale(mSaleDataSource);
            }
        }
        if (mRefundDataSource != null) {
            if (mRefundDataSource.size() > 0) {
                dailyReportSendingEmailRefund(mRefundDataSource);
                weeklyReportSendingEmailRefund(mRefundDataSource);
                monthlyReportSendingEmailRefund(mRefundDataSource);
            }
        }

    }

    //TODO:Refunds Emailing
    private void monthlyReportSendingEmailRefund(ArrayList<Refund> mRefundDataSource) {
        ArrayList<Refund> todayList = new ArrayList<>();
        for (Refund refund : mRefundDataSource) {
            long curentTime = new Date().getTime();
            long paidTime = refund.getCreated_at() * 1000;
            long dayDiff = getDayDiffDates(paidTime, curentTime);
            Date currentDate = new Date(curentTime);
            Date paidDate = new Date(paidTime);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(currentDate);
            cal2.setTime(paidDate);
            boolean sameMonth = cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
//            if(sameMonth){
            todayList.add(refund);
//            }
        }
        if (todayList.size() > 0) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
                date = sdf.parse(preferences.getString("myMonthlyDate2", "01-01-2000"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long curentTime = new Date().getTime();
            Date currentDate = new Date(curentTime);
            if (date != null) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(currentDate);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                if (sameDay) {
                    //showToast("Already Refund Email Report Month ");
                } else {
                    //showToast("New  Refund Report Email Month");
                    sendEmailMonthlyRefund(todayList);
                }
            } else {
                //showToast("First Refund Time New Report Email Month");
                sendEmailMonthlyRefund(todayList);
            }
        }
    }

    private void sendEmailMonthlyRefund(ArrayList<Refund> todayList) {
        String merchantId = "merchant";
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID();
            }
        }
        long curentTime = new Date().getTime();
        Date currentDate = new Date(curentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
        preferences.edit().putString("myMonthlyDate2", sdf.format(currentDate)).apply();

        //Create Folder
        File file = null;
        boolean t = createDirIfNotExists("/CLightData/Refund/Monthly");
        if (t) {
            String extStorageDirectory = folderpath.toString();

            String fileName = merchantId + "_Monthly" + getUnixTimeStamp() + ".csv";
            file = new File(extStorageDirectory, fileName);
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                List<String[]> data2 = toStringArrayFromRefund(todayList);
                // create a List which contains String array
                writer.writeAll(data2);
                // closing writer connection
                writer.close();
                GlobalState.getInstance().setRefundFile(file);
                sendRefundEmail("Monthly");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            showToast("Error Making Folder");

        }
    }

    private void weeklyReportSendingEmailRefund(ArrayList<Refund> mRefundDataSource) {
        ArrayList<Refund> todayList = new ArrayList<>();
        for (Refund refund : mRefundDataSource) {
            long curentTime = new Date().getTime();
            long paidTime = refund.getCreated_at() * 1000;
            long dayDiff = getDayDiffDates(paidTime, curentTime);
            Date currentDate = new Date(curentTime);
            Date paidDate = new Date(paidTime);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(currentDate);
            cal2.setTime(paidDate);
            boolean isSameWeek = isCurrentWeekDateSelect(cal2);
//            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
//                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            if (isSameWeek) {
                todayList.add(refund);
            }
        }
        if (todayList.size() > 0) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
                date = sdf.parse(preferences.getString("myWeeklyDate2", "01-01-2000"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long curentTime = new Date().getTime();
            Date currentDate = new Date(curentTime);

            if (date != null) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(currentDate);
                boolean sameWeek = isCurrentWeekDateSelect(cal1);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

                if (sameWeek) {
                    //  showToast("Already Email In Week ");
                } else {
                    //   showToast("New Report Email in Week");
                    sendEmailWeeklyRefund(todayList);
                }
            } else {
                //  showToast("First Time New Report Email in Week");
                sendEmailWeeklyRefund(todayList);
            }
        }
    }

    private void sendEmailWeeklyRefund(ArrayList<Refund> todayList) {
        String merchantId = "merchant";
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID();
            }
        }
        long curentTime = new Date().getTime();
        Date currentDate = new Date(curentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
        preferences.edit().putString("myWeeklyDate2", sdf.format(currentDate)).apply();

        //Create Folder
        File file = null;
        boolean t = createDirIfNotExists("/CLightData/Refund/Weekly");
        if (t) {
            String extStorageDirectory = folderpath.toString();

            String fileName = merchantId + "_Weekly" + getUnixTimeStamp() + ".csv";
            file = new File(extStorageDirectory, fileName);
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                List<String[]> data2 = toStringArrayFromRefund(todayList);
                // create a List which contains String array
                writer.writeAll(data2);
                // closing writer connection
                writer.close();
                GlobalState.getInstance().setSaleFile(file);
                sendRefundEmail("Weekly");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            showToast("Error Making Folder");
        }
    }

    private void dailyReportSendingEmailRefund(ArrayList<Refund> mRefundDataSource) {


        ArrayList<Refund> todayList = new ArrayList<>();
        for (Refund refund : mRefundDataSource) {
            long curentTime = new Date().getTime();
            long paidTime = refund.getCreated_at() * 1000;
            long dayDiff = getDayDiffDates(paidTime, curentTime);
            Date currentDate = new Date(curentTime);
            Date paidDate = new Date(paidTime);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(currentDate);
            cal2.setTime(paidDate);
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            if (sameDay) {
                todayList.add(refund);
            }
        }
        if (todayList.size() > 0) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
                date = sdf.parse(preferences.getString("myDailyDate2", "01-01-2000"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long curentTime = new Date().getTime();
            Date currentDate = new Date(curentTime);

            if (date != null) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(currentDate);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

                if (sameDay) {
                    // showToast("Already Email Report Today ");
                } else {
                    // showToast("New  Report Email Today ");
                    sendEmailDailyRefund(todayList);
                }
            } else {
                // showToast("First Time New Report Email Today ");
                sendEmailDailyRefund(todayList);
            }
        }
    }

    private void sendEmailDailyRefund(ArrayList<Refund> todayList) {
        long curentTime = new Date().getTime();
        Date currentDate = new Date(curentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
        preferences.edit().putString("myDailyDate2", sdf.format(currentDate)).apply();

        String merchantId = "merchant";
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID();
            }
        }
        //Create Folder
        File file = null;
        boolean t = createDirIfNotExists("/CLightData/Refund/Daily");
        if (t) {
            String extStorageDirectory = folderpath.toString();

            String fileName = merchantId + "_daily" + getUnixTimeStamp() + ".csv";
            file = new File(extStorageDirectory, fileName);
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                List<String[]> data2 = toStringArrayFromRefund(todayList);
                // create a List which contains String array
                writer.writeAll(data2);
                // closing writer connection
                writer.close();
                GlobalState.getInstance().setSaleFile(file);
                sendRefundEmail("Daily");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            showToast("Error Making Folder");
        }
    }

    //TODO:Sales Emailing
    private void monthlyReportSendingEmailSale(ArrayList<Sale> saleList) {
        ArrayList<Sale> todayList = new ArrayList<>();
        for (Sale sale : saleList) {
            long curentTime = new Date().getTime();
            long paidTime = sale.getPaid_at() * 1000;
            long dayDiff = getDayDiffDates(paidTime, curentTime);
            Date currentDate = new Date(curentTime);
            Date paidDate = new Date(paidTime);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(currentDate);
            cal2.setTime(paidDate);
            boolean sameMonth = cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
//            if(sameMonth){
            todayList.add(sale);
//            }
        }
        if (todayList.size() > 0) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
                date = sdf.parse(preferences.getString("myMonthlyDate", "01-01-2000"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long curentTime = new Date().getTime();
            Date currentDate = new Date(curentTime);

            if (date != null) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(currentDate);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

                if (sameDay) {
                    //  showToast("Already Email Report Month ");
                } else {
                    //   showToast("New Report Email Month");
                    sendEmailMonthly(todayList);
                }
            } else {
                // showToast("First Time New Report Email Month");
                sendEmailMonthly(todayList);
            }
        }
    }

    private void sendEmailMonthly(ArrayList<Sale> todayList) {
        String merchantId = "merchant";
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID();
            }
        }
        long curentTime = new Date().getTime();
        Date currentDate = new Date(curentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
        preferences.edit().putString("myMonthlyDate", sdf.format(currentDate)).apply();

        //Create Folder
        File file = null;
        boolean t = createDirIfNotExists("/CLightData/Sales/Monthly");
        if (t) {
            String extStorageDirectory = folderpath.toString();

            String fileName = merchantId + "_Monthly" + getUnixTimeStamp() + ".csv";
            file = new File(extStorageDirectory, fileName);
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                List<String[]> data2 = toStringArrayFromSale(todayList);
                // create a List which contains String array
                writer.writeAll(data2);
                // closing writer connection
                writer.close();
                GlobalState.getInstance().setSaleFile(file);
                sendSaleEmail("Monthly");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            showToast("Error Making Folder");

        }
    }

    private void weeklyReportSendingEmailSale(ArrayList<Sale> saleList) {

        ArrayList<Sale> todayList = new ArrayList<>();
        for (Sale sale : saleList) {
            long curentTime = new Date().getTime();
            long paidTime = sale.getPaid_at() * 1000;
            long dayDiff = getDayDiffDates(paidTime, curentTime);
            Date currentDate = new Date(curentTime);
            Date paidDate = new Date(paidTime);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(currentDate);
            cal2.setTime(paidDate);
            boolean isSameWeek = isCurrentWeekDateSelect(cal2);
//            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
//                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
            if (isSameWeek) {
                todayList.add(sale);
            }
        }
        if (todayList.size() > 0) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
                date = sdf.parse(preferences.getString("myWeeklyDate", "01-01-2000"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long curentTime = new Date().getTime();
            Date currentDate = new Date(curentTime);

            if (date != null) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(currentDate);
                boolean sameWeek = isCurrentWeekDateSelect(cal1);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

                if (sameWeek) {
                    //showToast("Already Email In Week ");
                } else {
                    //showToast("New Report Email in Week");
                    sendEmailWeekly(todayList);
                }
            } else {
                //showToast("First Time New Report Email in Week");
                sendEmailWeekly(todayList);
            }
        }
    }

    private void sendEmailWeekly(ArrayList<Sale> todayList) {
        String merchantId = "merchant";
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID();
            }
        }
        long curentTime = new Date().getTime();
        Date currentDate = new Date(curentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
        preferences.edit().putString("myWeeklyDate", sdf.format(currentDate)).apply();

        //Create Folder
        File file = null;
        boolean t = createDirIfNotExists("/CLightData/Sales/Weekly");
        if (t) {
            String extStorageDirectory = folderpath.toString();

            String fileName = merchantId + "_Weekly" + getUnixTimeStamp() + ".csv";
            file = new File(extStorageDirectory, fileName);
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                List<String[]> data2 = toStringArrayFromSale(todayList);
                // create a List which contains String array
                writer.writeAll(data2);
                // closing writer connection
                writer.close();
                GlobalState.getInstance().setSaleFile(file);
                sendSaleEmail("Weekly");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            showToast("Error Making Folder");

        }
    }

    private void dailyReportSendingEmailSale(ArrayList<Sale> saleList) {

        ArrayList<Sale> todayList = new ArrayList<>();
        for (Sale sale : saleList) {
            long curentTime = new Date().getTime();
            long paidTime = sale.getPaid_at() * 1000;
            long dayDiff = getDayDiffDates(paidTime, curentTime);
            Date currentDate = new Date(curentTime);
            Date paidDate = new Date(paidTime);
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(currentDate);
            cal2.setTime(paidDate);
            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            if (sameDay) {
                todayList.add(sale);
            }
        }
        if (todayList.size() > 0) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
                date = sdf.parse(preferences.getString("myDailyDate", "01-01-2000"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long curentTime = new Date().getTime();
            Date currentDate = new Date(curentTime);

            if (date != null) {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(currentDate);
                boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

                if (sameDay) {
                    //showToast("Already Email Report Today ");
                } else {
                    //showToast("New  Report Email Today ");
                    sendEmailDaily(todayList);
                }
            } else {
                //showToast("First Time New Report Email Today ");
                sendEmailDaily(todayList);
            }
        }
    }

    private void sendEmailDaily(ArrayList<Sale> todayList) {
        long curentTime = new Date().getTime();
        Date currentDate = new Date(curentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.OUTPUT_DATE_FORMATE, Locale.US);
        preferences.edit().putString("myDailyDate", sdf.format(currentDate)).apply();

        String merchantId = "merchant";
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID();
            }
        }

        //Create Folder
        File file = null;
        boolean t = createDirIfNotExists("/CLightData/Sales/Daily");
        if (t) {
            String extStorageDirectory = folderpath.toString();

            String fileName = merchantId + "_daily" + getUnixTimeStamp() + ".csv";
            file = new File(extStorageDirectory, fileName);
            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(file);

                // create CSVWriter object filewriter object as parameter
                CSVWriter writer = new CSVWriter(outputfile);

                List<String[]> data2 = toStringArrayFromSale(todayList);
                // create a List which contains String array
                writer.writeAll(data2);
                // closing writer connection
                writer.close();
                GlobalState.getInstance().setSaleFile(file);
                sendSaleEmail("Daily");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            showToast("Error Making Folder");

        }
    }

    private static List<String[]> toStringArrayFromSale(List<Sale> emps) {
        List<String[]> records = new ArrayList<String[]>();

        // adding header record
        records.add(new String[]{"label", "msatoshi", "status", "paid_at", "payment_preimage", "description"});
        Iterator<Sale> it = emps.iterator();
        while (it.hasNext()) {
            Sale emp = it.next();
            records.add(new String[]{emp.getLabel(), excatFigure2(emp.getMsatoshi()), emp.getStatus(), getDateFromUTCTimestamp2(emp.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE), emp.getPayment_preimage(), emp.getDescription()});
        }
        return records;
    }

    private static List<String[]> toStringArrayFromRefund(List<Refund> emps) {
        List<String[]> records = new ArrayList<String[]>();

        // adding header record
        records.add(new String[]{"bolt11", "msatoshi", "status", "created_at", "destination", "payment_hash", "payment_preimage"});
        Iterator<Refund> it = emps.iterator();
        while (it.hasNext()) {
            Refund emp = it.next();
            records.add(new String[]{emp.getBolt11(), excatFigure2(emp.getMsatoshi()), emp.getStatus(), getDateFromUTCTimestamp2(emp.getCreated_at(), AppConstants.OUTPUT_DATE_FORMATE), emp.getDestination(), emp.getPayment_hash(), emp.getPayment_preimage()});
        }
        return records;
    }

    private boolean isCurrentWeekDateSelect(Calendar yourSelectedDate) {
        Date ddd = yourSelectedDate.getTime();
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("CST"));
        c.setFirstDayOfWeek(Calendar.MONDAY);

        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date monday = c.getTime();
        Date nextMonday = new Date(monday.getTime() + 7 * 24 * 60 * 60 * 1000);

        return ddd.after(monday) && ddd.before(nextMonday);
    }

    private void getSenderInfo(final String mail, final String pas, final String emailType) {
        String merchantId = "Merchant " + emailType + " Sales Sheet" + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID() + " Sales Sheet " + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
            }
        }

        final String finalMerchantId = merchantId;

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gMailSender = new GMailSender(mail, pas);
                    senderEmail = mail;
                    gMailSender.sendMail("CLight App",
                            "Sales ",
                            senderEmail,
                            "decentralizedworldinc@protonmail.com", finalMerchantId, 1);

                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                    //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        sender.start();


    }

    private void getSenderInfo2(final String mail, final String pas, final String emailType) {
        String merchantId = "Merchant " + emailType + " Sales Sheet" + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID() + " Sales Sheet " + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
            }
        }
//d4amenace@yahoo.com
        final String finalMerchantId = merchantId;
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gMailSender = new GMailSender(mail, pas);
                    senderEmail = mail;
                    gMailSender.sendMail("CLight App",
                            "Sales ",
                            senderEmail,
                            "d4amenace@yahoo.com", finalMerchantId, 1);

                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                    //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        sender.start();


    }

    private void getSenderInfo3(final String mail, final String pas, final String emailType) {
        String merchantId = "Merchant " + emailType + " Refunds Sheet" + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID() + " Refunds Sheet " + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
            }
        }
//d4amenace@yahoo.com
        final String finalMerchantId = merchantId;
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gMailSender = new GMailSender(mail, pas);
                    senderEmail = mail;
                    gMailSender.sendMail("CLight App",
                            "Refunds ",
                            senderEmail,
                            "decentralizedworldinc@protonmail.com", finalMerchantId, 0);

                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                    //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        sender.start();


    }

    private void getSenderInfo4(final String mail, final String pas, final String emailType) {
        String merchantId = "Merchant " + emailType + " Refunds Sheet" + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
        UserInfo userInfo = GlobalState.getInstance().getUserInfo();
        if (userInfo != null) {
            if (userInfo.getUserID() != null) {
                merchantId = userInfo.getUserID() + " Refunds Sheet " + getDateFromUTCTimestamp(getUnixTimeStampInLong(), AppConstants.OUTPUT_DATE_FORMATE);
            }
        }
//d4amenace@yahoo.com
        final String finalMerchantId = merchantId;
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gMailSender = new GMailSender(mail, pas);
                    senderEmail = mail;
                    gMailSender.sendMail("CLight App",
                            "Refunds ",
                            senderEmail,
                            "d4amenace@yahoo.com", finalMerchantId, 0);

                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                    //Toast.makeText(getApplicationContext(),"Error:"+e.getMessage().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        sender.start();


    }

    private void sendSaleEmail(String emailType) {
        getSenderInfo("nextlayertechnology@gmail.com", "bitcoin2020", emailType);
        getSenderInfo2("nextlayertechnology@gmail.com", "bitcoin2020", emailType);

    }

    private void sendRefundEmail(String emailType) {
        getSenderInfo3("nextlayertechnology@gmail.com", "bitcoin2020", emailType);
        getSenderInfo4("nextlayertechnology@gmail.com", "bitcoin2020", emailType);

    }


    public void onBackPressed() {
        //ByNaeem
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
                getContext().stopService(new Intent(getContext(), MyLogOutService.class));
                Intent ii = new Intent(getContext(), MainActivity.class);
                startActivity(ii);
              /*  ExitingFromServer exitingFromServer = new ExitingFromServer(getActivity());
                if (Build.VERSION.SDK_INT >= 11) {
                    exitingFromServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String("bye")});
                    exitFromServerProgressDialog.show();
                } else {
                    exitingFromServer.execute(new String[]{new String("bye")});
                    exitFromServerProgressDialog.show();
                }*/
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

    private void SubscrieChannel() {
        simpleloader.show();
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
                    simpleloader.dismiss();
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
                                                    simpleloader.dismiss();
                                                } catch (JSONException e) {
                                                    simpleloader.dismiss();
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                            simpleloader.dismiss();
                        }

                    } catch (JSONException err) {
                        simpleloader.dismiss();
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
                simpleloader.dismiss();
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
                            new CustomSharedPreferences().setString(merchantData.getSsh_password(), "sshkeypass", getContext());
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

    public void Confirmpayment(final String lable) {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());

                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listinvoices" + " " + lable + "\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                parseJSONForConfirmPayment(text);
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
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void CreateInvoice(final String rMSatoshi, final String label, final String descrption) {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());

                String json = UrlConstants.getInvoiceSendCommand(token, rMSatoshi, label, descrption);

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        webSocket.close(1000, null);
                                        webSocket.cancel();
                                        goTo2FaPasswordDialog();
                                    }
                                });
                            } else {
                                parseJSONForCreatInvocie(text);
                                simpleloader.dismiss();
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
                Log.e("TAG", "FAIL: " + response);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void getInvoicelist() {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listinvoices\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                parseJSONForSales(text);
                                simpleloader.dismiss();
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
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
        clientCoinPrice.retryOnConnectionFailure();
    }

    public void getRefundslist() {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listsendpays\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                parseJSONForRefunds(text);
                                simpleloader.dismiss();
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
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void sendpayslist() {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli listsendpays\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                parseJSONForRefunds(text);
                                simpleloader.dismiss();
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
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void RefundDecodePay(final String bolt11) {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli decodepay" + " " + bolt11 + "\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                if (!text.contains("error")) {
                                    parseJSONForDecodePayBolt11(text);
                                    simpleloader.dismiss();
                                } else {
                                    simpleloader.dismiss();
                                    try {
                                        JSONObject jsonObject1 = new JSONObject(text);
                                        String message = jsonObject1.getString("message");
                                        showToast(message);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
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
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void PayRequestToOther(final String bolt11value, String rMSatoshi, final String labelval) {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli pay" + " " + bolt11value + " " + "null" + " " + labelval + "\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    simpleloader.dismiss();
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
                                paytoothersResponse(text);
                                simpleloader.dismiss();
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
                        simpleloader.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }
}
