package zlobniyslaine.ru.ficbook;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import zlobniyslaine.ru.ficbook.controls.MLRoundedImageView;
import zlobniyslaine.ru.ficbook.pagers.ProfilePagerAdapter;


public class ActivityProfile extends AppCompatActivity {

    Fragment fragment_works;

    FloatingActionButton fab_newfic;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView tv_author_name;
    MLRoundedImageView iv_author_avatar;

    void newPart() {
        Intent intent = new Intent(Application.getContext(), ActivityFanficNew.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_profile);

        fab_newfic = findViewById(R.id.fab_newfic);
        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);
        tv_author_name = findViewById(R.id.tv_author_name);
        iv_author_avatar = findViewById(R.id.iv_author_avatar);

        fab_newfic.setOnClickListener(v -> newPart());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Информация"));
        tabLayout.addTab(tabLayout.newTab().setText("Работы"));
        tabLayout.addTab(tabLayout.newTab().setText("Соавтор"));
        tabLayout.addTab(tabLayout.newTab().setText("Бета"));
        tabLayout.addTab(tabLayout.newTab().setText("В избранном"));
        tabLayout.addTab(tabLayout.newTab().setText("Сборники"));
        tabLayout.addTab(tabLayout.newTab().setText("Отзывы"));
        tabLayout.addTab(tabLayout.newTab().setText("Мои отзывы"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                animateFab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tv_author_name.setText(Application.sPref.getString("user_name", ""));

        try {
            if (!Objects.requireNonNull(Application.sPref.getString("user_avatar", "")).isEmpty()) {
                Picasso.with(this)
                        .load(Application.sPref.getString("user_avatar", ""))
                        .into(iv_author_avatar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Fragment> fragments = new Vector<>();
        FragmentManager fm = getSupportFragmentManager();


        Bundle bundle_info = new Bundle();
        bundle_info.putString("url", "https://ficbook.net/authors/" + Application.sPref.getString("user_id", ""));
        Fragment fragment_info = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentProfile_Info.class.getName());
        fragment_info.setArguments(bundle_info);
        fragments.add(fragment_info);

        Bundle bundle_works = new Bundle();
        bundle_works.putString("url", "https://ficbook.net/home/myfics");
        fragment_works = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentWorks_List.class.getName());
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

        Bundle bundle_favs = new Bundle();
        bundle_favs.putString("id", Application.sPref.getString("user_id", ""));
        Fragment fragment_favs = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentProfile_Favourites.class.getName());
        fragment_favs.setArguments(bundle_favs);
        fragments.add(fragment_favs);

        Fragment fragment_collections = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentMain_Collections.class.getName());
        fragments.add(fragment_collections);

        Bundle bundle_reviews = new Bundle();
        bundle_reviews.putString("id", "");
        bundle_reviews.putString("url", "https://ficbook.net/home/comments?p=@");
        Fragment fragment_reviews = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Review.class.getName());
        fragment_reviews.setArguments(bundle_reviews);
        fragments.add(fragment_reviews);

        Bundle bundle_oreviews = new Bundle();
        bundle_oreviews.putString("id", "");
        bundle_oreviews.putString("url", "https://ficbook.net/authors/" + Application.user_id + "/comments?p=@");
        Fragment fragment_oreviews = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Review.class.getName());
        fragment_oreviews.setArguments(bundle_oreviews);
        fragments.add(fragment_oreviews);

        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(new ProfilePagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private void animateFab(int position) {
        switch (position) {
            case 1:
                fab_newfic.show();
                break;
            case 0:
            case 2:
            case 4:
            case 3:
            default:
                fab_newfic.hide();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment_works.onResume();
    }
}
