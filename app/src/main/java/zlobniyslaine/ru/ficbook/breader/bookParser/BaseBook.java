package zlobniyslaine.ru.ficbook.breader.bookParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zlobniyslaine.ru.ficbook.breader.common.Pair;

public abstract class BaseBook {
    public static final HashMap<String, Boolean> s_rtlLanguages = new HashMap<>();

    static {
        s_rtlLanguages.put("ar", true); // Arabic
        s_rtlLanguages.put("dv", true); // Divehi
        s_rtlLanguages.put("ha", true); // Hausa
        s_rtlLanguages.put("he", true); // Hebrew
        s_rtlLanguages.put("fa", true); // Persian (Farsi)
        s_rtlLanguages.put("ps", true); // Pashto
        s_rtlLanguages.put("ur", true); // Urdu
        s_rtlLanguages.put("yi", true); // Yiddish

    }

    protected Map<String, Integer> m_styles = new HashMap<>();
    protected final Map<String, List<BookLine>> m_notes = new HashMap<>();
    protected List<Pair<Long, String>> m_chapters = new ArrayList<>(32);
    protected List<FontData> m_fonts = new ArrayList<>(2);
    protected List<ImageData> m_images = new ArrayList<>();
    protected float m_firstLineIdent = 55.0f;
    protected String m_title;
    protected String m_language;
    protected boolean m_inited;
    protected BaseBookReader m_reader;
    // --Commented out by Inspection (16.07.20 23:08):protected boolean m_checkDir = false;

    public float getFirstLine() {
        return m_firstLineIdent;
    }

    public Map<String, Integer> getStyles() {
        return m_styles;
    }

    public List<Pair<Long, String>> getChapters() {
        return m_chapters;
    }

    public List<FontData> getFonts() {
        return m_fonts;
    }

    public List<ImageData> getImages() {
        return m_images;
    }

    public BaseBookReader getReader() {
        return m_reader;
    }

    public Map<String, List<BookLine>> getNotes() {
        return m_notes;
    }

    public boolean init(String cachePath) {
        m_styles = new HashMap<>();
        m_chapters = new ArrayList<>(32);
        m_fonts = new ArrayList<>();
        m_images = new ArrayList<>();
        m_firstLineIdent = 55.0f;
        m_inited = true;
        return false;
    }

    protected void checkLanguage(BookData data) {
        if (m_language == null)
            for (int i = 0; i < 5; i++) {
                BookLine line = data.getLine(i);
                String lang = line.getAttribute("xml:lang");
                if (lang == null)
                    lang = line.getAttribute("lang");

                if (lang != null) {
                    m_language = lang;
                    break;
                }
            }

        if (m_language != null && s_rtlLanguages.containsKey(m_language)) {
            //m_checkDir = true;
            m_firstLineIdent = 0;
        }
    }
}