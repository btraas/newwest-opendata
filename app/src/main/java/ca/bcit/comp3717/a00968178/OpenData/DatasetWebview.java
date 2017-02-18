package ca.bcit.comp3717.a00968178.OpenData;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DatasetWebview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_webview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String title = intent.getStringExtra("name");
        //Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();

        //getSupportActionBar().setTitle(title);

        WebView webView = (WebView) findViewById(R.id.dataset_webview);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                // Runs javascript in an API < 19
                DatasetTools.evaluateJavascript(view, "$('nav').remove()");
                getSupportActionBar().setTitle(view.getTitle());
            }
        });
        webView.loadUrl(intent.getStringExtra("URL"));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // we can do this because this is only ever invoked from DatasetsActivity
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
