package com.janlent.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.janlent.utils.Log;

/**
 * 操作数据库
 * 
 * @author Administrator
 * 
 */
public class DataBaseHelper extends SQLiteOpenHelper {
	private static String TAG = "TAG";
	private static String DB_PATH = "/data/data/YOUR_PACKAGE/databases/";
	private static String DB_NAME = "Sodex.sqlite";
	private SQLiteDatabase mDataBase;
	private final Context mContext;

	public DataBaseHelper(Context context) {
		super(context, DB_NAME, null, 1);
		DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
		this.mContext = context;
	}

	/**
	 * 创建数据库
	 * 
	 * @throws IOException
	 */
	public void createDataBase() throws IOException {
		boolean mDataBaseExist = checkDataBase();
		if (!mDataBaseExist) {
			this.getReadableDatabase();
			try {
				copyDataBase();
			} catch (IOException mIOException) {
				throw new Error("ErrorCopyingDataBase");
			}
		}
	}

	/**
	 * 检测数据库是否存在
	 * 
	 * @return
	 */
	private boolean checkDataBase() {
		SQLiteDatabase mCheckDataBase = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			mCheckDataBase = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		} catch (SQLiteException mSQLiteException) {
			Log.e("DatabaseNotFound " + mSQLiteException.toString());
		}
		if (mCheckDataBase != null) {
			mCheckDataBase.close();
		}
		return mCheckDataBase != null;
	}


	/**
	 * 复制数据库
	 * 
	 * @throws IOException
	 */
	private void copyDataBase() throws IOException {
		InputStream mInput = mContext.getAssets().open(DB_NAME);
		String outFileName = "sdcard/database/Sodex.sqlite";
		OutputStream mOutput = new FileOutputStream(outFileName);
		byte[] mBuffer = new byte[1024];
		int mLength;
		while ((mLength = mInput.read(mBuffer)) > 0) {
			mOutput.write(mBuffer, 0, mLength);
		}
		Log.e("数据库复制成功！");
		mOutput.flush();
		mOutput.close();
		mInput.close();
	}

	/**
	 * 是否打开数据库
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean openDataBase() throws SQLException {
		String mPath = DB_PATH + DB_NAME;
		mDataBase = SQLiteDatabase.openDatabase(mPath, null,
				SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		Log.e("数据库打开成功！");
		return mDataBase != null;
	}

	@Override
	public synchronized void close() {
		if (mDataBase != null)
			mDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("UpgradingDatabase, This will drop current database and will recreate it");
	}
}
