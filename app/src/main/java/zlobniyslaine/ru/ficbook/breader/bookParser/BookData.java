package zlobniyslaine.ru.ficbook.breader.bookParser;

public class BookData {

    private final BookLine[] m_lines;
    private final int m_maxPosition;
    private final String[] m_classes;
    private final String[] m_tags;

    private int m_lineIndex;

    public BookLine getCurrentLine() {
        return m_lines[m_lineIndex];
    }

    public BookLine[] getLines() {
        return m_lines;
    }

    public int getPosition() {
        return m_lineIndex < 0 ? -1 : m_lines[m_lineIndex].getPosition();
    }

    public int setPosition(int position) {
        for (int i = 0; i < m_lines.length; i++)
            if (m_lines[i].getPosition() >= position) {
                m_lineIndex = i > 0 ? i - 1 : 0;
                int offset = position - m_lines[m_lineIndex].getPosition();
                if (offset < 0) {
                    offset = 0;
                }
                return offset;
            }

        m_lineIndex = m_lines.length - 1;
        return m_maxPosition - m_lines[m_lineIndex].getPosition();
    }

    public int getLineIndex() {
        return m_lineIndex;
    }

    public void setLineIndex(int index) {
        m_lineIndex = index;
    }

    public BookLine getLine(int lineIndex) {
        return m_lines[lineIndex];
    }

    public boolean advance() {
        if (m_lineIndex >= m_lines.length - 1) {
            return false;
        }
        m_lineIndex++;
        return true;
    }

    public int getMaxPosition() {
        return m_maxPosition;
    }

    public String[] getClasses() {
        return m_classes;
    }

    public String[] getTags() {
        return m_tags;
    }

    public BookData(BookLine[] lines, String[] tags, String[] classes, int maxPosition) {
        m_lines = lines;
        m_tags = tags;
        m_classes = classes;
        m_lineIndex = -1;
        m_maxPosition = maxPosition;
    }

}
