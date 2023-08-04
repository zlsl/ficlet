package zlobniyslaine.ru.ficbook.breader.bookRenderer;

import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.TextPaint;

import zlobniyslaine.ru.ficbook.breader.common.FixedCharSequence;

public class FontStyle {

    private static float[] s_width = new float[32];

    private final TextPaint m_paint;

    private final float m_heightMod;
    private final float m_height;
    private final float m_dashWidth;
    private float m_firstLine;

    private final int m_dashWidthInt;
    private final int m_spaceWidthInt;
    private final int m_wordSpaceWidthInt;
    private final int m_heightInt;
    private final int m_heightModInt;
    private int m_firstLineInt;
    private final boolean m_hasTab;

    private final char m_spaceChar;

    public TextPaint getPaint() {
        return m_paint;
    }

    public float getHeightMod() {
        return m_heightMod;
    }

    public float getHeight() {
        return m_height;
    }

    public int getHeightModInt() {
        return m_heightModInt;
    }

    public int getHeightInt() {
        return m_heightInt;
    }

    public float getDashWidth() {
        return m_dashWidth;
    }

    public int getDashWidthInt() {
        return m_dashWidthInt;
    }

    public int getSpaceWidthInt() {
        return m_spaceWidthInt;
    }

    public float getFirstLine() {
        return m_firstLine;
    }

    public int getFirstLineInt() {
        return m_firstLineInt;
    }

    public int getWordSpaceWidthInt() {
        return m_wordSpaceWidthInt;
    }

    public char getSpaceChar() {
        return m_spaceChar;
    }

    public boolean hasTab() {
        return m_hasTab;
    }

    public void setFirstLine(float value) {
        m_firstLine = value;
        m_firstLineInt = (int) (value * 16);
    }

    public FontStyle(TextPaint paint, float lineSpace, float firstLine) {
        m_paint = paint;
        m_firstLine = firstLine;

        m_height = paint.getTextSize();

        m_heightMod = (lineSpace - 0.75f) * paint.getTextSize() * 0.5f;
        float m_wordSpaceWidth = measureChar(' ');

        Rect spaceBounds = new Rect();
        m_paint.getTextBounds(new char[]{' '}, 0, 1, spaceBounds);

        float spaceWidth = spaceBounds.width();
        float m_spaceWidth;
        if (spaceBounds.height() > paint.getTextSize() / 4 || spaceWidth == 0) {
            m_spaceWidth = m_wordSpaceWidth;
            m_spaceChar = ' ';
        } else {
            m_spaceWidth = spaceWidth;
            m_spaceChar = ' ';
        }

        m_dashWidth = measureChar('-');

        Rect tabBounds = new Rect();
        m_paint.getTextBounds(new char[]{'\t'}, 0, 1, tabBounds);

        m_hasTab = tabBounds.height() < paint.getTextSize() / 4;

        m_heightModInt = (int) (m_heightMod * 16);
        m_spaceWidthInt = (int) (m_spaceWidth * 16);
        m_wordSpaceWidthInt = (int) (m_wordSpaceWidth * 16);
        m_dashWidthInt = (int) (m_dashWidth * 16);
        m_heightInt = (int) (m_height * 16);
        m_firstLineInt = (int) (firstLine * 16);
    }

    public void changeContrast(float extraStroke) {
        if (extraStroke > 0) {
            m_paint.setStrokeWidth(extraStroke);
            m_paint.setStyle(Style.FILL_AND_STROKE);
        } else
            m_paint.setStyle(Style.FILL);
    }

    public void drawText(Canvas canvas, FixedCharSequence text, float posx, float posy) {
        char[] chars = text.getChars();
        int offset = text.getOffset();
        int length = text.length();
		
        canvas.drawText(chars, offset, length, posx, posy, m_paint);
    }

    public float measureChar(char ch) {
        return m_paint.measureText(new char[]{ch}, 0, 1);
    }

    public float[] getTextWidths(CharSequence text) {
        if (text.length() > s_width.length)
            s_width = new float[text.length()];
        m_paint.getTextWidths(text, 0, text.length(), s_width);
        return s_width;
    }

    public float measureText(FixedCharSequence text) {
        return m_paint.measureText(text.getChars(), text.getOffset(), text.length());
    }

    public int measureTextInt(CharSequence text) {
        return (int) (m_paint.measureText(text, 0, text.length()) * 16);
    }

    public int measureTextInt(FixedCharSequence text) {
        return (int) (m_paint.measureText(text.getChars(), text.getOffset(), text.length()) * 16);
    }
}
