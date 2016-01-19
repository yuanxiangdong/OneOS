package com.eli.oneos.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eli.oneos.R;

public class FileSelectPanel extends RelativeLayout {

    private Context mContext;
    private Button mCancelBtn, mSelectBtn;
    private TextView mTitleTxt;

    private OnSelectListener mListener;
    private Animation mShowAnim, mHidemAnim;

    private int totalCount, selectCount;

    public FileSelectPanel(Context context) {
        super(context);
    }

    public FileSelectPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_file_select, this, true);

        mShowAnim = AnimationUtils.loadAnimation(context, R.anim.push_top_in);
        mHidemAnim = AnimationUtils.loadAnimation(context, R.anim.push_top_out);

        mTitleTxt = (TextView) view.findViewById(R.id.text_count);
        mCancelBtn = (Button) view.findViewById(R.id.btn_select_cancel);
        mSelectBtn = (Button) view.findViewById(R.id.btn_select_all);
        mCancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hidePanel(true);
            }
        });
        mSelectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onSelect(totalCount > selectCount);
                }
            }
        });
    }

    public void updateCount(int totalCount, int selectCount) {
        this.totalCount = totalCount;
        this.selectCount = selectCount;

        if (totalCount == selectCount) {
            mSelectBtn.setText(R.string.select_none);
        } else {
            mSelectBtn.setText(R.string.select_all);
        }

        if (selectCount <= 0) {
            mTitleTxt.setText(R.string.hint_select_file);
        } else {
            mTitleTxt.setText(String.format(getResources().getString(R.string.selected_item), selectCount));
        }
    }

    public void setOnSelectListener(OnSelectListener mListener) {
        this.mListener = mListener;
    }

    /**
     * show select panel if is invisible
     *
     * @param isAnim if show with animation
     */
    public void showPanel(boolean isAnim) {
        if (this.isShown()) {
            return;
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

        mTitleTxt.setText(R.string.hint_select_file);
        this.setVisibility(View.GONE);
        if (isAnim) {
            this.startAnimation(mHidemAnim);
        }

        if (mListener != null) {
            mListener.onShown(false);
        }
    }

    public interface OnSelectListener {
        void onSelect(boolean isSelectAll);

        void onShown(boolean isVisible);
    }
}
