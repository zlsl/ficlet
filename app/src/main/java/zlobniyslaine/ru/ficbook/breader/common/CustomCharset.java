package zlobniyslaine.ru.ficbook.breader.common;

import java.nio.CharBuffer;

public abstract class CustomCharset {
    public abstract CharBuffer decode(byte[] buffer, int start, int length);

// --Commented out by Inspection START (16.07.20 22:32):
//    public String processString(String str) {
//        if (str == null || str.length() == 0)
//            return str;
//
//        Log.d("TextReader", "Processing string " + str);
//
//        byte[] bytes;
//        bytes = str.getBytes(StandardCharsets.UTF_8); // UTF-7
//        return decode(bytes, 0, bytes.length).toString();
//    }
// --Commented out by Inspection STOP (16.07.20 22:32)
}
