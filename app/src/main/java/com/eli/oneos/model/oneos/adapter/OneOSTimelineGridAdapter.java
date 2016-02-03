//package com.eli.oneos.model.oneos.adapter;
//
//import android.content.Context;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.eli.oneos.R;
//import com.eli.oneos.model.oneos.OneOSFile;
//import com.eli.oneos.model.user.LoginSession;
//import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersSimpleAdapter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class OneOSTimelineGridAdapter extends OneOSFileBaseAdapter implements StickyGridHeadersSimpleAdapter {
//
//    public OneOSTimelineGridAdapter(Context context, List<OneOSFile> fileList, ArrayList<OneOSFile> selectedList, LoginSession mLoginSession) {
//        super(context, fileList, selectedList, null, mLoginSession);
//    }
//
//    @Override
//    public int getCount() {
//        return mFileList.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return position;
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    /**
//     * Get the header id associated with the specified position in the list.
//     *
//     * @param position The position of the item within the adapter's data set whose
//     *                 header id we want.
//     * @return The id of the header at the specified position.
//     */
//    @Override
//    public long getHeaderId(int position) {
//        return 0;
//    }
//
//    /**
//     * Get a View that displays the header data at the specified position in the
//     * set. You can either create a View manually or inflate it from an XML
//     * layout file.
//     *
//     * @param position    The position of the header within the adapter's header data
//     *                    set.
//     * @param convertView The old view to reuse, if possible. Note: You should check
//     *                    that this view is non-null and of an appropriate type before
//     *                    using. If it is not possible to convert this view to display
//     *                    the correct data, this method can create a new view.
//     * @param parent      The parent that this view will eventually be attached to.
//     * @return A View corresponding to the data at the specified position.
//     */
//    @Override
//    public View getHeaderView(int position, View convertView, ViewGroup parent) {
//        return null;
//    }
//
//    class ViewHolder {
//        ImageView mIconView;
//        TextView mNameTxt;
//        CheckBox mSelectCb;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        if (convertView == null) {
//            convertView = mInflater.inflate(R.layout.item_gridview_filelist, null);
//
//            holder = new ViewHolder();
//            holder.mIconView = (ImageView) convertView.findViewById(R.id.iv_icon);
//            holder.mNameTxt = (TextView) convertView.findViewById(R.id.txt_name);
//            holder.mSelectCb = (CheckBox) convertView.findViewById(R.id.cb_select);
//
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        OneOSFile file = mFileList.get(position);
//        holder.mNameTxt.setText(file.getName());
//        holder.mIconView.setTag(file.getName());
//
//        if (file.isEncrypt()) {
//            holder.mIconView.setImageResource(R.drawable.icon_file_encrypt);
//        } else {
//            if (file.isPicture()) {
//                // TODO...  load picture preview
//                holder.mIconView.setImageResource(R.drawable.icon_file_pic);
//            } else {
//                holder.mIconView.setImageResource(file.getIcon());
//            }
//        }
//
//        if (isMultiChooseModel()) {
//            holder.mSelectCb.setVisibility(View.VISIBLE);
//            holder.mSelectCb.setChecked(getSelectedList().contains(file));
//        } else {
//            holder.mSelectCb.setVisibility(View.GONE);
//        }
//
//        return convertView;
//    }
//}
