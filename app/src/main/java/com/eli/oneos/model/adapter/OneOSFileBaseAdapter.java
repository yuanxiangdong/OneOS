package com.eli.oneos.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.eli.oneos.model.api.OneOSFile;
import com.eli.oneos.model.user.LoginSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OneOSFileBaseAdapter extends BaseAdapter {
    public LayoutInflater mInflater;
    public List<OneOSFile> mFileList = new ArrayList<OneOSFile>();
    public HashMap<Integer, Boolean> mSelectedMap = new HashMap<Integer, Boolean>();
    public boolean isMultiChoose = false;
    public String mBasicUrl = null;
    public String mSession = null;
    public OnMultiChooseClickListener mListener = null;

    public OneOSFileBaseAdapter(Context context, List<OneOSFile> fileList, OnMultiChooseClickListener listener, LoginSession mLoginSession) {
        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
        this.mFileList = fileList;
        mBasicUrl = mLoginSession.getBaseUrl();
        mSession = mLoginSession.getSession();

        initSelectedMap();
    }

    /**
     * init Selected Map
     */
    private void initSelectedMap() {
        mSelectedMap.clear();
        for (int i = 0; i < mFileList.size(); i++) {
            mSelectedMap.put(i, false);
        }
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
            initSelectedMap();
        }

        notifyDataSetChanged();
    }

    public void setIsMultiModel(boolean isMulti) {
        if (this.isMultiChoose != isMulti) {
            this.isMultiChoose = isMulti;
            if (isMulti) {
                initSelectedMap();
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMultiChooseModel() {
        return this.isMultiChoose;
    }

    public HashMap<Integer, Boolean> getIsSelected() {
        return mSelectedMap;
    }

    public ArrayList<OneOSFile> getSelectFileList() {
        ArrayList<OneOSFile> mList = new ArrayList<>();
        if (isMultiChoose) {
            for (HashMap.Entry<Integer, Boolean> entry : mSelectedMap.entrySet()) {
                if (entry.getValue()) {
                    mList.add(mFileList.get(entry.getKey()));
                }
            }
        }

        return mList;
    }

    public interface OnMultiChooseClickListener {
        void onClick(View view);
    }
}
