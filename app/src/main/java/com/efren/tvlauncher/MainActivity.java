package com.efren.tvlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;

public class MainActivity extends Activity {
    private PackageManager packageManager;
    private final String THIS_PACKAGE = "com.tumuyan.fixedplay";
    private long splash_time = 0;
    private String mode = "r2";
    private String action = "";
    private ImageView imgview = null;
    private static final int GO = 1;
    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GO) {
                openLauncher();
                splash_time = 0;
            }
        }
    };
    private boolean isDebug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isDebug) {
            openATVLauncher();
        }
        packageManager = getPackageManager();
        Log.w("wqs", "Create");
        SharedPreferences read = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        final String splash_img = read.getString("splash_img", "");
        splash_time = read.getInt("splash_time", 0);
        Log.w("wqs", "Create, splash_time = " + splash_time);
        if (splash_time > 0) {
            handler.sendEmptyMessageDelayed(GO, splash_time);
            setContentView(R.layout.splash_activity);
            imgview = findViewById(R.id.splash_img);
            imgview.setOnClickListener(v -> skip_splash());
            /*Glide.with(getBaseContext())
                    .load(splash_img)
                    .placeholder(R.drawable.ic_baseline_hourglass_top_24)
                    .into(imgview);*/
        }

    }

    private void skip_splash() {
        if (imgview != null) {
            handler.removeMessages(GO);
            splash_time = 0;
            imgview.setImageDrawable(null);
            openLauncher();
        }
    }

    @Override
    public void onResume() {
        Log.w("wqs", String.format("Resume, splash %d", splash_time));
        if (splash_time <= 0) {
            openLauncher();
        }
        super.onResume();
    }


    private void openLauncher() {
        SharedPreferences read = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        String app = read.getString("app", "");
        String claseName = read.getString("class", "");
        String uri = read.getString("uri", "");
        mode = read.getString("mode", "r2");
        action = read.getString("action", "");
        Log.i("wqs.go()", "mode=" + mode + ", packagename=" + app);

        boolean apply2nd = read.getBoolean("apply2nd", false);
        long lastTime = read.getLong("lastTime", 0);
        int combo = read.getInt("combo", 0);

        final String app_2nd = read.getString("app_2nd", "");
        final String class_2nd = read.getString("class_2nd", "");
        Log.w("2nd", app_2nd + ", combo=" + combo);

        if (apply2nd) {

            long time = System.currentTimeMillis();
            if (combo > 3)
                combo = 0;
            else {
                if (time - lastTime < 500) {
                    combo++;
                } else {
                    combo = 0;
                }
            }

            Log.w("combo", "" + combo);
            {
                SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_MULTI_PROCESS).edit();
                editor.putInt("combo", combo);
                editor.putLong("lastTime", time);
                editor.commit();
            }
            if (combo > 1) {
                if (!app_2nd.isEmpty()) {
                    Intent intent = packageManager.getLaunchIntentForPackage(app_2nd);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_HOME);
                        Log.w("2nd2", "length>0 -> intent not null");
                        startActivity(intent);
                    } else {
                        // Toast.makeText(com.efren.launcher.SettingActivity.this,R.string.error_could_not_start,Toast.LENGTH_SHORT).show();

                        intent = new Intent();
                        intent.setAction(Intent.ACTION_MAIN);
                        if (class_2nd.length() > 5) {
                            intent.setClassName(app_2nd, class_2nd);
                        }
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("startActivity", app_2nd + ", " + class_2nd);
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, R.string.error_could_not_start, Toast.LENGTH_SHORT).show();
                            intent = new Intent(MainActivity.this, SettingActivity.class);
                            startActivity(intent);
                        }
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                    startActivity(intent);
                }
                return;
            }
        }

        if (!app.isBlank() && !app.equals(THIS_PACKAGE)) {
            Intent intent = new Intent();
            switch (mode) {
                case "r2": {
                    Log.w("wqs", mode);
                    intent = packageManager.getLaunchIntentForPackage(app);
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        startActivity(intent);
                    }
                    break;
                }

                case "r1":
                    if (claseName.length() > 5) {
                        intent.setClassName(app, claseName);
                        startActivity(intent);
                    } else {
                        intent = packageManager.getLaunchIntentForPackage(app);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        this.startActivity(intent);
                    }
                    break;
                case "beta":
                    if (!action.isEmpty() && !"none".equals(action)) {
                        intent.setAction(action);
                    }
                    if (claseName.length() > 5) {
                        intent.setClassName(app, claseName);
                    } else {
                        intent = packageManager.getLaunchIntentForPackage(app);
                        if (intent != null) {
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                    }
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //   Toast.makeText(this,getString(R.string.toast_main_start_error,mode)"模式"+mode + "启动应用时发生了错误",Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, getString(R.string.toast_main_start_error, mode), Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this, SettingActivity.class);
                        startActivity(intent);

                    }
                    break;

                case "uri": {
                    Uri u = Uri.parse(uri);
                    intent = new Intent(Intent.ACTION_VIEW, u);
                    if (!claseName.isEmpty()) {
                        intent.setClassName(app, claseName);
                    } else {
                        intent.setPackage(app);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    break;
                }

                /*   似乎没用*/
                case "uri_dail": {
                    Uri u = Uri.parse(uri);
                    intent = new Intent(Intent.ACTION_DIAL, u);
                    if (!claseName.isEmpty()) {
                        intent.setClassName(app, claseName);
                    } else {
                        intent.setPackage(app);
                    }
                    startActivity(intent);
                    break;
                }

                case "uri_file": {
                    intent = new Intent("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (!claseName.isEmpty()) {
                        intent.setClassName(app, claseName);
                    } else {
                        intent.setPackage(app);
                    }
                    Uri u = Uri.fromFile(new File(uri));
                    intent.setDataAndType(u, "*/*");
                    startActivity(intent);
                    break;
                }


            }
            finish();
        } else {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            skip_splash();
            return true;
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

    private void openATVLauncher() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER"); // 指定其中一个 category
        intent.setPackage("ca.dstudio.atvlauncher.pro"); // 指定目标包名
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 确保从非 Activity 环境启动
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }
}




