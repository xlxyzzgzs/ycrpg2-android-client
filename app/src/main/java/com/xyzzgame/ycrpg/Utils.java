package com.xyzzgame.ycrpg;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static int DEFAULT_BUFFER_SIZE = 8196;

    /* return moved byte number */
    public static int fromInToOutStream(@NonNull InputStream is, @NonNull OutputStream os) throws IOException {
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int n;
        int total = 0;
        while ((n = is.read(buf, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            os.write(buf, 0, n);
            total += n;
        }
        return total;
    }

    /* copy from openjdk11 java.io.InputStream.readNBytes */
    @NonNull
    public static byte[] readAllBytes(@NonNull InputStream is) throws IOException {
        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int n;
        do {
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            int nread = 0;
            while ((n = is.read(buf, nread, buf.length - nread)) > 0) {
                nread += n;
            }
            if (nread > 0) {
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
        } while (n >= 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ? result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        int remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }
        return result;
    }

    @NonNull
    public static byte[] inputStreamHash(@NonNull InputStream is, @NonNull MessageDigest md) throws IOException {
        md.reset();
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = is.read(buf, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            md.update(buf, 0, n);
        }
        return md.digest();
    }

    public static MessageDigest getMessageDigest(String algorithm) {
        if (algorithm == null) {
            algorithm = "SHA-512";
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            try {
                md = MessageDigest.getInstance("SHA-512");
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                noSuchAlgorithmException.printStackTrace();
            }
        }
        return md;
    }

    public static byte[] fileHash(String filePath, String algorithm) {
        byte[] result;
        MessageDigest md = getMessageDigest(algorithm);
        try (InputStream is = new BufferedInputStream(new FileInputStream(filePath))) {
            result = inputStreamHash(is, md);
        } catch (IOException e) {
            e.printStackTrace();
            result = new byte[0];
        }
        return result;
    }

    public static String fileHashHex(String filePath, String algorithm) {
        byte[] hash = fileHash(filePath, algorithm);
        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    public static void closeInputStream(InputStream s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // nothing to do
        }
    }

    public static void closeOutputStream(OutputStream s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // nothing to do
        }
    }

    public static void disconnectConnection(HttpURLConnection connection) {
        try {
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
