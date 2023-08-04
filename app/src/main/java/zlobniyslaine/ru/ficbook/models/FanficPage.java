package zlobniyslaine.ru.ficbook.models;


import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.sql.Date;
import java.util.List;

@Table(name = "FanficPage")
public class FanficPage extends Model {

    @Column(name = "nid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String nid;

    @Column(name = "timestamp", index = true)
    private Date timestamp;

    @Column(name = "date_read", index = true)
    public String date_read;

    @Column(name = "page_number")
    public Integer page_number;

    @Column(name = "scroll_position")
    public Integer scroll_position;

    @Column(name = "scroll_max")
    public Integer scroll_max;

    @Column(name = "audio_page_number")
    public Integer audio_page_number;

    @Column(name = "page_count")
    public Integer page_count;

    @Column(name = "file_size")
    public Integer file_size;


    public static void Create() {
        SQLiteUtils.execSql("DROP TABLE IF EXISTS FanficPage;");
        SQLiteUtils.execSql(SQLiteUtils.createTableDefinition(Cache.getTableInfo(FanficPage.class)));
    }

    public FanficPage() {
        super();
    }

    public static FanficPage getLastPage(String fanfic_id) {
        return new Select()
                .from(FanficPage.class)
                .where("nid = ?", fanfic_id)
                .limit(1)
                .executeSingle();
    }

    public static List<FanficPage> getAll() {
        return new Select()
                .from(FanficPage.class)
                .orderBy("date_read DESC")
                .execute();
    }

    public static Integer getCount() {
        return new Select()
                .from(FanficPage.class)
                .count();
    }

}
