package com.sis.clightapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.sis.clightapp.R;
import com.sis.clightapp.Utills.NetworkManager;

public class RPCTestActivity extends BaseActivity {


    String TAG = "CLighting App";
    TextView skiptext, responsetext;
    EditText param1;
    Button sendtoserverbtn;
    public String URL = "https://www.google.com:443";
    public String EXAMPLE_METHOD_NAME = "login";
    public String EXAMPLE_PARAM_1 = "user";
    public String EXAMPLE_PARAM_2 = "password";
    String role="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_r_p_c_test);
        dialog = new ProgressDialog(RPCTestActivity.this);
        dialog.setMessage("Connecting...");
            Intent iin = getIntent();
        Bundle b = iin.getExtras();
        if (b != null) {
            role = (String) b.get("role");

            Log.e(TAG, "Role:"+role);

        }
        skiptext = findViewById(R.id.skiptext);
        responsetext = findViewById(R.id.textresponse);
        param1 = findViewById(R.id.editTextTextparam1);
        sendtoserverbtn = findViewById(R.id.sendbutton);
        sendtoserverbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                String param1text = param1.getText().toString();
                if (param1text.isEmpty()) {
                    showToast("Enter the param to send server");
                    Log.e(TAG, "Enter the param to send server");
                } else {
                    Log.e(TAG, " Request Input :" + param1text);
//                    sendParamtoRPCServer(param1text);




                    PostRequestServer postRequestServer=new PostRequestServer(RPCTestActivity.this);
                    postRequestServer.execute(new String[] { new String(param1text)});
                   ReadServerResponse readServerResponse= new ReadServerResponse(RPCTestActivity.this);
                    readServerResponse.execute();



                }

            }
        });

        skiptext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(role.equals("checkout"))
                {
                    Intent i = new Intent(RPCTestActivity.this, CheckOutMain11.class);
                    startActivity(i);
                    finish();
                }
                else
                    if(role.equals("merchant"))
                    {
                        Intent i = new Intent(RPCTestActivity.this, MerchnatMain11.class);
                        startActivity(i);
                    }
                    else
                        if(role.equals("admin"))
                        {
                            Intent i = new Intent(RPCTestActivity.this, AdminMain11.class);
                            startActivity(i);
                        }

            }
        });


    }

    private class ReadServerResponse extends AsyncTask<Void, Integer, String> {

        // Constant for identifying the dialog
        private static final int LOADING_DIALOG = 100;
        private Activity parent;

        public ReadServerResponse(Activity parent) {
            // record the calling activity, to use in showing/hiding dialogs
            this.parent = parent;
        }

        @Override
        protected String doInBackground(Void... voids) {
String response=null;
            try {
              response=NetworkManager.getInstance().recvFromServer();
            } catch (Exception e) {
               Log.e(TAG,e.getLocalizedMessage());
            }


            return response;
        }

        protected void onPreExecute () {
            // called on UI thread
            // parent.showDialog(LOADING_DIALOG);
        }


        protected void onProgressUpdate(Integer... progress) {
            // called on the UI thread
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {
            // this method is called back on the UI thread, so it's safe to
            //  make UI calls (like dismissing a dialog) here
            //  parent.dismissDialog(LOADING_DIALOG);

            dialog.dismiss();
            if(result==null)
            {
                responsetext.setText("No response Getting");
            }
            else
            {
                responsetext.setText(result);
            }



        }

    }


    private class PostRequestServer extends AsyncTask<String, Integer, Void> {

        // Constant for identifying the dialog
        private static final int LOADING_DIALOG = 100;
        private Activity parent;

        public PostRequestServer(Activity parent) {
            // record the calling activity, to use in showing/hiding dialogs
            this.parent = parent;
        }



        @Override
        protected Void doInBackground(String... strings) {
           String rqsttext=strings[0];
            try {
                NetworkManager.getInstance().sendToServer(rqsttext);
            } catch (Exception e) {
                Log.e(TAG,e.getLocalizedMessage());
            }
            return null;
        }

        protected void onPreExecute () {
            // called on UI thread
            // parent.showDialog(LOADING_DIALOG);
        }


        protected void onProgressUpdate(Integer... progress) {
            // called on the UI thread
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Void voids) {
            // this method is called back on the UI thread, so it's safe to
            //  make UI calls (like dismissing a dialog) here
            //  parent.dismissDialog(LOADING_DIALOG);
            dialog.dismiss();

        }
    }
}