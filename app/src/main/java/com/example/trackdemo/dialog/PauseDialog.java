package com.example.trackdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.trackdemo.R;

public class PauseDialog extends Dialog implements View.OnClickListener {
    private Button btContinue;
    private Button btFinish;
    private Listener listener;

    public interface Listener {
        void onClickContinue();

        void onClickFinsh();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public PauseDialog(@NonNull Context context) {
        this(context, 0);
    }

    public PauseDialog(@NonNull Context context, int themeResId) {
        super(context, R.style.traindialog_style);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);

        final View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pausedialog_layout, null);
        setContentView(inflate);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        btContinue = findViewById(R.id.bt_continue);
        btFinish = findViewById(R.id.bt_finish);
        btContinue.setOnClickListener(this);
        btFinish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_continue) {
            if (listener != null) {
                listener.onClickContinue();
            }
        } else if (v.getId() == R.id.bt_finish) {
            if (listener != null) {
                listener.onClickFinsh();
            }
        }
    }
}
