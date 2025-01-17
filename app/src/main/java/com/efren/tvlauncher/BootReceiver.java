package com.efren.tvlauncher;

import static java.sql.DriverManager.println;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent();
        newIntent.setAction("android.intent.action.MAIN");
        newIntent.addCategory("android.intent.category.LAUNCHER");
        newIntent.setPackage("ca.dstudio.atvlauncher.pro");
        newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        try {
            Log.e("wqs", "receive boot completed");
            context.startActivity(newIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("wqs", "Failed to start activity");
        }
    }
}
