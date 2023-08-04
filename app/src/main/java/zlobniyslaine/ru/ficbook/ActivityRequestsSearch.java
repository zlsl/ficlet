package zlobniyslaine.ru.ficbook;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import zlobniyslaine.ru.ficbook.pagers.SearchPagerAdapter;


public class ActivityRequestsSearch extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    Button btn_search;

    void startSearch() {
        try {
            Log.i("SU", getUrl());
            Intent intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
            intent.putExtra("url", getUrl());
            intent.putExtra("title", "Результаты поиска");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Fragment fragment_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_requests_search);

        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);
        btn_search = findViewById(R.id.btn_search);

        btn_search.setOnClickListener(v -> startSearch());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Поиск");
        }

        Objects.requireNonNull(getSupportActionBar()).hide();

        tabLayout.addTab(tabLayout.newTab().setText("Основное"));

        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = new Vector<>();
        fragment_main = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentRequestsSearchMain.class.getName());

        fragments.add(fragment_main);

        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(new SearchPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private String getUrl() {
        String tmp = "";
        tmp = tmp + ((FragmentRequestsSearchMain) fragment_main).getUrl();
        return tmp;
    }
}

/*
https://ficbook.net/requests?
status: 0
0 - active
1 - done
-1 - archive


title: test******************
fandom_ids[]: 112437
ratings[]: 5
ratings[]: 6
ratings[]: 7
ratings[]: 8
ratings[]: 9

directions[]: 1
directions[]: 2
directions[]: 3
directions[]: 4
directions[]: 7
directions[]: 6
directions[]: 5

genres[]: 50
genres_ignore[]: 47

warnings[]: 12
warnings_ignore[]: 81

sort: 1
1 - by waiters
2 - by date

find: Найти!
 */