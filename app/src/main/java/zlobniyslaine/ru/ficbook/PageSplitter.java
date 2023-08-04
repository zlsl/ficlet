package zlobniyslaine.ru.ficbook;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

class PageSplitter {
    private final int pageWidth;
    private final int pageHeight;
    private final float lineSpacingMultiplier;
    private final int lineSpacingExtra;
    private final List<CharSequence> pages = new ArrayList<>();
    private SpannableStringBuilder currentLine = new SpannableStringBuilder();
    private SpannableStringBuilder currentPage = new SpannableStringBuilder();
    private int currentLineHeight;
    private int pageContentHeight;
    private int currentLineWidth;
    private int textLineHeight;

    private OnPaginatorListener onPaginatorListener;


    public interface OnPaginatorListener {
        void onPaginator(Integer progress);
    }

    public void setOnPaginatorListener(OnPaginatorListener listener) {
        onPaginatorListener = listener;
    }

    PageSplitter(int pageWidth, int pageHeight) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.lineSpacingMultiplier = 1f;
        this.lineSpacingExtra = 0;
    }

    public void append(String text, TextPaint textPaint) {
        textPaint.setTypeface(Typeface.create(textPaint.getTypeface(), Typeface.BOLD));
        textLineHeight = (int) Math.ceil(textPaint.getFontMetrics(null) * lineSpacingMultiplier + lineSpacingExtra);

        String[] paragraphs = text.split("\n", -1);
        int i, lp;
        lp = 0;

        for (i = 0; i < paragraphs.length - 1; i++) {
            appendText(paragraphs[i], textPaint);
            appendNewLine();
            if ((i * 100) / paragraphs.length != lp) {
                lp = (i * 100) / paragraphs.length;
                if (onPaginatorListener != null) {
                    onPaginatorListener.onPaginator(lp);
                }
            }
        }
        if (onPaginatorListener != null) {
            onPaginatorListener.onPaginator(100);
        }
        appendText(paragraphs[i], textPaint);
    }

    private void appendText(String text, TextPaint textPaint) {
        String[] words = text.split(" ", -1);
        int i;
        for (i = 0; i < words.length - 1; i++) {
            appendWord(words[i] + " ", textPaint);
        }
        appendWord(words[i], textPaint);
    }

    private void appendNewLine() {
        currentLine.append("\n");
        checkForPageEnd();
        appendLineToPage(textLineHeight);
    }

    private void checkForPageEnd() {
        if (pageContentHeight + currentLineHeight > pageHeight) {
            pages.add(currentPage);
            currentPage = new SpannableStringBuilder();
            pageContentHeight = 0;
        }
    }

    private void appendWord(String appendedText, TextPaint textPaint) {
        int textWidth = (int) Math.ceil(textPaint.measureText(appendedText));

        if (currentLineWidth + textWidth >= pageWidth) {
            checkForPageEnd();
            appendLineToPage(textLineHeight);
        }
        appendTextToLine(appendedText, textPaint, textWidth);
    }

    private void appendLineToPage(int textLineHeight) {
        currentPage.append(currentLine);
        pageContentHeight += currentLineHeight;

        currentLine = new SpannableStringBuilder();
        currentLineHeight = textLineHeight;
        currentLineWidth = 0;
    }

    private void appendTextToLine(String appendedText, TextPaint textPaint, int textWidth) {
        currentLineHeight = Math.max(currentLineHeight, textLineHeight);
        currentLine.append(renderToSpannable(appendedText, textPaint));
        currentLineWidth += textWidth;
    }

// --Commented out by Inspection START (16.07.20 22:56):
//    public Integer getPagesCount() {
//        return pages.size();
//    }
// --Commented out by Inspection STOP (16.07.20 22:56)


    public List<CharSequence> getPages() {
        ArrayList<CharSequence> copyPages = new ArrayList<>(pages);
        SpannableStringBuilder lastPage = new SpannableStringBuilder(currentPage);
        if (pageContentHeight + currentLineHeight > pageHeight) {
            copyPages.add(lastPage);
            lastPage = new SpannableStringBuilder();
        }
        lastPage.append(currentLine);
        copyPages.add(lastPage);
        return copyPages;
    }

    private SpannableString renderToSpannable(String text, TextPaint textPaint) {
        SpannableString spannable = new SpannableString(text);

        if (textPaint.isFakeBoldText()) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), 0);
        }
        return spannable;
    }

    public void savePages(Context context, String id) {
        try {
            List<CharSequence> spages = getPages();
            for (CharSequence s : pages) {
                if (s.length() > 0) {
                    spages.add(s.toString());
                }
            }
            FileOutputStream fos = context.openFileOutput(id + ".pagesz", Context.MODE_PRIVATE);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(spages);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            Log.d("FPAGE_CACHE", e.toString());
        }
    }
}