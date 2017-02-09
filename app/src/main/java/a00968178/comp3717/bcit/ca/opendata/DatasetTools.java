package a00968178.comp3717.bcit.ca.opendata;

import android.webkit.WebView;

/**
 * Created by Brayden on 2/4/2017.
 */

public class DatasetTools {

    public static String escape(String input) {
        return input.replace(' ', '_')
                    .replace('(', '_')
                    .replace(')', '_')
                    .replace('-', '_')
                    .replace('/', '_')
                    .replace(',', '_');
    }

    public static void evaluateJavascript(WebView view, String code) {
        view.loadUrl("javascript:(function() { " +
                code +
                "})()");
    }

}
