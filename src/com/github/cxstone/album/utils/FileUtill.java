package com.github.cxstone.album.utils;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

/**
 * 文件工具�?
 * 
 * @author liujian
 * 
 */
public class FileUtill {

	/**
	 * 判断SD卡是否存�?
	 * 
	 * @return
	 */
	public static boolean sdCardIsExit() {
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}



	/**
	 * 获取SD卡路�?
	 * 
	 * @return /sdcard/
	 */
	public static String getSDCardPath() {
		if (sdCardIsExit()) {
			return Environment.getExternalStorageDirectory().toString() + "/";
		}
		return null;
	}

	/**
	 * 创建文件�?
	 * 
	 * @param dirPath
	 */
	public static String creatDir2SDCard(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists()) {// 判断文件是否存在
			file.mkdirs();
		}
		return dirPath;
	}

	/**
	 * 创建文件
	 * 
	 * 如果�?/sdcard/download/123.doc则只�?传入filePath=download/123.doc
	 * 
	 * @param filePath
	 *            文件路径
	 * @return 创建文件的路�?
	 * @throws IOException
	 */
	public static String creatFile2SDCard(String filePath) throws IOException {
		// 无论传入�?么�?? 都是从根目录�?�? �?/sdcard/+filePath
		// 创建文件路径包含的文件夹
		String filedir = creatDir2SDCard(getFileDir(filePath));
		String fileFinalPath = filedir + getFileName(filePath);
		File file = new File(fileFinalPath);
		if (!file.exists()) {
			file.createNewFile();
		}
		return fileFinalPath;
	}

	/**
	 * 获取文件目录路径
	 * 
	 * @param filePath
	 * @return
	 */
	private static String getFileDir(String filePath) {
		if (filePath.startsWith(getSDCardPath())) {
			return filePath.replace(getFileName(filePath), "");
		}
		return getSDCardPath() + filePath.replace(getFileName(filePath), "");
	}

	/**
	 * 获取文件�?
	 * 
	 * @param filePath
	 * @return
	 */
	private static String getFileName(String filePath) {
		int index = 0;
		String tempName = "";
		if ((index = filePath.lastIndexOf("/")) != -1) {
			// 如果有后�?名才
			tempName = filePath.substring(index + 1);
		}
		return tempName.contains(".") ? tempName : "";
	}

	
	public static boolean delDir(File f) {
		try {
			if (f.exists() && f.isDirectory()) {// 判断是文件还是目�?
				if (f.listFiles().length == 0) {// 若目录下没有文件则直接删�?
					f.delete();
				} else {// 若有则把文件放进数组，并判断是否有下级目�?
					File delFile[] = f.listFiles();
					int i = f.listFiles().length;
					for (int j = 0; j < i; j++) {
						if (delFile[j].isDirectory()) {
							delDir(delFile[j]);// 递归调用del方法并取得子目录路径
						}
						delFile[j].delete();
					}// 删除文件
					f.delete();
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}  
}
