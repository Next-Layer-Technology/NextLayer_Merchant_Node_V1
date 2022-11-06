package com.sis.clightapp.fragments.merchant;

import android.Manifest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sis.clightapp.Interface.ApiClient2;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.Interface.ApiPaths2;
import com.sis.clightapp.Network.CheckNetwork;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.Functions2;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.MainActivity;

import com.sis.clightapp.adapter.MerchantItemAdapter;
import com.sis.clightapp.model.Channel_BTCResponseData;
import com.sis.clightapp.model.GsonModel.Items;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.AddItemsModel;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemLIstModel;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemPhotoPath;
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemsDataMerchant;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.GsonModel.StringImageOfUPCItem;
import com.sis.clightapp.model.GsonModel.UPCofImages;

import com.sis.clightapp.model.ImageRelocation.AddImageResp;
import com.sis.clightapp.model.ImageRelocation.GetItemImageRSP;
import com.sis.clightapp.model.ImageRelocation.GetItemImageReloc;
import com.sis.clightapp.model.currency.CurrentAllRate;
import com.sis.clightapp.model.currency.CurrentSpecificRateData;
import com.sis.clightapp.session.MyLogOutService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

import static android.app.Activity.RESULT_OK;

import org.json.JSONException;
import org.json.JSONObject;

public class MerchantFragment2 extends MerchantBaseFragment {
    private MerchantFragment2 merchantFragment2;
    Button additem, deleteitem, updateitem, inventorybtn;
    ListView merchantinvetorylistview;
    String TAG = "CLighting App";
    File itemImageFile;
    String file;
    String photoPath = "";
    private WebSocketClient webSocketClient;
    Bitmap btmapimage;
    private Dialog dialog, addItemDialog;
    ProgressDialog refreshProgressDialog, addItemprogressDialog, addItemImageStringProgressDialog, deleteItemProgressBar, updateProgressBar, exitFromServerProgressDialog;


    RecyclerView recyclerView;
    MerchantItemAdapter merchantItemAdapter;


    int intScreenWidth, intScreenHeight;
    double CurrentRateInBTC;
    ApiPaths fApiPaths;
    Functions2 functions;

    String upcsl = "";
    public de.hdodenhof.circleimageview.CircleImageView itemImage;
    public static final int GALLERY_REQUEST = 1;
    public static final int REQUEST_WRITE_PERMISSION = 786;
    Bitmap selectImgBitMap = null;
    Bitmap cameraImgBitMap = null;
    int cursize = 0;
    int totalSize = 0;
    static Boolean ASYNC_TASK_FINISHED;
    static Boolean isAddorDelete;
    //    static  boolean isImageGet=false;
    static boolean isbarGet = true;
    private static final int CAMERA_REQ = 12;
    private static final int GALLERY_REQ = 13;

    ArrayList<StringImageOfUPCItem> mDataSourceImageString;
    String x = null;
    String itemName2 = null;
    String itemPrice2 = null;
    String itemQuantity2 = null;
    String itemUpc2 = null;


    ArrayList<Items> mMerchantItemsDataSource = new ArrayList<>();
    List<ItemLIstModel> listModelList = new ArrayList<>();
    TextView setTextWithSpan;
    ImageView selectedItemImage;
    boolean isImageGet = false;
    boolean isInApp = true;


    public MerchantFragment2() {
        // Required empty public constructor
    }

