package com.eli.oneos.widget;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.model.FileOperateItem;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.Utils;

import java.util.ArrayList;

public class FileOperatePanel extends RelativeLayout {

    private Context mContext;
    private LinearLayout mContainerLayout;
    private OnOperateListener mListener;

    private ArrayList<FileOperateItem> mOperateItemList = new ArrayList<>();
    private Animation mShowAnim, mHidemAnim;

    public FileOperatePanel(Context context) {
        super(context);
    }

    public FileOperatePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_in);
        mHidemAnim = AnimationUtils.loadAnimation(context, R.anim.push_bottom_out);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_file_operate, this, true);
        mContainerLayout = (LinearLayout) view.findViewById(R.id.layout_file_operate);
    }

    public void setOnOperateListener(OnOperateListener mListener) {
        this.mListener = mListener;
    }

    public void showPanel(ArrayList<FileOperateItem> mList, boolean isAnim) {
        if (EmptyUtils.isEmpty(mList)) {
            return;
        }

        this.mOperateItemList.clear();
        this.mOperateItemList.addAll(mList);
        this.mContainerLayout.removeAllViews();

        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        int padding = Utils.dipToPx(5);
        for (FileOperateItem item : this.mOperateItemList) {
//            <Button
//            android:id="@+id/btn_copy"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:layout_weight="1.0"
//            android:background="@android:color/transparent"
//            android:drawableTop="@drawable/button_opt_copy"
//            android:padding="5dp"
//            android:text="@string/copy"
//            android:textColor="@color/selector_gray_to_blue"
//            android:textSize="@dimen/text_size_13" />
            Button mButton = new Button(getContext());
            mButton.setId(item.getId());
            mButton.setText(item.getTxtId());
            mButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, R.dimen.text_size_sm);
            mButton.setTextColor(getResources().getColor(R.color.selector_gray_to_primary));
            mButton.setLayoutParams(mLayoutParams);
            // Button icon
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{-android.R.attr.state_focused}, getResources().getDrawable(item.getNormalIcon()));
            drawable.addState(new int[]{android.R.attr.state_selected, android.R.attr.state_pressed}, getResources().getDrawable(item.getPressedIcon()));
            mButton.setCompoundDrawables(null, drawable, null, null);
            mButton.setPadding(padding, padding, padding, padding);
            mContainerLayout.addView(mButton);
        }


        this.setVisibility(View.VISIBLE);
        if (isAnim) {
            this.startAnimation(mShowAnim);
            mShowAnim.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mListener != null) {
                        mListener.onShown(true);
                    }
                }
            });
        } else {
            if (mListener != null) {
                mListener.onShown(true);
            }
        }
    }

    public void hidePanel(boolean isAnim) {
        if (!this.isShown()) {
            return;
        }

        this.setVisibility(View.GONE);
        if (isAnim) {
            this.startAnimation(mHidemAnim);
        }

        if (mListener != null) {
            mListener.onShown(false);
        }
    }

    public interface OnOperateListener {
        void onClick(View view, int id);

        void onShown(boolean isVisible);
    }
}
