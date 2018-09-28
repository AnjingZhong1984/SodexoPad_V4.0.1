package com.janlent.sodexo.adapter;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * 相册界面中GridView所使用的适配器，这里主要显示图片。
 * 
 * @author Administrator
 * 
 */
public class ImgGridAdapter extends BaseAdapter {

	private List<String> imgPaths; // 图片路径
	private Activity context;
	public static final float DISPLAY_WIDTH = 100;

	public static final float DISPLAY_HEIGHT = 100;

	public ImgGridAdapter(Activity context, List<String> imgPaths) {

		this.context = context;
		this.imgPaths = imgPaths;

	}

	@Override
	public int getCount() {
		return imgPaths.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			ImageView iv = new ImageView(context);

			convertView = iv;

			iv.setTag(imgPaths.get(position));
			iv.setImageBitmap(decodeBitmap(imgPaths.get(position)));
			iv.setLayoutParams(new GridView.LayoutParams(120, 120));
			convertView = iv;


		}
		return convertView;
	}

	/**
	 * 显示缩略图的代码
	 * 
	 * @param path
	 *            图片的路径
	 * @return bitmap位图
	 */
	private Bitmap decodeBitmap(String path) {

		BitmapFactory.Options op = new BitmapFactory.Options();

		op.inJustDecodeBounds = true;

		Bitmap bmp = BitmapFactory.decodeFile(path, op); // 获取尺寸信息

		// 获取比例大小
		int wRatio = (int) Math.ceil(op.outWidth / DISPLAY_WIDTH);
		int hRatio = (int) Math.ceil(op.outHeight / DISPLAY_HEIGHT);

		// 如果超出指定大小，则缩小相应的比例
		if (wRatio > 1 && hRatio > 1) {
			if (wRatio > hRatio) {
				op.inSampleSize = wRatio;
			} else {
				op.inSampleSize = hRatio;
			}
		}

		op.inJustDecodeBounds = false;

		bmp = BitmapFactory.decodeFile(path, op);

		return bmp;
	}
}
