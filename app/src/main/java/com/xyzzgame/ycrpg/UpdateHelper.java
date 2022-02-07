package com.xyzzgame.ycrpg;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class UpdateHelper {
    private final Context context;
    private final WebView webView;
    public DownloadHelper downloadHelper;

    public UpdateHelper(@NonNull Context context, @NonNull WebView webView) {
        this.context = context;
        this.webView = webView;
        downloadHelper = new DownloadHelper(context);
    }

    @JavascriptInterface
    public String getRootPath() {
        return downloadHelper.filePrefix;
    }

    @JavascriptInterface
    public String getHashInfoJson() {
        File file = new File(downloadHelper.filePrefix, "file_info.json");
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return new String(Utils.readAllBytes(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @JavascriptInterface
    public String getFileHashHex(String fileName) {
        String path = downloadHelper.fileNameToFullPath(fileName);
        return Utils.fileHashHex(path, "SHA-512");
    }

    @JavascriptInterface
    public void updateFile(String fileName, String success, String fail) {
        String filePath = downloadHelper.fileNameToFullPath(fileName);
        String urlPath = downloadHelper.fileNameToFullUrl(fileName);
        downloadHelper.downloadFileFromUrl(
                filePath, urlPath,
                () -> ContextCompat.getMainExecutor(context).execute(
                        () -> webView.evaluateJavascript(success, null)
                ),
                () -> ContextCompat.getMainExecutor(context).execute(
                        () -> webView.evaluateJavascript(fail, null)
                )
        );
    }

    @JavascriptInterface
    public void updateFileCompleted(boolean bool){
        SharedPreferences preferences= context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean isInit = preferences.getBoolean(context.getString(R.string.init_complete_key),false);
        if(bool && !isInit){
            editor.putBoolean(context.getString(R.string.init_complete_key), true);
            ContextCompat.getMainExecutor(context).execute(
                    ()->webView.loadUrl(context.getString(R.string.local_index_url))
            );
            editor.apply();
        }

    }
}
