package com.janlent.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

/**
 * 实现数据库备份和还原的异步任务
 * 
 * @author Administrator
 * 
 */
public class BackupDBTask extends AsyncTask<String, Void, Integer> {

	public static final String COMMAND_BACKUP = "backupDatabase";
	public static final String COMMAND_RESTORE = "restroeDatabase";
	public Context mContext;
	private Handler handler;
	public String dataPath;

	public BackupDBTask(Context context, Handler handler, String path) {
		this.mContext = context;
		dataPath = path;
		this.handler = handler;
	}

	@Override
	protected Integer doInBackground(String... params) {

		File dbFile = mContext.getDatabasePath(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/Sodexo/database/Sodexo.sqlite");

		File exportDir = new File("/sdcard/Sodexo/", "DBbackup");
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		File backup = new File(exportDir, "Sodexo"
				+ ConvertUtils.date2Str("yyyyMMddHHmmss") + ".sqlite");

		String command = params[0];

		if (command.equals(COMMAND_BACKUP)) {
			try {
				// 备份数据
				backup.createNewFile();
				fileCopy(dbFile, backup);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
			// 数据还原
		} else if (command.equals(COMMAND_RESTORE)) {
			try {
				File filerestore = new File("/sdcard/Sodexo/DBbackup/"
						+ dataPath);
				fileCopy(filerestore, dbFile);
				return 1;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (result != 1) {
			Toast.makeText(mContext, "数据操作失败", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 单个文件的复制
	 * 
	 * @param dbFile
	 *            源文件
	 * @param backup
	 *            复制文件
	 * @throws IOException
	 *             IO流异常
	 */
	private void fileCopy(File dbFile, File backup) throws IOException {
		FileChannel inChannel = new FileInputStream(dbFile).getChannel();
		FileChannel outChannel = new FileOutputStream(backup).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
	}
}