package com.sis.clightapp.fragments.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sis.clightapp.Interface.ApiClient;
import com.sis.clightapp.Interface.ApiClientBoost;
import com.sis.clightapp.Interface.ApiClientStartStop;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.Utills.NetworkManager;
import com.sis.clightapp.activity.MainActivity;

import com.sis.clightapp.model.GsonModel.Getinfoerror;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp;
import com.sis.clightapp.model.ScreenInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

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
public class AdminFragment2 extends AdminBaseFragment {
    AdminFragment2 adminFragment2;
    ProgressDialog exitingdialog;
    ImageView thorNodeStatusImg;
    TextView setTextWithSpan, result_Lightning, result_Bitcoin;
    ProgressDialog startServerPD, stopServerPD, stopcall, screenCall, quitCall, addscreenCall, detachscreencall, startcall, getinfocall, simpleLoader;
    Button startBitcoinBtn, stopBitcoinBtn, startLightningBtn, stopLightningBtn;
    boolean isConfirmMerchant = false;
    MerchantData currentMerchantData;
    int countDetach = 0;
    int countquit = 0;
    int count_add_screen = 1;
    ArrayList<ScreenInfo> list = new ArrayList<>();
    ArrayList<ScreenInfo> listafterAdd = new ArrayList<>();
    ArrayList<ScreenInfo> listafterDetach = new ArrayList<>();
    ArrayList<ScreenInfo> newlist = new ArrayList<>();

    CustomSharedPreferences sharedPreferences;
    //private final String gdaxUrl = "ws://98.226.215.246:8095/SendCommands";
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    // uri = new URI("ws://" + sharedPreferences.getvalueofipaddress("ip", bContext) + "/SendCommands");
    String code1 = "";
    int code = 0;
    private WebSocketClient webSocketClient;

    public AdminFragment2() {
        // Required empty public constructor
    }

    public AdminFragment2 getInstance() {
        if (adminFragment2 == null) {
            adminFragment2 = new AdminFragment2();
        }
        return adminFragment2;
    }

    public void onBackPressed() {
        ask_exit();

    }

    @Override
    public void onDestroy() {
//        handler.removeCallbacks(runnable);
        super.onDestroy();
        //ExitingFromServerOnDestroy exitingFromServer = new ExitingFromServerOnDestroy(getActivity());
        //exitingFromServer.execute(new String[]{new String("bye")});
    }

    public void startPage() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin2, container, false);

        setTextWithSpan = view.findViewById(R.id.imageView3);
        result_Lightning = view.findViewById(R.id.result_Lightninng);
        result_Bitcoin = view.findViewById(R.id.result_Bitcoin);
        thorNodeStatusImg = view.findViewById(R.id.thor_status);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan, getString(R.string.welcome_text), getString(R.string.welcome_text_bold), boldStyle);
        exitingdialog = new ProgressDialog(getContext());
        exitingdialog.setMessage("Loading...");
        startServerPD = new ProgressDialog(getContext());
        startServerPD.setMessage("Connecting...");
        stopServerPD = new ProgressDialog(getContext());
        stopServerPD.setMessage("Connecting...");
        stopcall = new ProgressDialog(getContext());
        stopcall.setMessage("Loading...");
        stopcall.setCancelable(false);
        startcall = new ProgressDialog(getContext());
        startcall.setMessage("Loading...");
        startcall.setCancelable(false);
        quitCall = new ProgressDialog(getContext());
        quitCall.setMessage("Loading...");
        quitCall.setCancelable(false);
        addscreenCall = new ProgressDialog(getContext());
        addscreenCall.setMessage("Loading...");
        addscreenCall.setCancelable(false);
        detachscreencall = new ProgressDialog(getContext());
        detachscreencall.setMessage("Loading...");
        detachscreencall.setCancelable(false);
        screenCall = new ProgressDialog(getContext());
        screenCall.setMessage("Loading...");
        screenCall.setCancelable(false);
        getinfocall = new ProgressDialog(getContext());
        getinfocall.setMessage("Loading...");
        getinfocall.setCancelable(false);
        simpleLoader = new ProgressDialog(getContext());
        simpleLoader.setMessage("In progress...");
        simpleLoader.setCancelable(false);
        gdaxUrl = new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());
        sharedPreferences = new CustomSharedPreferences();
        currentMerchantData = GlobalState.getInstance().getMerchantData();
        if (currentMerchantData != null) {
            isConfirmMerchant = true;
            GlobalState.getInstance().setMerchantConfirm(true);
        } else {
            isConfirmMerchant = false;
            GlobalState.getInstance().setMerchantConfirm(false);
        }
        //startBitcoin,stopBitcoin,startLightning,stopLightning
        startBitcoinBtn = view.findViewById(R.id.startBitcoinBtn);
        stopBitcoinBtn = view.findViewById(R.id.stopBitcoinBtn);
        startLightningBtn = view.findViewById(R.id.startLightningBtn);
        stopLightningBtn = view.findViewById(R.id.stopLightningBtn);
        startBitcoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count_add_screen = 0;
                screen_ls();
