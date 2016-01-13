package com.eli.oneos.ui.nav;

/**
 * Navigation Base Abstract Class
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/1/13.
 */
public abstract class BaseNavFragment {

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    public abstract boolean onBackPressed();

    /**
     * Show/Hide Top Title Bar
     *
     * @param isShown whether show
     */
    public abstract void showTitleBar(boolean isShown);

    /**
     * Show/Hide Bottom Navigation Bar
     *
     * @param isShown whether show
     */
    public abstract void showNavBar(boolean isShown);

    /**
     * Network State Changed
     *
     * @param isAvailable
     * @param isWifiAvailable
     */
    public abstract void onNetworkChanged(boolean isAvailable, boolean isWifiAvailable);
}
