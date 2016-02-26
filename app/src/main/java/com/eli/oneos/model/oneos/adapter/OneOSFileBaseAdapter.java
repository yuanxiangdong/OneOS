package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.eli.oneos.R;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.user.LoginSession;
import com.eli.oneos.utils.HttpBitmap;

import java.util.ArrayList;
import java.util.List;

public class OneOSFileBaseAdapter extends BaseAdapter {
    private static final String TAG = OneOSFileBaseAdapter.class.getSimpleName();

    public LayoutInflater mInflater;
    public List<OneOSFile> mFileList = null;
    public ArrayList<OneOSFile> mSelectedList = null;
    private boolean isMultiChoose = false;
    public LoginSession mLoginSession = null;
    public String mBasicUrl = null;
    public String mSession = null;
    private boolean isWifiAvailable = true;
    public OnMultiChooseClickListener mListener = null;

    public OneOSFileBaseAdapter(Context context, List<OneOSFile> fileList, ArrayList<OneOSFile> selectedList, OnMultiChooseClickListener listener, LoginSession mLoginSession) {
        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
        this.mFileList = fileList;
        this.mSelectedList = selectedList;
        this.mLoginSession = mLoginSession;
        this.mBasicUrl = mLoginSession.getBaseUrl();
        this.mSession = mLoginSession.getSession();

        clearSelectedList();
    }

    /**
     * init Selected Map
     */
    private void clearSelectedList() {
        if (mSelectedList == null) {
            Log.e(TAG, "Selected List is NULL");
            return;
        }
        mSelectedList.clear();
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void notifyDataSetChanged(boolean addItem) {
        if (addItem) {
            clearSelectedList();
        }

        notifyDataSetChanged();
    }

    public void setIsMultiModel(boolean isMulti) {
        if (this.isMultiChoose != isMulti) {
            this.isMultiChoose = isMulti;
            if (isMulti) {
                clearSelectedList();
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMultiChooseModel() {
        return this.isMultiChoose;
    }

    public ArrayList<OneOSFile> getSelectedList() {
        if (isMultiChooseModel()) {
            return mSelectedList;
        }

        return null;
    }

    public int getSelectedCount() {
        int count = 0;
        if (isMultiChoose && null != mSelectedList) {
            count = mSelectedList.size();
        }

        return count;
    }

    public void selectAllItem(boolean isSelectAll) {
        if (isMultiChoose && null != mSelectedList) {
            mSelectedList.clear();
            if (isSelectAll) {
                mSelectedList.addAll(mFileList);
            }
        }
    }

    public void setWifiAvailable(boolean isWifiAvailable) {
        this.isWifiAvailable = isWifiAvailable;
    }

    public void showPicturePreview(ImageView imageView, OneOSFile file) {
        if (!mLoginSession.getUserInfo().getIsPreviewPicOnlyWifi() || isWifiAvailable) {
            HttpBitmap.getInstance().display(imageView, OneOSAPIs.genThumbnailUrl(mLoginSession, file));
        } else {
            imageView.setImageResource(R.drawable.icon_file_pic);
        }
    }

    public interface OnMultiChooseClickListener {
        void onClick(View view);
    }
}
