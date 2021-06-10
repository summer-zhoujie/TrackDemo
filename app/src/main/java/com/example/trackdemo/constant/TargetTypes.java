package com.example.trackdemo.constant;

import com.example.trackdemo.TrainActivity;

/**
 * 定义目标的类型: 距离, 时间, 速度, 卡路里, 未知
 */
public class TargetTypes {
    public static final int DISTANCE = 0;
    public static final int TIME = 1;
    public static final int KCAL = 2;
    public static final int SPEED = 3;
    public static final int UNKNOWN = TrainActivity.TARGET_TYPE_DEFAULT;
}
