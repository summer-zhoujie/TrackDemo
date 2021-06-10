/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.trackdemo.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.trackdemo.GPSStrengthes;
import com.example.trackdemo.R;
import com.example.trackdemo.TrainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class LocationUpdatesService extends Service {

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final String TAG = "ZJLogLocationHelper";
    private static final String CHANNEL_ID = "channel_location";
    private static final int NOTIFICATION_ID = 12345678;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location curLocation;
    private final IBinder mBinder = new LocalBinder();
    private Listener listener = null;
    private NotificationManager mNotificationManager;
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "onLocationResult: " + locationResult.toString());
                if (!locationResult.getLocations().isEmpty()) {
                    curLocation = locationResult.getLastLocation();
                    if (listener != null) {
                        listener.onLocationChange(locationResult.getLocations());
                    }
                }
            }
        };

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        startForeground(NOTIFICATION_ID, getNotification());
    }

    private Notification getNotification() {

        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = curLocation == null ? "Unknown location" :
                "(" + curLocation.getLatitude() + ", " + curLocation.getLongitude() + ")";

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TrainActivity.class), 0);

        CharSequence title = getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(title)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    public void start() {
        Log.d(TAG, "start: ");
        // 获取上次记录的位置
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            final ArrayList<Location> list = new ArrayList<>();
                            list.add(task.getResult());
                            mLocationCallback.onLocationResult(LocationResult.create(list));
                            Log.d(TAG, "onComplete: " + task.getResult());
                        } else {
                            Log.w(TAG, "Failed to get location.");
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "onFailure: " + Log.getStackTraceString(e)));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }

        startListenGPSStrength();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void stop() {
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }

        stopListenGPSStrength();
    }

    public class LocalBinder extends Binder {
        public LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onLocationChange(List<Location> location);

        /**
         * GPS信号强度发生改变
         *
         * @param strength 强度, 参考{@link GPSStrengthes}
         */
        void onGPSStrengthChange(@Strengthes int strength);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GPSStrengthes.SMALL, GPSStrengthes.MIDDLE, GPSStrengthes.HIGH})
    public @interface Strengthes {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GPS信号强度

    final GpsStatus.Listener listenerGPSStautsChange = event -> {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            updateGPSStrength();
        }
    };

    /**
     * 开启GPS信号强度的监听
     */
    private void startListenGPSStrength() {
        stopListenGPSStrength();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(listenerGPSStautsChange);
        updateGPSStrength();
    }

    private void updateGPSStrength() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        GpsStatus gpsStatus = null;
        synchronized (this) {
            if (locationManager != null) {
                gpsStatus = locationManager.getGpsStatus(null);
            }
        }

        if (gpsStatus != null) {
            //获取卫星颗数的默认最大值
            int maxSatellites = gpsStatus.getMaxSatellites();
            //创建一个迭代器保存所有卫星
            Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
            int totalCount = 0;
            int inUse = 0;
            while (iters.hasNext() && totalCount <= maxSatellites) {
                GpsSatellite s = iters.next();
                totalCount++;
                if (s.usedInFix()) {
                    inUse++;
                }
            }
            int strength = GPSStrengthes.HIGH;
            if (inUse <= 4) {
                strength = GPSStrengthes.SMALL;
            } else if (inUse <= 7) {
                strength = GPSStrengthes.MIDDLE;
            }

            if (listener != null) {
                listener.onGPSStrengthChange(strength);
            }
        }
    }

    /**
     * 关闭GPS信号强度的监听
     */
    private void stopListenGPSStrength() {
        if (locationManager == null)
            return;
        locationManager.removeGpsStatusListener(listenerGPSStautsChange);
        locationManager = null;
    }
}
