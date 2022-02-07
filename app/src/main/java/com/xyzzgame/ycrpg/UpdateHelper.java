package com.xyzzgame.ycrpg;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
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

    public Runnable jsToRunnable(String js, ValueCallback<String> callback) {
        return () -> ContextCompat.getMainExecutor(context).execute(
                () -> webView.evaluateJavascript(js, callback)
        );
    }

    public Runnable jsToRunnable(String js) {
        return this.jsToRunnable(js, null);
    }

    @JavascriptInterface
    public String getRootPath() {
        return downloadHelper.filePrefix;
    }

    @JavascriptInterface
    public String getHashInfoJson() {
        File file = new File(downloadHelper.fileNameToFullPath("file_info.json"));
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
                jsToRunnable(success), jsToRunnable(fail)
        );
    }

    @JavascriptInterface
    public void updateFileCompleted(boolean bool) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean isInit = preferences.getBoolean(context.getString(R.string.init_complete_key), false);
        if (bool && !isInit) {
            editor.putBoolean(context.getString(R.string.init_complete_key), true);
            ContextCompat.getMainExecutor(context).execute(
                    () -> webView.loadUrl(context.getString(R.string.local_index_url))
            );
            editor.apply();
        }
    }

    @JavascriptInterface
    public boolean deleteFile(String fileName) {
        File file = new File(downloadHelper.fileNameToFullPath(fileName));
        return file.delete();
    }

    @JavascriptInterface
    public void downloadFile(String fileName, String urlPath, String success, String fail) {
        File file = new File(downloadHelper.fileNameToFullPath(fileName));
        URL url;
        try {
            url = new URL(urlPath);
            downloadHelper.downloadFileFromUrl(file, url, jsToRunnable(success), jsToRunnable(fail));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @JavascriptInterface
    public void writeFile(String fileName,String content){
        File file = new File(downloadHelper.fileNameToFullPath(fileName));
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            Utils.closeOutputStream(os);
        }
    }
}
