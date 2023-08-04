package zlobniyslaine.ru.ficbook;


import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zlobniyslaine.ru.ficbook.models.Fanfic;

public class YO {
    private static ArrayList<CharSequence> noyo_words;
    private static ArrayList<CharSequence> yo_words;
    private static HashMap<Character, Integer> map_begin;
    private static HashMap<Character, Integer> map_end;

    private static ArrayList<CharSequence> tts_words;
    private static ArrayList<CharSequence> tts_u_words;
    private static HashMap<Character, Integer> tts_map_begin;
    private static HashMap<Character, Integer> tts_map_end;


    private final static String INDENT_SPACES = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";


    static void init() {
        if (!Application.sPref.getBoolean("yo_fix", false)) {
            return;
        }
        if (yo_words != null) {
            if (yo_words.size() > 0) {
                return;
            }
        }

        tts_words = new ArrayList<>();
        tts_u_words = new ArrayList<>();
        tts_map_begin = new HashMap<>();
        tts_map_end = new HashMap<>();

        yo_words = new ArrayList<>();
        noyo_words = new ArrayList<>();
        map_begin = new HashMap<>();
        map_end = new HashMap<>();

        new Thread(
                () -> {
                    char current = ' ';
                    int idx = -1;
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(Application.getContext().getAssets().open("yo.dic")));
                        String mLine;
                        while ((mLine = reader.readLine()) != null) {
                            idx++;
                            if (mLine.charAt(0) != current) {
                                map_end.put(current, idx);
                                map_begin.put(mLine.charAt(0), idx);
                                current = mLine.charAt(0);
                            }
                            try {
                                yo_words.add(mLine);
                                noyo_words.add(mLine.replace("ё", "е"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        map_end.put(current, idx);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.i("YO", "dict loaded " + yo_words.size() + " words");
                    Log.i("YO", "test: " + fix("Мертвая зеленая веселая мышка упала под ежика!"));
                }
        ).start();

        new Thread(
                () -> {
                    char current = ' ';
                    int idx = -1;
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(Application.getContext().getAssets().open("tts.dic")));
                        String mLine;
                        while ((mLine = reader.readLine()) != null) {
                            String[] blk = mLine.split("\\|");
                            String w1 = blk[0].trim();
                            String w2 = blk[1].trim();
                            idx++;
                            if (w1.charAt(0) != current) {
                                tts_map_end.put(current, idx);
                                tts_map_begin.put(w1.charAt(0), idx);
                                current = w1.charAt(0);
                            }
                            tts_u_words.add(w2);
                            tts_words.add(w1);
                        }
                        tts_map_end.put(current, idx);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Log.i("TTS", "dict loaded " + tts_words.size() + " words");
                    Log.i("TTS", "test: " + fixtts("аберрация тест слово ."));
                }
        ).start();
    }

    private static CharSequence upperFirst(CharSequence s) {
        if (s.length() == 0) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0))
                    + s.subSequence(1, s.length()).toString();
        }
    }

    static String fix(String input) {
        long start = System.currentTimeMillis();
        int replaces = 0;

        init();

        try {
            StringBuilder sb = new StringBuilder();
            String[] stream = input.split("\\b");
            boolean found;
            boolean upper;
            CharSequence ww;
            for (String w : stream) {
                found = false;
                if (w.length() > 1) {
                    if ((w.contains("е")) || (w.contains("Е"))) {
                        ww = w;
                        upper = Character.isUpperCase(ww.charAt(0));
                        if (upper) {
                            ww = w.toLowerCase(Locale.getDefault());
                        }
                        int i_begin;
                        if (map_begin.get(ww.charAt(0)) != null) {
                            i_begin = map_begin.get(ww.charAt(0));
                        } else {
                            i_begin = 1;
                            Log.e("YO", "null1");
                        }
                        int i_end;
                        if (map_end.get(ww.charAt(0)) != null) {
                            i_end = map_end.get(ww.charAt(0));
                        } else {
                            i_end = 1;
                            Log.e("YO", "null2");
                        }

                        if (ww.charAt(0) == 'е') {
                            i_end = map_end.get('ё');
                        }
                        for (int i = i_begin; i < i_end; i++) {
                            if (TextUtils.equals(ww, noyo_words.get(i))) {
                                if (upper) {
                                    sb.append(upperFirst(yo_words.get(i)));
                                } else {
                                    sb.append(yo_words.get(i));
                                }

                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    sb.append(w);
                } else {
                    replaces++;
                }
            }
            long end = System.currentTimeMillis();
            Log.i("YO", (end - start) + " ms, " + replaces + " replaced");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return input;
        }
    }

    static String fixtts(String input) {
        long start = System.currentTimeMillis();
        int replaces = 0;

        init();

        try {
            StringBuilder sb = new StringBuilder();
            String[] stream = input.split("\\b");
            boolean found;
            for (String w : stream) {
                found = false;
                if (w.length() > 1) {
                    int i_begin;
                    if (tts_map_begin.get(w.charAt(0)) != null) {
                        i_begin = tts_map_begin.get(w.charAt(0));
                    } else {
                        i_begin = 1;
                        Log.e("YO", "null1");
                    }
                    int i_end;
                    if (tts_map_end.get(w.charAt(0)) != null) {
                        i_end = tts_map_end.get(w.charAt(0));
                    } else {
                        i_end = 1;
                        Log.e("YO", "null2");
                    }

                    for (int i = i_begin; i < i_end; i++) {
                        if (TextUtils.equals(w, tts_words.get(i))) {
                            sb.append(tts_u_words.get(i));
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    sb.append(w);
                } else {
                    replaces++;
                }
            }
            long end = System.currentTimeMillis();
            Log.i("TTS", (end - start) + " ms, " + replaces + " replaced");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return input;
        }
    }


    public static String html2fb2(String input, String id) {

        Fanfic f = Fanfic.getById(id);

        if (f == null) {
            return "";
        }

        String dblock = "<description><title-info>\n" +
                "<book-title>" + f.title + "</book-title>\n" +
                "<annotation>\n" +
                "<p><strong>Направленность:</strong>" + f.direction + "</p>\n" +
                "<p><strong>Автор:</strong>" + f.authors + "</p>\n" +
                "<p><strong>Фэндом:</strong>" + f.fandom + "</p>\n" +
                "<p><strong>Пэйринг и персонажи:</strong>" + f.pairings + "</p>\n" +
                "<p><strong>Размер:</strong>" + f.sizetype + ", " + f.pages + "</p>\n" +
                "<p><strong>Статус:</strong>" + f.status + "</p>\n" +
                "<p><strong>Метки:</strong>" + f.tags + "</p>\n" +
                "<p><strong>Примечания автора:</strong></p>\n" +
                "<p><strong>Описание:</strong>" + f.info + "</p>\n" +
                "</annotation>\n" +
                "<date value=\"1869-01-01 00:00:00\">2001-01-01 01:01:01</date>\n" +
                "</title-info></description>\n";

        String tmp = input;
        tmp = tmp.replace("\n", "</p><p>");
        tmp = tmp.replaceAll("<h1(.*?)>", "<section>\n<title>\n<p>");
        tmp = tmp.replace("</h1>", "</p>\n</title>\n<p>");
        tmp = tmp.replaceAll("<h2(.*?)>", "<section>\n<title>\n<p>");
        tmp = tmp.replace("</h2>", "</p>\n</title>\n<p>");
        tmp = tmp.replaceAll("<div(.*?)>", "");
        tmp = tmp.replace("</div>", "");

        tmp = tmp.replace("<br>", "</p><p>");

        tmp = tmp.replace("<b>", "<strong>");
        tmp = tmp.replace("</b>", "</strong>");
        tmp = tmp.replace("<i>", "<emphasis>");
        tmp = tmp.replace("</i>", "</emphasis>");
        tmp = tmp.replace("<strike>", "<strikethrough>");
        tmp = tmp.replace("</strike>", "</strikethrough>");
        tmp = tmp.replace("<s>", "<strikethrough>");
        tmp = tmp.replace("</s>", "</strikethrough>");
        tmp = tmp.replace("&nbsp;", "");

        tmp = tmp.replaceAll("<p(.*?)>", "<p>");
        tmp = tmp.replace("<p>***</p>", "<subtitle>* * *</subtitle>");

        tmp = tmp.replaceAll("/<h[4-6]([^>]*?)>/is", "<subtitle\1>");

        tmp = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<FictionBook xmlns=\"http://www.gribuser.ru/xml/fictionbook/2.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "    <stylesheet type=\"text/css\">\n" +
                "        .body{font-family : Verdana, Geneva, Arial, Helvetica, sans-serif;} .p{margin:0.5em 0 0 0.3em; padding:0.2em;\n" +
                "        text-align:justify;}\n" +
                "    </stylesheet>\n" +
                dblock +
                "<body><section><title>" + f.title + "</title>\n" +
                tmp +
                "</p></section></body></FictionBook>";

        tmp = tmp.replace("<p></p>", "");
        tmp = tmp.replace("<p><section>", "<section>");
        tmp = tmp.replace("</p><section>", "<section>");

        return tmp;
    }

    public static String cleanRaw(String input) {
        String tmp = input;
        tmp = tmp.replace("\n\n", "\n");
        tmp = tmp.replace("\n\n", "\n");
        tmp = tmp.replace("\n\n", "\n");
        tmp = tmp.replace("\n\n", "\n");
        tmp = tmp.replace("\n", "<br>");

        tmp = tmp.replace(" style=\"margin-bottom: 0px;\"", "");
        tmp = tmp.replace("<br />", "<br>");
        tmp = tmp.replace("<div>", "<br>");
        tmp = tmp.replace("<hr />", "<br>");
        tmp = tmp.replace("<hr>", "<br>");
        tmp = tmp.replace("<div class=\"part_list\">", "<br>");
        tmp = tmp.replace("<p align=\"center\" style=\"margin: 0px;\">", "<p>");
        tmp = tmp.replace("<p align=\"right\" style=\"margin: 0px;\">", "<p>");
        tmp = tmp.replace("<p>", "");
        tmp = tmp.replace("</p>", "");
        tmp = tmp.replace("<div class=\"part_text\">", "<br>");
        tmp = tmp.replace("</div>", "<br>");
        tmp = tmp.replace("<h1>", "<b>");
        tmp = tmp.replace("<h2>", "<b>");
        tmp = tmp.replace("</h1>", "</b><br>");
        tmp = tmp.replace("</h2>", "</b><br>");
        tmp = tmp.replace("<br>\n<br>\n<br>\n<br>\n<br>\n<br>\n<br>\n", "<br>\n");
        tmp = tmp.replace("<br>\n<br>\n", "<br>\n");
        tmp = tmp.replace("&nbsp;", " ");
        tmp = tmp.replace(" <br>", "<br>");
        tmp = tmp.replace(" <br>", "<br>");
        tmp = tmp.replace("\n<br>\n", "<br>");
        tmp = tmp.replace("\n<br>\n", "<br>");
        tmp = tmp.replace("<br><br>", "<br>");
        tmp = tmp.replace("<br><br>", "<br>");
        tmp = tmp.replace("<br><br>", "<br>");
        tmp = tmp.replace("<br><br>", "<br>");
        tmp = tmp.replace("<br><br>", "<br>");
        tmp = tmp.replace("<br><br>", "<br>");
        tmp = tmp.replace("<br>\n", "\n");
        tmp = tmp.replaceAll("([\\r\\n\\t])", "");
        tmp = tmp.replace("<br>", "\n");
        tmp = tmp.replace("\n \n", "\n");
        tmp = tmp.replace(" \n \n", "\n");

        return tmp;
    }

    public static String Typograf(String input) {
        StringBuilder sb = new StringBuilder();

        try {
            String[] stream = input.split("\n");

            for (String w : stream) {
                String tmp = w.trim();

                if (tmp.contains("\"")) {
                    tmp = tmp.replaceAll("\"([\\w\\s—.:,!?\\-]+)\"", "«$1»");
                }

                if (tmp.contains("-")) {
                    tmp = tmp.replace("--", "-");
                    tmp = tmp.replace(",-", ", —");
                    tmp = tmp.replace(", -", ", —");
                    tmp = tmp.replace(".-", ". —");
                    tmp = tmp.replace(". -", ". —");
                    tmp = tmp.replace("!-", "! —");
                    tmp = tmp.replace("! -", "! —");
                    tmp = tmp.replace("?-", "? —");
                    tmp = tmp.replace("? -", "? —");
                }

                if (tmp.startsWith("-")) {
                    tmp = "—" + tmp.substring(1);
                }
                if (tmp.startsWith("—")) {
                    if (!tmp.startsWith("— ")) {
                        tmp = "— " + tmp.substring(1);
                    }
                }
                sb.append(INDENT_SPACES).append(tmp).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return input;
        }
        return sb.toString();
    }

    public static String createTags(String source) {
        String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(source);
        String tmp = source;
        while (m.find()) {
            if ((m.group().contains("jpg")) || (m.group().contains("png"))) {
                tmp = tmp.replace(m.group(), "<br /><img src=\"" + m.group() + "\"><br />");
            } else {
                tmp = tmp.replace(m.group(), "<a href='" + m.group() + "'>" + m.group() + "</a>");
            }
        }
        return tmp;
    }

// --Commented out by Inspection START (16.07.20 22:37):
//    public static String PrepareHtml(String input) {
//        String tmp = "<!DOCTYPE html><html lang='ru'><head><meta charset='utf-8'><style>* {font-size: 14px;}h2 {font-size: 16px;} p {text-indent: 20px;text-align: justify;}</style></head><body>" +
//                input +
//                "</body></html>";
//
//        return tmp;
//    }
// --Commented out by Inspection STOP (16.07.20 22:37)

    public static String unescape(String s) {
        int i = 0, len = s.length();
        char c;
        StringBuilder sb = new StringBuilder(len);
        while (i < len) {
            c = s.charAt(i++);
            if (c == '\\') {
                if (i < len) {
                    c = s.charAt(i++);
                    if (c == 'u') {
                        c = (char) Integer.parseInt(s.substring(i, i + 4), 16);
                        i += 4;
                    } // add other cases here as desired...
                }
            } // fall through: \ escapes itself, quotes any character but u
            sb.append(c);
        }
        return sb.toString();
    }

}