//                if (isConfirmMerchant) {
//                    if (currentMerchantData != null) {
//                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
//                            String type = "start";
//                            String ssh = currentMerchantData.getSsh_ip_port();
//                            if (ssh != null) {
//                                if (!ssh.isEmpty()) {
//                                    if (ssh.contains(":")) {
//                                        String[] sh = ssh.split(":");
//                                        String host = sh[0];
//                                        String port = sh[1];
//                                        if (currentMerchantData.isIs_own_bitcoin()) {
////                                            String sshPass=currentMerchantData.getSsh_password();
////                                            String sshUsername=currentMerchantData.getSsh_username();
////                                            stopBitcoinServer(type,host,port,sshUsername,sshPass);
//
//
//                                            String sshPass = currentMerchantData.getSsh_password();
//                                            String sshUsername = currentMerchantData.getSsh_username();
//                                            startBitcoinServer(type, host, port, sshUsername, sshPass);
//
//                                        } else {
////                                            if(currentMerchantData.getRpc_password()!=null&&currentMerchantData.getRpc_username()!=null){
////                                                String sshPass=currentMerchantData.getSsh_password();
////                                                String sshUsername=currentMerchantData.getSsh_username();
////                                                startBitcoinServer(type,host,port,sshUsername,sshPass);
////                                            }else {
////                                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
////                                                builder.setMessage("SSH Info Missing!!")
////                                                        .setCancelable(false)
////                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
////                                                            public void onClick(final DialogInterface dialog, final int id) {
////                                                                dialog.cancel();
////                                                            }
////                                                        }).show();
////                                            }
//                                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                            builder.setMessage("No Own Bitcoin Node!!")
//                                                    .setCancelable(false)
//                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                        public void onClick(final DialogInterface dialog, final int id) {
//                                                            dialog.cancel();
//                                                        }
//                                                    }).show();
//                                        }
//                                    } else {
//                                        //TODO
//                                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                        builder.setMessage("Invalid SSH IP!")
//                                                .setCancelable(false)
//                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                    public void onClick(final DialogInterface dialog, final int id) {
//                                                        dialog.cancel();
//                                                    }
//                                                }).show();
//                                    }
//                                } else {
//                                    //TODO
//                                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                    builder.setMessage("Empty SSH IP!")
//                                            .setCancelable(false)
//                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                public void onClick(final DialogInterface dialog, final int id) {
//                                                    dialog.cancel();
//                                                }
//                                            }).show();
//                                }
//                            } else {
//                                //TODO
//                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                builder.setMessage("Unavaiable SSH IP!")
//                                        .setCancelable(false)
//                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                            public void onClick(final DialogInterface dialog, final int id) {
//                                                dialog.cancel();
//                                            }
//                                        }).show();
//                            }
//
//                        } else {
//                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                            builder.setMessage("Merchant Info Missing")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        public void onClick(final DialogInterface dialog, final int id) {
//                                            dialog.cancel();
//                                        }
//                                    }).show();
//                        }
//                    } else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Enter Merchant ID")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                    }
//                } else {
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Enter Merchant ID")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                }
            }
        });
        stopBitcoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmMerchant) {
                    if (currentMerchantData != null) {
                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                            String type = "stop";
                            String ssh = currentMerchantData.getSsh_ip_port();
                            if (ssh != null) {
                                if (!ssh.isEmpty()) {
                                    if (ssh.contains(":")) {
                                        String[] sh = ssh.split(":");
                                        String host = sh[0];
                                        String port = sh[1];
                                        if (currentMerchantData.isIs_own_bitcoin()) {
//                                            String sshPass=currentMerchantData.getSsh_password();
//                                            String sshUsername=currentMerchantData.getSsh_username();
//                                            stopBitcoinServer(type,host,port,sshUsername,sshPass);
                                            String sshPass = currentMerchantData.getSsh_password();
                                            String sshUsername = currentMerchantData.getSsh_username();
                                            stopBitcoinServer(type, host, port, sshUsername, sshPass);


                                        } else {
                                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                            builder.setMessage("No Own Bitcoin Node!!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        public void onClick(final DialogInterface dialog, final int id) {
                                                            dialog.cancel();
                                                        }
                                                    }).show();

//                                            if(currentMerchantData.getRpc_password()!=null&&currentMerchantData.getRpc_username()!=null){
//                                                String sshPass=currentMerchantData.getSsh_password();
//                                                String sshUsername=currentMerchantData.getSsh_username();
//                                                stopBitcoinServer(type,host,port,sshUsername,sshPass);
//                                            }else {
//                                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                                builder.setMessage("SSH Info Missing!!")
//                                                        .setCancelable(false)
//                                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                            public void onClick(final DialogInterface dialog, final int id) {
//                                                                dialog.cancel();
//                                                            }
//                                                        }).show();
//                                            }
                                        }


                                    } else {
                                        //TODO
                                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                        builder.setMessage("Invalid SSH IP!")
                                                .setCancelable(false)
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(final DialogInterface dialog, final int id) {
                                                        dialog.cancel();
                                                    }
                                                }).show();
                                    }
                                } else {
                                    //TODO
                                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                    builder.setMessage("Empty SSH IP!")
                                            .setCancelable(false)
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(final DialogInterface dialog, final int id) {
                                                    dialog.cancel();
                                                }
                                            }).show();
                                }
                            } else {
                                //TODO
                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                builder.setMessage("Unavaiable SSH IP!")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog, final int id) {
                                                dialog.cancel();
                                            }
                                        }).show();
                            }

                        } else {
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                            builder.setMessage("Merchant Info Missing")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }
                    } else {
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setMessage("Enter Merchant ID")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                } else {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                    builder.setMessage("Enter Merchant ID")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                }

            }
        });
        startLightningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                screen_ls();
