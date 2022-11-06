package com.sis.clightapp.fragments.merchant;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.os.Handler;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sis.clightapp.Interface.ApiClientStartStop;
import com.sis.clightapp.Interface.ApiPaths;
import com.sis.clightapp.R;
import com.sis.clightapp.Utills.AppConstants;
import com.sis.clightapp.Utills.CustomSharedPreferences;
import com.sis.clightapp.Utills.GlobalState;
import com.sis.clightapp.activity.MainActivity;

import com.sis.clightapp.activity.MainEntryActivity;
import com.sis.clightapp.model.GsonModel.Getinfoerror;
import com.sis.clightapp.model.GsonModel.Merchant.MerchantData;
import com.sis.clightapp.model.REST.ServerStartStop.Node.NodeResp;
import com.sis.clightapp.model.ScreenInfo;
import com.sis.clightapp.session.MyLogOutService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tech.gusavila92.websocketclient.WebSocketClient;

public class MerchantFragment3 extends MerchantBaseFragment {
    private MerchantFragment3 merchantFragment3;
    String TAG = "CLighting App";
    int count_add_screen = 1;
    private String gdaxUrl = "ws://73.36.65.41:8095/SendCommands";
    ProgressDialog exitingdialog;
    int count;
    String code1 = "";
    int code = 0;
    int countDetach = 0;
    int countquit = 0;
    private WebSocketClient webSocketClient;
    ArrayList<ScreenInfo> list = new ArrayList<>();
    ArrayList<ScreenInfo> listafterAdd = new ArrayList<>();
    ArrayList<ScreenInfo> listafterDetach = new ArrayList<>();
    ArrayList<ScreenInfo> newlist = new ArrayList<>();
    ProgressDialog startServerPD, stopServerPD, stopcall, startcall, screenCall, quitCall, addscreenCall, detachscreencall, getinfocall, simpleLoader;
    Button startBitcoinBtn, stopBitcoinBtn, startLightningBtn, stopLightningBtn, removeSshKeyBtn, rebootUpdateUpgradeBtn, remove_credentials, update_reboodNodeHost;
    boolean isConfirmMerchant = false;
    MerchantData currentMerchantData;
    TextView setTextWithSpan, result_Lightning, result_Bitcoin, result_RebootUpdateUpgrade;
    boolean isBitcoinConfirmed = false;
    boolean isThorConfirmed = false;
    boolean isLightningConfirmed = false;
    ProgressDialog checkStatusPD;
    Context fContext;
    ImageView thorNodeStatusImg, lightningNodeStatusImg, bitcoinNodeStatusImg;
    CustomSharedPreferences sharedPreferences;

    public MerchantFragment3() {
        // Required empty public constructor
    }