    public MerchantFragment2 getInstance() {
        if (merchantFragment2 == null) {
            merchantFragment2 = new MerchantFragment2();
        }
        return merchantFragment2;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    ;

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //pickItemImage();
            imageOptions();
            isInApp = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_merchant2, container, false);
        setTextWithSpan = view.findViewById(R.id.poweredbyimage);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan, getString(R.string.welcome_text), getString(R.string.welcome_text_bold), boldStyle);
        exitFromServerProgressDialog = new ProgressDialog(getContext());
        exitFromServerProgressDialog.setMessage("Exiting");
        addItemprogressDialog = new ProgressDialog(getContext());
        addItemprogressDialog.setMessage("Adding...");
        refreshProgressDialog = new ProgressDialog(getContext());
        refreshProgressDialog.setMessage("Realoding");
        deleteItemProgressBar = new ProgressDialog(getContext());
        deleteItemProgressBar.setMessage("Deleting...");
        updateProgressBar = new ProgressDialog(getContext());
        updateProgressBar.setMessage("Updating...");
        addItemImageStringProgressDialog = new ProgressDialog(getContext());
        addItemImageStringProgressDialog.setMessage("Add Image..");
        intScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        intScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        additem = view.findViewById(R.id.imageView5);
        deleteitem = view.findViewById(R.id.imageView7);
        recyclerView = view.findViewById(R.id.merchant2listview);
        inventorybtn = view.findViewById(R.id.inventrytxt);

        inventorybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SubscrieChannel();
                refreshProgressDialog.show();
                refreshProgressDialog.setCancelable(false);
                refreshProgressDialog.setCanceledOnTouchOutside(false);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        refreshProgressDialog.dismiss();
                    }
                }, 2000); // 3000 milliseconds delay

            }
        });
        additem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBoxForAddItem();

            }
        });

        deleteitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBoxForDeleteItem();
            }
        });
        if (CheckNetwork.isInternetAvailable(getContext())) {
            //getcurrentrate();
            SubscrieChannel();
        } else {
            setcurrentrate("Not Found");
        }
        if (CheckNetwork.isInternetAvailable(getContext())) {
            MerchantData merchantData = GlobalState.getInstance().getMerchantData();
            if (merchantData != null) {
                // getAllItemsImageList(merchantData);
            } else {
            }
        } else {
        }
        return view;
    }

    private void getAllItems() {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token = "Bearer" + " " + RefToken;
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", RefToken);

        Call<ItemsDataMerchant> call = (Call<ItemsDataMerchant>) ApiClient2.getRetrofit().create(ApiPaths2.class).getInventoryItems(token);
        call.enqueue(new Callback<ItemsDataMerchant>() {
            @Override
            public void onResponse(Call<ItemsDataMerchant> call, Response<ItemsDataMerchant> response) {
                if (response.body() != null) {
                    ItemsDataMerchant itemsDataMerchant = response.body();
                    if (itemsDataMerchant.getSuccess()) {
                        showToast(itemsDataMerchant.getMessage());
                        List<ItemLIstModel> lIstModelList = itemsDataMerchant.getList();
                        listModelList.clear();
                        listModelList = itemsDataMerchant.getList();
                        refreshItemsAdapter(lIstModelList);
                        //parseJSONItems(lIstModelList);
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
                                            // showToast("Ok!!");
                                            //getItemFromThor();
                                            //ByNaeem
                                            parseJSON("");
                                        } else {

                                        }
                                    } else {

                                    }
                                } else {

                                }

                            } else {
                                //show msg

                            }
                        } else {
                            //fail with reason
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

    private void setAdapter() {
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

    public void dialogBoxForUpdateDelItem(ItemLIstModel itemLIstModel) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialoglayoutupdateitem2);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        final EditText edtUpdateName = dialog.findViewById(R.id.edtUpdateName);
        final CircleImageView imageview = dialog.findViewById(R.id.iv_update);
//
        final EditText edtUpdatePrice = dialog.findViewById(R.id.edtUpdatePrice);
//
        final EditText edtUpdateQuanity = dialog.findViewById(R.id.edtUpdateQuantity);
        String itemUpc = "";
//
        final Button btnUpdate = dialog.findViewById(R.id.btnUpdate2);//btnUpdate2
        final Button btnDelete = dialog.findViewById(R.id.btnDelete);
        final ImageView ivBack = dialog.findViewById(R.id.iv_back_invoice);

        edtUpdateName.setText(itemLIstModel.getName());
        edtUpdatePrice.setText(itemLIstModel.getUnit_price());
        edtUpdateQuanity.setText(itemLIstModel.getQuantity_left());

        final String finalItemUpc = itemUpc;
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String updateNameValue = edtUpdateName.getText().toString();
                String updatePriceValue = edtUpdatePrice.getText().toString();
                String updateQuaitiyValue = edtUpdateQuanity.getText().toString();

                if (updatePriceValue.isEmpty() || updateQuaitiyValue.isEmpty()) {
                    showToast("Not Valid");
                } else {
                    dialog.dismiss();
                    ask_UpdateItemConfirmation(updateNameValue,updatePriceValue,updateQuaitiyValue,itemLIstModel);
                }

            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ask_UpdateItemDelete(itemLIstModel.getId());
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    public void ask_UpdateItemConfirmation(String name,String price,String quantity,ItemLIstModel itemLIstModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.update_title));
        builder.setMessage(getString(R.string.update_subtitle));
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateItem(itemLIstModel.getId(), name, quantity, price);

            }
        });
        builder.show();
    }
    public void ask_UpdateItemDelete(String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete_title));
        builder.setMessage(getString(R.string.delete_subtitle));
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem(id);

            }
        });
        builder.show();
    }
    private void updateItem(String id,String name,String qty,String price) {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token="Bearer"+" "+RefToken;
        JsonObject jsonObject1 = new JsonObject();
        // jsonObject1.addProperty("refresh", RefToken);
        jsonObject1.addProperty("name", name);
       // jsonObject1.addProperty("upc", upc);
        jsonObject1.addProperty("quantity",  Float.parseFloat(qty));
        jsonObject1.addProperty("price",Float.parseFloat(price));
       // jsonObject1.addProperty("img_path", photoPath);

        Call<AddItemsModel> call = (Call<AddItemsModel>) ApiClient2.getRetrofit().create(ApiPaths2.class).updateInventoryItems(token, Integer.parseInt(id),jsonObject1);
        call.enqueue(new Callback<AddItemsModel>() {
            @Override
            public void onResponse(Call<AddItemsModel> call, Response<AddItemsModel> response) {
                if (response.body() != null) {
                    AddItemsModel itemsDataMerchant = response.body();
                    if (itemsDataMerchant.isSuccess()) {
                        showToast(itemsDataMerchant.getMessage());
                        getAllItems();
                    }else {
                        showToast(itemsDataMerchant.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<AddItemsModel> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });

    }
    private void deleteItem(String id) {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token="Bearer"+" "+RefToken;
        Call<AddItemsModel> call = (Call<AddItemsModel>) ApiClient2.getRetrofit().create(ApiPaths2.class).deleteInventoryItems(token, Integer.parseInt(id));
        call.enqueue(new Callback<AddItemsModel>() {
            @Override
            public void onResponse(Call<AddItemsModel> call, Response<AddItemsModel> response) {
                if (response.body() != null) {
                    AddItemsModel itemsDataMerchant = response.body();
                    if (itemsDataMerchant.isSuccess()) {
                        showToast(itemsDataMerchant.getMessage());
                        getAllItems();
                    }else {
                        showToast(itemsDataMerchant.getMessage());
                        getAllItems();
                    }
                }
            }
            @Override
            public void onFailure(Call<AddItemsModel> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });

    }

    public void dialogBoxForUpdateItemMerchant2(String selectedItemName) {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialoglayoutupdateitem2);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        final EditText edtUpdateName = dialog.findViewById(R.id.edtUpdateName);
        final CircleImageView imageview = dialog.findViewById(R.id.iv_update);
//        edtUpdateName.setEnabled(false);
//        edtUpdateName.setFocusable(false);
//        edtUpdateName.setFocusableInTouchMode(false);
        final EditText edtUpdatePrice = dialog.findViewById(R.id.edtUpdatePrice);
//        edtUpdatePrice.setEnabled(false);
//        edtUpdatePrice.setFocusable(false);
//        edtUpdatePrice.setFocusableInTouchMode(false);
        final EditText edtUpdateQuanity = dialog.findViewById(R.id.edtUpdateQuantity);
        String itemUpc = "";
//        final EditText edtUpdateDescription=dialog.findViewById(R.id.edtUpdateAdditionalInfo);
//        edtUpdateDescription.setEnabled(false);
//        edtUpdateDescription.setFocusable(false);
//        edtUpdateDescription.setFocusableInTouchMode(false);
        final Button btnUpdate = dialog.findViewById(R.id.btnUpdate2);
        final ImageView ivBack = dialog.findViewById(R.id.iv_back_invoice);
        String itemName = String.valueOf(selectedItemName);
        ArrayList<Items> mdSource = GlobalState.getInstance().getmDataSourceCheckOutInventory();
        if (mdSource != null) {
            for (int j = 0; j < mdSource.size(); j++) {
                if (mdSource.get(j).getName().equals(itemName)) {
                    itemUpc = mdSource.get(j).getUPC();
                    edtUpdateName.setText(mdSource.get(j).getName());
                    edtUpdatePrice.setText(mdSource.get(j).getPrice());
                    edtUpdateQuanity.setText(mdSource.get(j).getQuantity());
                    file = mdSource.get(j).getQuantity();
//                    Glide.with(getContext()).load(AppConstants.MERCHANT_ITEM_IMAGE + mdSource.get(j).getImageUrl()).into(imageview);
//                    imageview.setDrawingCacheEnabled(true);
//                    btmapimage = imageview.getDrawingCache();
//                    File file = null;
//                    try {
//                        file = savebitmap(selectImgBitMap);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        isImageGet = false;
//                    }
//                    if (file != null) {
//                        isImageGet = true;
//                        itemImageFile = file;
//                        itemImage.setImageBitmap(selectImgBitMap);
//                    }
                }

            }
        }
        final String finalItemUpc = itemUpc;
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String updateNameValue = edtUpdateName.getText().toString();
                String updatePriceValue = edtUpdatePrice.getText().toString();
                String updateQuaitiyValue = edtUpdateQuanity.getText().toString();
                // String updateDescriptionValue=edtUpdateDescription.getText().toString();
                if (updatePriceValue.isEmpty() || updateQuaitiyValue.isEmpty()) {
                    showToast("Not Valid");
                } else {
                    ask_UpdateItem(finalItemUpc, updatePriceValue, updateNameValue, updateQuaitiyValue);
                }

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    /*Creating dialogue for ADD,UPDATE,DELETE Items*/
    public void dialogBoxForUpdateItemMerchant() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialoglayoutupdateitem);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        final EditText edtUpdateName = dialog.findViewById(R.id.edtUpdateName);
        final EditText edtUpdatePrice = dialog.findViewById(R.id.edtUpdatePrice);
        final EditText edtUpdateQuanity = dialog.findViewById(R.id.edtUpdateQuantity);
        final EditText edtUpdateDescription = dialog.findViewById(R.id.edtUpdateAdditionalInfo);
        final Button btnUpdate = dialog.findViewById(R.id.btnUpdate2);

        edtUpdateName.setVisibility(View.GONE);
        edtUpdatePrice.setVisibility(View.GONE);
        edtUpdateQuanity.setVisibility(View.GONE);
        edtUpdateDescription.setVisibility(View.GONE);
        btnUpdate.setVisibility(View.GONE);

        final ImageView ivBack = dialog.findViewById(R.id.iv_back_invoice);
        // progressBar = dialog.findViewById(R.id.progress_bar);
        Spinner dropdown = (Spinner) dialog.findViewById(R.id.spinner1);

        ArrayList<String> itemlist = new ArrayList<>();
        ArrayList<Items> iteminventoryList = new ArrayList<>();
        iteminventoryList = GlobalState.getInstance().getmDataSourceCheckOutInventory();
        itemlist.add("Select Delete item");

        if (iteminventoryList != null) {
            for (int i = 0; i < iteminventoryList.size(); i++) {
                itemlist.add(iteminventoryList.get(i).getName());
            }
        }


        //  ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, itemlist);
        dropdown.setAdapter(adapter);
        final ArrayList<Items> finalIteminventoryList = iteminventoryList;
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0) {
                    // TODO:do nothing
                } else {
                    edtUpdateName.setVisibility(View.VISIBLE);
                    edtUpdatePrice.setVisibility(View.VISIBLE);
                    edtUpdateQuanity.setVisibility(View.VISIBLE);
                    edtUpdateDescription.setVisibility(View.VISIBLE);
                    btnUpdate.setVisibility(View.VISIBLE);
                    showToast(String.valueOf(i));
                    Object objname = adapterView.getSelectedItem();
                    String itemName = String.valueOf(objname);
                    ArrayList<Items> mdSource = GlobalState.getInstance().getmDataSourceCheckOutInventory();
                    if (mdSource != null) {
                        for (int j = 0; j < mdSource.size(); j++) {
                            if (mdSource.get(j).getName().equals(itemName)) {
                                edtUpdateName.setText(mdSource.get(j).getName());
                                edtUpdatePrice.setText(mdSource.get(j).getPrice());
                                edtUpdateQuanity.setText(mdSource.get(j).getQuantity());
                                edtUpdateDescription.setText(mdSource.get(j).getAdditionalInfo());
                            }

                        }
                    }
                    btnUpdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            String updateNameValue = edtUpdateName.getText().toString();
                            String updatePriceValue = edtUpdatePrice.getText().toString();
                            String updateQuaitiyValue = edtUpdateQuanity.getText().toString();
                            String updateDescriptionValue = edtUpdateDescription.getText().toString();
                            if (updatePriceValue.isEmpty() || updateQuaitiyValue.isEmpty() || updateDescriptionValue.isEmpty()) {
                                showToast("Not Valid");
                            } else {
                                //ask_UpdateItem(updateNameValue,updatePriceValue,updateQuaitiyValue,updateDescriptionValue);
                            }

                        }
                    });

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    private void pickItemImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }
    private void imageOptions() {
        final CharSequence items[] = {"Camera", "Gallery", "Cancel"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Add Image From");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,CAMERA_REQ);
                }
                if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select Image"), GALLERY_REQUEST);
                }
                dialogInterface.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST:
                    Uri selectedImage = data.getData();
                    try {
                        selectImgBitMap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);

                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }

                    File file = null;
                    try {
                        file = savebitmap(selectImgBitMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        isImageGet = false;
                    }
                    if (file != null) {
                        isImageGet = true;
                        itemImageFile = file;
                        uploadImageItems();
                        itemImage.setImageBitmap(selectImgBitMap);
//                        int file_size = Integer.parseInt(String.valueOf(file.length()/1024));
//                        Log.e("Size",String.valueOf(file_size));
//                        if(file_size>5) {
////                            isImageGet=false;
//                            new AlertDialog.Builder(getContext())
//                                    .setMessage("Image Size must be Less then 5kb")
//                                    .setPositiveButton("Retry", null)
//                                    .show();
//                            itemImage.setImageResource(R.drawable.ic_launcher2);
//
//                        }else
//                        {
////                            isImageGet=true;
//                            x= ImageToBase16Hex.bitMapToBase16String(selectImgBitMap);
//                            itemImage.setImageBitmap(selectImgBitMap);
//
                    }
                    break;
                case CAMERA_REQ:
                    Bundle bundle = data.getExtras();
                    cameraImgBitMap = (Bitmap) bundle.get("data");
                    File file2 = null;
                    try {
                        file2 = savebitmap(cameraImgBitMap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        isImageGet = false;
                    }
                    if (file2 != null) {
                        isImageGet = true;
                        itemImageFile = file2;
                        uploadImageItems();
                        itemImage.setImageBitmap(cameraImgBitMap);
                    }
                    break;
            }


        }


    }
    private void uploadImageItems() {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token="Bearer"+" "+RefToken;
        JsonObject jsonObject1 = new JsonObject();
        jsonObject1.addProperty("refresh", RefToken);

        MultipartBody.Part itemImageFileMPBody = null;
        if (itemImageFile != null) {
            RequestBody photo_id = RequestBody.create(MediaType.parse("image/png"), itemImageFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", itemImageFile.getPath(), photo_id);
        }

        Call<ItemPhotoPath> call = (Call<ItemPhotoPath>) ApiClient2.getRetrofit().create(ApiPaths2.class).uploadImage(token,itemImageFileMPBody);
        call.enqueue(new Callback<ItemPhotoPath>() {
            @Override
            public void onResponse(Call<ItemPhotoPath> call, Response<ItemPhotoPath> response) {
                if (response.body() != null) {
                    ItemPhotoPath itemsDataMerchant = response.body();
                    if (itemsDataMerchant.isSuccess()) {
                        photoPath=itemsDataMerchant.getData();
                        //parseJSONItems(lIstModelList);
                    }else {
                        showToast(itemsDataMerchant.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<ItemPhotoPath> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
            }
        });

    }

    /*Method For Calling the Asynch Task for CRUD Operations*/
    //TODO:Add Item  TO ThorServer And WebAdminPAnnel
    private void dialogBoxForAddItem() {
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        addItemDialog = new Dialog(getContext());
        addItemDialog.setContentView(R.layout.dialoglayoutadditem);
        Objects.requireNonNull(addItemDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addItemDialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        addItemDialog.setCancelable(false);
        final EditText etCardTitle = addItemDialog.findViewById(R.id.et_card_title);
        final EditText etCardNumber = addItemDialog.findViewById(R.id.et_card_number);
        final EditText etCVV = addItemDialog.findViewById(R.id.et_cvv);
        final EditText etExpiryDate = addItemDialog.findViewById(R.id.et_expiry_date);
        final ImageView ivBack = addItemDialog.findViewById(R.id.iv_back_invoice);

        itemImage = addItemDialog.findViewById(R.id.itemImage);
//        itemImage.setImageResource(R.drawable.question2);
        itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
                }
                 //pickItemImage();
            }
        });
        Button btnCard = addItemDialog.findViewById(R.id.btn_add);

        //     progressBar = dialog.findViewById(R.id.progress_bar);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemDialog.dismiss();
            }
        });

        btnCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = etCardTitle.getText().toString();
                String itemQuantity = etCardNumber.getText().toString();
                String itemPrice = etCVV.getText().toString();
                String itemUpc = etExpiryDate.getText().toString();
                boolean status = true;
                if (!isImageGet) {
                    showToast("Insert Image");
                    status = false;
                    return;
                }
                if (itemName.isEmpty()) {
                    showToast("Item Name" + getString(R.string.empty));
                    status = false;
                    return;
                }
                if (itemQuantity.isEmpty()) {
                    showToast("Item Quantity" + getString(R.string.empty));
                    status = false;
                    return;
                }
                if (itemQuantity.equals("0") || itemQuantity.equals("0.0") || itemQuantity.equals("0.00") || itemQuantity.equals("0.000") || itemQuantity.equals("0.0000") || itemQuantity.equals("0.0000") || itemQuantity.equals("0.0000") || itemQuantity.equals("00") || itemQuantity.equals("000") || itemQuantity.equals("0000") || itemQuantity.equals("0000") || itemQuantity.equals("000000")) {

                    showToast("Item Quatitiy Never be 0");
                    status = false;
                    return;
                }
                if (itemPrice.equals("0") || itemPrice.equals("00") || itemPrice.equals("0.0") || itemPrice.equals("0.00") || itemPrice.equals("0.000") || itemPrice.equals("0.0000") || itemPrice.equals("0.00000") || itemPrice.equals("0.000000") || itemPrice.equals("0.000000")) {
                    showToast("Item Price Never be 0");
                    status = false;
                    return;
                }

                if (itemPrice.isEmpty()) {
                    showToast("Item Price" + getString(R.string.empty));
                    status = false;
                    return;
                }
                if (itemUpc.isEmpty()) {
                    showToast("Please add Item UPC");
                    status = false;
                    return;
                }
                //      progressBar.setVisibility(View.VISIBLE);
                if (status) {
                    addItem(itemName, itemQuantity, itemPrice, itemUpc);
                }

            }
        });
        addItemDialog.show();
    }

    private void addItem(String itemName, String itemQuantity, String itemPrice, String itemUpc) {
        itemName2 = itemName;
        itemPrice2 = itemPrice;
        itemQuantity2 = itemQuantity;
        itemUpc2 = itemUpc;
        //goToAddImageToWebAdminPanel(itemUpc2, itemName2, itemQuantity2, itemPrice2);
        if (!photoPath.equals("")){
            addNewItem(itemUpc2, itemName2, itemQuantity2, itemPrice2);
        }else {
            showToast("Please upload image first");
        }
    }
    private void addNewItem(String upc,String name,String qty,String price) {
        String RefToken = new CustomSharedPreferences().getvalueofRefresh("refreshToken", getContext());
        String token="Bearer"+" "+RefToken;
        JsonObject jsonObject1 = new JsonObject();
       // jsonObject1.addProperty("refresh", RefToken);
        jsonObject1.addProperty("name", name);
        jsonObject1.addProperty("upc", upc);
        jsonObject1.addProperty("quantity",  Float.parseFloat(qty));
        jsonObject1.addProperty("price",Float.parseFloat(price));
        jsonObject1.addProperty("img_path", photoPath);


        Call<AddItemsModel> call = (Call<AddItemsModel>) ApiClient2.getRetrofit().create(ApiPaths2.class).addInventoryItems(token,jsonObject1);
        call.enqueue(new Callback<AddItemsModel>() {
            @Override
            public void onResponse(Call<AddItemsModel> call, Response<AddItemsModel> response) {
                if (response.body() != null) {
                    AddItemsModel itemsDataMerchant = response.body();
                    if (itemsDataMerchant.isSuccess()) {
                        addItemDialog.dismiss();
                       // List<ItemLIstModel> lIstModelList=itemsDataMerchant.getList();
                        //refreshItemsAdapter(lIstModelList);
                        getAllItems();
                    }else {
                        showToast(itemsDataMerchant.getMessage());
                        addItemDialog.dismiss();
                    }
                }
            }
            @Override
            public void onFailure(Call<AddItemsModel> call, Throwable t) {
                Log.e("get-funding-nodes:", t.getMessage());
                addItemDialog.dismiss();
            }
        });

    }
    private void goToAddImageToWebAdminPanel(String itemUpc2, String name, String quantity, String price) {
        //reLoadItemsInList();
        //Image File : itemImageFile
        MultipartBody.Part itemImageFileMPBody = null;
        if (itemImageFile != null) {
            RequestBody photo_id = RequestBody.create(MediaType.parse("image/png"), itemImageFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", itemImageFile.getPath(), photo_id);
            int id = GlobalState.getInstance().getMerchantData().getId();

            RequestBody merchant_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(id));
            RequestBody item_name = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(name));
            RequestBody item_quantity = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(quantity));
            RequestBody item_price = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));

            RequestBody upc = RequestBody.create(MediaType.parse("text/plain"), itemUpc2);
            Call<AddImageResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).addItemImageToMerchant(merchant_id, upc, item_name, item_quantity, item_price, itemImageFileMPBody);
            call.enqueue(new Callback<AddImageResp>() {
                @Override
                public void onResponse(Call<AddImageResp> call, Response<AddImageResp> response) {

                    if (response.isSuccessful()) {
                        if (response.body() != null) {

                            if (response.body().getStatus().equals("success")) {
                                if (response.body().getMessage().equals("merchant data successfully inserted")) {

                                    reLoadItemsInList();
//                                    addItemTomerchantDbAlso();
//                                if(addItemDialog.isShowing()){
//                                    addItemDialog.dismiss();
//                                }
                                    showToast(response.body().getMessage());
                                } else if (response.body().getMessage().equals("upc Already exist")) {
                                    showToast(response.body().getMessage());
                                } else if (response.body().getMessage().equals("please upload a file")) {
                                    showToast(response.body().getMessage());
                                }
                            } else {

                            }
                        } else {

                        }
                    } else {

                    }

                }

                @Override
                public void onFailure(Call<AddImageResp> call, Throwable t) {
                    showToast("No");

                }
            });


        }
    }

    private void goToAddUpdateImageToWebAdminPanel(String itemUpc2, String name, String quantity, String price) {

        MultipartBody.Part itemImageFileMPBody = null;
        RequestBody photo_id = null;
        if (itemImageFile != null) {
            photo_id = RequestBody.create(MediaType.parse("image/png"), itemImageFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", itemImageFile.getPath(), photo_id);
        } else {
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", "");
        }
        int id = GlobalState.getInstance().getMerchantData().getId();
        RequestBody merchant_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(id));
        RequestBody item_name = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(name));
        RequestBody item_quantity = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(quantity));
        RequestBody item_price = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(price));

        RequestBody upc = RequestBody.create(MediaType.parse("text/plain"), itemUpc2);
        Call<AddImageResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).UpdateItemImageToMerchant(merchant_id, upc, item_name, item_quantity, item_price, itemImageFileMPBody);
        call.enqueue(new Callback<AddImageResp>() {
            @Override
            public void onResponse(Call<AddImageResp> call, Response<AddImageResp> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        if (response.body().getStatus().equals("success")) {
                            if (response.body().getMessage().equals("Merchant File has updated successfully")) {

                                reLoadItemsInList();
//                                    addItemTomerchantDbAlso();
//                                if(addItemDialog.isShowing()){
//                                    addItemDialog.dismiss();
//                                }
                                showToast(response.body().getMessage());
                            } else {

                            }
                        } else if (response.body().getStatus().equals("failed")) {
                            if (response.body().getMessage().equals("Please upload a file/image")) {
                                showToast(response.body().getMessage());
                            } else if (response.body().getMessage().equals("Merchant File not updated")) {
                                showToast("Invalid UPC!");
                            }
                        }
                    } else {

                    }
                } else {

                }

            }

            @Override
            public void onFailure(Call<AddImageResp> call, Throwable t) {
                showToast("No");

            }
        });


    }

    private void goToDELETEImageToWebAdminPanel(String itemUpc2) {

        int id = GlobalState.getInstance().getMerchantData().getId();
        RequestBody merchant_id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(id));
        RequestBody upc = RequestBody.create(MediaType.parse("text/plain"), itemUpc2);
        Call<AddImageResp> call = ApiClientBoost.getRetrofit().create(ApiPaths.class).DeleteItemImageToMerchant(merchant_id, upc);
        call.enqueue(new Callback<AddImageResp>() {
            @Override
            public void onResponse(Call<AddImageResp> call, Response<AddImageResp> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {

                        if (response.body().getStatus().equals("success")) {
                            if (response.body().getMessage().equals("merchant data successfully deleted")) {

                                reLoadItemsInList();
//
                                showToast(response.body().getMessage());
                            } else {

                            }
                        } else if (response.body().getStatus().equals("failed")) {
                            if (response.body().getMessage().equals("Please upload a file/image")) {
                                showToast(response.body().getMessage());
                            } else if (response.body().getMessage().equals("Merchant File not updated")) {
                                showToast("Invalid UPC!");
                            }
                        }
                    } else {

                    }
                } else {

                }

            }

            @Override
            public void onFailure(Call<AddImageResp> call, Throwable t) {
                showToast("No");

            }
        });


    }

    //TODO:Update Item TO ThorServer And WebAdminPAnnel
    /*Creating Confirmation Dialogue for Update Item Delete Item*/
    public void ask_UpdateItem(String mName, String mPrice, final String itemName, String mQuantity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.update_title));
        builder.setMessage(getString(R.string.update_subtitle));
        builder.setCancelable(true);
        final String keyName = mName;
        final String price = mPrice;
        final String quantity = mQuantity;
