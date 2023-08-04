package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

@Table(name = "Feeds")
public class Feeds extends Model {

    @Column(name = "title", index = true)
    public String title;

    @Column(name = "url", index = true)
    public String url;

    @Column(name = "chk", index = true)
    public String auto;

    @Column(name = "hash")
    public String hash;

    public static void Create() {
        SQLiteUtils.execSql("DROP TABLE IF EXISTS Feeds;");
        SQLiteUtils.execSql(SQLiteUtils.createTableDefinition(Cache.getTableInfo(Feeds.class)));
    }

    public Feeds() {
        super();
    }

    private Feeds(String title, String url, String auto, String hash) {
        super();
        this.title = title;
        this.url = url;
        this.auto = auto;
        this.hash = hash;
    }

    public static Feeds getById(Integer id) {
        return new Select()
                .from(Feeds.class)
                .where("Id = ?", id)
                .executeSingle();
    }

    public static List<Feeds> getAll() {
        return new Select()
                .from(Feeds.class)
                .orderBy("title ASC")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(Feeds.class)
                .count();
    }
}
