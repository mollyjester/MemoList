package nest.rat.memolist;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by slikharev on 28.12.2016.
 */

public class TextViewEx extends TextView {

    private MemoListEntry entry;

    public TextViewEx(Context context, MemoListEntry _entry) {
        super(context);

        entry = _entry;

        setText(entry.getNAME());
        setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setTextSize(24);

        affectState();
    }

    public MemoListEntry getEntry() {
        return entry;
    }

    public void setEntry(MemoListEntry _entry) {
        entry = _entry;
    }

    public void affectState() {
        switch (entry.getSTATE())
        {
            case NONE:
                setPaintFlags(getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                break;
            case MARKED:
                setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                break;
        }
    }
}