//        final  String description=mDescription;
        // Action if user selects 'yes'
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                updateItemfromInventory(keyName, price, quantity);
                goToAddUpdateImageToWebAdminPanel(keyName, itemName, quantity, price);

            }
        });

        // Actions if user selects 'no'
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //showToast("No");
            }

        });

        // Create the alert dialog using alert dialog builder
        AlertDialog dialog = builder.create();
        // Finally, display the dialog when user press back button
        dialog.show();
    }
    //TODO:Delete Item TO ThorServer And WebAdminPAnnel
    private void dialogBoxForDeleteItem() {

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialoglayoutdeleteitem);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (width / 1.1f), (int) (height / 1.3));
//        dialog.getWindow().setLayout(500, 500);
        final ImageView ivBack = dialog.findViewById(R.id.iv_back_invoice);
        Spinner dropdown = (Spinner) dialog.findViewById(R.id.spinner1);


        ArrayList<String> itemlist = new ArrayList<>();
        ArrayList<Items> iteminventoryList = new ArrayList<>();
        iteminventoryList = GlobalState.getInstance().getmDataSourceCheckOutInventory();
        itemlist.add("Select Delete item");
        if (iteminventoryList != null) {
            if (iteminventoryList.size() > 0) {
                for (int i = 0; i < iteminventoryList.size(); i++) {
                    itemlist.add(iteminventoryList.get(i).getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, itemlist);
                dropdown.setAdapter(adapter);
                final ArrayList<Items> finalIteminventoryList = iteminventoryList;
                dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        if (i == 0) {
                            // TODO:do nothing
                        } else {
                            //  showToast(finalIteminventoryList.get(i).getName());
                            int pos = i--;

                            Object objname = adapterView.getSelectedItem();
                            String itemName = String.valueOf(objname);
                            String itemSelectedUPC = "";
                            GlobalState.getInstance().setDelteItemPosition(pos);
                            ArrayList<Items> iteminventoryListtest = new ArrayList<>();
                            iteminventoryListtest = GlobalState.getInstance().getmDataSourceCheckOutInventory();

                            for (int ii = 0; ii < iteminventoryListtest.size(); ii++) {
                                if (iteminventoryListtest.get(ii).getName().equals(itemName)) {
                                    itemSelectedUPC = iteminventoryListtest.get(ii).getUPC();
                                }
                            }

                            GlobalState.getInstance().setDellSelectedItemName(itemName);
                            GlobalState.getInstance().setDellSelectedItemUPC(itemSelectedUPC);
                            ask_deleteItem(pos);

                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

            }
        }


        //  ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);


        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();


    }

    public void ask_deleteItem(int i) {
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View deleteDialogView = factory.inflate(R.layout.alertdialogdeletel_ayout, null);
        final AlertDialog deleteDialog = new AlertDialog.Builder(getContext()).create();
        deleteDialog.setView(deleteDialogView);
        deleteDialogView.findViewById(R.id.iv_back_invoice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });
        deleteDialogView.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//your business logic
                String itemUpc = GlobalState.getInstance().getDellSelectedItemUPC();
//                deleteItemFromInventory(1);
                goToDELETEImageToWebAdminPanel(itemUpc);
                deleteDialog.dismiss();
            }
        });
        deleteDialogView.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog.show();
    }
    private void refreshItemsAdapter(List<ItemLIstModel> list) {
        merchantItemAdapter = new MerchantItemAdapter(list, getContext(), MerchantFragment2.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(merchantItemAdapter);
    }
    private void parseJSONItems(List<ItemLIstModel> list) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Items>>() {
        }.getType();
        ArrayList<Items> itemsList = new ArrayList<>();
//        ArrayList<Items> itemsList = gson.fromJson(jsonString, type);
        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() != null) {
            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
        }

        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() == null) {

        } else {
            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
        }
        ArrayList<GetItemImageReloc> itemImageRelocArrayList = GlobalState.getInstance().getCurrentItemImageRelocArrayList();
        if (itemsList.isEmpty()) {
            for (int j = 0; j < itemImageRelocArrayList.size(); j++) {
                Items items = new Items();
                if (itemImageRelocArrayList.get(j).getUpc_number() != null) {
                    items.setUPC(itemImageRelocArrayList.get(j).getUpc_number());
                }
                if (itemImageRelocArrayList.get(j).getImage() != null) {
                    items.setImageUrl(itemImageRelocArrayList.get(j).getImage());
                }
                if (itemImageRelocArrayList.get(j).getName() != null) {
                    items.setName(itemImageRelocArrayList.get(j).getName());
                } else {
                    items.setName("Item name");
                }
                if (itemImageRelocArrayList.get(j).getQuantity() != null) {
                    items.setQuantity(itemImageRelocArrayList.get(j).getQuantity());
                } else {
                    items.setQuantity("1");
                }
                if (itemImageRelocArrayList.get(j).getPrice() != null) {
                    items.setPrice(itemImageRelocArrayList.get(j).getPrice());
                } else {
                    items.setPrice("0");
                }
                if (itemImageRelocArrayList.get(j).getTotal_price() != 0) {
                    items.setTotalPrice(itemImageRelocArrayList.get(j).getTotal_price());
                }
                if (itemImageRelocArrayList.get(j).getImage_in_hex() != null) {
                    items.setImageInHex(itemImageRelocArrayList.get(j).getImage_in_hex());
                }
                if (itemImageRelocArrayList.get(j).getAdditional_info() != null) {
                    items.setAdditionalInfo(itemImageRelocArrayList.get(j).getAdditional_info());
                }

                itemsList.add(j, items);
            }

        } else {

        }

        GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsList);
        for (Items items : itemsList) {
            Log.e("ItemsDetails", "Name:" + items.getName() + "-" + "Quantity:" + items.getQuantity() + "-" + "Price:" + items.getPrice() + "-" + "UPC:" + items.getUPC() + "-" + "ImageUrl:" + items.getImageUrl());
        }
        refreshAdapter();

