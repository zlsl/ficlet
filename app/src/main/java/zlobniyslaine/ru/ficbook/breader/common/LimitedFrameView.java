package zlobniyslaine.ru.ficbook.breader.common;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;

public class LimitedFrameView extends FrameLayout {
    private final int m_heightLimit;
    private final int m_widthLimit;

// --Commented out by Inspection START (16.07.20 22:49):
//    public LimitedFrameView(Context context, int widthLimit, int heightLimit) {
//        super(context);
//        m_heightLimit = heightLimit;
//        m_widthLimit = widthLimit;
//        LayoutParams params = new LayoutParams(widthLimit, heightLimit);
//        params.gravity = Gravity.CENTER;
//        setLayoutParams(params);
//    }
// --Commented out by Inspection STOP (16.07.20 22:49)

    public LimitedFrameView(Context context) {
        super(context);
        m_heightLimit = 0;
        m_widthLimit = 0;
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        setLayoutParams(params);
    }

// --Commented out by Inspection START (16.07.20 22:49):
//    public void setSize(int widthLimit, int heightLimit) {
//        m_heightLimit = heightLimit;
//        m_widthLimit = widthLimit;
//        LayoutParams params = new LayoutParams(widthLimit, heightLimit);
//        params.gravity = Gravity.CENTER;
//        setLayoutParams(params);
//    }
// --Commented out by Inspection STOP (16.07.20 22:49)

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (m_widthLimit > 0 && w > m_widthLimit)
            w = m_widthLimit;

        if (m_heightLimit > 0 && h > m_heightLimit)
            h = m_heightLimit;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        if (m_widthLimit > 0 && w > m_widthLimit)
            w = m_widthLimit;

        if (m_heightLimit > 0 && h > m_heightLimit)
            h = m_heightLimit;

        setMeasuredDimension(w, h);
    }
}
