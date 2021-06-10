package com.example.trackdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.trackdemo.bean.TrainData;
import com.example.trackdemo.constant.TargetTypes;
import com.example.trackdemo.fragments.TrainResultFragment;

public class TrainResultActivity extends AppCompatActivity {

    private static final String TRAINDATA_KEY = "TRAINDATA_KEY";
    private TrainResultFragment trainResultFragment;
    private TrainData data;

    public static void launch(Context context, TrainData trainData) {
        final Intent intent = new Intent(context, TrainResultActivity.class);
        intent.putExtra(TRAINDATA_KEY, trainData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_result);
        data = getIntent().getParcelableExtra(TRAINDATA_KEY);
        if (data != null) {
            solveData(data);
            trainResultFragment = (TrainResultFragment) getSupportFragmentManager().findFragmentById(R.id.train_result_fragment);
            trainResultFragment.setResultMap(data.locations);
            trainResultFragment.setTrainData(data);
            trainResultFragment.setListener(new TrainResultFragment.Listener() {
                @Override
                public void onClickExits(String feel) {
                    finish();
                }

                @Override
                public void onMapShown() {

                }
            });
        }
    }

    private void solveData(TrainData data) {
        String time_unit = getString(R.string.unit_time);
        String time_value = data.getTimDesc();

        String distance_unit = getString(R.string.km);
        String distance_value = String.format("%.2f", data.distance);

        String speed_unit = getString(R.string.unit_speed);
        // 速度: 分/千米
        String speed_value = data.getSpeedSecondDesc();

        String kcal_unit = getString(R.string.kcal);
        String kcal_value = String.format("%.2f", data.kcal);

        data.top = distance_value;
        data.top_unit = distance_unit;
        data.left = time_value;
        data.left_unit = time_unit;
        data.right = kcal_value;
        data.right_unit = kcal_unit;

        data.process = data.distance;

        data.mid = speed_value;
        data.mid_unit = speed_unit;
    }
}