//                if (isConfirmMerchant) {
//                    if (currentMerchantData != null) {
//                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
//                            String type = "start";
//                            String ssh = currentMerchantData.getSsh_ip_port();
//                            if (ssh != null) {
//                                if (!ssh.isEmpty()) {
//                                    if (ssh.contains(":")) {
//                                        String[] sh = ssh.split(":");
//                                        String host = sh[0];
//                                        String port = sh[1];
//                                        String sshPass = currentMerchantData.getSsh_password();
//                                        String sshUsername = currentMerchantData.getSsh_username();
//                                        startLightningServer(type, host, port, sshUsername, sshPass);
//                                    } else {
//                                        //TODO
//                                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                        builder.setMessage("Invalid SSH IP!")
//                                                .setCancelable(false)
//                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                    public void onClick(final DialogInterface dialog, final int id) {
//                                                        dialog.cancel();
//                                                    }
//                                                }).show();
//                                    }
//                                } else {
//                                    //TODO
//                                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                    builder.setMessage("Empty SSH IP!")
//                                            .setCancelable(false)
//                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                public void onClick(final DialogInterface dialog, final int id) {
//                                                    dialog.cancel();
//                                                }
//                                            }).show();
//                                }
//                            } else {
//                                //TODO
//                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                builder.setMessage("Unavaiable SSH IP!")
//                                        .setCancelable(false)
//                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                            public void onClick(final DialogInterface dialog, final int id) {
//                                                dialog.cancel();
//                                            }
//                                        }).show();
//                            }
//
//                        } else {
//                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                            builder.setMessage("Merchant Info Missing")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        public void onClick(final DialogInterface dialog, final int id) {
//                                            dialog.cancel();
//                                        }
//                                    }).show();
//                        }
//                    } else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Enter Merchant ID")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                    }
//                } else {
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Enter Merchant ID")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                }
            }
        });
        stopLightningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stoplightning();
