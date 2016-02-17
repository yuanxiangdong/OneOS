package com.eli.oneos.widget.preview;
///*
// Copyright (c) 2012 Roman Truba
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial
// portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
// TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//package com.eli.onespace.widget.viewer;
//
//import java.util.ArrayList;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.PopupWindow;
//import android.widget.Toast;
//
//import com.eli.spaceone.R;
//import com.eli.spaceone.model.FileInfo;
//import com.eli.spaceone.utils.ToastHelper;
//import com.eli.onespace.widget.viewer.BasePagerAdapter.OnItemChangeListener;
//
//public class PictureViewer {
//
//	private GalleryViewPager mViewPager;
//	private Context context;
//
//	private PopupWindow mPopupViewer;
//
//	public PictureViewer(Context context, ArrayList<FileInfo> mPicList, String baseUrl,
//			String session) {
//		this.context = context;
//
//		View view = LayoutInflater.from(context).inflate(R.layout.layout_viewer_picture, null);
//
//		GalleryPagerAdapter pagerAdapter = new GalleryPagerAdapter(context, mPicList, baseUrl,
//				session, null);
//		pagerAdapter.setOnItemChangeListener(new OnItemChangeListener() {
//			@Override
//			public void onItemChange(int currentPosition) {
//				ToastHelper.showToast("Current item is " + currentPosition, Toast.LENGTH_SHORT);
//			}
//		});
//
//		mViewPager = (GalleryViewPager) view.findViewById(R.id.viewer);
//		mViewPager.setOffscreenPageLimit(3);
//		mViewPager.setAdapter(pagerAdapter);
//
//		mViewPager.setFocusableInTouchMode(true);
//		mViewPager.setFocusable(true);
//
//		mPopupViewer = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		mPopupViewer.setAnimationStyle(R.style.AnimationBottomEnterTopExit);
//
//		mPopupViewer
//				.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
//	}
//
//	public void showViewer(View parent) {
//		if (mPopupViewer == null) {
//			return;
//		}
//		mPopupViewer.showAtLocation(parent, Gravity.CENTER, 0, 0);
//		mPopupViewer.setFocusable(true);
//		mPopupViewer.setOutsideTouchable(true);
//		mPopupViewer.update();
//	}
//}