    public MerchantFragment3 getInstance() {
        if (merchantFragment3 == null) {
            merchantFragment3 = new MerchantFragment3();
        }
        return merchantFragment3;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant3, container, false);
        fContext = getContext();
        sharedPreferences = new CustomSharedPreferences();
        thorNodeStatusImg = view.findViewById(R.id.thor_status);
        lightningNodeStatusImg = view.findViewById(R.id.lightning_status);
        bitcoinNodeStatusImg = view.findViewById(R.id.bitcoin_status);
        setTextWithSpan = view.findViewById(R.id.imageView3);
        result_Lightning = view.findViewById(R.id.result_Lightninng);
        result_Bitcoin = view.findViewById(R.id.result_Bitcoin);
        result_RebootUpdateUpgrade = view.findViewById(R.id.result_RebootUpdateUpgrade);
        StyleSpan boldStyle = new StyleSpan(Typeface.BOLD);
        setTextWithSpan(setTextWithSpan, getString(R.string.welcome_text), getString(R.string.welcome_text_bold), boldStyle);
        exitingdialog = new ProgressDialog(getContext());
        exitingdialog.setMessage("Loading...");
        exitingdialog.setCancelable(false);
        stopcall = new ProgressDialog(getContext());
        stopcall.setMessage("Loading...");
        stopcall.setCancelable(false);
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
        startcall = new ProgressDialog(getContext());
        startcall.setMessage("Loading...");
        startcall.setCancelable(false);
        getinfocall = new ProgressDialog(getContext());
        getinfocall.setMessage("Loading...");
        getinfocall.setCancelable(false);
        simpleLoader = new ProgressDialog(getContext());
        simpleLoader.setMessage("In progress...");
        simpleLoader.setCancelable(false);
        startServerPD = new ProgressDialog(getContext());
        startServerPD.setMessage("Connecting...");
        startServerPD.setCancelable(false);
        stopServerPD = new ProgressDialog(getContext());
        stopServerPD.setMessage("Connecting...");
        stopServerPD.setCancelable(false);
        checkStatusPD = new ProgressDialog(fContext);
        checkStatusPD.setMessage("Loading...");
        checkStatusPD.setCancelable(false);
        String json = new CustomSharedPreferences().getvalueofMerchantData("data", getContext());
        Gson gson = new Gson();
        MerchantData merchantData = gson.fromJson(json, MerchantData.class);

