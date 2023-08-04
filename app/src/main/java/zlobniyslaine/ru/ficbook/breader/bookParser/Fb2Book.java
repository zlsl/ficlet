package zlobniyslaine.ru.ficbook.breader.bookParser;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import zlobniyslaine.ru.ficbook.breader.common.Base64InputStream;
import zlobniyslaine.ru.ficbook.breader.common.ByteCharSequence;
import zlobniyslaine.ru.ficbook.breader.common.Pair;
import zlobniyslaine.ru.ficbook.breader.common.XmlReader;

public class Fb2Book extends BaseBook {
    private final String m_fileName;

    public Fb2Book(String fileName) {
        m_title = fileName;
        m_fileName = fileName;
    }

    @Override
    public boolean init(String cachePath) {
        if (m_inited) {
            return false;
        }

        super.init(cachePath);

        try {
            InputStream stream = null;
            ZipFile zipFile = null;
            long size = 0;

            try {
                if (m_fileName.toLowerCase(Locale.getDefault()).endsWith(".zip")) {
                    zipFile = new ZipFile(m_fileName);

                    ZipEntry entry;
                    Enumeration<? extends ZipEntry> en = zipFile.entries();
                    while (en.hasMoreElements()) {
                        entry = en.nextElement();
                        size = entry.getSize();
                        if (size > 0) {
                            stream = new BufferedInputStream(zipFile.getInputStream(entry), 0x1000);
                            break;
                        }
                    }
                } else {
                    File file = new File(m_fileName);
                    size = file.length();
                    stream = new FileInputStream(file);
                }

                XmlReader xmlReader = new XmlReader(stream, (int) size);

                if (stream != null) {
                    stream.close();
                    stream = null;
                }

                if (zipFile != null) {
                    zipFile.close();
                    zipFile = null;
                }

                XmlBookParser bookParser = new XmlBookParser();
                bookParser.parse(xmlReader);

                BookData data = bookParser.bake();

                xmlReader.clean();

                String[] tags = data.getTags();
                BookLine[] lines = data.getLines();

                HashMap<String, Long> hashTags = new HashMap<>(tags.length);

                for (int i = 0; i < tags.length; i++) {
                    hashTags.put(tags[i], 1L << i);
                }

                Long fictionBookMask = hashTags.get("fictionbook");
                Long binaryMask = hashTags.get("binary");
                Long bookTitleMask = hashTags.get("book-title");
                Long coverPageMask = hashTags.get("coverpage");
                Long imageMask = hashTags.get("image");
                Long sectionMask = hashTags.get("section");
                Long bodyMask = hashTags.get("body");
                Long titleMask = hashTags.get("title");
                Long pMask = hashTags.get("p");

                if (bodyMask == null || sectionMask == null) {
                    Log.e("Reader", "No <body> and <section> tags in FB2 file!");
                    return true;
                }

                StringBuilder title = new StringBuilder();
                int imageBytes = 0;
                int bookEnd = 0;
                int bookStart = 0;
                boolean notes = false;
                LinkedList<BookLine> noteLines = null;

                for (int i = 0; i < lines.length; i++) {
                    BookLine line = lines[i];

                    long mask = line.getTagMask();

                    if (bookTitleMask != null && (mask & bookTitleMask) != 0) {
                        title.append(line.getText());
                        continue;
                    }

                    if (coverPageMask != null && (mask & (coverPageMask | imageMask)) == (coverPageMask | imageMask)) {
                        mask |= bodyMask;
                        line.setTagMask(mask);
                        lines[i + 1].setTagMask(fictionBookMask | bodyMask | sectionMask); // hacky, but works
                        continue;
                    }

                    if (binaryMask != null && (mask & binaryMask) != 0) {
                        String id = line.getAttribute("id");
                        String contentType = line.getAttribute("content-type");

                        if (id != null && contentType != null && contentType.startsWith("image")) {
                            ByteCharSequence bytes = ((ByteCharSequence) line.getText());

                            byte[] image = Base64InputStream.processBase64(bytes.getBytes(), bytes.getOffset(), bytes.length());
                            try {
                                ImageData imageData = new ImageData(id, image, 0, image.length);
                                imageData.init(cachePath);
                                m_images.add(imageData);
                            } catch (Exception ex) {
                                Log.w("Reader", "Failed to read image data " + ex);
                            }
                            Log.d("Reader", "Got FB2 image " + id);

                            imageBytes += bytes.length();
                        }
                        line.setText(null);
                        line.setAttributes(null);
                    }

                    if (!line.isPart() && (mask & (bodyMask | sectionMask)) == bodyMask) {
                        String name = line.getAttribute("name");
                        if (name != null) {
                            Log.d("Reader", "Got body name " + name);

                            if (name.equals("notes")) {
                                notes = true;
                            }
                        }
                    }

                    if (notes) {
                        if ((mask & (bodyMask | sectionMask)) == (bodyMask | sectionMask)) {
                            String id = line.getAttribute("id");

                            if (id != null) {
                                Log.d("Reader", "Got section " + id);
                                if (!m_notes.containsKey(id)) {
                                    m_notes.put(id, noteLines = new LinkedList<>());
                                }

                            } else if (noteLines != null && (mask & titleMask) == 0) {
                                noteLines.add(line);
                            }
                        }
                    } else {
                        if (titleMask != null && pMask != null) {
                            if ((mask & (sectionMask | titleMask | pMask)) == (sectionMask | titleMask | pMask) && line.getText() != null) {
                                m_chapters.add(new Pair<>((long) line.getPosition(), line.getText().toString()));
                            }
                        }

                        if ((mask & bodyMask) != 0) {
                            bookEnd = i == lines.length - 1 ? line.getPosition() : lines[i + 1].getPosition();
                            if (bookStart == 0) {
                                bookStart = line.getPosition();
                            }
                        }
                    }
                    line.optimize();
                }

                if (title.length() > 0) {
                    m_title = title.toString();
                }

                Log.d("Reader", "Images use " + imageBytes + " bytes");

                m_chapters.add(0, new Pair<>(0L, title.length() > 0 ? title.toString() : "Title Page"));
                if (m_chapters.size() == 1) {
                    m_chapters.add(new Pair<>((long) bookEnd, ""));
                }

                checkLanguage(data);
                m_reader = new Fb2BookReader(data, m_title);
                m_reader.setMaxSize(bookEnd - bookStart);
                m_reader.setBookStart(bookStart);
            } finally {
                if (zipFile != null) {
                    zipFile.close();
                }
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (Exception ex) {
            Log.e("Reader", "FB data retrieve failed: " + ex);
            ex.printStackTrace();
            return true;
        }

        return false;
    }
}
