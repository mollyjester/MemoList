package nest.rat.memolist;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends Activity implements View.OnClickListener {

    private LinearLayout llMain;
    private MemoListDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        llMain = (LinearLayout) findViewById(R.id.llMain);
        dbHelper = new MemoListDBHelper(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (llMain.getChildCount() != dbHelper.entryCount()){
            buildItemList();
        }
    }

    private void buildItemList() {
        llMain.removeAllViews();

        MemoListEntry[] listEntries = dbHelper.listEntries();

        for (MemoListEntry entry : listEntries) {
            drawEntry(entry);
        }
    }

    private void drawEntry(MemoListEntry entry) {
        TextViewEx tvNew = new TextViewEx(this, entry);
        tvNew.setOnClickListener(this);
        llMain.addView(tvNew);
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

        drawEntry(entry);
        return super.onOptionsItemSelected(item);
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