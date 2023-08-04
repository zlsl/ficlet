package zlobniyslaine.ru.ficbook.breader.bookParser;

import android.content.res.XmlResourceParser;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import zlobniyslaine.ru.ficbook.breader.common.ByteCharSequence;
import zlobniyslaine.ru.ficbook.breader.common.XmlReader;

public final class XmlBookParser {
    private static final byte[] s_classBytes = {'c', 'l', 'a', 's', 's'};
    private static final byte[] s_dirBytes = {'d', 'i', 'r'};

    private final List<BookLine> m_lines = new LinkedList<>();
    private final StringIntCollection m_classes = new StringIntCollection();
    private final StringIntCollection m_tags = new StringIntCollection();
    private int m_position;

    public void parse(XmlReader reader) {
        Stack<BookLine> hierarchy = new Stack<>();
        int lineCount = 0;

        int eventType;
        try {
            eventType = reader.getEventType();

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlResourceParser.START_TAG: {
                        ByteCharSequence tag = (ByteCharSequence) reader.getName();

                        int position = reader.getPosition();
                        CharSequence attributes = null;
                        long tagMask = 0;
                        long classMask = 0;
                        boolean rtl = false;
                        BookLine parent = hierarchy.size() == 0 ? null : hierarchy.peek();

                        if (tag.length() > 0 && tag.charAt(0) != '?' && tag.charAt(0) != '!') {
                            ByteCharSequence nclass = (ByteCharSequence) reader.getAttribute(s_classBytes);
                            ByteCharSequence dir = (ByteCharSequence) reader.getAttribute(s_dirBytes);

                            if (dir != null && dir.toString().equals("rtl")) {
                                rtl = true;
                            }

                            attributes = reader.getAttributes();

                            if (attributes != null && attributes.length() > 0) {
                                int tcnt = 0;
                                if (nclass != null) {
                                    tcnt++;
                                }
                                if (dir != null) {
                                    tcnt++;
                                }

                                byte[] attrBytes = ((ByteCharSequence) attributes).getBytes();
                                int offset = ((ByteCharSequence) attributes).getOffset();

                                int cnt = 0;

                                for (int i = 0; i < attributes.length(); i++) {
                                    if (attrBytes[i + offset] == '=') {
                                        cnt++;
                                    }
                                }

                                if (cnt == tcnt) {
                                    attributes = null;
                                }
                            }

                            if (parent != null) {
                                classMask = parent.getClassMask();
                            }

                            if (nclass != null && nclass.length() > 0) {
                                byte[] classBytes = nclass.getBytes();
                                int offset = nclass.getOffset();
                                int i = nclass.length() - 1;
                                int end = nclass.length();

                                do {
                                    if (i == 0 || classBytes[i + offset] == ' ') {
                                        ByteCharSequence str = (ByteCharSequence) nclass.subSequence(i == 0 ? 0 : i + 1, end);

                                        /// !!!! HACK
                                        if (str.length() == 3 && str.toString().equals("rtl")) {
                                            rtl = true;
                                        }
                                        /// !!! HACK

                                        int pos = m_classes.get(str);

                                        if (pos == -1) {
                                            pos = m_classes.size();
                                            m_classes.put(str, pos);
                                        }

                                        classMask |= 1L << pos;
                                        end = i;
                                    }
                                } while (--i >= 0);
                            }

                            {
                                int pos = m_classes.get(tag);

                                if (pos == -1) {
                                    pos = m_classes.size();
                                    m_classes.put(tag, pos);
                                }

                                classMask |= 1L << pos;
                            }

                            if (parent != null)
                                tagMask = parent.getTagMask();

                            {
                                int pos = m_tags.get(tag);

                                if (pos == -1) {
                                    pos = m_tags.size();
                                    m_tags.put(tag, pos);
                                }

                                tagMask |= 1L << pos;
                            }
                        }

                        if (!rtl && parent != null && parent.isRtl()) {
                            rtl = true;
                        }

                        CharSequence text = reader.nextText();
                        BookLine line = new BookLine(tagMask, attributes, classMask, text, rtl, m_position + position, parent != null && parent.isEmpty());

                        m_lines.add(line);
                        lineCount++;
                        hierarchy.push(line);
                        break;
                    }
                    case XmlResourceParser.END_TAG: {
                        if (hierarchy.size() > 0) {
                            hierarchy.pop();
                        }
                        break;
                    }
                    case XmlResourceParser.TEXT: {
                        int position = reader.getPosition();
                        CharSequence text = reader.nextText();
                        if (hierarchy.size() > 0 && text != null && text.length() > 0) {
                            boolean delimiters = true;
                            byte[] textBytes = ((ByteCharSequence) text).getBytes();
                            int offset = ((ByteCharSequence) text).getOffset();

                            for (int i = 0; i < text.length(); i++) {
                                switch (textBytes[i + offset]) {
                                    case ' ':
                                    case '\r':
                                    case '\n':
                                    case '\0':
                                    case '\t':
                                        break;
                                    default:
                                        delimiters = false;
                                        break;
                                }
                                if (!delimiters) {
                                    break;
                                }
                            }

                            if (!delimiters) {
                                BookLine line = new BookLine(hierarchy.peek(), text, m_position + position);
                                m_lines.add(line);
                                lineCount++;
                            }
                        }
                        break;
                    }

                }
                eventType = reader.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (lineCount == 0)
            return;

        m_position += reader.getMaxPosition();
    }

    public BookData bake() {
        String[] sclasses = m_classes.toArray(new String[m_classes.size()]);
        String[] stags = m_tags.toArray(new String[m_tags.size()]);
        BookLine[] slines = m_lines.toArray(new BookLine[0]);

        m_lines.clear();

        return new BookData(slines, stags, sclasses, m_position);
    }

    private static class StringIntCollection {
        public static class Chunk {
            public final ByteCharSequence String;
            public final int Value;

            public Chunk(ByteCharSequence str, int value) {
                String = str;
                Value = value;
            }
        }

        private final Chunk[][] m_array;
        private int m_count;

        public int size() {
            return m_count;
        }

        public StringIntCollection() {
            m_array = new Chunk[26][];

            for (int i = 0; i < m_array.length; i++)
                m_array[i] = new Chunk[8];
        }

        public int get(ByteCharSequence key) {
            int firstChar = key.byteAt(0) - 97;

            if (firstChar < 0 || firstChar >= m_array.length - 1)
                firstChar = m_array.length - 1;

            Chunk[] array = m_array[firstChar];

            for (Chunk chunk : array) {
                if (chunk == null)
                    break;

                if (chunk.String.equals(key))
                    return chunk.Value;
            }
            return -1;
        }

        public void put(ByteCharSequence key, int value) {
            int firstChar = key.byteAt(0) - 97;

            if (firstChar < 0 || firstChar >= m_array.length - 1)
                firstChar = m_array.length - 1;

            Chunk[] array = m_array[firstChar];
            m_count++;

            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    array[i] = new Chunk(key, value);
                    break;
                }
            }
        }

        public String[] toArray(String[] array) {
            for (Chunk[] chunks : m_array) {
                for (Chunk chunk : chunks)
                    if (chunk != null)
                        array[chunk.Value] = chunk.String.toString();
            }
            return array;
        }
    }
}
