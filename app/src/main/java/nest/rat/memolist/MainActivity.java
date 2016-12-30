package nest.rat.memolist;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private ListView lvMain;
    private MemoListDBHelper dbHelper;
    private static String TAG = "MemoListMain";
    private ArrayAdapter<MemoListEntry> entryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lvMain = (ListView) findViewById(R.id.lvMain);
        registerForContextMenu(lvMain);
        dbHelper = new MemoListDBHelper(getApplicationContext());
        entryAdapter = new ArrayAdapter<MemoListEntry>(
                this, android.R.layout.simple_list_item_1, dbHelper.listEntries());
        lvMain.setAdapter(entryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MemoListEntry entry = new MemoListEntry(dbHelper);
        entry.setNAME("Новая запись");
        entry.save();
        entryAdapter.add(entry);
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
        //TextViewEx tvEx = (TextViewEx) llMain.getChildAt(info.position);

        Log.d(TAG, item.getMenuInfo().toString());

        switch (item.getItemId()) {
            case R.id.entryEditMenu:
                Log.d(TAG, "Edit");
                return true;
            case R.id.entryDelMenu:
                //dbHelper.deleteEntry(tvEx.getEntry());
                //buildItemList();
                Log.d(TAG, "Delete");
                return true;
            default:
                return super.onContextItemSelected(item);
         }
    }

    @Override
    public void onClick(View v) {
        TextViewEx tvItem = (TextViewEx) v;
        MemoListEntry entry = tvItem.getEntry();
        entry.switchState();
        tvItem.getEntry().save();
        tvItem.affectState();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        dbHelper.close();
    }
}