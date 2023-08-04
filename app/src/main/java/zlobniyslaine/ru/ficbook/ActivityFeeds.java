package zlobniyslaine.ru.ficbook;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import zlobniyslaine.ru.ficbook.adapters.AdapterFeeds;
import zlobniyslaine.ru.ficbook.models.Feeds;


public class ActivityFeeds extends AppCompatActivity {

    RecyclerView rv_feeds;
    List<Feeds> flist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_feeds);
        rv_feeds = findViewById(R.id.rv1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        flist = Feeds.getAll();
        rv_feeds.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        AdapterFeeds rv_fadapter = new AdapterFeeds(this, flist);
        rv_feeds.setAdapter(rv_fadapter);

        Button btn_def = findViewById(R.id.btn_def);
        btn_def.setOnClickListener(view -> {
            Application.AddDefaultFeeds();
            flist.clear();
            flist.addAll(Feeds.getAll());
            rv_fadapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
