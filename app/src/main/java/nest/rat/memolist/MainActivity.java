package nest.rat.memolist;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends Activity implements EditEntryDialog.EditEntryDialogListener {

    private ListView lvMain;
    private MemoListDBHelper dbHelper;
    private ArrayAdapter<MemoListEntry> entryAdapter;
    private ArrayList<MemoListEntry> entryList;

    private static String TAG = "MemoListMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lvMain = (ListView) findViewById(R.id.lvMain);
        registerForContextMenu(lvMain);
        dbHelper = new MemoListDBHelper(getApplicationContext());
        entryList = dbHelper.listEntries();
        entryAdapter = new ArrayAdapter<MemoListEntry>(
                this, android.R.layout.simple_list_item_1, entryList);
        lvMain.setAdapter(entryAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showEditEntryDialog(null);
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
        MemoListEntry entry = entryAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.entryEditMenu:
                showEditEntryDialog(entry);
                return true;
            case R.id.entryDelMenu:
                dbHelper.deleteEntry(entry);
                entryList.remove(entry);
                entryAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
         }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dbHelper.close();
    }

    @Override
    public void onDialogPositiveClick(MemoListEntry _entry, String entryText) {
        if (_entry == null) {
            MemoListEntry entry = new MemoListEntry(dbHelper);
            entry.setNAME(entryText);
            entry.save();
            entryList.add(entry);
        }
        else {
            _entry.setNAME(entryText);
            _entry.save();
        }

        entryAdapter.notifyDataSetChanged();
    }

    private void showEditEntryDialog(MemoListEntry entry) {
        EditEntryDialog dialog = new EditEntryDialog();
        dialog.setEntry(entry);
        dialog.show(getFragmentManager(), EditEntryDialog.TAG);
    }
}