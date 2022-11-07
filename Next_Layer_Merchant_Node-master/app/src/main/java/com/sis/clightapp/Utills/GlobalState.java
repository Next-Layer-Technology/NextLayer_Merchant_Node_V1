package com.sis.clightapp.Utills;

import android.app.Application;
import android.util.Log;

import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.Data;
import com.sis.clightapp.model.GsonModel.CreateInvoice;
import com.sis.clightapp.model.GsonModel.DecodePayBolt11;
import com.sis.clightapp.model.GsonModel.Invoice;
import com.sis.clightapp.model.GsonModel.InvoiceForPrint;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.Refund;
import com.sis.clightapp.model.GsonModel.Sale;
import com.sis.clightapp.model.GsonModel.SaleInfo;
import com.sis.clightapp.model.GsonModel.StringImageOfUPCItem;
import com.sis.clightapp.model.GsonModel.UPCofImages;
import com.sis.clightapp.model.ImageRelocation.GetItemImageReloc;
import com.sis.clightapp.model.InvenotryItem;
import com.sis.clightapp.model.REST.FundingNode;
import com.sis.clightapp.model.REST.NodeLineInfo;
import com.sis.clightapp.model.Tax;
import com.sis.clightapp.model.UserInfo;
import com.sis.clightapp.model.currency.CurrentAllRate;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.model.server.ServerData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GlobalState extends Application {
    public static final String TAG = "GlobalState";
    private int itemCountInCart;
    private String merchant_id;
    Boolean isLogin;
    String sendPylist;
    private static GlobalState mInstance;
    private Data merchantDetail;
    private Channel_BTCResponseData channel_btcResponseData;
    private CurrentSpecificRateData currentSpecificRateData;
    private CurrentAllRate currentAllRate;
    private ServerData serverData;
    private String ServerURL;
    private ArrayList<Items> mSelectedDataSourceCheckOutInventory;
    private ArrayList<Items> mDataSourceCheckOutInventory;
    private ArrayList<Items> mDataScannedForPage1;
    private ArrayList<Items> mDataScanedSourceCheckOutInventory;
    private ArrayList<Sale> mMerchantSalesListDataSource;
    private ArrayList<Refund> mMerchantRefundsLIstDataSource;
    private ArrayList<Sale> mAdminReceiveablesListDataSource;
    private ArrayList<Refund> mAdminSendblesListDataSource;
    private ArrayList<UPCofImages> mUPCListOfImagesDataSorce;
    private ArrayList<StringImageOfUPCItem> mStringImageOfUPCItems;
    private boolean isCheckoutBtnPress = false;
    private String lattitude;
    private String longitude;
    private Items deleteItem;
    private int delteItemPosition;
    private String dellSelectedItemName;
    private String dellSelectedItemUPC;
    private String updateSelectedItemName;
    private Invoice invoice;
    private SaleInfo saleInfo;
    private InvoiceForPrint invoiceForPrint;
    private CreateInvoice createInvoice;
    private UserInfo userInfo;
    private Tax tax;
    private String tcIdUTC;
    private DecodePayBolt11 currentDecodePayBolt11;
    private ArrayList<Items> mSeletedForPayDataSourceCheckOutInventory;
    //Sale & Refund Merchant Side
    private ArrayList<Sale> mTodaySaleList;
    private ArrayList<Sale> mTotalSaleList;
    private ArrayList<Sale> mTotalPaidSaleList;
    private ArrayList<Sale> mTotalUnPaidSaleList;
    private ArrayList<Refund> mTodayRefundList;
    private ArrayList<Refund> mTotalRefundList;
    private ArrayList<Refund> mTotalCompleteRefundList;
    private ArrayList<Refund> mTotalUnCompleteRefundList;
    //SendeableList & ReceiveablesList    Admin Side
    private ArrayList<Sale> mTodayReceiveablesList_Sale;
    private ArrayList<Sale> mTotalReceiveablesList_Sale;
    private ArrayList<Sale> mTotalPaidReceiveablesList_Sale;
    private ArrayList<Sale> mTotalUnPaidReceiveablesListt_Sale;
    private ArrayList<Refund> mTodaySendeableList_Refund;
    private ArrayList<Refund> mTotalSendeableList_Refund;
    private ArrayList<Refund> mTotalCompleteSendeableList_Refund;
    private ArrayList<Refund> mTotalUnCompleteSendeableList_Refund;
    private FundingNode fundingNode;
    private NodeLineInfo nodeLineInfo;
    private MerchantData merchantData;

    private boolean isMerchantConfirm = false;

    //This arraylist is set when /UserStorage/inventory/ is called from CheckOutFragment1
    private ArrayList<GetItemImageReloc> currentItemImageRelocArrayList;

    public String getSendPylist() {
        return sendPylist;
    }

    public void setSendPylist(String sendPylist) {
        this.sendPylist = sendPylist;
    }

    public Boolean getLogin() {
        return isLogin;
    }

    public void setLogin(Boolean login) {
        isLogin = login;
    }

    public Channel_BTCResponseData getChannel_btcResponseData() {
        return channel_btcResponseData;
    }

    public void setChannel_btcResponseData(Channel_BTCResponseData channel_btcResponseData) {
        this.channel_btcResponseData = channel_btcResponseData;
    }

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

    public boolean isMerchantConfirm() {
        return isMerchantConfirm;
    }

    public void setMerchantConfirm(boolean merchantConfirm) {
        isMerchantConfirm = merchantConfirm;
    }

    public MerchantData getMerchantData() {
        return merchantData;
    }

    public void setMerchantData(MerchantData merchantData) {
        this.merchantData = merchantData;
    }

    public NodeLineInfo getNodeLineInfo() {
        return nodeLineInfo;
    }

    public void setNodeLineInfo(NodeLineInfo nodeLineInfo) {
        this.nodeLineInfo = nodeLineInfo;
    }

    public FundingNode getFundingNode() {
        return fundingNode;
    }

    public void setFundingNode(FundingNode fundingNode) {
        this.fundingNode = fundingNode;
    }

    public ArrayList<Sale> getmTodayReceiveablesList_Sale() {
        return mTodayReceiveablesList_Sale;
    }

    public void setmTodayReceiveablesList_Sale(ArrayList<Sale> mTodayReceiveablesList_Sale) {
        this.mTodayReceiveablesList_Sale = mTodayReceiveablesList_Sale;
    }

    public ArrayList<Sale> getmTotalReceiveablesList_Sale() {
        return mTotalReceiveablesList_Sale;
    }

    public void setmTotalReceiveablesList_Sale(ArrayList<Sale> mTotalReceiveablesList_Sale) {
        this.mTotalReceiveablesList_Sale = mTotalReceiveablesList_Sale;
    }

    public ArrayList<Sale> getmTotalPaidReceiveablesList_Sale() {
        return mTotalPaidReceiveablesList_Sale;
    }

    public void setmTotalPaidReceiveablesList_Sale(ArrayList<Sale> mTotalPaidReceiveablesList_Sale) {
        this.mTotalPaidReceiveablesList_Sale = mTotalPaidReceiveablesList_Sale;
    }

    public ArrayList<Sale> getmTotalUnPaidReceiveablesListt_Sale() {
        return mTotalUnPaidReceiveablesListt_Sale;
    }

    public void setmTotalUnPaidReceiveablesListt_Sale(ArrayList<Sale> mTotalUnPaidReceiveablesListt_Sale) {
        this.mTotalUnPaidReceiveablesListt_Sale = mTotalUnPaidReceiveablesListt_Sale;
    }

    public ArrayList<Refund> getmTodaySendeableList_Refund() {
        return mTodaySendeableList_Refund;
    }

    public void setmTodaySendeableList_Refund(ArrayList<Refund> mTodaySendeableList_Refund) {
        this.mTodaySendeableList_Refund = mTodaySendeableList_Refund;
    }

    public ArrayList<Refund> getmTotalSendeableList_Refund() {
        return mTotalSendeableList_Refund;
    }

    public void setmTotalSendeableList_Refund(ArrayList<Refund> mTotalSendeableList_Refund) {
        this.mTotalSendeableList_Refund = mTotalSendeableList_Refund;
    }

    public ArrayList<Refund> getmTotalCompleteSendeableList_Refund() {
        return mTotalCompleteSendeableList_Refund;
    }

    public void setmTotalCompleteSendeableList_Refund(ArrayList<Refund> mTotalCompleteSendeableList_Refund) {
        this.mTotalCompleteSendeableList_Refund = mTotalCompleteSendeableList_Refund;
    }

    public ArrayList<Refund> getmTotalUnCompleteSendeableList_Refund() {
        return mTotalUnCompleteSendeableList_Refund;
    }

    public void setmTotalUnCompleteSendeableList_Refund(ArrayList<Refund> mTotalUnCompleteSendeableList_Refund) {
        this.mTotalUnCompleteSendeableList_Refund = mTotalUnCompleteSendeableList_Refund;
    }

    public ArrayList<Refund> getmTodayRefundList() {
        return mTodayRefundList;
    }

    public void setmTodayRefundList(ArrayList<Refund> mTodayRefundList) {
        this.mTodayRefundList = mTodayRefundList;
    }

    public ArrayList<Refund> getmTotalRefundList() {
        return mTotalRefundList;
    }

    public void setmTotalRefundList(ArrayList<Refund> mTotalRefundList) {
        this.mTotalRefundList = mTotalRefundList;
    }

    public ArrayList<Refund> getmTotalCompleteRefundList() {
        return mTotalCompleteRefundList;
    }

    public void setmTotalCompleteRefundList(ArrayList<Refund> mTotalCompleteRefundList) {
        this.mTotalCompleteRefundList = mTotalCompleteRefundList;
    }

    public ArrayList<Refund> getmTotalUnCompleteRefundList() {
        return mTotalUnCompleteRefundList;
    }

    public void setmTotalUnCompleteRefundList(ArrayList<Refund> mTotalUnCompleteList) {
        this.mTotalUnCompleteRefundList = mTotalUnCompleteList;
    }

    public ArrayList<Sale> getmTodaySaleList() {
        return mTodaySaleList;
    }

    public void setmTodaySaleList(ArrayList<Sale> mTodaySaleList) {
        this.mTodaySaleList = mTodaySaleList;
    }

    public ArrayList<Sale> getmTotalSaleList() {
        return mTotalSaleList;
    }

    public void setmTotalSaleList(ArrayList<Sale> mTotalSaleList) {
        this.mTotalSaleList = mTotalSaleList;
    }

    public ArrayList<Sale> getmTotalPaidSaleList() {
        return mTotalPaidSaleList;
    }

    public void setmTotalPaidSaleList(ArrayList<Sale> mTotalPaidSaleList) {
        this.mTotalPaidSaleList = mTotalPaidSaleList;
    }

    public ArrayList<Sale> getmTotalUnPaidSaleList() {
        return mTotalUnPaidSaleList;
    }

    public void setmTotalUnPaidSaleList(ArrayList<Sale> mTotalUnPaidSaleList) {
        this.mTotalUnPaidSaleList = mTotalUnPaidSaleList;
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

    ArrayList<Sale> mSaleDataSource;

    public static File getSaleFile() {
        return saleFile;
    }

    public static void setSaleFile(File saleFile) {
        GlobalState.saleFile = saleFile;
    }

    public ArrayList<Sale> getmSaleDataSource() {
        return mSaleDataSource;
    }

    public void setmSaleDataSource(ArrayList<Sale> mSaleDataSource) {
        this.mSaleDataSource = mSaleDataSource;
    }

    public ArrayList<StringImageOfUPCItem> getmStringImageOfUPCItems() {
        return mStringImageOfUPCItems;
    }

    public void setmStringImageOfUPCItems(ArrayList<StringImageOfUPCItem> mStringImageOfUPCItems) {
        this.mStringImageOfUPCItems = mStringImageOfUPCItems;
    }

    public ArrayList<UPCofImages> getmUPCListOfImagesDataSorce() {
        return mUPCListOfImagesDataSorce;
    }

    public void setmUPCListOfImagesDataSorce(ArrayList<UPCofImages> mUPCListOfImagesDataSorce) {
        this.mUPCListOfImagesDataSorce = mUPCListOfImagesDataSorce;
    }

    public String getDellSelectedItemUPC() {
        return dellSelectedItemUPC;
    }

    public void setDellSelectedItemUPC(String dellSelectedItemUPC) {
        this.dellSelectedItemUPC = dellSelectedItemUPC;
    }

    public ArrayList<Items> getmDataScannedForPage1() {
        return mDataScannedForPage1;
    }

    public void removeInMDataScannedForPage1(Items item) {
        if (this.mDataScannedForPage1.size() > 0) {
            this.mDataScannedForPage1.remove(item);
        }
    }

    public void setmDataScannedForPage1(ArrayList<Items> mDataScannedForPage1) {
        this.mDataScannedForPage1 = mDataScannedForPage1;
    }

    public void addInmDataScannedForPage1(Items items) {
        if (this.mDataScannedForPage1 != null) {
            this.mDataScannedForPage1.add(items);
        } else {
            this.mDataScannedForPage1 = new ArrayList<>();
            this.mDataScannedForPage1.add(items);
        }
    }

    public DecodePayBolt11 getCurrentDecodePayBolt11() {
        return currentDecodePayBolt11;
    }

    public void setCurrentDecodePayBolt11(DecodePayBolt11 currentDecodePayBolt11) {
        this.currentDecodePayBolt11 = currentDecodePayBolt11;
    }

    public String getTcIdUTC() {
        return tcIdUTC;
    }

    public void setTcIdUTC(String tcIdUTC) {
        this.tcIdUTC = tcIdUTC;
    }

    public ArrayList<Refund> getmAdminSendblesListDataSource() {
        return mAdminSendblesListDataSource;
    }

    public void setmAdminSendblesListDataSource(ArrayList<Refund> mAdminSendblesListDataSource) {
        this.mAdminSendblesListDataSource = mAdminSendblesListDataSource;
    }

    public ArrayList<Sale> getmAdminReceiveablesListDataSource() {
        return mAdminReceiveablesListDataSource;
    }

    public void setmAdminReceiveablesListDataSource(ArrayList<Sale> mAdminReceiveablesListDataSource) {
        this.mAdminReceiveablesListDataSource = mAdminReceiveablesListDataSource;
    }

    public int getItemCountInCart() {
        return itemCountInCart;
    }

    public void setItemCountInCart(int itemCountInCart) {
        this.itemCountInCart = itemCountInCart;
    }

    public ArrayList<Refund> getmMerchantRefundsLIstDataSource() {
        return mMerchantRefundsLIstDataSource;
    }

    public void setmMerchantRefundsLIstDataSource(ArrayList<Refund> mMerchantRefundsLIstDataSource) {
        this.mMerchantRefundsLIstDataSource = mMerchantRefundsLIstDataSource;
    }

    public ArrayList<Sale> getmMerchantSalesListDataSource() {
        return mMerchantSalesListDataSource;
    }

    public void setmMerchantSalesListDataSource(ArrayList<Sale> mMerchantSalesListDataSource) {
        this.mMerchantSalesListDataSource = mMerchantSalesListDataSource;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public ArrayList<Items> getmDataScanedSourceCheckOutInventory() {
        return mDataScanedSourceCheckOutInventory;
    }

    public void setmDataScanedSourceCheckOutInventory(ArrayList<Items> mDataScanedSourceCheckOutInventory) {
        this.mDataScanedSourceCheckOutInventory = mDataScanedSourceCheckOutInventory;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public CreateInvoice getCreateInvoice() {
        return createInvoice;
    }

    public void setCreateInvoice(CreateInvoice createInvoice) {
        this.createInvoice = createInvoice;
    }

    public InvoiceForPrint getInvoiceForPrint() {
        return invoiceForPrint;
    }

    public void setInvoiceForPrint(InvoiceForPrint invoiceForPrint) {
        this.invoiceForPrint = invoiceForPrint;
    }

    public void setMerchantDetail(Data merchantDetail) {
        this.merchantDetail = merchantDetail;
    }

    public void setServerURL(String serverURL) {
        ServerURL = serverURL;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public SaleInfo getSaleInfo() {
        return saleInfo;
    }

    public void setSaleInfo(SaleInfo saleInfo) {
        this.saleInfo = saleInfo;
    }

    public String getUpdateSelectedItemName() {
        return updateSelectedItemName;
    }

    public void setUpdateSelectedItemName(String updateSelectedItemName) {
        this.updateSelectedItemName = updateSelectedItemName;
    }

    public String getDellSelectedItemName() {
        return dellSelectedItemName;
    }

    public void setDellSelectedItemName(String dellSelectedItemName) {
        this.dellSelectedItemName = dellSelectedItemName;
    }

    public Items getDeleteItem() {
        return deleteItem;
    }

    public void setDeleteItem(Items deleteItem) {
        this.deleteItem = deleteItem;
    }

    public int getDelteItemPosition() {
        return delteItemPosition;
    }

    public void setDelteItemPosition(int delteItemPosition) {
        this.delteItemPosition = delteItemPosition;
    }

    public String getLattitude() {
        return lattitude;
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


    /*
    These Items appear in Checkout Page 3
     */
    public ArrayList<Items> getmSeletedForPayDataSourceCheckOutInventory() {
        Log.v(TAG, "getmSeletedForPayDataSourceCheckOutInventory: ");
        return mSeletedForPayDataSourceCheckOutInventory;

    }

    public void addAllmSeletedForPayDataSourceCheckOutInventory(ArrayList<Items> addAllList) {
        Log.v(TAG, "addAllmSeletedForPayDataSourceCheckOutInventory: " + addAllList.toString());
        if (this.mSeletedForPayDataSourceCheckOutInventory != null) {
            this.mSeletedForPayDataSourceCheckOutInventory.addAll(addAllList);
            Set<Items> s = new HashSet<Items>();
            s.addAll(this.mSeletedForPayDataSourceCheckOutInventory);
            this.mSeletedForPayDataSourceCheckOutInventory = new ArrayList<Items>();
            this.mSeletedForPayDataSourceCheckOutInventory.addAll(s);
        } else {
            this.mSeletedForPayDataSourceCheckOutInventory = new ArrayList<>();
            this.mSeletedForPayDataSourceCheckOutInventory.addAll(addAllList);
        }
    }

    public void setmSeletedForPayDataSourceCheckOutInventory(ArrayList<Items> mSeletedForPayDataSourceCheckOutInventory) {
        Log.v(TAG, "setmSeletedForPayDataSourceCheckOutInventory: " + mSeletedForPayDataSourceCheckOutInventory.toString());
        this.mSeletedForPayDataSourceCheckOutInventory = mSeletedForPayDataSourceCheckOutInventory;
    }

    public ArrayList<Items> getmSelectedDataSourceCheckOutInventory() {
        return mSelectedDataSourceCheckOutInventory;
    }

    public void setmSelectedDataSourceCheckOutInventory(ArrayList<Items> mSelectedDataSourceCheckOutInventory) {
        this.mSelectedDataSourceCheckOutInventory = mSelectedDataSourceCheckOutInventory;
    }

    public ArrayList<Items> getmDataSourceCheckOutInventory() {
        return mDataSourceCheckOutInventory;
    }

    public void setmDataSourceCheckOutInventory(ArrayList<Items> mDataSourceCheckOutInventory) {
        this.mDataSourceCheckOutInventory = mDataSourceCheckOutInventory;
    }

    public void addInmScannedDataSourceCheckOutInventory(Items item) {
        if (this.mDataScanedSourceCheckOutInventory != null) {
            this.mDataScanedSourceCheckOutInventory.add(item);
        } else {
            this.mDataScanedSourceCheckOutInventory = new ArrayList<>();
            this.mDataScanedSourceCheckOutInventory.add(item);
        }
    }

    public void removeInmScannedDataSourceCheckOutInventory(Items item) {
        if (this.mDataScanedSourceCheckOutInventory.size() > 0) {
            this.mDataScanedSourceCheckOutInventory.remove(item);
        }
    }

    public void addInmSelectedDataSourceCheckOutInventory(Items item) {
        if (this.mSelectedDataSourceCheckOutInventory != null) {
            this.mSelectedDataSourceCheckOutInventory.add(item);
        } else {
            this.mSelectedDataSourceCheckOutInventory = new ArrayList<>();
            this.mSelectedDataSourceCheckOutInventory.add(item);
        }
    }

    public void removeInmSelectedDataSourceCheckOutInventory(Items item) {
        if (this.mSelectedDataSourceCheckOutInventory.size() > 0) {
            this.mSelectedDataSourceCheckOutInventory.remove(item);
        }
    }

    public void addInmSeletedForPayDataSourceCheckOutInventory(Items item) {
        Log.v(TAG, "addInmSeletedForPayDataSourceCheckOutInventory: " + item.toString());
        if (this.mSeletedForPayDataSourceCheckOutInventory != null) {
            this.mSeletedForPayDataSourceCheckOutInventory.add(item);
        } else {
            this.mSeletedForPayDataSourceCheckOutInventory = new ArrayList<>();
            this.mSeletedForPayDataSourceCheckOutInventory.add(item);
        }
    }

    public void removeInmSeletedForPayDataSourceCheckOutInventory(Items item) {
        Log.v(TAG, "removeInmSeletedForPayDataSourceCheckOutInventory: " + item.toString());
        if (this.mSeletedForPayDataSourceCheckOutInventory.size() > 0) {
            this.mSeletedForPayDataSourceCheckOutInventory.remove(item);
        }
    }

    public void addInmDataSourceCheckOutInventory(Items invenotryItem) {
        if (this.mDataSourceCheckOutInventory != null) {
            this.mDataSourceCheckOutInventory.add(invenotryItem);
        } else {
            this.mDataSourceCheckOutInventory = new ArrayList<>();
            this.mDataSourceCheckOutInventory.add(invenotryItem);
        }
    }

    public void removeInmDataSourceCheckOutInventory(Items invenotryItem) {
        if (this.mDataSourceCheckOutInventory.size() > 0) {
            this.mDataSourceCheckOutInventory.remove(invenotryItem);
        }
    }

    public ServerData getServerData() {
        return serverData;
    }

    public void setServerData(ServerData serverData) {
        this.serverData = serverData;
    }

    public String getServerURL() {
        return ServerURL;
    }

    public boolean isUsersession() {
        return usersession;
    }

    public void setUsersession(boolean usersession) {
        this.usersession = usersession;
    }

    private boolean usersession;

    public CurrentAllRate getCurrentAllRate() {
        return currentAllRate;
    }

    public void setCurrentAllRate(CurrentAllRate currentAllRate) {
        this.currentAllRate = currentAllRate;
    }

    public CurrentSpecificRateData getCurrentSpecificRateData() {
        return currentSpecificRateData;
    }

    public void setCurrentSpecificRateData(CurrentSpecificRateData currentSpecificRateData) {
        this.currentSpecificRateData = currentSpecificRateData;
    }

    public Data getMerchantDetail() {
        return merchantDetail;
    }

    public void setUserDetail(Data merchantDetail) {
        this.merchantDetail = merchantDetail;
    }


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

    public void clearData() {
//mInstance.merchantDetail=null;
    }
}
