package com.eli.oneos.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.utils.EmptyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * To display customized dialog
 * <p/>
 * Created by gaoyun@eli-tech.com on 2016/4/20.
 */
public class MagicDialog {

    public enum MagicDialogType {
        NOTIFY, CONFIRM, LIST
    }

    public enum MagicDialogButton {
        POSITIVE, NEGATIVE, NEUTRAL
    }

    public static class MagicDialogListItem {
        public int position = 0;
        public String title = null;
        public String content = null;
        public int color = 0;

        public MagicDialogListItem() {
        }

        public MagicDialogListItem(String title, String content, int color) {
            this.title = title;
            this.content = content;
            this.color = color;
        }
    }

    public interface OnMagicDialogClickListener {
        /**
         * On Magic Dialog Button Click
         *
         * @param view   click view
         * @param button {@link com.eli.oneos.widget.MagicDialog.MagicDialogButton}
         */
        void onClick(View view, MagicDialogButton button);
    }

    private Activity activity = null;
    private Resources resources = null;
    private Dialog mDialog = null;
    private LayoutInflater inflater = null;
    // click listener
    private OnMagicDialogClickListener listener = null;
    // dialog cancelable
    private boolean cancelable = false;
    // dialog type
    private MagicDialogType type = MagicDialogType.NOTIFY;
    // dialog top title
    private String title = null;
    // dialog middle content
    private String content = null;
    // dialog content list
    private ArrayList<? extends MagicDialogListItem> list = null;
    // dialog positive button
    private String positive = null;
    // dialog negative button
    private String negative = null;
    // dialog neutral button
    private String neutral = null;
    // are warning
    private boolean warning = false;
    // negative button on left
    private boolean positiveRight = false;
    // dialog bold button
    private MagicDialogButton bold = MagicDialogButton.NEGATIVE;

    public MagicDialog(Activity activity) {
        if (null == activity) {
            throw new NullPointerException("Activity cannot be NULL");
        }
        this.activity = activity;
        resources = activity.getResources();
        mDialog = new Dialog(activity, R.style.DialogTheme);
        inflater = activity.getLayoutInflater();
    }

