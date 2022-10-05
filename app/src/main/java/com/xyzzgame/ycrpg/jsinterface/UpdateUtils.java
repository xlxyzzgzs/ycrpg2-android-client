package com.xyzzgame.ycrpg.jsinterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.xyzzgame.ycrpg.R;
import com.xyzzgame.ycrpg.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateUtils {
    public Context context;
    public WebView webView;
    public File rootFolder;
    public ExecutorService service;
    public String urlPrefix;

    public UpdateUtils(@NonNull Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        service = Executors.newSingleThreadExecutor();
        this.urlPrefix = "https://" + context.getString(R.string.remote_domain) + context.getString(R.string.remote_root_folder);
        this.rootFolder = new File(context.getFilesDir(), context.getString(R.string.local_root_folder));
    }

    public File fileNameToFile(String fileName) {
        File file = new File(fileName);
        return file.isAbsolute() ? file : new File(this.rootFolder, fileName);
    }

    public void evaluateJavascript(String script, ValueCallback<String> callback) {
        runInWebViewThread(() -> webView.evaluateJavascript(script, callback));
    }

    public void runInWebViewThread(Runnable runnable) {
        ContextCompat.getMainExecutor(context).execute(runnable);
    }

    @JavascriptInterface
    public void evaluateJavascript(String script) {
        evaluateJavascript(script, null);
    }

    @JavascriptInterface
    public void downloadFullUrl(String fileName, String urlString, String success, String fail) {
        File file = fileNameToFile(fileName);
        URL url;
        InputStream is;
        HttpURLConnection urlConnection = null;
        OutputStream os = null;

        try {
            url = new URL(urlString);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            String encoding = urlConnection.getContentEncoding();
            is = new BufferedInputStream(urlConnection.getInputStream());
            os = new BufferedOutputStream(new FileOutputStream(file));
            Utils.fromInToOutStream(is, os);

            evaluateJavascript(success);
        } catch (IOException e) {
            e.printStackTrace();
            evaluateJavascript(fail);
        } finally {
            Utils.closeOutputStream(os);
            Utils.disconnectConnection(urlConnection);
        }

    }

    @JavascriptInterface
    public void downloadRelativeUrl(String fileName, String urlString, String success, String fail) {
        downloadFullUrl(fileName, this.urlPrefix + urlString, success, fail);
    }

    @JavascriptInterface
    public void updateFileCompleted(boolean bool, boolean needReload) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean isInit = preferences.getBoolean(context.getString(R.string.init_complete_key), false);
        if (bool && !isInit) {
            editor.putBoolean(context.getString(R.string.init_complete_key), true);
            editor.apply();
        }
        if (needReload) {
            ContextCompat.getMainExecutor(context).execute(
                    () -> webView.loadUrl(context.getString(R.string.local_index_url))
            );
        }
    }

    @JavascriptInterface
    public String getLocalInfoJson() {
        File file = fileNameToFile("file_info_local.json");
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return new String(Utils.readAllBytes(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"content_version\":1,\"content\":{\"game_version\":\"V0.0.0\"}}";
        }
    }

    @JavascriptInterface
    public void updateFile(String fileName, String success, String fail) {
        downloadRelativeUrl(fileName, fileName, success, fail);
    }

    @JavascriptInterface
    public void startUpdateFiles() {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean isInit = preferences.getBoolean(context.getString(R.string.init_complete_key), false);
        editor.putBoolean(context.getString(R.string.init_complete_key), false);
        editor.apply();
    }
}
