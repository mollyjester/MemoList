package nest.rat.memolist;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;


public class MainActivity extends ListActivity implements
        EditEntryDialog.EditEntryDialogListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private DB db;
    private MemoCursorAdapter mAdapter;
    private final static String TAG = "MemoListMain";

    static class MemoCursorLoader extends CursorLoader {

        DB db;

        public MemoCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            return db.getAllMemos();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DB(this);
        db.open();

        mAdapter = new MemoCursorAdapter(this, null, 0);
        setListAdapter(mAdapter);

        registerForContextMenu(getListView());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showEditEntryDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.entry_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case R.id.entryEditMenu:
                Cursor cursor = mAdapter.getCursor();
                showEditEntryDialog(info.id, cursor.getString(cursor.getColumnIndex(DB.MEMO_TABLE_COL_NAME_NAME)));
                return true;
            case R.id.entryDelMenu:
                db.deleteMemo(info.id);
                getLoaderManager().getLoader(0).forceLoad();
                return true;
            default:
                return super.onContextItemSelected(item);
         }
    }

    @Override
    public void onDialogPositiveClick(long id, String txt) {
        String[] columns = {DB.MEMO_TABLE_COL_NAME_NAME};
        String[] values = {txt};

        if (id == 0) {
            db.addMemo(txt);
        } else {
            db.updateMemo(id, columns, values);
        }

        getLoaderManager().getLoader(0).forceLoad();
    }

    private void showEditEntryDialog(long id, String txt) {
        EditEntryDialog dialog = new EditEntryDialog();
        dialog.setItemId(id);
        dialog.setItemTxt(txt);
        dialog.show(getFragmentManager(), EditEntryDialog.TAG);
    }

    private void showEditEntryDialog() {
        showEditEntryDialog(0, "");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MemoCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}