//                if (isConfirmMerchant) {
//                    if (currentMerchantData != null) {
//                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
//                            String type = "stop";
//                            String ssh = currentMerchantData.getSsh_ip_port();
//                            if (ssh != null) {
//                                if (!ssh.isEmpty()) {
//                                    if (ssh.contains(":")) {
//                                        String[] sh = ssh.split(":");
//                                        String host = sh[0];
//                                        String port = sh[1];
//                                        String sshPass = currentMerchantData.getSsh_password();
//                                        String sshUsername = currentMerchantData.getSsh_username();
//                                        stopLightningServer(type, host, port, sshUsername, sshPass);
//                                    } else {
//                                        //TODO
//                                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                        builder.setMessage("Invalid SSH IP!")
//                                                .setCancelable(false)
//                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                    public void onClick(final DialogInterface dialog, final int id) {
//                                                        dialog.cancel();
//                                                    }
//                                                }).show();
//                                    }
//                                } else {
//                                    //TODO
//                                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                    builder.setMessage("Empty SSH IP!")
//                                            .setCancelable(false)
//                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                public void onClick(final DialogInterface dialog, final int id) {
//                                                    dialog.cancel();
//                                                }
//                                            }).show();
//                                }
//                            } else {
//                                //TODO
//                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                                builder.setMessage("Unavaiable SSH IP!")
//                                        .setCancelable(false)
//                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                            public void onClick(final DialogInterface dialog, final int id) {
//                                                dialog.cancel();
//                                            }
//                                        }).show();
//                            }
//
//                        } else {
//                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                            builder.setMessage("Merchant Info Missing")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        public void onClick(final DialogInterface dialog, final int id) {
//                                            dialog.cancel();
//                                        }
//                                    }).show();
//                        }
//                    } else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Enter Merchant ID")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                    }
//                } else {
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Enter Merchant ID")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                }
            }
        });
        return view;
    }

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
                //PostRequestServer postRequestServer = new PostRequestServer(getActivity());
                //postRequestServer.execute(new String[]{new String("bye")});
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
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

    //TODO:START AND STOP Lightnning SERVER APIs
    private void startLightningServer(String type, String host, String port, String sshUsername, String sshPass) {
//        startServerPD.show();
//        Call call= ApiClientStartStop.getRetrofit().create(ApiPaths.class).startLightningServer(type,host,port,sshUsername,sshPass);
//        call.enqueue(new Callback<NodeResp>() {
//            @Override
//            public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
//
//                if(response.isSuccessful()){
//                    final NodeResp resp=response.body();
//                    if(resp!=null){
//                        if(resp.getCode()==200){
//                            final String repsMessag=resp.getMessage();
//                            Handler handler = new Handler();
//                            handler.postDelayed(new Runnable() {
//                                public void run() {
//                                    // yourMethod();
//                                    startServerPD.dismiss();
//                                    updateResultLightningStatus("Lightnning: "+repsMessag);
//
//                                }
//                            }, AppConstants.TIMEFORWAITLN2);
//                        }else {
//                            updateResultLightningStatus(resp.getMessage());
//                            startServerPD.dismiss();
//                        }
//
//                    }else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Invalid SSH Info!")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                        startServerPD.dismiss();
//                    }
//
//
//                }else {
//                    NodeResp resp=response.body();
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Invalid SSH Info!")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                    startServerPD.dismiss();
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(Call<NodeResp> call, Throwable t) {
//                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                builder.setMessage("Server Side Issue!!")
//                        .setCancelable(false)
//                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                            public void onClick(final DialogInterface dialog, final int id) {
//                                dialog.cancel();
//                            }
//                        }).show();
//                startServerPD.dismiss();
//            }
//        });


    }

    private void stopLightningServer(String type, String host, String port, String sshUsername, String sshPass) {
//        startServerPD.show();
//        Call call= ApiClientStartStop.getRetrofit().create(ApiPaths.class).stopLightningServer(type,host,port,sshUsername,sshPass);
//        call.enqueue(new Callback<NodeResp>() {
//            @Override
//            public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
//
//
//
//                if(response.isSuccessful()){
//                    NodeResp resp=response.body();
//                    if(resp!=null){
//                        if(resp.getCode()==200){
//                            updateResultLightningStatus("Lightning: "+resp.getMessage());
//                        }else {
//                            updateResultLightningStatus(resp.getMessage());
//                        }
//
//                    }else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Invalid SSH Info!")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                    }
//
//
//                }else {
//                    NodeResp resp=response.body();
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Invalid SSH Info!")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                }
//                startServerPD.dismiss();
//            }
//
//            @Override
//            public void onFailure(Call<NodeResp> call, Throwable t) {
//                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                builder.setMessage("Server Side Issue!!")
//                        .setCancelable(false)
//                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                            public void onClick(final DialogInterface dialog, final int id) {
//                                dialog.cancel();
//                            }
//                        }).show();
//                startServerPD.dismiss();
//            }
//        });

    }

    //TODO:START AND STOP Bitcoin SERVER APIs
    private void startBitcoinServer(String type, String host, String port, String sshUsername, String sshPass) {
//        startServerPD.show();
//        Call call= ApiClientStartStop.getRetrofit().create(ApiPaths.class).startBitcoinServer(type,host,port,sshUsername,sshPass);
//        call.enqueue(new Callback<NodeResp>() {
//            @Override
//            public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
//
//                if(response.isSuccessful()){
//                    final NodeResp resp=response.body();
//                    if(resp!=null){
//                        if(resp.getCode()==200){
//                            final String respMessgr=resp.getMessage();
//                            Handler handler = new Handler();
//                            handler.postDelayed(new Runnable() {
//                                public void run() {
//                                    // yourMethod();
//                                    startServerPD.dismiss();
//                                    updateResultBitcoinStatus("Bitcoin: "+respMessgr);
//                                }
//                            }, AppConstants.TIMEFORWAITLN2);
//                        }else {startServerPD.dismiss();
//                            updateResultBitcoinStatus(resp.getMessage());
//                        }
//
//                    }else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Invalid SSH Info!")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                        startServerPD.dismiss();
//                    }
//
//
//                }else {
//                    NodeResp resp=response.body();
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Invalid SSH Info!")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                    startServerPD.dismiss();
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(Call<NodeResp> call, Throwable t) {
//                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                builder.setMessage("Server Side Issue!!")
//                        .setCancelable(false)
//                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                            public void onClick(final DialogInterface dialog, final int id) {
//                                dialog.cancel();
//                            }
//                        }).show();
//                startServerPD.dismiss();
//            }
//        });


    }

    private void stopBitcoinServer(String type, String host, String port, String sshUsername, String sshPass) {
//        startServerPD.show();
//        Call call= ApiClientStartStop.getRetrofit().create(ApiPaths.class).stopBitcoinServer(type,host,port,sshUsername,sshPass);
//        call.enqueue(new Callback<NodeResp>() {
//            @Override
//            public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
//                if(response.isSuccessful()){
//                    NodeResp resp=response.body();
//                    if(resp!=null){
//                        if(resp.getCode()==200){
//                            updateResultBitcoinStatus("Bitcoin: "+resp.getMessage());
//                        }else {
//                            updateResultBitcoinStatus(resp.getMessage());
//                        }
//
//                    }else {
//                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                        builder.setMessage("Invalid SSH Info!")
//                                .setCancelable(false)
//                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                    public void onClick(final DialogInterface dialog, final int id) {
//                                        dialog.cancel();
//                                    }
//                                }).show();
//                    }
//
//
//                }else {
//                    NodeResp resp=response.body();
//                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                    builder.setMessage("Invalid SSH Info!")
//                            .setCancelable(false)
//                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                public void onClick(final DialogInterface dialog, final int id) {
//                                    dialog.cancel();
//                                }
//                            }).show();
//                }
//                startServerPD.dismiss();
//            }
//
//            @Override
//            public void onFailure(Call<NodeResp> call, Throwable t) {
//                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
//                builder.setMessage("Server Side Issue!!")
//                        .setCancelable(false)
//                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
//                            public void onClick(final DialogInterface dialog, final int id) {
//                                dialog.cancel();
//                            }
//                        }).show();
//                startServerPD.dismiss();
//            }
//        });

    }

    //TODO:Update the Result on View
    private void updateResultLightningStatus(String s) {
        result_Lightning.setText(s);
    }

    private void updateResultBitcoinStatus(String s) {
        result_Bitcoin.setText(s);
    }

    public void startlightning() {
        startcall.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -S Lightning -p 0 -X stuff \\\"lightningd --disable-plugin bcli^M\\\"\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    startcall.show();
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

            }

            @Override
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        parseJSONForRefunds(text);
                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            } else {
                                startcall.show();
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
                startcall.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        showToast(String.valueOf(response));
                        simpleLoader.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getinfo();
                                simpleLoader.dismiss();
                            }
                        }, 30000);

                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void stoplightning() {
        stopcall.show();
        final OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -S Lightning -p 0 -X stuff \\\"^C\\\"\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopcall.dismiss();

                        }
                    });
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
                                stopcall.dismiss();
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
                stopcall.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        showToast(String.valueOf(response));
                        simpleLoader.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getinfo();
                                simpleLoader.dismiss();
                            }
                        }, 5000);


                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    private void getinfo() {
        getinfocall.show();
        URI uri;
        try {
            // Connect to local host
            uri = new URI(gdaxUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"lightning-cli getinfo\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocketClient.send(String.valueOf(obj));


                } catch (Throwable t) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getinfocall.dismiss();

                        }
                    });
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

                Log.i("WebSocket", "Session is starting");
