package com.eli.oneos.model.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.model.api.OneOSFile;
import com.eli.oneos.model.user.LoginSession;

import java.util.HashMap;
import java.util.List;

public class OneOSFileGridAdapter extends OneOSFileBaseAdapter {

    public OneOSFileGridAdapter(Context context, List<OneOSFile> fileList, HashMap<Integer, Boolean> selectedMap, LoginSession mLoginSession) {
        super(context, fileList, selectedMap, null, mLoginSession);
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
        CheckBox mSelectCb;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview_filelist, null);

            holder = new ViewHolder();
            holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mSelectCb = (CheckBox) convertView.findViewById(R.id.cb_select);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OneOSFile file = mFileList.get(position);
        holder.mNameTxt.setText(file.getName());
        holder.mIconView.setTag(file.getName());

        if (file.isEncrypt()) {
            holder.mIconView.setImageResource(R.drawable.icon_file_encrypt);
        } else {
            if (file.isPicture()) {
                // TODO...  load picture preview
                holder.mIconView.setImageResource(R.drawable.icon_file_pic);
            } else {
                holder.mIconView.setImageResource(file.getIcon());
            }
        }

        if (isMultiChooseModel()) {
            holder.mSelectCb.setVisibility(View.VISIBLE);
            holder.mSelectCb.setChecked(getSelectedMap().get(position));
        } else {
            holder.mSelectCb.setVisibility(View.GONE);
        }

        return convertView;
    }
}