        //currentMerchantData = GlobalState.getInstance().getMerchantData();
        currentMerchantData = merchantData;
        if (currentMerchantData != null) {
            isConfirmMerchant = true;
            GlobalState.getInstance().setMerchantConfirm(true);
        } else {
            isConfirmMerchant = false;
            GlobalState.getInstance().setMerchantConfirm(false);
        }
        //startBitcoin,stopBitcoin,startLightning,stopLightning
        rebootUpdateUpgradeBtn = view.findViewById(R.id.rebootUpdateUpgradeBtn);
        removeSshKeyBtn = view.findViewById(R.id.removeSshKeyBtn);
        startBitcoinBtn = view.findViewById(R.id.startBitcoinBtn);
        stopBitcoinBtn = view.findViewById(R.id.stopBitcoinBtn);
        startLightningBtn = view.findViewById(R.id.startLightningBtn);
        stopLightningBtn = view.findViewById(R.id.stopLightningBtn);
        remove_credentials = view.findViewById(R.id.removecredentials);
        update_reboodNodeHost = view.findViewById(R.id.reboot_restartnodehost);
        gdaxUrl=new CustomSharedPreferences().getvalueofMWSCommand("mws_command", getContext());
        sharedPreferences = new CustomSharedPreferences();
        //new CustomSharedPreferences().setvalueofaccestoken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNjM0MTkyMzk5LCJleHAiOjE2MzQyMzU1OTl9.va8ixffbBQ14gMvjZDOTiDW-b0G2C4hSfGWW1gnmxV0", "accessToken", getContext());
        remove_credentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.clearAllPreferences(getContext());
            }
        });
        update_reboodNodeHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getinfo();
            }
        });
        startBitcoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmMerchant) {
                    if (currentMerchantData != null) {
                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                            String type = "start";
                            String ssh = currentMerchantData.getSsh_ip_port();
                            if (ssh != null) {
                                if (!ssh.isEmpty()) {
                                    if (ssh.contains(":")) {
                                        String[] sh = ssh.split(":");
                                        String host = sh[0];
                                        String port = sh[1];
                                        if (currentMerchantData.isIs_own_bitcoin()) {
                                            String sshPass = currentMerchantData.getSsh_password();
                                            String sshUsername = currentMerchantData.getSsh_username();
                                            startBitcoinServer(type, host, port, sshUsername, sshPass);
                                        } else {
                                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                            builder.setMessage("No Own Bitcoin Node!!")
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
        removeSshKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmMerchant) {
                    if (currentMerchantData != null) {
                        goTo2FaPasswordDialog(currentMerchantData);
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "OK", "");
                }
            }
        });
        rebootUpdateUpgradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmMerchant) {
                    if (currentMerchantData != null) {
                        if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null) {
                            String type = "update";
                            String ssh = currentMerchantData.getSsh_ip_port();
                            if (ssh != null) {
                                if (!ssh.isEmpty()) {
                                    if (ssh.contains(":")) {
                                        String[] sh = ssh.split(":");
                                        String host = sh[0];
                                        String port = sh[1];
                                        String sshPass = currentMerchantData.getSsh_password();
                                        String sshUsername = currentMerchantData.getSsh_username();
                                        updateServer(type, host, port, sshUsername, sshPass);
                                    } else {
                                        //TODO
                                        goAlertDialogwithOneBTn(1, "", "Invalid SSH IP!", "Ok", "");
                                    }
                                } else {
                                    //TODO
                                    goAlertDialogwithOneBTn(1, "", "Empty SSH IP!", "Ok", "");
                                }
                            } else {
                                //TODO
                                goAlertDialogwithOneBTn(1, "", "Unavaiable SSH IP!", "Ok", "");
                            }
                        } else {
                            goAlertDialogwithOneBTn(1, "", "Merchant Info Missing!", "Ok", "");
                        }
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "Ok", "");
                    }
                } else {
                    goAlertDialogwithOneBTn(1, "", "Enter Merchant ID", "Ok", "");
                }
            }
        });
        // updateServer(type,host,port,sshUsername,sshPass);
        //  upgradeServer(type,host,port,sshUsername,sshPass);
        // rebootServer(type, host, port, sshUsername, sshPass);
        return view;
    }

    private void upgradeServer(final String type, final String host, final String port, final String sshUsername, final String sshPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).upgradeServer(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        final NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                final String repsMessag = resp.getMessage();
                                startServerPD.dismiss();
                                result_RebootUpdateUpgrade.setText(resp.getMessage());
                                rebootServer("reboot", host, port, sshUsername, sshPass);
                            } else {
                                startServerPD.dismiss();
                                result_RebootUpdateUpgrade.setText(resp.getMessage());
                            }
                        } else {
                            startServerPD.dismiss();
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "");
                        }
                    } else {
                        startServerPD.dismiss();
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "");
                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", t.getMessage(), "Retry", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            goAlertDialogwithOneBTn(1, "", "SSH is Missing", "Retry", "");
        }
    }

    private void updateServer(final String type, final String host, final String port, final String sshUsername, final String sshPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).updateServer(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        final NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                startServerPD.dismiss();
                                result_RebootUpdateUpgrade.setText(resp.getMessage());
                                upgradeServer("upgrade", host, port, sshUsername, sshPass);
                            } else {
                                startServerPD.dismiss();
                                result_RebootUpdateUpgrade.setText(resp.getMessage());
                            }

                        } else {
                            startServerPD.dismiss();
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "");
                        }
                    } else {
                        startServerPD.dismiss();
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "");

                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", t.getMessage(), "Retry", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            goAlertDialogwithOneBTn(1, "", "SSH is Missing", "Retry", "");
        }
    }

    private void rebootServer(String type, String host, String port, String sshUsername, String sshPass) {
        String yourFilePath = Environment.getExternalStorageDirectory().toString() + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).rebootServer(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        final NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                final String repsMessag = resp.getMessage();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        startServerPD.dismiss();
                                        result_RebootUpdateUpgrade.setText(resp.getMessage());
                                        sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getContext());
                                        startActivity(new Intent(getActivity(), MainEntryActivity.class));
                                    }
                                }, AppConstants.TIMEFORWAITLN2);
                            } else {
                                startServerPD.dismiss();
                                result_RebootUpdateUpgrade.setText(resp.getMessage());
                            }

                        } else {
                            startServerPD.dismiss();
                            goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "");
                        }
                    } else {
                        startServerPD.dismiss();
                        NodeResp resp = response.body();
                        goAlertDialogwithOneBTn(1, "", "Invalid SSH Info!", "Retry", "");

                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    goAlertDialogwithOneBTn(1, "", t.getMessage(), "Retry", "");
                    startServerPD.dismiss();
                }
            });
        } else {
            goAlertDialogwithOneBTn(1, "", "SSH is Missing", "Retry", "");
        }
    }

    private void goTo2FaPasswordDialog(MerchantData merchantData) {
        final MerchantData merchantDatafinal = merchantData;
        final Dialog enter2FaPassDialog;
        enter2FaPassDialog = new Dialog(getContext());
        enter2FaPassDialog.setContentView(R.layout.merchat_twofa_pass_lay);
        Objects.requireNonNull(enter2FaPassDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        enter2FaPassDialog.setCancelable(false);
        final EditText et_2Fa_pass = enter2FaPassDialog.findViewById(R.id.taskEditText);
        et_2Fa_pass.setHint("Enter Administrative Password");
        final Button btn_confirm = enter2FaPassDialog.findViewById(R.id.btn_confirm);
        final Button btn_cancel = enter2FaPassDialog.findViewById(R.id.btn_cancel);
        final ImageView iv_back = enter2FaPassDialog.findViewById(R.id.iv_back_invoice);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter2FaPassDialog.dismiss();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // closeSoftKeyBoard();
                String task = String.valueOf(et_2Fa_pass.getText());
                if (task.isEmpty()) {
                    goAlertDialogwithOneBTn(1, "", "Enter 2FA Password", "OK", "");
                } else {
                    if (task.equals(merchantDatafinal.getAdmin_administrator_password())) {
                        //  String sshkeypasval=sharedPreferences.getString("sshkeypass",fContext);
                        String yourFilePath = Environment
                                .getExternalStorageDirectory().toString()
                                + "/merhantapp";
                        File yourFile = null;
                        try {
                            yourFile = new File(yourFilePath);
                        } catch (Exception e) {
                            showToast("File Not Found");
                        }
                        boolean deleted = yourFile.delete();
                        if (deleted) {
                            showToast("File Remove");
                        }
                        sharedPreferences.clearAllPrefExceptOfSShkeyPassword(getContext());
                        enter2FaPassDialog.dismiss();
                        startActivity(new Intent(getActivity(), MainEntryActivity.class));
                    } else {
                        goAlertDialogwithOneBTn(1, "", "Incorrect Password", "Retry", "");
                    }
                }

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter2FaPassDialog.dismiss();
            }
        });
        enter2FaPassDialog.show();
    }

    private void goAlertDialogwithOneBTn(int i, String alertTitleMessage, String alertMessage, String alertBtn1Message, String alertBtn2Message) {
        final Dialog goAlertDialogwithOneBTnDialog;
        goAlertDialogwithOneBTnDialog = new Dialog(getContext());
        goAlertDialogwithOneBTnDialog.setContentView(R.layout.alert_dialog_layout);
        Objects.requireNonNull(goAlertDialogwithOneBTnDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        goAlertDialogwithOneBTnDialog.setCancelable(false);
        final TextView alertTitle_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertTitle);
        final TextView alertMessage_tv = goAlertDialogwithOneBTnDialog.findViewById(R.id.alertMessage);
        final Button yesbtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.yesbtn);
        final Button nobtn = goAlertDialogwithOneBTnDialog.findViewById(R.id.nobtn);
        yesbtn.setText(alertBtn1Message);
        nobtn.setText(alertBtn2Message);
        alertTitle_tv.setText(alertTitleMessage);
        alertMessage_tv.setText(alertMessage);
        if (i == 1) {
            nobtn.setVisibility(View.GONE);
            alertTitle_tv.setVisibility(View.GONE);
        } else {

        }

        yesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAlertDialogwithOneBTnDialog.dismiss();
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


    public void checkAppFlow() {
        updateStatusBox(1, true);
        if (isConfirmMerchant) {
            if (currentMerchantData != null) {
                if (currentMerchantData.getSsh_ip_port() != null && currentMerchantData.getSsh_password() != null && currentMerchantData.getSsh_username() != null && currentMerchantData.getRpc_username() != null && currentMerchantData.getRpc_password() != null) {
                    String type = "status";
                    String ssh = currentMerchantData.getSsh_ip_port();
                    if (ssh != null) {
                        if (!ssh.isEmpty()) {
                            if (ssh.contains(":")) {
                                String[] sh = ssh.split(":");
                                String host = sh[0];
                                String port = sh[1];
                                if (currentMerchantData.isIs_own_bitcoin()) {
                                    //TODO:When  Own BTC
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    goToOwnBitcoinCase(host, port, sshUsername, sshPass, rpcUserName, rpcPassword);


                                } else {
                                    //TODO:When Not Own BTC
                                    String sshPass = currentMerchantData.getSsh_password();
                                    String sshUsername = currentMerchantData.getSsh_username();
                                    String rpcUserName = currentMerchantData.getRpc_username();
                                    String rpcPassword = currentMerchantData.getRpc_password();
                                    goTOtheNotOwnBitcoinCase(host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
                                }
                            } else {
                                //TODO
                                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
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
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
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
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                        builder.setMessage("Unavaiable SSH IP!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }

                } else {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                    builder.setMessage("Merchant Info Missing")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                }
            } else {
                final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                builder.setMessage("Enter Merchant ID")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        }).show();
            }
        } else {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
            builder.setMessage("Enter Merchant ID")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    }).show();
        }
    }

    //TODO: When Own Bitcoin
    private void goToOwnBitcoinCase(String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
//        checkLightningNodeSatatus(host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
        getinfo();
        checkBitcoinNodeStatus(host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
    }

    //TODO: When No Own Bitcoin
    private void goTOtheNotOwnBitcoinCase(String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
//        checkLightningNodeSatatus(host, port, sshUsername, sshPass, rpcUserName, rpcPassword);
        getinfo();
        updateStatusBox(3, true);
        updateResultBitcoinStatus("NO LOCAL NODE");

        // checkBitcoinNodeStatus(host,port,sshUsername,sshPass,rpcUserName,rpcPassword);
        //ye msg set krna

        //NO LOCAL NODE


    }

    //TODO:Check The Status Of Lightnning Node
    private void checkLightningNodeSatatus(String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            checkStatusPD.show();
            String sshkeypasval = "";
            if (sharedPreferences.getString("sshkeypass", fContext) == null) {
                sshkeypasval = "";
            } else {
                sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).checkLightningNodeServerStatus2(sshkeypass, host2, port2, sshUsername2, itemImageFileMPBody, rpcUserName2, rpcPassword2);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200 && resp.getMessage().equals("up")) {
                                updateStatusBox(2, true);
                                updateResultLightningStatus("ACTIVE");


                            } else if (resp.getCode() == 200 && resp.getMessage().equals("down")) {
                                updateStatusBox(2, false);
                                updateResultLightningStatus("INACTIVE ");
//luqman pending
                            }

                        } else {
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                            builder.setMessage("Invalid SSH Info")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();

                        }
                    } else {
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                        builder.setMessage("Invalid SSH Info")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();

                    }
                    checkStatusPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                    builder.setMessage("Server Side Issue!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    checkStatusPD.dismiss();
                }
            });
        } else {
            showToast("SSh is Missing");
        }


    }

    //TODO:Check The Status Of Bitcoin Node
    private void checkBitcoinNodeStatus(String host, String port, String sshUsername, String sshPass, String rpcUserName, String rpcPassword) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            checkStatusPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            RequestBody rpcUserName2 = RequestBody.create(MediaType.parse("text/plain"), rpcUserName);
            RequestBody rpcPassword2 = RequestBody.create(MediaType.parse("text/plain"), rpcPassword);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).checkBitcoinNodeServerStatus2(sshkeypass, host2, port2, sshUsername2, itemImageFileMPBody, rpcUserName2, rpcPassword2);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200 && resp.getMessage().equals("up")) {
                                updateStatusBox(3, true);
                                updateResultBitcoinStatus("ACTIVE");

                            } else if (resp.getCode() == 200 && resp.getMessage().equals("down")) {
                                updateResultBitcoinStatus("INACTIVE");
                                updateStatusBox(3, false);
                            }
                        } else {
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                            builder.setMessage("Invalid SSH Info")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }
                    } else {
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                        builder.setMessage("Invalid SSH Info")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                    checkStatusPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(fContext);
                    builder.setMessage("Server Side Issue!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    checkStatusPD.dismiss();
                }
            });
        } else {
            showToast("SSh is Missing");
        }


    }

    //TODO:START AND STOP Lightnning SERVER APIs
    private void startLightningServer(String type, String host, String port, String sshUsername, String sshPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {

                    if (response.isSuccessful()) {

                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                final String repsMessag = resp.getMessage();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        startServerPD.dismiss();
                                        updateResultLightningStatus(repsMessag);

                                    }
                                }, AppConstants.TIMEFORWAITLN2);
                            } else {
                                startServerPD.dismiss();
                                updateResultLightningStatus(resp.getMessage());
                            }

                        } else {
                            startServerPD.dismiss();
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                            builder.setMessage("Invalid SSH Info!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }
                    } else {
                        startServerPD.dismiss();
                        NodeResp resp = response.body();
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setMessage("Invalid SSH Info!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                    builder.setMessage("Server Side Issue!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH is Missing");
        }
    }

    private void stopLightningServer(String type, String host, String port, String sshUsername, String sshPass) {


        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).stopLightningServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                updateResultLightningStatus(resp.getMessage());
                            } else {
                                updateResultLightningStatus(resp.getMessage());
                            }

                        } else {
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                            builder.setMessage("Invalid SSH Info!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }


                    } else {
                        NodeResp resp = response.body();
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setMessage("Invalid SSH Info!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                    startServerPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                    builder.setMessage("Server Side Issue!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSh is Missing");
        }


    }

    //TODO:START AND STOP Bitcoin SERVER APIs
    private void startBitcoinServer(String type, String host, String port, String sshUsername, String sshPass) {

        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).startBitcoinServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                final String respMessgr = resp.getMessage();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        startServerPD.dismiss();
                                        updateResultBitcoinStatus(respMessgr);
                                    }
                                }, AppConstants.TIMEFORWAITLN2);


                            } else {
                                startServerPD.dismiss();
                                updateResultBitcoinStatus(resp.getMessage());
                            }

                        } else {
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                            builder.setMessage("Invalid SSH Info!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                            startServerPD.dismiss();
                        }


                    } else {
                        NodeResp resp = response.body();
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setMessage("Invalid SSH Info!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                        startServerPD.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                    builder.setMessage("Server Side Issue!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSh is Missing");
        }
    }

    private void stopBitcoinServer(String type, String host, String port, String sshUsername, String sshPass) {
        String yourFilePath = Environment
                .getExternalStorageDirectory().toString()
                + "/merhantapp";
        File yourFile = null;
        try {
            yourFile = new File(yourFilePath);
        } catch (Exception e) {
            showToast("File Not Found");
        }
        if (yourFile.exists()) {
            startServerPD.show();
            String sshkeypasval = sharedPreferences.getString("sshkeypass", fContext);
            if (sshkeypasval == null) {
                sshkeypasval = "";
            }
            RequestBody sshkeypass = RequestBody.create(MediaType.parse("text/plain"), sshkeypasval);
            RequestBody type2 = RequestBody.create(MediaType.parse("text/plain"), type);
            RequestBody host2 = RequestBody.create(MediaType.parse("text/plain"), host);
            RequestBody port2 = RequestBody.create(MediaType.parse("text/plain"), port);
            RequestBody sshUsername2 = RequestBody.create(MediaType.parse("text/plain"), sshUsername);
            RequestBody sshPass2 = RequestBody.create(MediaType.parse("text/plain"), sshPass);
            MultipartBody.Part itemImageFileMPBody = null;
            RequestBody photo_id = RequestBody.create(MediaType.parse(""), yourFile);
            itemImageFileMPBody = MultipartBody.Part.createFormData("key", yourFile.getPath(), photo_id);
            Call call = ApiClientStartStop.getRetrofit().create(ApiPaths.class).stopBitcoinServer2(sshkeypass, type2, host2, port2, sshUsername2, itemImageFileMPBody);
            call.enqueue(new Callback<NodeResp>() {
                @Override
                public void onResponse(Call<NodeResp> call, Response<NodeResp> response) {
                    if (response.isSuccessful()) {
                        NodeResp resp = response.body();
                        if (resp != null) {
                            if (resp.getCode() == 200) {
                                updateResultBitcoinStatus(resp.getMessage());
                            } else {
                                updateResultBitcoinStatus(resp.getMessage());
                            }

                        } else {
                            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                            builder.setMessage("Invalid SSH Info!")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                        }


                    } else {
                        NodeResp resp = response.body();
                        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        builder.setMessage("Invalid SSH Info!")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                    }
                    startServerPD.dismiss();
                }

                @Override
                public void onFailure(Call<NodeResp> call, Throwable t) {
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                    builder.setMessage("Server Side Issue!!")
                            .setCancelable(false)
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int id) {
                                    dialog.cancel();
                                }
                            }).show();
                    startServerPD.dismiss();
                }
            });
        } else {
            showToast("SSH is Missing");
        }


    }

    //TODO:Update the Result on View
    private void updateResultLightningStatus(String s) {
        switch (s) {

            case "ACTIVE":
                sharedPreferences.setBoolean(true, LIGHTNINGSTATUS, getContext());
                result_Lightning.setText(s);
                result_Lightning.setTextColor(ContextCompat.getColor(getContext(), R.color.active_text_colour));
                break;
            case "INACTIVE":
                sharedPreferences.setBoolean(false, LIGHTNINGSTATUS, getContext());
                result_Lightning.setText(s);
                result_Lightning.setTextColor(ContextCompat.getColor(getContext(), R.color.inactive_text_colour));
                break;
            default:
                sharedPreferences.setBoolean(false, LIGHTNINGSTATUS, getContext());
                result_Lightning.setText(s);
                result_Lightning.setTextColor(ContextCompat.getColor(getContext(), R.color.remote_text_colour));
                break;

        }


    }

    private void updateResultBitcoinStatus(String s) {
        switch (s) {
            case "NO LOCAL NODE":
                sharedPreferences.setBoolean(true, BITCOINSTATUS, getContext());
                result_Bitcoin.setText(s);
                result_Bitcoin.setTextColor(ContextCompat.getColor(getContext(), R.color.remote_text_colour));

                break;
            case "ACTIVE":
                result_Bitcoin.setText(s);
                result_Bitcoin.setTextColor(ContextCompat.getColor(getContext(), R.color.active_text_colour));
                break;
            case "INACTIVE":
                sharedPreferences.setBoolean(false, BITCOINSTATUS, getContext());
                result_Bitcoin.setText(s);
                result_Bitcoin.setTextColor(ContextCompat.getColor(getContext(), R.color.inactive_text_colour));
                break;
            default:
                sharedPreferences.setBoolean(false, BITCOINSTATUS, getContext());
                result_Bitcoin.setText(s);
                break;

        }


    }

    //TODO: Update the Status Box
    private void updateStatusBox(int i, boolean b) {
//        switch (i) {
//            case 1:
//                if (b) {
//                    sharedPreferences.setBoolean(true, THORSTATUS, getContext());
//                    isThorConfirmed = true;
//                    thorNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, THORSTATUS, getContext());
//                    isThorConfirmed = false;
//                    thorNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.redstatus));
//                }
//                break;
//            case 2:
//                if (b) {
//                    sharedPreferences.setBoolean(true, LIGHTNINGSTATUS, getContext());
//                    isLightningConfirmed = true;
//                    lightningNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, LIGHTNINGSTATUS, getContext());
//                    isLightningConfirmed = false;
//                    lightningNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.redstatus));
//                }
//                break;
//            case 3:
//                if (b) {
//                    sharedPreferences.setBoolean(true, BITCOINSTATUS, getContext());
//                    isBitcoinConfirmed = true;
//                    bitcoinNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.greenstatus));
//                } else {
//                    sharedPreferences.setBoolean(false, BITCOINSTATUS, getContext());
//                    isBitcoinConfirmed = false;
//                    bitcoinNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.redstatus));
//                }
//                break;
//        }
    }

    //Exit Mode
    @Override
    public void onDestroy() {
//        handler.removeCallbacks(runnable);
        super.onDestroy();
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
    }

    @Override
    public void onStart() {
        super.onStart();

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

                getContext().stopService(new Intent(getContext(), MyLogOutService.class));
                Intent ii = new Intent(getContext(), MainActivity.class);
                startActivity(ii);
               // PostRequestServer postRequestServer = new PostRequestServer(getActivity());
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

    public void onBackPressed() {
        ask_exit();
    }
    public void ifPostSuccefully() {
        getContext().stopService(new Intent(getContext(), MyLogOutService.class));
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
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
                    Getinfoerror getinfoerror = gson.fromJson(s, Getinfoerror.class);
                    if (getinfoerror.isError()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sharedPreferences.setBoolean(false, THORSTATUS, getContext());
                                isThorConfirmed = false;
                                thorNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.redstatus));
                                updateResultLightningStatus("INACTIVE ");

                            }
                        });

                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateResultLightningStatus("ACTIVE ");
                                sharedPreferences.setBoolean(true, THORSTATUS, getContext());
                                isThorConfirmed = true;
                                thorNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.greenstatus));
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
                                    sharedPreferences.setBoolean(false, THORSTATUS, getContext());
                                    isThorConfirmed = false;
                                    thorNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.redstatus));
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
                                    sharedPreferences.setBoolean(true, THORSTATUS, getContext());
                                    isThorConfirmed = true;
                                    thorNodeStatusImg.setImageDrawable(fContext.getDrawable(R.drawable.greenstatus));
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

                        } else if (text.equals("No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addScreen(); //1
                                } });

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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            }
                        });
                    } else {
                        if (text.equals("There are screens on:\r\r\n")) {

                        }else if (text.equals("No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addScreen();//3
                                } });
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webSocket.close(1000, null);
                                webSocket.cancel();
                                goTo2FaPasswordDialog();
                            }
                        });
                    } else {
                        if (text.equals("There are screens on:\r\r\n")) {

                        }else if (text.equals("No Sockets found in /run/screen/S-routing-node-4.\r\n\r\r\n")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addScreen(); //5
                                } });
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
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        webSocket.close(1000, null);
                                        webSocket.cancel();
                                        goTo2FaPasswordDialog();
                                    }
                                });
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
                                detachscreencall.dismiss();
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

                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        if (jsonObject.has("code") && jsonObject.getInt("code") == 724) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    webSocket.close(1000, null);
                                    webSocket.cancel();
                                    goTo2FaPasswordDialog();
                                }
                            });
                        } else {
                            if (text.equals("routing-node-4@routingnode4-desktop:~$ ")) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
                                });

                            } else {
                                addscreenCall.dismiss();
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
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        webSocket.close(1000, null);
                                        webSocket.cancel();
                                        goTo2FaPasswordDialog();
                                    }
                                });
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