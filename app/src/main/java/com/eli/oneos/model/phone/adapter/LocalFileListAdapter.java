package com.eli.oneos.model.phone.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalFileListAdapter extends LocalFileBaseAdapter {

    public LocalFileListAdapter(Context context, List<File> fileList, ArrayList<File> selectedList, OnMultiChooseClickListener listener) {
        super(context, fileList, selectedList, listener);
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

    class ViewHolder {
        ImageView mIconView;
        TextView mNameTxt;
        TextView mTimeTxt;
        TextView mSizeTxt;
        CheckBox mSelectCb;
        ImageButton mSelectIBtn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = (CheckBox) convertView.findViewById(R.id.cb_select);
            holder.mSizeTxt = (TextView) convertView.findViewById(R.id.txt_size);
            holder.mTimeTxt = (TextView) convertView.findViewById(R.id.txt_time);
            holder.mSelectIBtn = (ImageButton) convertView.findViewById(R.id.ibtn_select);
            holder.mSelectIBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(v);
                    }
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mSelectIBtn.setTag(position); // for get Select Button Index

        File file = mFileList.get(position);
        holder.mNameTxt.setText(file.getName());
        holder.mIconView.setTag(file.getName());
        holder.mTimeTxt.setText(FileUtils.formatTime(file.lastModified()));
        holder.mSizeTxt.setText(FileUtils.fmtFileSize(file.length()));

        if (FileUtils.isPictureFile(file.getName())) {
            showPicturePreview(holder.mIconView, file);
        } else {
            holder.mIconView.setImageResource(FileUtils.fmtFileIcon(file));
        }

        if (isMultiChooseModel()) {
            holder.mSelectIBtn.setVisibility(View.GONE);
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedList().contains(file));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
            holder.mSelectIBtn.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
