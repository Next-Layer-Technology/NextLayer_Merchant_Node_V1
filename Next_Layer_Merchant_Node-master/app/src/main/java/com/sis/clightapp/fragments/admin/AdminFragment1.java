package com.sis.clightapp.fragments.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sis.clightapp.Interface.ApiClient;
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
import com.sis.clightapp.adapter.AdminReceiveablesListAdapter;
import com.sis.clightapp.adapter.AdminSendablesListAdapter;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.CreateInvoice;
import com.sis.clightapp.model.GsonModel.DecodePayBolt11;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.model.GsonModel.InvoiceForPrint;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.Pay;
import com.sis.clightapp.model.GsonModel.Refund;
import com.sis.clightapp.model.GsonModel.Sale;
import com.sis.clightapp.model.Invoices.InvoicesResponse;
import com.sis.clightapp.model.REST.TransactionInfo;
import com.sis.clightapp.model.REST.TransactionResp;
import com.sis.clightapp.model.RefundsData.RefundResponse;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
//qq

/**
 * By
 * khuwajahassan15@gmail.com
 * 17/09/2020
 */
public class AdminFragment1 extends AdminBaseFragment {

    AdminFragment1 adminFragment1;
    int INTENT_AUTHENTICATE = 1234;
    int setwidht, setheight;
    private WebSocketClient webSocketClient;
    Button distributebutton, commandeerbutton, confirpaymentbtn;
    ImageView receiveablestextview, sendeablestextview, qRCodeImage;
    ListView receiveableslistview, sendeableslistview;
    AdminReceiveablesListAdapter adminReceiveablesListAdapter;
    AdminSendablesListAdapter adminSendablesListAdapter;

    ProgressDialog dialog, getSalesListProgressDialog, getRefundsListProgressDialog, createInvoiceProgressDialog, confirmInvoicePamentProgressDialog, payOtherProgressDialog, decodePayBolt11ProgressDialog;
    double CurrentRateInBTC;
    ApiPaths fApiPaths;
    Functions2 functions;
    CustomSharedPreferences sharedPreferences = new CustomSharedPreferences();
    Dialog distributeGetPaidDialog, confirmPaymentDialog, commandeerRefundDialog, commandeerRefundDialogstep2;
    String currentTransactionLabel = "";
    String bolt11fromqr = "";
    String distributeDescription = "";
    String commandeerDescription = "";
    //creat scan object
    // private final String gdaxUrl = "ws://98.226.215.246:8095/SendCommands";
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    private IntentIntegrator qrScan;
    //TODO:For Printing Purpose
    private static final int REQUEST_ENABLE_BT = 2;

    BluetoothAdapter mBluetoothAdapter;
    private UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mBluetoothSocket;
    BluetoothDevice mBluetoothDevice;
    int printstat;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private static OutputStream btoutputstream;
    ProgressDialog printingProgressBar, simpleloader;
    Dialog blutoothDevicesDialog;
    //Date Filter
    EditText fromDaterReceivables, toDateReceivables, fromDateSednables, toDateSednables;
    DatePickerDialog picker;
    String fromDateVaue = "";
    String toDateValue = "";
    boolean isInApp = true;
    TextView setTextWithSpan;
    double AMOUNT_BTC = 0;
    double AMOUNT_USD = 0;
    double CONVERSION_RATE = 0;
    double MSATOSHI = 0;
    String getPaidLABEL = "";
    String getRefubdLABEL = "";
    String current_transaction_description = "";

    public AdminFragment1() {
        // Required empty public constructor
    }

    public AdminFragment1 getInstance() {
        if (adminFragment1 == null) {
            adminFragment1 = new AdminFragment1();
        }
        return adminFragment1;
    }

    public void onBackPressed() {
        ask_exit();
    }

