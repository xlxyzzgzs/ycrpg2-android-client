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

import java.io.File;

public class WebViewHelper extends WebView {
    public WebViewAssetLoader assetLoader;
    public UpdateHelper updateHelper;

    @SuppressLint("JavascriptInterface")
    public WebViewHelper(@NonNull Context context) {
        super(context);
        updateHelper = new UpdateHelper(context, this);
        changeWebViewSetting();
        setWebContentsDebuggingEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });
        addJavascriptInterface(updateHelper, "updateHelper");

        assetLoader = new WebViewAssetLoader.Builder()
                .setDomain(context.getString(R.string.local_domain))
                .addPathHandler(context.getString(R.string.local_root_folder),
                        new WebViewAssetLoader.InternalStoragePathHandler(
                                context, new File(updateHelper.downloadHelper.filePrefix)
                        )
                ).setHttpAllowed(true)
                .build();
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
