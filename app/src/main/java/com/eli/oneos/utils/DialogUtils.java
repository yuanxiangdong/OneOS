package com.eli.oneos.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.eli.oneos.R;

import java.util.List;

public class DialogUtils {
    private static final String TAG = DialogUtils.class.getSimpleName();

    public static final int RESOURCE_ID_NONE = -1;
    private static Dialog mDialog = null;

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param activity  current Activity, it is necessary
     * @param contentId dialog content text resource id
     * @param notifyId  notify text resource id
     * @param mListener dialog button click listener
     */
    public static void showNotifyDialog(Activity activity, int titleId, int contentId,
                                        int notifyId, final OnDialogClickListener mListener) {
        try {
            String title = titleId > 0 ? activity.getResources().getString(titleId) : null;
            String content = contentId > 0 ? activity.getResources().getString(contentId) : null;
            String notify = notifyId > 0 ? activity.getResources().getString(notifyId) : null;
            showNotifyDialog(activity, title, content, notify, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param activity   current Activity, it is necessary
     * @param contentTxt dialog content text
     * @param notifyTxt  notify text
     * @param mListener  dialog button click listener
     */
    public static void showNotifyDialog(Activity activity, String titleTxt, String contentTxt,
                                        String notifyTxt, final OnDialogClickListener mListener) {
        if (activity == null || contentTxt == null) {
            Log.e(TAG, "activity or dialog content is null");
            return;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_notify, null);
        mDialog = new Dialog(activity, R.style.DialogTheme);
        TextView titleTextView = (TextView) dialogView.findViewById(R.id.txt_title);
        TextView contentTextView = (TextView) dialogView.findViewById(R.id.txt_content);

        if (titleTxt != null) {
            titleTextView.setText(titleTxt);
            titleTextView.setVisibility(View.VISIBLE);
        }

        if (contentTxt != null) {
            contentTextView.setText(contentTxt);
            contentTextView.setVisibility(View.VISIBLE);
        }

        if (notifyTxt != null) {
            Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
            positiveBtn.setText(notifyTxt);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(true);
                    }
                    mDialog.dismiss();
                }
            });
        }

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param activity   current Activity, it is necessary
     * @param contentId  dialog content text resource id
     * @param positiveId positive button text resource id
     * @param negativeId negative button text resource id
     * @param mListener  dialog button click listener
     */
    public static void showConfirmDialog(Activity activity, int titleId, int contentId,
                                         int positiveId, int negativeId, final OnDialogClickListener mListener) {
        try {
            String title = titleId > 0 ? activity.getResources().getString(titleId) : null;
            String content = contentId > 0 ? activity.getResources().getString(contentId) : null;
            String positive = positiveId > 0 ? activity.getResources().getString(positiveId) : null;
            String negative = negativeId > 0 ? activity.getResources().getString(negativeId) : null;
            showConfirmDialog(activity, title, content, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param activity    current Activity, it is necessary
     * @param contentTxt  dialog content text
     * @param positiveTxt positive button text
     * @param negativeTxt negative button text
     * @param mListener   dialog button click listener
     */
    public static void showConfirmDialog(Activity activity, String titleTxt, String contentTxt,
                                         String positiveTxt, String negativeTxt, final OnDialogClickListener mListener) {
        if (activity == null || (contentTxt == null && positiveTxt == null)) {
            Log.e(TAG, "activity or dialog content is null");
            return;
        }

        if (positiveTxt == null && negativeTxt == null) {
            Log.e(TAG, "positive and negative content is null");
            return;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm, null);
        mDialog = new Dialog(activity, R.style.DialogTheme);
        TextView titleTextView = (TextView) dialogView.findViewById(R.id.txt_title);
        TextView contentTextView = (TextView) dialogView.findViewById(R.id.txt_content);

        if (titleTxt != null) {
            titleTextView.setText(titleTxt);
            titleTextView.setVisibility(View.VISIBLE);
        }

        if (contentTxt != null) {
            contentTextView.setText(contentTxt);
            contentTextView.setVisibility(View.VISIBLE);
        }

        if (positiveTxt != null) {
            Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
            positiveBtn.setText(positiveTxt);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(true);
                    }
                    mDialog.dismiss();
                }
            });
        }

