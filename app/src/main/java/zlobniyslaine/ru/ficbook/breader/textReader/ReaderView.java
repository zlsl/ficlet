package zlobniyslaine.ru.ficbook.breader.textReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.breader.bookParser.BaseBookReader;
import zlobniyslaine.ru.ficbook.breader.bookParser.BookLine;
import zlobniyslaine.ru.ficbook.breader.bookParser.ImageData;
import zlobniyslaine.ru.ficbook.breader.bookRenderer.FontStyle;
import zlobniyslaine.ru.ficbook.breader.bookRenderer.TextPage;
import zlobniyslaine.ru.ficbook.breader.common.Dips;
import zlobniyslaine.ru.ficbook.breader.common.FixedCharSequence;
import zlobniyslaine.ru.ficbook.breader.common.Pair;
import zlobniyslaine.ru.ficbook.breader.common.SortedList;

@SuppressLint("ViewConstructor")
public class ReaderView extends View {
    public static final int ORIENTATION_NORMAL = 0;
    public static final int ORIENTATION_CW = 1;
    public static final int ORIENTATION_CCW = 2;
    public static final int ORIENTATION_180 = 3;
    public static final int FOOTER_OFF = 0;
    public static final int FOOTER_TICKS = 2;
    private static final int HEADER_FONT_SIZE = Dips.spToPx(12);
    private static final String[] s_superscriptNumbers = {" ¹", " ²", " ³", " \u2074"};
    private static final int FOOTER_HEIGHT = 22;

    private final SortedList<Integer> m_averagePageSizes;
    private final int m_maxCachedPages;
    private final int m_footerSpace;
    private final boolean m_fullScreen;
    private final float m_dpiCompensation;
    private final DateFormat m_dateFormat;
    private BaseBookReader m_reader;
    private List<TextPage> m_pages;
    private int m_currentPage;
    private long m_bookStart;
    private int m_averagePages;
    private int m_averageTotal;
    private List<Pair<String, Float>> m_chapters;
    private List<ImageData> m_images;
    private Map<String, List<BookLine>> m_notes;
    private Map<String, Integer> m_styles;
    private FontStyle m_textPaint;
    private FontStyle m_superPaint;
    private FontStyle m_boldPaint;
    private FontStyle m_boldItalicPaint;
    private FontStyle m_italicPaint;
    private FontStyle m_header1Paint;
    private FontStyle m_header2Paint;
    private FontStyle m_header3Paint;
    private FontStyle m_header4Paint;
    private FontStyle m_subtitlePaint;
    private TextPaint m_systemPaint;
    private TextPaint m_pagePaint;
    private TextPaint m_headPaint;
    private Paint m_whitePaint;
    private Paint m_blackPaint;
    private Paint m_lightGrayPaint;
    private Paint m_darkGrayPaint;
    private Paint m_grayPaint;
    private Boolean end_of_book = false;
    private int int_current_page;
    private int int_total_page;
    private int m_pageNumber;
    private int m_actualWidth;
    private int m_actualHeight;
    private int m_paddingLeft;
    private int m_paddingRight;
    private int m_paddingTop;
    private int m_paddingBottom;
    private int m_header;
    private int m_footer;
    private int m_refreshMode;
    private int m_orientation = ORIENTATION_NORMAL;
    private boolean m_inverse = true;
    private int m_backColor;
    private int m_clockWidth;

    private CharSequence m_footerTextLeft;
    private int m_footerTextOffset;


    private ReaderView.OnPageRenderedListener onPageRenderedListener;


    public ReaderView(Context context, ViewGroup container, float dpiCompensation) {
        super(context);
        setFocusable(false);
        m_dpiCompensation = dpiCompensation;

        m_fullScreen = true;

        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        container.addView(this);

        m_actualWidth = getWidth();
        m_actualHeight = getHeight();

        m_maxCachedPages = 15;
        m_paddingLeft = 5;
        m_paddingRight = 5;
        m_paddingTop = 5;
        m_paddingBottom = 5;

        m_header = 64;
        m_clockWidth = 100;
        m_footerSpace = 3;

        m_dateFormat = android.text.format.DateFormat.getTimeFormat(getContext());

        m_footer = FOOTER_TICKS;

        m_pageNumber = 0;

        m_averagePageSizes = new SortedList<>();
    }

    public void setOnPageRenderedListener(OnPageRenderedListener listener) {
        onPageRenderedListener = listener;
    }

// --Commented out by Inspection START (16.07.20 22:47):
//    @SuppressLint("Range")
//    public void setOverlayView(View value) {
//        if (value == null && m_overlayView != null) {
//            int[] location = new int[4];
//            m_overlayView.getLocationOnScreen(location);
//            location[0] += m_overlayView.getPaddingLeft();
//            location[1] += m_overlayView.getPaddingTop();
//            location[2] = location[0] + m_overlayView.getWidth() - m_overlayView.getPaddingRight();
//            location[3] = location[1] + m_overlayView.getHeight() - m_overlayView.getPaddingBottom();
//            invalidate(new Rect(location[0], location[1], location[2], location[3]));
//        }
//
//        m_overlayView = value;
//    }
// --Commented out by Inspection STOP (16.07.20 22:47)

    public List<Pair<String, Float>> getChapters() {
        return m_chapters;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (m_dpiCompensation != 1f) {
            if (w > h)
                w = (int) (w / m_dpiCompensation); // landscape
            else
                h = (int) (h / m_dpiCompensation); // portrait
        }

        if ((m_actualHeight != h || m_actualWidth != w) && m_currentPage != -1) {
            m_actualWidth = w;
            m_actualHeight = h;
            reset();
        } else {
            m_actualWidth = w;
            m_actualHeight = h;
            if (m_currentPage == -1)
                nextPage(true, true);
        }
    }

