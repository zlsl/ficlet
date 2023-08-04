package zlobniyslaine.ru.ficbook.breader.bookParser;

public abstract class BaseBookReader {
    public final static int NORMAL = 0x1000000;
    public final static int JUSTIFY = 0x01;
    public final static int HEADER_1 = 0x02;
    public final static int HEADER_2 = 0x04;
    public final static int HEADER_3 = 0x08;
    public final static int HEADER_4 = 0x10;
    public final static int ITALIC = 0x20;
    public final static int BOLD = 0x40;
    public final static int NEW_LINE = 0x80;
    public final static int NEW_PAGE = 0x100;
    public final static int LINE_BREAK = 0x400;
    public final static int SUBTITLE = 0x800;
    public final static int FIRST_LINE = 0x1000;
    public final static int DIV = 0x2000;
    public final static int WORD_BREAK = 0x4000;
    public final static int LINK = 0x8000;
    public final static int SUPER = 0x10000;
    public final static int IMAGE = 0x20000;
    public final static int NO_NEW_LINE = 0x40000;
    public final static int NO_NEW_PAGE = 0x80000;
    public final static int RTL = 0x100000;
    public final static int FOOTER = 0x200000;

    public final static int TEXT = JUSTIFY | HEADER_1 | HEADER_2 | HEADER_3 | HEADER_4 | DIV;
    public final static int STYLE = HEADER_1 | HEADER_2 | HEADER_3 | HEADER_4 | SUBTITLE | NORMAL | FOOTER;

    protected final boolean m_dirty;
    protected CharSequence m_text = null;
    protected String[] m_classNames = null;
    protected boolean m_finished = true;
    protected int m_flags = 0;
    protected boolean m_inited = false;
    protected long m_maxSize;
    protected long m_bookStart;

    public boolean isDirty() {
        return m_dirty;
    }

    public boolean isFinished() {
        return m_finished;
    }

    public CharSequence getText() {
        if (m_finished) {
            return null;
        }

        if (m_text == null) {
            advance();
        }

        return m_text;
    }

    public String[] getClassNames() {
        return m_classNames;
    }

    public int getFlags() {
        return m_flags;
    }

    public void setMaxSize(long value) {
        m_maxSize = value;
    }

    public long getMaxSize() {
        return m_maxSize;
    }

    public void setBookStart(long value) {
        m_bookStart = value;
    }

    public float getPercent(long position) {
        float result = 100f * (position - m_bookStart) / getMaxSize();
        if (result > 100f) {
            return 100f;
        }
        if (result < 0) {
            return 0;
        }
        return result;
    }

    public long getGlobalPosition(long position) {
        return position;
    }

    public void advance() {
        m_text = null;
    }

    protected BaseBookReader(boolean dirty) {
        m_dirty = dirty;
    }

    public abstract void reset(long position);

    public abstract int getOffset();

    public abstract void setOffset(int value);

    public abstract long getPosition();

    public abstract int seekBackwards(long position, int value, int pageLines, int lineChars);

    public abstract String getTitle();

    public abstract String getLinkTitle();

    public abstract String getLinkHRef();

    public abstract String getImageSrc();

    public abstract Object getData();
}
