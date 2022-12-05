package com.sis.clightapp.fragments.admin;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.UrlConstants;
import com.sis.clightapp.activity.HomeActivity;
import com.sis.clightapp.adapter.AdminReceiveablesListAdapter;
import com.sis.clightapp.adapter.AdminSendablesListAdapter;
import com.sis.clightapp.fragments.shared.ExitDialogFragment;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.CreateInvoice;
import com.sis.clightapp.model.GsonModel.DecodePayBolt11;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.model.GsonModel.InvoiceForPrint;
import com.sis.clightapp.model.GsonModel.Pay;
import com.sis.clightapp.model.GsonModel.Refund;
import com.sis.clightapp.model.GsonModel.Sale;
import com.sis.clightapp.model.Invoices.InvoicesResponse;
import com.sis.clightapp.model.REST.TransactionResp;
import com.sis.clightapp.model.RefundsData.RefundResponse;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

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
    ImageView qRCodeImage;
    ListView receiveableslistview, sendeableslistview;
    AdminReceiveablesListAdapter adminReceiveablesListAdapter;
    AdminSendablesListAdapter adminSendablesListAdapter;

    ProgressDialog dialog, getSalesListProgressDialog, getRefundsListProgressDialog, createInvoiceProgressDialog, confirmInvoicePamentProgressDialog, payOtherProgressDialog, decodePayBolt11ProgressDialog;
    CustomSharedPreferences sharedPreferences = new CustomSharedPreferences();
    Dialog distributeGetPaidDialog, confirmPaymentDialog, commandeerRefundDialog, commandeerRefundDialogstep2;
    String currentTransactionLabel = "";
    String bolt11fromqr = "";
    String distributeDescription = "";
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    private IntentIntegrator qrScan;

    BluetoothAdapter mBluetoothAdapter;
    ProgressDialog printingProgressBar, simpleloader;
    EditText fromDaterReceivables, toDateReceivables, fromDateSednables, toDateSednables;
    DatePickerDialog picker;
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
    }

    public AdminFragment1 getInstance() {
        if (adminFragment1 == null) {
            adminFragment1 = new AdminFragment1();
        }
        return adminFragment1;
    }

    public void onBackPressed() {
        new ExitDialogFragment().show(getChildFragmentManager(), null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().stopService(new Intent(getContext(), MyLogOutService.class));
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

        distributebutton = view.findViewById(R.id.distributebutton);
        commandeerbutton = view.findViewById(R.id.commandeerbutton);
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
        ViewGroup.LayoutParams lp = receiveableslistview.getLayoutParams();
        lp.width = setwidht;
        receiveableslistview.setLayoutParams(lp);
        sharedPreferences = new CustomSharedPreferences();

        ViewGroup.LayoutParams lp2 = sendeableslistview.getLayoutParams();
        lp2.width = setwidht;
        sendeableslistview.setLayoutParams(lp2);
        gdaxUrl = new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());

        getInvoicelist();
        getRefundslist();
        if (CheckNetwork.isInternetAvailable(requireContext())) {
            subscrieChannel();
        }
        distributebutton.setOnClickListener(view1 -> dialogBoxForGetPaidDistribute());
        commandeerbutton.setOnClickListener(view12 -> {
            isInApp = false;
            KeyguardManager km = (KeyguardManager) requireActivity().getSystemService(KEYGUARD_SERVICE);
            if (km.isKeyguardSecure()) {
                Intent authIntent = km.createConfirmDeviceCredentialIntent("Authorize Payment", "");
                startActivityForResult(authIntent, INTENT_AUTHENTICATE);
            } else {
                dialogBoxForRefundCommandeer();
            }
        });
        fromDaterReceivables.setInputType(InputType.TYPE_NULL);
        toDateReceivables.setInputType(InputType.TYPE_NULL);
        fromDateSednables.setInputType(InputType.TYPE_NULL);
        toDateSednables.setInputType(InputType.TYPE_NULL);
        fromDaterReceivables.setOnClickListener(view13 -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(getContext(),
                    (view131, year1, monthOfYear, dayOfMonth) -> {
                        String date = getDateInCorrectFormat(year1, monthOfYear, dayOfMonth);
                        fromDaterReceivables.setText(date);
                        fromDateSednables.setText("");
                        toDateSednables.setText("");
                        toDateReceivables.setText("");
                        setAdapterFromDateReceivables_Sale(date);
                    }, year, month, day);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year

            picker.show();

        });

        toDateReceivables.setOnClickListener(view14 -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);

            picker = new DatePickerDialog(getContext(),
                    (view141, year12, monthOfYear, dayOfMonth) -> {
                        String date = getDateInCorrectFormat(year12, monthOfYear, dayOfMonth);
                        toDateReceivables.setText(date);
                        fromDaterReceivables.setText("");
                        fromDateSednables.setText("");
                        toDateSednables.setText("");
                        setAdapterToDateReceivables_Sale(date);

                    }, year, month, day);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
            picker.show();

        });

        fromDateSednables.setOnClickListener(view15 -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(getContext(),
                    (view151, year13, monthOfYear, dayOfMonth) -> {
                        String date = getDateInCorrectFormat(year13, monthOfYear, dayOfMonth);
                        fromDateSednables.setText(date);
                        toDateSednables.setText("");
                        fromDaterReceivables.setText("");
                        toDateReceivables.setText("");
                        setAdapterFromDateSednables_Refund(date);
                    }, year, month, day);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());// TODO: used to hide future date,month and year
            picker.show();

        });

        toDateSednables.setOnClickListener(view16 -> {


            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog


            picker = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view16, int year, int monthOfYear, int dayOfMonth) {

                            String date = getDateInCorrectFormat(year, monthOfYear, dayOfMonth);
                            toDateSednables.setText(date);
                            fromDateSednables.setText("");
                            fromDaterReceivables.setText("");
                            toDateReceivables.setText("");
                            setAdapterToDateSednables_Refund(date);
                            //setAdapterToDateRefund(date);
                        }
                    }, year, month, day);
            picker.getDatePicker().setMaxDate(System.currentTimeMillis());
            picker.show();

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
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                    cal.set(year, month - 1, day);
                    Date date = cal.getTime();
                    long paidTime = refund.getCreated_at() * 1000;
                    Date date2 = new Date(paidTime);
                    if (date2.before(date)) {
                        fromDateSendablesList_Refund.add(refund);
                    }

                }
            }
            adminSendablesListAdapter = new AdminSendablesListAdapter(requireContext(), fromDateSendablesList_Refund);
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
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("CST"));
                    cal.set(year, month - 1, day);
                    Date date = cal.getTime();
                    long paidTime = refund.getCreated_at() * 1000;
                    Date date2 = new Date(paidTime);

                    if (date2.after(date) || date.getDay() == date2.getDay()) {
                        fromDateSendablesList_Refund.add(refund);
                    }

                }
            }
            adminSendablesListAdapter = new AdminSendablesListAdapter(requireContext(), fromDateSendablesList_Refund);
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
                    long paidTime = sale.getPaid_at() * 1000;
                    Date date2 = new Date(paidTime);
                    if (date2.before(date)) {
                        mFilteredReceiveablesList_Sale.add(sale);
                    }
                }
            }

            adminReceiveablesListAdapter = new AdminReceiveablesListAdapter(requireContext(), mFilteredReceiveablesList_Sale);
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
                    long paidTime = sale.getPaid_at() * 1000;
                    Date date2 = new Date(paidTime);
                    if (date2.after(date) || date.getDay() == date2.getDay()) {
                        mFilteredReceiveablesList_Sale.add(sale);
                    }
                }
            }

            adminReceiveablesListAdapter = new AdminReceiveablesListAdapter(requireContext(), mFilteredReceiveablesList_Sale);
            receiveableslistview.setAdapter(adminReceiveablesListAdapter);
        }
    }

    private void getSendeableListFromMerchantServer() {
        sendpayslist();
    }

    private void getReceiveablesListFromMerchantServer() {
        getInvoicelist();
    }

    private void parseJSONForSales(String jsonString) {
        Gson gson = new Gson();
        ArrayList<Sale> saleArrayList = new ArrayList<>();
        InvoicesResponse invoicesResponse;

        try {
            invoicesResponse = gson.fromJson(jsonString, InvoicesResponse.class);
            saleArrayList = invoicesResponse.getInvoiceArrayList();
        } catch (Exception e) {
            Log.e("GsonnParsingError_Sale", Objects.requireNonNull(e.getMessage()));
        }

        GlobalState.getInstance().setmAdminReceiveablesListDataSource(saleArrayList);
        setReceiveablesAdapter();
    }

    private void parseJSONForRefunds(String jsonString) {
        Gson gson = new Gson();
        ArrayList<Refund> refundArrayList = new ArrayList<>();
        RefundResponse refundResponse;
        try {
            refundResponse = gson.fromJson(jsonString, RefundResponse.class);
            refundArrayList = refundResponse.getRefundArrayList();
        } catch (Exception e) {
            Log.e("GsonParsingError_Refund", Objects.requireNonNull(e.getMessage()));
        }

        GlobalState.getInstance().setmAdminSendblesListDataSource(refundArrayList);
        setSendablesableAdapter();

    }

    private void setReceiveablesAdapter() {
        if (GlobalState.getInstance().getmAdminReceiveablesListDataSource() != null) {
            ArrayList<Sale> mAdminReceiveablesListDataSource = GlobalState.getInstance().getmAdminReceiveablesListDataSource();
            ArrayList<Sale> mTodayReceiveablesList_Sale = new ArrayList<>();
            ArrayList<Sale> mTotalReceiveablesList_Sale;
            ArrayList<Sale> mTotalPaidReceiveablesList_Sale = new ArrayList<>();
            ArrayList<Sale> mTotalUnPaidReceiveablesListt_Sale = new ArrayList<>();
            for (Sale sale : mAdminReceiveablesListDataSource) {
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
            adminReceiveablesListAdapter = new AdminReceiveablesListAdapter(requireContext(), mTodayReceiveablesList_Sale);
            receiveableslistview.setAdapter(adminReceiveablesListAdapter);
        }
    }

    private void setSendablesableAdapter() {

        if (GlobalState.getInstance().getmAdminSendblesListDataSource() != null) {

            ArrayList<Refund> mAdminSaleablesListDataSource = GlobalState.getInstance().getmAdminSendblesListDataSource();
            ArrayList<Refund> mTodaySendeableList_Refund = new ArrayList<>();
            ArrayList<Refund> mTotalSendeableList_Refund;
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
                        mTodaySendeableList_Refund.add(refund);

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

            adminSendablesListAdapter = new AdminSendablesListAdapter(requireContext(), mTodaySendeableList_Refund);
            sendeableslistview.setAdapter(adminSendablesListAdapter);


        }
    }


    @SuppressLint("SetTextI18n")
    private void dialogBoxForGetPaidDistribute() {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        distributeGetPaidDialog = new Dialog(getContext());
        distributeGetPaidDialog.setContentView(R.layout.dialoglayoutgetpaiddistribute);

        Objects.requireNonNull(distributeGetPaidDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        distributeGetPaidDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
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
        ivBack.setOnClickListener(v -> distributeGetPaidDialog.dismiss());
        btnCreatInvoice.setOnClickListener(v -> {

            String msatoshi = et_msatoshi.getText().toString();
            String label = et_label.getText().toString();
            String descrption = et_description.getText().toString();
            if (msatoshi.isEmpty()) {
                showToast("Amount" + getString(R.string.empty));
                return;
            }
            if (label.isEmpty()) {
                showToast("Label" + getString(R.string.empty));
                return;
            }
            if (descrption.isEmpty()) {
                showToast("Description" + getString(R.string.empty));
                return;
            }
            currentTransactionLabel = label;
            AMOUNT_USD = Double.parseDouble(msatoshi);
            double priceInBTC = 1 / GlobalState.getInstance().getChannel_btcResponseData().getPrice();
            priceInBTC = priceInBTC * Double.parseDouble(msatoshi);
            AMOUNT_BTC = priceInBTC;
            double amountInMsatoshi = priceInBTC * AppConstants.btcToSathosi;
            MSATOSHI = amountInMsatoshi;
            amountInMsatoshi = amountInMsatoshi * AppConstants.satoshiToMSathosi;

            CONVERSION_RATE = AMOUNT_USD / AMOUNT_BTC;
            NumberFormat formatter = new DecimalFormat("#0");
            String rMSatoshi = formatter.format(amountInMsatoshi);
            distributeDescription = descrption;
            CreateInvoice(rMSatoshi, label, descrption);

        });


        confirpaymentbtn.setOnClickListener(view -> {
            confirmPayment(currentTransactionLabel);
        });
        distributeGetPaidDialog.show();
    }

    private CreateInvoice parseJSONForCreatInvocie(String jsonString) {
        Log.e("CreateInvoice", jsonString);
        Gson gson = new Gson();
        CreateInvoice createInvoice = gson.fromJson(jsonString, CreateInvoice.class);
        GlobalState.getInstance().setCreateInvoice(createInvoice);
        showToast(createInvoice.getBolt11());
        if (createInvoice.getBolt11() != null) {

            String temHax = createInvoice.getBolt11();
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

        return createInvoice;
    }

    private Invoice parseJSONForConfirmPayment(String jsonString) {
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
        distributeGetPaidDialog.setCancelable(false);
        //init dialog views
        final ImageView ivBack = distributeGetPaidDialog.findViewById(R.id.iv_back_invoice);
        final TextView amount = distributeGetPaidDialog.findViewById(R.id.et_amount);
        final ImageView payment_preImage = distributeGetPaidDialog.findViewById(R.id.et_preimage);
        final TextView paid_at = distributeGetPaidDialog.findViewById(R.id.et_paidat);
        final TextView purchased_Items = distributeGetPaidDialog.findViewById(R.id.et_perchaseditems);
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
                printInvoice.setVisibility(View.VISIBLE);
                double amounttempusd = round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(new StringBuilder()
                        .append(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)))
                        .append("BTC\n$").append(precision.format(round(amounttempusd, 2)))
                        .append("USD").toString());

                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                paid_at.setText(getDateFromUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                purchased_Items.setText(distributeDescription);
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
                printInvoice.setVisibility(View.VISIBLE);
                DecimalFormat precision = new DecimalFormat("0.00");
                amount.setText(new StringBuilder()
                        .append(excatFigure(round((mSatoshoToBtc(invoice.getMsatoshi())), 9)))
                        .append("BTC\n$").append(precision.format(round(getUsdFromBtc(mSatoshoToBtc(invoice.getMsatoshi())), 2))).append("USD").toString());
                paid_at.setText(getDateFromUTCTimestamp(invoice.getPaid_at(), AppConstants.OUTPUT_DATE_FORMATE));
                payment_preImage.setImageBitmap(getBitMapImg(invoice.getPayment_preimage(), 300, 300));
                purchased_Items.setText("N/A");
            }
        }
        printInvoice.setOnClickListener(view -> {
            InvoiceForPrint invoiceForPrint = GlobalState.getInstance().getInvoiceForPrint();
            if (invoice.getStatus().equals("paid")) {
                getSendeableListFromMerchantServer();
                getReceiveablesListFromMerchantServer();
                if (invoiceForPrint != null) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!mBluetoothAdapter.isEnabled()) {
//                        dialogBoxForConnecctingBTPrinter();
                    } else {
//                        if (mBluetoothSocket != null) {
                        Toast.makeText(getContext(), "Already Connected", Toast.LENGTH_LONG).show();
//                            try {
//                                sendData("getPaidDistribute");
//                            } catch (IOException e) {
//                                Log.e("SendDataError", e.toString());
//                                e.printStackTrace();
//                            }
//                        } else {
//                            dialogBoxForConnecctingBTPrinter();
//                        }
                    }
                } else {
                    confirmPaymentDialog.dismiss();
                }
            } else {
                getSendeableListFromMerchantServer();
                getReceiveablesListFromMerchantServer();
                if (invoiceForPrint != null) {
                    confirmPaymentDialog.dismiss();
                } else {
                    confirmPaymentDialog.dismiss();
                }
            }
        });
        ivBack.setOnClickListener(v -> {
            getSendeableListFromMerchantServer();
            getReceiveablesListFromMerchantServer();
            distributeGetPaidDialog.dismiss();
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
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
            }
        }
        return pay;
    }

    private void parseJSONForDecodePayBolt11(String jsonString) {

        DecodePayBolt11 decodePayBolt11;
        if (jsonString.isEmpty()) {
            return;
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<DecodePayBolt11>() {
            }.getType();
            decodePayBolt11 = gson.fromJson(jsonString, type);
            GlobalState.getInstance().setCurrentDecodePayBolt11(decodePayBolt11);
        } catch (Exception e) {
            Log.e("Error", Objects.requireNonNull(e.getMessage()));
        }

    }

    @SuppressLint("SetTextI18n")
    private void dialogBoxForRefundCommandeer() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        commandeerRefundDialog = new Dialog(getContext());
        commandeerRefundDialog.setContentView(R.layout.dialoglayoutrefundcommandeer);


        Objects.requireNonNull(commandeerRefundDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        commandeerRefundDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
        commandeerRefundDialog.setCancelable(false);


        final EditText bolt11 = commandeerRefundDialog.findViewById(R.id.bolt11val);
        final ImageView ivBack = commandeerRefundDialog.findViewById(R.id.iv_back_invoice);
        final TextView tv_title = commandeerRefundDialog.findViewById(R.id.tv_title);
        tv_title.setText("COMMANDEER");
        Button btnNext = commandeerRefundDialog.findViewById(R.id.btn_next);
        Button btnscanQr = commandeerRefundDialog.findViewById(R.id.btn_scanQR);

        ivBack.setOnClickListener(v -> commandeerRefundDialog.dismiss());

        btnNext.setOnClickListener(v -> {
            String bolt11value = bolt11.getText().toString();
            if (bolt11value.isEmpty()) {
                showToast("Bolt11 " + getString(R.string.empty));
            } else {
                commandeerRefundDialog.dismiss();

                bolt11fromqr = bolt11value;
                refundDecodePay(bolt11value);

            }

        });
        btnscanQr.setOnClickListener(view -> IntentIntegrator.forSupportFragment(AdminFragment1.this).initiateScan());


        commandeerRefundDialog.show();

    }

    private void showCofirmationDialog(final Pay payresponse) {

        InvoiceForPrint invoiceForPrint = new InvoiceForPrint();
        invoiceForPrint.setDestination(payresponse.getDestination());
        invoiceForPrint.setMsatoshi(payresponse.getMsatoshi());
        invoiceForPrint.setPayment_preimage(payresponse.getPayment_preimage());
        invoiceForPrint.setCreated_at(payresponse.getCreated_at());
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
        commandeerRefundDialogstep2.setCancelable(false);
        textView.setText("Payment Status:" + payresponse.getStatus());
        if (payresponse.getStatus().equals("complete")) {
            ok.setText("Print");
        }
        ok.setOnClickListener(view -> {
            InvoiceForPrint invoiceForPrint1 = GlobalState.getInstance().getInvoiceForPrint();
            if (payresponse.getStatus().equals("complete")) {
                getReceiveablesListFromMerchantServer();
                getSendeableListFromMerchantServer();
                if (invoiceForPrint1 != null) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!mBluetoothAdapter.isEnabled()) {
//                        dialogBoxForConnecctingBTPrinter();
                    } else {
//                        if (mBluetoothSocket != null) {
                        Toast.makeText(getContext(), "Already Connected", Toast.LENGTH_LONG).show();
//                            try {
//                                sendData("commandeerRefund");
//                            } catch (IOException e) {
//                                e.printStackTrace();
//
//                            }
                        commandeerRefundDialogstep2.dismiss();
//                        } else {
//                            dialogBoxForConnecctingBTPrinter();
//                        }
                    }
                } else {
                    confirmPaymentDialog.dismiss();
                }

            } else {
                getReceiveablesListFromMerchantServer();
                getSendeableListFromMerchantServer();
                commandeerRefundDialogstep2.dismiss();
            }
        });

        ivBack.setOnClickListener(v -> {
            getReceiveablesListFromMerchantServer();
            getSendeableListFromMerchantServer();
            commandeerRefundDialogstep2.dismiss();
        });
        commandeerRefundDialogstep2.show();


    }

    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1234:
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode == RESULT_OK) {
                    dialogBoxForRefundCommandeer();
                }
                break;
            case 49374:
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() == null) {
                        showToast("Result Not Found");

                    } else {
                        commandeerRefundDialog.dismiss();
                        bolt11fromqr = result.getContents();
                        refundDecodePay(bolt11fromqr);
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
                break;
        }

    }

    private String getDateInCorrectFormat(int year, int monthOfYear, int dayOfMonth) {
        String date;
        String formatedMonth;
        String formatedDay;
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
            public void onResponse(@NonNull Call<TransactionResp> call, @NonNull Response<TransactionResp> response) {
                if (response.body() != null) {
                    TransactionResp transactionResp = response.body();
                    if (!transactionResp.getMessage().equals("successfully done") || transactionResp.getTransactionInfo() == null) {
                        showToast("Not Done!!");
                    }
                    Log.e("Test", "Test");
                }

                Log.e("AddTransactionLog", response.message());
            }

            @Override
            public void onFailure(@NonNull Call<TransactionResp> call, @NonNull Throwable t) {
                Log.e("AddTransactionLog", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void subscrieChannel() {
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
                if (!s.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        final String subscription = jsonObject.getString("event");
                        final JSONObject objects = jsonObject.getJSONObject("data");
                        if (subscription.equals("bts:subscription_succeeded")) {
                            getActivity().runOnUiThread(() -> {
//                                    showToast(subscription);
                            });
                        } else {
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

    public void getInvoicelist() {
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
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
                            parseJSONForSales(text);
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
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                //TODO: stuff

                requireActivity().runOnUiThread(() -> showToast(String.valueOf(response)));

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
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
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
                            parseJSONForRefunds(text);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                });

            }


            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
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
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
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
                            parseJSONForRefunds(text);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                });

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
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
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
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
                            parseJSONForCreatInvocie(text);
                            simpleloader.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });

            }


            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                //TODO: stuff
                Log.e("TAG", "FAIL: " + response);
                requireActivity().runOnUiThread(() -> {
                    simpleloader.dismiss();
                    showToast(String.valueOf(response));
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void confirmPayment(final String lable) {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
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
                            parseJSONForConfirmPayment(text);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                });

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, Throwable t, final okhttp3.Response response) {
                requireActivity().runOnUiThread(() -> {
                    simpleloader.dismiss();
                    showToast(String.valueOf(response));
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }


    public void refundDecodePay(final String bolt11) {
        simpleloader.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {

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
                });

            }

            @Override
            public void onClosing(WebSocket webSocket, int code, @NonNull String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e("TAG", "CLOSE: " + code + " " + reason);
            }


            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, final okhttp3.Response response) {
                requireActivity().runOnUiThread(() -> {
                    simpleloader.dismiss();
                    showToast(String.valueOf(response));
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

}