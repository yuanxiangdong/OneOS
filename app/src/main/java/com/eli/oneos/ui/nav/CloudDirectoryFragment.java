package com.eli.oneos.ui.nav;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eli.oneos.R;
import com.eli.oneos.model.api.OneOSGetFilesAPI;
import com.eli.oneos.model.user.LoginManager;
import com.eli.oneos.model.user.LoginSession;
import com.eli.oneos.widget.pullrefresh.PullToRefreshListView;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudDirectoryFragment extends Fragment {
    private static final String TAG = CloudDirectoryFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_directory, container, false);

        final PullToRefreshListView pullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.listview);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                pullToRefreshListView.setRefreshing();
            }

        }, 1000);

        LoginSession loginSession = LoginManager.getInstance().getLoginSession();
        OneOSGetFilesAPI getFilesAPI = new OneOSGetFilesAPI(loginSession.getDeviceInfo().getIp(), loginSession.getDeviceInfo().getPort(), loginSession.getSession(), "/");
        getFilesAPI.login();

        return view;
    }

}
