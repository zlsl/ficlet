package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;

@Table(name = "Fanfic")
public class Fanfic extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "title")
    public String title;

    @Column(name = "sup")
    public String sup;

    @Column(name = "authors")
    public String authors;

    @Column(name = "direction")
    public String direction;

    @Column(name = "pairings")
    public String pairings;

    @Column(name = "fandom")
    public String fandom;

    @Column(name = "rating")
    public String rating;

    @Column(name = "genres")
    public String genres;

    @Column(name = "cautions")
    public String cautions;

    @Column(name = "tags")
    public String tags;

    @Column(name = "size")
    public String size;

    @Column(name = "sizetype")
    public String sizetype;

    @Column(name = "pages")
    public String pages;

    @Column(name = "parts")
    public String parts;

    @Column(name = "status")
    public String status;

    @Column(name = "info")
    public String info;

    @Column(name = "collection_id")
    public String collection_id;

    @Column(name = "new_part")
    public String new_part;

    @Column(name = "date_changes")
    public String date_changes;

    @Column(name = "new_content")
    public String new_content;

    @Column(name = "bad")
    public String bad;

    @Column(name = "critic")
    public String critic;

    @Column(name = "bookmark")
    public String bookmark;

    @Column(name = "trophy")
    public String trophy;


    public static void Create() {
        SQLiteUtils.execSql("DROP TABLE IF EXISTS Fanfic;");
        SQLiteUtils.execSql(SQLiteUtils.createTableDefinition(Cache.getTableInfo(Fanfic.class)));
    }

    public Fanfic() {
        super();
    }

    private Fanfic(String nid) {
        super();
        this.nid = nid;
    }

    public static Integer getCount() {
        return new Select()
                .from(Fanfic.class)
                .count();
    }

    public static String getCollectionId(String fanfic_id) {
        Fanfic f = new Select()
                .from(Fanfic.class)
                .where("nid = ?", fanfic_id)
                .limit(1)
                .executeSingle();
        if (f != null) {
            if (f.collection_id != null) {
                return f.collection_id;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void setCollectionId(String fanfic_id, String collection_id) {
        Fanfic f = new Select()
                .from(Fanfic.class)
                .where("nid = ?", fanfic_id)
                .limit(1)
                .executeSingle();
        if (f != null) {
            f.collection_id = collection_id;
            f.save();
        }
    }

    public static void clearFlags(String fanfic_id) {
        Fanfic f = null;
        try {
            f = new Select()
                    .from(Fanfic.class)
                    .where("nid = ?", fanfic_id)
                    .limit(1)
                    .executeSingle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (f != null) {
            f.new_part = "";
            f.save();
        }
    }

    public static Fanfic getById(String fanfic_id) {
        try {
            return new Select()
                    .from(Fanfic.class)
                    .where("nid = ?", fanfic_id)
                    .limit(1)
                    .executeSingle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Fanfic> getAll() {
        return new Select()
                .from(Fanfic.class)
                .orderBy("title ASC")
                .execute();
    }
}
