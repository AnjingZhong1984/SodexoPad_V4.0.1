package com.janlent.sodexo.adapter;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.janlent.sodexo.activity.Image3DActivity;
import com.janlent.sodexo.widget.GalleryFlow;
import com.janlent.utils.ImgUtil;

public class Image3DAdapter extends BaseAdapter {

	private Context mContext;

	private Map<String, Bitmap> cacheBitmap = new HashMap<String, Bitmap>();

	public Image3DAdapter(Context mContext) {
		this.mContext = mContext;

	}

	public int getCount() {
		return Image3DActivity.imageList == null ? 0
				: Image3DActivity.imageList.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("null")
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold mViewHold;
		if (convertView == null) {
			mViewHold = new ViewHold();
			convertView = new ImageView(mContext);
			mViewHold.iv = (ImageView) convertView;

			mViewHold.iv
					.setLayoutParams(new GalleryFlow.LayoutParams(300, 400));

			convertView.setTag(mViewHold);

		} else {
			mViewHold = (ViewHold) convertView.getTag();

			mViewHold.iv.setBackgroundColor(Color.alpha(Color.TRANSPARENT));

		}
		if (ImgUtil.getBitmapFromFile(Image3DActivity.imageList.get(position),
				6) != null) {

			Bitmap ReflectBitmap;

			if (cacheBitmap
					.containsKey(Image3DActivity.imageList.get(position))) {
				ReflectBitmap = cacheBitmap.get(Image3DActivity.imageList
						.get(position));
			} else {
				ReflectBitmap = ImgUtil.bitmap2reflectBimap(ImgUtil
						.getBitmapFromFile(
								Image3DActivity.imageList.get(position), 6));

				cacheBitmap.put(Image3DActivity.imageList.get(position),
						ReflectBitmap);
			}

			mViewHold.iv.setTag(Image3DActivity.imageList.get(position));
			mViewHold.iv.setImageBitmap(ReflectBitmap);

		}

		return mViewHold.iv;
	}

	private class ViewHold {

		ImageView iv;

	}

}
