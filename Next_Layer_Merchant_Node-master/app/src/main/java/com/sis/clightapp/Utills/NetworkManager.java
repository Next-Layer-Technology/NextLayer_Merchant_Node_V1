package com.sis.clightapp.Utills;

import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.util.Config.LOGV;

public class NetworkManager {
    private static NetworkManager networkManager = null;
    private SSLContext sslCtx;
    private SSLSocketFactory factory;
    private SSLSocket socket;
    private static BufferedReader in;
    private static PrintWriter out;

    private NetworkManager() {
        factory = null;
        socket = null;
        sslCtx = null;
        in = null;
        out = null;
        try {
            sslCtx = SSLContext.getInstance("SSL");
            sslCtx.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (GeneralSecurityException e) {
            Log.e("CLightningApp", Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };


    public static NetworkManager getInstance() {
        if (networkManager == null) {
            networkManager = new NetworkManager();
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }
        return networkManager;
    }
    public static void  setNetworkManagerNull () {
        if(networkManager!=null){
            networkManager=null;
        }

    }

    public boolean connectClient(String url, int port) {

        int userMode = -1;
        try {
            factory = sslCtx.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(url, port);
            socket.startHandshake();

            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

        } catch (Exception e) {
            Log.e("CLightningApp", Objects.requireNonNull(e.getLocalizedMessage()));
            return false;
        }
        return true;
    }
    /*
              * auth,who,<$#EOT#$>
              auth,admin,admin,<$#EOT#$>
              auth,ok,0,<$#EOT#$>
              db,get-list,images,<$#EOT#$>
              resp,ok,0,response,[ { "UPC" : "069138da-fc90-4b13-b40a-ac06675b06c2"},{ "UPC" : "0d4589ea-198f-47ae-95a7-b186c31e62a6"},{ "UPC" : "16481257-825f-4a06-8014-7c14f600307b"},{ "UPC" : "57ff029e-00a1-48a0-9919-3dadb72f1b57"},{ "UPC" : "a0812988-e76f-4808-928f-275a8e61339c"},{ "UPC" : "b7569980-56a6-41e6-b555-a9c8a34ee687"},{ "UPC" : "ecaa4c9b-51a4-455c-9aa0-41cc731200cc"}],<$#EOT#$>
              db,get-image,images,069138da-fc90-4b13-b40a-ac06675b06c2,<$#EOT#$>
              resp,ok,0,response,[ { "Image" : "89504E470D0A1A0A0000000D494844520000008E0000008E0100000000CF860DA5000000C94944415478DACD96510E843008444938408FE4D57B240E40C2CEC06A5CF71B2221C6BE1F613AB44A3CC3E51D481807528D6FDA8F141FB603A978464C20132C646D3E51CA1812D631897C12A5D0683B1EDAB7A13293DA9FBFDA504539E96786DA90AFED59883353E86614B141CF3A76F4A352191540E86B6B5B113C949D53680E6A3F2A1B696CA4ACDCDA6654910323BE42FBD17760A2FABF5D276DA8166CDBE8E009741E7CB4AFCD214D4BB19429048979EAD931814AE8CAFB79DF86CA4C9E9734B5EE47EFFCE3FB00275EBC0617B451B80000000049454E44AE426082"}],<$#EOT#$>

              * */
    public int validateUser(String id, String psswd) {

        int userMode = -1;

        String response = "";
        try {
            response = this.recvFromServer();
            if (response.contains("auth,who")) {
                this.sendToServer("auth," + id + "," + psswd);
                response = this.recvFromServer();
                if (response.contains("auth,ok")) {
                    userMode = Integer.parseInt((response.split(","))[2]);
                } else if (response.contains("auth,fail,-1")) {
                    userMode = Integer.parseInt((response.split(","))[2]);
                }
            }
        } catch (Exception e) {
            Log.e("CLightningApp", Objects.requireNonNull(e.getLocalizedMessage()));
        }
        return userMode;
    }
    public String recvFromServer() throws IOException {
        StringBuilder sb = new StringBuilder();
        if (in != null) {
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
                if(line.contains("<$#EOC#$>"))
                    break;
            }

        } else {
            return "";
        }
        return sb.toString().replace(",<$#EOT#$>", "").replace("\n", "");
    }
    public void sendToServer(String msg) throws IOException {
        if (!msg.isEmpty() && out != null) {
            if (!out.checkError()) {
                out.println(msg + ",<$#EOT#$>");
                out.println();
                out.flush();
            }
        }
    }
}
