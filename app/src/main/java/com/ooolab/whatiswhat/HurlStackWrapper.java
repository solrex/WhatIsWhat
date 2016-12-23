package com.ooolab.whatiswhat;

import android.util.Log;

import com.android.volley.toolbox.HurlStack;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URL;


/**
 * Created by solrex on 2016/12/21.
 */

public class HurlStackWrapper extends HurlStack {
    private static String TAG = "HurlStackWrapper";

    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     * 支持使用系统代理，支持跟随 302 重定向
     */
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        Log.i(TAG, "createConnection: " + url);

        final HttpURLConnection urlConnection;
        Proxy proxy = null;
        try {
            proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (proxy == null) {
            urlConnection = (HttpURLConnection) url.openConnection();
        } else {
            urlConnection = (HttpURLConnection) url.openConnection(proxy);
        }
        urlConnection.setInstanceFollowRedirects(true);
        return urlConnection;
    }
}
