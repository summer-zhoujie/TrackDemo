package com.example.trackdemo.service;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.example.trackdemo.BuildConfig;
import com.example.trackdemo.R;

public class PermissionHelper {

    private static Listener listener = null;

    public interface Listener {
        void onPermissionGranted();
    }

    public static final void registerListener(Listener listener) {
        PermissionHelper.listener = listener;
    }

    public static final void unRegisterListener(Listener listener) {
        if (PermissionHelper.listener == listener) {
            PermissionHelper.listener = null;
        }
    }

    public static boolean requestPermissionIfNeed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
            if (i != PermissionChecker.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
                return true;
            }
        }
        if (listener != null) {
            listener.onPermissionGranted();
        }
        return false;
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, Activity activity) {
        if (requestCode == 1000) {
            boolean hasGrant = false;
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PermissionChecker.PERMISSION_GRANTED) {
                    if (listener != null) {
                        listener.onPermissionGranted();
                    }
                    hasGrant = true;
                    break;
                }
            }

            if (!hasGrant) {
                final Dialog dialog = new AlertDialog
                        .Builder(activity)
                        .setMessage(R.string.permission_granted_hint)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.goto_set, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                    //提示用户前往设置界面自己打开权限
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                } else {
                                    requestPermissionIfNeed(activity);
                                }
                            }
                        })
                        .create();
                dialog.show();
            }
        }
    }
}
