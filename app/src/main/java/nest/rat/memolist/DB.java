package nest.rat.memolist;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;

/**
 * Created by mollyjester on 10.01.2017.
 */

public class DB {
    final static int DB_VER = 3;
    final static String DB_NAME = "MemoListDB.db";
    final static String MEMO_TABLE = "Memos";
    final static String MEMO_TABLE_COL_ID_NAME = "_id";
    final static String MEMO_TABLE_COL_NAME_NAME = "MemoText";
    final static String MEMO_TABLE_COL_STATE_NAME = "State";
    final static String CREATE_MEMO_TABLE = "CREATE TABLE " + MEMO_TABLE + " ("
            + MEMO_TABLE_COL_ID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + MEMO_TABLE_COL_NAME_NAME + " TEXT, "
            + MEMO_TABLE_COL_STATE_NAME + " INTEGER DEFAULT 0);";
    final static String DROP_MEMO_TABLE = "DROP TABLE IF EXISTS " + MEMO_TABLE;
    final static String TAG = "DB";

    static class MemoState {
        final static int MEMO_STATE_NONE = 0;
        final static int MEMO_STATE_CHECKED = 1;

        public static int switchState(int state) {
            switch (state) {
                case MEMO_STATE_NONE:
                    return MEMO_STATE_CHECKED;
                case MEMO_STATE_CHECKED:
                    return MEMO_STATE_NONE;
            }

            return MEMO_STATE_NONE;
        }
    }

    private Context mCtx;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VER);
        mDB = mDBHelper.getWritableDatabase();
    }

    public void close() {
        if (mDBHelper!=null) {
            mDBHelper.close();
        }
    }

    public Cursor getAllMemos() {
        return mDB.query(MEMO_TABLE, null, null, null, null, null, null);
    }

    public void addMemo(String txt) {
        ContentValues cv = new ContentValues();
        cv.put(MEMO_TABLE_COL_NAME_NAME, txt);
        mDB.insert(MEMO_TABLE, null, cv);
    }

    public void deleteMemo(long id) {
        mDB.delete(MEMO_TABLE, MEMO_TABLE_COL_ID_NAME + " = " + id, null);
    }

    public void updateMemo(long id, String[] columns, String[] values) {
        if (columns.length == values.length) {
            ContentValues cv = new ContentValues();
            int index = 0;

            for (String col : columns) {
                cv.put(col, values[index]);
                index++;
            }
            mDB.update(MEMO_TABLE, cv, MEMO_TABLE_COL_ID_NAME + " = " + id, null);
        }
    }

    public void switchMemoState(long id, Cursor cursor) {
        String[] columns = {MEMO_TABLE_COL_STATE_NAME};
        String[] values = {Integer.toString(MemoState.switchState(
                cursor.getInt(cursor.getColumnIndexOrThrow(MEMO_TABLE_COL_STATE_NAME))))};
        updateMemo(id, columns, values);
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_MEMO_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_MEMO_TABLE);
            onCreate(db);
        }
    }
}
