package zlobniyslaine.ru.ficbook.breader.common;


import android.content.res.XmlResourceParser;
import android.util.Log;

import java.io.InputStream;
import java.util.HashMap;

public class XmlReader {
    private final static byte[] s_quoteBytes = {'\"'};
    private final static byte[] s_xmlBytes = {'?', 'x', 'm', 'l'};
    private final static byte[] s_encodingBytes = {'e', 'n', 'c', 'o', 'd', 'i', 'n', 'g'};

    private final static HashMap<ByteCharSequence, ByteCharSequence> s_replacementStrings = new HashMap<>();

    private static void registerReplacement(String pattern, String value) {
        s_replacementStrings.put(new ByteCharSequence(pattern), new ByteCharSequence(value));
    }

    static {
        registerReplacement("&nbsp;", "\u00a0");
        registerReplacement("&lt;", "<");
        registerReplacement("&gt;", ">");
        registerReplacement("&amp;", "&");
        registerReplacement("&quot;", "\"");
        registerReplacement("&agrave;", "à");
        registerReplacement("&Agrave;", "À");
        registerReplacement("&acirc;", "â");
        registerReplacement("&auml;", "ä");
        registerReplacement("&Auml;", "Ä");
        registerReplacement("&Acirc;", "Â");
        registerReplacement("&aring;", "å");
        registerReplacement("&Aring;", "Å");
        registerReplacement("&aelig;", "æ");
        registerReplacement("&AElig;", "Æ");
        registerReplacement("&ccedil;", "ç");
        registerReplacement("&Ccedil;", "Ç");
        registerReplacement("&eacute;", "é");
        registerReplacement("&Eacute;", "É");
        registerReplacement("&egrave;", "è");
        registerReplacement("&Egrave;", "È");
        registerReplacement("&ecirc;", "ê");
        registerReplacement("&Ecirc;", "Ê");
        registerReplacement("&euml;", "ë");
        registerReplacement("&Euml;", "Ë");
        registerReplacement("&iuml;", "ï");
        registerReplacement("&Iuml;", "Ï");
        registerReplacement("&ocirc;", "ô");
        registerReplacement("&Ocirc;", "Ô");
        registerReplacement("&ouml;", "ö");
        registerReplacement("&Ouml;", "Ö");
        registerReplacement("&oslash;", "ø");
        registerReplacement("&Oslash;", "Ø");
        registerReplacement("&szlig;", "ß");
        registerReplacement("&ugrave;", "ù");
        registerReplacement("&Ugrave;", "Ù");
        registerReplacement("&ucirc;", "û");
        registerReplacement("&Ucirc;", "Û");
        registerReplacement("&uuml;", "ü");
        registerReplacement("&Uuml;", "Ü");
        registerReplacement("&copy;", "\u00a9");
        registerReplacement("&reg;", "\u00ae");
        registerReplacement("&euro;", "\u20a0");
        registerReplacement("&#8211;", "\u2013");
        registerReplacement("&#8212;", "\u2014");
        registerReplacement("&#8217;", "\u2019");
        registerReplacement("&#8220;", "\u201C");
        registerReplacement("&#8221;", "\u201D");
        registerReplacement("&#160;", "\u00a0");
        registerReplacement("&#169;", "\u00a9");
        registerReplacement("&#38;", "\u0026");
        registerReplacement("&#39;", "\u0027");
    }

    private int m_eventType;
    private ByteCharSequence m_tag;
    private ByteCharSequence m_attributes;
    private boolean m_closedTag;
    private InputByteStream m_stream;

    public int getEventType() {
        return m_eventType;
    }

    public int getPosition() {
        return m_stream.getPosition();
    }

    public int getMaxPosition() {
        return m_stream.getSize();
    }

    public CharSequence getName() {
        return m_tag;
    }

    public CharSequence getAttributes() {
        return m_attributes;
    }

    public CharSequence getAttribute(byte[] name) {
        if (m_attributes == null || m_attributes.length() == 0)
            return null;

        int nattribute = m_attributes.indexOf(name, 0);

        if (nattribute == -1)
            return null;

        int nquote = m_attributes.indexOf(s_quoteBytes, nattribute + 1);
        if (nquote == -1)
            return null;

        int equote = m_attributes.indexOf(s_quoteBytes, nquote + 1);
        if (equote == -1)
            return null;

        return m_attributes.subSequence(nquote + 1, equote);
    }



    public XmlReader(InputStream stream, int length)// throws IOException
    {
        m_stream = new InputByteStream(stream, length);
        m_eventType = XmlResourceParser.START_DOCUMENT;
    }


