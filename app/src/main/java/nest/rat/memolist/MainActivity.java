package nest.rat.memolist;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends ListActivity implements
        EditEntryDialog.EditEntryDialogListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    private DB db;
    private MemoCursorAdapter mAdapter;
    private final static String TAG = "MemoListMain";
    private MemoListShare mMemoListShare;

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

        ListView lvMain = getListView();
        lvMain.setOnItemClickListener(this);
        registerForContextMenu(lvMain);
        getLoaderManager().initLoader(0, null, this);

        mMemoListShare = new MemoListShare(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuAddItem:
                showEditEntryDialog();
                break;
            case R.id.mnuUploadItem:
                mMemoListShare.setListData(db.getMemoListXml());
                mMemoListShare.connect();
                Toast.makeText(this, "mnuUploadItem", Toast.LENGTH_SHORT).show();
                break;
        }

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
                showEditEntryDialog(info.id, cursor.getString(cursor.getColumnIndexOrThrow(DB.MEMO_TABLE_COL_NAME_NAME)));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MemoListShare.REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mMemoListShare.connect();
                }
                break;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        db.switchMemoState(id, mAdapter.getCursor());
        getLoaderManager().getLoader(0).forceLoad();
    }

    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}