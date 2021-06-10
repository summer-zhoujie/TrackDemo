package com.example.trackdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.trackdemo.R;

public class LockDialog extends Dialog implements View.OnClickListener {
    private Listener listener;

    public interface Listener{
        void onClickUnlock();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public LockDialog(@NonNull Context context) {
        this(context, 0);
    }

    public LockDialog(@NonNull Context context, int themeResId) {
        super(context, R.style.traindialog_style);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);

        final View inflate = LayoutInflater.from(getContext()).inflate(R.layout.lockdialog_layout, null);
        setContentView(inflate);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        findViewById(R.id.bt_unlock).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_unlock) {
            if (listener != null) {
                listener.onClickUnlock();
            }
        }
    }
}