    @Override
    public void onDestroy() {
//        handler.removeCallbacks(runnable);
        super.onDestroy();
        try {
            if (mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
        //ExitingFromServerOnDestroy exitingFromServer = new ExitingFromServerOnDestroy(getActivity());
        //exitingFromServer.execute(new String[]{new String("bye")});
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isInApp) {
            getSendeableListFromMerchantServer();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_admin1, container, false);
        setTextWithSpan = view.findViewById(R.id.poweredbyimage);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan,
                getString(R.string.welcome_text),
                getString(R.string.welcome_text_bold),
                boldStyle);


        fromDaterReceivables = view.findViewById(R.id.et_from_date_sale);
        toDateReceivables = view.findViewById(R.id.et_to_date_sale);
        fromDateSednables = view.findViewById(R.id.et_from_date_refund);
        toDateSednables = view.findViewById(R.id.et_to_date_refund);

        distributebutton = (Button) view.findViewById(R.id.distributebutton);
        commandeerbutton = (Button) view.findViewById(R.id.commandeerbutton);
        qrScan = new IntentIntegrator(getActivity());
        qrScan.setOrientationLocked(false);
        String prompt = getResources().getString(R.string.scanqrforbolt11);
        qrScan.setPrompt(prompt);
        printingProgressBar = new ProgressDialog(getContext());
        printingProgressBar.setMessage("Printing...");
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Loading...");
        simpleloader = new ProgressDialog(getContext());
        simpleloader.setCancelable(false);
        simpleloader.setMessage("Loading ...");
        decodePayBolt11ProgressDialog = new ProgressDialog(getContext());
        decodePayBolt11ProgressDialog.setMessage("Loading...");
        getSalesListProgressDialog = new ProgressDialog(getContext());
        getSalesListProgressDialog.setMessage("Loading Sendables");
        createInvoiceProgressDialog = new ProgressDialog(getContext());
        createInvoiceProgressDialog.setMessage("Creating...");
        getRefundsListProgressDialog = new ProgressDialog(getContext());
        getRefundsListProgressDialog.setMessage("Loading Sendables");
        payOtherProgressDialog = new ProgressDialog(getContext());
        payOtherProgressDialog.setMessage("Paying...");
        confirmInvoicePamentProgressDialog = new ProgressDialog(getContext());
        confirmInvoicePamentProgressDialog.setMessage("Confirming Payment");
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        setwidht = width * 45;
        setwidht = setwidht / 100;
        setheight = height / 2;
        receiveableslistview = view.findViewById(R.id.receivablesListview);
        sendeableslistview = view.findViewById(R.id.sednablesListview);
        receiveableslistview.setMinimumWidth(setwidht);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) receiveableslistview.getLayoutParams();
        lp.width = setwidht;
        receiveableslistview.setLayoutParams(lp);
        sharedPreferences = new CustomSharedPreferences();

