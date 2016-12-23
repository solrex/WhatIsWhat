package com.ooolab.whatiswhat;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by solrex on 2016/12/21.
 */

public class QueryParser {
    private static String TAG = "QueryParser";

    public static String getWhat(String str) {
        String what = "";
        Pattern whatPattern;
        if (str.contains("什么是")) {
            whatPattern = Pattern.compile("什么是(.+)");
        } else {
            whatPattern = Pattern.compile("(.+)是什么");
        }
        Matcher match = whatPattern.matcher(str);
        if (match.find()) {
            what = match.group(1);
        }
        Log.i(TAG, "getWhat(" + str + ")=" + what);
        return what;
    }
}
