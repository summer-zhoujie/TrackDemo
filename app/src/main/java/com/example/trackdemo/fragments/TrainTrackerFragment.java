package com.example.trackdemo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trackdemo.R;
import com.example.trackdemo.bean.TrainData;

/**
 * 运动的轨迹视图
 */
public class TrainTrackerFragment extends Fragment {

    private TextView mTvTargetValue;
    private TextView mTvTargetUnit;
    private TextView mTvLeftValue;
    private TextView mTvLeftUnit;
    private TextView mTvMidValue;
    private TextView mTvMidUnit;
    private TextView mTvRightValue;
    private TextView mTvRightUnit;
    private MapFragment mMapFragment;
    private ProgressBar processBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train_tracker, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTargetValue = view.findViewById(R.id.tv_target_value);
        mTvTargetUnit = view.findViewById(R.id.tv_target_unit);
        mTvLeftValue = view.findViewById(R.id.tv_left_value);
        mTvLeftUnit = view.findViewById(R.id.tv_left_unit);
        mTvMidValue = view.findViewById(R.id.tv_mid_value);
        mTvMidUnit = view.findViewById(R.id.tv_mid_unit);
        mTvRightValue = view.findViewById(R.id.tv_right_value);
        mTvRightUnit = view.findViewById(R.id.tv_right_unit);
        processBar = view.findViewById(R.id.process_bar);
        mMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
    }


    public void update(TrainData trainData) {
        if (mTvTargetValue != null && trainData != null) {
            mTvTargetUnit.post(() -> {
                mTvTargetValue.setText(trainData.top);
                mTvTargetUnit.setText(trainData.top_unit);
                mTvLeftValue.setText(trainData.left);
                mTvLeftUnit.setText(trainData.left_unit);
                mTvMidValue.setText(trainData.mid);
                mTvMidUnit.setText(trainData.mid_unit);
                mTvRightValue.setText(trainData.right);
                mTvRightUnit.setText(trainData.right_unit);

                processBar.setVisibility(trainData.hasTarget?View.VISIBLE:View.GONE);
                if (trainData.hasTarget) {
                    processBar.setMax(trainData.getMax());
                    processBar.setProgress(trainData.getPercent());
                }
            });
        }
    }

    public MapFragment getmMapFragment() {
        return mMapFragment;
    }
}