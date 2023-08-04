package zlobniyslaine.ru.ficbook.breader.bookParser;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class FontData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String m_name;
    private final int m_length;
    private InputStream m_stream;

    transient private String m_file;

    public String getName() {
        return m_name;
    }

    public FontData(String name, InputStream stream, int length) {
        m_stream = stream;
        m_length = length;

        if (name.contains("/")) {
            m_name = name.substring(name.lastIndexOf("/"));
        } else {
            m_name = name;
        }
    }

    public String extractFont(String path) {
        if (m_file != null) {
            return m_file;
        }

        try {
            m_file = path + "/" + m_name;
            File file = new File(m_file);
            if (file.exists() && file.length() == m_length) {
                m_stream = null; // clean up
                return m_file;
            }

            if (m_stream == null) {
                return null;
            }

            if (!file.createNewFile()){
                Log.w("FONTDATA", "cant create");
            }

            FileOutputStream stream = new FileOutputStream(m_file);

            byte[] buffer = new byte[0x1000];
            int pos = 0;
            while (m_stream.available() > 0 && pos < m_length) {
                int read = m_stream.read(buffer, 0, buffer.length);
                stream.write(buffer, 0, read);
                pos += read;
            }
            stream.close();

            m_stream = null; // clean up
            return m_file;
        } catch (IOException ex) {
            ex.printStackTrace();
            m_file = null;
        }
        return null;
    }
}
