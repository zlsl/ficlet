package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.sql.Date;
import java.util.List;

@Table(name = "Parts")
public class Parts extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "fanfic_id", index = true)
    public String fanfic_id;

    @Column(name = "title", index = true)
    public String title;

    @Column(name = "created", index = true)
    public String created;

    @Column(name = "position", index = true)
    public Integer position;

    @Column(name = "timestamp", index = true)
    public Date timestamp;

    @Column(name = "date_read", index = true)
    public String date_read;

    @Column(name = "page_number")
    public Integer page_number;

    @Column(name = "file_size")
    public Integer file_size;


    public static void Create() {
        SQLiteUtils.execSql("DROP TABLE IF EXISTS Parts;");
        SQLiteUtils.execSql(SQLiteUtils.createTableDefinition(Cache.getTableInfo(Parts.class)));
    }

    public Parts() {
        super();
    }

    public static Parts getPart(String fanfic_id, String part_id) {
        return new Select()
                .from(Parts.class)
                .where("nid = ?", part_id)
                .limit(1)
                .executeSingle();
    }

    public static List<Parts> getParts(String fanfic_id) {
        return new Select()
                .from(Parts.class)
                .where("fanfic_id = ?", fanfic_id)
                .execute();
    }

    public static String getIdByName(String name, String fid) {
        try {
            Parts p = new Select()
                    .from(Parts.class)
                    .where("title = ?", name)
                    .where("fanfic_id = ?", fid)
                    .executeSingle();
            return p.nid;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getNextPart(String fanfic_id, String part_id) {
        Parts current = getPart(fanfic_id, part_id);
        Parts n = new Select()
                .from(Parts.class)
                .where("fanfic_id = ?", fanfic_id)
                .where("position > ?", current.position)
                .limit(1)
                .orderBy("position")
                .executeSingle();
        if (n != null) {
            return n.nid;
        } else {
            return "";
        }
    }

    public static String getLastReadedPartId(String fanfic_id) {
        try {
            Parts p = new Select()
                    .from(Parts.class)
                    .where("fanfic_id = ?", fanfic_id)
                    .where("date_read != ''")
                    .orderBy("nid DESC")
                    .limit(1)
                    .executeSingle();
            return p.nid;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Parts> getAll() {
        return new Select()
                .from(Parts.class)
                .orderBy("position")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(Parts.class)
                .count();
    }

}