        if (negativeTxt != null) {
            Button negativeBtn = (Button) dialogView.findViewById(R.id.negative);
            negativeBtn.setText(negativeTxt);
            negativeBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(false);
                    }
                    mDialog.dismiss();
                }
            });
            negativeBtn.setVisibility(View.VISIBLE);
        }

        if (positiveTxt != null && negativeTxt != null) {
            ImageView line = (ImageView) dialogView.findViewById(R.id.line_btn);
            line.setVisibility(View.VISIBLE);
        }

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void showEditDialog(Activity activity, int titleId, int hintId, int defContentId,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            Resources resources = activity.getResources();
            String title = titleId > 0 ? resources.getString(titleId) : null;
            String hint = hintId > 0 ? resources.getString(hintId) : null;
            String defContent = defContentId > 0 ? resources.getString(defContentId) : null;
            String positive = posId > 0 ? resources.getString(posId) : null;
            String negative = negId > 0 ? resources.getString(negId) : null;
            showEditDialog(activity, title, hint, defContent, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void showEditDialog(Activity activity, int titleId, int hintId, String defaultContent,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            Resources resources = activity.getResources();
            String title = titleId > 0 ? resources.getString(titleId) : null;
            String hint = hintId > 0 ? resources.getString(hintId) : null;
            String positive = posId > 0 ? resources.getString(posId) : null;
            String negative = negId > 0 ? resources.getString(negId) : null;
            showEditDialog(activity, title, hint, defaultContent, positive, negative, mListener);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show customized dialog, parameter determines the dialog UI
     *
     * @param activity    current Activity, it is necessary
     * @param contentHint dialog content text
     * @param positiveTxt positive button text
     * @param negativeTxt negative button text
     * @param mListener   dialog button click listener
     */
    public static void showEditDialog(Activity activity, String titleTxt, String contentHint, String defaultContent,
                                      String positiveTxt, String negativeTxt, final OnEditDialogClickListener mListener) {
        if (activity == null) {
            Log.e(TAG, "activity or dialog content is null");
            return;
        }

        if (positiveTxt == null || negativeTxt == null) {
            Log.e(TAG, "positive or negative content is null");
            return;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit, null);
        mDialog = new Dialog(activity, R.style.DialogTheme);
        TextView titleTextView = (TextView) dialogView.findViewById(R.id.txt_title);
        final EditText contentEditText = (EditText) dialogView.findViewById(R.id.et_content);

        if (titleTxt != null) {
            titleTextView.setText(titleTxt);
        }

        if (contentHint != null) {
            contentEditText.setHint(contentHint);
        }
        if (defaultContent != null) {
            contentEditText.setText(defaultContent);
            contentEditText.setSelection(0, defaultContent.length());
        }
        InputMethodUtils.showKeyboard(activity, contentEditText, 200);

        if (positiveTxt != null) {
            Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
            positiveBtn.setText(positiveTxt);
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(true, contentEditText);
                    }
                }
            });
        }

        if (negativeTxt != null) {
            Button negativeBtn = (Button) dialogView.findViewById(R.id.negative);
            negativeBtn.setText(negativeTxt);
            negativeBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(false, contentEditText);
                    }
                    mDialog.dismiss();
                }
            });
        }

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void showEditPwdDialog(final Activity activity, int titleId, int tipsId, int hintId, int confirmHintId,
                                         final int posId, int negId, final OnEditDialogClickListener mListener) {
        if (activity == null) {
            Log.e(TAG, "activity or dialog content is null");
            return;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_pwd, null);
        mDialog = new Dialog(activity, R.style.DialogTheme);
        TextView titleTextView = (TextView) dialogView.findViewById(R.id.txt_title);
        TextView tipsTextView = (TextView) dialogView.findViewById(R.id.txt_tips);
        final EditText pwdEditText = (EditText) dialogView.findViewById(R.id.et_pwd);
        final EditText confirmPwdEditText = (EditText) dialogView.findViewById(R.id.et_pwd_confirm);

        titleTextView.setText(titleId);
        tipsTextView.setText(tipsId);
        pwdEditText.setHint(hintId);
        InputMethodUtils.showKeyboard(activity, pwdEditText, 200);
        confirmPwdEditText.setHint(confirmHintId);

        Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
        positiveBtn.setText(posId);
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String pwd = pwdEditText.getText().toString();
                if (EmptyUtils.isEmpty(pwd)) {
                    AnimUtils.sharkEditText(activity, pwdEditText);
                    pwdEditText.requestFocus();
                    return;
                }
                String cfPwd = confirmPwdEditText.getText().toString();
                if (EmptyUtils.isEmpty(cfPwd)) {
                    AnimUtils.sharkEditText(activity, confirmPwdEditText);
                    confirmPwdEditText.requestFocus();
                    return;
                }
                if (!pwd.equals(cfPwd)) {
                    ToastHelper.showToast(R.string.error_confirm_pwd);
                    return;
                }

                if (mListener != null) {
                    mListener.onClick(true, pwdEditText);
                }
            }
        });

        Button negativeBtn = (Button) dialogView.findViewById(R.id.negative);
        negativeBtn.setText(negId);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(false, pwdEditText);
                }
                mDialog.dismiss();
            }
        });

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    // public static void showListDialog(Activity activity, int titleId, List<String> itemList, int
    // positiveId, final OnDialogClickListener mListener) {
    // try {
    // String title = titleId > 0 ? activity.getResources().getString(titleId) : null;
    // String positive = positiveId > 0 ? activity.getResources().getString(positiveId) : null;
    // showListDialog(activity, title, itemList, positive, mListener);
    // } catch (NotFoundException e) {
    // e.printStackTrace();
    // }
    // }
    //

    public static void showListDialog(Activity activity, List<String> titleList, List<String> contentList, int titleId,
                                      int posId, int negId, final OnDialogClickListener mListener) {
        if (activity == null || titleList == null) {
            Log.e(TAG, "activity or dialog content is null");
            return;
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_list, null);
        final Dialog mDialog = new Dialog(activity, R.style.DialogTheme);

        TextView titleTextView = (TextView) dialogView.findViewById(R.id.txt_title);
        titleTextView.setText(titleId);
        titleTextView.setVisibility(View.VISIBLE);

        ListView mListView = (ListView) dialogView.findViewById(R.id.listview);
        DialogListAdapter mAdapter = new DialogListAdapter(activity, titleList, contentList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        // setListViewVisibleLines(activity, mListView, 6);

        Button positiveBtn = (Button) dialogView.findViewById(R.id.positive);
        positiveBtn.setText(posId);
        positiveBtn.setBackgroundResource(R.drawable.selector_square_bottom_radius_white_trans_gray);
        positiveBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(true);
                }
                mDialog.dismiss();
            }
        });

        if (negId > 0) {
            View lineView = dialogView.findViewById(R.id.line_button);
            lineView.setVisibility(View.VISIBLE);
            Button negativeBan = (Button) dialogView.findViewById(R.id.negative);
            negativeBan.setText(negId);
            negativeBan.setVisibility(View.VISIBLE);
            negativeBan.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(false);
                    }
                    mDialog.dismiss();
                }
            });
        }

        mDialog.setContentView(dialogView);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private static void setListViewVisibleLines(Activity activity, ListView listView, int lines) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int itemCount = listAdapter.getCount();

        int totalHeight = 0;
        if (itemCount > 0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0, 0);
            totalHeight = listItem.getMeasuredHeight() * lines;
        } else {
            totalHeight = Utils.dipToPx(200);
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (lines - 1));
        listView.setLayoutParams(params);
    }

    public static void dismiss() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    /**
     * Customized Dialog Click Listener
     *
     * @author shz
     */
    public interface OnDialogClickListener {
        /**
         * On dialog button click
         *
         * @param isPositiveBtn if true is positive button clicked, else is negative button clicked
         */
        void onClick(boolean isPositiveBtn);
    }

    public interface OnEditDialogClickListener {
        void onClick(boolean isPositiveBtn, EditText mContentEditText);
    }

    private static class DialogListAdapter extends BaseAdapter {
        public LayoutInflater mInflater;
        private List<String> mTitleList;
        private List<String> mContentList;

        public DialogListAdapter(Context context, List<String> titleList, List<String> contentList) {
            this.mTitleList = titleList;
            this.mContentList = contentList;
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mTitleList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            TextView mTitleTxt;
            TextView mContentTxt;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_listview_dialog, null);

                holder = new ViewHolder();
                holder.mTitleTxt = (TextView) convertView.findViewById(R.id.txt_title);
                holder.mContentTxt = (TextView) convertView.findViewById(R.id.txt_content);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mTitleTxt.setText(mTitleList.get(position));
            holder.mContentTxt.setText(mContentList.get(position));

            return convertView;
        }
    }
}
