package com.eli.oneos.ui.nav.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eli.oneos.MyApplication;
import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.db.UserSettingsKeeper;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.service.OneSpaceService;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.AnimCircleProgressBar;
import com.eli.oneos.widget.SwitchButton;
import com.eli.oneos.widget.TitleBackLayout;

public class BackupPhotoActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = BackupPhotoActivity.class.getSimpleName();
    private static final int MSG_REFRESH_UI = 1;
    private static final int MSG_REFRESH_PROGRESS = 2;

    private SwitchButton mSwitchButton, mCtrlSwitchButton;
    private LinearLayout mProgressLayout, mCompleteLayout;
    private TextView mProgressTxt, mServerDirTxt, mCompleteTipTxt;
    private AnimCircleProgressBar mProgressBar;
    private TitleBackLayout mTitleLayout;

    private LoginSession mLoginSession;
    private boolean isAutoBackup = false, isWifiBackup = true;
    private boolean isFragmentVisible = true;
    private Thread mThread = null;

    private OneSpaceService mBackupService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tool_backup_photo);
        initSystemBarStyle();

        mLoginSession = LoginManage.getInstance().getLoginSession();
        mBackupService = MyApplication.getTransferService();

        initViews();

        isFragmentVisible = true;
        startUpdateUIThread();
    }

    /**
     * init view by id
     */
    private void initViews() {
        mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_backup_photo);
        mTitleLayout.setRightButton(R.drawable.selector_button_title_reset);
        mTitleLayout.setOnRightClickListener(this);

        mProgressLayout = (LinearLayout) findViewById(R.id.layout_progress);
        mCompleteLayout = (LinearLayout) findViewById(R.id.layout_complete);

        mProgressBar = (AnimCircleProgressBar) findViewById(R.id.progressbar);
        mProgressTxt = (TextView) findViewById(R.id.txt_progress);
        mCompleteTipTxt = (TextView) findViewById(R.id.txt_complete_tips);
        mServerDirTxt = (TextView) findViewById(R.id.txt_server_dir);

        mSwitchButton = (SwitchButton) findViewById(R.id.btn_auto_backup);
        if (getLoginStatus()) {
            String dir = getResources().getString(R.string.backup_dir_shown) + Constants.BACKUP_FILE_ONEOS_ROOT_DIR_NAME_ALBUM;
            mServerDirTxt.setText(dir);
            mSwitchButton.setEnabled(true);
            isAutoBackup = LoginManage.getInstance().getLoginSession().getUserSettings().getIsAutoBackupFile();
        } else {
            mServerDirTxt.setText(R.string.please_login_onespace);
            mSwitchButton.setEnabled(false);
        }

        mSwitchButton.setChecked(isAutoBackup);
        mSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (getLoginStatus()) {
                    if (isAutoBackup != isChecked) {
                        Log.e(TAG, "-----On Checked Changed-----");
                        isAutoBackup = isChecked;
                        mLoginSession.getUserSettings().setIsAutoBackupFile(isAutoBackup);
                        UserSettingsKeeper.update(mLoginSession.getUserSettings());

                        if (isChecked) {
                            Log.d(TAG, "-----Start Backup-----");
                            mBackupService.startBackupFile();
                        } else {
                            Log.d(TAG, "-----Stop Backup-----");
                            mBackupService.stopBackupFile();
                        }
                    }
                }
                // else {
                // mSwitchButton.setChecked(isAutoBackup);
                // }
            }
        });

        isWifiBackup = mLoginSession.getUserSettings().getIsBackupFileOnlyWifi();
        mCtrlSwitchButton = (SwitchButton) findViewById(R.id.btn_wifi_backup);
        mCtrlSwitchButton.setChecked(isWifiBackup);
        mCtrlSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isWifiBackup = isChecked;
                mLoginSession.getUserSettings().setIsBackupFileOnlyWifi(isWifiBackup);
                UserSettingsKeeper.update(mLoginSession.getUserSettings());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_title_back:
            case R.id.btn_back:
                BackupPhotoActivity.this.finish();
                break;
            case R.id.ibtn_title_right:
                resetBackPhotoDialog();
                break;
            default:
                break;
        }
    }

    private void resetBackPhotoDialog() {
        DialogUtils.showConfirmDialog(this, R.string.title_reset_backup, R.string.tips_reset_backup, R.string.reset_now,
                R.string.cancel, new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            mBackupService.resetBackupFile();
                            ToastHelper.showToast(R.string.success_reset_backup);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentVisible = false;
    }

    private boolean getLoginStatus() {
        if (LoginManage.getInstance().isLogin()) {
            return true;
        } else {
            ToastHelper.showToast(R.string.please_login_onespace);
            return false;
        }
    }

    private void refreshBackupView(int count) {
        mProgressTxt.setText(String.valueOf(count));
        if (count > 0) {
            // mBgView.setVisibility(View.GONE);
            mCompleteLayout.setVisibility(View.GONE);
            // mProgressBar.setVisibility(View.VISIBLE);
            mProgressLayout.setVisibility(View.VISIBLE);
            mTitleLayout.setRightButtonVisible(View.GONE);
        } else {
            // mProgressBar.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.GONE);
            if (mSwitchButton.isChecked()) {
                mCompleteTipTxt.setText(R.string.backup_complete);
                mTitleLayout.setRightButtonVisible(View.VISIBLE);
            } else {
                mCompleteTipTxt.setText(R.string.backup_closed);
                mTitleLayout.setRightButtonVisible(View.GONE);
            }
            mCompleteLayout.setVisibility(View.VISIBLE);
            // mBgView.setVisibility(View.VISIBLE);
        }
    }

    private void startUpdateUIThread() {
        if (mThread == null || !mThread.isAlive()) {
            mThread = new Thread(new UIThread());
            mThread.start();
        }
    }

    private static final int REFRESH_FREQUENCY = 40; // 刷新频率，单位ms
    private static final int TIMES_PRE_SECONDS = 1000 / REFRESH_FREQUENCY; // 每秒刷新次数
    private static final int PROGRESS_PRE_TIMES = 100 / TIMES_PRE_SECONDS; // 刷新进度变化值基数
    private boolean isProgressUp = true;
    private boolean isBackup = false;

    public class UIThread implements Runnable {
        @Override
        public void run() {
            int times = 0;

            while (isFragmentVisible) {
                Message message;
                try {
                    if (times == 0) {
                        message = new Message();
                        message.what = MSG_REFRESH_UI;
                        handler.sendMessage(message);
                    }

                    message = new Message();
                    message.what = MSG_REFRESH_PROGRESS;
                    message.arg1 = times;
                    handler.sendMessage(message);

                    Thread.sleep(REFRESH_FREQUENCY); // sleep 800ms
                    if (times == 0) {
                        isProgressUp = true;
                    } else if (times == TIMES_PRE_SECONDS) {
                        isProgressUp = false;
                    }

                    if (isProgressUp) {
                        times++;
                    } else {
                        times--;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_UI:
                    int count = mBackupService.getBackupFileCount();
                    if (count > 0) {
                        isBackup = true;
                    } else {
                        isBackup = false;
                    }
                    refreshBackupView(count);
                    break;
                case MSG_REFRESH_PROGRESS:
                    if (isBackup) {
                        int p = msg.arg1;
                        mProgressBar.setMainProgress(p * PROGRESS_PRE_TIMES);
                    } else {
                        mProgressBar.setMainProgress(0);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
}