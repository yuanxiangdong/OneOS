package com.eli.oneos.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;

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

        boolean hasTitle = (titleTxt != null);
        if (hasTitle) {
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

        boolean hasTitle = (titleTxt != null);
        if (hasTitle) {
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

    public static void showEditDialog(Activity activity, int titleId, int hintId,
                                      int posId, int negId, final OnEditDialogClickListener mListener) {
        try {
            Resources resources = activity.getResources();
            String title = titleId > 0 ? resources.getString(titleId) : null;
            String hint = hintId > 0 ? resources.getString(hintId) : null;
            String positive = posId > 0 ? resources.getString(posId) : null;
            String negative = negId > 0 ? resources.getString(negId) : null;
            showEditDialog(activity, title, hint, null, positive, negative, mListener);
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

        boolean hasTitle = (titleTxt != null);
        if (hasTitle) {
            titleTextView.setText(titleTxt);
        }

        if (contentHint != null) {
            contentEditText.setHint(contentHint);
        }
        if (defaultContent != null) {
            contentEditText.setText(defaultContent);
            contentEditText.setSelection(defaultContent.length());
        }
        InputMethodUtils.showKeyboard(activity, contentEditText);

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
    // /**
    // * show customized list dailog
    // *
    // * @param activity
    // * @param titleTxt
    // * @param itemList
    // * @param positiveTxt
    // * @param mListener
    // */
    // public static void showListDialog(Activity activity, String titleTxt, List<String> itemList,
    // String positiveTxt, final OnDialogClickListener mListener) {
    // if (activity == null || itemList == null || positiveTxt == null) {
    // Log.e(TAG, "activity or dialog content is null");
    // return;
    // }
    //
    // LayoutInflater inflater = activity.getLayoutInflater();
    // View dialogView = inflater.inflate(R.layout.dialog_list, null);
    // final Dialog mDialog = new Dialog(activity, R.style.DialogTheme);
    //
    // if (titleTxt != null) {
    // TextView titleTextView = (TextView) dialogView.findViewById(R.id.layout_top_title);
    // titleTextView.setText(titleTxt);
    // titleTextView.setVisibility(View.VISIBLE);
    // }
    //
    // ListView mListView = (ListView) dialogView.findViewById(R.id.listview);
    // ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(activity,
    // R.layout.item_listview_dialog, R.id.txt_content, itemList);
    // mListView.setAdapter(mAdapter);
    // mAdapter.notifyDataSetChanged();
    // setListViewVisibleLines(activity, mListView, 4);
    //
    // if (positiveTxt != null) {
    // Button positiveBtn = (Button) dialogView.findViewById(R.id.btn_positive);
    // positiveBtn.setText(positiveTxt);
    // positiveBtn.setVisibility(View.VISIBLE);
    // positiveBtn.setOnClickListener(new OnClickListener() {
    // public void onClick(View v) {
    // if (mListener != null) {
    // mListener.onClick(true);
    // }
    // mDialog.dismiss();
    // }
    // });
    // }
    //
    // mDialog.setContentView(dialogView);
    // mDialog.setCancelable(false);
    // mDialog.show();
    // }
    //
    // private static void setListViewVisibleLines(Activity activity, ListView listView, int lines)
    // {
    // ListAdapter listAdapter = listView.getAdapter();
    // if (listAdapter == null) {
    // return;
    // }
    // int itemCount = listAdapter.getCount();
    //
    // int totalHeight = 0;
    // if (itemCount > 0) {
    // View listItem = listAdapter.getView(0, null, listView);
    // listItem.measure(0, 0);
    // totalHeight = listItem.getMeasuredHeight() * lines;
    // } else {
    // totalHeight = Utils.dipToPx(200);
    // }
    //
    // ViewGroup.LayoutParams params = listView.getLayoutParams();
    // params.height = totalHeight + (listView.getDividerHeight() * (lines - 1));
    // listView.setLayoutParams(params);
    // }

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
}
