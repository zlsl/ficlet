package zlobniyslaine.ru.ficbook.breader.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class InputByteStream {
    private int m_position;
    private int m_size;
    private byte[] m_buffer;
    private CustomCharset m_charset;

    public int getSize() {
        return m_size;
    }

    public int getPosition() {
        int result = m_position;
        if (m_position >= m_size) {
            result = m_size;
        }

        return result;
    }

    public byte[] getBuffer() {
        return m_buffer;
    }

    public CustomCharset getCharset() {
        return m_charset;
    }

    public InputByteStream(InputStream stream, int size) {
        m_charset = SystemCharset.defaultCharset();
        int pos = 0;

        try {
            m_buffer = new byte[size];

            if (stream != null) {
                while (stream.available() > 0 && pos < size) {
                    pos += stream.read(m_buffer, pos, size - pos);
                }
            }

            m_size = pos;

        } catch (IOException e) {
            e.printStackTrace();
            m_buffer = null;
            m_size = 0;
        }
    }

    public void clean() {
        m_buffer = null;
    }

    public byte peekByte() {
        if (m_position >= m_size)
            return 0;

        return m_buffer[m_position];
    }

    public boolean skipTo(byte stop/*, boolean advance*/) {
        for (int i = m_position; i < m_size; i++) {
            if (m_buffer[i] == stop) {
                m_position = i + 1;
                return true;
            }
        }
        m_position = m_size;
        return false;
    }

    public void reset(int position) {
        m_position = position;
    }

    public ByteCharSequence getString(int start, int length) {
        if (start + length > m_size)
            length = m_size - start;

        return new ByteCharSequence(m_buffer, start, length, m_charset);
    }

    public ByteCharSequence getAsciiString(int start, int length) {
        if (start + length > m_size)
            length = m_size - start;

        return new ByteCharSequence(m_buffer, start, length, null);
    }

    public ByteCharSequence getAsciiLowerString(int start, int length) {
        for (int i = 0; i < length; i++) {
            byte b = m_buffer[start + i];
            if (b >= 65 && b <= 90)
                m_buffer[start + i] = (byte) (b + 32);
        }
        return new ByteCharSequence(m_buffer, start, length, null);
    }

    public void setCharset(String name) {
        try {
            if (name.equalsIgnoreCase("windows-1251") || name.equalsIgnoreCase("CP1251")) {
                m_charset = new Charset1251();
            } else {
                m_charset = new SystemCharset(Charset.forName(name));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            m_charset = SystemCharset.defaultCharset();
        }
    }

    public void replaceChars(int start, int length, byte[] byteArray) {
        for (int i = 0; i < length; i++) {
            m_buffer[start + i] = i >= byteArray.length ? 0 : byteArray[i];
        }

    }
}