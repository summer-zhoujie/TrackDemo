package com.example.trackdemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.example.trackdemo.bean.TrainData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhangxiaohai on 2018/4/16.
 */

public class LocalDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "com_train_track.db";

    private static final String TABLE_WORKOUT = "table_train";
    private static final String _ID = "id";
    /**
     * 锻炼的id
     */
    private static final String _TRAIN_ID = "train_id";
    /**
     * 锻炼的时间点(单位: long时间戳)
     */
    private static final String _DATE = "date";
    /**
     * 锻炼的类型, 参考{@link com.example.trackdemo.constant.TrainTypes}
     */
    private static final String _TRAIN_TYPE = "train_type";
    /**
     * 锻炼的里程(单位: km)
     */
    private static final String _DISTANCE = "distance";
    /**
     * 锻炼的耗时(单位: s)
     */
    private static final String _TRAIN_TIME = "train_time";
    /**
     * 锻炼的卡路里(单位: kcal)
     */
    private static final String _TRAIN_KCAL = "train_kcal";
    /**
     * 锻炼的配速(单位: s/km, 一千米耗时多少秒)
     */
    private static final String _TRAIN_SPEED = "train_speed";
    /**
     * 锻炼的轨迹图(单位: string , 存储的本地文件的url)
     */
    private static final String _TRAIN_TRACKER = "train_tracker";
    /**
     * 锻炼的轨迹点(单位: string , 'lat0,lng0,lat1,lng1...')
     */
    private static final String _TRAIN_POINTS = "train_points";
    /**
     * 记录感想
     */
    private static final String _TRAIN_FEEL = "train_feel";
    private static final String TAG = "ZJLog_traindb";

    private final Object mLock = new Object();
    private final Context context;
    public static LocalDBHelper mLocalDBHelper;

    public static LocalDBHelper getInstance(Context context) {
        if (mLocalDBHelper == null) {
            mLocalDBHelper = new LocalDBHelper(context.getApplicationContext());
        }
        return mLocalDBHelper;
    }

    public LocalDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_WORKOUT + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                _TRAIN_ID + " INTEGER UNIQUE," +
                _DATE + " INTEGER," +
                _TRAIN_TYPE + " INTEGER," +
                _DISTANCE + " INTEGER," +
                _TRAIN_TIME + " INTEGER," +
                _TRAIN_KCAL + " INTEGER," +
                _TRAIN_SPEED + " INTEGER," +
                _TRAIN_TRACKER + " TEXT," +
                _TRAIN_POINTS + " TEXT," +
                _TRAIN_FEEL + " TEXT" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 更新数据
     */
    public long updateOrInsertRecord(TrainData trainData) {

        SQLiteDatabase db = this.getWritableDatabase();
        synchronized (mLock) {
            Cursor cursor = db.query(
                    TABLE_WORKOUT,
                    new String[]{_TRAIN_ID},
                    _TRAIN_ID + " = ?",
                    new String[]{String.valueOf(trainData.trainId)},
                    null,
                    null,
                    null);

            ContentValues initialValues = new ContentValues();
            initialValues.put(_DATE, System.currentTimeMillis());
            initialValues.put(_TRAIN_TYPE, trainData.trainType);
            initialValues.put(_DISTANCE, trainData.distance);
            initialValues.put(_TRAIN_TIME, trainData.seconds);
            initialValues.put(_TRAIN_KCAL, trainData.kcal);
            initialValues.put(_TRAIN_SPEED, trainData.speed_seconds);
            initialValues.put(_TRAIN_TRACKER, trainData.url);
            initialValues.put(_TRAIN_POINTS, convertLocations2PointsString(trainData.locations));
            initialValues.put(_TRAIN_FEEL, trainData.feel);

            long result = 0;
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    //更新操作
                    result = db.update(TABLE_WORKOUT, initialValues, _TRAIN_ID + "=?", new String[]{String.valueOf(trainData.trainId)});
                } else {
                    //插入操作
                    result = db.insert(TABLE_WORKOUT, null, initialValues);
                }
            } catch (Exception e) {

            } finally {
                if (null != cursor) {
                    cursor.close();
                }
                db.close();
            }
            return result;
        }
    }

    /**
     * 产生一条记录的唯一id
     */
    public long generateTrainId() {
        return System.currentTimeMillis();
    }

    /**
     * 计算里程总和
     */
    public float getTotalDistance() {
        SQLiteDatabase db = this.getWritableDatabase();
        synchronized (mLock) {
            String selectQuery = "select sum(" + _DISTANCE + ") from " + TABLE_WORKOUT;
            try {
                final Cursor cursor = db.rawQuery(selectQuery, null);
                float aFloat;
                if (cursor.moveToFirst()) {
                    return cursor.getFloat(0);
                }
                return cursor.getFloat(0);
            } catch (Exception e) {
                Log.d(TAG, "getTotalDistance: " + Log.getStackTraceString(e));
                return 0;
            }
        }
    }

    public List<TrainData> getAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        synchronized (mLock) {
            List<TrainData> infoList = new ArrayList<>();
            Cursor cur = db.query(TABLE_WORKOUT, null, null, null, null, null, _DATE + " desc");
            if (null != cur && cur.getCount() > 0) {
                cur.moveToFirst();
                do {
                    TrainData info = new TrainData();
                    info.date = cur.getLong(cur.getColumnIndex(_DATE));
                    info.trainType = cur.getInt(cur.getColumnIndex(_TRAIN_TYPE));
                    info.distance = cur.getFloat(cur.getColumnIndex(_DISTANCE));
                    info.seconds = cur.getLong(cur.getColumnIndex(_TRAIN_TIME));
                    info.kcal = cur.getFloat(cur.getColumnIndex(_TRAIN_KCAL));
                    info.speed_seconds = cur.getInt(cur.getColumnIndex(_TRAIN_SPEED));
                    info.url = cur.getString(cur.getColumnIndex(_TRAIN_TRACKER));
                    info.feel = cur.getString(cur.getColumnIndex(_TRAIN_FEEL));
                    info.locations = convertPointsString2Locations(cur.getString(cur.getColumnIndex(_TRAIN_POINTS)));
                    infoList.add(info);
                } while (cur.moveToNext());
                cur.close();
                return infoList;
            } else {
                if (null != cur) cur.close();
                return null;
            }
        }
    }


    /**
     * locations 转换成 String 存入数据库的一个字段
     */
    private String convertLocations2PointsString(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            sb.append(l.getLatitude() + "");
            sb.append(",");
            sb.append(l.getLongitude() + "");
            if (i != locations.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 从数据库的一个字段读出String, 转换成locations
     *
     * @param string
     * @return
     */
    private List<Location> convertPointsString2Locations(String string) {

        final ArrayList<Location> locations = new ArrayList<>();
        if (!TextUtils.isEmpty(string)) {
            final String[] split = string.split(",");
            for (int i = 0; i < split.length - 1; i += 2) {
                double lat = Double.parseDouble(split[i]);
                double lng = Double.parseDouble(split[i + 1]);
                final Location location = new Location("");
                location.setLatitude(lat);
                location.setLongitude(lng);
                locations.add(location);
            }
        }
        return locations;
    }
}
