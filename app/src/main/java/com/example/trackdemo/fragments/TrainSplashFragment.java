package com.example.trackdemo.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.example.trackdemo.R;

/**
 * 进入锻炼界面的321倒计时
 */
public class TrainSplashFragment extends Fragment {

    private final static int COUNT = 3;
    private TextView tv;
    private int curCount = COUNT;
    private Listener listener = null;

    public interface Listener {
        /**
         * 动画播放完毕
         */
        void onAnimEnd();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_train_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv = view.findViewById(R.id.tv);
        startAnim();
    }

    /**
     * 开启倒计时动画
     */
    private void startAnim() {
        if (tv != null) {
            tv.setText("" + curCount--);
            final float baseX = tv.getX();
            final float endX = dpToPx(300);
            final AnimatorSet animatorSet = new AnimatorSet();
            final ObjectAnimator translationY = ObjectAnimator.ofFloat(tv, "translationY", baseX, endX);
            final ObjectAnimator alpha = ObjectAnimator.ofFloat(tv, "alpha", 0f, 1f, 0f);
            animatorSet.playTogether(translationY, alpha);
            animatorSet.setInterpolator(new LinearInterpolator());
            animatorSet.setDuration(1000);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (curCount <= 0) {
                        // 动画结束
                        if (listener != null) {
                            listener.onAnimEnd();
                        }
                    } else {
                        tv.setText("" + curCount--);
                        animatorSet.start();
                    }

                }
            });
            animatorSet.start();
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}