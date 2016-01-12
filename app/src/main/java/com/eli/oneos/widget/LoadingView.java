package com.eli.oneos.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.eli.oneos.R;

public class LoadingView {

    private static final String TAG = LoadingView.class.getSimpleName();

    private static final int NO_RESOURCES_ID = 0;
    private static final boolean DEFAULT_CANCELABLE = false;

    private static LoadingView INSTANCE = new LoadingView();
    private LoadingProgressDialog mProgressDialog;

    private LoadingView() {
    }

    public static LoadingView getInstance() {
        return LoadingView.INSTANCE;
    }

    public void show(Context context) {
        show(context, NO_RESOURCES_ID);
    }

    public void show(Context context, int msgId) {
        show(context, msgId, DEFAULT_CANCELABLE);
    }

    public void show(Context context, int msgId, boolean cancelable) {
        show(context, msgId, cancelable, null);
    }

    public void show(Context context, int msgId, boolean cancelable, DialogInterface.OnDismissListener listener) {
        if (context == null) {
            return;
        }
        dismiss();

        mProgressDialog = new LoadingProgressDialog(context, msgId, cancelable);
        mProgressDialog.setOnDismissListener(listener);
        try {
            mProgressDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "LoadingProgressDialog Exception: ", e);
        }
    }

    public void dismiss() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public boolean isShown() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    private class LoadingProgressDialog extends ProgressDialog {
        private Context context = null;
        private int msgId = 0;
        private boolean isCancelable = DEFAULT_CANCELABLE;

        public LoadingProgressDialog(Context context, int msgId, boolean isCancelable) {
            super(context);
            this.context = context;
            this.msgId = msgId;
            this.isCancelable = isCancelable;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout_dialog_progress_loading);
            if (msgId > 0) {
                TextView mTipsTxt = (TextView) findViewById(R.id.txt_tips);
                mTipsTxt.setText(context.getResources().getString(msgId));
                mTipsTxt.setVisibility(View.VISIBLE);
            }
            setScreenBrightness();

            this.setCancelable(isCancelable);
        }

        private void setScreenBrightness() {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0f;
            window.setAttributes(lp);
        }
    }

}
