package com.example.trackdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.trackdemo.bean.TrainData;
import com.example.trackdemo.constant.TargetTypes;
import com.example.trackdemo.constant.TrainTypes;
import com.example.trackdemo.constant.targettype;
import com.example.trackdemo.constant.traintype;
import com.example.trackdemo.db.LocalDBHelper;
import com.example.trackdemo.dialog.LockDialog;
import com.example.trackdemo.dialog.PauseDialog;
import com.example.trackdemo.fragments.MapFragment;
import com.example.trackdemo.fragments.TrainDataFragment;
import com.example.trackdemo.fragments.TrainResultFragment;
import com.example.trackdemo.fragments.TrainSplashFragment;
import com.example.trackdemo.fragments.TrainTrackerFragment;
import com.example.trackdemo.service.LocationUpdatesService;
import com.example.trackdemo.service.PermissionHelper;
import com.example.trackdemo.service.TrackerIconHelper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.trackdemo.TrainActivity.FragmentIndexs.TAB_DATA;
import static com.example.trackdemo.TrainActivity.FragmentIndexs.TAB_MAP;
import static com.example.trackdemo.TrainActivity.FragmentIndexs.TAB_RESULT;
import static com.example.trackdemo.TrainActivity.FragmentIndexs.TAB_SPLASH;

public class TrainActivity extends FragmentActivity implements View.OnClickListener {


    private static final String WEIGHT = "WEIGHT";
    private static final String TARGET_TYPE = "TARGET_TYPE";
    private static final String TARGET = "TARGET";
    private static final String TRAIN_TYPE = "TRAIN_TYPE";
    /**
     * 默认体重: 60kg
     */
    public static final float WEIGHT_DEFAULT = 60f;
    /**
     * 默认目标类型: 没有目标类型
     */
    public static final int TARGET_TYPE_DEFAULT = -1000;
    /**
     * 默认目标值: 没有目标
     */
    public static final float TARGET_DEFAULT = -1000;
    /**
     * 默认的训练类型: 跑步
     */
    public static final int TRAIN_TYPE_DEFAULT = TrainTypes.RUNNING_TRAIN;

    private static final String TAG = "ZJLog_TrainAc";
    private TrainTrackerFragment trainTrackerFragment;
    private TrainDataFragment trainDataFragment;
    private TrainResultFragment trainResultFragment;
    final PermissionHelper.Listener listener = () -> doAfterPermissionsGranted();
    private LocationUpdatesService mService;
    private boolean mBound = false;
    private Fragment[] fragments = new Fragment[FragmentIndexs.MAX_FRAGMENTS_NUM];
    private FragmentManager fragmentManager;
    private int currentIndex = 0;
    private Fragment currentFragment;
    /**
     * 记录随(定位和时间)变化的运动数据
     */
    private TrainData trainData = new TrainData();
    /**
     * 传入参数,用户当前的体重
     */
    private float weight;
    /**
     * 传入参数,目标类型(参考 {@link targettype})
     */
    private int targetType;
    /**
     * 记录当前的GPS信号强度(默认: 低强度)
     */
    private int curStrength = GPSStrengthes.SMALL;
    private Button btSwitch;
    private Button btPause;
    private Button btLock;
    /**
     * 是否暂停运动
     */
    private volatile boolean isTrainPause = false;

    /**
     * 计时器
     */
    private TimerHelper timerHelper;

    private void doAfterPermissionsGranted() {
        trainTrackerFragment.getmMapFragment().initMap();
        initMenu();
    }

    /**
     * 初始化操作菜单&&加载底部banner广告
     */
    private void initMenu() {
        btSwitch = findViewById(R.id.bt_switch);
        btPause = findViewById(R.id.bt_pause);
        btLock = findViewById(R.id.bt_lock);
        btSwitch.setOnClickListener(this);
        btPause.setOnClickListener(this);
        btLock.setOnClickListener(this);
        findViewById(R.id.ll_menu).setVisibility(View.VISIBLE);
        findViewById(R.id.tv_ad).setVisibility(View.VISIBLE);
    }

