package com.example.trackdemo.fragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.example.trackdemo.R;
import com.example.trackdemo.service.TrackerIconHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import static com.example.trackdemo.fragments.MapFragment.MAPSTATUS.NONE;
import static com.example.trackdemo.fragments.MapFragment.MAPSTATUS.LOADING;
import static com.example.trackdemo.fragments.MapFragment.MAPSTATUS.OK;

public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "ZJLog_Maps";
    /**
     * 定义最小的移动距离识别(单位: 米)
     */
    private static final double MIN_METER_INTERVAL = 1.0;
    /**
     * 定义运动轨迹的线宽
     */
    private static final float INITIAL_STROKE_WIDTH_PX = 15.5f;
    /**
     * 地图的默认缩放比例
     */
    private static final float ZOOM_DEFAILT = 16f;
    /**
     * 规定转动或者移动定位导航图标{@link R.drawable#navigation}的动画时长
     */
    private static final long LOCATION_MARKER_ANIM_DURATION = 500;
    /**
     * 规定移动CAMERA的动画时长
     */
    private static final int CAMERA_ANIM_DURATION = 1000;
    /**
     * Google Map 实例
     */
    private GoogleMap mMap;
    /**
     * 记录了运动过程中产生的轨迹点
     */
    private List<Location> mCurLocations = new ArrayList<>();
    /**
     * 界面控件,我的位置{@link R.drawable#location_icon}
     */
    private ImageView ivMyLocation;
    /**
     * 记录当前的GPS位置信息
     */
    private Location curLast = null;
    /**
     * 保存当前的定位导航图标{@link R.drawable#navigation}实体
     */
    private Marker mPositionMarker;
    /**
     * 记录当前地图的缩放比例
     */
    private float curZoom;
    /**
     * 定义fragment的外部监听
     */
    private Listener listenerOut;
    /**
     * 记录当前地图状态, true 代表已准备好
     */
    private int mapStatus = NONE;
    private SupportMapFragment mapFragment;

    /**
     * 定义地图的加载状态
     */
    static class MAPSTATUS {
        public static final int NONE = 0;
        public static final int LOADING = 1;
        public static final int OK = 2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_google_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivMyLocation = view.findViewById(R.id.iv_mylocation);
        ivMyLocation.setOnClickListener(this);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(false);
        mMap.setOnCameraIdleListener(() -> {
            curZoom = mMap.getCameraPosition().zoom;
        });
        ivMyLocation.setVisibility(View.VISIBLE);
        mapStatus = OK;
        if (listenerOut != null) {
            listenerOut.onMapready();
        }
    }

    public void getSnapShot(SnapShotListener listener) {

        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(@Nullable Bitmap bitmap) {
                final Uri uri = TrackerIconHelper.saveBitmap(bitmap, getActivity());
                if (listener != null) {
                    listener.onSnapShotReady(uri);
                }
            }
        });
    };

    public static interface SnapShotListener{
        void onSnapShotReady(Uri uri);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // out-funcs

    /**
     * 设置 locations
     */
    public void setLocations(List<Location> locations) {
        if (locations == null) {
            mCurLocations.clear();
        } else {
            mCurLocations = locations;
        }
    }

    /**
     * 转换成结果页的Fragment
     */
    public void transformToResult() {
        if (mMap != null) {
            if (!mCurLocations.isEmpty()) {
                drawPolyLine(convert2LatLngArray(mCurLocations));
            }
            moveCamera2AjustTrainTracker();
            ivMyLocation.setVisibility(View.GONE);
        }
    }

    private LatLng[] convert2LatLngArray(List<Location> mCurLocations) {
        final LatLng[] latLngs = new LatLng[mCurLocations.size()];
        for (int i = 0; i < mCurLocations.size(); i++) {
            final Location location = mCurLocations.get(i);
            latLngs[i] = convert2LatLng(location);
        }
        return latLngs;
    }

    public List<Location> getmCurLocations() {
        return mCurLocations;
    }

    public interface Listener {
        /**
         * 地图状态准备好
         */
        void onMapready();
    }

    public void regiterListener(@NonNull Listener listener) {
        this.listenerOut = listener;
        if (mapStatus == OK) {
            listener.onMapready();
        }
    }

    /**
     * 初始化地图
     */
    public void initMap() {
        if (mapStatus == LOADING || mapStatus == OK) {
            return;
        }
        mapStatus = LOADING;
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googlemap);
        mapFragment.getMapAsync(MapFragment.this);
    }

    /**
     * 获取当前GPS点位统计的总路程(单位: km)
     */
    public float getDistance() {
        if (mCurLocations.isEmpty()) {
            return 0f;
        }

        float d = 0;
        Location pre = null;
        for (Location cur : mCurLocations) {
            if (pre == null) {
                pre = cur;
                continue;
            }

            d += SphericalUtil.computeDistanceBetween(convert2LatLng(pre), convert2LatLng(cur));
            pre = cur;
        }
        d /= 1000;
        return d;
    }

    /**
     * 坐标点更新
     *
     * @param locations 新的一批坐标点
     */
    public void updateLocation(List<Location> locations) {

        Location last = null;
        final int size = mCurLocations.size();
        if (mCurLocations != null && !mCurLocations.isEmpty()) {
            last = mCurLocations.get(size - 1);
        }

        final ArrayList<LatLng> points = new ArrayList<>();
        if (size >= 2) {
            points.add(convert2LatLng(mCurLocations.get(size - 2)));
        }
        if (size >= 1) {
            points.add(convert2LatLng(mCurLocations.get(size - 1)));
        }

        boolean isNeedMoveCamera = false;
        for (Location location : locations) {
            if (last == null) {
                last = location;
                mCurLocations.add(location);
                isNeedMoveCamera = true;
                continue;
            }
            final double v = SphericalUtil.computeDistanceBetween(convert2LatLng(last), convert2LatLng(location));
            Log.d(TAG, "onGenerateNewLocations: 距离: 米 " + v);
            if (v >= MIN_METER_INTERVAL) {
                mCurLocations.add(location);
                points.add(convert2LatLng(location));
                last = location;
                isNeedMoveCamera = true;
            }
        }

        if (isNeedMoveCamera) {
            final LatLng[] pointsOfLine = points.toArray(new LatLng[points.size()]);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawPolyLine(pointsOfLine);
                }
            }, 0);
            boolean isFirstIn = curLast == null;
            curLast = last;
            moveAndRotateLocationMarker(last);
            moveCamera(last, isFirstIn);
        } else if (!locations.isEmpty()) {
            final Location newOne = locations.get(locations.size() - 1);
            if (newOne.getBearing() != curLast.getBearing()) {
                curLast.setBearing(newOne.getBearing());
                moveAndRotateLocationMarker(curLast);
            }
        }
    }

    /**
     * 移动地图'镜头'以显示全部路线
     */
    public void moveCamera2AjustTrainTracker() {
        if (mCurLocations.isEmpty()) {
            return;
        }

        double s = 0;
        double n = 0;
        double w = 0;
        double e = 0;

        if (mCurLocations.size() == 1) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(convert2LatLng(mCurLocations.get(0)), ZOOM_DEFAILT));
            moveAndRotateLocationMarker(mCurLocations.get(0));
            return;
        }

        boolean isFirst = true;
        for (Location mCurLocation : mCurLocations) {
            if (isFirst) {
                s = n = mCurLocation.getLatitude();
                e = w = mCurLocation.getLongitude();
                isFirst = false;
                continue;
            }
            n = Math.max(mCurLocation.getLatitude(), n);
            s = Math.min(mCurLocation.getLatitude(), s);
            e = Math.max(mCurLocation.getLongitude(), e);
            w = Math.min(mCurLocation.getLongitude(), w);
        }
        LatLngBounds australiaBounds = new LatLngBounds(
                new LatLng(s, w), // SW bounds
                new LatLng(n, e)  // NE bounds
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(australiaBounds, 30));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // in-funcs

    /**
     * 移动镜头到指定目标位置
     *
     * @param target          目标位置
     * @param isNeedResetZoom 是否需要重置缩放比例
     */
    private void moveCamera(Location target, boolean isNeedResetZoom) {
        final LatLng latlngTarget = convert2LatLng(target);
        final CameraPosition.Builder builder = new CameraPosition.Builder().target(latlngTarget)
                .bearing(0)
                .tilt(0);
        if (isNeedResetZoom) {
            curZoom = ZOOM_DEFAILT;
            builder.zoom(curZoom);
        } else {
            builder.zoom(curZoom);
        }
        CameraPosition p = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(p), CAMERA_ANIM_DURATION, null);
    }

    /**
     * 移动并转动定位图标
     *
     * @param target
     * @return
     */
    private LatLng moveAndRotateLocationMarker(Location target) {
        final LatLng latlngTarget = convert2LatLng(target);
        if (mPositionMarker == null && latlngTarget != null) {
            mPositionMarker = mMap.addMarker(new MarkerOptions()
                    .flat(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.navigation))
                    .anchor(0.5f, 0.5f)
                    .position(latlngTarget));
        }
        if (mPositionMarker != null) {
            animateMarker(mPositionMarker, latlngTarget, target.getBearing());
        }
        return latlngTarget;
    }

    /**
     * 转动定位图标的方向
     *
     * @param marker  定位图标
     * @param latLng  位置信息(经纬度)
     * @param bearing
     */
    private void animateMarker(final Marker marker, final LatLng latLng, float bearing) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = LOCATION_MARKER_ANIM_DURATION;

        final Interpolator interpolator = new LinearInterpolator();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                marker.setPosition(latLng);
                marker.setRotation(bearing);
            }
        }, duration);

