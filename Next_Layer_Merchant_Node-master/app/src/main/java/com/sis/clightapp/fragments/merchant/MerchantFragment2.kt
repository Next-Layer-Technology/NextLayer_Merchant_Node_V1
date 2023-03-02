package com.sis.clightapp.fragments.merchant

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sis.clightapp.Interface.ApiPaths2
import com.sis.clightapp.Interface.Webservice
import com.sis.clightapp.Network.CheckNetwork
import com.sis.clightapp.R
import com.sis.clightapp.adapter.MerchantItemAdapter
import com.sis.clightapp.model.GsonModel.Items
import com.sis.clightapp.model.GsonModel.ItemsMerchant.AddItemsModel
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemLIstModel
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemPhotoPath
import com.sis.clightapp.model.GsonModel.ItemsMerchant.ItemsDataMerchant
import com.sis.clightapp.model.GsonModel.StringImageOfUPCItem
import com.sis.clightapp.model.ImageRelocation.AddImageResp
import com.sis.clightapp.services.SessionService
import com.sis.clightapp.session.MyLogOutService
import com.sis.clightapp.util.CustomSharedPreferences
import com.sis.clightapp.util.GlobalState
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.koin.android.ext.android.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tech.gusavila92.websocketclient.WebSocketClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MerchantFragment2 : MerchantBaseFragment() {
    private val apiClient: ApiPaths2 by inject()
    private val webservice: Webservice by inject()
    private val sessionService: SessionService by inject()

    private lateinit var additem: Button
    private lateinit var deleteitem: Button
    private lateinit var inventorybtn: Button
    var TAG = "CLighting App"
    var itemImageFile: File? = null
    var file: String? = null
    var photoPath = ""
    private lateinit var webSocketClient: WebSocketClient
    var btmapimage: Bitmap? = null
    private lateinit var dialog: Dialog
    private lateinit var addItemDialog: Dialog
    lateinit var refreshProgressDialog: ProgressDialog
    lateinit var addItemprogressDialog: ProgressDialog
    lateinit var addItemImageStringProgressDialog: ProgressDialog
    lateinit var deleteItemProgressBar: ProgressDialog
    lateinit var updateProgressBar: ProgressDialog
    lateinit var exitFromServerProgressDialog: ProgressDialog
    lateinit var recyclerView: RecyclerView
    var merchantItemAdapter: MerchantItemAdapter? = null
    var intScreenWidth = 0
    var intScreenHeight = 0
    var CurrentRateInBTC = 0.0
    var upcsl = ""
    lateinit var itemImage: CircleImageView
    lateinit var selectImgBitMap: Bitmap
    lateinit var cameraImgBitMap: Bitmap
    var cursize = 0
    var totalSize = 0
    var mDataSourceImageString: ArrayList<StringImageOfUPCItem>? = null
    var x: String? = null
    var itemName2: String = ""
    var itemPrice2: String = ""
    var itemQuantity2: String = ""
    var itemUpc2: String = ""
    var listModelList: MutableList<ItemLIstModel> = ArrayList()
    lateinit var setTextWithSpan: TextView
    var isImageGet = false
    var isInApp = true

    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(Intent(context, MyLogOutService::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
           // pickItemImage();
            //imageOptions()
            isInApp = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_merchant2, container, false)
        setTextWithSpan = view.findViewById(R.id.poweredbyimage)
        val boldStyle = StyleSpan(Typeface.BOLD)
        setTextWithSpan(
            setTextWithSpan,
            getString(R.string.welcome_text),
            getString(R.string.welcome_text_bold),
            boldStyle
        )
        exitFromServerProgressDialog = ProgressDialog(context)
        exitFromServerProgressDialog.setMessage("Exiting")
        addItemprogressDialog = ProgressDialog(context)
        addItemprogressDialog.setMessage("Adding...")
        refreshProgressDialog = ProgressDialog(context)
        refreshProgressDialog.setMessage("Realoding")
        deleteItemProgressBar = ProgressDialog(context)
        deleteItemProgressBar.setMessage("Deleting...")
        updateProgressBar = ProgressDialog(context)
        updateProgressBar.setMessage("Updating...")
        addItemImageStringProgressDialog = ProgressDialog(context)
        addItemImageStringProgressDialog.setMessage("Add Image..")
        intScreenWidth = Resources.getSystem().displayMetrics.widthPixels
        intScreenHeight = Resources.getSystem().displayMetrics.heightPixels
        additem = view.findViewById(R.id.imageView5)
        deleteitem = view.findViewById(R.id.imageView7)
        recyclerView = view.findViewById(R.id.merchant2listview)
        inventorybtn = view.findViewById(R.id.inventrytxt)
        inventorybtn.setOnClickListener {
            refreshProgressDialog.show()
            refreshProgressDialog.setCancelable(false)
            refreshProgressDialog.setCanceledOnTouchOutside(false)
            val handler = Handler()
            handler.postDelayed(
                { refreshProgressDialog.dismiss() },
                2000
            ) // 3000 milliseconds delay
            refreshList()
        }
        additem.setOnClickListener { dialogBoxForAddItem() }
        deleteitem.setOnClickListener { dialogBoxForDeleteItem() }
        if (CheckNetwork.isInternetAvailable(context)) {
            refreshList()
        }
        return view
    }

    private fun refreshList() {
            val RefToken = CustomSharedPreferences().getvalueofRefresh("refreshToken", context)
            val token = "Bearer $RefToken"
            val jsonObject1 = JsonObject()
            jsonObject1.addProperty("refresh", RefToken)
            val call = apiClient.getInventoryItems(token) as Call<ItemsDataMerchant>
            call.enqueue(object : Callback<ItemsDataMerchant?> {
                override fun onResponse(call: Call<ItemsDataMerchant?>, response: Response<ItemsDataMerchant?>) {
                    Log.i("get-funding-nodes:", response.toString())
                    if (response.body() != null) {
                        val itemsDataMerchant = response.body()!!
                        if (itemsDataMerchant.success) {
                            showToast(itemsDataMerchant.message)
                            val lIstModelList = itemsDataMerchant.list
                            listModelList.clear()
                            listModelList = itemsDataMerchant.list
                            refreshItemsAdapter(lIstModelList as ArrayList<ItemLIstModel>)
                        }
                    }
                }

                override fun onFailure(call: Call<ItemsDataMerchant?>, t: Throwable) {
                    Log.e("get-funding-nodes:", t.message.toString())
                }
            })
        }

    fun dialogBoxForUpdateDelItem(itemLIstModel: ItemLIstModel) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutupdateitem2)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        val edtUpdateName = dialog.findViewById<EditText>(R.id.edtUpdateName)
        val edtUpdatePrice = dialog.findViewById<EditText>(R.id.edtUpdatePrice)
        val edtUpdateQuanity = dialog.findViewById<EditText>(R.id.edtUpdateQuantity)
        val itemUpc = ""
        val btnUpdate = dialog.findViewById<Button>(R.id.btnUpdate2) //btnUpdate2
        val btnDelete = dialog.findViewById<Button>(R.id.btnDelete)
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        edtUpdateName.setText(itemLIstModel.name)
        edtUpdatePrice.setText(itemLIstModel.unit_price)
        edtUpdateQuanity.setText(itemLIstModel.quantity_left)
        btnUpdate.setOnClickListener {
            val updateNameValue = edtUpdateName.text.toString()
            val updatePriceValue = edtUpdatePrice.text.toString()
            val updateQuaitiyValue = edtUpdateQuanity.text.toString()
            if (updatePriceValue.isEmpty() || updateQuaitiyValue.isEmpty()) {
                showToast("Not Valid")
            } else {
                dialog.dismiss()
                ask_UpdateItemConfirmation(updateNameValue, updatePriceValue, updateQuaitiyValue, itemLIstModel)
            }
        }
        btnDelete.setOnClickListener {
            dialog.dismiss()
            ask_UpdateItemDelete(itemLIstModel.id)
        }
        ivBack.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun ask_UpdateItemConfirmation(name: String, price: String, quantity: String, itemLIstModel: ItemLIstModel) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.update_title))
        builder.setMessage(getString(R.string.update_subtitle))
        builder.setCancelable(true)
        builder.setPositiveButton("Yes") { dialogInterface, i -> updateItem(itemLIstModel.id, name, quantity, price) }
        builder.show()
    }

    fun ask_UpdateItemDelete(id: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.delete_title))
        builder.setMessage(getString(R.string.delete_subtitle))
        builder.setCancelable(true)
        builder.setPositiveButton("Yes") { dialogInterface, i -> deleteItem(id) }
        builder.show()
    }

    private fun updateItem(id: String, name: String, qty: String, price: String) {
        val RefToken = CustomSharedPreferences().getvalueofRefresh("refreshToken", context)
        val token = "Bearer $RefToken"
        val jsonObject1 = JsonObject()
        // jsonObject1.addProperty("refresh", RefToken);
        jsonObject1.addProperty("name", name)
        // jsonObject1.addProperty("upc", upc);
        jsonObject1.addProperty("quantity", qty.toFloat())
        jsonObject1.addProperty("price", price.toFloat())
        // jsonObject1.addProperty("img_path", photoPath);
        val call = apiClient.updateInventoryItems(token, id.toInt(), jsonObject1) as Call<AddItemsModel>
        call.enqueue(object : Callback<AddItemsModel?> {
            override fun onResponse(call: Call<AddItemsModel?>, response: Response<AddItemsModel?>) {
                if (response.body() != null) {
                    val itemsDataMerchant = response.body()!!
                    if (itemsDataMerchant.isSuccess) {
                        showToast(itemsDataMerchant.message)
                        refreshList()
                    } else {
                        showToast(itemsDataMerchant.message)
                    }
                }
            }

            override fun onFailure(call: Call<AddItemsModel?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message.toString())
                refreshList()
            }
        })
    }

    private fun deleteItem(id: String) {
        val RefToken = CustomSharedPreferences().getvalueofRefresh("refreshToken", context)
        val token = "Bearer $RefToken"
        val call = apiClient.deleteInventoryItems(token, id.toInt()) as Call<AddItemsModel>
        call.enqueue(object : Callback<AddItemsModel?> {
            override fun onResponse(call: Call<AddItemsModel?>, response: Response<AddItemsModel?>) {
                if (response.body() != null) {
                    val itemsDataMerchant = response.body()!!
                    if (itemsDataMerchant.isSuccess) {
                        showToast(itemsDataMerchant.message)
                        refreshList()
                    } else {
                        showToast(itemsDataMerchant.message)
                        refreshList()
                    }
                }
            }

            override fun onFailure(call: Call<AddItemsModel?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message.toString())
                refreshList()
            }
        })
    }
    private fun imageOptions() {
            val items = arrayOf<CharSequence>("Camera", "Gallery", "Cancel")
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Add Image From")
            builder.setItems(items) { dialogInterface, i ->
                if (items[i] == "Camera") {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_REQ)
                }
                if (items[i] == "Gallery") {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY_REQUEST)
                }
                dialogInterface.dismiss()
            }
            builder.setCancelable(false)
            builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null)
            return
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST -> {
                    val selectedImage = data.data
                    try {
                        selectImgBitMap =
                            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImage)
                    } catch (e: IOException) {
                        Log.i("TAG", "Some exception $e")
                    }
                    var file: File? = null
                    try {
                        file = requireContext().savebitmap(selectImgBitMap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        isImageGet = false
                    }
                    if (file != null) {
                        isImageGet = true
                        itemImageFile = file
                        uploadImageItems()
                        itemImage.setImageBitmap(selectImgBitMap)
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
                }

                CAMERA_REQ -> {
                    val bundle = data.extras
                    cameraImgBitMap = bundle?.get("data") as Bitmap
                    var file2: File? = null
                    try {
                        file2 = requireContext().savebitmap(cameraImgBitMap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        isImageGet = false
                    }
                    if (file2 != null) {
                        isImageGet = true
                        itemImageFile = file2
                        uploadImageItems()
                        itemImage.setImageBitmap(cameraImgBitMap)
                    }
                }
            }
        }
    }

    private fun uploadImageItems() {
        val token = CustomSharedPreferences().getvalueofRefresh("refreshToken", context)
        val bearer = "Bearer $token"
        val jsonObject1 = JsonObject()
        jsonObject1.addProperty("refresh", bearer)
        var itemImageFileMPBody: MultipartBody.Part? = null
        if (itemImageFile != null) {
            val photoId: RequestBody = RequestBody.create("image/png".toMediaTypeOrNull(), itemImageFile!!)
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", itemImageFile!!.path, photoId)
        }
        val call = apiClient.uploadImage(bearer, itemImageFileMPBody) as Call<ItemPhotoPath>
        call.enqueue(object : Callback<ItemPhotoPath?> {
            override fun onResponse(call: Call<ItemPhotoPath?>, response: Response<ItemPhotoPath?>) {
                if (response.body() != null) {
                    val itemsDataMerchant = response.body()!!
                    if (itemsDataMerchant.isSuccess) {
                        photoPath = itemsDataMerchant.data
                        //parseJSONItems(lIstModelList);
                    } else {
                        showToast(itemsDataMerchant.message)
                    }
                }
            }

            override fun onFailure(call: Call<ItemPhotoPath?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message.toString())
            }
        })
    }

    /*Method For Calling the Asynch Task for CRUD Operations*/ //TODO:Add Item  TO ThorServer And WebAdminPAnnel
    private fun dialogBoxForAddItem() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        addItemDialog = Dialog(requireContext())
        addItemDialog.setContentView(R.layout.dialoglayoutadditem)
        addItemDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addItemDialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        //        dialog.getWindow().setLayout(500, 500);
        addItemDialog.setCancelable(false)
        val etCardTitle = addItemDialog.findViewById<EditText>(R.id.et_card_title)
        val etCardNumber = addItemDialog.findViewById<EditText>(R.id.et_card_number)
        val etCVV = addItemDialog.findViewById<EditText>(R.id.et_cvv)
        val etExpiryDate = addItemDialog.findViewById<EditText>(R.id.et_expiry_date)
        val ivBack = addItemDialog.findViewById<ImageView>(R.id.iv_back_invoice)
        itemImage = addItemDialog.findViewById(R.id.itemImage)
        itemImage.setOnClickListener {
            imageOptions()
        }
        val btnCard = addItemDialog.findViewById<Button>(R.id.btn_add)
        ivBack.setOnClickListener { addItemDialog.dismiss() }
        btnCard.setOnClickListener(View.OnClickListener {
            val itemName = etCardTitle.text.toString()
            val itemQuantity = etCardNumber.text.toString()
            val itemPrice = etCVV.text.toString()
            val itemUpc = etExpiryDate.text.toString()
            if (!isImageGet) {
                showToast("Insert Image")
                return@OnClickListener
            }
            if (itemName.isEmpty()) {
                showToast("Item Name" + getString(R.string.empty))
                return@OnClickListener
            }
            if (itemQuantity.isEmpty()) {
                showToast("Item Quantity" + getString(R.string.empty))
                return@OnClickListener
            }
            if (itemQuantity == "0" || itemQuantity == "0.0" || itemQuantity == "0.00" || itemQuantity == "0.000" || itemQuantity == "0.0000" || itemQuantity == "0.0000" || itemQuantity == "0.0000" || itemQuantity == "00" || itemQuantity == "000" || itemQuantity == "0000" || itemQuantity == "0000" || itemQuantity == "000000") {
                showToast("Item Quatitiy Never be 0")
                return@OnClickListener
            }
            if (itemPrice == "0" || itemPrice == "00" || itemPrice == "0.0" || itemPrice == "0.00" || itemPrice == "0.000" || itemPrice == "0.0000" || itemPrice == "0.00000" || itemPrice == "0.000000" || itemPrice == "0.000000") {
                showToast("Item Price Never be 0")
                return@OnClickListener
            }
            if (itemPrice.isEmpty()) {
                showToast("Item Price" + getString(R.string.empty))
                return@OnClickListener
            }
            if (itemUpc.isEmpty()) {
                showToast("Please add Item UPC")
                return@OnClickListener
            }
            addItem(itemName, itemQuantity, itemPrice, itemUpc)
        })
        addItemDialog.show()
    }

    private fun addItem(itemName: String, itemQuantity: String, itemPrice: String, itemUpc: String) {
        itemName2 = itemName
        itemPrice2 = itemPrice
        itemQuantity2 = itemQuantity
        itemUpc2 = itemUpc
        //goToAddImageToWebAdminPanel(itemUpc2, itemName2, itemQuantity2, itemPrice2);
        if (photoPath != "") {
            addNewItem(itemUpc2, itemName2, itemQuantity2, itemPrice2)
        } else {
            showToast("Please upload image first")
        }
    }

    private fun addNewItem(upc: String, name: String, qty: String, price: String) {
        val RefToken = CustomSharedPreferences().getvalueofRefresh("refreshToken", context)
        val token = "Bearer $RefToken"
        val jsonObject1 = JsonObject()
        // jsonObject1.addProperty("refresh", RefToken);
        jsonObject1.addProperty("name", name)
        jsonObject1.addProperty("upc", upc)
        jsonObject1.addProperty("quantity", qty.toFloat())
        jsonObject1.addProperty("price", price.toFloat())
        jsonObject1.addProperty("img_path", photoPath)
        val call = apiClient.addInventoryItems(token, jsonObject1) as Call<AddItemsModel>
        call.enqueue(object : Callback<AddItemsModel?> {
            override fun onResponse(call: Call<AddItemsModel?>, response: Response<AddItemsModel?>) {

                if (response.body() != null) {
                    val itemsDataMerchant = response.body()!!
                    if (itemsDataMerchant.isSuccess) {
                        addItemDialog.dismiss()
                        // List<ItemLIstModel> lIstModelList=itemsDataMerchant.getList();
                        //refreshItemsAdapter(lIstModelList);
                        refreshList()
                    } else {
                        showToast(itemsDataMerchant.message)
                        addItemDialog.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<AddItemsModel?>, t: Throwable) {
                Log.e("get-funding-nodes:", t.message.toString())
                addItemDialog.dismiss()
                refreshList()
            }
        })
    }

    private fun goToAddUpdateImageToWebAdminPanel(itemUpc2: String, name: String, quantity: String, price: String) {
        var itemImageFileMPBody: MultipartBody.Part? = null
        var photoId: RequestBody? = null
        if (itemImageFile != null) {
            photoId = RequestBody.create("image/png".toMediaTypeOrNull(), itemImageFile!!)
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", itemImageFile!!.path, photoId)
        } else {
            itemImageFileMPBody = MultipartBody.Part.createFormData("file", "")
        }
        val merchant_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), id.toString())
        val item_name: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val item_quantity: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), quantity)
        val item_price: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), price)
        val upc: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), itemUpc2)
        val call: Call<AddImageResp> = webservice.UpdateItemImageToMerchant(
            merchant_id,
            upc,
            item_name,
            item_quantity,
            item_price,
            itemImageFileMPBody
        )
        call.enqueue(object : Callback<AddImageResp?> {
            override fun onResponse(call: Call<AddImageResp?>, response: Response<AddImageResp?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()?.status == "success") {
                            if (response.body()?.message == "Merchant File has updated successfully") {
                                reLoadItemsInList()
                                showToast(response.body()?.message.toString())
                            }
                        } else if (response.body()?.status == "failed") {
                            if (response.body()?.message == "Please upload a file/image") {
                                showToast(response.body()?.message)
                            } else if (response.body()?.message == "Merchant File not updated") {
                                showToast("Invalid UPC!")
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AddImageResp?>, t: Throwable) {
                showToast("No")
            }
        })
    }

    private fun goToDELETEImageToWebAdminPanel(itemUpc2: String) {
        sessionService.getMerchantData()?.let {
            val merchantId: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), it.id.toString())
            val upc: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), itemUpc2)
            val call: Call<AddImageResp> = webservice.DeleteItemImageToMerchant(merchantId, upc)
            call.enqueue(object : Callback<AddImageResp?> {
                override fun onResponse(call: Call<AddImageResp?>, response: Response<AddImageResp?>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if (response.body()?.status == "success") {
                                if (response.body()?.message == "merchant data successfully deleted") {
                                    reLoadItemsInList()
                                    //
                                    showToast(response.body()?.message)
                                }
                            } else if (response.body()?.status == "failed") {
                                if (response.body()?.message == "Please upload a file/image") {
                                    showToast(response.body()?.message)
                                } else if (response.body()?.message == "Merchant File not updated") {
                                    showToast("Invalid UPC!")
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<AddImageResp?>, t: Throwable) {
                    showToast("No")
                }
            })
        }

    }

    private fun dialogBoxForDeleteItem() {
        val width = Resources.getSystem().displayMetrics.widthPixels
        val height = Resources.getSystem().displayMetrics.heightPixels
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialoglayoutdeleteitem)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout((width / 1.1f).toInt(), (height / 1.3).toInt())
        val ivBack = dialog.findViewById<ImageView>(R.id.iv_back_invoice)
        val dropdown = dialog.findViewById<View>(R.id.spinner1) as Spinner
        val itemlist = ArrayList<String>()
        val iteminventoryList = ArrayList<Items>()
        itemlist.add("Select Delete item")
        if (iteminventoryList.size > 0) {
            for (i in iteminventoryList.indices) {
                itemlist.add(iteminventoryList[i].name)
            }
            val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, itemlist)
            dropdown.adapter = adapter
            dropdown.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                    var i = i
                    if (i == 0) {
                        // TODO:do nothing
                    } else {
                        //  showToast(finalIteminventoryList.get(i).getName());
                        val pos = i--
                        val objname = adapterView.selectedItem
                        val itemName = objname.toString()
                        var itemSelectedUPC = ""
                        GlobalState.getInstance().setDelteItemPosition(pos)
                        val iteminventoryListtest = ArrayList<Items>()
                        //                            iteminventoryListtest = GlobalState.getInstance().getmDataSourceCheckOutInventory();
                        for (ii in iteminventoryListtest.indices) {
                            if (iteminventoryListtest[ii].name == itemName) {
                                itemSelectedUPC = iteminventoryListtest[ii].upc
                            }
                        }
                        GlobalState.getInstance().dellSelectedItemUPC = itemSelectedUPC
                        ask_deleteItem(pos)
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        }


        //  ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        ivBack.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun ask_deleteItem(i: Int) {
        val factory = LayoutInflater.from(context)
        val deleteDialogView = factory.inflate(R.layout.alertdialogdeletel_ayout, null)
        val deleteDialog = AlertDialog.Builder(context).create()
        deleteDialog.setView(deleteDialogView)
        deleteDialogView.findViewById<View>(R.id.iv_back_invoice).setOnClickListener { deleteDialog.dismiss() }
        deleteDialogView.findViewById<View>(R.id.btn_yes).setOnClickListener { //your business logic
            val itemUpc = GlobalState.getInstance().dellSelectedItemUPC
            //                deleteItemFromInventory(1);
            goToDELETEImageToWebAdminPanel(itemUpc)
            deleteDialog.dismiss()
        }
        deleteDialogView.findViewById<View>(R.id.btn_no).setOnClickListener { deleteDialog.dismiss() }
        deleteDialog.show()
    }

    private fun refreshItemsAdapter(list: ArrayList<ItemLIstModel>) {
        merchantItemAdapter = MerchantItemAdapter(list, requireContext(), this@MerchantFragment2)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = merchantItemAdapter
    }

    private fun parseJSONItems(list: List<ItemLIstModel>) {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Items?>?>() {}.type
        val itemsList = ArrayList<Items>()
        //        ArrayList<Items> itemsList = gson.fromJson(jsonString, type);
//        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() != null) {
//            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
//        }
//
//        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() == null) {
//
//        } else {
//            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
//        }
        val itemImageRelocArrayList = GlobalState.getInstance().currentItemImageRelocArrayList
        if (itemsList.isEmpty()) {
            for (j in itemImageRelocArrayList.indices) {
                val items = Items()
                if (itemImageRelocArrayList[j].upc_number != null) {
                    items.upc = itemImageRelocArrayList[j].upc_number
                }
                if (itemImageRelocArrayList[j].image != null) {
                    items.imageUrl = itemImageRelocArrayList[j].image
                }
                if (itemImageRelocArrayList[j].name != null) {
                    items.name = itemImageRelocArrayList[j].name
                } else {
                    items.name = "Item name"
                }
                if (itemImageRelocArrayList[j].quantity != null) {
                    items.quantity = itemImageRelocArrayList[j].quantity
                } else {
                    items.quantity = "1"
                }
                if (itemImageRelocArrayList[j].price != null) {
                    items.price = itemImageRelocArrayList[j].price
                } else {
                    items.price = "0"
                }
                if (itemImageRelocArrayList[j].total_price != 0.0) {
                    items.totalPrice = itemImageRelocArrayList[j].total_price
                }
                if (itemImageRelocArrayList[j].image_in_hex != null) {
                    items.imageInHex = itemImageRelocArrayList[j].image_in_hex
                }
                if (itemImageRelocArrayList[j].additional_info != null) {
                    items.additionalInfo = itemImageRelocArrayList[j].additional_info
                }
                itemsList.add(j, items)
            }
        } else {
        }

//        GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsList);
        for (items in itemsList) {
            Log.e(
                "ItemsDetails",
                "Name:" + items.name + "-" + "Quantity:" + items.quantity + "-" + "Price:" + items.price + "-" + "UPC:" + items.upc + "-" + "ImageUrl:" + items.imageUrl
            )
        }
        refreshAdapter()

//        getImagesOfItems();
    }

    /*Get ItemsList JSON Response and convert in Dictionary*/
    private fun parseJSON(jsonString: String) {
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Items?>?>() {}.type
        val itemsList = ArrayList<Items>()
        //        ArrayList<Items> itemsList = gson.fromJson(jsonString, type);
//        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() != null) {
//            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
//        }
//
//        if (GlobalState.getInstance().getmDataSourceCheckOutInventory() == null) {
//
//        } else {
//            GlobalState.getInstance().getmDataSourceCheckOutInventory().clear();
//        }
        val itemImageRelocArrayList = GlobalState.getInstance().currentItemImageRelocArrayList
        if (itemsList.isEmpty()) {
            for (j in itemImageRelocArrayList.indices) {
                val items = Items()
                if (itemImageRelocArrayList[j].upc_number != null) {
                    items.upc = itemImageRelocArrayList[j].upc_number
                }
                if (itemImageRelocArrayList[j].image != null) {
                    items.imageUrl = itemImageRelocArrayList[j].image
                }
                if (itemImageRelocArrayList[j].name != null) {
                    items.name = itemImageRelocArrayList[j].name
                } else {
                    items.name = "Item name"
                }
                if (itemImageRelocArrayList[j].quantity != null) {
                    items.quantity = itemImageRelocArrayList[j].quantity
                } else {
                    items.quantity = "1"
                }
                if (itemImageRelocArrayList[j].price != null) {
                    items.price = itemImageRelocArrayList[j].price
                } else {
                    items.price = "0"
                }
                if (itemImageRelocArrayList[j].total_price != 0.0) {
                    items.totalPrice = itemImageRelocArrayList[j].total_price
                }
                if (itemImageRelocArrayList[j].image_in_hex != null) {
                    items.imageInHex = itemImageRelocArrayList[j].image_in_hex
                }
                if (itemImageRelocArrayList[j].additional_info != null) {
                    items.additionalInfo = itemImageRelocArrayList[j].additional_info
                }
                itemsList.add(j, items)
            }
        } else {
        }

//        GlobalState.getInstance().setmDataSourceCheckOutInventory(itemsList);
        for (items in itemsList) {
            Log.e(
                "ItemsDetails",
                "Name:" + items.name + "-" + "Quantity:" + items.quantity + "-" + "Price:" + items.price + "-" + "UPC:" + items.upc + "-" + "ImageUrl:" + items.imageUrl
            )
        }
        refreshAdapter()

//        getImagesOfItems();
    }

    private fun refreshAdapter() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = merchantItemAdapter
    }

    /*For Reload The Items List after getting Response from Server*/
    fun reLoadItemsInList() {
        sessionService.getMerchantData()?.let {
            refreshList()
        }
    }

    companion object {
        const val GALLERY_REQUEST = 1
        const val REQUEST_WRITE_PERMISSION = 786
        private const val CAMERA_REQ = 12
        fun round(value: Double, places: Int): Double {
            var value = value
            require(places >= 0)
            val factor = Math.pow(10.0, places.toDouble()).toLong()
            value = value * factor
            val tmp = Math.round(value)
            return tmp.toDouble() / factor
        }

        @Throws(IOException::class)
        fun Context.savebitmap(bmp: Bitmap?): File {
            val bytes = ByteArrayOutputStream()
            bmp?.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
            val f = createFileInCache("testimage.jpg")
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            fo.close()
            return f
        }
        private fun Context.createFileInCache(fileName: String): File = File(cacheDir, fileName)
    }
}