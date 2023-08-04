package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.List;


@SuppressWarnings("WeakerAccess")
@Table(name = "Category")
public class Category extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.FAIL)
    public String nid;

    @Column(name = "Name", index = true)
    public String name;

    @Column(name = "Url")
    public String url;

    public static void Create() {
        try {
            SQLiteUtils.execSql("DELETE FROM Category;");
            SQLiteUtils.execSql("INSERT INTO Category (nid, Name, Url) VALUES " +
                    "(1,'Аниме и манга', 'anime_and_manga')," +
                    "(2,'Книги', 'books')," +
                    "(3,'Мультфильмы', 'cartoons')," +
                    "(4,'Игры', 'games')," +
                    "(5,'Фильмы и сериалы', 'movies_and_tv_series')," +
                    "(6,'Другое', 'other')," +
                    "(7,'Ориджиналы', 'originals')," +
                    "(9,'Известные люди (RPF)', 'rpf')," +
                    "(10,'Комиксы', 'comics')," +
                    "(11,'Мюзиклы', 'musicals');"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Category() {
        super();
    }

    public Category(String nid, String name, String url) {
        super();
        this.url = url;
        this.name = name;
        this.nid = nid;
    }

    public static String getIdByName(String search_name) {
        Category x = new Select()
                .from(Category.class)
                .where("name = ?", search_name)
                .limit(1)
                .executeSingle();
        if (x == null) {
            return "";
        } else {
            return x.nid;
        }
    }

    public static List<Category> getAll() {
        return new Select()
                .from(Category.class)
                .orderBy("Name ASC")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(Category.class)
                .count();
    }

}