    private void showConfirmDialog() {
        View view = inflater.inflate(R.layout.magic_dialog_confirm, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        mTextView.setText(content);
        mTextView.setTextColor(warning ? activity.getResources().getColor(R.color.red) : activity.getResources().getColor(R.color.black));
        Button mButton = (Button) view.findViewById(positiveRight ? R.id.btn_dialog_right : R.id.btn_dialog_left);
        mButton.setText(positive);
        mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClick(v, MagicDialogButton.POSITIVE);
                }
                mDialog.dismiss();
            }
        });
        mButton = (Button) view.findViewById(positiveRight ? R.id.btn_dialog_left : R.id.btn_dialog_right);
        mButton.setText(negative);
        mButton.setTypeface(bold == MagicDialogButton.NEGATIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClick(v, MagicDialogButton.NEGATIVE);
                }
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private void showNotifyDialog() {
        View view = inflater.inflate(R.layout.magic_dialog_notify, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        mTextView.setText(content);
        mTextView.setTextColor(warning ? activity.getResources().getColor(R.color.red) : activity.getResources().getColor(R.color.black));
        Button mButton = (Button) view.findViewById(R.id.btn_dialog_positive);
        mButton.setText(positive);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClick(v, MagicDialogButton.POSITIVE);
                }
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    private void showListDialog() {
        View view = inflater.inflate(R.layout.magic_dialog_list, null);
        TextView mTextView = (TextView) view.findViewById(R.id.txt_dialog_title);
        mTextView.setText(title);
        mTextView = (TextView) view.findViewById(R.id.txt_dialog_content);
        if (!EmptyUtils.isEmpty(content)) {
            mTextView.setText(content);
            mTextView.setTextColor(warning ? activity.getResources().getColor(R.color.red) : activity.getResources().getColor(R.color.black));
            mTextView.setVisibility(View.VISIBLE);
        } else {
            mTextView.setVisibility(View.GONE);
        }
        ListView mListView = (ListView) view.findViewById(R.id.listview_dialog);
        if (!EmptyUtils.isEmpty(list)) {
            MagicDialogListAdapter adapter = new MagicDialogListAdapter(activity, list);
            mListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.GONE);
        }

        LinearLayout mLinearLayout;
        Button mButton;
        if (!EmptyUtils.isEmpty(positive)) {
            mLinearLayout = (LinearLayout) view.findViewById(R.id.layout_dialog_positive);
            mButton = (Button) view.findViewById(R.id.btn_dialog_positive);
            mButton.setText(positive);
            mButton.setTypeface(bold == MagicDialogButton.POSITIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onClick(v, MagicDialogButton.POSITIVE);
                    }
                    mDialog.dismiss();
                }
            });
            mLinearLayout.setVisibility(View.VISIBLE);
        }
        if (!EmptyUtils.isEmpty(neutral)) {
            mLinearLayout = (LinearLayout) view.findViewById(R.id.layout_dialog_neutral);
            mButton = (Button) view.findViewById(R.id.btn_dialog_neutral);
            mButton.setText(neutral);
            mButton.setTypeface(bold == MagicDialogButton.NEUTRAL ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onClick(v, MagicDialogButton.NEUTRAL);
                    }
                    mDialog.dismiss();
                }
            });
            mLinearLayout.setVisibility(View.VISIBLE);
        }
        mButton = (Button) view.findViewById(R.id.btn_dialog_negative);
        mButton.setText(negative);
        mButton.setTypeface(bold == MagicDialogButton.NEGATIVE ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClick(v, MagicDialogButton.NEGATIVE);
                }
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
        mDialog.setCancelable(cancelable);
        mDialog.show();
    }

    /**
     * show dialog
     */
    public void show() {
        if (type == MagicDialogType.LIST) {
            showListDialog();
        } else if (type == MagicDialogType.NOTIFY) {
            showNotifyDialog();
        } else {
            showConfirmDialog();
        }

    }

    /**
     * set {@link MagicDialog#type}
     *
     * @param type {@link MagicDialog#type}
     * @return {@link MagicDialog}
     */
    public MagicDialog type(MagicDialogType type) {
        if (null == type) {
            throw new NullPointerException("MagicDialogType cannot be NULL");
        }
        this.type = type;

        return this;
    }

    /**
     * set {@link MagicDialog#cancelable}
     *
     * @param cancelable {@link MagicDialog#cancelable}
     * @return {@link MagicDialog}
     */
    public MagicDialog cancelable(boolean cancelable) {
        this.cancelable = cancelable;

        return this;
    }

    /**
     * Set {@link MagicDialog#title}
     *
     * @param id title resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#title(String)
     */
    public MagicDialog title(int id) {
        try {
            title = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#title}
     *
     * @param title title string
     * @return {@link MagicDialog}
     * @see MagicDialog#title(int)
     */
    public MagicDialog title(String title) {
        this.title = title;

        return this;
    }

    /**
     * Set {@link MagicDialog#content}
     *
     * @param id content resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#content(String)
     */
    public MagicDialog content(int id) {
        try {
            content = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#content}
     *
     * @param content content string
     * @return {@link MagicDialog}
     * @see MagicDialog#content(int)
     */
    public MagicDialog content(String content) {
        this.content = content;

        return this;
    }

    /**
     * Set {@link MagicDialog#list}
     *
     * @param list item list
     * @return MagicDialog#content(int)
     */
    public MagicDialog list(ArrayList<? extends MagicDialogListItem> list) {
        this.list = list;

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param positive positive string
     * @return {@link MagicDialog}
     * @see MagicDialog#positive(int)
     */
    public MagicDialog positive(String positive) {
        this.positive = positive;

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param id positive resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#positive(String)
     */
    public MagicDialog positive(int id) {
        try {
            positive = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param negative negative string
     * @return {@link MagicDialog}
     * @see MagicDialog#negative(int)
     */
    public MagicDialog negative(String negative) {
        this.negative = negative;

        return this;
    }

    /**
     * Set {@link MagicDialog#positive}
     *
     * @param id negative resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#negative(String)
     */
    public MagicDialog negative(int id) {
        try {
            negative = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#neutral}
     *
     * @param id neutral resource id
     * @return {@link MagicDialog}
     * @see MagicDialog#neutral(String)
     */
    public MagicDialog neutral(int id) {
        try {
            neutral = resources.getString(id);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set {@link MagicDialog#neutral}
     *
     * @param neutral neutral string
     * @return {@link MagicDialog}
     * @see MagicDialog#neutral(int)
     */
    public MagicDialog neutral(String neutral) {
        this.neutral = neutral;

        return this;
    }

    /**
     * Set {@link MagicDialog#warning}
     *
     * @param warning are warning
     * @return {@link MagicDialog}
     */
    public MagicDialog warning(boolean warning) {
        this.warning = warning;

        return this;
    }

    /**
     * Set {@link MagicDialog#warning} {@code true}
     *
     * @return {@link MagicDialog}
     * @see MagicDialog#warning(boolean)
     */
    public MagicDialog warning() {
        this.warning = true;

        return this;
    }

    /**
     * Set {@link OnMagicDialogClickListener}
     *
     * @param listener {@link OnMagicDialogClickListener}
     * @return {@link MagicDialog}
     */
    public MagicDialog listener(OnMagicDialogClickListener listener) {
        this.listener = listener;

        return this;
    }

    /**
     * Set {@link MagicDialog#positiveRight} {@code true}, negative button will be on the right.
     *
     * @return {@link MagicDialog}
     */
    public MagicDialog negativeRight() {
        this.positiveRight = false;

        return this;
    }

    /**
     * Set {@link MagicDialog#positiveRight} {@code false}, positive button will be on the right.
     *
     * @return {@link MagicDialog}
     */
    public MagicDialog positiveRight() {
        this.positiveRight = true;

        return this;
    }

    /**
     * Set {@link MagicDialog#bold} {@link com.eli.oneos.widget.MagicDialog.MagicDialogButton}
     *
     * @param button {@link com.eli.oneos.widget.MagicDialog.MagicDialogButton}
     * @return {@link MagicDialog}
     */
    public MagicDialog bold(MagicDialogButton button) {
        this.bold = button;

        return this;
    }

    private static class MagicDialogListAdapter extends BaseAdapter {
        public LayoutInflater mInflater;
        private List<? extends MagicDialogListItem> mItemList;

        public MagicDialogListAdapter(Context context, List<? extends MagicDialogListItem> itemList) {
            this.mInflater = LayoutInflater.from(context);
            this.mItemList = itemList;
        }

        @Override
        public int getCount() {
            return mItemList.size();
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

            String title = mItemList.get(position).title;
            if (null == title) {
                holder.mTitleTxt.setVisibility(View.GONE);
            } else {
                holder.mTitleTxt.setText(title);
                holder.mTitleTxt.setTextColor(mItemList.get(position).color);
                holder.mTitleTxt.setVisibility(View.VISIBLE);
            }
            holder.mContentTxt.setText(mItemList.get(position).content);
            holder.mContentTxt.setTextColor(mItemList.get(position).color);

            return convertView;
        }
    }
}
