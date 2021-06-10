package com.example.trackdemo;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trackdemo.constant.TargetTypes;

/**
 * 计步主页
 */
public class TrainMainPageFragment extends Fragment {

    private static final float CUR_TARGET_ERR_ET_EMPTY = -2;
    private Spinner spinnerTargettypes;
    private LinearLayout llLeft;
    private EditText etLeft;
    private TextView tvUnitLeft;
    private TextView tvTimeSplit;
    private LinearLayout llRight;
    private EditText etRight;
    private TextView tvUnitRight;
    private Spinner spinnerTraintypes;
    private Button btStart;
    private TextView tvNoTargetHint;
    private int curTrainType;
    private int curTargetType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train_main_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        spinnerTargettypes = view.findViewById(R.id.spinner_targettypes);
        llLeft = view.findViewById(R.id.ll_left);
        etLeft = view.findViewById(R.id.et_left);
        tvUnitLeft = view.findViewById(R.id.tv_unit_left);
        tvTimeSplit = view.findViewById(R.id.tv_time_split);
        llRight = view.findViewById(R.id.ll_right);
        etRight = view.findViewById(R.id.et_right);
        tvUnitRight = view.findViewById(R.id.tv_unit_right);
        spinnerTraintypes = view.findViewById(R.id.spinner_traintypes);
        btStart = view.findViewById(R.id.bt_start);
        tvNoTargetHint = view.findViewById(R.id.tv_no_target_hint);

        spinnerTargettypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosedTargetType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerTraintypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosedTrainType(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btStart.setOnClickListener(v -> {
            doStart();
        });
    }

    /**
     * 开始运动
     */
    private void doStart() {
        float target = culTarget();
        if (target == CUR_TARGET_ERR_ET_EMPTY) {
            Toast.makeText(getActivity(), getString(R.string.target_empty_hint), Toast.LENGTH_SHORT).show();
            return;
        }
        TrainActivity.launch(getActivity(), 60f, curTargetType, target, curTrainType);
    }

    /**
     * 计算当前目标值
     *
     * @return 返回 {@link #CUR_TARGET_ERR_ET_EMPTY}代表{@link EditText }为空
     */
    private float culTarget() {
        try {
            if (curTargetType == TargetTypes.DISTANCE) {
                final String etString = etLeft.getText().toString().trim();
                return Float.parseFloat(etString);
            } else if (curTargetType == TargetTypes.KCAL) {
                final String etString = etLeft.getText().toString().trim();
                return Integer.parseInt(etString);
            } else if (curTargetType == TargetTypes.TIME) {
                final String etLString = etLeft.getText().toString().trim();
                int hour = Integer.parseInt(etLString);
                final String etRString = etRight.getText().toString().trim();
                int min = Integer.parseInt(etRString);

                int seconds = hour * 60 * 60 + min * 60;
                return seconds;
            } else {
                return TrainActivity.TARGET_DEFAULT;
            }
        } catch (NullPointerException e) {
            return CUR_TARGET_ERR_ET_EMPTY;
        }

    }

    private void choosedTrainType(int trainType) {
        curTrainType = trainType;
    }

    private void choosedTargetType(int targetType) {
        if (targetType == TargetTypes.DISTANCE) {
            curTargetType = TargetTypes.DISTANCE;
            llLeft.setVisibility(View.VISIBLE);
            llRight.setVisibility(View.GONE);

            etLeft.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            tvUnitLeft.setText(R.string.km);

            tvTimeSplit.setVisibility(View.GONE);

            tvNoTargetHint.setVisibility(View.GONE);

        } else if (targetType == TargetTypes.TIME) {
            curTargetType = TargetTypes.TIME;
            llLeft.setVisibility(View.VISIBLE);
            llRight.setVisibility(View.VISIBLE);

            etLeft.setInputType(InputType.TYPE_CLASS_NUMBER);
            tvUnitRight.setText(R.string.hour);

            tvTimeSplit.setVisibility(View.VISIBLE);

            etRight.setInputType(InputType.TYPE_CLASS_NUMBER);
            tvUnitRight.setText(R.string.min);

            tvNoTargetHint.setVisibility(View.GONE);
        } else if (targetType == TargetTypes.KCAL) {
            curTargetType = TargetTypes.KCAL;
            llLeft.setVisibility(View.VISIBLE);
            llRight.setVisibility(View.GONE);

            etLeft.setInputType(InputType.TYPE_CLASS_NUMBER);
            tvUnitLeft.setText(R.string.kcal_unit);

            tvTimeSplit.setVisibility(View.GONE);

            tvNoTargetHint.setVisibility(View.GONE);
        } else {
            curTargetType = TargetTypes.UNKNOWN;
            llLeft.setVisibility(View.GONE);
            llRight.setVisibility(View.GONE);

            tvTimeSplit.setVisibility(View.GONE);

            tvNoTargetHint.setVisibility(View.VISIBLE);
        }
    }
}