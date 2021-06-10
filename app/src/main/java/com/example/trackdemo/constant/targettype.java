package com.example.trackdemo.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({TargetTypes.TIME, TargetTypes.KCAL, TargetTypes.DISTANCE})
public @interface targettype {

}
