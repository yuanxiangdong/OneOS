package com.eli.oneos.ui;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.eli.oneos.R;
import com.eli.oneos.db.UserInfoKeeper;
import com.eli.oneos.db.greendao.DeviceInfo;
import com.eli.oneos.db.greendao.UserInfo;
import com.eli.oneos.model.api.OneOSLoginAPI;
import com.eli.oneos.model.user.LoginInfo;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.utils.Utils;
import com.eli.oneos.widget.ClearEditText;
import com.eli.oneos.widget.TitleBackLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for User Login OneSpace
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/8.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private ClearEditText mUserTxt, mPwdTxt, mPortTxt;
    private EditText mIPTxt;
    private Button mLoginBtn;

    private List<UserInfo> mHistoryUserList = new ArrayList<UserInfo>();
    private List<DeviceInfo> mLANDeviceList = new ArrayList<DeviceInfo>();

    private View.OnClickListener onLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attempLogin();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initStatusBarStyle();

        initView();
        initLastLoginHistory();
    }

    private void initView() {
        TitleBackLayout mTitleLayout = (TitleBackLayout) findViewById(R.id.layout_title);
        mTitleLayout.setOnClickBack(this);
        mTitleLayout.setBackTitle(R.string.title_back);
        mTitleLayout.setTitle(R.string.title_login);

        mUserTxt = (ClearEditText) findViewById(R.id.editext_user);
        mPwdTxt = (ClearEditText) findViewById(R.id.editext_pwd);
        mPortTxt = (ClearEditText) findViewById(R.id.editext_port);
        mIPTxt = (EditText) findViewById(R.id.editext_ip);
        mLoginBtn = (Button) findViewById(R.id.btn_login);
        mLoginBtn.setOnClickListener(onLoginClickListener);
    }

    private void initLastLoginHistory() {
        List<UserInfo> userList = UserInfoKeeper.all();
        if (!EmptyUtils.isEmpty(userList)) {
            mHistoryUserList.addAll(userList);
            UserInfo userInfo = userList.get(0);
            mUserTxt.setText(userInfo.getName());
            mPwdTxt.setText(userInfo.getPwd());
        }
    }

    private void attempLogin() {
        String user = mUserTxt.getText().toString();
        if (EmptyUtils.isEmpty(user)) {
            AnimUtils.sharkEditText(LoginActivity.this, mUserTxt);
            mUserTxt.requestFocus();
            return;
        }

        String pwd = mPwdTxt.getText().toString();
        if (EmptyUtils.isEmpty(pwd)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPwdTxt);
            mPwdTxt.requestFocus();
            return;
        }

        String ip = mIPTxt.getText().toString();
        if (EmptyUtils.isEmpty(ip)) {
            AnimUtils.sharkEditText(LoginActivity.this, mIPTxt);
            mIPTxt.requestFocus();
            return;
        }

        String port = mPortTxt.getText().toString();
        if (EmptyUtils.isEmpty(port)) {
            port = "80";
            mPortTxt.setText("80");
        } else if (!Utils.checkPort(port)) {
            AnimUtils.sharkEditText(LoginActivity.this, mPortTxt);
            mPortTxt.requestFocus();
            ToastHelper.showToast(R.string.tip_invaild_port);
            return;
        }

        String mac = getLANDeviceMacByIP(ip);
        doLogin(user, pwd, ip, port, mac);
    }

    private void doLogin(String user, String pwd, String ip, String port, String mac) {
        OneOSLoginAPI loginAPI = new OneOSLoginAPI(ip, port, user, pwd, mac);
        loginAPI.setOnLoginListener(new OneOSLoginAPI.OnLoginListener() {
            @Override
            public void onStart(String url) {

            }

            @Override
            public void onSuccess(String url, LoginInfo loginInfo) {

            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {

            }
        });
        loginAPI.login();
    }

    private String getLANDeviceMacByIP(String ip) {
        for (DeviceInfo info : mLANDeviceList) {
            if (info.getIp().equals(ip)) {
                return info.getMac();
            }
        }

        return null;
    }
}
