package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.model.FileOptAction;
import com.eli.oneos.model.FileOptItem;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.Utils;

import java.util.ArrayList;

public class FileOperatePanel extends RelativeLayout {

    private Context mContext;
    private LinearLayout mContainerLayout;
    private OnFileOperateListener mListener;

    private Animation mShowAnim, mHideAnim;

    public FileOperatePanel(Context context) {
        super(context);
    }

    public FileOperatePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_in);
        mHideAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_out);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_file_operate, this, true);
        mContainerLayout = (LinearLayout) view.findViewById(R.id.layout_root_operate);
    }

    public void setOnOperateListener(OnFileOperateListener mListener) {
        this.mListener = mListener;
    }

    private void updatePanelItems(ArrayList<FileOptItem> mList) {
        this.mContainerLayout.removeAllViews();
        if (EmptyUtils.isEmpty(mList)) {
            return;
        }

        int padding = Utils.dipToPx(2);
        int txtSize = getResources().getDimensionPixelSize(R.dimen.text_size_min);
        ColorStateList txtColors = (ColorStateList) getResources().getColorStateList(R.color.selector_gray_to_primary);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        for (FileOptItem item : mList) {
            Button mButton = new Button(getContext());
            mButton.setId(item.getId());
            mButton.setTag(item.getAction());
            mButton.setText(item.getTxtId());
            mButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
            mButton.setTextColor(txtColors);
            mButton.setLayoutParams(mLayoutParams);
            // Button icon with different state
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
            drawable.addState(new int[]{}, getResources().getDrawable(item.getNormalIcon()));
            mButton.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            mButton.setBackgroundResource(android.R.color.transparent);
            mButton.setPadding(padding, padding, padding, padding);
            mButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onClick(v, (FileOptAction) v.getTag());
                    }
                }
            });
            mContainerLayout.addView(mButton);
        }
    }

    public void showPanel(ArrayList<FileOptItem> mList, boolean isAnim) {
        if (EmptyUtils.isEmpty(mList)) {
            return;
        }

        if (!this.isShown()) {
            this.setVisibility(View.VISIBLE);
            if (isAnim) {
                this.startAnimation(mShowAnim);
            }
        }
        updatePanelItems(mList);
    }

    public void hidePanel(boolean isAnim) {
        if (this.isShown()) {
            this.setVisibility(View.INVISIBLE);
            if (isAnim) {
                this.startAnimation(mHideAnim);
            }

            if (mListener != null) {
                mListener.onDismiss();
            }
        }
    }

    public interface OnFileOperateListener {
        void onClick(View view, FileOptAction action);

        void onDismiss();
    }
}
