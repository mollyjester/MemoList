package nest.rat.memolist;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * Created by mollyjester on 10.01.2017.
 */

public class DB {
    final static int DB_VER = 2;
    final static String DB_NAME = "MemoListDB.db";
    final static String ITEM_TABLE = "ListItems";
    final static String ITEM_TABLE_COL_ID_NAME = "_id";
    final static String ITEM_TABLE_COL_NAME_NAME = "ItemText";
    final static String ITEM_TABLE_COL_STATE_NAME = "State";
    final static String[] ITEM_TABLE_COLUMNS = {ITEM_TABLE_COL_ID_NAME,
            ITEM_TABLE_COL_NAME_NAME,
            ITEM_TABLE_COL_STATE_NAME};
    final static int ITEM_TABLE_COL_ID = 0;
    final static int ITEM_TABLE_COL_ITEMTEXT = 1;
    final static int ITEM_TABLE_COL_STATE = 2;
    final static String CREATE_TABLE = "CREATE TABLE " + ITEM_TABLE + " ("
            + ITEM_TABLE_COL_ID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ITEM_TABLE_COL_NAME_NAME + " TEXT, "
            + ITEM_TABLE_COL_STATE_NAME + " INTEGER);";
    final static String DROP_TABLE = "DROP TABLE IF EXISTS " + ITEM_TABLE;
    final static String TAG = "DB";

    private final Context mCtx;
    private DBHelper mDBHelper;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE);
            onCreate(db);
        }

        private void closeDB(SQLiteDatabase db) {
            if (db.isOpen()) {
                db.close();
            }
        }
    }
}
