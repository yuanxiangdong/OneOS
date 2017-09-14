package com.eli.oneos.ui.nav.tansfer;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.MenuPopupView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/19.
 */
public class TransferActivity extends BaseActivity  implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = TransferActivity.class.getSimpleName();

    private static final int[] TRANS_CONTROL_TITLE = new int[]{R.string.start_all, R.string.pause_all, R.string.delete_all};

    private static final int[] TRANS_CONTROL_ICON = new int[]{R.drawable.ic_title_menu_download, R.drawable.ic_title_menu_pause, R.drawable.ic_title_menu_delete};
    private static final int[] RECORD_CONTROL_TITLE = new int[]{R.string.delete_all};
    private static final int[] RECORD_CONTROL_ICON = new int[]{R.drawable.ic_title_menu_delete};

    private RadioGroup mUploadOrDownloadGroup;
    private RadioGroup mTransOrCompleteGroup;
    private RadioButton mDownloadBtn, mUploadBtn, mTransferBtn, mCompleteBtn;

    private MenuPopupView mMenuView;

    private boolean isDownload = true;
    private boolean isTransfer = true;
    private BaseTransferFragment mCurFragment;
    private FragmentManager fragmentManager;
    private List<BaseTransferFragment> mFragmentList = new ArrayList<>();
    private LinearLayout mBackLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_nav_transfer);
        initView();
        initFragment();
        initSystemBarStyle();

    }

    private void initView() {
        mUploadOrDownloadGroup = (RadioGroup) findViewById(R.id.segmented_radiogroup);
        mUploadOrDownloadGroup.setOnCheckedChangeListener(this);
        mDownloadBtn = (RadioButton) findViewById(R.id.segmented_download);
        mUploadBtn = (RadioButton) findViewById(R.id.segmented_upload);

        mTransOrCompleteGroup = (RadioGroup) findViewById(R.id.radiogroup);
        mTransOrCompleteGroup.setOnCheckedChangeListener(this);

        mTransferBtn = (RadioButton) findViewById(R.id.radio_transfer);
        mCompleteBtn = (RadioButton) findViewById(R.id.radio_complete);

        mBackLayout = (LinearLayout) findViewById(R.id.layout_title_left);
        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView mControlBtn = (TextView) findViewById(R.id.btn_control);
        mControlBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                mMenuView = new MenuPopupView(TransferActivity.this, Utils.dipToPx(130));
                if (mCurFragment instanceof TransmissionFragment) {
                    mMenuView.setMenuItems(TRANS_CONTROL_TITLE, TRANS_CONTROL_ICON);
                } else if (mCurFragment instanceof RecordsFragment) {
                    mMenuView.setMenuItems(RECORD_CONTROL_TITLE, RECORD_CONTROL_ICON);
                }
                mMenuView.setOnMenuClickListener(new MenuPopupView.OnMenuClickListener() {
                    @Override
                    public void onMenuClick(int index, View view) {
                        mCurFragment.onMenuClick(index, view);
                    }
                });
                mMenuView.showPopupDown(v, -1, true);
            }
        });



    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        TransmissionFragment mDownloadingFragment = new TransmissionFragment(true);
        mFragmentList.add(mDownloadingFragment);
        RecordsFragment mDownloadedFragment = new RecordsFragment(true);
        mFragmentList.add(mDownloadedFragment);
        TransmissionFragment mUploadingFragment = new TransmissionFragment(false);
        mFragmentList.add(mUploadingFragment);
        RecordsFragment mUploadedFragment = new RecordsFragment(false);
        mFragmentList.add(mUploadedFragment);

        onChangeFragment(isDownload, isTransfer);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mCurFragment) {
            mCurFragment.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mCurFragment) {
            mCurFragment.onResume();
        }
        if (isDownload) {
            mUploadOrDownloadGroup.check(mDownloadBtn.getId());
        } else {
            mUploadOrDownloadGroup.check(mUploadBtn.getId());
        }
        if (isTransfer) {
            mTransOrCompleteGroup.check(mTransferBtn.getId());
        } else {
            mTransOrCompleteGroup.check(mCompleteBtn.getId());
        }
    }

    public void setTransferUI(boolean isDownload, boolean isTransfer) {
        this.isDownload = isDownload;
        this.isTransfer = isTransfer;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.segmented_radiogroup) {
            if (checkedId == R.id.segmented_download) {
                isDownload = true;
            } else {
                isDownload = false;
            }
        } else {
            if (checkedId == R.id.radio_transfer) {
                isTransfer = true;
            } else {
                isTransfer = false;
            }
        }

        onChangeFragment(isDownload, isTransfer);
    }

    private void onChangeRadioBtnText(boolean isDownload) {
        if (isDownload) {
            mTransferBtn.setText(R.string.downloading_list);
            mCompleteBtn.setText(R.string.download_record);
        } else {
            mTransferBtn.setText(R.string.uploading_list);
            mCompleteBtn.setText(R.string.upload_record);
        }
    }

    /**
     * chang fragment layout
     *
     * @param isDownload Download/Upload
     * @param isTransfer Transfer/Complete
     */
    private void onChangeFragment(boolean isDownload, boolean isTransfer) {
        onChangeRadioBtnText(isDownload);

        BaseTransferFragment mFragment = mFragmentList.get(getFragmentIndex(isDownload, isTransfer));

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (mCurFragment != null) {
            mCurFragment.onPause();
            transaction.hide(mCurFragment);
        }

        if (!mFragment.isAdded()) {
            transaction.add(R.id.transfer_frame_layout, mFragment);
        } else {
            mFragment.onResume();
        }
        mCurFragment = mFragment;
        transaction.show(mCurFragment);
        transaction.commit();
    }

    private int getFragmentIndex(boolean isDownload, boolean isTransfer) {
        if (isDownload) {
            if (isTransfer) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (isTransfer) {
                return 2;
            } else {
                return 3;
            }
        }
    }


}
