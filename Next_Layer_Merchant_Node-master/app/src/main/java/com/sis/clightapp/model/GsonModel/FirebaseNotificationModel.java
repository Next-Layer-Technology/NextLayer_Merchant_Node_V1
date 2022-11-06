package com.sis.clightapp.model.GsonModel;

import android.os.Parcel;
import android.os.Parcelable;

public class FirebaseNotificationModel  implements Parcelable {
    private String update_type;
    private String title;
    private String body;
    private String invoice_label;

    public FirebaseNotificationModel() {
    }

    public FirebaseNotificationModel(String update_type, String title, String body, String invoice_label) {
        this.update_type = update_type;
        this.title = title;
        this.body = body;
        this.invoice_label = invoice_label;
    }

    public String getUpdate_type() {
        return update_type;
    }

    public void setUpdate_type(String update_type) {
        this.update_type = update_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getInvoice_label() {
        return invoice_label;
    }

    public void setInvoice_label(String invoice_label) {
        this.invoice_label = invoice_label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
