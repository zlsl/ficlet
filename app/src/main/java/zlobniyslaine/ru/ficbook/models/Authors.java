package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

@Table(name = "Authors")
public class Authors extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "name", index = true)
    public String name;

    @Column(name = "avatar_url")
    public String avatar_url;


    public static void Create() {
        SQLiteUtils.execSql("DELETE FROM Authors;");
    }

    public Authors() {
        super();
    }

    public Authors(String nid, String name, String avatar_url) {
        super();
        this.nid = nid;
        this.name = name;
        this.avatar_url = avatar_url;
    }

    public static List<Authors> getAll() {
        return new Select()
                .from(Authors.class)
                .orderBy("nid")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(Authors.class)
                .count();
    }

    public static Authors getById(String author_id) {
        try {
            return new Select()
                    .from(Authors.class)
                    .where("nid = ?", author_id)
                    .limit(1)
                    .executeSingle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Authors getByName(String author_name) {
        try {
            return new Select()
                    .from(Authors.class)
                    .where("name = ?", author_name)
                    .limit(1)
                    .executeSingle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
