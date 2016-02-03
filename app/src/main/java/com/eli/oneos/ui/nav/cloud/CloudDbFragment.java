package com.eli.oneos.ui.nav.cloud;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.eli.oneos.R;
import com.eli.oneos.constant.Constants;
import com.eli.oneos.constant.OneOSAPIs;
import com.eli.oneos.model.FileManageAction;
import com.eli.oneos.model.FileOrderType;
import com.eli.oneos.model.oneos.OneOSFile;
import com.eli.oneos.model.oneos.OneOSFileManage;
import com.eli.oneos.model.oneos.OneOSFileType;
import com.eli.oneos.model.oneos.adapter.OneOSFileBaseAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSFileGridAdapter;
import com.eli.oneos.model.oneos.adapter.OneOSStickyListAdapter;
import com.eli.oneos.model.oneos.api.OneOSListDBAPI;
import com.eli.oneos.model.oneos.comp.OneOSFileNameComparator;
import com.eli.oneos.model.oneos.comp.OneOSFileTimeComparator;
import com.eli.oneos.ui.MainActivity;
import com.eli.oneos.ui.nav.BaseNavFragment;
import com.eli.oneos.utils.AnimUtils;
import com.eli.oneos.utils.EmptyUtils;
import com.eli.oneos.utils.ToastHelper;
import com.eli.oneos.widget.FileManagePanel;
import com.eli.oneos.widget.FileSelectPanel;
import com.eli.oneos.widget.PullToRefreshView;
import com.eli.oneos.widget.sticky.gridview.StickyGridHeadersGridView;
import com.eli.oneos.widget.sticky.listview.StickyListHeadersListView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by gaoyun@eli-tech.com on 2016/02/03.
 */
public class CloudDbFragment extends BaseCloudFragment {
    private static final String TAG = CloudDbFragment.class.getSimpleName();

    private StickyListHeadersListView mListView;
    private StickyGridHeadersGridView mGridView;
    private PullToRefreshView mPullToRefreshView;
    private boolean isPullDownRefresh = true;
    private OneOSStickyListAdapter mListAdapter;
    private OneOSFileGridAdapter mGridAdapter;