//                Toast.makeText(getApplicationContext(), "opend", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onTextReceived(String s) {
                Log.e("TAG", "MESSAGE: " + s);

                final String message = s;
                System.out.println(s);
                Gson gson = new Gson();
                try {
                    final Getinfoerror getinfoerror = gson.fromJson(s, Getinfoerror.class);
                    if (getinfoerror.isError()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                thorNodeStatusImg.setImageDrawable(getContext().getDrawable(R.drawable.redstatus));
                                updateResultLightningStatus("INACTIVE ");

                            }
                        });

                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateResultLightningStatus("ACTIVE ");

                                thorNodeStatusImg.setImageDrawable(getContext().getDrawable(R.drawable.greenstatus));
                            }
                        });
                    }
                } catch (Exception e) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        code1 = jsonObject.getString("id");
                        if (code1.equals("")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    thorNodeStatusImg.setImageDrawable(getContext().getDrawable(R.drawable.redstatus));
                                    updateResultLightningStatus("INACTIVE ");
                                }
                            });

                            sharedPreferences.setvalueofconnectedSocket("", "socketconnected", getContext());
                        } else {
                            sharedPreferences.setvalueofconnectedSocket(code1, "socketconnected", getContext());

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateResultLightningStatus("ACTIVE ");
                                    thorNodeStatusImg.setImageDrawable(getContext().getDrawable(R.drawable.greenstatus));
                                }
                            });
                        }

                        if (code == 724) {
                            sharedPreferences.setvalueofSocketCode(code, "socketcode", getContext());

                        } else if (code == 724) {
                            sharedPreferences.setvalueofSocketCode(code, "socketcode", getContext());
                        } else {

                        }

                    } catch (JSONException err) {
                        Log.d("Error", err.toString());
                    }

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getinfocall.dismiss();

                    }
                });


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
                getinfocall.dismiss();
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void screen_ls() {
        screenCall.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -ls\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    screenCall.dismiss();
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

            }

            @Override
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null);
                        webSocket.cancel();
                        goTo2FaPasswordDialog();
                    } else {
                        if (text.equals("There are screens on:\r\r\n")) {

                        }
                        else if (text.equals("No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addScreen(); //1
                                }
                            });

                        }
                        else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {



                                    String s = text;
                                    s = s.replaceAll("[\\n\\t\\r]", " ");
                                    s = s.toLowerCase();
                                    String words[] = s.split(" ");
                                    for (int i = 0; i < words.length; i++) {
                                        if (words[i].equals("(detached)") || words[i].equals("(attached)") || words[i].equals("detached") || words[i].equals("attached")) {
                                            ScreenInfo secreeninfo = new ScreenInfo();
                                            secreeninfo.setStatus(words[i]);
                                            if (list.size() == 0) {
                                                list.add(0, secreeninfo);
                                            } else {
                                                list.add(list.size(), secreeninfo);
                                            }

                                        }

                                    }
                                    newlist.clear();
                                    for (int i = 0; i < words.length; i++) {
                                        if (words[i].contains(".lightning")) {
                                            String str = words[i];
                                            String kept = str.substring(0, str.indexOf("."));
                                            ScreenInfo secreeninfo = new ScreenInfo();
                                            secreeninfo.setPid(kept);
                                            if (newlist.size() == 0) {
                                                newlist.add(0, secreeninfo);
                                            } else {
                                                newlist.add(newlist.size(), secreeninfo);
                                            }

                                        }

                                    }

                                    for (int i = 0; i < list.size(); i++) {
                                        for (int j = 0; j < newlist.size(); j++) {
                                            if (i == j) {
                                                list.get(i).setPid(newlist.get(j).getPid());
                                            }
                                        }
                                    }
                                    if (list.size() == 0) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                screenCall.dismiss();
                                                simpleLoader.show();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        list.clear();
                                                        addScreen(); //2
                                                        simpleLoader.dismiss();
                                                    }
                                                }, 2000);

                                            }
                                        });

                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                screenCall.dismiss();
                                                simpleLoader.show();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        QuitcreenScnerio();
                                                        simpleLoader.dismiss();
                                                    }
                                                }, 2000);

                                            }
                                        });
                                    }

                                }
                            });
                        }

                        screenCall.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }




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
                        screenCall.dismiss();
                        showToast(String.valueOf(response));
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void screen_ls_afterdetach() {
        screenCall.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -ls\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    screenCall.dismiss();
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

            }

            @Override
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null);
                        webSocket.cancel();
                        goTo2FaPasswordDialog();
                    } else {
                        if (text.equals("There are screens on:\r\r\n")) {

                        } else if (text.equals("No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addScreen();//3
                                }
                            });
                        }
                        else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String s = text;
                                    s = s.replaceAll("[\\n\\t\\r]", " ");
                                    s = s.toLowerCase();
                                    String words[] = s.split(" ");
                                    listafterDetach.clear();
                                    for (int i = 0; i < words.length; i++) {
                                        if (words[i].equals("(detached)") || words[i].equals("attached") || words[i].equals("(attached)") || words[i].equals("detached")) {
                                            ScreenInfo secreeninfo = new ScreenInfo();
                                            secreeninfo.setStatus(words[i]);
                                            if (listafterDetach.size() == 0) {
                                                listafterDetach.add(0, secreeninfo);
                                            } else {
                                                listafterDetach.add(listafterDetach.size(), secreeninfo);
                                            }

                                        }

                                    }
                                    newlist.clear();
                                    for (int i = 0; i < words.length; i++) {
                                        if (words[i].contains(".lightning")) {
                                            String str = words[i];
                                            String kept = str.substring(0, str.indexOf("."));
                                            ScreenInfo secreeninfo = new ScreenInfo();
                                            secreeninfo.setPid(kept);
                                            if (newlist.size() == 0) {
                                                newlist.add(0, secreeninfo);
                                            } else {
                                                newlist.add(newlist.size(), secreeninfo);
                                            }

                                        }

                                    }

                                    for (int i = 0; i < list.size(); i++) {
                                        for (int j = 0; j < newlist.size(); j++) {
                                            if (i == j) {
                                                list.get(i).setPid(newlist.get(j).getPid());
                                            }
                                        }
                                    }
                                    if (listafterDetach.size() == 0) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                screenCall.dismiss();
                                                simpleLoader.show();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addScreen(); //4
                                                        simpleLoader.dismiss();
                                                    }
                                                }, 2000);

                                            }
                                        });

                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                screenCall.dismiss();
                                                simpleLoader.show();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Ifdetached();
                                                        simpleLoader.dismiss();
                                                    }
                                                }, 2000);

                                            }
                                        });


                                    }


                                }
                            });

                            screenCall.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                        screenCall.dismiss();
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void screen_ls_afterAddnew() {
        screenCall.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -ls\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    screenCall.dismiss();
                    Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                }

            }

            @Override
            public void onMessage(WebSocket webSocket, final String text) {
                Log.e("TAG", "MESSAGE: " + text);

                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                        webSocket.close(1000, null);
                        webSocket.cancel();
                        goTo2FaPasswordDialog();
                    } else {
                        if (text.equals("There are screens on:\r\r\n")) {

                        } else if (text.equals("No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addScreen(); //5
                                }
                            });
                        }
                        else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    String s = text;
                                    s = s.replaceAll("[\\n\\t\\r]", " ");
                                    s = s.toLowerCase();
                                    String words[] = s.split(" ");
                                    listafterAdd.clear();
                                    for (int i = 0; i < words.length; i++) {
                                        if (words[i].equals("(detached)") || words[i].equals("(attached)") || words[i].equals("detached") || words[i].equals("attached")) {
                                            ScreenInfo secreeninfo = new ScreenInfo();
                                            secreeninfo.setStatus(words[i]);
                                            if (listafterAdd.size() == 0) {
                                                listafterAdd.add(0, secreeninfo);
                                            } else {
                                                listafterAdd.add(listafterAdd.size(), secreeninfo);
                                            }

                                        }

                                    }
                                    newlist.clear();
                                    for (int i = 0; i < words.length; i++) {
                                        if (words[i].contains(".lightning")) {
                                            String str = words[i];
                                            String kept = str.substring(0, str.indexOf("."));
                                            ScreenInfo secreeninfo = new ScreenInfo();
                                            secreeninfo.setPid(kept);
                                            if (newlist.size() == 0) {
                                                newlist.add(0, secreeninfo);
                                            } else {
                                                newlist.add(newlist.size(), secreeninfo);
                                            }

                                        }

                                    }

                                    for (int i = 0; i < list.size(); i++) {
                                        for (int j = 0; j < newlist.size(); j++) {
                                            if (i == j) {
                                                list.get(i).setPid(newlist.get(j).getPid());
                                            }
                                        }
                                    }
                                    if (listafterAdd.size() == 0) {
                                        simpleLoader.show();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                screenCall.dismiss();
                                                listafterAdd.clear();
                                                addScreen();//6
                                                simpleLoader.dismiss();
                                            }
                                        }, 2000);

                                    } else {
                                        screenCall.dismiss();
                                        simpleLoader.show();
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                DetachscreenScnerio();
                                                simpleLoader.dismiss();
                                            }
                                        }, 2000);
                                    }


                                }
                            });
                            screenCall.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                        screenCall.dismiss();
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void DetachScreen(final String id) {
        detachscreencall.show();
        OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();
        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());

                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\" screen -d" + " " + id + "\"] }";

                try {

                    JSONObject obj = new JSONObject(json);

                    Log.d("My App", obj.toString());


                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    detachscreencall.dismiss();
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
                                String s = text;
                                s = s.replaceAll("[\\n\\t\\r]", " ");
                                s = s.toLowerCase();
                                String words[] = s.split(" ");
                                for (int i = 0; i < words.length; i++) {
                                    if (words[i].equals("(detached)") || words[i].equals("(attached)")) {
                                        ScreenInfo secreeninfo = new ScreenInfo();
                                        secreeninfo.setStatus(words[i]);
                                        if (listafterDetach.size() == 0) {
                                            listafterDetach.add(0, secreeninfo);
                                        } else {
                                            listafterDetach.add(listafterDetach.size(), secreeninfo);
                                        }

                                    }

                                }
                                for (int i = 0; i < words.length; i++) {
                                    if (words[i].contains(".lightning")) {
                                        for (int j = 0; j < listafterDetach.size(); j++) {
                                            String str = words[i];
                                            String kept = str.substring(0, str.indexOf("."));
                                            listafterDetach.get(j).setPid(kept);
                                        }

                                    }
                                }
                                countDetach++;
                                if (listafterAdd.size() == countDetach) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            detachscreencall.dismiss();
                                            simpleLoader.show();
                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    screen_ls_afterdetach();
                                                    countDetach = 0;
                                                    listafterAdd.clear();
                                                    simpleLoader.dismiss();
                                                }
                                            }, 2000);

                                        }
                                    });

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                });
                detachscreencall.dismiss();
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
                        countDetach = 0;
                        detachscreencall.dismiss();
                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void addScreen() {
        if (count_add_screen == 2) {
            showToast("Please start again");
        } else {
            addscreenCall.show();
            count_add_screen++;
            final OkHttpClient clientCoinPrice = new OkHttpClient();
            Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

            WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                    String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                    String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -S Lightning\"] }";

                    try {

                        JSONObject obj = new JSONObject(json);

                        Log.d("My App", obj.toString());


                        webSocket.send(String.valueOf(obj));


                    } catch (Throwable t) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addscreenCall.dismiss();

                            }
                        });
                        Log.e("My App", "Could not parse malformed JSON: \"" + json + "\"");
                    }

                }

                @Override
                public void onMessage(WebSocket webSocket, final String text) {
                    Log.e("TAG", "MESSAGE: " + text);
                    if (text.equals("routing-node-4@routingnode4-desktop:~$ ")) {
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
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addscreenCall.dismiss();
                                                simpleLoader.show();
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        screen_ls_afterAddnew();
                                                        simpleLoader.dismiss();
                                                    }
                                                }, 3000);

                                            }
                                        });

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } else {
                        addscreenCall.dismiss();
                    }

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
                    addscreenCall.dismiss();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        showToast(String.valueOf(response));

                            simpleLoader.show();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                getinfo();
                                    simpleLoader.dismiss();
                                }
                            }, 2000);


                        }
                    });

                }
            };

            clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
            clientCoinPrice.dispatcher().executorService().shutdown();
        }

    }

    public void QuitScreen(final String pid) {
        quitCall.show();
        final OkHttpClient clientCoinPrice = new OkHttpClient();
        Request requestCoinPrice = new Request.Builder().url(gdaxUrl).build();

        WebSocketListener webSocketListenerCoinPrice = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {

                String token = sharedPreferences.getvalueofaccestoken("accessToken", getContext());
                String json = "{\"token\" : \"" + token + "\", \"commands\" : [\"screen -XS" + " " + pid + " " + "quit" + "\"] }";

                try {

                    JSONObject obj = new JSONObject(json);
                    Log.d("My App", obj.toString());
                    webSocket.send(String.valueOf(obj));


                } catch (Throwable t) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            quitCall.dismiss();

                        }
                    });
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
                                quitCall.dismiss();
                                simpleLoader.show();
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (countquit == list.size()) {
                                            addScreen(); //7
                                            list.clear();
                                            countquit = 0;
                                        }

                                        simpleLoader.dismiss();
                                    }
                                }, 2000);

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
                quitCall.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        showToast(String.valueOf(response));
                        simpleLoader.show();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                countquit++;
                                if (countquit == list.size()) {
                                    countquit = 0;
                                    list.clear();
                                    addScreen(); //8


                                }

                                simpleLoader.dismiss();
                            }
                        }, 2000);


                    }
                });

            }
        };

        clientCoinPrice.newWebSocket(requestCoinPrice, webSocketListenerCoinPrice);
        clientCoinPrice.dispatcher().executorService().shutdown();
    }

    public void DetachscreenScnerio() {
        for (int i = 0; i < listafterAdd.size(); i++) {
            if (listafterAdd.size() > 0) {
                DetachScreen(listafterAdd.get(i).getPid());
            }
        }

    }

    public void QuitcreenScnerio() {
        for (int i = 0; i < list.size(); i++) {
            if (list.size() > 0) {
                QuitScreen(list.get(i).getPid());
            }
        }
    }

    public void Ifdetached() {
        for (int i = 0; i < listafterDetach.size(); i++) {
            if (listafterDetach.size() > 0) {
                if (listafterDetach.get(i).getStatus().equals("(detached)") || listafterDetach.get(i).getStatus().equals("detached")) {
                    startlightning();
                } else {
                    showToast("Attached");
                }
            } else if (listafterDetach.get(i).getStatus().equals("attached")) {
                listafterDetach.clear();
                DetachscreenScnerio();
            }

        }
    }
}