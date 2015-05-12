package com.github.cxstone.album.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

/**
 * 图片的相关处�?
 */
public class PhotoUtil {



	/**
	 * decode bitmap form path(big)
	 */
	private static Bitmap decodeBitmapFromPath(String filePath) {
		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		opts.inSampleSize = ImageZoomUtils.computeSampleSize(opts, 768,
				768 * 1024);
		opts.inJustDecodeBounds = false;
		opts.inInputShareable = true;
		opts.inPurgeable = true;

		try {
			bmp = BitmapFactory.decodeFile(filePath, opts);
		} catch (Exception e) {
			bmp = null;
		} catch (OutOfMemoryError e) {
			bmp = null;
			e.printStackTrace();
		}

		if (bmp != null) {
			bmp = rotatePhoto(bmp, filePath);
		}

		// try {
		// int t = bmp.getRowBytes() * bmp.getHeight() / 1024;
		// Log.v("PhotoUtil--decodefile", "---get---bitmap--size--" + t +
		// "k");
		// Log.v("PhotoUtil--decodefile", "--conpute--sample--size=" +
		// opts.inSampleSize + "---");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		return bmp;
	}

	/**
	 * 图片压缩
	 */
	public static Bitmap zoomBitmap(String fromPath, int hight, int width,
			String topath) { // JPEG格式
		int new_hight = 800;
		int new_width = new_hight * width / hight;
		Bitmap bm = null;
		try {
			bm = ImageZoomUtils.getBitmapFromFile(fromPath, new_width,
					new_hight, topath);
		} catch (OutOfMemoryError e) {
			// Toast.makeText(this, "图片过大", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// LogUtils.e(this,TAG, "图片压缩失败");
			e.printStackTrace();
		}
		return bm;
	}

	

