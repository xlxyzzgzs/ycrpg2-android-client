package com.xyzzgame.ycrpg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.webkit.WebViewAssetLoader;

import com.xyzzgame.ycrpg.jsinterface.FileUtils;
import com.xyzzgame.ycrpg.jsinterface.UpdateUtils;

import java.io.File;

public class WebViewHelper extends WebView {
    public WebViewAssetLoader assetLoader;
    public Context context;
    public FileUtils fileUtils;
    public UpdateUtils updateUtils;

    @SuppressLint("JavascriptInterface")
    public WebViewHelper(@NonNull Context context) {
        super(context);
        this.context = context;

        changeWebViewSetting();
        setWebContentsDebuggingEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });

        fileUtils = new FileUtils(context, this);
        updateUtils = new UpdateUtils(context, this);

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler(
                        "/",
                        new WebViewAssetLoader.AssetsPathHandler(context)
                ).setHttpAllowed(true)
                .build();

        addAllJavascriptInterface();
    }

    public void addAllJavascriptInterface() {
        addJavascriptInterface(fileUtils, "FileUtils");
        addJavascriptInterface(updateUtils, "UpdateUtils");
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void changeWebViewSetting() {
        WebSettings webSettings;
        webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);

        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

    }
}