    public String getPageText() {
        try {
            if (m_pages.get(m_currentPage) == null) {
                return "";
            } else {
                return m_pages.get(m_currentPage).getPageText();
            }
        } catch (Exception e) {
            Log.e("getpt", "exception");
            return "";
        }
    }

// --Commented out by Inspection START (16.07.20 22:46):
//    public void resetBackgroundDrawable() {
//        m_BackgroundDrawableId = 0;
//        m_backgroundBitmap = null;
//        m_backgroundDrawable = null;
//    }
// --Commented out by Inspection STOP (16.07.20 22:46)


// --Commented out by Inspection START (16.07.20 22:47):
//    public void setBackgroundDrawableId(int id) {
//        m_BackgroundDrawableId = id;
//        m_backgroundBitmap = BitmapFactory.decodeResource(getContext().getResources(), m_BackgroundDrawableId);
//        m_backgroundDrawable = new BitmapDrawable(getContext().getResources(), m_backgroundBitmap);
//        m_backgroundDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//        Rect src = new Rect(0, 0, m_actualWidth - 1, m_actualHeight - 1);
//        m_backgroundDrawable.setBounds(src);
//    }
// --Commented out by Inspection STOP (16.07.20 22:47)

// --Commented out by Inspection START (16.07.20 22:46):
//    public void setBackgroundDrawableFile(String file_name) {
//        m_BackgroundDrawableId = -1;
//        m_backgroundBitmap = BitmapFactory.decodeFile(file_name);
//        m_backgroundDrawable = new BitmapDrawable(getContext().getResources(), m_backgroundBitmap);
//        m_backgroundDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//        Rect src = new Rect(0, 0, m_actualWidth - 1, m_actualHeight - 1);
//        m_backgroundDrawable.setBounds(src);
//    }
// --Commented out by Inspection STOP (16.07.20 22:46)

    public int getRealPage() {
        return int_current_page;
    }

    public int getRealTotal() {
        return int_total_page;
    }

    public Boolean isEndOfBook() {
        return end_of_book;
    }

    public void init(BaseBookReader reader, long position, int page, List<Pair<String, Float>> chapters, Map<String, Integer> styles, List<ImageData> images, Map<String, List<BookLine>> notes) {
        if (reader == null) {
            clear();
            return;
        }
        m_chapters = chapters;
        m_images = images;
        m_styles = styles;
        m_reader = reader;
        m_notes = notes;
        m_bookStart = m_reader.getPosition();
        m_reader.reset(position);
        m_footerTextLeft = null;

        int offset = m_reader.getOffset();
        if (offset < 10) {
            m_reader.setOffset(0);
        }

        resetPages(false);
        m_pageNumber = page;
        nextPage(true, false);
    }

    public void clear() {
        m_chapters = null;
        m_images = null;
        m_styles = null;
        m_reader = null;
        m_notes = null;
        m_bookStart = 0;
        resetPages(false);
        m_pageNumber = 0;
    }

    private void resetPages(boolean clearAverage) {
        if (m_pages != null) {
            for (int i = 0; i < m_pages.size(); i++) {
                TextPage page = m_pages.get(i);
                if (page != null) {
                    page.clean();
                }
            }
        }

        m_pages = new LinkedList<>();
        m_currentPage = -1;

        if (clearAverage) {
            m_averagePages = 0;
            m_averageTotal = 0;
            m_averagePageSizes.clear();
        }
    }

    public void changeSettings(float extraStroke, int orientation, int header, int footer, boolean inverse, int textColor, int backColor, int refreshMode, boolean reset) {
        if (reset) {
            reset();
        }

        m_inverse = inverse;

        if (refreshMode != m_refreshMode) {
            m_refreshMode = refreshMode;
        }

        if (m_textPaint != null) {
            m_textPaint.changeContrast(extraStroke);
            m_superPaint.changeContrast(extraStroke);
            m_boldPaint.changeContrast(extraStroke);
            m_boldItalicPaint.changeContrast(extraStroke);
            m_italicPaint.changeContrast(extraStroke);
            m_header1Paint.changeContrast(extraStroke);
            m_header2Paint.changeContrast(extraStroke);
            m_header3Paint.changeContrast(extraStroke);
            m_header4Paint.changeContrast(extraStroke);
            m_subtitlePaint.changeContrast(extraStroke);

            int ntextColor = m_inverse ? backColor : textColor;

            m_headPaint.setColor(ntextColor);

            m_textPaint.getPaint().setColor(ntextColor);
            m_superPaint.getPaint().setColor(ntextColor);
            m_boldPaint.getPaint().setColor(ntextColor);
            m_boldItalicPaint.getPaint().setColor(ntextColor);
            m_italicPaint.getPaint().setColor(ntextColor);
            m_header1Paint.getPaint().setColor(ntextColor);
            m_header2Paint.getPaint().setColor(ntextColor);
            m_header3Paint.getPaint().setColor(ntextColor);
            m_header4Paint.getPaint().setColor(ntextColor);
            m_subtitlePaint.getPaint().setColor(ntextColor);
            m_pagePaint.setColor(ntextColor);
        }
        m_backColor = m_inverse ? textColor : backColor;

        m_footer = footer;
        m_header = header;
        m_orientation = orientation;

        if (m_headPaint != null && m_header > 0) {
            m_headPaint.setTextSize(HEADER_FONT_SIZE);
            m_systemPaint.setTextSize(HEADER_FONT_SIZE);
        }

        if (m_dateFormat != null && m_systemPaint != null) {
            m_clockWidth = (int) m_systemPaint.measureText("2020-01-01 11:11");
        }
    }

