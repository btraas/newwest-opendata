package a00968178.comp3717.bcit.ca.opendata.webtools;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Brayd on 2/9/2017.
 */

public class TextDownload {

    private static final String TAG = TextDownload.class.getName();

    public static final int MODE_BINARY = 0;
    public static final int MODE_TEXT = 1;
    public static final int MODE_DEFAULT = MODE_TEXT;

    public String[] getLines() {
        return lines;
    }

    public String getData() {
        return data;
    }

    private String   data;  // whole file in a string
    private String[] lines; // lines of the file in a string array

    public TextDownload(String url) {
        Bundle b = download(url);
        if(b != null) {

            this.data = b.getString("data");
            this.lines = b.getStringArray("lines");
        }
    }

    private static Bundle download(String rawUrl) {
        Bundle b = new Bundle();
        URL url = null;
        HttpURLConnection http;
        try {
            url = new URL(rawUrl);
            http = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "HTTP code: " + http.getResponseCode());
            if(http.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new Exception("Failed to download from "+rawUrl+" with HTTP code: "+http.getResponseCode());

            // opens input stream from the HTTP connection
            ArrayList<String> lines = new ArrayList<String>();
            InputStream s = http.getInputStream();
            String line = "";
            String data = "";

            BufferedReader reader = new BufferedReader(new InputStreamReader(s));
            while((line = reader.readLine()) != null) {
                lines.add(line);
                data += line + "\n";
            }
            b.putString("data", data);
            b.putStringArray("lines", lines.toArray(new String[lines.size()]));
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
