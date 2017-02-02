package com.texasgamer.zephyr.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.texasgamer.zephyr.BuildConfig;
import com.texasgamer.zephyr.Constants;
import com.texasgamer.zephyr.R;
import com.texasgamer.zephyr.model.Token;
import com.texasgamer.zephyr.util.TokenUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class WebLoginActivity extends BaseActivity {

    Map<String, String> headers = new HashMap<>();
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        headers.put("Authorization", BuildConfig.ZEPHYR_API_KEY);

        webView = (WebView) findViewById(R.id.loginWebView);

        setupWebView();

        navigateToLoginPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.reset:
                navigateToLoginPage();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupWebView() {
        CookieManager.getInstance().setAcceptCookie(true);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if(url.contains("localhost")) {
                    url = url.replace("localhost", "10.0.2.2");
                }

                if(url.contains("/api/v2/2/result")) {
                    new FetchTokenTask().execute(url);
                    return true;
                }

                view.loadUrl(url, headers);

                return false;
            }
        });
    }

    private void navigateToLoginPage() {
        webView.loadUrl(Constants.ZEPHYR_BASE_WEB_URL + "/api/v2/2", headers);
    }

    class FetchTokenTask extends AsyncTask<String, Void, Token> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(WebLoginActivity.this, "", "Please wait...", true);
        }

        protected Token doInBackground(String... urls) {
            try {
                URL aURL = new URL(urls[0]);
                URLConnection conn = aURL.openConnection();
                conn.setRequestProperty("Cookie", CookieManager.getInstance().getCookie(Constants.ZEPHYR_BASE_WEB_URL));
                conn.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine = in.readLine();
                in.close();

                JSONObject tokenJson = new JSONObject(inputLine);

                return new Token(tokenJson.getString("name"), tokenJson.getString("avatar"), tokenJson.getString("room"), tokenJson.getString("token"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Token token) {
            if (token == null) {
                Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.web_login_failed), Toast.LENGTH_SHORT).show();
                navigateToLoginPage();
                progressDialog.dismiss();
                return;
            }

            Log.i(TAG, token.toString());
            TokenUtils.getInstance(getBaseContext()).saveToken(getBaseContext(), token);
            progressDialog.dismiss();
            finish();
        }
    }
}
