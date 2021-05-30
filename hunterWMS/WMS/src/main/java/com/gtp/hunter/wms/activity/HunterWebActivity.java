package com.gtp.hunter.wms.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.api.HunterURL;

import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HunterWebActivity extends AppCompatActivity {

    private final String BASE = (HunterURL.useSSL ? "https://" : "http://") + HunterURL.IP;

    private ConstraintLayout baseLayout;
    private WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hunter_web);

        baseLayout = findViewById(R.id.baseLayout);
        wv = findViewById(R.id.wbvwMiddle);
        showFullScreen();
        prepareWebView();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        wv.loadUrl(BASE + "/home/process/viewTasks/WMSDRONEINVENTORY");
    }

    private void showFullScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        baseLayout.setVisibility(View.VISIBLE);
        baseLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void prepareWebView() {
        WebSettings webSettings = wv.getSettings();

        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setBlockNetworkLoads(false);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setUserAgentString("hunter WMS Mobile");
        wv.addJavascriptInterface(this, "AndroidInterface");
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                Timber.e("Error on URL: %s ( %d ) - %s", failingUrl, errorCode, description);
                try {
                    webView.stopLoading();
                } catch (Exception ignored) {
                }

                if (webView.canGoBack()) {
                    webView.goBack();
                }
                webView.loadUrl("about:blank");
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }
        });
        wv.setWebChromeClient(new WebChromeClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        wv.clearCache(true);
        wv.clearFormData();
        wv.clearHistory();
        wv.clearMatches();
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get Token from WEB
     */
    @JavascriptInterface
    public String getToken() {
        return HunterMobileWMS.getToken();
    }
}
