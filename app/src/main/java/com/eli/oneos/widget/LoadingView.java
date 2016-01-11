package com.eli.oneos.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.eli.oneos.R;

public class LoadingView {

    private static final String TAG = LoadingView.class.getSimpleName();

    private static LoadingProgressDialog mProgressDialog;

    public static void show(int msgId, Context context) {
        if (msgId <= 0 || context == null) {
            return;
        }
        dismiss();

        mProgressDialog = new LoadingProgressDialog(context, msgId);
        try {
            mProgressDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "LoadingProgressDialog Exception: ", e);
        }
    }

    public static void show(int msgId, boolean cancelable, Context context) {
        if (msgId <= 0 || context == null) {
            return;
        }
        dismiss();

        mProgressDialog = new LoadingProgressDialog(context, msgId, cancelable);
        try {
            mProgressDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "LoadingProgressDialog Exception: ", e);
        }
    }
    public static void show(int msgId, boolean cancelable, Context context, DialogInterface.OnDismissListener listener) {
        if (msgId <= 0 || context == null) {
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

    public static void dismiss() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private static class LoadingProgressDialog extends ProgressDialog {
        private Context context = null;
        private int msgId = R.string.loading;
        private boolean isCancelable = true;
        private TextView mTipsTxt;

        public LoadingProgressDialog(Context context, int msgId) {
            super(context);
            this.context = context;
            this.msgId = msgId;
            this.isCancelable = true;
        }

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
            setScreenBrightness();
            this.setOnShowListener(new OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    mTipsTxt = (TextView) LoadingProgressDialog.this.findViewById(R.id.txt_tips);
                    mTipsTxt.setText(context.getResources().getString(msgId));
                }
            });

            this.setCancelable(isCancelable);
        }

        // public int getMsgId() {
        // return msgId;
        // }
        //
        // public void setMsgId(int msgId) {
        // this.msgId = msgId;
        // }

        private void setScreenBrightness() {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0f;
            window.setAttributes(lp);
        }
    }

}
