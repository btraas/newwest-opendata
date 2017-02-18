package ca.bcit.comp3717.a00968178.OpenData;

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
