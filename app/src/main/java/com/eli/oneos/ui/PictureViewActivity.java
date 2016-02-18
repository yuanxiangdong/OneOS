package com.eli.oneos.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.HttpBitmap;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.preview.BasePagerAdapter;
import com.eli.oneos.widget.preview.GalleryPagerAdapter;
import com.eli.oneos.widget.preview.GalleryViewPager;

import java.io.File;
import java.util.ArrayList;

public class PictureViewActivity extends Activity {
    private static final String TAG = PictureViewActivity.class.getSimpleName();

    private ArrayList<OneOSFile> mPicList = new ArrayList<>();
    private ArrayList<File> mLocalPicList = new ArrayList<>();

    protected LoginSession mLoginSession = null;
    private MyApplication mApplication = null;
    private TextView mCurTxt, mTotalTxt;
    private HttpBitmap httpBitmap;
    private RelativeLayout mTitleLayout;

    private int startIndex = 0;
    private boolean isLocalPicture = false;
    private OnClickListener onBackListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_viewer_picture);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            startIndex = bundle.getInt("StartIndex", 0);
            isLocalPicture = bundle.getBoolean("IsLocalPicture");
            startIndex = startIndex >= 0 ? startIndex : 0;
            if (isLocalPicture) {
                mLocalPicList = (ArrayList<File>) intent.getSerializableExtra("PictureList");
            } else {
                mPicList = (ArrayList<OneOSFile>) intent.getSerializableExtra("PictureList");
            }
        }
        Log.d(TAG, "---Start Index: " + startIndex);

        mLoginSession = LoginManage.getInstance().getLoginSession();

        httpBitmap = HttpBitmap.getInstance();

        // initFinalBitmap();
        initView();
    }

    private void initView() {
        mTitleLayout = (RelativeLayout) findViewById(R.id.layout_title);
        mCurTxt = (TextView) findViewById(R.id.text_index);
        mTotalTxt = (TextView) findViewById(R.id.text_total);
        mTitleLayout.setVisibility(View.VISIBLE);
        ImageButton ivBtn = (ImageButton) findViewById(R.id.btn_back);
        ivBtn.setOnClickListener(onBackListener);
        TextView mBackTxt = (TextView) findViewById(R.id.txt_title_back);
        mBackTxt.setOnClickListener(onBackListener);

        if (isLocalPicture ? mLocalPicList.size() > 0 : mPicList.size() > 0) {

            GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(this, isLocalPicture ? mLocalPicList : mPicList, isLocalPicture, mLoginSession, httpBitmap);
            pagerAdapter.setOnItemChangeListener(new BasePagerAdapter.OnItemChangeListener() {
                @Override
                public void onItemChange(int currentPosition) {
                    setIndicatorTxt(currentPosition + 1, isLocalPicture ? mLocalPicList.size() : mPicList.size());
                }
            });
            GalleryViewPager mViewPager = (GalleryViewPager) this.findViewById(R.id.switch_viewer);
            mViewPager.setOffscreenPageLimit(1);
            mViewPager.setAdapter(pagerAdapter);
            mViewPager.setPageMargin(100);
            mViewPager.setCurrentItem(startIndex);
            // mViewPager.setOnItemClickListener(new OnItemClickListener() {
            //
            // @Override
            // public void onItemClicked(View view, int position) {
            // mTitleLayout.setVisibility(mTitleLayout.isShown() ? View.GONE : View.VISIBLE);
            // }
            // });
        } else {
            ToastHelper.showToast(R.string.app_exception);
            finish();
        }
    }

    private void setIndicatorTxt(int curIndex, int total) {
        if (curIndex <= total) {
            mCurTxt.setText(String.valueOf(curIndex));
            mTotalTxt.setText(String.valueOf(total));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestory");
    }

}
