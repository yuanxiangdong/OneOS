package com.eli.oneos.ui.nav.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.widget.PowerPopupView;
import com.eli.oneos.widget.StickListView;

import java.util.ArrayList;
import java.util.List;

public class ToolsFragment extends BaseNavFragment implements OnItemClickListener {

    private static final String TAG = "ToolsActvity";

    private static final int TOOL_SETTING = R.string.tool_setting;
    private static final int TOOL_BACKUP_PHOTO = R.string.tool_backup_photo;
    private static final int TOOL_SYNC_CONTACT = R.string.tool_sync_contact;
    private static final int TOOL_SYNC_SMS = R.string.tool_sync_sms;
    private static final int TOOL_APP = R.string.tool_app;
    private static final int TOOL_OFFLINE = R.string.tool_offline;
    private static final int TOOL_POWER = R.string.tool_power;
    private static final int TOOL_CHANGE_USER = R.string.tool_changer_user;
    private static final int[] TOOL_TITLE_M3X = new int[]{TOOL_SETTING, TOOL_BACKUP_PHOTO, TOOL_SYNC_CONTACT, TOOL_SYNC_SMS, TOOL_OFFLINE, TOOL_APP,
            TOOL_POWER, TOOL_CHANGE_USER};
    private static final int[] TOOL_ICON_M3X = new int[]{R.drawable.icon_tools_setting, R.drawable.icon_tools_backup_photo, R.drawable.icon_tools_contact,
            R.drawable.icon_tools_sms, R.drawable.icon_tools_offline, R.drawable.icon_tools_app, R.drawable.icon_tools_power,
            R.drawable.icon_tools_change_user};

