package com.broy.musicplayer;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloader extends AsyncTask<String, Integer, String> {

    private final String downloadUrl;
    private final DownloadProgressListener mDownloadProgressListener;

    public Downloader(String downloadUrl, DownloadProgressListener mDownloadProgressListener) {
        this.downloadUrl = downloadUrl;
        this.mDownloadProgressListener = mDownloadProgressListener;
        Log.e("Downloader", Environment.getExternalStorageDirectory().getPath());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        mDownloadProgressListener.onDownloadProgress(values[0]);
    }

    @Override
    protected void onPostExecute(final String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(final String... voids) {

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();
            output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/ca3");

            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;

                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}
