package zlobniyslaine.ru.ficbook.breader.textReader;

import org.jetbrains.annotations.NotNull;

public class FontInfo {
    public static final int NORMAL = 0;
    public static final int ITALIC = 1;
    public static final int BOLD = 2;
    public static final int BOLD_ITALIC = 2;

    public final String Name;
    public final String Typeface;
    public final String File;
    public final String Path;
    public final String ID;

    public FontInfo(String id, String file, String name, String typeface) {
        ID = id;
        Name = name;
        File = file;
        Path = file.substring(0, file.lastIndexOf('/'));
        Typeface = typeface;
    }

    @NotNull
    @Override
    public String toString() {
        return ID;
    }
}