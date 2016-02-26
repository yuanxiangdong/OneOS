package com.eli.oneos.ui.nav.tools.app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.eli.oneos.R;
import com.eli.oneos.model.oneos.PluginInfo;
import com.eli.oneos.model.oneos.adapter.PluginAdapter;
import com.eli.oneos.model.oneos.api.OneOSListPluginAPI;
import com.eli.oneos.model.oneos.api.OneOSPluginManageAPI;
import com.eli.oneos.model.oneos.user.LoginManage;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.ui.BaseActivity;
import com.eli.oneos.ui.nav.tools.aria.AriaActivity;
import com.eli.oneos.utils.DialogUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.SwipeListView;
import com.eli.oneos.widget.SwitchButton;

import java.util.ArrayList;
import java.util.List;

public class PluginFragment extends Fragment {
    private static final String TAG = PluginFragment.class.getSimpleName();

    private BaseActivity activity;
    private SwipeListView mListView;
    private List<PluginInfo> mPlugList = new ArrayList<>();
    private PluginAdapter mAdapter;
    private LoginSession loginSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tool_app, container, false);

        loginSession = LoginManage.getInstance().getLoginSession();

        initViews(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mListView.hiddenRight();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPluginsFromServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViews(View view) {
        View mEmptyView = view.findViewById(R.id.layout_empty);
        mListView = (SwipeListView) view.findViewById(R.id.list_app);
        mListView.setEmptyView(mEmptyView);
        mListView.setRightViewWidth(Utils.dipToPx(70));
        mAdapter = new PluginAdapter(activity, mListView.getRightViewWidth(), mPlugList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = null;
                PluginInfo info = mPlugList.get(arg2);
                if (info.getPack().equalsIgnoreCase("aria2")) {
                    intent = new Intent(getActivity(), AriaActivity.class);
                }

                if (null != intent) {
                    startActivity(intent);
                }
            }
        });
        mAdapter.setOnClickListener(new PluginAdapter.OnPluginClickListener() {

            @Override
            public void onClick(View view, PluginInfo info) {
                switch (view.getId()) {
                    case R.id.app_uninstall:
                        if (!LoginManage.getInstance().getLoginSession().isAdmin()) {
                            ToastHelper.showToast(R.string.please_login_onespace_with_admin);
                        } else {
                            showOperatePluginDialog(info, true);
                        }
                        break;
                    case R.id.btn_state:
                        SwitchButton mBtn = (SwitchButton) view;
                        // 屏蔽非主动点击事件
                        if (info.isOpened() != mBtn.isChecked()) {
                            if (!LoginManage.getInstance().getLoginSession().isAdmin()) {
                                ToastHelper.showToast(R.string.please_login_onespace_with_admin);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                showOperatePluginDialog(info, false);
                            }
                        }
                        break;
                }
            }
        });
    }

    private void showOperatePluginDialog(final PluginInfo info, final boolean isUninstall) {
        String title = null;
        if (isUninstall) {
            title = getResources().getString(R.string.confirm_uninstall_plugin);
        } else {
            if (info.isOpened()) {
                title = getResources().getString(R.string.confirm_close_plugin);
            } else {
                title = getResources().getString(R.string.confirm_open_plugin);
            }
        }
        title += " " + info.getName() + " ?";

        Resources resources = getResources();
        DialogUtils.showConfirmDialog(getActivity(), resources.getString(R.string.tip), title,
                resources.getString(R.string.confirm), resources.getString(R.string.cancel),
                new DialogUtils.OnDialogClickListener() {

                    @Override
                    public void onClick(boolean isPositiveBtn) {
                        if (isPositiveBtn) {
                            doOperatePluginToServer(info, isUninstall);
                        } else {
                            mListView.hiddenRight();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void getPluginsFromServer() {
        if (!LoginManage.getInstance().isLogin()) {
            Log.e(TAG, "Do not Login OneSpace");
            return;
        }

        OneOSListPluginAPI listAppAPI = new OneOSListPluginAPI(loginSession);
        listAppAPI.setOnListPluginListener(new OneOSListPluginAPI.OnListPluginListener() {
            @Override
            public void onStart(String url) {
                activity.showLoading(R.string.getting_app_list);
            }

            @Override
            public void onSuccess(String url, ArrayList<PluginInfo> plugins) {
                mPlugList.clear();
                if (null != plugins) {
                    mPlugList.addAll(plugins);
                }
                mListView.hiddenRight();
                mAdapter.notifyDataSetChanged();
                activity.dismissLoading();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                activity.dismissLoading();
            }
        });
        listAppAPI.list();
    }

    private void doOperatePluginToServer(PluginInfo info, boolean isUninstall) {
        OneOSPluginManageAPI manageAPI = new OneOSPluginManageAPI(loginSession);
        manageAPI.setOnManagePluginListener(new OneOSPluginManageAPI.OnManagePluginListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, String pack, String cmd, boolean ret) {
                refreshListDelayed();
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                refreshListDelayed();
            }
        });
        if (isUninstall) {
            manageAPI.delete(info.getPack());
        } else if (info.isOpened()) {
            manageAPI.off(info.getPack());
        } else {
            manageAPI.on(info.getPack());
        }
    }

    private void refreshListDelayed() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                getPluginsFromServer();
            }
        }, 2000);
    }
}
