package com.ooolab.whatiswhat;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by solrex on 2016/12/20.
 */

public class BaiduBaikeHandler implements Response.Listener<String>, Response.ErrorListener {
    private static String TAG = "BaiduBaikeHandler";
    public static String site = "https://wapbaike.baidu.com";

    private MainActivity mActivity;
    private WebView mWebView;
    private String mWhat;

    public BaiduBaikeHandler(MainActivity activity) {
        mActivity = activity;
        mWebView = (WebView) mActivity.findViewById(R.id.webview);
    }

    public void search(String what) {
        mWhat = what;
        try {
            String url = BaiduBaikeHandler.site + "/item/" + URLEncoder.encode(mWhat, "UTF-8");
            StringRequestWrapper urlReq = new StringRequestWrapper(url, this, this);
            mActivity.getVolleyQueue().add(urlReq);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "search: ", e);
        }
    }

    @Override
    public void onResponse(String response) {
        Log.i(TAG, "onResponse: " + response.substring(0, 32));
        //Log.w("Baike", "Response: " + response);
        Pattern secondSummary =
                Pattern.compile("<div class=\"second-summary.*data-url=\"([\\S^\"]+)\".*>");
        Matcher match = secondSummary.matcher(response);
        if (match.find()) {
            String secondPath = match.group(1);
            mActivity.hideInfo();
            mWebView.loadUrl(this.site + secondPath);
            if (mWebView.getVisibility() != View.VISIBLE) {
                mWebView.setVisibility(View.VISIBLE);
            }
        } else {
            mActivity.showInfo(String.format(
                    mActivity.getResources().getString(R.string.no_miaodong_result),
                    mWhat));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.i(TAG, "onErrorResponse: ", error);
        mActivity.showInfo(R.string.error_on_fetch_url);
    }
}
