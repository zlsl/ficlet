package zlobniyslaine.ru.ficbook.breader.bookParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XmlBookReader extends BaseBookReader {
    private final Map<String, Integer> m_textTags;
    private final BookData m_bookData;
    private final String[] m_chapterTags;

    private int[] m_tagFlags;
    private long m_bodyIndex;
    private long m_chapterTagIndex;

    private final String m_title;
    private String m_linkTitle;
    private String m_linkHRef;
    private String m_imageSrc;

    private long m_classMask;
    private int m_offset;

    public String getTitle() {
        return m_title;
    }

    public String getLinkTitle() {
        return m_linkTitle;
    }

    public String getLinkHRef() {
        return m_linkHRef;
    }

    public String getImageSrc() {
        return m_imageSrc;
    }

    public long getPosition() {
        return m_bookData.getPosition();
    }

    public int getOffset() {
        return m_offset;
    }

    public void setOffset(int value) {
        m_offset = value;
    }

    public BookData getBookData() {
        return m_bookData;
    }

    public XmlBookReader(BookData data, String title, Map<String, Integer> textTags, String[] chapterTags, boolean dirty) {
        super(dirty);
        m_title = title;
        m_bookData = data;
        m_chapterTags = chapterTags;

        m_textTags = textTags;
        reinitTags();
        init();
    }

    public void reinitTags() {
        String[] tags = m_bookData.getTags();
        m_tagFlags = new int[tags.length];

        m_bodyIndex = 0;
        m_chapterTagIndex = 0;

        for (int i = 0; i < tags.length; i++) {
            Integer value = m_textTags.get(tags[i]);
            m_tagFlags[i] = value == null ? 0 : value;

            if (tags[i] != null) {
                if (tags[i].equals("body")) {
                    m_bodyIndex |= 1L << i;
                }
                for (String m_chapterTag : m_chapterTags) {
                    if (tags[i].equals(m_chapterTag)) {
                        m_chapterTagIndex |= 1L << i;
                    }
                }
            }
        }

        m_maxSize = m_bookData.getMaxPosition();
    }

    private void init() {
        if (m_inited)
            return;

        try {
            m_inited = true;
            reset(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            m_finished = true;
        }
    }

    private boolean processLine(BookLine line) {
        long mask = line.getTagMask();

        if (mask == 0 || mask == m_chapterTagIndex || (mask & m_bodyIndex) == 0) {
            if (mask == m_chapterTagIndex && !line.isPart()) {
                m_text = "\u00a0"; // &nbsp;
                m_flags = NEW_PAGE;
                return true;
            }
            return false;
        }

        int textFlag = retrieveFlags(mask);

        if (textFlag != 0) {
            if (line.getClassMask() != m_classMask) {
                m_classMask = line.getClassMask();
                updateClass(m_classMask);
            }

            m_text = line.getText();
            m_flags = textFlag;

            if ((textFlag & LINK) != 0) {
                m_linkTitle = line.getAttribute("title");
                m_linkHRef = line.getAttribute("href");
                if (m_linkHRef == null)
                    m_linkHRef = line.getAttribute("l:href");

                if ((m_text == null || m_text.length() == 0))
                    m_text = "*";

            }

            if ((textFlag & IMAGE) != 0) {
                m_imageSrc = line.getAttribute("src");
                if ((m_imageSrc == null || m_imageSrc.length() == 0))
                    m_imageSrc = line.getAttribute("l:href");
                if ((m_imageSrc == null || m_imageSrc.length() == 0))
                    m_imageSrc = line.getAttribute("xlink:href");

                if (m_imageSrc != null && m_imageSrc.startsWith("#"))
                    m_imageSrc = m_imageSrc.substring(1);

                if ((m_text == null || m_text.length() == 0))
                    m_text = line.getAttribute("alt");
                if ((m_text == null || m_text.length() == 0))
                    m_text = " ";
                return true;
            }

            if (line.isPart())
                m_flags &= ~(NEW_LINE | NEW_PAGE);
            else if (!line.isParentEmpty()) {
                if ((m_flags & NO_NEW_LINE) != 0)
                    m_flags &= ~NEW_LINE;

                if ((m_flags & NO_NEW_PAGE) != 0)
                    m_flags &= ~NEW_PAGE;
            }

            if (line.isRtl()) {
                m_flags |= RTL;
            }

            if ((m_text == null || m_text.length() == 0) && (m_flags & (NEW_LINE | NEW_PAGE)) != 0) {
                m_text = "\u00a0"; // &nbsp;
                return true;
            }

            return m_text != null && m_text.length() > 0;
        }
        return false;
    }

    public void advance() {
        init();
        super.advance();
        m_offset = 0;

        while (m_bookData.advance()) {
            BookLine line = m_bookData.getCurrentLine();
            if (processLine(line))
                return;
        }

        m_flags = NEW_PAGE;
        m_text = null;
        m_finished = true;
        //Log.d("TextReader", "Reader finished");
    }

    private void updateClass(long mask) {
        String[] classes = m_bookData.getClasses();
        List<String> nclasses = new ArrayList<>(classes.length);
        for (int i = 0; i < classes.length; i++) {
            if ((mask & (1L << i)) != 0)
                nclasses.add(classes[i]);
        }
        m_classNames = nclasses.toArray(new String[0]);
    }

    public int retrieveFlags(long mask) {
        int result = 0;
        for (int i = 0; i < m_tagFlags.length; i++)
            if (m_tagFlags[i] != 0 && (mask & (1L << i)) != 0)
                result |= m_tagFlags[i];
        return result;
    }

    public void reset(long position) {
        if (!m_inited)
            return;
        if (m_bookData.getPosition() + m_offset == position)
            return;

        m_flags = NEW_PAGE;
        m_text = null;
        m_finished = false;
        m_classMask = 0;

        m_offset = m_bookData.setPosition((int) position);

        do {
            BookLine line = m_bookData.getCurrentLine();
            if (processLine(line))
                break;
        }
        while (m_bookData.advance());
    }

    public int seekBackwards(long nposition, int value, int pageLines, int lineChars) {
        reset(nposition);

        int currentLine = m_bookData.getLineIndex();
        int nbytes = m_offset;

        //if (nbytes < value)
        while (currentLine-- > 0) {
            BookLine line = m_bookData.getLine(currentLine);

            if ((line.getTagMask() & m_bodyIndex) == 0)
                continue;

            int textFlag = retrieveFlags(line.getTagMask());

            if (textFlag != 0) {
                if (line.isPart())
                    textFlag &= ~(NEW_LINE | NEW_PAGE);

                if ((textFlag & NO_NEW_PAGE) != 0)
                    textFlag &= ~NEW_PAGE;

                CharSequence text = line.getText();

                if (text != null) {
                    nbytes += text.length();
                }

                if ((textFlag & IMAGE) != 0) {
                    nbytes += lineChars * 2; // don't know image height
                }

                if (nbytes > 0 && (textFlag & NEW_PAGE) != 0) {
                    break;
                }

                if (nbytes >= value) {
                    break;
                }
            }
        }

        if (currentLine < 0)
            currentLine = 0;

        m_bookData.setLineIndex(currentLine);
        m_offset = 0;
        m_flags = NEW_PAGE;
        m_text = null;
        m_finished = false;
        m_classMask = 0;

        do {
            BookLine line = m_bookData.getCurrentLine();
            if (processLine(line))
                break;
        }
        while (m_bookData.advance());

        return nbytes;
    }

    @Override
    public Object getData() {
        return getBookData();
    }
}
