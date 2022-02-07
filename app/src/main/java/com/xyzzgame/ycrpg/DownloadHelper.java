package com.xyzzgame.ycrpg;


import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadHelper {
    private final Context context;
    private final ExecutorService executorService;
    public String urlPrefix;
    public String filePrefix;
    public File rootFolder;

    public DownloadHelper(Context context) {
        this.context = context;
        executorService = Executors.newSingleThreadExecutor();
        urlPrefix = "https://" + context.getString(R.string.remote_domain) + context.getString(R.string.remote_root_folder);
        filePrefix = context.getFilesDir() + context.getString(R.string.local_root_folder);
        rootFolder = new File(filePrefix);
    }

    public void downloadFileFromUrl(File file, URL url, Runnable success, Runnable fail) {
        executorService.submit(() -> {
            InputStream is;
            OutputStream os = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                int downloadSize;
                int fileSize = connection.getContentLength();
                is = new BufferedInputStream(connection.getInputStream());
                os = new BufferedOutputStream(new FileOutputStream(file));
                downloadSize = Utils.fromInToOutStream(is, os);

                if (downloadSize == fileSize) {
                    executorService.submit(success);
                } else {
                    executorService.submit(fail);
                }
            } catch (IOException e) {
                e.printStackTrace();
                executorService.submit(fail);
            } finally {
                Utils.closeOutputStream(os);
                Utils.disconnectConnection(connection);
            }
        });
    }

    public void downloadFileFromUrl(String filePath, String urlPath, Runnable success, Runnable fail) {
        try {
            File file = new File(filePath);
            URL url = new URL(urlPath);
            downloadFileFromUrl(file, url, success, fail);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            executorService.submit(fail);
        }
    }

    public String fileNameToFullPath(String fileName) {
        return (new File(rootFolder, fileName)).getAbsolutePath();
    }

    public String fileNameToFullUrl(String fileName) {
        return urlPrefix + fileName;
    }
}
