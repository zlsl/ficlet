package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

import zlobniyslaine.ru.ficbook.Application;


@Table(name = "Collections")
public class Collections extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "title", index = true)
    public String title;

    @Column(name = "count", index = true)
    public String count;

    @Column(name = "author", index = true)
    public String author;

    @Column(name = "locked")
    public String locked;

    public static void Create() {
        SQLiteUtils.execSql("DELETE FROM Collections;");
    }

    public Collections() {
        super();
    }

    private Collections(String nid, String title, String count, String author, String locked) {
        super();
        this.nid = nid;
        this.title = title;
        this.count = count;
        this.locked = locked;
        this.author = author;
    }

    public static String getById(String id) {
        try {
            Collections f = new Select()
                    .from(Collections.class)
                    .where("nid = ?", id)
                    .executeSingle();
            return f.title;
        } catch (Exception e) {
            return "";
        }
    }

    public static void prepare(String collection_id) {
        SQLiteUtils.execSql("UPDATE Fanfic SET collection_id='' WHERE collection_id=" + collection_id + ";");
    }

    public static List<Collections> getAll() {
        return new Select()
                .from(Collections.class)
                .orderBy("title ASC")
                .execute();
    }

    public static List<Collections> getOwnAll() {
        return new Select()
                .from(Collections.class)
                .where("author = ?", Application.getUser_name())
                .orderBy("title ASC")
                .execute();
    }


// --Commented out by Inspection START (01.11.20 13:00):
//    public static Integer getCount() {
//        return new Select()
//                .from(Collections.class)
//                .count();
//    }
// --Commented out by Inspection STOP (01.11.20 13:00)
}
