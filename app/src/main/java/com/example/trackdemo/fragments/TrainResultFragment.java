package com.example.trackdemo.fragments;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trackdemo.R;
import com.example.trackdemo.bean.TrainData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 锻炼结果
 */
public class TrainResultFragment extends Fragment implements View.OnClickListener {

    private Button btExits;
    private TextView tvTargetValue;
    private TextView tvTargetUnit;
    private TextView tvLeftValue;
    private TextView tvLeftUnit;
    private TextView tvMidValue;
    private TextView tvMidUnit;
    private TextView tvRightValue;
    private TextView tvRightUnit;
    private Listener listener;
    private EditText etFeel;
    private TextView tvDate;
    private MapFragment mMapFragment;
    private List<Location> locations;
    private TrainData trainData;
    private boolean isViewCreated = false;

    public interface Listener {

        /**
         * 点击退出
         */
        void onClickExits(String feel);

        /**
         * map完全展示出来
         */
        void onMapShown();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    public MapFragment getmMapFragment() {
        return mMapFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;

        btExits = view.findViewById(R.id.bt_exits);
        btExits.setOnClickListener(this);
        etFeel = view.findViewById(R.id.et_feel);
        tvDate = view.findViewById(R.id.tv_date);
        tvTargetValue = view.findViewById(R.id.tv_target_value);
        tvTargetUnit = view.findViewById(R.id.tv_target_unit);
        tvLeftValue = view.findViewById(R.id.tv_left_value);
        tvLeftUnit = view.findViewById(R.id.tv_left_unit);
        tvMidValue = view.findViewById(R.id.tv_mid_value);
        tvMidUnit = view.findViewById(R.id.tv_mid_unit);
        tvRightValue = view.findViewById(R.id.tv_right_value);
        tvRightUnit = view.findViewById(R.id.tv_right_unit);

        mMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        tvDate.postDelayed(() -> {
            mMapFragment.regiterListener(() -> {
                mMapFragment.setLocations(TrainResultFragment.this.locations);
                mMapFragment.transformToResult();
                tvDate.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onMapShown();
                        }
                    }
                });

            });
            mMapFragment.initMap();
        }, 0);

        updateTrainDataIfViewCreated(trainData);
    }

    private void updateTrainDataIfViewCreated(TrainData data) {
        if (data != null && tvTargetValue != null && isViewCreated) {
            tvTargetValue.post(() -> {
                tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trainData.date));
                tvTargetValue.setText(data.top);
                tvTargetUnit.setText(data.top_unit);
                tvLeftValue.setText(data.left);
                tvLeftUnit.setText(data.left_unit);
                tvMidValue.setText(data.mid);
                tvMidUnit.setText(data.mid_unit);
                tvRightValue.setText(data.right);
                tvRightUnit.setText(data.right_unit);
                if (!TextUtils.isEmpty(trainData.feel)) {
                    etFeel.setFocusable(false);
                    etFeel.setText(trainData.feel);
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_exits) {
            if (listener != null) {
                listener.onClickExits(etFeel.getText().toString().trim());
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // out-funcs

    /**
     * 设置结果页的地图
     *
     * @param locations 定位打点
     */
    public void setResultMap(List<Location> locations) {
        this.locations = locations;
    }

    public void setTrainData(TrainData trainData) {
        this.trainData = trainData;
        updateTrainDataIfViewCreated(trainData);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

}