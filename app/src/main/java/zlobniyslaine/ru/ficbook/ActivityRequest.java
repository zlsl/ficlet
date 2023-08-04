package zlobniyslaine.ru.ficbook;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Vector;

import zlobniyslaine.ru.ficbook.ajax.AjaxActionRequestBookmark;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionRequestLike;
import zlobniyslaine.ru.ficbook.controls.ViewAuthorWidget;
import zlobniyslaine.ru.ficbook.pagers.RequestPagerAdapter;


@SuppressWarnings("WeakerAccess")
public class ActivityRequest extends AppCompatActivity {

    private String id;
    private String title = "";

    ProgressBar pb1;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView tv_title;
    LinearLayout l_authors;
    LinearLayout l_btnbar;
    Button btn_like;
    Button btn_bookmark;

    void likeRequest() {
        AjaxActionRequestLike action = new AjaxActionRequestLike();
        action.Do(id, true);
        CoordinatorLayout mRoot = findViewById(R.id.coordl);
        Snackbar snackbar = Snackbar.make(mRoot, "Заявка «" + title + "» помечена как интересная", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    void bookmarkRequest() {
        AjaxActionRequestBookmark action = new AjaxActionRequestBookmark();
        action.Do(id, true);
        CoordinatorLayout mRoot = findViewById(R.id.coordl);
        Snackbar snackbar = Snackbar.make(mRoot, "Заявка «" + title + "» добавлена в закладки", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_request);

        pb1 = findViewById(R.id.pb1);
        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);
        tv_title = findViewById(R.id.tv_title);
        l_authors = findViewById(R.id.l_authors_layout);
        l_btnbar = findViewById(R.id.l_btnbar);
        btn_like = findViewById(R.id.btn_like);
        btn_bookmark = findViewById(R.id.btn_bookmark);

        btn_like.setOnClickListener(v -> likeRequest());

        btn_bookmark.setOnClickListener(v -> bookmarkRequest());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Информация"));
        tabLayout.addTab(tabLayout.newTab().setText("Работы"));

        Intent intent = getIntent();

        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");
        String urlTemplate = "https://ficbook.net/requests/" + id + "?p=@";

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Bundle bundle_info = new Bundle();
        Bundle bundle_fics = new Bundle();
        bundle_info.putString("url", urlTemplate);
        bundle_fics.putString("url", urlTemplate);

        List<Fragment> fragments = new Vector<>();
        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment_info = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentRequest_Info.class.getName());
        Fragment fragment_fics = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFanficList.class.getName());

        fragment_info.setArguments(bundle_info);
        fragment_fics.setArguments(bundle_fics);

        fragments.add(fragment_info);
        fragments.add(fragment_fics);

        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(new RequestPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        if (Application.isGuest()) {
            l_btnbar.setVisibility(View.GONE);
        }

        pb1.setVisibility(View.GONE);
    }

    public void setInfo(String title, String author_id, String author_name, String avatar_url) {
        tv_title.setText(title);
        ViewAuthorWidget add_author = new ViewAuthorWidget(this);
        add_author.setAuthorInfo(author_id, author_name, "", avatar_url);
        l_authors.addView(add_author);
    }
}
