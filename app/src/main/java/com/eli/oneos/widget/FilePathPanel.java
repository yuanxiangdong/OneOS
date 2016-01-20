package com.eli.oneos.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.Utils;

import java.io.File;

public class FilePathPanel extends RelativeLayout {
    private static final String TAG = FilePathPanel.class.getSimpleName();

    private Context mContext;
    private LinearLayout mPathLayout;
    private View mLineView;
    private ImageButton mNewFolderBtn;
    private OnPathPanelClickListener mListener;

    private String path = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
    private String mPrivateRootDirShownName = null;
    private String mPublicRootDirShownName = null;
    private int pathMaxWidth = 0, pathMinWidth = 0, pathBtnPadding = 0;

    public FilePathPanel(Context context) {
        super(context);
    }

    public FilePathPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.layout_path_panel, this, true);

        mPrivateRootDirShownName = getResources().getString(R.string.root_dir_name_private);
        mPublicRootDirShownName = getResources().getString(R.string.root_dir_name_public);
        pathMaxWidth = Utils.dipToPx(120);
        pathMinWidth = Utils.dipToPx(30);
        pathBtnPadding = Utils.dipToPx(5);

        mPathLayout = (LinearLayout) view.findViewById(R.id.layout_file_path);
        mLineView = (View) findViewById(R.id.view_path_mid_line);
        mNewFolderBtn = (ImageButton) findViewById(R.id.ibtn_new_folder);
        mNewFolderBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onClick(v, null);
                }
            }
        });
    }

    public void updatePath(String path) {
        this.path = path;
        if (EmptyUtils.isEmpty(this.path)) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            genFilePathLayout();
        }
    }

    public void showNewFolderButton(boolean isShown) {
        mNewFolderBtn.setVisibility(isShown ? View.VISIBLE : View.GONE);
        mLineView.setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    public void setOnPathPanelClickListener(OnPathPanelClickListener listener) {
        this.mListener = listener;
    }

    private void genFilePathLayout() {
        Log.i(TAG, "Add path button:" + path);
        mPathLayout.removeAllViews();

        boolean isPrivateDir = path.startsWith(File.separator);

        try {
            final String rootStr = isPrivateDir ? OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR : OneOSAPIs.ONE_OS_PUBLIC_ROOT_DIR;
            String rootShownName = isPrivateDir ? mPrivateRootDirShownName : mPublicRootDirShownName;
            String setPath = path.replaceFirst(rootStr, rootShownName + File.separator);
            Log.d(TAG, "Add path button:" + setPath);
            final String[] pathItems = setPath.split(File.separator);
            Button[] pathBtn = new Button[pathItems.length];
            Resources resource = getResources();
            ColorStateList csl = (ColorStateList) resource.getColorStateList(R.color.selector_gray_to_primary);

            for (int i = 0; i < pathItems.length; ++i) {
                pathBtn[i] = new Button(getContext());
                pathBtn[i].setTag(i);
                pathBtn[i].setText(pathItems[i]);
                pathBtn[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_size_sm));
                pathBtn[i].setMaxWidth(pathMaxWidth);
                pathBtn[i].setMinWidth(pathMinWidth);
                pathBtn[i].setPadding(pathBtnPadding, 0, pathBtnPadding, 0);
                pathBtn[i].setSingleLine(true);
                pathBtn[i].setEllipsize(TextUtils.TruncateAt.END);
                pathBtn[i].setTextColor(csl);
                pathBtn[i].setGravity(Gravity.CENTER);
                pathBtn[i].setBackgroundResource(R.drawable.bg_path_item);
                mPathLayout.addView(pathBtn[i]);
                pathBtn[i].setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = (Integer) v.getTag();
                        String tarPath = rootStr;
                        for (int j = 1; j <= i; j++) {
                            tarPath += pathItems[j] + File.separator;
                        }
                        Log.d(TAG, "Click target path is " + tarPath);
                        if (null != mListener) {
                            mListener.onClick(v, tarPath);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(TAG, "Generate Path Layout Exception: ", e);
        }
    }

    public interface OnPathPanelClickListener {
        void onClick(View view, String path);
    }
}
