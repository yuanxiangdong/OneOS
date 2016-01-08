package com.eli.oneos.ui;

import android.os.Bundle;
import android.view.Window;

import com.eli.oneos.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initStatusBarStyle();
    }
}
