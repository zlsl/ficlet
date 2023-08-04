package zlobniyslaine.ru.ficbook.controls;

import android.content.Context;
import android.util.AttributeSet;

import zlobniyslaine.ru.ficbook.R;

public class CheckBoxTriStates extends androidx.appcompat.widget.AppCompatCheckBox {
    static public final int MID = -1;
    static public final int DISLIKE = 0;
    static public final int LIKE = 1;
    private int state;

    public CheckBoxTriStates(Context context) {
        super(context);
        init();
    }

    public CheckBoxTriStates(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckBoxTriStates(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        state = MID;
        updateBtn();

        setOnCheckedChangeListener((buttonView, isChecked) -> {
            switch (state) {
                case MID:
                    state = LIKE;
                    break;
                case LIKE:
                    state = DISLIKE;
                    break;
                case DISLIKE:
                    state = MID;
                    break;
            }
            updateBtn();
        });
    }

    private void updateBtn() {
        int btnDrawable = R.drawable.tri_neutral;
        switch (state) {
            case MID:
                btnDrawable = R.drawable.tri_neutral;
                break;
            case LIKE:
                btnDrawable = R.drawable.tri_like;
                break;
            case DISLIKE:
                btnDrawable = R.drawable.tri_dislike;
                break;
        }
        setButtonDrawable(btnDrawable);
    }

// --Commented out by Inspection START (16.07.20 22:26):
//    public int getState() {
//        return state;
//    }
// --Commented out by Inspection STOP (16.07.20 22:26)

// --Commented out by Inspection START (16.07.20 22:26):
//    public void setState(int state) {
//        this.state = state;
//        updateBtn();
//    }
// --Commented out by Inspection STOP (16.07.20 22:26)
}