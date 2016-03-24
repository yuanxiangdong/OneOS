package com.eli.oneos.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.eli.oneos.R;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.phone.LocalFile;
import com.eli.oneos.model.phone.LocalFileManage;
import com.eli.oneos.model.phone.LocalFileType;
import com.eli.oneos.receiver.NetworkStateManager;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.ui.nav.cloud.CloudNavFragment;
import com.eli.oneos.ui.nav.phone.LocalNavFragment;
import com.eli.oneos.ui.nav.tansfer.TransferNavFragment;
import com.eli.oneos.ui.nav.tools.ToolsFragment;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.ImageCheckBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_UPLOAD_INTENT = "extra_upload_intent";
    public static final String ACTION_SHOW_TRANSFER_DOWNLOAD = "action_show_transfer_download";
    public static final String ACTION_SHOW_TRANSFER_UPLOAD = "action_show_transfer_upload";
    public static final String ACTION_SHOW_LOCAL_NAV = "action_show_local_nav";

    private List<BaseNavFragment> mFragmentList = new ArrayList<>();
    private BaseNavFragment mCurNavFragment;
    private TransferNavFragment mTransferFragment;
    //    private RadioGroup radioGroup;
    private LinearLayout mNavLayout;
    private ImageCheckBox mLocalBox, mCloudBox, mTransferBox, mToolsBox;
    private FragmentManager fragmentManager;
    private int mCurPageIndex = 1;

    private NetworkStateManager.OnNetworkStateChangedListener mNetworkListener = new NetworkStateManager.OnNetworkStateChangedListener() {
        @Override
        public void onChanged(boolean isAvailable, boolean isWifiAvailable) {
            LoginManage mLoginManager = LoginManage.getInstance();
            if (mLoginManager.isLogin()) {
                boolean isLANDevice = mLoginManager.getLoginSession().isLANDevice();
                if (isLANDevice) {
                    if (!isWifiAvailable) {
                        DialogUtils.showNotifyDialog(MainActivity.this, R.string.tips, R.string.wifi_not_available, R.string.ok, null);
                    }
                } else {
                    if (!isAvailable) {
                        DialogUtils.showNotifyDialog(MainActivity.this, R.string.tips, R.string.network_not_available, R.string.ok, null);
                    }
                }
            } else {
                if (!isAvailable) {
                    ToastHelper.showToast(R.string.network_not_available);
                }
            }

            if (mCurNavFragment != null) {
                mCurNavFragment.onNetworkChanged(isAvailable, isWifiAvailable);
            }
        }
    };
    private ImageCheckBox.OnImageCheckedChangedListener listener = new ImageCheckBox.OnImageCheckedChangedListener() {

        @Override
        public void onChecked(ImageCheckBox imageView, boolean checked) {
            updateImageCheckBoxGroup(imageView);

            int index = 0;
            switch (imageView.getId()) {
                case R.id.ib_local:
                    index = 0;
                    break;
                case R.id.ib_cloud:
                    index = 1;
                    break;
                case R.id.ib_transfer:
                    index = 2;
                    break;
                case R.id.ib_tools:
                    index = 3;
                    break;
                default:
                    break;
            }
            Log.d(TAG, "onCheckedChanged: " + index);
            changFragmentByIndex(index);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initSystemBarStyle();

        initViews();

        Intent intent = getUploadIntent();
        if (LoginManage.getInstance().isLogin()) {
            initFragment();
            onNewIntent(intent);
        } else {
            Intent i = new Intent(this, LoginActivity.class);
            if (null != intent) {
                i.putExtra(EXTRA_UPLOAD_INTENT, intent);
            }
            startActivity(i);
            this.finish();
        }
    }

    private Intent getUploadIntent() {
        Intent intent = getIntent().getParcelableExtra(EXTRA_UPLOAD_INTENT);
        if (null == intent) {
            intent = getIntent();
            String action = intent.getAction();
            if (!Intent.ACTION_VIEW.equals(action) && !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                intent = null;
            }
        }

        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (null != intent) {
            super.onNewIntent(intent);
            String action = intent.getAction();
            Log.e(TAG, ">>>>>>> Action: " + action);
            // Handle upload intent
            if (!Intent.ACTION_VIEW.equals(action) && !Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                intent = null;
            }

            if (!LoginManage.getInstance().isLogin()) {
                Intent i = new Intent(this, LoginActivity.class);
                if (null != intent) {
                    i.putExtra(EXTRA_UPLOAD_INTENT, intent);
                }
                startActivity(i);
                this.finish();
            } else {
                if (null != intent) {
                    handleUploadIntent(intent);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkStateManager.getInstance().addNetworkStateChangedListener(mNetworkListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "on destroy.");
        NetworkStateManager.getInstance().removeNetworkStateChangedListener(mNetworkListener);
    }

    private long mExitTime;

    @Override
    public void onBackPressed() {
        if (mCurNavFragment != null && mCurNavFragment.onBackPressed()) {
            return;
        }

        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastHelper.showToast(R.string.press_again_to_exit);
            mExitTime = System.currentTimeMillis();
            return;
        }

        super.onBackPressed();
    }

    /**
     * Init Views
     */
    private void initViews() {
        mRootView = findViewById(R.id.layout_root);
        mNavLayout = (LinearLayout) findViewById(R.id.layout_nav);
        mLocalBox = (ImageCheckBox) findViewById(R.id.ib_local);
        mLocalBox.setOnImageCheckedChangedListener(listener);
        mCloudBox = (ImageCheckBox) findViewById(R.id.ib_cloud);
        mCloudBox.setOnImageCheckedChangedListener(listener);
        mTransferBox = (ImageCheckBox) findViewById(R.id.ib_transfer);
        mTransferBox.setOnImageCheckedChangedListener(listener);
        mToolsBox = (ImageCheckBox) findViewById(R.id.ib_tools);
        mToolsBox.setOnImageCheckedChangedListener(listener);
    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();

        LocalNavFragment localFragment = new LocalNavFragment();
        mFragmentList.add(localFragment);
        CloudNavFragment cloudFragment = new CloudNavFragment();
        mFragmentList.add(cloudFragment);
        mTransferFragment = new TransferNavFragment();
        mFragmentList.add(mTransferFragment);
        ToolsFragment toolsFragment = new ToolsFragment();
        mFragmentList.add(toolsFragment);

        changFragmentByIndex(mCurPageIndex);
    }

    private void handleUploadIntent(final Intent mIntent) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<Uri> sendUriList = new ArrayList<>();
                String action = mIntent.getAction();
                if (Intent.ACTION_VIEW.equals(action)) {
                    Uri beamUri = mIntent.getData();
                    sendUriList.add(beamUri);
                } else if (Intent.ACTION_SEND.equals(action)) {
                    Uri mSendUri = mIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                    sendUriList.add(mSendUri);
                } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                    ArrayList<Uri> mMultiUris = mIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (null != mMultiUris) {
                        sendUriList.addAll(mMultiUris);
                    }
                }

                if (!EmptyUtils.isEmpty(sendUriList)) {
                    final ArrayList<LocalFile> mUploadFiles = new ArrayList<>();
                    for (Uri uri : sendUriList) {
                        File file = uri2File(uri);
                        if (null != file) {
                            mUploadFiles.add(new LocalFile(file));
                        }
                    }

                    if (mUploadFiles.size() > 0) {
                        LocalFileManage manage = new LocalFileManage(MainActivity.this, mRootView, null);
                        manage.manage(LocalFileType.PRIVATE, FileManageAction.UPLOAD, mUploadFiles);
                    } else {
                        showTipView(R.string.failed_get_upload_file, false);
                    }
                } else {
                    showTipView(R.string.failed_get_upload_file, false);
                }
            }
        }, 500);
    }

    private void updateImageCheckBoxGroup(ImageCheckBox imageView) {
        mLocalBox.setChecked(false);
        mCloudBox.setChecked(false);
        mTransferBox.setChecked(false);
        mToolsBox.setChecked(false);
        imageView.setChecked(true);
    }

    private void changFragmentByIndex(int index) {
        Log.d(TAG, "changFragmentByIndex: " + index);
        try {
            BaseNavFragment fragment = getFragmentByIndex(index);
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (index > mCurPageIndex) {
                transaction.setCustomAnimations(R.anim.slide_nav_in_from_right, R.anim.slide_nav_out_to_left);
            } else if (index < mCurPageIndex) {
                transaction.setCustomAnimations(R.anim.slide_nav_in_from_left, R.anim.slide_nav_out_to_right);
            }

            if (mCurNavFragment != null && fragment != mCurNavFragment) {
                mCurNavFragment.onPause();
                transaction.hide(mCurNavFragment);
            }

            mCurNavFragment = fragment;
            mCurPageIndex = index;
            if (fragment.isAdded()) {
                if (!fragment.isVisible()) {
                    fragment.onResume();
                    transaction.show(fragment);
                }
            } else {
                transaction.add(R.id.content, fragment);
            }

            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            Log.e(TAG, "Switch Fragment Exception", e);
        }
    }

    public BaseNavFragment getFragmentByIndex(int index) {
        BaseNavFragment fragment = mFragmentList.get(index);
        Log.d(TAG, "Get Fragment By Index: " + index);
        return fragment;
    }

    @Override
    public boolean controlActivity(String action) {
        if (action.equals(ACTION_SHOW_TRANSFER_DOWNLOAD)) {
            mTransferFragment.setTransferUI(true, true);
//            RadioButton radioButton = (RadioButton) findViewById(R.id.radio_transfer);
//            radioButton.setChecked(true);
            listener.onChecked(mTransferBox, true);
            return true;
        } else if (action.equals(ACTION_SHOW_TRANSFER_UPLOAD)) {
            mTransferFragment.setTransferUI(false, true);
//            RadioButton radioButton = (RadioButton) findViewById(R.id.radio_transfer);
//            radioButton.setChecked(true);
            listener.onChecked(mTransferBox, true);
            return true;
        } else if (action.equals(ACTION_SHOW_LOCAL_NAV)) {
//            RadioButton radioButton = (RadioButton) findViewById(R.id.radio_local);
//            radioButton.setChecked(true);
            listener.onChecked(mLocalBox, true);
            return true;
        }

        return false;
    }

    public void showNavBar() {
        mNavLayout.setVisibility(View.VISIBLE);
    }

    public void hideNavBar(boolean isGone) {
        mNavLayout.setVisibility(isGone ? View.GONE : View.INVISIBLE);
    }

    private File uri2File(Uri uri) {
        if (null == uri) {
            return null;
        }

        File file = null;

        // get from URI path
        String path = uri.getPath();
        if (!EmptyUtils.isEmpty(path)) {
            file = new File(path);
            if (file.exists()) {
                return file;
            }
        }

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        path = cursor.getString(columnIndex);
        cursor.close();
        if (null != path) {
            file = new File(path);
        }

        // get from MediaStore
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
//        int columnIndex = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        actualimagecursor.moveToFirst();
//        path = actualimagecursor.getString(columnIndex);
//        file = new File(path);
//        Log.d(TAG, "=====>>>Uri Path = " + path);

        return file;
    }
}
