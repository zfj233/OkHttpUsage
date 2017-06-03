package com.zfj.android.sample;

import android.util.Log;

/**
 * Created by zfj_ on 2017/5/17.
 */

public class L {
    private static final String TAG = "imooc_okhttp";
    private static boolean debug = true;

    public static void e(String msg){

        if(debug) {
            Log.e(TAG, msg);
        }
    }
}
