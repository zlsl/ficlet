package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

@Table(name = "Fandoms")
public class Fandoms extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "title", index = true)
    public String title;

    @Column(name = "sec_title")
    public String sec_title;

    @Column(name = "slug")
    public String slug;

    @Column(name = "count", index = true)
    public String count;

    @Column(name = "group_id", index = true)
    public String group_id;

    public static void Create() {
        SQLiteUtils.execSql("DELETE FROM Fandoms;");
    }

    public Fandoms() {
        super();
    }

    private Fandoms(String nid, String title, String sec_title, String slug, String group_id, String count) {
        super();
        this.nid = nid;
        this.title = title;
        this.sec_title = sec_title;
        this.count = count;
        this.slug = slug;
        this.group_id = group_id;
    }

    public static List<Fandoms> getAll(String gid) {

        if (gid.isEmpty()) {
            return new Select()
                    .from(Fandoms.class)
                    .orderBy("title ASC")
                    .execute();
        } else {
            return new Select()
                    .from(Fandoms.class)
                    .where("group_id = ?", gid)
                    .orderBy("title ASC")
                    .execute();
        }
    }

    public static String getFandomIdByNameFull(String name, String group_id) {
        try {
            String[] t = name.split("\\|");
            Fandoms f = new Select()
                    .from(Fandoms.class)
                    .where("title = ?", t[0])
                    .where("sec_title = ?", t[1])
                    .where("count = ?", t[2])
                    .where("group_id = ?", group_id)
                    .executeSingle();
            return f.nid;
        } catch (Exception e) {
            return null;
        }
    }


    public static Fandoms getFandomById(String id) {
        try {
            return new Select()
                    .from(Fandoms.class)
                    .where("nid = ?", id)
                    .executeSingle();
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getCount(String gid) {
        if (gid.isEmpty()) {
            return new Select()
                    .from(Fandoms.class)
                    .count();
        } else {
            return new Select()
                    .from(Fandoms.class)
                    .where("group_id = ?", gid)
                    .count();
        }
    }
}
