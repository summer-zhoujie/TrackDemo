package com.example.trackdemo.bean;

import android.content.Context;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.trackdemo.GPSStrengthes;
import com.example.trackdemo.R;

import java.util.List;

public class TrainData implements Parcelable  {
    public String top;
    public String top_unit;
    public String left;
    public String left_unit;
    public String mid;
    public String mid_unit;
    public String right;
    public String right_unit;
    /**
     * GPS信号强度, 参考{@link GPSStrengthes}
     */
    public int strength;
    /**
     * 目标值
     * <p>
     * 注意:
     * 如果是时间, 则单位: s
     * 如果是距离, 则单位: km
     * 如果是卡路里, 则单位: kcal
     */
    public float target;
    /**
     * 当前进度值
     * <p>
     * 注意:
     * 如果是时间, 则单位: s
     * 如果是距离, 则单位: km
     * 如果是卡路里, 则单位: kcal
     */
    public float process;
    /**
     * 当前是否有目标
     */
    public boolean hasTarget;
    /**
     * 锻炼类型, 参考 {@link com.example.trackdemo.TrainActivity.TrainTypes}
     */
    public int trainType;
    /**
     * 距离(单位: km)
     */
    public float distance;
    /**
     * 时间(单位: s)
     */
    public long seconds;
    /**
     * 锻炼的配速(单位: s/km, 一千米耗时多少秒)
     */
    public int speed_seconds;
    /**
     * 锻炼的卡路里(单位: kcal)
     */
    public float kcal;
    /**
     * 锻炼后的感想
     */
    public String feel;
    /**
     * 数据库唯一id
     */
    public long trainId;
    /**
     * 运动的点位信息
     */
    public List<Location> locations;
    /**
     * 运动轨迹的截图
     */
    public String url;
    /**
     * 记录最近一次更新时间
     */
    public long date;

    public TrainData(Parcel in) {
        top = in.readString();
        top_unit = in.readString();
        left = in.readString();
        left_unit = in.readString();
        mid = in.readString();
        mid_unit = in.readString();
        right = in.readString();
        right_unit = in.readString();
        strength = in.readInt();
        target = in.readFloat();
        process = in.readFloat();
        hasTarget = in.readByte() != 0;
        trainType = in.readInt();
        distance = in.readFloat();
        seconds = in.readLong();
        speed_seconds = in.readInt();
        kcal = in.readFloat();
        feel = in.readString();
        trainId = in.readLong();
        locations = in.createTypedArrayList(Location.CREATOR);
        url = in.readString();
        date = in.readLong();
    }

    public TrainData() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(top);
        dest.writeString(top_unit);
        dest.writeString(left);
        dest.writeString(left_unit);
        dest.writeString(mid);
        dest.writeString(mid_unit);
        dest.writeString(right);
        dest.writeString(right_unit);
        dest.writeInt(strength);
        dest.writeFloat(target);
        dest.writeFloat(process);
        dest.writeByte((byte) (hasTarget ? 1 : 0));
        dest.writeInt(trainType);
        dest.writeFloat(distance);
        dest.writeLong(seconds);
        dest.writeInt(speed_seconds);
        dest.writeFloat(kcal);
        dest.writeString(feel);
        dest.writeLong(trainId);
        dest.writeTypedList(locations);
        dest.writeString(url);
        dest.writeLong(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrainData> CREATOR = new Creator<TrainData>() {
        @Override
        public TrainData createFromParcel(Parcel in) {
            return new TrainData(in);
        }

        @Override
        public TrainData[] newArray(int size) {
            return new TrainData[size];
        }
    };

    /**
     * 计算进度条的最大值(默认用1000格表示)
     */
    public int getMax() {
        return 1000;
    }

    /**
     * 计算进度当前占多少格
     *
     * @return
     */
    public int getPercent() {
        if (target <= 0) {
            return 0;
        }

        return (int) (process / target * getMax());
    }

    /**
     * 获取锻炼类型的描述
     */
    public String getTrainTypeDesc(Context context) {
        final String[] stringArray = context.getResources().getStringArray(R.array.traintypes_choose_array);
        return stringArray[trainType];
    }
    /**
     * 获取合适的时间描述 00:00:00
     * @return
     */
    public String getTimDesc() {
        return seconds2String(seconds);
    }

    /**
     * 获取合适的锻炼速度描述 00:00 分/千米
     * @return
     */
    public String getSpeedSecondDesc() {
        int speed_seconds = distance <= 0 ? 0 : (int) (seconds / distance);
        String speed_value = seconds2String(speed_seconds);
        return speed_value;
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
}
