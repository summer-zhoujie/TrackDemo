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
 * 纯数据的运动界面
 */
public class TrainDataFragment extends Fragment {


    private TextView tvGpsStrength;
    private TextView tvTargetValue;
    private TextView tvTargetUnit;
    private ProgressBar processBar;
    private TextView tvLeftValue;
    private TextView tvLeftUnit;
    private TextView tvMidValue;
    private TextView tvMidUnit;
    private TextView tvRightValue;
    private TextView tvRightUnit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGpsStrength = view.findViewById(R.id.tv_gps_strength);
        tvTargetValue = view.findViewById(R.id.tv_target_value);
        tvTargetUnit = view.findViewById(R.id.tv_target_unit);
        processBar = view.findViewById(R.id.process_bar);
        tvLeftValue = view.findViewById(R.id.tv_left_value);
        tvLeftUnit = view.findViewById(R.id.tv_left_unit);
        tvMidValue = view.findViewById(R.id.tv_mid_value);
        tvMidUnit = view.findViewById(R.id.tv_mid_unit);
        tvRightValue = view.findViewById(R.id.tv_right_value);
        tvRightUnit = view.findViewById(R.id.tv_right_unit);
    }

    public void update(TrainData trainData) {
        if (trainData != null && tvGpsStrength != null) {
            tvGpsStrength.post(new Runnable() {
                @Override
                public void run() {
                    tvGpsStrength.setText("信号强度: " + trainData.strength);
                    tvTargetValue.setText(trainData.top);
                    tvTargetUnit.setText(trainData.top_unit);
                    tvLeftValue.setText(trainData.left);
                    tvLeftUnit.setText(trainData.left_unit);
                    tvMidValue.setText(trainData.mid);
                    tvMidUnit.setText(trainData.mid_unit);
                    tvRightValue.setText(trainData.right);
                    tvRightUnit.setText(trainData.right_unit);

                    processBar.setVisibility(trainData.hasTarget?View.VISIBLE:View.GONE);
                    if (trainData.hasTarget) {
                        processBar.setMax(trainData.getMax());
                        processBar.setProgress(trainData.getPercent());
                    }
                }
            });
        }
    }
}