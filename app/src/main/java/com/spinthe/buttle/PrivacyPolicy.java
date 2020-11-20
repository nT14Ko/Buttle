package com.spinthe.buttle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.util.HashSet;
import java.util.Set;

public class PrivacyPolicy extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR = 1;
    private WebSettings setBew;

    private String param = "";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_privacy_policy);


        sharedPreferences = getApplicationContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        param = sharedPreferences.getString("param", "");

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        sets();

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.requestFocus(View.FOCUS_DOWN);

        webView.setWebViewClient(new ProWebViewClient());

        Intent intent = getIntent();
        String per = intent.getStringExtra("privpo");

        if (isNetworkConnected()) {if (per != null)
                webView.loadUrl("file:///android_asset/privacy_policy.html");
        }
        else {
            webView.loadUrl("file:///android_asset/error_page.html");
        }
    }

    public void sets(){
        setBew = webView.getSettings();
        setBew.setAppCacheEnabled(true);
        setBew.setDomStorageEnabled(true);
        setBew.setDatabaseEnabled(true);
        setBew.setSupportZoom(false);
        setBew.setAllowFileAccess(true);
        setBew.setAllowContentAccess(true);
        setBew.setJavaScriptEnabled(true);
        setBew.setLoadWithOverviewMode(true);
        setBew.setUseWideViewPort(true);
        setBew.setJavaScriptCanOpenWindowsAutomatically(true);
        setBew.setPluginState(WebSettings.PluginState.ON);
        setBew.setSavePassword(true);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Uri[] results = null;
        if (resultCode == PrivacyPolicy.RESULT_OK) {
            if (requestCode == FCR) {
                if (null == mUMA) {
                    return;
                }
                if (intent == null) {
                    if (mCM != null) {
                        results = new Uri[]{Uri.parse(mCM)};
                    }
                } else {
                    String dataString = intent.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
        }
        mUMA.onReceiveValue(results);
        mUMA = null;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            openQuitDialog();
        }
    }

    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                PrivacyPolicy.this);
        quitDialog.setTitle("Exit?");

        quitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });

        quitDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.show();
    }

    private class ProWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
            super.onReceivedError(view, req, err);
            boolean val = true;
            Set<String> results = sharedPreferences.getStringSet("results", new HashSet<String>());
            for (String value : results) {
                if (req.getUrl().toString().contains(value)) {
                    val = false;
                }
            }
            if (val && !req.getUrl().toString().endsWith(".mp4")
                    && !req.getUrl().toString().endsWith(".mp3") && !req.getUrl().toString().endsWith(".jpg") &&
                    !req.getUrl().toString().endsWith(".png") && !req.getUrl().toString().endsWith(".gif"))
                webView.loadUrl("file:///android_asset/error_page.html");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            boolean val = true;
            Set<String> results = sharedPreferences.getStringSet("results", new HashSet<String>());
            for (String value : results) {
                if (failingUrl.contains(value)) {
                    val = false;
                    break;
                }
            }
            if (val && !failingUrl.endsWith(".mp4")
                    && !failingUrl.endsWith(".mp3") && !failingUrl.endsWith(".jpg") &&
                    !failingUrl.endsWith(".png") && !failingUrl.endsWith(".gif")) {
                webView.loadUrl("file:///android_asset/error_page.html");
            }
        }
    }
}