    private int readTag() {
        int position = m_stream.getPosition();
        int size = m_stream.getSize();
        byte[] buffer = m_stream.getBuffer();

        int tagType = XmlResourceParser.START_TAG;
        int tagStart = position;
        int tagEnd = 0;

        while (position < size) {
            byte b = buffer[position++];//m_stream.readByte();

            switch (b) {
                case '>':
                    if (tagType == XmlResourceParser.START_TAG && position - tagStart > 1 && buffer[position - 2] == '/') {
                        m_tag = m_stream.getAsciiLowerString(tagStart, tagEnd == 0 ? position - tagStart - 2 : tagEnd - tagStart - 1);
                        if (tagEnd == 0)
                            m_attributes = null;
                        else
                            m_attributes = m_stream.getString(tagEnd, position - tagEnd - 2);

                        //Log.d("TextReader", "Got self-closed tag " + m_tag);
                        m_closedTag = true;
                    } else {
                        m_tag = m_stream.getAsciiLowerString(tagStart, tagEnd == 0 ? position - tagStart - 1 : tagEnd - tagStart - 1);

                        if (tagEnd == 0)
                            m_attributes = null;
                        else
                            m_attributes = m_stream.getString(tagEnd, position - tagEnd - 1);

                        //Log.d("TextReader", "Got tag " + m_tag + ", type " + tagType + ", tag end " + tagEnd);
                        m_closedTag = false;
                    }

                    if (tagStart < 10 && m_tag.indexOf(s_xmlBytes, 0) == 0) {
                        CharSequence encoding = getAttribute(s_encodingBytes);

                        Log.i("Reader", "Got xml header: " + m_attributes);

                        if (encoding != null && encoding.length() > 0) {
                            //Log.i("Reader", "Encoding is " + encoding);
                            m_stream.setCharset(encoding.toString());
                        }
                    }

                    m_stream.reset(position);
                    return tagType;

                case '/':
                    if (position - tagStart <= 1) {
                        tagStart = position;
                        tagType = XmlResourceParser.END_TAG;
                    }
                    break;
                case '<':
                    //if (position - tagStart <= 1)
                {
                    tagStart = position;
                    tagEnd = 0;
                }
                break;
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    if (tagEnd == 0)
                        tagEnd = position;
                    break;
            }
        }
        //m_stream.setCharset("UTF-8");
        m_stream.reset(position);
        return XmlResourceParser.END_DOCUMENT;
    }

    private int nextTag() {
        if (m_stream.skipTo((byte) '<')) {
            return readTag();
        }

        return m_eventType = XmlResourceParser.END_DOCUMENT;
    }

    public CharSequence nextText() {
        if (m_eventType == XmlResourceParser.START_TAG && m_closedTag) {
            return null;
        }

        if (m_stream.peekByte() == '<') {
            return null;
        }

        int start = m_stream.getPosition();
        int position = start;
        int size = m_stream.getSize();
        byte[] buffer = m_stream.getBuffer();

        int specialStart = -1;

        while (position < size) {
            byte b = buffer[position++];

            switch (b) {
                case '<':
                    m_eventType = XmlResourceParser.TEXT;
                    //Log.d("TextReader", "Got < while parsing text!");
                    m_stream.reset(position - 1);
                    return m_stream.getString(start, position - start - 1);
                case '&':
                    specialStart = position;
                    //Log.d("TextReader", "Got & symbol!");
                    break;
                case ';':
                    if (specialStart != -1 && position - specialStart < 10) {
                        ByteCharSequence seq = m_stream.getAsciiString(specialStart - 1, position - specialStart + 1);

                        ByteCharSequence replacement;

                        replacement = s_replacementStrings.get(seq);
                        if (replacement != null) {
                            m_stream.replaceChars(specialStart - 1, position - specialStart + 1, replacement.getBytes());
                        }
                        specialStart = -1;
                    }
                    break;
            }
        }

        m_stream.reset(position);
        m_eventType = XmlResourceParser.END_DOCUMENT;
        return m_stream.getString(start, position - start);
    }

    public int next() {
        try {
            switch (m_eventType) {
                case XmlResourceParser.TEXT:
                    return m_eventType = readTag();
                case XmlResourceParser.START_TAG:
                    if (m_closedTag) {
                        m_closedTag = false;
                        return m_eventType = XmlResourceParser.END_TAG;
                    }
                    if (m_stream.peekByte() != '<')
                        return m_eventType = XmlResourceParser.TEXT;
                    break;
                case XmlResourceParser.END_TAG:
                    if (m_stream.peekByte() != '<')
                        return m_eventType = XmlResourceParser.TEXT;
                    break;
                case XmlResourceParser.END_DOCUMENT:
                    return XmlResourceParser.END_DOCUMENT;
            }
            return m_eventType = nextTag();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return m_eventType = XmlResourceParser.END_DOCUMENT;
    }

    // --Commented out by Inspection START (16.07.20 22:37):
//    public void reset(int position) {
//        m_stream.reset(position);
//        m_eventType = XmlResourceParser.START_DOCUMENT;
//        m_closedTag = false;
//        m_tag = null;
//        m_attributes = null;
//        //m_simpleTag = false;
//    }
// --Commented out by Inspection STOP (16.07.20 22:37)

// --Commented out by Inspection START (16.07.20 22:37):
//    public boolean skipTo(String pattern) {
//        byte[] bytes = pattern.getBytes();
//        //char [] chars = new char[pattern.length()];
//        //pattern.getChars(0, pattern.length()-1, chars, 0);
//
//
//        if (m_stream.skipTo(bytes, false)) {
//            //Log.d("TextReader", "Skip to pattern " + pattern + ", result true, position " + m_stream.getPosition());
//            m_eventType = XmlResourceParser.START_DOCUMENT;
//            m_closedTag = false;
//            m_tag = null;
//            m_attributes = null;
//            return true;
//        } else {
//            //Log.d("TextReader", "Skip to pattern " + pattern + ", result false");
//            m_eventType = XmlResourceParser.END_DOCUMENT;
//            return false;
//        }
//    }
// --Commented out by Inspection STOP (16.07.20 22:37)

    public void clean() {
        m_eventType = XmlResourceParser.START_DOCUMENT;
        m_closedTag = false;
        m_tag = null;
        m_attributes = null;
        m_stream.clean();
        m_stream = null;
    }
}
