package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;


@Table(name = "TagsCategory")
public class TagsCategory extends Model {


    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "title", index = true)
    public String title;

    public static void Create() {
        SQLiteUtils.execSql("DELETE FROM TagsCategory;");
    }

    public TagsCategory() {
        super();
    }


    public static List<TagsCategory> getAll() {
        return new Select()
                .from(TagsCategory.class)
                .orderBy("title ASC")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(TagsCategory.class)
                .count();
    }

    public static TagsCategory getById(String id) {
        try {
            return new Select()
                    .from(TagsCategory.class)
                    .where("nid = ?", id)
                    .limit(1)
                    .executeSingle();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getIdByName(String search_name) {
        TagsCategory x = new Select()
                .from(TagsCategory.class)
                .where("title = ?", search_name)
                .limit(1)
                .executeSingle();
        if (x == null) {
            return "";
        } else {
            return x.nid;
        }
    }
}
