package com.bitalinograph;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class WebAppInterface {
    Context mContext;
    WebView webView;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, WebView myWebView) {
        mContext = c;
        webView = myWebView;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Log.d("console", "Toast Log");
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

}