	/**
	 * 生成缩略�? path : 图片路径 width: 缩略图宽�? height:缩略图高�?
	 */
	private static Bitmap getSmallPhoto(String path, int width, int height) {
		Bitmap bitmap = null;
		try {
			bitmap = decodeBitmapFromPath(path);
		} catch (Exception e) {
			bitmap = null;
		} catch (OutOfMemoryError e) {
			bitmap = null;
		}
		if (bitmap == null) {
			return null;
		}
		Bitmap bmpSmall = bitmap;
		// 获取宽高相等图片（用于公共模块，相册�?
		if (width == height) {
			try {
				bmpSmall = getCutPhoto(bitmap, width);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return bmpSmall;
		}
		// 等比缩放图片
		if (bitmap.getWidth() > width && bitmap.getHeight() > height) {
			double rate1 = (double) bitmap.getWidth() / (double) width + 0.1;

			double rate2 = (double) bitmap.getHeight() / (double) height + 0.1;
			double rate = rate1 > rate2 ? rate1 : rate2;

			int newWidth = (int) ((double) bitmap.getWidth() / rate);
			int newHeight = (int) ((double) bitmap.getHeight() / rate);
			// 获取压缩图后的图�?
			try {
				bmpSmall = zoomImage(bitmap, newWidth, newHeight);
			} catch (Exception e) {
				bmpSmall = null;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}

			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}

		// try {
		// int t = bmpSmall.getRowBytes() * bmpSmall.getHeight() / 1024;
		// Log.v("PhotoUtil--decodefile", "---get--small--bitmap--size--" + t
		// + "k");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		return bmpSmall;
	}

	/**
	 * 截取图片 length:截取长度
	 */
	private static Bitmap getCutPhoto(Bitmap bitmap, int length) {
		if (bitmap == null) {
			return null;
		}

		Bitmap bmp = null;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		try {
			// 截取等高的图�?
			if (width > height) {
				bmp = Bitmap.createBitmap(bitmap, (width - height) / 2, 0,
						height, height);
			} else if (width < height) {
				bmp = Bitmap.createBitmap(bitmap, 0, 0, width, width);
			}

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		Bitmap newBmp = null;// 缩放后图�?

		if (bmp != null)// 截图成功
		{
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
			}

			bitmap = null;

			try {
				newBmp = zoomImage(bmp, length, length);// 缩放图片
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}

			if (newBmp != null) {
				if (bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
				}

				bmp = null;
			} else {
				newBmp = bmp;
			}
		} else// 截图失败或没有截图（width == height�?
		{
			try {
				newBmp = zoomImage(bitmap, length, length);// 缩放图片
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}

			if (newBmp != null) {
				if (bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
				}

				bitmap = null;
			} else {
				newBmp = bitmap;
			}
		}

		return newBmp;
	}

	/**
	 * 生成会话用户头像 80 * 80
	 */
	private static Bitmap getHeadPhoto(String path) {

		Bitmap bitmap = null;
		try {
			bitmap = decodeBitmapFromPath(path);
		} catch (Exception e) {
			bitmap = null;
		} catch (OutOfMemoryError e) {
			bitmap = null;
		}

		if (bitmap == null) {
			return null;
		}

		Bitmap bmpSmall = bitmap;
		if (bitmap.getWidth() > 80 || bitmap.getHeight() > 80) {
			double rate1 = (double) bitmap.getWidth() / (double) 80 + 0.1;

			double rate2 = (double) bitmap.getHeight() / (double) 80 + 0.1;
			double rate = rate1 > rate2 ? rate1 : rate2;

			int newWidth = (int) ((double) bitmap.getWidth() / rate);
			int newHeight = (int) ((double) bitmap.getHeight() / rate);

			// 获取压缩图后的图�?
			try {
				bmpSmall = zoomImage(bitmap, newWidth, newHeight);
			} catch (Exception e) {
				bmpSmall = null;
			} catch (OutOfMemoryError e) {

			}

			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}

		return bmpSmall;
	}

	/**
	 * base64编码处理: file to base64
	 * 
	 * @param srcUrl
	 *            文件路径
	 */
	public static String fileToBase64String(String srcUrl) {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		String base64Str = null;
		try {
			fis = new FileInputStream(srcUrl);
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1892];
			int count = 0;

			while ((count = fis.read(buffer)) > -1) {
				baos.write(buffer, 0, count);
			}

			base64Str = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return base64Str;
	}

	/**
	 * bitmap to base64
	 */
	public static String bitmapToBase64Strng(Bitmap bmp) {
		String str = null;
		ByteArrayOutputStream baos = null;

		if (bmp != null) {
			try {
				baos = new ByteArrayOutputStream();
				int rate = 100;
				if ((bmp.getRowBytes()) * (bmp.getHeight()) / 1024 > 500) {// 大于500k质量压缩�?95%
					rate = 90;
				}
				bmp.compress(Bitmap.CompressFormat.JPEG, rate, baos);
				byte[] bmpBytes = baos.toByteArray();
				str = Base64.encodeToString(bmpBytes, Base64.DEFAULT);
				Log.e("PhotoUtil---bitmapToBase64String", "---bmpBytes--"
						+ bmpBytes.length / 1024 + "k");

				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (baos != null) {

					try {
						baos.flush();
						baos.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				str = null;
			}
		}

		return str;
	}

	/**
	 * base64 to bitmap
	 */
	public static Bitmap base64StringToBitmap(String base64Str) {
		Bitmap bitmap = null;
		if (base64Str == null || base64Str.equals("")) {
			return null;
		}

		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(base64Str, Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	/**
	 * 图片圆角处理
	 */
	private static Bitmap getRoundBitmap(Bitmap bmp) {
		Bitmap bgBitmap = null;
		bgBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Config.ARGB_8888); // 创建新位�?
		Canvas canvas = new Canvas(bgBitmap); // 创建的位图作为画�?
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		RectF rectF = new RectF(rect);
		float roundPx = 20; // 设置圆角半径
		paint.setAntiAlias(true);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 绘制圆角矩形
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置图像叠加模式
		canvas.drawBitmap(bmp, rect, rect, paint); // 绘制图像
		return bgBitmap;
	}

	/**
	 * rotate photo
	 */
	private static Bitmap rotatePhoto(Bitmap bmp, String path) {
		int degree = getPhotoDegree(path);
		if (degree == 0) {
			return bmp;
		}
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap resizedBmp = null;
		try {
			resizedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}

		if (resizedBmp == null) {
			return bmp;
		} else {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
			}
			bmp = null;

			return resizedBmp;
		}
	}

	/**
	 * get photo degree
	 */
	private static int getPhotoDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return degree;
	}

	

	/***
	 * 图片缩放
	 * 
	 * @param bgimage
	 *            源图片资�?
	 * @param newWidth
	 *            缩放后宽�?
	 * @param newHeight
	 *            缩放后高�?
	 * @return Bitmap
	 */
	public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
			double newHeight) {

		// 获取这个图片的宽和高
		float width = bgimage.getWidth();
		float height = bgimage.getHeight();

		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();

		// 计算宽高缩放�?
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// 缩放图片动作
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
				(int) height, matrix, true);

		return bitmap;

	}



}
