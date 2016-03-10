package com.eli.oneos.model.oneos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.eli.oneos.R;
import com.eli.oneos.db.greendao.BackupFile;
import com.eli.oneos.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupFileListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private List<BackupFile> mList = new ArrayList<>();
    private onDeleteClickListener listener;
    private int rightWidth = 0;

    public BackupFileListAdapter(Context context, List<BackupFile> mRecords, int rightWidth) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mList = mRecords;
        this.rightWidth = rightWidth;
    }

    @Override
    public int getCount() {
        int length = 0;
        if (mList != null) {
            length = mList.size();
        } else {
            length = 0;
        }
        return length;
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
        LinearLayout mLeftLayout;
        LinearLayout mRightLayout;
        TextView mNameTxt;
        TextView mTimeTxt;
        TextView mStateTxt;
        TextView mDeleteTxt;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_listview_backup_file, null);
            holder = new ViewHolder();

            holder.mLeftLayout = (LinearLayout) convertView.findViewById(R.id.layout_left);
            holder.mRightLayout = (LinearLayout) convertView.findViewById(R.id.layout_right);
            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
            holder.mTimeTxt = (TextView) convertView.findViewById(R.id.txt_time);
            holder.mStateTxt = (TextView) convertView.findViewById(R.id.txt_state);
            holder.mDeleteTxt = (TextView) convertView.findViewById(R.id.txt_delete);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LayoutParams leftLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        holder.mLeftLayout.setLayoutParams(leftLayout);

        LayoutParams rightLayout = new LayoutParams(rightWidth, LayoutParams.MATCH_PARENT);
        holder.mRightLayout.setLayoutParams(rightLayout);

        BackupFile item = mList.get(position);
        holder.mNameTxt.setText(item.getPath());
        holder.mStateTxt.setText(R.string.last_sync_time);
        long time = item.getTime();
        holder.mTimeTxt.setText(time <= 0 ? context.getString(R.string.not_sync) : FileUtils.formatTime(item.getTime()));
        holder.mDeleteTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(position);
                }
            }
        });
        holder.mTimeTxt.setTag(position); // for update status

        return convertView;
    }

    public void updateItem(ListView listView, BackupFile backupFile, File file) {
        int position = -1;
        for (int i = 0; i < mList.size(); i++) {
            BackupFile f = mList.get(i);
            if (f.getId() == backupFile.getId()) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            try {
                View view = listView.getChildAt(position);
                TextView mStateTxt = (TextView) view.findViewById(R.id.txt_state);
                TextView mTimeTxt = (TextView) view.findViewById(R.id.txt_time);
                if (null == file) {
                    mStateTxt.setText(R.string.last_sync_time);
                    mTimeTxt.setText(FileUtils.formatTime(backupFile.getTime()));
                } else {
                    mStateTxt.setText(R.string.is_backup);
                    mTimeTxt.setText(file.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnDeleteListener(onDeleteClickListener listener) {
        this.listener = listener;
    }

    public interface onDeleteClickListener {
        void onDelete(int position);
    }

}