//        getImagesOfItems();
    }

    /*Get ItemsList JSON Response and convert in Dictionary*/
    private void parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Items>>() {
        }.getType();
        ArrayList<Items> itemsList = new ArrayList<>();
//        ArrayList<Items> itemsList = gson.fromJson(jsonString, type);
        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() != null) {
            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
        }

        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() == null) {

        } else {
            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
        }
        ArrayList<GetItemImageReloc> itemImageRelocArrayList = GlobalState.getInstance().getCurrentItemImageRelocArrayList();
        if (itemsList.isEmpty()) {
            for (int j = 0; j < itemImageRelocArrayList.size(); j++) {
                Items items = new Items();
                if (itemImageRelocArrayList.get(j).getUpc_number() != null) {
                    items.setUPC(itemImageRelocArrayList.get(j).getUpc_number());
                }
                if (itemImageRelocArrayList.get(j).getImage() != null) {
                    items.setImageUrl(itemImageRelocArrayList.get(j).getImage());
                }
                if (itemImageRelocArrayList.get(j).getName() != null) {
                    items.setName(itemImageRelocArrayList.get(j).getName());
                } else {
                    items.setName("Item name");
                }
                if (itemImageRelocArrayList.get(j).getQuantity() != null) {
                    items.setQuantity(itemImageRelocArrayList.get(j).getQuantity());
                } else {
                    items.setQuantity("1");
                }
                if (itemImageRelocArrayList.get(j).getPrice() != null) {
                    items.setPrice(itemImageRelocArrayList.get(j).getPrice());
                } else {
                    items.setPrice("0");
                }
                if (itemImageRelocArrayList.get(j).getTotal_price() != 0) {
                    items.setTotalPrice(itemImageRelocArrayList.get(j).getTotal_price());
                }
                if (itemImageRelocArrayList.get(j).getImage_in_hex() != null) {
                    items.setImageInHex(itemImageRelocArrayList.get(j).getImage_in_hex());
                }
                if (itemImageRelocArrayList.get(j).getAdditional_info() != null) {
                    items.setAdditionalInfo(itemImageRelocArrayList.get(j).getAdditional_info());
                }

                itemsList.add(j, items);
            }

        } else {

        }

        GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsList);
        for (Items items : itemsList) {
            Log.e("ItemsDetails", "Name:" + items.getName() + "-" + "Quantity:" + items.getQuantity() + "-" + "Price:" + items.getPrice() + "-" + "UPC:" + items.getUPC() + "-" + "ImageUrl:" + items.getImageUrl());
        }
        refreshAdapter();

