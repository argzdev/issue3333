package com.argz.issue3333;

import static com.android.volley.VolleyLog.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity {

    TextView tv_server;
    TextView tv_remote;
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_server = findViewById(R.id.tv_server);
        tv_remote = findViewById(R.id.tv_remote);

        callRemoteConfig();
        callRequestHttps();
    }

    private SSLSocketFactory allowSSL(){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = getResources().openRawResource(R.raw.cert);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            SSLSocketFactory sf = context.getSocketFactory();
            return sf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void callRequestHttps(){
        RequestQueue queue = Volley.newRequestQueue(this, new HurlStack(null, allowSSL()));
        String url ="https://10.0.2.2:3443";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            response -> tv_server.setText(response),
            error -> tv_server.setText("That didn't work! " + error.getMessage())
        );

        queue.add(stringRequest);
    }

    private void callRemoteConfig(){
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        tv_remote.setText(mFirebaseRemoteConfig.getString("random_value"));

        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    boolean updated = task.getResult();
                    Log.d(TAG, "Config params updated: " + updated);
                    Toast.makeText(MainActivity.this, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Fetch failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                displayWelcomeMessage();
            });
    }

    private void displayWelcomeMessage(){
        String fetched = mFirebaseRemoteConfig.getString("random_value");
        Log.d(TAG, "displayWelcomeMessage: " + fetched);
        tv_remote.setText(fetched);
    }

    private void callRequestHttp(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.0.2.2:3443";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> tv_server.setText("callRequest: " + response),
                error -> tv_server.setText("callRequest: That didn't work!")
        );

        queue.add(stringRequest);
    }

}