    private void hideMenuAndAd() {
        findViewById(R.id.ll_menu).setVisibility(View.GONE);
        findViewById(R.id.tv_ad).setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        trainData.trainId = LocalDBHelper.getInstance(this).generateTrainId();
        weight = getIntent().getFloatExtra(WEIGHT, WEIGHT_DEFAULT);
        targetType = getIntent().getIntExtra(TARGET_TYPE, TARGET_TYPE_DEFAULT);
        trainData.hasTarget = targetType != TARGET_TYPE_DEFAULT;
        trainData.target = getIntent().getFloatExtra(TARGET, TARGET_DEFAULT);
        trainData.trainType = getIntent().getIntExtra(TRAIN_TYPE, TRAIN_TYPE_DEFAULT);

        // 初始化各个fragment
        fragmentManager = getSupportFragmentManager();
        fragments[TAB_SPLASH] = new TrainSplashFragment();
        ((TrainSplashFragment) fragments[TAB_SPLASH]).setListener(() -> {
            //开屏动画播放完毕开始加载主界面

            // 初始化地图Fragment
            trainTrackerFragment.getmMapFragment().regiterListener(() -> startLocationService());

            // 请求必要权限
            PermissionHelper.registerListener(listener);
            PermissionHelper.requestPermissionIfNeed(TrainActivity.this);

            switchTo(TAB_DATA);
        });
        fragments[TAB_MAP] = trainTrackerFragment = new TrainTrackerFragment();
        fragments[TAB_DATA] = trainDataFragment = new TrainDataFragment();
        fragments[TAB_RESULT] = trainResultFragment = new TrainResultFragment();

        prepareFragments();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 解注册权限监听
        PermissionHelper.unRegisterListener(listener);
        // 关闭定位服务
        stopLocationService();
        // 关闭计时器
        if (timerHelper != null) {
            timerHelper.stop();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_switch) {
            if (currentIndex == TAB_DATA) {
                switchTo(TAB_MAP);
            } else {
                switchTo(TAB_DATA);
            }
        } else if (v.getId() == R.id.bt_pause) {
            doPauseTrain();
            final PauseDialog pauseDialog = new PauseDialog(this);
            pauseDialog.setListener(new PauseDialog.Listener() {
                @Override
                public void onClickContinue() {
                    pauseDialog.dismiss();
                    doResumeTrain();
                }

                @Override
                public void onClickFinsh() {
                    pauseDialog.dismiss();
                    doFinishTrain();
                }
            });
            pauseDialog.show();
        } else if (v.getId() == R.id.bt_lock) {
            final LockDialog lockDialog = new LockDialog(this);
            lockDialog.setListener(new LockDialog.Listener() {
                @Override
                public void onClickUnlock() {
                    lockDialog.dismiss();
                }
            });
            lockDialog.show();
        }
    }

    /**
     * 停止运动
     */
    private void doFinishTrain() {
        doPauseTrain();

        trainResultFragment.setListener(new TrainResultFragment.Listener() {
            @Override
            public void onClickExits(String feel) {
                trainData.feel = feel;
                trainData.locations = trainTrackerFragment.getmMapFragment().getmCurLocations();
                if (TextUtils.isEmpty(trainData.url)) {
                    trainResultFragment.getmMapFragment().getSnapShot(new MapFragment.SnapShotListener() {
                        @Override
                        public void onSnapShotReady(Uri uri) {
                            if (uri != null) {
                                trainData.url = uri.getPath();
                            }
                            doRecordUpdate(trainData);
                            finish();
                        }
                    });
                    return;
                }
                doRecordUpdate(trainData);
                finish();
            }

            @Override
            public void onMapShown() {
                trainResultFragment.getmMapFragment().getSnapShot(new MapFragment.SnapShotListener() {
                    @Override
                    public void onSnapShotReady(Uri uri) {
                        if (uri != null) {
                            trainData.url = uri.getPath();
                        }
                    }
                });
            }
        });
        trainData.date = System.currentTimeMillis();
        trainResultFragment.setResultMap(trainTrackerFragment.getmMapFragment().getmCurLocations());
        trainResultFragment.setTrainData(trainData);
        hideMenuAndAd();
        switchTo(TAB_RESULT);
    }

    /**
     * 更新运动数据到数据库
     *
     * @param trainData 锻炼数据
     */
    private void doRecordUpdate(TrainData trainData) {
        Log.d(TAG, "doRecordUpdate: 更新运动数据到数据库");
        LocalDBHelper.getInstance(TrainActivity.this).updateOrInsertRecord(trainData);
    }

