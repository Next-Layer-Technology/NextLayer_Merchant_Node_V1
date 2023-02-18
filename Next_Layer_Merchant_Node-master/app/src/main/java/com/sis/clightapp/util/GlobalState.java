package com.sis.clightapp.util;

import android.app.Application;

import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.ImageRelocation.GetItemImageReloc;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.UserInfo;
import com.sis.clightapp.model.currency.CurrentAllRate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;


public class GlobalState extends Application {
    public static final String TAG = "GlobalState";
    private String merchant_id;
    Boolean isLogin;
    private static GlobalState mInstance;
//    private Channel_BTCResponseData channel_btcResponseData;
//    private CurrentSpecificRateData currentSpecificRateData;
    private CurrentAllRate currentAllRate;
//    private ArrayList<Invoice> mMerchantSalesListDataSource;
//    private ArrayList<Refund> mMerchantRefundsLIstDataSource;
    private boolean isCheckoutBtnPress = false;
    private String lattitude;
    private String longitude;
    private Items deleteItem;
    private int delteItemPosition;
    private String dellSelectedItemUPC;
    private UserInfo userInfo;
    private Tax tax;
    private String tcIdUTC;
    //Sale & Refund Merchant Side
    private FundingNode fundingNode;
//    private MerchantData merchantData;

    public ArrayList<Items> itemsList = new ArrayList<>();
    public HashSet<Items> selectedItems = new HashSet<>();


    //This arraylist is set when /UserStorage/inventory/ is called from CheckOutFragment1
    private ArrayList<GetItemImageReloc> currentItemImageRelocArrayList;

    public Boolean getLogin() {
        return isLogin;
    }

    public void setLogin(Boolean login) {
        isLogin = login;
    }

//    public Channel_BTCResponseData getChannel_btcResponseData() {
//        return channel_btcResponseData;
//    }
//
//    public void setChannel_btcResponseData(Channel_BTCResponseData channel_btcResponseData) {
//        this.channel_btcResponseData = channel_btcResponseData;
//    }

    public ArrayList<GetItemImageReloc> getCurrentItemImageRelocArrayList() {
        return currentItemImageRelocArrayList;
    }

    //This arraylist is set when /UserStorage/inventory/ is called from CheckOutFragment1
    public void setCurrentItemImageRelocArrayList(ArrayList<GetItemImageReloc> currentItemImageRelocArrayList) {
        this.currentItemImageRelocArrayList = currentItemImageRelocArrayList;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }


//    public MerchantData getMerchantData() {
//        return merchantData;
//    }
//
//    public void setMerchantData(MerchantData merchantData) {
//        this.merchantData = merchantData;
//    }


    public FundingNode getFundingNode() {
        return fundingNode;
    }

    public void setFundingNode(FundingNode fundingNode) {
        this.fundingNode = fundingNode;
    }

    //Emailing Purpose
    private static File saleFile;
    private static File refundFile;

    public static File getRefundFile() {
        return refundFile;
    }

    public static void setRefundFile(File refundFile) {
        GlobalState.refundFile = refundFile;
    }


    public static File getSaleFile() {
        return saleFile;
    }

    public static void setSaleFile(File saleFile) {
        GlobalState.saleFile = saleFile;
    }

    public String getDellSelectedItemUPC() {
        return dellSelectedItemUPC;
    }

    public void setDellSelectedItemUPC(String dellSelectedItemUPC) {
        this.dellSelectedItemUPC = dellSelectedItemUPC;
    }

//    public ArrayList<Refund> getmAdminSendblesListDataSource() {
//        return mAdminSendblesListDataSource;
//    }
//
//    public void setmAdminSendblesListDataSource(ArrayList<Refund> mAdminSendblesListDataSource) {
//        this.mAdminSendblesListDataSource = mAdminSendblesListDataSource;
//    }

//    public ArrayList<Sale> getmAdminReceiveablesListDataSource() {
//        return mAdminReceiveablesListDataSource;
//    }
//
//    public void setmAdminReceiveablesListDataSource(ArrayList<Sale> mAdminReceiveablesListDataSource) {
//        this.mAdminReceiveablesListDataSource = mAdminReceiveablesListDataSource;
//    }


//    public ArrayList<Refund> getmMerchantRefundsLIstDataSource() {
//        return mMerchantRefundsLIstDataSource;
//    }
//
//    public void setmMerchantRefundsLIstDataSource(ArrayList<Refund> mMerchantRefundsLIstDataSource) {
//        this.mMerchantRefundsLIstDataSource = mMerchantRefundsLIstDataSource;
//    }

//    public ArrayList<Invoice> getmMerchantSalesListDataSource() {
//        return mMerchantSalesListDataSource;
//    }
//
//    public void setmMerchantSalesListDataSource(ArrayList<Invoice> mMerchantSalesListDataSource) {
//        this.mMerchantSalesListDataSource = mMerchantSalesListDataSource;
//    }

    public UserInfo getUserInfo() {
        return userInfo;
    }


    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }


    public void setDelteItemPosition(int delteItemPosition) {
        this.delteItemPosition = delteItemPosition;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isCheckoutBtnPress() {
        return isCheckoutBtnPress;
    }

    public void setCheckoutBtnPress(boolean checkoutBtnPress) {
        isCheckoutBtnPress = checkoutBtnPress;
    }

    private boolean usersession;

    public CurrentAllRate getCurrentAllRate() {
        return currentAllRate;
    }

    public void setCurrentAllRate(CurrentAllRate currentAllRate) {
        this.currentAllRate = currentAllRate;
    }

//    public CurrentSpecificRateData getCurrentSpecificRateData() {
//        return currentSpecificRateData;
//    }
//
//    public void setCurrentSpecificRateData(CurrentSpecificRateData currentSpecificRateData) {
//        this.currentSpecificRateData = currentSpecificRateData;
//    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized GlobalState getInstance() {
        if (mInstance == null) {
            mInstance = new GlobalState();
        }
        return mInstance;
    }
}