        ViewGroup.LayoutParams lp2 = (ViewGroup.LayoutParams) sendeableslistview.getLayoutParams();
        lp2.width = setwidht;
        sendeableslistview.setLayoutParams(lp2);
        gdaxUrl = new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());

        getInvoicelist();
        getRefundslist();
        if (CheckNetwork.isInternetAvailable(getContext())) {
            // getcurrentrate();
            SubscrieChannel();
            //getHeartBeat();
        } else {

            // setcurrentrate("Not Found");

        }

     /*   final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                //call function
                if (CheckNetwork.isInternetAvailable(getContext())) {
                    getcurrentrate();
                    //getHeartBeat();
                } else {
                    // setcurrentrate("Not Found");
                }
                ha.postDelayed(this, 180000);
            }
        }, 180000);*/

        distributebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBoxForGetPaidDistribute();

            }
        });
        commandeerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInApp = false;
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
        fromDaterReceivables.setInputType(InputType.TYPE_NULL);
        toDateReceivables.setInputType(InputType.TYPE_NULL);
        fromDateSednables.setInputType(InputType.TYPE_NULL);
        toDateSednables.setInputType(InputType.TYPE_NULL);
        fromDaterReceivables.setOnClickListener(new View.OnClickListener() {
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
                                fromDaterReceivables.setText(date);
                                fromDateSednables.setText("");
                                toDateSednables.setText("");
                                toDateReceivables.setText("");
                                setAdapterFromDateReceivables_Sale(date);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year

                picker.show();

            }
        });

        toDateReceivables.setOnClickListener(new View.OnClickListener() {
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
                                toDateReceivables.setText(date);
                                fromDaterReceivables.setText("");
                                fromDateSednables.setText("");
                                toDateSednables.setText("");
                                setAdapterToDateReceivables_Sale(date);

                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
                picker.show();

            }
        });

        fromDateSednables.setOnClickListener(new View.OnClickListener() {
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
                                fromDateSednables.setText(date);
                                toDateSednables.setText("");
                                fromDaterReceivables.setText("");
                                toDateReceivables.setText("");
                                setAdapterFromDateSednables_Refund(date);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
                picker.show();

            }
        });

        toDateSednables.setOnClickListener(new View.OnClickListener() {
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
                                toDateSednables.setText(date);
                                fromDateSednables.setText("");
                                fromDaterReceivables.setText("");
                                toDateReceivables.setText("");
                                setAdapterToDateSednables_Refund(date);
                                //setAdapterToDateRefund(date);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
                picker.show();

            }
        });


        return view;
    }

    private void setAdapterToDateSednables_Refund(String datex) {


        if (GlobalState.getInstance().getmAdminSendblesListDataSource() != null) {
            ArrayList<Refund> mAdminSendblesListDataSource = GlobalState.getInstance().getmAdminSendblesListDataSource();
            ArrayList<Refund> fromDateSendablesList_Refund = new ArrayList<>();
            for (Refund refund : mAdminSendblesListDataSource) {
                if (refund.getStatus().equals("complete")) {
                    String[] sourceSplit = datex.split("-");
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
                    if (date2.before(date))
//                    long dayDiff=getDayDiffDates(curentTime,paidTime);
//                    if(dayDiff<1)
                    {
                        fromDateSendablesList_Refund.add(refund);
                    }

                } else {

                }
            }
            adminSendablesListAdapter = new AdminSendablesListAdapter(getContext(), fromDateSendablesList_Refund);
            sendeableslistview.setAdapter(adminSendablesListAdapter);
        }

    }

    private void setAdapterFromDateSednables_Refund(String datex) {


        if (GlobalState.getInstance().getmAdminSendblesListDataSource() != null) {
            ArrayList<Refund> mAdminSendblesListDataSource = GlobalState.getInstance().getmAdminSendblesListDataSource();
            ArrayList<Refund> fromDateSendablesList_Refund = new ArrayList<>();
            for (Refund refund : mAdminSendblesListDataSource) {
                if (refund.getStatus().equals("complete")) {
                    String[] sourceSplit = datex.split("-");
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
                        fromDateSendablesList_Refund.add(refund);
                    }

                } else {

                }
            }
            adminSendablesListAdapter = new AdminSendablesListAdapter(getContext(), fromDateSendablesList_Refund);
            sendeableslistview.setAdapter(adminSendablesListAdapter);
        }

    }

    private void setAdapterToDateReceivables_Sale(String datex) {

        if (GlobalState.getInstance().getmAdminReceiveablesListDataSource() != null) {
            ArrayList<Sale> mAdminReceiveablesListDataSource = GlobalState.getInstance().getmAdminReceiveablesListDataSource();
            ArrayList<Sale> mFilteredReceiveablesList_Sale = new ArrayList<>();
            for (Sale sale : mAdminReceiveablesListDataSource) {
                if (sale.getPayment_preimage() != null) {

                    String[] sourceSplit = datex.split("-");
                    int month = Integer.parseInt(sourceSplit[0]);
                    int day = Integer.parseInt(sourceSplit[1]);
                    int year = Integer.parseInt(sourceSplit[2]);
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.set(year, month - 1, day);
                    Date date = calendar.getTime();
                    long curentTime = date.getTime();
                    long paidTime = sale.getPaid_at() * 1000;
                    Date date2 = new Date(paidTime);
                    if (date2.before(date)) {
                        mFilteredReceiveablesList_Sale.add(sale);
                    } else {
                        //TODO:If greater
                    }


                }
            }

            adminReceiveablesListAdapter = new AdminReceiveablesListAdapter(getContext(), mFilteredReceiveablesList_Sale);
            receiveableslistview.setAdapter(adminReceiveablesListAdapter);
        }


    }

    private void setAdapterFromDateReceivables_Sale(String datex) {
        if (GlobalState.getInstance().getmAdminReceiveablesListDataSource() != null) {
            ArrayList<Sale> mAdminReceiveablesListDataSource = GlobalState.getInstance().getmAdminReceiveablesListDataSource();
            ArrayList<Sale> mFilteredReceiveablesList_Sale = new ArrayList<>();
            for (Sale sale : mAdminReceiveablesListDataSource) {
                if (sale.getPayment_preimage() != null) {

                    String[] sourceSplit = datex.split("-");
                    int month = Integer.parseInt(sourceSplit[0]);
                    int day = Integer.parseInt(sourceSplit[1]);
                    int year = Integer.parseInt(sourceSplit[2]);
                    GregorianCalendar calendar = new GregorianCalendar();
                    calendar.set(year, month - 1, day);
                    Date date = calendar.getTime();
                    long curentTime = date.getTime();
                    long paidTime = sale.getPaid_at() * 1000;
                    Date date2 = new Date(paidTime);
                    int d1 = date.getDay();
                    int d2 = date2.getDay();
                    if (date2.after(date) || date.getDay() == date2.getDay()) {
                        mFilteredReceiveablesList_Sale.add(sale);
                    } else {
                        //TODO:If greater
                    }


                }
            }

            adminReceiveablesListAdapter = new AdminReceiveablesListAdapter(getContext(), mFilteredReceiveablesList_Sale);
            receiveableslistview.setAdapter(adminReceiveablesListAdapter);
        }
    }

    private void getSendeableListFromMerchantServer() {
        sendpayslist(); //ByNaeemTest
       /* GetRefundsListFromMerchantServer getRefundsListFromMerchantServer = new GetRefundsListFromMerchantServer(getActivity());
        if (Build.VERSION.SDK_INT >= 11) {
            getRefundsListFromMerchantServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String("listsendpays")});
            getRefundsListProgressDialog.show();
            getRefundsListProgressDialog.setCancelable(false);
            getRefundsListProgressDialog.setCanceledOnTouchOutside(false);
        } else {
            getRefundsListFromMerchantServer.execute(new String[]{new String("listsendpays")});
            getRefundsListProgressDialog.show();
            getRefundsListProgressDialog.setCancelable(false);
            getRefundsListProgressDialog.setCanceledOnTouchOutside(false);
        }*/
    }

    private void getReceiveablesListFromMerchantServer() {
        getInvoicelist();
//        GetSalesListFromMerchantServer getSalesListFromMerchantServer = new GetSalesListFromMerchantServer(getActivity());
//        if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
//            getSalesListFromMerchantServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String("listinvoices")});
//            getSalesListProgressDialog.show();
//            getSalesListProgressDialog.setCancelable(false);
//            getSalesListProgressDialog.setCanceledOnTouchOutside(false);
//        } else {
//            getSalesListFromMerchantServer.execute(new String[]{new String("listinvoices")});
//            getSalesListProgressDialog.show();
//            getSalesListProgressDialog.setCancelable(false);
//            getSalesListProgressDialog.setCanceledOnTouchOutside(false);
//        }
    }

    private void parseJSONForSales(String jsonString) {
        String temre = jsonString;
        Gson gson = new Gson();
//        Type type = new TypeToken<ArrayList<Sale>>() {
//        }.getType();
        ArrayList<Sale> saleArrayList = new ArrayList<>();
        InvoicesResponse invoicesResponse;

        try {
            invoicesResponse = gson.fromJson(jsonString, InvoicesResponse.class);
            saleArrayList = invoicesResponse.getInvoiceArrayList();
        } catch (Exception e) {
            Log.e("GsonnParsingError_Sale", e.getMessage());
        }

//        Collections.reverse(saleArrayList);
        GlobalState.getInstance().setmAdminReceiveablesListDataSource(saleArrayList);
//        for (Sale sales : saleArrayList){
//            Log.i("Sales Details", sales.getLabel()+"-"+sales.getBolt11() + "-"+sales.getStatus() + "-" + sales.getAmount_msat() + "-" + sales.getMsatoshi()+".......");
//        }
        setReceiveablesAdapter();

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
            Log.e("GsonnParsingError_Rfnd", e.getMessage());
        }

