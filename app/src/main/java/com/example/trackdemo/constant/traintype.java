package com.example.trackdemo.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({TrainTypes.WALK_TRAIN, TrainTypes.RUNNING_TRAIN, TrainTypes.CYCLING_TRAIN})
public @interface traintype {

}
