package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;


@Table(name = "Tags")
public class Tags extends Model {

    /*
    category: "Формат"
description: "Работа написана в формате небольшой зарисовки описательного характера, чаще всего во втором лице и адресованная автором или персонажем самому читателю."
highlight: []
id: "838"
isAdult: false
isSpoiler: false
synonyms: [{id: 1018, title: "Имеджин"}, {id: 1019, title: "Imagines"}, {id: 1020, title: "Imagine your OTP"}]
0: {id: 1018, title: "Имеджин"}
1: {id: 1019, title: "Imagines"}
2: {id: 1020, title: "Imagine your OTP"}
title: "Имейджин"
title_highlighted: false
usage_count: 70
     */


    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "title", index = true)
    public String title;

    @Column(name = "category", index = true)
    public String category;

    @Column(name = "description")
    public String description;


    @Column(name = "adult")
    public Integer adult;

    @Column(name = "spoiler")
    public Integer spoiler;

    @Column(name = "highlighted")
    public Integer highlighted;

    @Column(name = "usage_count")
    public Integer usage_count;

    public static void Create() {
        SQLiteUtils.execSql("DELETE FROM Tags;");
    }

    public Tags() {
        super();
    }


    public static List<Tags> getAll() {
        return new Select()
                .from(Tags.class)
                .orderBy("title ASC")
                .execute();
    }

    public static List<Tags> getAllByCat(String cid) {
        return new Select()
                .from(Tags.class)
                .where("category = ?", cid)
                .orderBy("title ASC")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(Tags.class)
                .count();
    }

    public static Tags getById(String tag_id) {
        try {
            return new Select()
                    .from(Tags.class)
                    .where("nid = ?", tag_id)
                    .limit(1)
                    .executeSingle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCategoryId(String tag_id) {
        try {
            Tags t = new Select()
                    .from(Tags.class)
                    .where("nid = ?", tag_id)
                    .limit(1)
                    .executeSingle();
            if (t == null) {
                return "0";
            }
            TagsCategory tc = new Select()
                    .from(TagsCategory.class)
                    .where("title = ?", t.category)
                    .limit(1)
                    .executeSingle();
            if (tc != null) {
                return tc.nid;
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }


// --Commented out by Inspection START (16.07.20 22:46):
//    public static String getTagIdByNameFull(String name, String group_id) {
//        try {
//            String[] t = name.split("\\|");
//            Tags f = new Select()
//                    .from(Tags.class)
//                    .where("title = ?", t[0])
////                    .where("description = ?", t[1])
//                    .where("category = ?", group_id)
//                    .executeSingle();
//            return f.nid;
//        } catch (Exception e) {
//            return null;
//        }
//    }
// --Commented out by Inspection STOP (16.07.20 22:46)

    public static String getTagIdByName(String name) {
        try {
            String[] t = name.split("\\|");
            Tags f = new Select()
                    .from(Tags.class)
                    .where("title = ?", t[0])
                    .executeSingle();
            return f.nid;
        } catch (Exception e) {
            return null;
        }
    }


}