    /**
     * 暂停运动
     */
    private void doPauseTrain() {
        isTrainPause = true;
        if (timerHelper != null) {
            timerHelper.pause();
        }
    }

    /**
     * 恢复运动
     */
    private void doResumeTrain() {
        isTrainPause = false;
        if (timerHelper != null) {
            timerHelper.resume();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // out-funcs


    /**
     * 启动
     *
     * @param weight     体重(单位:kg), 默认参考{@link #WEIGHT_DEFAULT}
     * @param targetType 目标类型(参考 {@link targettype})
     * @param target     目标值
     * @param traintype  锻炼类型(参考 {@link traintype})
     */
    public static void launch(Context context, float weight, @targettype int targetType, float target, @traintype int traintype) {
        final Intent intent = new Intent(context, TrainActivity.class);
        intent.putExtra(WEIGHT, weight);
        intent.putExtra(TARGET_TYPE, targetType);
        intent.putExtra(TARGET, target);
        intent.putExtra(TRAIN_TYPE, traintype);
        context.startActivity(intent);
    }

    /**
     * 启动(没有目标,自由锻炼)
     */
    public static void launch(Context context, float weight, @traintype int traintype) {
        final Intent intent = new Intent(context, TrainActivity.class);
        intent.putExtra(WEIGHT, weight);
        intent.putExtra(TARGET_TYPE, TARGET_TYPE_DEFAULT);
        intent.putExtra(TARGET, TARGET_DEFAULT);
        intent.putExtra(TRAIN_TYPE, traintype);
        context.startActivity(intent);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // fragment 调度

    /**
     * 预加载一些fragment
     */
    private void prepareFragments() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.content, fragments[TAB_MAP], "" + TAB_MAP);
        transaction.hide(fragments[TAB_MAP]);
        transaction.add(R.id.content, fragments[TAB_DATA], "" + TAB_DATA);
        transaction.hide(fragments[TAB_DATA]);
        transaction.add(R.id.content, fragments[TAB_SPLASH], "" + TAB_SPLASH);
        transaction.commit();
    }

    /**
     * 定义Fragment的层次
     */
    static class FragmentIndexs {
        public static final int MAX_FRAGMENTS_NUM = 4;
        public static final int TAB_SPLASH = 0;
        public static final int TAB_MAP = 1;
        public static final int TAB_DATA = 2;
        public static final int TAB_RESULT = 3;
    }

    /**
     * 显示指定索引的Fragment
     *
     * @param currentIndex 指定索引的Fragment
     */
    private void switchTo(int currentIndex) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        if (!fragments[currentIndex].isAdded()) {
            transaction.add(R.id.content, fragments[currentIndex], "" + currentIndex);  //第三个参数为添加当前的fragment时绑定一个tag
        } else {
            transaction.show(fragments[currentIndex]);
        }

        currentFragment = fragments[currentIndex];
        this.currentIndex = currentIndex;
        transaction.commit();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 权限请求

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 计时器

    /**
     * 初始化计时器
     */
    private void initTimer() {
        if (timerHelper == null) {
            timerHelper = new TimerHelper();
            timerHelper.setListener(seconds -> {
                onTrainDataUpdate();
            });
        }
    }

    /**
     * 运动数据发生变化
     */
    private void onTrainDataUpdate() {
        final long seconds = timerHelper.getSeconds();
        String time_unit = getString(R.string.unit_time);
        String time_value = seconds2String(seconds);

        String distance_unit = getString(R.string.km);
        float distance = trainTrackerFragment.getmMapFragment().getDistance();
        String distance_value = String.format("%.2f", distance);

        String speed_unit = getString(R.string.unit_speed);
        // 速度: 分/千米
        int speed_seconds = distance <= 0 ? 0 : (int) (seconds / distance);
        String speed_value = seconds2String(speed_seconds);

        String kcal_unit = getString(R.string.kcal);
        final float culKcal = culKcal(weight, distance);
        String kcal_value = String.format("%.2f", culKcal);

        trainData.distance = distance;
        trainData.seconds = seconds;
        trainData.speed_seconds = speed_seconds;
        trainData.kcal = culKcal;

        if (targetType == TargetTypes.TIME) {
            trainData.top = time_value;
            trainData.top_unit = time_unit;
            trainData.left = distance_value;
            trainData.left_unit = distance_unit;
            trainData.right = kcal_value;
            trainData.right_unit = kcal_unit;

            trainData.process = seconds;
        } else if (targetType == TargetTypes.KCAL) {
            trainData.top = kcal_value;
            trainData.top_unit = kcal_unit;
            trainData.left = time_value;
            trainData.left_unit = time_unit;
            trainData.right = distance_value;
            trainData.right_unit = distance_unit;

            trainData.process = culKcal;
        } else {
            trainData.top = distance_value;
            trainData.top_unit = distance_unit;
            trainData.left = time_value;
            trainData.left_unit = time_unit;
            trainData.right = kcal_value;
            trainData.right_unit = kcal_unit;

            trainData.process = distance;
        }

        trainData.mid = speed_value;
        trainData.mid_unit = speed_unit;
        trainData.strength = curStrength;

        if (trainData.getPercent() >= trainData.getMax()) {
            // 目标达成
            doFinishTrain();
        } else {
            trainTrackerFragment.update(trainData);
            trainDataFragment.update(trainData);
        }
    }

    /**
     * 是否有目标
     *
     * @param targetType
     * @return
     */
    private boolean hasTarget(int targetType) {
        return targetType != TARGET_TYPE_DEFAULT;
    }

    /**
     * 计算卡路里
     *
     * @param weight   体重(单位: kg)
     * @param distance 距离(单位: km)
     */
    private float culKcal(float weight, float distance) {
        return weight * distance * 1.036f;
    }

    /**
     * 秒数转换成 00:00:00 格式
     *
     * @param seconds 秒数
     */
    private String seconds2String(long seconds) {
        if (seconds <= 0) {
            return "00:00";
        }
        long hour = seconds / (60 * 60);
        String hour_s = hour < 10 ? ("0" + hour) : "" + hour;

        long left = seconds % (60 * 60);
        long min = left / 60;
        String min_s = min < 10 ? ("0" + min) : "" + min;

        left = seconds % 60;
        long s = left;
        String s_s = s < 10 ? ("0" + s) : "" + s;


        return hour_s + ":" + min_s + ":" + s_s;
    }

    /**
     * 定义计时器
     */
    static class TimerHelper {

        private Timer timer;
        private long seconds = 0;
        private Listener listener = null;

        private interface Listener {
            /**
             * 计时更新
             *
             * @param seconds 当前累计时间(单位:s)
             */
            void onTimerChanged(long seconds);
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        /**
         * 获取当前计时的秒数(单位:s)
         */
        long getSeconds() {
            return seconds;
        }

        /**
         * 开始计时
         */
        void start() {
            reset();
            resume();
        }

        /**
         * 接着上次恢复计时
         */
        void resume() {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // 超过long最大范围将不再计时
                    if (seconds < Long.MAX_VALUE) {
                        seconds++;
                        if (listener != null) {
                            listener.onTimerChanged(seconds);
                        }
                    }
                }
            }, 0, 1000);
        }

