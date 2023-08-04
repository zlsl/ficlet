package zlobniyslaine.ru.ficbook;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Vector;

import zlobniyslaine.ru.ficbook.pagers.WorksPagerAdapter;


public class ActivityWorks extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_works);

        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Работы"));
        tabLayout.addTab(tabLayout.newTab().setText("Соавтор"));
        tabLayout.addTab(tabLayout.newTab().setText("Бета"));
        tabLayout.addTab(tabLayout.newTab().setText("Части"));
        tabLayout.addTab(tabLayout.newTab().setText("Запросы"));

        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = new Vector<>();

        Bundle bundle_works = new Bundle();
        bundle_works.putString("url", "https://ficbook.net/home/myfics");
        Fragment fragment_works = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentWorks_List.class.getName());
        fragment_works.setArguments(bundle_works);
        fragments.add(fragment_works);

        Bundle bundle_co = new Bundle();
        bundle_co.putString("url", "https://ficbook.net/home/roled_fics?role=coauthor");
        Fragment fragment_co = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentWorks_List.class.getName());
        fragment_co.setArguments(bundle_co);
        fragments.add(fragment_co);

        Bundle bundle_beta = new Bundle();
        bundle_beta.putString("url", "https://ficbook.net/home/roled_fics?role=beta");
        Fragment fragment_beta = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentWorks_List.class.getName());
        fragment_beta.setArguments(bundle_beta);
        fragments.add(fragment_beta);

        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(new WorksPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
