package com.keshav.samsungdeeplinking;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.WebChromeClient;

import static com.keshav.samsungdeeplinking.R.layout.webview;

/**
 * Created by keshav.p on 1/5/17.
 */

public class JsInterface {

    public interface JsMediator {
        void jsInteraction(final String url);
    }

    Context context;
    JsMediator jsMediator;


    public JsInterface() {
    }

    public Context getContext() {
        return context;

    }

    public JsMediator getJsMediator() {
        return jsMediator;
    }

    public void setJsMediator(JsMediator jsMediator) {
        this.jsMediator = jsMediator;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    @JavascriptInterface
    public void openURL(String primaryURL, String secondaryURL) {
        try {
            openIntent(primaryURL);
            jsMediator.jsInteraction(primaryURL);

        } catch (ActivityNotFoundException e) {

            try {
                openIntent(secondaryURL);
                jsMediator.jsInteraction(secondaryURL);
            } catch (Exception e1) {
                Log.e("Ex", "Error opening " + primaryURL + " Sec " + secondaryURL, e1);
            }
        }
    }

    private void openIntent(String uri) throws ActivityNotFoundException {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(browserIntent);
    }

}

