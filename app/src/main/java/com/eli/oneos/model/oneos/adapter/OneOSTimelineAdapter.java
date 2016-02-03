//package com.eli.oneos.model.oneos.adapter;
//
//import android.content.Context;
//import android.graphics.Point;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.eli.oneos.model.oneos.OneOSFile;
//import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersGridView;
//import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersSimpleAdapter;
//import com.eli.onespace.R;
//import com.eli.onespace.database.UserInfoKeeper;
//import com.eli.onespace.model.FileInfo;
//import com.eli.onespace.utils.ImageLoaderTask;
//import com.eli.onespace.utils.operate.ServerOptConstants;
//import com.eli.onespace.widget.timeline.TimeImageView.OnMeasureListener;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//public class OneOSTimelineAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {
//
//    private List<OneOSFile> mFileList;
//    private LayoutInflater mInflater;
//
//    private HashMap<Integer, Boolean> mSelectedMap = new HashMap<Integer, Boolean>();
//    private boolean isMultiChoose = false;
//
//    private StickyGridHeadersGridView mGridView;
//    private Point mPoint = new Point(0, 0);
//    // private HttpBitmap mHttpBitmap = HttpBitmap.getInstance();
//    private String mBaseUrl = null, mSession = null;
//
////    private ImageLoaderTask bitmapWorkerTask;
//    private boolean isIdle = true;
//
//    public OneOSTimelineAdapter(Context context, List<OneOSFile> list, StickyGridHeadersGridView mGridView) {
//        this.mFileList = list;
//        mInflater = LayoutInflater.from(context);
//        this.mGridView = mGridView;
////        bitmapWorkerTask = ImageLoaderTask.getInstance(context);
//        initUserInfo(context);
//        initSelectMap(false);
//    }
//
//    /**
//     * init date of mSelectedMap
//     */
//    private void initSelectMap(boolean isSelect) {
//        mSelectedMap.clear();
//        for (int i = 0; i < mFileList.size(); i++) {
//            mSelectedMap.put(i, isSelect);
//        }
//    }
//
//    private void initUserInfo(Context context) {
//        mBaseUrl = ServerOptConstants.PRE_IP
//                + UserInfoKeeper.readUserInfo(context, UserInfoKeeper.KEY_DEVICE_IP, null);
//        mSession = UserInfoKeeper.readUserInfo(context, UserInfoKeeper.KEY_SESSION, null);
//    }
//
//    @Override
//    public int getCount() {
//        return mFileList.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return mFileList.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = mInflater.inflate(R.layout.item_gridview_cloud_timeline, parent, false);
//            holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox_select);
//            holder.mEncryptView = (ImageView) convertView.findViewById(R.id.file_crypt);
//            holder.mImageView = (TimeImageView) convertView.findViewById(R.id.grid_item);
//            convertView.setTag(holder);
//
//            holder.mImageView.setOnMeasureListener(new OnMeasureListener() {
//
//                @Override
//                public void onMeasureSize(int width, int height) {
//                    mPoint.set(width, height);
//                }
//            });
//
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        FileInfo mInfo = mFileList.get(position).getFileInfo();
//        if (isMultiChoose) {
//            holder.mCheckBox.setVisibility(View.VISIBLE);
//            holder.mCheckBox.setChecked(mSelectedMap.get(position));
//        } else {
//            holder.mCheckBox.setVisibility(View.INVISIBLE);
//        }
//        if (mInfo.fileEncrypt == 1) {
//            holder.mEncryptView.setVisibility(View.VISIBLE);
//        } else {
//            holder.mEncryptView.setVisibility(View.GONE);
//        }
//
//        holder.mImageView.setTag(mInfo.fullName);
//        if (mInfo.fileEncrypt != 1) {
//            holder.mImageView.setImageResource(R.drawable.icon_file_pic_default);
//            bitmapWorkerTask.DisplayImage(holder.mImageView, mInfo, mBaseUrl, mSession,
//                    R.drawable.icon_file_pic_default);
//            if (isIdle) {
//                bitmapWorkerTask.unlock(isIdle);
//            } else {
//                bitmapWorkerTask.lock(isIdle);
//            }
//            // mHttpBitmap.display(holder.mImageView, getServerFileUri(mInfo.fullName));
//        } else {
//            holder.mImageView.setImageResource(R.drawable.view_timeline_pic_failed);
//        }
//
//        return convertView;
//    }
//
//    @Override
//    public View getHeaderView(int position, View convertView, ViewGroup parent) {
//        HeaderViewHolder mHeaderHolder;
//        if (convertView == null) {
//            mHeaderHolder = new HeaderViewHolder();
//            convertView = mInflater.inflate(R.layout.layout_time_line_header, parent, false);
//            mHeaderHolder.mTextView = (TextView) convertView.findViewById(R.id.header);
//            // mHeaderHolder.mStateView = (ImageView) convertView.findViewById(R.id.iv_state);
//            convertView.setTag(mHeaderHolder);
//        } else {
//            mHeaderHolder = (HeaderViewHolder) convertView.getTag();
//        }
//
//        // int headCount = mGridView.getCountFromHeader(position);
//        // if (headCount == 0) {
//        // mHeaderHolder.mStateView.setImageResource(R.drawable.icon_timeline_open);
//        // } else {
//        // mHeaderHolder.mStateView.setImageResource(R.drawable.icon_timeline_close);
//        // }
//
//        mHeaderHolder.mTextView.setText(mFileList.get(position).getShotTime());
//
//        return convertView;
//    }
//
//    public static class ViewHolder {
//        public TimeImageView mImageView;
//        public ImageView mEncryptView;
//        public CheckBox mCheckBox;
//    }
//
//    public static class HeaderViewHolder {
//        public TextView mTextView;
//        // public ImageView mStateView;
//    }
//
//    @Override
//    public long getHeaderId(int position) {
//        return mFileList.get(position).getSection();
//    }
//
//    // private String getServerFileUri(String fullName) {
//    // String pathUrl = android.net.Uri.encode(fullName);
//    // return mBaseUrl + File.separator + pathUrl + "?s=" + mSession;
//    // }
//
//    public HashMap<Integer, Boolean> getmSelectedMap() {
//        return mSelectedMap;
//    }
//
//    public void setmSelectedMap(HashMap<Integer, Boolean> mSelectedMap) {
//        this.mSelectedMap = mSelectedMap;
//    }
//
//    public void setIsMultiModel(boolean isMulti) {
//        if (!isMultiChoose) {
//            initSelectMap(false);
//        }
//        this.isMultiChoose = isMulti;
//        notifyDataSetChanged();
//    }
//
//    public boolean isMultiModel() {
//        return this.isMultiChoose;
//    }
//
//    @Override
//    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
//    }
//
//    private boolean isAllSelected() {
//        for (HashMap.Entry<Integer, Boolean> entry : mSelectedMap.entrySet()) {
//            if (!entry.getValue()) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    public void toggleSelected() {
//        boolean isSelectAll = isAllSelected();
//        initSelectMap(!isSelectAll);
//
//        notifyDataSetChanged();
//    }
//
//    public int getSelectItemCount() {
//        int count = 0;
//        for (HashMap.Entry<Integer, Boolean> entry : mSelectedMap.entrySet()) {
//            if (entry.getValue()) {
//                count++;
//            }
//        }
//
//        return count;
//    }
//
//    public ArrayList<FileInfo> getSelectFileList() {
//        ArrayList<FileInfo> mList = new ArrayList<FileInfo>();
//        for (HashMap.Entry<Integer, Boolean> entry : mSelectedMap.entrySet()) {
//            if (entry.getValue()) {
//                mList.add(mFileList.get(entry.getKey()).getFileInfo());
//            }
//        }
//
//        return mList;
//    }
//
//    public void setScrollStateBusy(boolean isIdle) {
//        this.isIdle = isIdle;
//    }
//}
