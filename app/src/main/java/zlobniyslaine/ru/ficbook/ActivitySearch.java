package zlobniyslaine.ru.ficbook;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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


public class ActivitySearch extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    Button btn_search;

    void startSearch() {
        try {
            Log.i("SU", getUrl());
            Intent intent = new Intent(getApplicationContext(), ActivityFanficList.class);
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
        setContentView(R.layout.activity_search);

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
        StartLoaders();
    }

    private void StartLoaders() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = new Vector<>();
        fragment_main = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentSearchMain.class.getName());

        fragments.add(fragment_main);

        viewPager.setOffscreenPageLimit(10);
        try {
            viewPager.setAdapter(new SearchPagerAdapter(getSupportFragmentManager(), fragments));
        } catch (Exception e) {
            e.printStackTrace();
        }

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setVisibility(View.GONE);
    }

    private String getUrl() {
        String tmp = "";
        tmp = tmp + ((FragmentSearchMain) fragment_main).getUrl();
        return tmp;
    }
}
