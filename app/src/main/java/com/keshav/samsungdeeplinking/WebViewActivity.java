package com.keshav.samsungdeeplinking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by keshav.p on 12/14/16.
 */

public class WebViewActivity extends AppCompatActivity implements JsInterface.JsMediator {

    public static final String TAG = WebViewActivity.class.getSimpleName();
    private WebView webView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        JsInterface jsInterface = new JsInterface();
        jsInterface.setContext(this);
        jsInterface.setJsMediator(this);
        webView.addJavascriptInterface(jsInterface, "myGalaxyObject");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e(TAG, "url response is:" + url);

            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);
                Log.e(TAG, "url response finished is:" + url);
            }
        });
        getad();
    }


    public static void longInfo(String str) {
        if (str.length() > 4000) {
            Log.i(TAG, str.substring(0, 4000));
            longInfo(str.substring(4000));
        } else
            Log.i(TAG, "complete html response is: " + str);
    }

    private void getad() {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"app\": {\n\t\t\"id\": \"1460406978213\",\n\t\t\"bundle\": \"com.samsung.commerce\",\n\t\t\"name\": \"SamsungCommerce\",\n\t\t\"storeurl\": \"\",\n\t\t\"cat\": [\"Commerce\"],\n\t\t\"ext\": {\n\t\t\t\"reftag\": \"Commerce\"\n\t\t}\n\t},\n\t\"imp\": {\n\t\t\"banner\": {\n\t\t\t\"w\": 320,\n\t\t\t\"h\": 480,\n\t\t\t\"api\": [1, 2]\n\t\t},\n\t\t\"secure\": 0,\n\t\t\"ext\": {\n\t\t\t\"ads\": 1\n\t\t}\n\t},\n\t\"device\": {\n\t\t\"gpid\": \"305c5dd8-fdfb-4a10-93fa-7170e091b750\",\n\t\t\"ua\": \"Mozilla/5.0 (Linux; Android 5.0; Aqua 4G+ Build/LRX21M) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/37.0.0.0 Mobile Safari/537.36\",\n\t\t\"lmt\": 0,\n\t\t\"geo\": {\n\t\t\t\"lat\": 40.7127,\n\t\t\t\"lon\": 74.0059,\n\t\t\t\"accu\": 1.0,\n\t\t\t\"type\": 1\n\t\t}\n\t},\n\t\"ext\": {\n\t\t\"responseformat\": \"jsonmeta\"\n\t}\n}");
        Request request = new Request.Builder()
                .url("https://c2s.w.inmobi.com/showad/v3.1")
                .post(body)
                .addHeader("content-type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Response IOException", e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!response.isSuccessful()) {
                                //throw new IOException("Unexpected code " + response);
                            }
                            String jsonData = response.body().string();
                            Log.e(TAG, "Response is: " + jsonData);


                            JSONObject jsonResponse = null;
                            try {
                                jsonResponse = new JSONObject(jsonData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JSONArray jsonMainNode = jsonResponse.optJSONArray("ads");
                            Log.d(TAG, "ads is" + jsonMainNode);

                            int lengthJsonArr = jsonMainNode.length();
                            for (int i = 0; i < lengthJsonArr; i++) {
                                /****** Get Object for each JSON node.***********/
                                JSONObject jsonChildNode = null;
                                try {
                                    jsonChildNode = jsonMainNode.getJSONObject(i);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                /******* Fetch node values **********/
                                final String html = jsonChildNode.optString("html");
                                longInfo(html);


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                                        webView.loadUrl("http://inmobiads.com/tizen.html");
                                    }
                                });
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Response IOException", e);

                        }
                    }


                }).start();

            }
        });
    }

    @Override
    public void jsInteraction(final String url) {
        Handler handler = new Handler(WebViewActivity.this.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("try{broadcastEvent('openExternalSuccessful',\"" + url + "\");}catch(e){}", null);
                // test if the script was loaded
                webView.evaluateJavascript("JsInjected()", null);
            }
        });
    }
}
