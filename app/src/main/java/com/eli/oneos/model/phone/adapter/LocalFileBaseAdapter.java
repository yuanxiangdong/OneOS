package com.eli.oneos.model.phone.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.eli.oneos.utils.ToastHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFileBaseAdapter extends BaseAdapter {
    private static final String TAG = LocalFileBaseAdapter.class.getSimpleName();

    public LayoutInflater mInflater;
    public List<File> mFileList = null;
    public ArrayList<File> mSelectedList = null;
    private boolean isMultiChoose = false;
    public OnMultiChooseClickListener mListener = null;

    public LocalFileBaseAdapter(Context context, List<File> fileList, ArrayList<File> selectedList, OnMultiChooseClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
        this.mFileList = fileList;
        this.mSelectedList = selectedList;

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

    public ArrayList<File> getSelectedList() {
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

    public void showPicturePreview(ImageView imageView, File file) {
        ToastHelper.showToast("TODO.. show picture preview");

//        if (!mLoginSession.getUserSettings().getIsPreviewPicOnlyWifi() || isWifiAvailable) {
//            HttpBitmap.getInstance().display(imageView, OneOSAPIs.genThumbnailUrl(mLoginSession, file));
//        } else {
//            imageView.setImageResource(R.drawable.icon_file_pic);
//        }
    }

    public interface OnMultiChooseClickListener {
        void onClick(View view);
    }
}
