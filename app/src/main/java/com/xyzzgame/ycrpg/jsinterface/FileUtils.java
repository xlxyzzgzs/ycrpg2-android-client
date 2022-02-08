package com.xyzzgame.ycrpg.jsinterface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.xyzzgame.ycrpg.R;
import com.xyzzgame.ycrpg.Utils;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public Context context;
    public WebView webView;
    public File rootFolder;

    public FileUtils(@NonNull Context context, @NonNull WebView webView) {
        this.context = context;
        this.webView = webView;
        this.rootFolder = new File(context.getFilesDir(), context.getString(R.string.local_root_folder));
    }

    public File fileNameToFile(String fileName) {
        File file = new File(fileName);
        return file.isAbsolute() ? file : new File(this.rootFolder, fileName);
    }

    public void evaluateJavascript(String script, ValueCallback<String> callback) {
        ContextCompat.getMainExecutor(context).execute(() -> webView.evaluateJavascript(script, callback));
    }

    public void runInWebViewThread(Runnable runnable) {
        ContextCompat.getMainExecutor(context).execute(runnable);
    }

    @JavascriptInterface
    public void evaluateJavascript(String script) {
        evaluateJavascript(script, null);
    }

    @JavascriptInterface
    public boolean canExecute(String fileName) {
        return fileNameToFile(fileName).canExecute();
    }

    @JavascriptInterface
    public boolean canRead(String fileName) {
        return fileNameToFile(fileName).canRead();
    }

    @JavascriptInterface
    public boolean canWrite(String fileName) {
        return fileNameToFile(fileName).canWrite();
    }

    @JavascriptInterface
    public int compareTo(String fileName1, String fileName2) {
        return fileNameToFile(fileName1).compareTo(fileNameToFile(fileName2));
    }

    @JavascriptInterface
    public boolean createNewFile(String fileName) {
        try {
            return fileNameToFile(fileName).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @JavascriptInterface
    public boolean delete(String fileName) {
        return fileNameToFile(fileName).delete();
    }

    @JavascriptInterface
    public boolean exists(String fileName) {
        return fileNameToFile(fileName).exists();
    }

    @JavascriptInterface
    public String getAbsolutePath(String fileName) {
        return fileNameToFile(fileName).getAbsolutePath();
    }

    @JavascriptInterface
    public String getCanonicalPath(String fileName) {
        try {
            return fileNameToFile(fileName).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @JavascriptInterface
    public long getFreeSpace(String fileName) {
        return fileNameToFile(fileName).getFreeSpace();
    }

    @JavascriptInterface
    public String getName(String fileName) {
        return fileNameToFile(fileName).getName();
    }

    @JavascriptInterface
    public String getParent(String fileName) {
        return fileNameToFile(fileName).getParent();
    }

    @JavascriptInterface
    public String getPath(String fileName) {
        return fileNameToFile(fileName).getPath();
    }

    @JavascriptInterface
    public long getTotalSpace(String fileName) {
        return fileNameToFile(fileName).getTotalSpace();
    }

    @SuppressLint("UsableSpace")
    @JavascriptInterface
    public long getUsableSpace(String fileName) {
        return fileNameToFile(fileName).getUsableSpace();
    }

    @JavascriptInterface
    public int hashCode(String fileName) {
        return fileNameToFile(fileName).hashCode();
    }

    @JavascriptInterface
    public boolean isAbsolute(String fileName) {
        return (new File(fileName)).isAbsolute();
    }

    @JavascriptInterface
    public boolean isDirectory(String fileName) {
        return fileNameToFile(fileName).isDirectory();
    }

    @JavascriptInterface
    public boolean isFile(String fileName) {
        return fileNameToFile(fileName).isFile();
    }

    @JavascriptInterface
    public boolean isHidden(String fileName) {
        return fileNameToFile(fileName).isHidden();
    }

    @JavascriptInterface
    public long lastModified(String fileName) {
        return fileNameToFile(fileName).lastModified();
    }

    @JavascriptInterface
    public long length(String fileName) {
        return fileNameToFile(fileName).length();
    }

    @JavascriptInterface
    public String list(String fileName) {
        File file = fileNameToFile(fileName);
        JSONArray array = new JSONArray();
        String[] names = file.list();
        if (names != null) {
            for (String name : names) {
                array.put(name);
            }
        }
        return array.toString();
    }

    @JavascriptInterface
    public boolean mkdir(String fileName) {
        return fileNameToFile(fileName).mkdir();
    }

    @JavascriptInterface
    public boolean mkdirs(String fileName) {
        return fileNameToFile(fileName).mkdirs();
    }

    @JavascriptInterface
    public boolean renameTo(String srcName, String dstName) {
        return fileNameToFile(srcName).renameTo(fileNameToFile(dstName));
    }

    @JavascriptInterface
    public boolean setExecutable(String fileName, boolean executable, boolean ownerOnly) {
        return fileNameToFile(fileName).setExecutable(executable, ownerOnly);
    }

    @JavascriptInterface
    public boolean setExecutable(String fileName, boolean executable) {
        return fileNameToFile(fileName).setExecutable(executable);
    }

    @JavascriptInterface
    public boolean setLastModified(String fileName, long time) {
        return fileNameToFile(fileName).setLastModified(time);
    }

    @JavascriptInterface
    public boolean setReadOnly(String fileName) {
        return fileNameToFile(fileName).setReadOnly();
    }

    @JavascriptInterface
    public boolean setReadable(String fileName, boolean readable, boolean ownerOnly) {
        return fileNameToFile(fileName).setReadable(readable, ownerOnly);
    }

    @JavascriptInterface
    public boolean setReadable(String fileName, boolean readable) {
        return fileNameToFile(fileName).setReadable(readable);
    }

    @JavascriptInterface
    public boolean setWritable(String fileName, boolean writable, boolean ownerOnly) {
        return fileNameToFile(fileName).setWritable(writable, ownerOnly);
    }

    @JavascriptInterface
    public boolean setWritable(String fileName, boolean writable) {
        return fileNameToFile(fileName).setWritable(writable);
    }

    @JavascriptInterface
    public String toString(String fileName) {
        return fileNameToFile(fileName).toString();
    }

    @JavascriptInterface
    public void test(String name) {
        runInWebViewThread(() -> {
            webView.addJavascriptInterface(new FileUtils(context, webView), name);
            webView.loadData("", "text/html", null);
            webView.loadUrl("javascript:console.log(window.FileUtils)");
        });
    }

    @JavascriptInterface
    public String readTextFile(String fileName) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(fileNameToFile(fileName)))) {
            return new String(Utils.readAllBytes(is), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @JavascriptInterface
    public boolean writeTextFile(String fileName,String content){
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(fileNameToFile(fileName)))) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
            return  true;
        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }
    }

    @JavascriptInterface
    public String getFileHashHex(String fileName, String algorithm){
        return Utils.fileHashHex(fileNameToFile(fileName).getAbsolutePath(),algorithm);
    }

    @JavascriptInterface
    public String getFileHashHex(String fileName){
        return Utils.fileHashHex(fileNameToFile(fileName).getAbsolutePath(),"SHA-512");
    }
}
