package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;

import java.util.ArrayList;

public class SpinnerView {
    private static final String TAG = SpinnerView.class.getSimpleName();

    private ArrayList<String> itemList;
    private ArrayList<Integer> iconList;
    private Context context;
    private PopupWindow mPopupSpinner;
    private ListView mListView;
    private RelativeLayout mBackLayout;
    private OnSpinnerButtonClickListener mOnButtonClickListener;

    public SpinnerView(Context context, int width) {
        this.context = context;
        itemList = new ArrayList<String>();
        iconList = new ArrayList<Integer>();

        View view = LayoutInflater.from(context).inflate(R.layout.layout_spinner_view, null);

        mListView = (ListView) view.findViewById(R.id.listview_spinner);
        mListView.setVisibility(View.VISIBLE);
        mListView.setAdapter(new PopupMenuAdapter());
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);

        mPopupSpinner = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);
        mPopupSpinner.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
    }

    public void addSpinnerItems(String[] items) {
        for (String s : items) {
            itemList.add(s);
        }
    }

    public void addSpinnerItems(String[] items, int[] icons) {
        for (String s : items) {
            itemList.add(s);
        }
        for (Integer i : icons) {
            iconList.add(i);
        }
    }

    public void addSpinnerItems(ArrayList<String> list) {
        if (null != list) {
            itemList.addAll(list);
        }
    }

    public void addSpinnerItems(ArrayList<String> list, ArrayList<Integer> icons) {
        if (null != list) {
            itemList.addAll(list);
        }
        if (null != icons) {
            iconList.addAll(icons);
        }
    }

    public void addSpinnerItem(String item) {
        itemList.add(item);
    }

    public String getSpinnerItem(int index) {
        if (index < itemList.size()) {
            return itemList.get(index);
        }

        return null;
    }

    public void showPopupTop(View parent) {
        mPopupSpinner.showAtLocation(parent, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, context
                .getResources().getDimensionPixelSize(R.dimen.layout_spinner_line_height));
        mPopupSpinner.setFocusable(true);
        mPopupSpinner.setOutsideTouchable(true);
        mPopupSpinner.update();
    }

    public void showPopupCenter(View parent) {
        mPopupSpinner.showAtLocation(parent, Gravity.CENTER, 0, context.getResources().getDimensionPixelSize(R.dimen.layout_spinner_line_height));
        mPopupSpinner.setFocusable(true);
        mPopupSpinner.setOutsideTouchable(true);
        mPopupSpinner.update();
    }

    public void showPopupDown(View parent) {
        mPopupSpinner.showAsDropDown(parent);
        mPopupSpinner.setFocusable(true);
        mPopupSpinner.setOutsideTouchable(true);
        mPopupSpinner.update();
    }

    public void dismiss() {
        if (mPopupSpinner != null && mPopupSpinner.isShowing()) {
            mPopupSpinner.dismiss();
        }
    }

    public boolean isShown() {
        return mPopupSpinner.isShowing();
    }

    public void setOnSpinnerItemClickListener(OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public void setOnSpinnerButtonClickListener(OnSpinnerButtonClickListener listener) {
        this.mOnButtonClickListener = listener;
    }

    public void setOnSpinnerDismissListener(OnDismissListener listener) {
        mPopupSpinner.setOnDismissListener(listener);
    }

    private final class PopupMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_listview_popup, null);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.mTitleTxt = (TextView) convertView.findViewById(R.id.txt_spinner_title);
                holder.mRightIBtn = (ImageView) convertView.findViewById(R.id.ibtn_spinner_right);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTitleTxt.setText(itemList.get(position));
            if (iconList != null && iconList.size() >= position) {
                holder.mRightIBtn.setImageResource(iconList.get(position));
                if (mOnButtonClickListener != null) {
                    holder.mRightIBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mOnButtonClickListener.onClick(v, position);
                        }
                    });
                }
            } else {
                holder.mRightIBtn.setVisibility(View.GONE);
            }

            return convertView;
        }

        private final class ViewHolder {
            ImageView mRightIBtn;
            TextView mTitleTxt;
        }
    }

    public interface OnSpinnerButtonClickListener {
        void onClick(View view, int index);
    }
}