    private AdapterView.OnItemClickListener mFileItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }
            mLastClickPosition = position;
            mLastClickItem2Top = view.getTop();

            OneOSFileBaseAdapter mAdapter = getCurFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (isMultiMode) {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                OneOSFile file = mFileList.get(position);
                boolean isSelected = mClickedCheckBox.isChecked();
                if (isSelected) {
                    mSelectedList.remove(file);
                } else {
                    mSelectedList.add(file);
                }
                mClickedCheckBox.toggle();

                mAdapter.notifyDataSetChanged();
                updateSelectAndManagePanel();
            } else {
                OneOSFile file = mFileList.get(position);
                curPath = file.getPath();
                autoPullToRefresh();
            }
        }
    };
    private AdapterView.OnItemLongClickListener mFileItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent instanceof ListView) {
                position -= 1; // for PullToRefreshView header
            }

            OneOSFileBaseAdapter mAdapter = getCurFileAdapter();
            boolean isMultiMode = mAdapter.isMultiChooseModel();
            if (!isMultiMode) {
                setMultiModel(true, position);
                updateSelectAndManagePanel();
            } else {
                CheckBox mClickedCheckBox = (CheckBox) view.findViewById(R.id.cb_select);
                OneOSFile file = mFileList.get(position);
                boolean isSelected = mClickedCheckBox.isChecked();
                if (isSelected) {
                    mSelectedList.remove(file);
                } else {
                    mSelectedList.add(file);
                }
                mClickedCheckBox.toggle();

                mAdapter.notifyDataSetChanged();
                updateSelectAndManagePanel();
            }

            return true;
        }
    };
    private FileSelectPanel.OnFileSelectListener mFileSelectListener = new FileSelectPanel.OnFileSelectListener() {
        @Override
        public void onSelect(boolean isSelectAll) {
            getCurFileAdapter().selectAllItem(isSelectAll);
            getCurFileAdapter().notifyDataSetChanged();
            updateSelectAndManagePanel();
        }

        @Override
        public void onDismiss() {
            setMultiModel(false, 0);
        }
    };
    private FileManagePanel.OnFileManageListener mFileManageListener = new FileManagePanel.OnFileManageListener() {
        @Override
        public void onClick(View view, ArrayList<OneOSFile> selectedList, FileManageAction action) {
            if (EmptyUtils.isEmpty(selectedList)) {
                ToastHelper.showToast(R.string.tip_select_file);
            } else {
                isSelectionLastPosition = true;
                OneOSFileManage fileManage = new OneOSFileManage(mMainActivity, mLoginSession, mOrderLayout, new OneOSFileManage.OnManageCallback() {
                    @Override
                    public void onComplete(boolean isSuccess) {
                        autoPullToRefresh();
                    }
                });
                fileManage.manage(mFileType, action, selectedList);
            }
        }

        @Override
        public void onDismiss() {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "On Create View");

        View view = inflater.inflate(R.layout.fragment_nav_cloud_db, container, false);

        mMainActivity = (MainActivity) getActivity();
        mParentFragment = (BaseNavFragment) getParentFragment();
        curPath = OneOSAPIs.ONE_OS_PRIVATE_ROOT_DIR;
        mFileType = OneOSFileType.PRIVATE;

        initLoginSession();
        initView(view);

        return view;
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Log.d(TAG, "On Configuration Changed");
//        int orientation = this.getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//
//        }
//
//        mListView.setAdapter(mListAdapter);
//        mGridView.setAdapter(mGridAdapter);
//        mListAdapter.notifyDataSetChanged();
//        mGridAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onResume() {
        super.onResume();
        autoPullToRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        setMultiModel(false, 0);
    }

    private void initView(View view) {
        mSlideInAnim = AnimationUtils.loadAnimation(mMainActivity, R.anim.slide_in_from_top);
        mSlideOutAnim = AnimationUtils.loadAnimation(mMainActivity, R.anim.slide_out_to_top);
        mOrderLayout = (LinearLayout) view.findViewById(R.id.layout_order_view);

        final RadioGroup mOrderGroup = (RadioGroup) view.findViewById(R.id.rg_order);
        mOrderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FileOrderType orderType = (checkedId == R.id.rbtn_order_name) ? FileOrderType.NAME : FileOrderType.TIME;
                if (mOrderType != orderType) {
                    mOrderType = orderType;
                    notifyRefreshComplete(false);
                }
            }
        });
        CheckBox mSwitcherBox = (CheckBox) view.findViewById(R.id.cb_switch_view);
        mSwitcherBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isListShown != isChecked) {
                    isListShown = isChecked;
                    if (isListShown) {
                        mGridView.setVisibility(View.GONE);
                        mListView.setVisibility(View.VISIBLE);
                        mListAdapter.notifyDataSetChanged();
                    } else {
                        mListView.setVisibility(View.GONE);
                        mGridView.setVisibility(View.VISIBLE);
                        mGridAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.layout_pull_refresh);
        mPullToRefreshView.setOnHeaderRefreshListener(new PullToRefreshView.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(PullToRefreshView view) {
                isPullDownRefresh = true;
                setMultiModel(false, 0);
                getOneOSFileList(curPath);
            }
        });
        mPullToRefreshView.setOnFooterRefreshListener(new PullToRefreshView.OnFooterRefreshListener() {
            @Override
            public void onFooterRefresh(PullToRefreshView view) {
                isPullDownRefresh = false;
                setMultiModel(false, 0);
                getOneOSFileList(curPath);
            }
        });

        View mEmptyView = view.findViewById(R.id.layout_empty_list);
        mListView = (StickyListHeadersListView) view.findViewById(R.id.listview_timeline);
        mListAdapter = new OneOSStickyListAdapter(getContext(), mFileList, mSelectedList, new OneOSFileBaseAdapter.OnMultiChooseClickListener() {
            @Override
            public void onClick(View view) {
                AnimUtils.shortVibrator();
                setMultiModel(true, (Integer) view.getTag());
                updateSelectAndManagePanel();
            }
        }, mLoginSession);
        mListView.setOnItemClickListener(mFileItemClickListener);
        mListView.setOnItemLongClickListener(mFileItemLongClickListener);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(mListAdapter);
        mListView.setVisibility(View.VISIBLE); // TODO..

        mEmptyView = view.findViewById(R.id.layout_empty_grid);
        mGridView = (StickyGridHeadersGridView) view.findViewById(R.id.gridview_timeline);
        mGridAdapter = new OneOSFileGridAdapter(getContext(), mFileList, mSelectedList, mLoginSession);
        mGridView.setOnItemClickListener(mFileItemClickListener);
        mGridView.setOnItemLongClickListener(mFileItemLongClickListener);
        mGridView.setEmptyView(mEmptyView);
        mGridView.setAdapter(mGridAdapter);
    }

    public void setFileType(OneOSFileType type, String path) {
        if (this.mFileType != type) {
            this.mFileType = type;
            this.curPath = path;
        }
    }

    private OneOSFileBaseAdapter getCurFileAdapter() {
        if (isListShown) {
            return mListAdapter;
        } else {
            return mGridAdapter;
        }
    }

    /**
     * Use to handle parent Activity back action
     *
     * @return If consumed returns true, otherwise returns false.
     */
    @Override
    public boolean onBackPressed() {
        if (getCurFileAdapter().isMultiChooseModel()) {
            showSelectAndOperatePanel(false);
            return true;
        }

        return false;
    }

    protected void autoPullToRefresh() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mPullToRefreshView.headerRefreshing();
            }
        }, Constants.DELAY_TIME_AUTO_REFRESH);
    }

    private void notifyRefreshComplete(boolean isItemChanged) {
        if (mOrderType == FileOrderType.NAME) {
            Collections.sort(mFileList, new OneOSFileNameComparator());
        } else {
            Collections.sort(mFileList, new OneOSFileTimeComparator());
        }

        ArrayList<Integer> mSectionIndices = new ArrayList<>();
        ArrayList<String> mSectionLetters = new ArrayList<>();
        int index = 0;
        for (OneOSFile file : mFileList) {
            String letter = String.valueOf(file.getMonth());
            if (!mSectionLetters.contains(letter)) {
                mSectionLetters.add(letter);
                mSectionIndices.add(new Integer(index));
                file.setSection(index);
                index++;
            }
        }
        mListAdapter.updateSections(mSectionIndices.toArray(new Integer[]{}), mSectionLetters.toArray(new String[]{}));

        if (isListShown) {
            mListAdapter.notifyDataSetChanged(isItemChanged);
            if (isSelectionLastPosition) {
                mListView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        } else {
            mGridAdapter.notifyDataSetChanged(isItemChanged);
            if (isSelectionLastPosition) {
                mGridView.setSelection(mLastClickPosition);
                // mGridView.setSelectionFromTop(mLastClickPosition, mLastClickItem2Top);
                isSelectionLastPosition = false;
            }
        }
        if (isPullDownRefresh) {
            mPullToRefreshView.onHeaderRefreshComplete();
        } else {
            mPullToRefreshView.onFooterRefreshComplete();
        }
    }

    private void showOrderLayout(boolean isShown) {
        if (isShown == mOrderLayout.isShown()) {
            return;
        }

        if (isShown) {
            mOrderLayout.startAnimation(mSlideInAnim);
            mOrderLayout.setVisibility(View.VISIBLE);
        } else {
            mOrderLayout.startAnimation(mSlideOutAnim);
            mSlideOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mOrderLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void showSelectAndOperatePanel(boolean isShown) {
        mParentFragment.showSelectBar(isShown);
        mParentFragment.showManageBar(isShown);
    }

    private void updateSelectAndManagePanel() {
        mParentFragment.updateSelectBar(mFileList.size(), mSelectedList.size(), mFileSelectListener);
        mParentFragment.updateManageBar(mFileType, mSelectedList, mFileManageListener);
    }

    private boolean setMultiModel(boolean isSetMultiModel, int position) {
        boolean curIsMultiModel = getCurFileAdapter().isMultiChooseModel();
        if (curIsMultiModel == isSetMultiModel) {
            return false;
        }

        if (isSetMultiModel) {
            updateSelectAndManagePanel();
            showSelectAndOperatePanel(true);
            mListAdapter.setIsMultiModel(true);
            mGridAdapter.setIsMultiModel(true);
            mSelectedList.add(mFileList.get(position));
            getCurFileAdapter().notifyDataSetChanged();
            return true;
        } else {
            showSelectAndOperatePanel(false);
            mListAdapter.setIsMultiModel(false);
            mGridAdapter.setIsMultiModel(false);
            getCurFileAdapter().notifyDataSetChanged();
            return true;
        }
    }

    private void getOneOSFileList(String path) {
        OneOSListDBAPI listDbAPI = new OneOSListDBAPI(mLoginSession, mFileType);
        listDbAPI.setOnFileListListener(new OneOSListDBAPI.OnFileDBListListener() {
            @Override
            public void onStart(String url) {
            }

            @Override
            public void onSuccess(String url, OneOSFileType type, int total, int pages, int page, ArrayList<OneOSFile> files) {
                mFileList.clear();
                if (!EmptyUtils.isEmpty(files)) {
                    mFileList.addAll(files);
                }

                notifyRefreshComplete(true);
            }

            @Override
            public void onFailure(String url, int errorNo, String errorMsg) {
                ToastHelper.showToast(errorMsg);
                notifyRefreshComplete(true);
            }
        });
        listDbAPI.list();
    }
}
