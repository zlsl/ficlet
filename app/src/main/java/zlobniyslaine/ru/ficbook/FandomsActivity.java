package zlobniyslaine.ru.ficbook;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.adapters.AdapterFandoms;
import zlobniyslaine.ru.ficbook.models.Fandoms;


public class FandomsActivity extends AppCompatActivity {

    private final ArrayList<HashMap<String, Object>> FandomList = new ArrayList<>();
    private AdapterFandoms rv_adapter;

    RecyclerView rv1;
    ProgressBar pb1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouplist);

        rv1 = findViewById(R.id.rv1);
        pb1 = findViewById(R.id.pb1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new Thread(this::StartLoaders).start();
    }

    private void StartLoaders() {
        Intent intent = getIntent();
        String group_id = intent.getStringExtra("group_id");
        String title = intent.getStringExtra("name");
        String url = intent.getStringExtra("group_url");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        Log.i("Loading", group_id + " - " + url + " - " + title);

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(this);

        rv1.setLayoutManager(mLayoutManager);

        rv_adapter = new AdapterFandoms(this, FandomList);
        rv1.setAdapter(rv_adapter);

        if (Fandoms.getCount(group_id) > 0) {
            Log.d("DB", "From DB");

            List<Fandoms> fdm = Fandoms.getAll(group_id);
            for (Fandoms f : fdm) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", f.nid);
                map.put("url", f.slug);
                map.put("title", f.title);
                map.put("count", f.count);
                map.put("secondary_title", f.sec_title);
                FandomList.add(map);
            }

            rv_adapter.snapshot();
            runOnUiThread(() -> Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                rv_adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                rv_adapter.filter(newText);
                return true;
            }
        });
        return true;
    }

}
