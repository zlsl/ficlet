package zlobniyslaine.ru.ficbook.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ScrollViewWithOnStopListener extends ScrollView {

    OnScrollStopListener listener;

    public interface OnScrollStopListener {
        void onScrollStopped(int y);
    }

    public ScrollViewWithOnStopListener(Context context) {
        super(context);
    }

    public ScrollViewWithOnStopListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            checkIfScrollStopped();
        }

        return super.onTouchEvent(ev);
    }

    int initialY = 0;

    private void checkIfScrollStopped() {
        initialY = getScrollY();
        this.postDelayed(() -> {
            int updatedY = getScrollY();
            if (updatedY == initialY) {
                //we've stopped
                if (listener != null) {
                    listener.onScrollStopped(getScrollY());
                }
            } else {
                initialY = updatedY;
                checkIfScrollStopped();
            }
        }, 50);
    }

    public void setOnScrollStoppedListener(OnScrollStopListener yListener) {
        listener = yListener;
    }
}