//        getImagesOfItems();
    }

    private void refreshAdapter() {
        ArrayList<Items> itemsArrayList = GlobalState.getInstance().getmDataSourceCheckOutInventory();
       // merchantItemAdapter = new MerchantItemAdapter(itemsArrayList, getContext(), MerchantFragment2.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(merchantItemAdapter);
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
//             luqman comment               GlobalState.getInstance().setCurrentSpecificRateData(cSRDtemp);
                            GlobalState.getInstance().setCurrentAllRate(response.body());
                            // sharedPreferences.setCurrentSpecificRateData(cSRDtemp,"CurrentSpecificRateData",getContext());
//                  luqman comment          setcurrentrate(String.valueOf(cSRDtemp.getRateinbitcoin()));
                            // Log.d("CurrentRate",String.valueOf(GlobalState.getInstance().getCurrentSpecificRateData().getRateinbitcoin()));
//                            Log.d("CurrentRate2", String.valueOf(cSRDtemp.getRateinbitcoin()));

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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /*For Reload The Items List after getting Response from Server*/
    public void reLoadItemsInList() {
        MerchantData merchantData = GlobalState.getInstance().getMerchantData();
        if (merchantData != null) {
            //getAllItemsImageList(merchantData);
            getAllItems();
        } else {

        }

    }

    /*For Reload Adapter after getting Item List*/
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
            mMerchantItemsDataSource = itemsArrayList;
            if (merchantItemAdapter != null) {
               // merchantItemAdapter.updateList(mMerchantItemsDataSource);
                merchantItemAdapter.notifyDataSetChanged();
            } else {

                //merchantItemAdapter = new MerchantItemAdapter(mMerchantItemsDataSource, getContext(), MerchantFragment2.this);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(merchantItemAdapter);
            }
        }

    }

    public static File savebitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "testimage.jpg");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
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

}
