package com.janlent.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;

public class ImgUtil {

	private static File cacheDir;

	private static HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();

	private ImgUtil() {
	}

	static {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"Sodexo/images");
			if (!cacheDir.exists())

				cacheDir.mkdirs();
		}
	}

	public static void loadImageFromUrl(String url, String headerPath) {

		int index = url.lastIndexOf("/");

		String filename = url.substring(index + 1, url.length());

		File f = new File(cacheDir, filename);

		InputStream is;

		try {
			is = new URL(headerPath + url).openStream();

			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Bitmap showImage(String url, int requiredSize) {

		if (cache.containsKey(url)) {
			return cache.get(url);
		}

		int index = url.lastIndexOf("/");

		// String filename = String.valueOf(url.hashCode());

		String filename = url.substring(index, url.length());

		File f = new File(cacheDir, filename);

		Bitmap bitmap = decodeFile(f, requiredSize);
		if(bitmap==null){
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),

		bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;

		final Paint paint = new Paint();

		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		final RectF rectF = new RectF(rect);

		final float roundPx = 12;

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);

		paint.setColor(color);

		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, rect, rect, paint);

		cache.put(url, output);

		return output;

	}

	/**
	 * 展示原图片
	 * 
	 * @param picPath
	 *            图片地址
	 * @return
	 */

	public static Bitmap showOriginalPic(String picPath) {

		int index = picPath.lastIndexOf("/");

		String filename = picPath.substring(index, picPath.length());

		File f = new File(cacheDir, filename);

		try {
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
			return bitmap;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * 
	 * @param f
	 *            图片路径
	 * @param requiredSize
	 *            Find the correct scale value. It should be the power of 2.
	 * @return
	 */

	private static Bitmap decodeFile(File f, int requiredSize) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {

				if (width_tmp / 2 < requiredSize
						|| height_tmp / 2 < requiredSize) {

					break;
				}
				width_tmp /= 2;

				height_tmp /= 2;

				scale++;

			}
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * 根据图片的路径获取bitmap
	 * 
	 * @param imagePath
	 *            图片在卡中的路径
	 * @param ratio
	 *            获取图片的时候的压缩率
	 * @return
	 */

	public static Bitmap getBitmapFromFile(String imagePath, int ratio) {

		if (imagePath == null) {
			return null;
		}

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(imagePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = false;
		opt.inSampleSize = ratio; // width，hight设为原来的十分一
		// 获取资源图片
		return BitmapFactory.decodeStream(fis, null, opt);

	}

	/**
	 * 给bitmap写水印
	 * 
	 * @param src
	 *            bitmap源文件
	 * @param watermark
	 *            水印文字
	 * @param textSize
	 *            文字大小
	 * @param x
	 *            文字在bitmap中的起始x坐标
	 * @param y
	 *            文字在bitmap中的起始y坐标
	 * @return
	 */
	public static Bitmap createBitmapForWatermark(Bitmap src, String watermark,
			int textSize, int x, int y) {
		if (src == null) {
			return null;
		}
		int w = src.getWidth();
		int h = src.getHeight();
		// create the new blank bitmap
		Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
		Canvas cv = new Canvas(newb);
		Paint p = new Paint();
		p.setColor(Color.RED);// 字符颜色
		p.setTextSize(textSize);// 字符大小
		// draw src into
		cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
		// draw watermark into
		cv.drawText(watermark, x, y, p);// 在src的右下角画入水印
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newb;
	}

	/**
	 * 保存图片为JPEG
	 * 
	 * @param bitmap
	 * @param path
	 */
	public static void saveJPGE_After(Bitmap bitmap, String path) {
		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将普通的bitmap转换成倒影的bitmap
	 * 
	 * @param bitmap
	 * @return
	 */
	public static Bitmap bitmap2reflectBimap(Bitmap bitmap) {

		if (bitmap == null) {
			return null;
		}

		int reflectionGap = 4;
		int width = bitmap.getWidth();

		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();

		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);

		canvas.drawBitmap(bitmap, 0, 0, null);

		Paint deafaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);

		paint.setShader(shader);

		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);
		return bitmapWithReflection;

	}

}
