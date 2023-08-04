package zlobniyslaine.ru.ficbook.breader.common;

import org.jetbrains.annotations.NotNull;

public class FixedStringBuilder {
    private final char[] m_chars;
    private int m_position;

    public int length() {
        return m_position;
    }

    public FixedStringBuilder(int length) {
        m_position = 0;
        m_chars = new char[length + 1];
    }

    public void append(CharSequence value) {
        append(FixedCharSequence.toFixedCharSequence(value));
    }

    public void append(FixedCharSequence value) {
        int len = value.length();
        System.arraycopy(value.getChars(), value.getOffset(), m_chars, m_position, len);
        m_position += len;
    }

    public void append(char value) {
        m_chars[m_position] = value;
        m_position++;
    }

    @NotNull
    public String toString() {
        //m_chars[m_position] = 0;
        return new String(m_chars);
    }

    public FixedCharSequence toCharSequence() {
        return new FixedCharSequence(m_chars, 0, m_position);
    }

    public FixedCharSequence toCharSequence(int offset) {
        return new FixedCharSequence(m_chars, offset, m_position - offset);
    }

// --Commented out by Inspection START (16.07.20 22:34):
//    public boolean equalsPart(int from, int to, CharSequence value) {
//        return equalsPart(from, to, value, value.length());
//    }
// --Commented out by Inspection STOP (16.07.20 22:34)

    public boolean equalsPart(int from, int to, CharSequence value, int len) {
        if (len != to - from)
            return false;

        while (--len >= 0)
            if (value.charAt(len) != m_chars[from + len])
                return false;

        return true;
    }
}