    private StickListView mListView;
    private ToolAdapter mAdapter;
    private PowerPopupView mPopupView;
    private ArrayList<ToolBar> mToolList = new ArrayList<ToolBar>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tools, container, false);
        Log.d(TAG, "On Create");

        initViews(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initDate();
    }

    private void initDate() {
        mToolList.clear();
        int[] title = TOOL_TITLE_M3X;
        int[] icon = TOOL_ICON_M3X;

        for (int i = 0; i < title.length; ++i) {
            ToolBar toolBar = new ToolBar();
            toolBar.toolTitle = title[i];
            toolBar.toolIcon = icon[i];
            mToolList.add(toolBar);
        }
        mAdapter.setToolList(mToolList);
        mAdapter.notifyDataSetChanged();
    }

    private void initViews(View view) {
        mListView = (StickListView) view.findViewById(R.id.listview_tools);
        mAdapter = new ToolAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mAdapter.setToolList(mToolList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        int tool = mToolList.get(arg2).toolTitle;
        Intent intent = null;
        Log.d(TAG, "On item click: tool = " + tool);
//        if (tool == TOOL_APP) {
//            intent = new Intent(getActivity(), AppActivity.class);
//        } else if (tool == TOOL_SETTING) {
//            intent = new Intent(getActivity(), SettingActivity.class);
//        } else if (tool == TOOL_SYNC_CONTACT) {
//            intent = new Intent(getActivity(), SyncContactActivity.class);
//        } else if (tool == TOOL_SYNC_SMS) {
//            intent = new Intent(getActivity(), SyncSmsActivity.class);
//        } else if (tool == TOOL_WIFI_CONFIG) {
//            intent = new Intent(getActivity(), WifiConfigActivity.class);
//        } else if (tool == TOOL_OFFLINE) {
//            if (isLogin()) {
//                intent = new Intent(getActivity(), AriaActivity.class);
//            } else {
//                ToastHelper.showToast(R.string.please_login_onespace);
//            }
//        } else if (tool == TOOL_CHANGE_USER) {
//            if (isLogin()) {
//                loginOutDialog();
//            } else {
//                doLoginOut();
//            }
//        } else if (tool == TOOL_BACKUP_PHOTO) {
//            intent = new Intent(getActivity(), BackupPhotoActivity.class);
//        } else if (tool == TOOL_POWER) {
//            int admin = UserInfoKeeper.readUserInfo(getActivity(), UserInfoKeeper.KEY_ADMIN_FLAG, UserInfoKeeper.DEFAULT_ADMIN_FLAG);
//            if (isLogin() && admin == 1) {
//                showPowerView(arg1);
//            } else {
//                DialogUtils.showNotifyDialog(getActivity(), R.string.dialog_tips, R.string.please_login_onespace_with_admin, R.string.ok, null);
//            }
//        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    private void showPowerView(View view) {
        if (mPopupView == null) {
            initPowerMenu(view);
        }

        mPopupView.showPopupCenter(view);
    }

    private void initPowerMenu(View view) {
        mPopupView = new PowerPopupView(getActivity(), null, new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPopupView.dismiss();

                if (v.getId() == R.id.layout_power_off) {
                    showPowerDialog(true);
                } else {
                    showPowerDialog(false);
                }
            }
        });
    }

    private void showPowerDialog(final boolean isPowerOff) {
        int contentRes = isPowerOff ? R.string.confirm_power_off_device : R.string.confirm_reboot_device;
        DialogUtils.showConfirmDialog(getActivity(), R.string.tip, contentRes, R.string.confirm, R.string.cancel, new DialogUtils.OnDialogClickListener() {
            @Override
            public void onClick(boolean isPositiveBtn) {
                if (isPositiveBtn) {
                    doPowerOffOrRebootDevice(isPowerOff);
                }
            }
        });
    }

    private void doPowerOffOrRebootDevice(final boolean isPowerOff) {
//        UserInfo mUserInfo = UserInfoKeeper.readUserInfo(getActivity());
//        String url = mUserInfo.getBaseUrl();
//        if (isPowerOff) {
//            url += ServerOptConstants.END_URL_HALT_DEVICE;
//        } else {
//            url += ServerOptConstants.END_URL_REBOOT_DEVICE;
//        }
//
//        HttpUtils httpUtils = new HttpUtils(new OnHttpResultListener() {
//
//            @Override
//            public void onStart() {
//            }
//
//            @Override
//            public void onSuccess(String url, String result) {
//                int timeout = 0;
//                int resId = 0;
//                if (isPowerOff) {
//                    timeout = 5;
//                    resId = R.string.shutting_down_device;
//                } else {
//                    timeout = 60;
//                    resId = R.string.rebooting_device;
//                }
//                IProgressDialog mDialog = new IProgressDialog(getActivity(), resId, timeout);
//                mDialog.setOnDismissListener(new OnDismissListener() {
//
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        // if (isPowerOff) {
//                        // ToastHelper.showToast(R.string.success_power_off_device);
//                        // } else {
//                        // ToastHelper.showToast(R.string.success_reboot_device);
//                        // }
//                        doLoginOut();
//                    }
//                });
//                mDialog.show();
//            }
//
//            @Override
//            public void onFailure(String url, int errorNo, String errorMsg) {
//                if (isPowerOff) {
//                    ToastHelper.showToast(R.string.failed_power_off_device);
//                } else {
//                    ToastHelper.showToast(R.string.failed_reboot_device);
//                }
//            }
//        }, 10);
//        httpUtils.get(url);
    }

    private boolean isLogin() {
        return LoginManage.getInstance().isLogin();
    }

    /**
     * login out
     */
    private void doLoginOut() {
//        TransferService mTransferService = MyApplication.getTransferService();
//        if (mTransferService == null) {
//            ToastHelper.showToast(R.string.app_exception);
//            return;
//        }
//        mTransferService.cancelDownload();
//        mTransferService.cancelUpload();
//
//        BackupService mBackupService = MyApplication.getBackupService();
//        mBackupService.notifyUserLogout();
//
//        MyApplication mApplication = (MyApplication) getActivity().getApplication();
//        mApplication.setLoginOut(true);
//
//        // 删除用户密码
//        // UserInfoKeeper.writeUserInfo(getActivity(),
//        // UserInfoKeeper.KEY_USER_PWD, "");
//        UserInfoKeeper.writeUserInfo(getActivity(), UserInfoKeeper.KEY_SESSION, "");
//        UserInfoKeeper.writeUserInfo(getActivity(), UserInfoKeeper.KEY_IS_LOGOUT, true);
//        UsageInfoKeeper.writeUsageInfo(getActivity(), UsageInfoKeeper.KEY_USE_BACKUP_PHOTO, UsageInfoKeeper.DEFAULT_USE_BACKUP_PHOTO);
//
//        Intent intent = new Intent(getActivity(), LoginActivity.class);
//        startActivity(intent);
//        getActivity().finish();
    }

    /**
     * dialog of confirm login out
     */
    private void loginOutDialog() {
        DialogUtils.showConfirmDialog(getActivity(), R.string.confirm_change_user, R.string.warning_change_user, R.string.confirm,
                R.string.cancel, new DialogUtils.OnDialogClickListener() {
                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doLoginOut();
                        }
                    }
                });
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    @Override
    public void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable) {
    }

    private class ToolBar {
        public int toolTitle = 0;
        public int toolIcon = 0;
    }

    public class ToolAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<ToolBar> mToolList = new ArrayList<ToolBar>();

        public ToolAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        public void setToolList(List<ToolBar> appList) {
            mToolList.clear();
            mToolList.addAll(appList);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mToolList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        class ViewHolder {
            ImageView toolIcon;
            TextView toolTitle;
            // BadgeView badgeView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_tools, null);
                holder = new ViewHolder();
                holder.toolIcon = (ImageView) convertView.findViewById(R.id.tool_icon);
                holder.toolTitle = (TextView) convertView.findViewById(R.id.tool_title);
                // holder.badgeView = (BadgeView)
                // convertView.findViewById(R.id.txt_badge);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ToolBar tool = mToolList.get(position);
            holder.toolIcon.setImageResource(tool.toolIcon);
            holder.toolTitle.setText(tool.toolTitle);

            // if (position == 0) {
            // //
            // holder.badgeView.setTypeface(Typeface.create(Typeface.SANS_SERIF,
            // // Typeface.ITALIC));
            // holder.badgeView.setBadgeCount(1);
            // holder.badgeView.setHideOnNull(false);
            // } else {
            // holder.badgeView.setHideOnNull(true);
            // }

            return convertView;
        }
    }
}