//        Collections.reverse(refundArrayList);
        GlobalState.getInstance().setmAdminSendblesListDataSource(refundArrayList);
//        for (Refund refund : refundArrayList) {
//            // Log.i("Refund Details", sales.getBolt11() + "-"+sales.getStatus() + "-" + sales.getPreimage() + "-" + sales.getAmount_sent_msat());
//        }
        setSendablesableAdapter();

    }

    private void setReceiveablesAdapter() {
        if (GlobalState.getInstance().getmAdminReceiveablesListDataSource() != null) {
            ArrayList<Sale> mAdminReceiveablesListDataSource = GlobalState.getInstance().getmAdminReceiveablesListDataSource();
            ArrayList<Sale> mTodayReceiveablesList_Sale = new ArrayList<>();
            ArrayList<Sale> mTotalReceiveablesList_Sale = new ArrayList<>();
            ArrayList<Sale> mTotalPaidReceiveablesList_Sale = new ArrayList<>();
            ArrayList<Sale> mTotalUnPaidReceiveablesListt_Sale = new ArrayList<>();
            for (Sale sale : mAdminReceiveablesListDataSource) {
                // sale.getPayment_preimage() != null
                if (sale.getLabel() != null) {
                    mTotalPaidReceiveablesList_Sale.add(sale);
                    long curentTime = new Date().getTime();
                    long paidTime = sale.getPaid_at() * 1000;

                    Date currentDate = new Date(curentTime);
                    Date refundDate = new Date(paidTime);
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(currentDate);
                    cal2.setTime(refundDate);
                    boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

                    if (sameDay) {
                        mTodayReceiveablesList_Sale.add(sale);
                    } else {
                        //TODO:If greater
                    }


                } else {
                    mTotalUnPaidReceiveablesListt_Sale.add(sale);
                }
            }
            mTotalReceiveablesList_Sale = mAdminReceiveablesListDataSource;
            GlobalState.getInstance().setmTodayReceiveablesList_Sale(mTodayReceiveablesList_Sale);
            GlobalState.getInstance().setmTotalReceiveablesList_Sale(mTotalReceiveablesList_Sale);
            GlobalState.getInstance().setmTotalPaidReceiveablesList_Sale(mTotalPaidReceiveablesList_Sale);
            GlobalState.getInstance().setmTotalUnPaidReceiveablesListt_Sale(mTotalUnPaidReceiveablesListt_Sale);
            adminReceiveablesListAdapter = new AdminReceiveablesListAdapter(getContext(), mTodayReceiveablesList_Sale);
            receiveableslistview.setAdapter(adminReceiveablesListAdapter);
        }
    }

    private void setSendablesableAdapter() {

        if (GlobalState.getInstance().getmAdminSendblesListDataSource() != null) {

            ArrayList<Refund> mAdminSaleablesListDataSource = GlobalState.getInstance().getmAdminSendblesListDataSource();
            ArrayList<Refund> mTodaySendeableList_Refund = new ArrayList<>();
            ArrayList<Refund> mTotalSendeableList_Refund = new ArrayList<>();
            ArrayList<Refund> mTotalCompleteSendeableList_Refund = new ArrayList<>();
            ArrayList<Refund> mTotalUnCompleteSendeableList_Refund = new ArrayList<>();


            for (Refund refund : mAdminSaleablesListDataSource) {
                if (refund.getStatus().equals("complete")) {
                    mTotalCompleteSendeableList_Refund.add(refund);
                    long currentTime = new Date().getTime();
                    long refundtime = refund.getCreated_at() * 1000;
                    Date currentDate = new Date(currentTime);
                    Date refundDate = new Date(refundtime);
                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(currentDate);
                    cal2.setTime(refundDate);
                    boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
                    if (sameDay) {
                        // under +/- 24hour , do the work

                        mTodaySendeableList_Refund.add(refund);

                    } else {
                        // over 24 hour
                    }

                } else {

                    mTotalUnCompleteSendeableList_Refund.add(refund);
                }

            }
            mTotalSendeableList_Refund = mAdminSaleablesListDataSource;
            GlobalState.getInstance().setmTodaySendeableList_Refund(mTodaySendeableList_Refund);
            GlobalState.getInstance().setmTotalSendeableList_Refund(mTotalSendeableList_Refund);
            GlobalState.getInstance().setmTotalCompleteSendeableList_Refund(mTotalCompleteSendeableList_Refund);
            GlobalState.getInstance().setmTotalUnCompleteSendeableList_Refund(mTotalUnCompleteSendeableList_Refund);

            adminSendablesListAdapter = new AdminSendablesListAdapter(getContext(), mTodaySendeableList_Refund);
            sendeableslistview.setAdapter(adminSendablesListAdapter);


        }
    }

    private void setcurrentrate(String rate) {
        //TODO:what need
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
//                     luqman commen btc rate       GlobalState.getInstance().setCurrentSpecificRateData(cSRDtemp);
                            GlobalState.getInstance().setCurrentAllRate(response.body());
                            // sharedPreferences.setCurrentSpecificRateData(cSRDtemp,"CurrentSpecificRateData",getContext());
//                    luqman comment        setcurrentrate(String.valueOf(cSRDtemp.getRateinbitcoin()));
//                            Tax tax=new Tax();
//                            tax.setTaxInUSD(AppConstants.TAXVALUEINDOLLAR);
//                            double taxBtc=1/response.body().getUSD().get15m();
//                            taxBtc=taxBtc*AppConstants.TAXVALUEINDOLLAR;


                            // Log.d("CurrentRate",String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
                            Log.d("CurrentRate2", String.valueOf(cSRDtemp.getRateinbitcoin()));

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

    private void dialogBoxForGetPaidDistribute() {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        distributeGetPaidDialog = new Dialog(getContext());
        distributeGetPaidDialog.setContentView(R.layout.dialoglayoutgetpaiddistribute);

        Objects.requireNonNull(distributeGetPaidDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        distributeGetPaidDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        distributeGetPaidDialog.setCancelable(false);


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
                //TODO:For just Tes Purpose
//                Invoice temPinvoice=new Invoice();
//                temPinvoice.setMsatoshi(200000000);
//                temPinvoice.setPaid_at(1597732924);
//                temPinvoice.setStatus("Unpaid");
//                temPinvoice.setPayment_preimage("881d6ee425fcb0b670191b140742364d35a4fc51a831197709756886aed8e7d7");
                //               dialogBoxForConfirmPaymentInvoice(temPinvoice);

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
                    //double priceInBTC = 1 / GlobalState.getInstance().getCurrentAllRate().getUSD().getLast();
                    double priceInBTC = 1 / GlobalState.getInstance().getChannel_btcResponseData().getPrice();
                    priceInBTC = priceInBTC * Double.parseDouble(msatoshi);
                    AMOUNT_BTC = priceInBTC;
                    double amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi;
                    MSATOSHI = amountInMsatoshi;
                    amountInMsatoshi = amountInMsatoshi * AppConstants.satoshiToMSathosi;

                    CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC;
                    //msatoshi=excatFigure(amountInMsatoshi);
                    NumberFormat formatter = new DecimalFormat("#0");
                    String rMSatoshi = formatter.format(amountInMsatoshi);
                    distributeDescription = descrption;
                    CreateInvoice(rMSatoshi, label, descrption);
                    // creatInvoice(rMSatoshi, label, descrption);
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
//                confirmInvoicePamentProgressDialog.setCancelable(false);
//                confirmInvoicePamentProgressDialog.setCanceledOnTouchOutside(false);
            }
        });
        distributeGetPaidDialog.show();
    }

    private CreateInvoice parseJSONForCreatInvocie(String jsonString) {
        Log.e("CreatInvoice", jsonString);
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
        /*{
   "invoices": [
      {
         "label": "sale1647270147",
         "bolt11": "lnbc25605930p1p3z7kglpp52q2vwtf82jgnc5jm3yqqpf6jqp36y0dsv085rg8y0qwztecdhgvqdq8w3jhgucxqyjw5qcqpjsp5nn04jmfz384ryv8x4t9y3mtqu37kycvnkd6ys6xx9x7h7u74q7dq9qyyssqn0glpc608mc38nsak9ctt6sdzedxftnrjt7km8utxtl6zml07prsn605vdyrreqmdjk4xcm79clh7jqmj6jm8qfvh5kewyxp7anhycqpmvjh5r",
         "payment_hash": "5014c72d2754913c525b890000a7520063a23db063cf41a0e4781c25e70dba18",
         "msatoshi": 2560593,
         "amount_msat": "2560593msat",
         "status": "unpaid",
         "description": "tets",
         "expires_at": 1647874975
      }
   ]
}*/
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
                dialogBoxForConfirmPaymentInvoice(invoice);
                confirmInvoicePamentProgressDialog.dismiss();
                simpleloader.dismiss();
            } else {
                simpleloader.dismiss();
                distributeGetPaidDialog.dismiss();
                confirmInvoicePamentProgressDialog.dismiss();
                new AlertDialog.Builder(getContext())
                        .setMessage("Payment Not Recieved")
                        .setPositiveButton("Retry", null)
                        .show();

            }

        } else {
            simpleloader.dismiss();
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
        distributeGetPaidDialog = new Dialog(getContext());
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
            if (invoice.getStatus().equals("paid")) {
                InvoiceForPrint invoiceForPrint = new InvoiceForPrint();

                //before // invoiceForPrint.setMsatoshi(invoice.getMsatoshi()/1000);
                //After
                invoiceForPrint.setMsatoshi(invoice.getMsatoshi());
                invoiceForPrint.setPayment_preimage(invoice.getPayment_preimage());
                invoiceForPrint.setPaid_at(invoice.getPaid_at());
                invoiceForPrint.setPurchasedItems(distributeDescription);
                invoiceForPrint.setDesscription(distributeDescription);
                invoiceForPrint.setMode("distributeGetPaid");
                GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
                amount.setVisibility(View.VISIBLE);
                payment_preImage.setVisibility(View.VISIBLE);
                paid_at.setVisibility(View.VISIBLE);
                purchased_Items.setVisibility(View.VISIBLE);
                //    tax.setVisibility(View.VISIBLE);
                printInvoice.setVisibility(View.VISIBLE);
                String amountInBtc = excatFigure(mSatoshoToBtc(invoice.getMsatoshi()));
                double amounttempusd = round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)) + "BTC\n$" + precision.format(round(amounttempusd, 2)) + "USD");

                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                paid_at.setText(getDateFromUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                purchased_Items.setText(distributeDescription);

                // tax.setText(excatFigure(round(getTaxOfBTC(mSatoshoToBtc(invoice.getMsatoshi())),9))+" BTC\n"+round(getTaxOfUSD(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi()))),2)+"$");

            } else {
                InvoiceForPrint invoiceForPrint = new InvoiceForPrint();
                invoiceForPrint.setMsatoshi(0.0);
                invoiceForPrint.setPayment_preimage("N/A");
                invoiceForPrint.setPaid_at(0000);
                invoiceForPrint.setMode("distributeGetPaid");
                GlobalState.getInstance().setInvoiceForPrint(invoiceForPrint);
                amount.setVisibility(View.VISIBLE);
                payment_preImage.setVisibility(View.VISIBLE);
                paid_at.setVisibility(View.VISIBLE);
                purchased_Items.setVisibility(View.VISIBLE);
                //    tax.setVisibility(View.VISIBLE);
                printInvoice.setVisibility(View.VISIBLE);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)) + "BTC\n$" + precision.format(round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2)) + "USD");
                paid_at.setText(getDateFromUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                // payment_preImage.setImageBitmap(getBitMapFromHex(invoice.getPayment_preimage()));
                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                ArrayList<Items> mselectedDatasourceForPAyment = GlobalState.getInstance().getmSeletedForPayDataSourceCheckOutInventory();

                purchased_Items.setText("N/A");

                //  tax.setText("N/A");
                //TODO: if payment not recived
            }
        }
        printInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InvoiceForPrint invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();
                if (invoice.getStatus().equals("paid")) {
                    getSendeableListFromMerchantServer();
                    getReceiveablesListFromMerchantServer();
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
                    getSendeableListFromMerchantServer();
                    getReceiveablesListFromMerchantServer();
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
                getSendeableListFromMerchantServer();
                getReceiveablesListFromMerchantServer();
                distributeGetPaidDialog.dismiss();
            }
        });
        distributeGetPaidDialog.show();
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
//        JSONArray jsonArr = null;
//        try {
//            jsonArr = new JSONArray(jsonString);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        JSONObject jsonObj = null;
//        if (jsonArr != null) {
//            try {
//                jsonObj = jsonArr.getJSONObject(0);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
        //{"error":true,"message":"Invalid bolt11: Bad bech32 string"}
        if (jsonString.equals("")) {

        } else {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<DecodePayBolt11>() {
                }.getType();
                decodePayBolt11 = gson.fromJson(jsonString.toString(), type);
                GlobalState.getInstance().setCurrentDecodePayBolt11(decodePayBolt11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
        return decodePayBolt11;

    }

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
        final TextView tv_title = commandeerRefundDialog.findViewById(R.id.tv_title);
        tv_title.setText("COMMANDEER");
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


                qrScan.forSupportFragment(AdminFragment1.this).initiateScan();

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
        final TextView tv_title = commandeerRefundDialogstep2.findViewById(R.id.tv_title);
        tv_title.setText("COMMANDEER");
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
        PayRequestToOther(bolt11value, rMSatoshi, labelval);
//        PayRequestToOtherFromServer payRequestToOtherFromServer = new PayRequestToOtherFromServer(getActivity());
//        payRequestToOtherFromServer.execute(new String[]{new String(bolt11value), new String(rMSatoshi), new String(labelval)});
//        payOtherProgressDialog.show();
//        payOtherProgressDialog.setCancelable(false);
//        payOtherProgressDialog.setCanceledOnTouchOutside(false);
    }

    protected void paytoothersResponse(String result) {
        // this method is called back on the UI thread, so it's safe to
        //  make UI calls (like dismissing a dialog) here
        //  parent.dismissDialog(LOADING_DIALOG);

        String response = result;
          /*  resp,status,rpc-cmd,cli-node,[ {
                "destination": "02dc8590dd675b5bf89c6bdf9eeed767290b3d6056465e5b013756f65616d3d372",
                        "payment_hash": "8c8fb25e7a1851944f2f10974549fa0845fbd480dde33569e4382a99a2ccd59d",
                        "created_at": 1596655318.504,
                        "parts": 1,
                        "msatoshi": 965000,
                        "amount_msat": "965000msat",
                        "msatoshi_sent": 965000,
                        "amount_sent_msat": "965000msat",
                        "payment_preimage": "881d6ee425fcb0b670191b140742364d35a4fc51a831197709756886aed8e7d7",
                        "status": "complete"
            }
 ]*/
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
//                showToast("3");
//                Pay payresponse2=new Pay();
//                payresponse2.setStatus("complete");
//                payresponse2.setMsatoshi(2000000000);
//                payresponse2.setPayment_preimage("jahsjhghsbzbnzbnzbzbnzbnbnzbnbnsanbbjjhjjhkjha");
//                payresponse2.setCreated_at(1596655318);
//                payresponse2.setPayment_hash("hahagghagagjkgjkagkagaghaghghaghkaghkagmnmhk");
//                payresponse2.setDestination("hahagghagagjkgjkagkagaghaghghaghkaghkagmnmhk");
//                showCofirmationDialog(payresponse2);
            Pay payresponse22 = new Pay();
            payresponse22.setStatus("Not complete");
            showCofirmationDialog(payresponse22);
            payOtherProgressDialog.dismiss();
        }
    }

    private void showCofirmationDialog(final Pay payresponse) {

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
                    getReceiveablesListFromMerchantServer();
                    getSendeableListFromMerchantServer();
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
                    getReceiveablesListFromMerchantServer();
                    getSendeableListFromMerchantServer();
                    commandeerRefundDialogstep2.dismiss();
                }
            }
        });

        // progressBar = dialog.findViewById(R.id.progress_bar);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getReceiveablesListFromMerchantServer();
                getSendeableListFromMerchantServer();
                commandeerRefundDialogstep2.dismiss();
            }
        });
        commandeerRefundDialogstep2.show();


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
                        //do something you want when pass the security
                        // Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_SHORT).show();
                        dialogBoxForRefundCommandeer();
                    }
                }

                break;


            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
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
                        showToast("Result Not Found");

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

    //Printing Stuff
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
                                        btoutputstream.write("\n\n".getBytes());
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

    /*Print END*/
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
                goAlertDialogwithOneBTnDialog.dismiss();
                ifPostSuccefully();
              /*PostRequestServer postRequestServer = new PostRequestServer(getActivity());
                if (Build.VERSION.SDK_INT >= 11) {
                    postRequestServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{new String("bye")});
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                } else {
                    postRequestServer.execute(new String[]{new String("bye")});
                    dialog.show();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
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

    public void ifPostSuccefully() {
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);

    }

    private String getDateInCorrectFormat(int year, int monthOfYear, int dayOfMonth) {
        String date = "";
        String formatedMonth = "";
        String formatedDay = "";
        if (monthOfYear < 9) {
            formatedMonth = "0" + (monthOfYear + 1);
        } else {
            formatedMonth = String.valueOf(monthOfYear + 1);
        }
        if (dayOfMonth < 10) {
            formatedDay = "0" + dayOfMonth;
        } else {
            formatedDay = String.valueOf(dayOfMonth);
        }
        date = formatedMonth + "-" + formatedDay + "-" + year;
        return date;
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

        String msatoshi = excatFigure(MSATOSHI);
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
                                                    //showToast(String.valueOf(channel_btcResponseData.getPrice()));
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

    public void getInvoicelist() {
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

    public void getRefundslist() {
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

    public void sendpayslist() {
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
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
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