    public void reset() {
        if (m_reader != null) {
            if (m_pages != null && m_pages.size() > 0 && m_currentPage >= 0) {
                TextPage currentPage = m_pages.get(m_currentPage);
                m_reader.reset(currentPage.getStartPosition());
                m_footerTextLeft = null;
                m_pageNumber = currentPage.getPageNumber() - 1;
            }
        }
        resetPages(true);
    }

    public void initFonts(float fsize, Typeface normalFont, Typeface boldFont, Typeface italicFont, Typeface boldItalicFont, int orientation, float extraStroke, boolean inverse, int textColor, int backColor, float lineSpace, float firstLine, int header, int footer, int refreshMode,
                          int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {


        int size = Dips.spToPx((int) (fsize * 0.75));
        reset();

        m_paddingLeft = paddingLeft;
        m_paddingRight = paddingRight;
        m_paddingTop = paddingTop;
        m_paddingBottom = paddingBottom;

        if (refreshMode != m_refreshMode) {
            m_refreshMode = refreshMode;
        }

        m_footer = footer;
        m_header = header;
        m_backColor = inverse ? textColor : backColor;

        TextPaint textPaint = new TextPaint();
        textPaint.setDither(false);
        textPaint.setLinearText(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(size);
        textPaint.setTypeface(normalFont);
        textPaint.setColor(inverse ? backColor : textColor);

        if (extraStroke > 0) {
            textPaint.setStrokeWidth(extraStroke);
            textPaint.setStyle(Style.FILL_AND_STROKE);
        } else
            textPaint.setStyle(Style.FILL);

        TextPaint superPaint = new TextPaint(textPaint);
        superPaint.setTextSize(size * 3f / 4);

        TextPaint italicPaint = new TextPaint(textPaint);
        italicPaint.setTypeface(italicFont);

        TextPaint boldPaint = new TextPaint(textPaint);
        boldPaint.setTypeface(boldFont);
        if (boldFont == normalFont || !boldFont.isBold()) {
            boldPaint.setFakeBoldText(true);
        }

        TextPaint boldItalicPaint = new TextPaint(textPaint);
        boldItalicPaint.setTypeface(boldItalicFont);
        if (boldItalicFont == italicFont || !boldItalicFont.isBold()) {
            boldItalicPaint.setFakeBoldText(true);
        }

        TextPaint header1Paint = new TextPaint(boldPaint);
        header1Paint.setTextSize(size + 8);

        TextPaint header2Paint = new TextPaint(boldPaint);
        header2Paint.setTextSize(size + 6);

        TextPaint header3Paint = new TextPaint(boldPaint);
        header3Paint.setTextSize(size + 4);

        TextPaint header4Paint = new TextPaint(textPaint);
        header4Paint.setTextSize(size + 2);

        TextPaint subtitlePaint = new TextPaint(italicPaint);
        subtitlePaint.setTextSize(size + 1);

        if (m_systemPaint == null || m_inverse != inverse) {
            m_systemPaint = new TextPaint();
            m_systemPaint.setTextSize(16);
            m_systemPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            m_systemPaint.setLinearText(true);
            m_systemPaint.setAntiAlias(true);
            m_systemPaint.setTextAlign(Align.RIGHT);
            m_systemPaint.setColor(inverse ? Color.WHITE : Color.BLACK);

            m_pagePaint = new TextPaint(m_systemPaint);
            m_pagePaint.setTextSize(Dips.spToPx(9));
            m_pagePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            m_pagePaint.setColor(textColor);

            m_darkGrayPaint = new Paint(m_systemPaint);
            m_darkGrayPaint.setColor(0x555555);
            m_darkGrayPaint.setAlpha(0xFF);

            m_whitePaint = new Paint(m_systemPaint);
            m_whitePaint.setColor(inverse ? Color.BLACK : Color.WHITE);

            m_blackPaint = new Paint(m_systemPaint);
            m_blackPaint.setColor(Color.BLACK);

            m_lightGrayPaint = new Paint(m_systemPaint);
            m_lightGrayPaint.setColor(0xAAAAAA);
            m_lightGrayPaint.setAlpha(0xFF);

            m_grayPaint = new Paint(m_systemPaint);
            m_grayPaint.setColor(0x808080);
            m_grayPaint.setAlpha(0xFF);

            m_headPaint = new TextPaint(m_systemPaint);
            m_headPaint.setTextAlign(Align.LEFT);
            m_headPaint.setTextSize(HEADER_FONT_SIZE);
            m_headPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            m_headPaint.setAlpha(255);
        }

        m_textPaint = new FontStyle(textPaint, lineSpace, firstLine);
        m_superPaint = new FontStyle(superPaint, lineSpace, firstLine);
        m_boldPaint = new FontStyle(boldPaint, lineSpace, firstLine);
        m_boldItalicPaint = new FontStyle(boldItalicPaint, lineSpace, firstLine);
        m_italicPaint = new FontStyle(italicPaint, lineSpace + 0.05f, firstLine);
        m_header1Paint = new FontStyle(header1Paint, lineSpace + 0.05f, 65.0f);
        m_header2Paint = new FontStyle(header2Paint, lineSpace + 0.05f, 60.0f);
        m_header3Paint = new FontStyle(header3Paint, lineSpace + 0.05f, 55.0f);
        m_header4Paint = new FontStyle(header4Paint, lineSpace + 0.05f, 50.0f);
        m_subtitlePaint = new FontStyle(subtitlePaint, lineSpace + 0.05f, firstLine);

        m_inverse = inverse;
        m_orientation = orientation;

        if (m_header > 0) {
            m_headPaint.setTextSize(HEADER_FONT_SIZE);
            m_systemPaint.setTextSize((int) (m_header / 1.8f));
        }

        m_clockWidth = (int) m_systemPaint.measureText("2020-01-01 11:11");
    }

    private void applyOrientation(Canvas canvas) {
        if (m_dpiCompensation != 1.0f) {
            if (m_actualWidth > m_actualHeight) {
                canvas.scale(m_dpiCompensation, 1.0f);
            } else {
                canvas.scale(1.0f, m_dpiCompensation);
            }
        }

        switch (m_orientation) {
            case ORIENTATION_180:
                canvas.rotate(180);
                canvas.translate(-m_actualWidth, -m_actualHeight);
                break;
            case ORIENTATION_CW:
                canvas.rotate(90);
                canvas.translate(0, -m_actualWidth);
                break;
            case ORIENTATION_CCW:
                canvas.rotate(-90);
                canvas.translate(-m_actualHeight, 0);
                break;
        }
    }

    public void doInvalidate() {
        if (m_textPaint == null) {
            return;
        }

        if (m_pages == null || m_pages.size() == 0 || m_currentPage == -1) {
            nextPage(false, true);
        }

        if (m_pages != null && m_pages.size() > 0) {
            invalidate();
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void drawInfo(Canvas canvas, TextPage page) {
        float percent = m_reader.getPercent(page.getEndPosition());
        int height;
        int width;

        if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180) {
            width = m_actualWidth;
            height = m_actualHeight;
        } else {
            width = m_actualHeight;
            height = m_actualWidth;
        }

        if (m_fullScreen && m_header > 0) {
            if (!m_inverse) {
                canvas.drawRect(new Rect(0, 0, width, m_header), m_whitePaint);
            } else {
                canvas.drawRect(new Rect(0, 0, width, m_header), m_blackPaint);
            }

            String ntitle = m_reader.getTitle().trim();
            if (ntitle.length() > 0 && ntitle.charAt(ntitle.length() - 1) == ',') {
                ntitle = ntitle.substring(0, ntitle.length() - 1);
            }

            StringBuilder title = new StringBuilder(100);
            title.append(" ");
            String chapter = null;

            float spercent = m_reader.getPercent(page.getStartPosition());

            for (int i = 0; i < m_chapters.size(); i++) {
                if (spercent < m_chapters.get(i).second) {
                    chapter = m_chapters.get(i == 0 ? 0 : i - 1).first.trim();
                    if (chapter.length() > 0 && chapter.charAt(chapter.length() - 1) == ',')
                        chapter = chapter.substring(0, chapter.length() - 1);
                    break;
                }
            }

            if (chapter != null && !chapter.equals(ntitle) && !chapter.equals("Title Page")) {
                if (title.length() > 0) {
                    char last = title.charAt(title.length() - 1);
                    if (Character.isLetter(last) || Character.isDigit(last))
                        title.append('.');
                    title.append(' ');
                }
                title.append(chapter);
            }

            StaticLayout layout = new StaticLayout(title.toString(), m_headPaint, width - m_clockWidth - m_header - 20, android.text.Layout.Alignment.ALIGN_NORMAL, 100.0f, 0.0f, true);

            String rtitle = layout.getLineCount() == 1 ? title.toString() : title.subSequence(0, layout.getLineEnd(0)).toString().trim() + "…";

            canvas.drawText(rtitle, Dips.dpToPx(24), m_header - Dips.dpToPx(3), m_headPaint);
            canvas.drawRect(0, m_header - 1, width, m_header, m_systemPaint);
        }

        if (m_footer != FOOTER_OFF) {
            int shift = height - FOOTER_HEIGHT;
            int barWidth = width - 30;
            int barLeft = 10;

            canvas.drawRect(new Rect(barLeft + barWidth, shift + 5, barLeft + barWidth + 1, shift + 20), m_lightGrayPaint);
            canvas.drawRect(new Rect(barLeft, shift + 4, barLeft + barWidth, shift + 19), m_lightGrayPaint);
            canvas.drawRect(new Rect(barLeft - 1, shift + 5, barLeft, shift + 19), m_darkGrayPaint);
            int progressX = barLeft + (int) (barWidth * percent / 100f);
            canvas.drawRect(new Rect(barLeft, shift + 4, progressX, shift + 16), m_darkGrayPaint);

            if (m_footer == FOOTER_TICKS) {
                int lastx = 0;
                for (int i = 0; i < m_chapters.size(); i++) {
                    float chapterPercent = m_chapters.get(i).second;
                    int x = barLeft + (int) (barWidth * chapterPercent / 100f);

                    if (x == lastx) {
                        continue;
                    }

                    lastx = x;
                    if (chapterPercent >= percent) {
                        canvas.drawLine(x, shift + 4, x, shift + 16, m_systemPaint);
                    } else {
                        canvas.drawLine(x, shift + 5, x, shift + 15, m_grayPaint);
                    }
                }
            }


            int pageNumber = page.getPageNumber();
            int total = m_averageTotal;

            if (total <= 0 && m_reader != null) {
                int pageLines = (int) (height / (m_textPaint.getHeight() + m_textPaint.getHeightMod()) + 1);
                int lineChars = (int) (width / m_textPaint.getDashWidth());
                total = (int) m_reader.getMaxSize() / (pageLines * lineChars) + m_chapters.size();
            }

            if (pageNumber > total || percent >= 100) {
                total = pageNumber;
            } else {
                if (pageNumber == total && percent < 100) {
                    total = pageNumber + 1;
                }
            }

            if (m_reader != null) {
                Log.d("PN", pageNumber + " " + m_reader.getMaxSize());
            }

            int_current_page = pageNumber;
            int_total_page = total;
            end_of_book = (pageNumber == total);
            if (onPageRenderedListener != null) {
                onPageRenderedListener.onPageRenderedListener();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        doDraw(canvas, m_currentPage);
    }

    public Bitmap getPageBitmap(int pageIndex) {
        Bitmap imageBitmap = Bitmap.createBitmap(m_actualWidth, m_actualHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        try {
            doDraw(canvas, pageIndex);
            Log.i("PAGE", pageIndex + ":" + m_currentPage);
        } catch (Exception e) {
            Log.e("ERR PAGE", pageIndex + ":" + m_currentPage);
            e.printStackTrace();
        }
        return imageBitmap;
    }

    @SuppressLint("Range")
    protected void doDraw(Canvas canvas, int page_num) {
        if (page_num == -1) {
            nextPage(true, true);
        }

        Rect clip;
        if (m_pages != null && m_pages.size() > 0 && page_num != -1) {

            canvas.drawColor(m_backColor, PorterDuff.Mode.SRC);

            TextPage page = m_pages.get(page_num);

            canvas.save();

            applyOrientation(canvas);

            clip = canvas.getClipBounds();

            int top = m_paddingTop + (m_fullScreen ? m_header : 0);
            int bottom = (m_orientation == ORIENTATION_180 || m_orientation == ORIENTATION_NORMAL ? m_actualHeight : m_actualWidth) - m_paddingBottom - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0);

            if (clip.bottom > top && clip.top < bottom) {
                page.draw(canvas, m_paddingLeft, top, m_pagePaint);
            }

            drawInfo(canvas, page);
            canvas.restore();
        }
    }

    private TextPage preparePage(int width, int height, int virtualHeight, long stopAt, int pageNumber) {
        TextPage page = new TextPage(width, virtualHeight == 0 ? height : virtualHeight, m_footerSpace, pageNumber);

        int lastFlags = 0;
        int lastStyleFlags = 0;
        int notesCount = 0;
        boolean footerBreak = false;

        while (!m_reader.isFinished()) {
            int flags;
            int offset;
            long position;
            CharSequence nchars;

            if (m_footerTextLeft != null) {
                flags = BaseBookReader.FOOTER | BaseBookReader.LINE_BREAK | BaseBookReader.JUSTIFY;
                nchars = m_footerTextLeft;
                offset = m_footerTextOffset;
                position = 0;
            } else {
                flags = m_reader.getFlags();

                if (!page.isEmpty() && (flags & BaseBookReader.NEW_PAGE) != 0) {
                    // Log.d("TextReader", "Breaking page");
                    break;
                }

                offset = m_reader.getOffset();
                position = m_reader.getPosition();

                if (stopAt > 0 && position + offset >= stopAt) {
                    page.finish(TextPage.PAGE_FULL, height, m_paddingTop + (m_fullScreen ? m_header : 0));
                    return page;
                }

                nchars = m_reader.getText();
                flags = m_reader.getFlags();
            }

            if ((flags & BaseBookReader.LINK) != 0) {
                String href = m_reader.getLinkHRef();

                if (href != null) {
                    if (href.startsWith("#") && m_notes.containsKey(href.substring(1))) {
                        StringBuilder noteSymbol;

                        if (notesCount >= s_superscriptNumbers.length) {
                            noteSymbol = new StringBuilder(" *");
                            for (int h = 0; h < notesCount - s_superscriptNumbers.length; h++) {
                                noteSymbol.append("*");
                            }
                        } else
                            noteSymbol = new StringBuilder(s_superscriptNumbers[notesCount]);

                        notesCount++;

                        StringBuilder noteText = new StringBuilder();
                        noteText.append(noteSymbol);
                        for (BookLine line : Objects.requireNonNull(m_notes.get(href.substring(1)))) {
                            if (line.getText() != null) {
                                noteText.append(" ");
                                noteText.append(line.getText().toString());
                            }
                        }
                        nchars = noteText.toString();

                        page.addNonBreakText(noteSymbol.toString());
                        flags = BaseBookReader.FOOTER | BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY;
                        lastFlags |= BaseBookReader.WORD_BREAK;
                        offset = 0;
                    } else {
                        String linkTitle = m_reader.getLinkTitle();

                        if (linkTitle != null && linkTitle.length() > 2 && href.contains("#") && nchars.length() < 10) {
                            StringBuilder noteSymbol;

                            if (notesCount >= s_superscriptNumbers.length) {
                                noteSymbol = new StringBuilder(" *");
                                for (int h = 0; h < notesCount - s_superscriptNumbers.length; h++) {
                                    noteSymbol.append("*");
                                }
                            } else {
                                noteSymbol = new StringBuilder(s_superscriptNumbers[notesCount]);
                            }

                            notesCount++;

                            nchars = noteSymbol +
                                    " " +
                                    linkTitle;

                            page.addNonBreakText(noteSymbol.toString());

                            flags = BaseBookReader.FOOTER | BaseBookReader.NEW_LINE | BaseBookReader.JUSTIFY;
                            lastFlags |= BaseBookReader.WORD_BREAK;
                            offset = 0;
                        }
                    }
                } else {
                    if ((flags & BaseBookReader.SUPER) != 0) // remove book's
                    // own footnotes
                    {
                        m_reader.advance();
                        continue;
                    }

                    flags = lastFlags;
                }
            }

            if ((flags & BaseBookReader.IMAGE) != 0) {
                String imageSrc = m_reader.getImageSrc();
                //Log.d("TextReader", "Processing image " + imageSrc);

                boolean added = false;

                if (m_images != null) {
                    for (int i = 0; i < m_images.size(); i++) {
                        if (m_images.get(i).getName().equals(imageSrc)) {
                            if (!page.addImage(m_images.get(i), m_textPaint, position + offset)) {
                                page.finish(TextPage.PAGE_FULL, height, m_paddingTop + (m_fullScreen ? m_header : 0));
                                return page;
                            }
                            added = true;
                            break;
                        }
                    }
                }

                if (added) {
                    //Log.d("TextReader", "Added image " + imageSrc);
                    m_reader.advance();
                    continue;
                }
            }

            if (nchars == null || nchars.length() == 0) {
                break;
            }

            String[] classes = m_reader.getClassNames();

            if (classes != null) {
                for (String aClass : classes) {
                    Integer istyle = m_styles.get(aClass);
                    if (istyle != null) {
                        flags |= istyle;
                    }
                }
            }

            FontStyle style = m_textPaint;

            if ((flags & BaseBookReader.HEADER_4) != 0) {
                style = m_header4Paint;
            } else if ((flags & BaseBookReader.FOOTER) != 0) {
                style = m_superPaint;
            } else if ((flags & BaseBookReader.HEADER_3) != 0) {
                style = m_header3Paint;
            } else if ((flags & BaseBookReader.HEADER_2) != 0) {
                style = m_header2Paint;
            } else if ((flags & BaseBookReader.HEADER_1) != 0) {
                style = m_header1Paint;
            } else if ((flags & BaseBookReader.SUBTITLE) != 0) {
                style = m_subtitlePaint;
            } else if ((flags & BaseBookReader.BOLD) != 0) {
                if ((flags & BaseBookReader.ITALIC) != 0)
                    style = m_boldItalicPaint;
                else
                    style = m_boldPaint;
            } else if ((flags & BaseBookReader.ITALIC) != 0)
                style = m_italicPaint;

            if (lastStyleFlags != 0 && (lastStyleFlags & BaseBookReader.STYLE) != (flags & BaseBookReader.STYLE))
                flags |= BaseBookReader.NEW_LINE;

            if ((flags & BaseBookReader.NEW_LINE) != 0 && offset > 0)
                flags &= ~BaseBookReader.NEW_LINE;

            FixedCharSequence text = FixedCharSequence.toFixedCharSequence(nchars);

            page.setPageText(page.getPageText() + text.toString() + "\n");
//            m_last_page.append(text.toString());

            int lastChar = text.length() - 1;

            boolean lineStart = (flags & BaseBookReader.NEW_LINE) != 0;
            boolean hasTab = false;
            int wordLength;
            int wordStart = offset;

            for (int i = offset; i <= lastChar; i++) {
                char ch = text.charAt(i);
                boolean separator = false;
                boolean dash = false;

                switch (ch) {
                    case ' ':
                        if (i > 0 && lineStart && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–')) {
                            text.setChar(i, '\u00a0'); // set nbsp
                            break;
                        }

                        separator = true;
                        break;
                    case '\t':
                        if (lineStart) {
                            if (!m_textPaint.hasTab()) {
                                text.setChar(i, ' ');
                            }
                            hasTab = true;
                            break;
                        }
                        separator = true;
                        break;
                    case '\n':
                    case '\r':
                    case '\0':
                        text.setChar(i, ' ');
                        if (lineStart) {
                            break;
                        }
                        separator = true;
                        break;
                    case '-':
                    case '\'':
                        if (m_reader.isDirty()) {
                            if ((i > 0 && text.charAt(i - 1) == ' ') || (i != lastChar && text.charAt(i + 1) == ' ')) {
                                text.setChar(i, '—');
                            } else {
                                if ((i != lastChar && text.charAt(i + 1) == '-')) {
                                    text.setChar(i, ' ');
                                } else {
                                    dash = true;
                                    separator = true;
                                }
                            }
                        } else {
                            if (!(i > 0 && text.charAt(i - 1) == ' ') || !(i != lastChar && text.charAt(i + 1) == ' ')) {
                                dash = true;
                                separator = true;
                            }
                        }
                        break;
                    case '\u00a0': // &nbsp;
                        if (lineStart) {
                            break;
                        }

                        if (i > 0 && (text.charAt(i - 1) == '—' || text.charAt(i - 1) == '-' || text.charAt(i - 1) == '–'))
                            break;

                        separator = true;
                        break;
                    case '—':
                    case '–':
                        break;
                    default:
                        lineStart = false;
                        if (i != lastChar)
                            continue;
                        break;
                }

                if (separator || i == lastChar) {
                    lineStart = false;
                    wordLength = i - wordStart;
                    if (dash || !separator) {
                        wordLength++;
                    }

                    if (wordLength > 0/* || lineStart */) {
                        FixedCharSequence word;

                        word = (FixedCharSequence) text.subSequence(wordStart, wordStart + wordLength);// .toString();

                        if (hasTab && m_textPaint.getFirstLine() != 0) {
                            String str = word.toString();
                            if (str.contains(m_textPaint.hasTab() ? "\t  " : "   ")) {
                                m_textPaint.setFirstLine(0);
                                m_italicPaint.setFirstLine(0);
                                m_boldPaint.setFirstLine(0);
                                m_boldItalicPaint.setFirstLine(0);
                                m_subtitlePaint.setFirstLine(0);
                            }
                            word = new FixedCharSequence(str.replace("\t", "      "));
                        }

                        if ((flags & BaseBookReader.LINE_BREAK) != 0 || (lastFlags & BaseBookReader.LINE_BREAK) != 0) {
                            if ((flags & BaseBookReader.FOOTER) != 0 && m_footerTextLeft != null) {
                                flags &= ~(BaseBookReader.LINE_BREAK | BaseBookReader.WORD_BREAK);
                            } else {
                                flags &= ~(BaseBookReader.LINE_BREAK | BaseBookReader.WORD_BREAK);
                                flags |= BaseBookReader.NEW_LINE;
                            }
                        } else if (dash) {
                            flags |= BaseBookReader.WORD_BREAK;
                        }

                        if (separator && !dash) {
                            flags &= ~BaseBookReader.WORD_BREAK;
                        }

                        if ((flags & BaseBookReader.FOOTER) == 0) {
                            lastStyleFlags = flags;
                        }

                        int addResult = page.addWord(word, flags, style, position + wordStart);

                        if (addResult >= wordLength) {
                            flags &= ~(BaseBookReader.NEW_PAGE | BaseBookReader.NEW_LINE | BaseBookReader.WORD_BREAK);
                        } else if (addResult <= 0) {
                            if ((flags & BaseBookReader.FOOTER) != 0) {
                                m_footerTextLeft = text;
                                m_footerTextOffset = wordStart;
                                footerBreak = true;
                                if (wordStart > 0) {
                                    page.addNonBreakFooterText(" >");
                                }
                                // break;
                            } else {
                                m_reader.setOffset(wordStart);
                            }

                            page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.PAGE_BREAK : TextPage.PAGE_BREAK, height, m_paddingTop + (m_fullScreen ? m_header : 0));
                            // Log.d("TextReader", "Parsing took " +
                            // (System.currentTimeMillis() - start) + ", symbols
                            // " + wordStart + ", page break");
                            return page;
                        } else {
                            if ((flags & BaseBookReader.FOOTER) != 0) {
                                m_footerTextLeft = text;
                                m_footerTextOffset = wordStart + addResult;
                                footerBreak = true;
                            } else {
                                m_reader.setOffset(wordStart + addResult);
                            }

                            page.finish(footerBreak ? TextPage.FOOTER_PAGE_BREAK | TextPage.PAGE_BREAK : TextPage.PAGE_BREAK, height, m_paddingTop + (m_fullScreen ? m_header : 0));
                            return page;
                        }
                    }
                    wordStart = i + 1;
                    hasTab = false;

                    if ((flags & BaseBookReader.FOOTER) == 0) {
                        if (stopAt > 0 && position + wordStart >= stopAt) {
                            if (position + wordStart != stopAt) {
                                page.trimLast();
                                wordStart = (int) (stopAt - position);
                            }

                            if (lastChar + 1 - wordStart > 0) {
                                m_reader.setOffset(wordStart);
                                page.finish(TextPage.PAGE_BREAK, height, m_paddingTop + (m_fullScreen ? m_header : 0));
                            } else
                                page.finish(TextPage.PAGE_FULL, height, m_paddingTop + (m_fullScreen ? m_header : 0));
                            return page;
                        }
                    }
                }
            }

            if (!footerBreak && (flags & BaseBookReader.FOOTER) != 0) {
                m_footerTextLeft = null;
            }

            if ((flags & BaseBookReader.FOOTER) == 0) {
                lastFlags = flags & ~(BaseBookReader.NEW_PAGE | BaseBookReader.NEW_LINE | BaseBookReader.WORD_BREAK);
            }

            if (m_footerTextLeft == null) {
                m_reader.advance();
            }
        }

        page.finish(TextPage.NEW_PAGE, height, m_paddingTop + (m_fullScreen ? m_header : 0));
        return page;
    }

    public void nextPage(boolean update, boolean cacheMore) {
        Log.e("NEXT PAGE", m_currentPage + "");
        if (m_pages == null)
            return;

        if (getWidth() == 0)
            return;

        if (m_currentPage >= m_pages.size() - 4)
            cachePage(m_pages.size() != 0 && m_reader.getPosition() == 0, cacheMore);

        if (m_currentPage >= m_pages.size() - 1)
            return;

        m_currentPage++;
        if (update) {
            doInvalidate();
        }

        Log.e("NEXT PAGE NEW", m_currentPage + "");
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void cachePage(boolean delay, boolean cacheMore) {
        if (m_reader == null || m_reader.isFinished()) {
            return;
        }

        int height;
        int width;

        if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180) {
            width = m_actualWidth;
            height = m_actualHeight;
        } else {
            width = m_actualHeight;
            height = m_actualWidth;
        }

        if (m_pages.size() > 0) {
            TextPage prevPage = m_pages.get(m_pages.size() - 1);
            if (prevPage.getEndPosition() != m_reader.getPosition()) {
                m_reader.reset(prevPage.getEndPosition() + prevPage.getEndOffset());
                m_footerTextLeft = null;
            }
        }

        TextPage page = preparePage(width - m_paddingLeft - m_paddingRight, height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0), 0, -1, ++m_pageNumber);

        float lineChars = page.getAverageLineChars(5);
        long endPosition = m_reader.getPosition();

        page.setEndPosition(endPosition);
        page.setEndOffset(m_reader.getOffset());

        long startPos = m_reader.getGlobalPosition(page.getStartPosition());
        long endPos = m_reader.getGlobalPosition(page.getEndPosition());

        try {
            if (lineChars != 0) {
                int pageSize = (int) (endPos - startPos);// *100/90;
                int lineHeight = (int) (m_textPaint.getHeight() + 2 * m_textPaint.getHeightMod());
                int pageHeight = height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0);
                int pageLines = pageHeight / lineHeight + 1;
                int maxSize = (int) (pageLines * lineChars * 1.2f);
                if (pageSize <= 0)
                    pageSize = maxSize;
                else if (pageSize > maxSize)
                    pageSize = maxSize;

                m_averagePageSizes.put(pageSize);

                int averageSize = m_averagePageSizes.getMedian();
                m_averagePages++;

                if (m_averagePages > 0 && ( (m_averagePages - 1) % (m_maxCachedPages) == 0) && averageSize != 0) {
                    int value = (int) (m_reader.getMaxSize() / averageSize) + m_chapters.size();

                    float percent = m_reader.getPercent(page.getStartPosition());

                    int percentValue = Math.round(100 * page.getPageNumber() / percent);
                    if (m_averageTotal == 0) {
                        if (percent > 10) {
                            m_averageTotal = percentValue;
                        } else {
                            m_averageTotal = value;
                        }
                    } else {
                        if (percent > 10)
                            if (value > percentValue * 1.2 || value < percentValue / 0.8)
                                value = percentValue;

                        m_averageTotal = (m_averageTotal * 3 + value) / 4;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_pages.add(page);

        if (m_pages.size() > m_maxCachedPages) {
            TextPage bpage = m_pages.remove(0);
            if (bpage != null)
                bpage.clean();
            m_currentPage--;
        }

        if (!m_reader.isFinished() && m_pages.size() - m_currentPage < m_maxCachedPages && cacheMore) {
            if (delay)
                postDelayed(new CachePageRunnable(/* m_currentPage + 1 */), 0);
            else
                cachePage(false, true);
        }
    }

    public void prevPage() {
        if (getWidth() == 0)
            return;

        if (m_currentPage <= 0) {
            if (m_pages == null || m_pages.size() == 0)
                return;

            int height;
            int width;

            if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180) {
                width = m_actualWidth;
                height = m_actualHeight;
            } else {
                //noinspection SuspiciousNameCombination
                width = m_actualHeight;
                //noinspection SuspiciousNameCombination
                height = m_actualWidth;
            }

            TextPage page = m_pages.get(0);
            long pagePos = page.getStartPosition();
            if (pagePos <= m_bookStart)
                return;

            int pageLines = (int) ((height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0)) / (m_textPaint.getHeight() + m_textPaint.getHeightMod()) + 1);
            int lineChars = (int) ((width - m_paddingLeft - m_paddingRight) / m_textPaint.getDashWidth());

            int pageSize = pageLines * lineChars;

            int count = 0;
            do {
                pageSize = m_reader.seekBackwards(pagePos, pageSize, pageLines, lineChars);

                int pageNumber = page.getPageNumber() - 1;
                if (pageNumber < 1)
                    pageNumber = 1;

                long startPos = m_reader.getPosition();
                page = preparePage(width - m_paddingLeft - m_paddingRight, height - m_paddingTop - m_paddingBottom - (m_fullScreen ? m_header : 0) - (m_footer != FOOTER_OFF ? FOOTER_HEIGHT : 0), 0x10000, pagePos, pageNumber);

                pagePos = startPos;
            } while (page.getStartPosition() == -1 && count++ < 5);

            long endPosition = m_reader.getPosition();

            page.setEndPosition(endPosition);
            page.setEndOffset(m_reader.getOffset());

            m_pages.add(0, page);

            if (m_pages.size() > 20)
                m_pages.remove(m_pages.size() - 1);

            m_currentPage = 1;
        }

        m_currentPage--;
        doInvalidate();
    }

    public void gotoPage(int page, float percent) {
        if (page == getPageNumber()) {
            return;
        }

        if (m_reader == null) {
            return;
        }

        m_reader.reset((long) (m_reader.getMaxSize() * percent));
        resetPages(false);
        m_pageNumber = page;
        nextPage(true, false);
    }

    public long getPosition() {
        if (m_pages == null || m_pages.size() == 0) {
            return -1;
        }

        if (m_currentPage < 0 || m_currentPage >= m_pages.size()) {
            return -1;
        }

        return m_pages.get(m_currentPage).getStartPosition();
    }

    public int getPageNumber() {
        if (m_pages == null || m_pages.size() == 0) {
            return 0;
        }

        if (m_currentPage < 0 || m_currentPage >= m_pages.size()) {
            return 0;
        }

        int result = m_pages.get(m_currentPage).getPageNumber() - 1;
        return Math.max(result, 1);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public int getTotalPages() {
        if (m_pages == null || m_pages.size() == 0) {
            return 0;
        }

        if (m_currentPage < 0 || m_currentPage >= m_pages.size()) {
            return 0;
        }

        TextPage page = m_pages.get(m_currentPage);

        float percent = m_reader.getPercent(page.getEndPosition());

        int height;
        int width;

        if (m_orientation == ORIENTATION_NORMAL || m_orientation == ORIENTATION_180) {
            width = m_actualWidth;
            height = m_actualHeight;
        } else {
            width = m_actualHeight;
            height = m_actualWidth;
        }

        int pageNumber = page.getPageNumber();

        int total = m_averageTotal;

        if (total <= 0 && m_reader != null) {
            int pageLines = (int) (height / (m_textPaint.getHeight() + m_textPaint.getHeightMod()) + 1);
            int lineChars = (int) (width / m_textPaint.getDashWidth());
            total = (int) m_reader.getMaxSize() / (pageLines * lineChars) + m_chapters.size();
        }

        if (pageNumber > total || percent >= 100) {
            total = pageNumber;
        } else {
            if (pageNumber == total && percent < 100) {
                total = pageNumber + 1;
            }
        }

        return total;
    }

    public int getTotalLength() {
        if (m_pages == null || m_pages.size() == 0) {
            return 0;
        }

        if (m_currentPage < 0 || m_currentPage >= m_pages.size()) {
            return 0;
        }

        TextPage page = m_pages.get(m_currentPage);

        return (int) page.getEndPosition();
    }

    public interface OnPageRenderedListener {
        void onPageRenderedListener();
    }

    private class CachePageRunnable implements Runnable {
        @Override
        public void run() {
            if (!m_reader.isFinished() && m_pages.size() - m_currentPage < m_maxCachedPages) {
                cachePage(true, true);
            }
        }
    }
}