        /**
         * 暂停计时
         */
        void pause() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        /**
         * 停止计时
         */
        void stop() {
            reset();
        }

        /**
         * 重置计时器
         */
        private void reset() {
            if (timer != null) {
                seconds = 0;
                timer.cancel();
                timer = null;
            }
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 定位服务

    private void stopLocationService() {
        if (mBound) {
            unbindService(mServiceConnection);
            stopService(new Intent(this, LocationUpdatesService.class));
        }
    }

    private void startLocationService() {
        ContextCompat.startForegroundService(TrainActivity.this, new Intent(TrainActivity.this, LocationUpdatesService.class));
        bindService(new Intent(TrainActivity.this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();

            // 开启位置监听
            mService.setListener(new LocationUpdatesService.Listener() {
                @Override
                public void onLocationChange(List<Location> locations) {
                    if (isTrainPause) {
                        return;
                    }
                    if (locations != null && !locations.isEmpty()) {
                        trainTrackerFragment.getmMapFragment().updateLocation(locations);
                        onTrainDataUpdate();
                    }
                }

                @Override
                public void onGPSStrengthChange(int strength) {
                    if (isTrainPause) {
                        return;
                    }
                    curStrength = strength;
                }
            });

            // 开启计时装置
            initTimer();
            timerHelper.start();

            mService.start();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };


}