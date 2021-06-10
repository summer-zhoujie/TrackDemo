package com.example.trackdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FragmentManager fragmentManager;
    private Fragment currentFragment = null;
    private Fragment[] fragments = new Fragment[2];
    private static final int TAB_MAIN = 0;
    private static final int TAB_HISTORY = 1;
    private int currentIndex = -1;
    private Button btLeft;
    private Button btRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        fragments[TAB_MAIN] = new TrainMainPageFragment();
        fragments[TAB_HISTORY] = new TrainHistoryPageFragment();
        switchTo(TAB_MAIN);
        btLeft = findViewById(R.id.bt_left);
        btRight = findViewById(R.id.bt_right);
        btLeft.setOnClickListener(this);
        btRight.setOnClickListener(this);
    }

    /**
     * 显示指定索引的Fragment
     *
     * @param currentIndex 指定索引的Fragment
     */
    private void switchTo(int currentIndex) {
        if (this.currentIndex == currentIndex) {
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        if (!fragments[currentIndex].isAdded()) {
            transaction.add(R.id.content, fragments[currentIndex], "" + currentIndex);  //第三个参数为添加当前的fragment时绑定一个tag
        } else {
            transaction.show(fragments[currentIndex]);
        }

        currentFragment = fragments[currentIndex];
        this.currentIndex = currentIndex;
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_left) {
            switchTo(TAB_MAIN);
        } else if (v.getId() == R.id.bt_right) {
            switchTo(TAB_HISTORY);
        }
    }
}