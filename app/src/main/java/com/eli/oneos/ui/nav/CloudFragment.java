package com.eli.oneos.ui.nav;

/**
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public class CloudFragment extends BaseNavFragment {

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
     * Show/Hide Top Title Bar
     *
     * @param isShown whether show
     */
    @Override
    public void showTitleBar(boolean isShown) {

    }

    /**
     * Show/Hide Bottom Navigation Bar
     *
     * @param isShown whether show
     */
    @Override
    public void showNavBar(boolean isShown) {

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
}
