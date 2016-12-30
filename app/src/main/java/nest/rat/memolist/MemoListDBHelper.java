package nest.rat.memolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Paint;
import android.util.Log;

import java.util.Locale;

import static nest.rat.memolist.MemoListEntryState.MARKED;
import static nest.rat.memolist.MemoListEntryState.NONE;

/**
 * Created by SLikharev on 29.12.2016.
 */

public class MemoListDBHelper extends SQLiteOpenHelper {
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
    final static String TAG = "MemoListDBHelper";

    private void closeDB(SQLiteDatabase db) {
        if (db.isOpen()) {
            db.close();
        }
    }

    public MemoListDBHelper(Context context) {
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

    public int entryCount() {
        int ret = 0;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cur = db.rawQuery("select count(1) from " + ITEM_TABLE, null);

        if (cur.moveToFirst()) {
            ret = cur.getInt(0);
        }

        cur.close();
        closeDB(db);
        return ret;
    }

    public MemoListEntry[] listEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(ITEM_TABLE, ITEM_TABLE_COLUMNS, "", null, "", "", "");

        cursor.moveToFirst();

        MemoListEntry[] entryArr = new MemoListEntry[cursor.getCount()];
        int i = 0;

        while (!cursor.isAfterLast()){
            MemoListEntry entry = new MemoListEntry(this);
            entry.set_ID(cursor.getInt(ITEM_TABLE_COL_ID));
            entry.setNAME(cursor.getString(ITEM_TABLE_COL_ITEMTEXT));
            entry.setSTATE(MemoListEntryState.values()[cursor.getInt(ITEM_TABLE_COL_STATE)]);
            entryArr[i] = entry;
            cursor.moveToNext();

            i++;
        }

        cursor.close();
        closeDB(db);
        
        return entryArr;
    }

    /**
     * Creates a new item in DB
     * @param entry A MemoListEntry object contains all needed data
     * @return ID of a newly created entry
     */
    public void createEntry(MemoListEntry entry) {
        ContentValues cv = new ContentValues();
        cv.put(ITEM_TABLE_COL_NAME_NAME, entry.getNAME());
        cv.put(ITEM_TABLE_COL_STATE_NAME, entry.getSTATE().ordinal());

        SQLiteDatabase db = this.getWritableDatabase();

        entry.set_ID((int) db.insert(ITEM_TABLE, null, cv));

        if (entry.get_ID() == -1){
            Log.e(TAG, "Ошибка при вставке элемента списка в БД!");
        }

        closeDB(db);
    }

    /**
     * Deletes the given entry from DB
     * @param entry A MemoListEntry object contains all needed data
     */
    public void deleteEntry(MemoListEntry entry) {
        String where = ITEM_TABLE_COL_ID_NAME + " = ?";
        String[] whereArgs = {String.valueOf(entry.get_ID())};
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEM_TABLE, where, whereArgs);
        closeDB(db);
    }

    /**
     * Updates the given entry in DB
     * @param entry A MemoListEntry object contains all needed data
     */
    public void updateEntry(MemoListEntry entry){
        ContentValues cv = new ContentValues();
        cv.put(ITEM_TABLE_COL_NAME_NAME, entry.getNAME());
        cv.put(ITEM_TABLE_COL_STATE_NAME, entry.getSTATE().ordinal());

        String where = ITEM_TABLE_COL_ID_NAME + " = ?";
        String[] whereArgs = {String.valueOf(entry.get_ID())};

        SQLiteDatabase db = this.getWritableDatabase();
        db.update(ITEM_TABLE, cv, where, whereArgs);
        closeDB(db);
    }
}

enum MemoListEntryState {NONE, MARKED}

class MemoListEntry {
    protected String TABLE_NAME;
    protected int _ID;
    protected String NAME;
    protected MemoListEntryState STATE;
    private MemoListDBHelper dbHelper;

    public MemoListEntry(MemoListDBHelper _dbHelper) {
        super();
        dbHelper = _dbHelper;
        STATE = NONE;
    }
    public String getTABLE_NAME() {
        return TABLE_NAME;
    }
    public void setTABLE_NAME(String table_name) {
        TABLE_NAME = table_name;
    }
    public int get_ID(){
        return _ID;
    }
    public void set_ID(int _id){
        _ID = _id;
    }
    public String getNAME() {
        return NAME;
    }
    public void setNAME(String name){
        NAME = name;
    }
    public MemoListEntryState getSTATE(){
        return STATE;
    }
    public void setSTATE(MemoListEntryState state){
        STATE = state;
    }

    public void save(){
        if (_ID != 0) {
            dbHelper.updateEntry(this);
        }
        else {
            dbHelper.createEntry(this);
        }
    }

    public void switchState() {
        switch (STATE)
        {
            case NONE:
                STATE = MemoListEntryState.MARKED;
                break;
            case MARKED:
                STATE = MemoListEntryState.NONE;
                break;
        }
    }

    @Override
    public String toString() {
        return String.format(new Locale("ru"), "object MemoListEntry: _ID %d, NAME: %s, STATE: %s", _ID, NAME, STATE.toString());
    }
}
