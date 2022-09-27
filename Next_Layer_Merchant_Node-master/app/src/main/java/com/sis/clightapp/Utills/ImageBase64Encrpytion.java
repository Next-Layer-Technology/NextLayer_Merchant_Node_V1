package com.sis.clightapp.Utills;

/*
By!
khuwaja Hassan 01-09-2020
*/

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import android.util.Log;

import java.io.ByteArrayOutputStream;

public class ImageBase64Encrpytion {
    //Decode Image
    public static Bitmap Base64StringToBitMap(String base64String) {
        byte[] decodedBytes=null;
        try{
             decodedBytes = Base64.decode(base64String,Base64.DEFAULT);
        }
      catch (Exception e){
          Log.e("DecodeError",e.getMessage());
}

        Bitmap bitmap=null;
        if(decodedBytes!=null)
        {

            bitmap=BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }
        else
        {
            bitmap=null;
        }
        return bitmap;
    }


    //Gernal MEthod ENcode Image
    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static String bitMapToBase64String(Bitmap bitmap){
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        byte[] byteArray = byteStream.toByteArray();
        String baseString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return baseString;
    }
}