//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long elapsed = SystemClock.uptimeMillis() - start;
//                float t = interpolator.getInterpolation((float) elapsed / duration);
//
//                double lng = t * latLng.longitude + (1 - t) * startLatLng.longitude;
//                double lat = t * latLng.latitude + (1 - t) * startLatLng.latitude;
//
//                float rotation = (float) (t * bearing + (1 - t) * startRotation);
//
//                marker.setPosition(new LatLng(lat, lng));
//                marker.setRotation(rotation);
//
//                if (t < 1.0) {
//                    // Post again 16ms later.
//                    handler.postDelayed(this, 16);
//                }
//            }
//        });
    }

    /**
     * 画线
     *
     * @param points 点
     */
    private void drawPolyLine(LatLng... points) {
        if (mMap != null) {
            mMap.addPolyline(new PolylineOptions()
                    .add(points)
                    .width(INITIAL_STROKE_WIDTH_PX)
                    .color(Color.BLUE)
                    .startCap(new RoundCap())
                    .endCap(new ButtCap())
                    .jointType(JointType.ROUND)
                    .pattern(null)
                    .geodesic(true)
                    .clickable(false));
        }
    }

    private LatLng convert2LatLng(Location l) {
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_mylocation) {
            if (mMap != null && curLast != null) {
                moveAndRotateLocationMarker(curLast);
                moveCamera(curLast, true);
            }
        }
    }
}