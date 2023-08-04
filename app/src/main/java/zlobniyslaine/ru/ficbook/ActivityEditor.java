package zlobniyslaine.ru.ficbook;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.activeandroid.ActiveAndroid;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.controls.ViewAuthorWidget;
import zlobniyslaine.ru.ficbook.models.Parts;
import zlobniyslaine.ru.ficbook.models.Tags;
import zlobniyslaine.ru.ficbook.pagers.EditorMainPagerAdapter;


@SuppressWarnings("WeakerAccess")
public class ActivityEditor extends AppCompatActivity {

    private Context context;
    private String id;
    private String UrlTemplate;
    private final ArrayList<HashMap<String, Object>> FicParts = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> FicFandoms = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> FicGenres = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> FicPairings = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> FicCharacters = new ArrayList<>();
    private final ArrayList<HashMap<String, Object>> FicCautions = new ArrayList<>();

    FloatingActionButton fab;
    FloatingActionButton fab_addpart;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView tv_title;
    TextView tv_direction;
    LinearLayout l_direction;
    LinearLayout l_authors;

    public void open_fanfic() {
        Intent intent = new Intent(context, ActivityFanfic.class);
        intent.putExtra("id", id);
        intent.putExtra("title", "");
        context.startActivity(intent);
    }

    public void add_part() {
        try {
            Intent intent = new Intent(this, ActivityPartEditor.class);
            intent.putExtra("fanfic_id", id);
            this.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_editor);

        fab = findViewById(R.id.fab);
        fab_addpart = findViewById(R.id.fab_addpart);
        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);
        tv_title = findViewById(R.id.tv_title);
        tv_direction = findViewById(R.id.tv_direction);
        l_authors = findViewById(R.id.l_authors_layout);
        l_direction = findViewById(R.id.l_direction);

        fab.setOnClickListener(v -> open_fanfic());
        fab_addpart.setOnClickListener(v -> add_part());

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Информация"));
        tabLayout.addTab(tabLayout.newTab().setText("Содержание"));
        tabLayout.addTab(tabLayout.newTab().setText("Отзывы"));
        tabLayout.addTab(tabLayout.newTab().setText("Сборники"));
        tabLayout.addTab(tabLayout.newTab().setText("Статистика"));

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


        Intent intent = getIntent();

        id = intent.getStringExtra("id");
        tv_title.setText(intent.getStringExtra("title"));
        UrlTemplate = "https://ficbook.net/readfic/" + id;

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new fetcher_main(this).execute();
    }

    private void animateFab(int position) {
        switch (position) {
            case 0:
                fab.show();
                fab_addpart.hide();
                break;
            case 1:
                fab.hide();
                fab_addpart.show();
                break;
            case 2:
            case 4:
            case 3:
                fab.hide();
                fab_addpart.hide();
                break;
        }
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityEditor> activityReference;

        fetcher_main(ActivityEditor context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;
        String bodyx = null;

        @Override
        protected Void doInBackground(String... params) {
            if (Application.isInternetAvailable()) {
                try {
                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(activityReference.get().UrlTemplate)).execute();
                    ResponseBody body = response.body();

                    if (body != null) {
                        bodyx = body.string();
                        response.close();
                        Log.i("LOAD", "From network");
                    }
                } catch (SocketTimeoutException e) {
                    Application.displayPopup("Сервер не отвечает");
                } catch (UnknownHostException e) {
                    Application.displayPopup("Проблемы с соединением");
                } catch (IOException e) {
                    Application.displayPopup("Ошибка загрузки");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if ((bodyx == null)) {
                Log.w("LOAD", "Failed network/rawcache");
                return null;
            }
            doc = Jsoup.parse(bodyx);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (activityReference.get() != null) {
                    activityReference.get().ParseFicbook(doc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseFicbook(Document doc) {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = new Vector<>();

        Fragment fragment_info = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Info.class.getName());
        Fragment fragment_reviews = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Review.class.getName());
        Fragment fragment_parts = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentWork_Parts.class.getName());
        Fragment fragment_collections = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Collections.class.getName());
        Fragment fragment_stat = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentWork_Stat.class.getName());

        Bundle bundle_info = new Bundle();
        Bundle bundle_reviews = new Bundle();
        Bundle bundle_parts = new Bundle();
        Bundle bundle_stat = new Bundle();
        Bundle bundle_collections = new Bundle();

        bundle_collections.putString("id", id);
        bundle_reviews.putString("url", "https://ficbook.net/collections/" + id + "/list");
        bundle_reviews.putString("id", id);
        bundle_reviews.putString("url", "https://ficbook.net/readfic/" + id + "/comments?p=@");
        bundle_stat.putString("url", "https://ficbook.net/home/myfics/" + id + "/stats");
        bundle_parts.putString("id", id);

        String id = "0";
        String f_direction;
        int dir_color = 0;
        ArrayList<HashMap<String, Object>> FicTags = new ArrayList<>();

        try {
            if (doc.select("input[name=fanfic_id]").first() != null) {
                id = doc.select("input[name=fanfic_id]").first().val();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (doc.select("a.give_reward_fic") != null) {
                doc.select("a.give_reward_fic").remove();
            }
            if (doc.getElementById("give_fanfic_reward_div") != null) {
                doc.getElementById("give_fanfic_reward_div").remove();
            }

            Elements hat = doc.select("section.fanfic-hat");
            Elements toc = doc.select("ul.list-of-fanfic-parts li");
            Elements info = doc.select("div.fanfic-main-info");

            Elements tmp;

            tmp = hat.select("div.tags").select("a");
            for (Element v : tmp) {
                HashMap<String, Object> map = new HashMap<>();
                String txt = v.text();
                if (v.hasClass("tag-adult")) {
                    txt = txt + " \uD83D\uDD1E";
                }

                String tag_id = v.attr("href").replace("/tags/", "");
                String tc_id = Tags.getCategoryId(tag_id);

                map.put("title", txt);
                map.put("url", tag_id);
                map.put("hint", v.attr("title"));

                switch (tc_id) {
                    case "25":
                        FicGenres.add(map);
                        break;
                    case "26":
                        FicCautions.add(map);
                        break;
                    default:
                        FicTags.add(map);
                }
            }

            Elements dt = hat.select("div.description div.mb-5");
            for (Element i : dt) {
                String section_title = i.select("Strong").text();
                switch (section_title) {
                    case "Рейтинг:":
                        bundle_info.putString("rating", i.select("div").html());
                        break;
                    case "Размер:":
                        bundle_info.putString("size", i.select("div").get(0).html());
                        break;
                    case "Статус:":
                        bundle_info.putString("status", i.select("div").html());
                        break;
                    case "Примечания автора:":
                        bundle_info.putString("author_comment", i.select("div").get(0).html());
                        break;
                    case "Посвящение:":
                        bundle_info.putString("belong", i.select("div").get(0).html());
                        break;
                    case "Работа написана по заявке:":
                        bundle_info.putString("request", i.select("div").get(0).html());
                        break;
                    case "Публикация на других ресурсах:":
                        bundle_info.putString("publication", i.select("div").get(0).html());
                        break;
                    case "Описание:":
                        bundle_info.putString("description", i.select("div").get(0).html());
                        break;
                    case "Основные персонажи:":
                        Elements pairs = i.select("a");
                        for (Element p : pairs) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("title", p.text());
                            map.put("url", p.attr("href"));
                            FicCharacters.add(map);
                        }
                        break;

                    case "Пэйринг:":
                    case "Пэйринг или персонажи:":
                    case "Пэйринг и персонажи:":
                        Elements pairs2 = i.select("a");
                        for (Element p : pairs2) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("title", p.text());
                            map.put("url", p.attr("href"));
                            FicCharacters.add(map);
                        }
                        break;

                    default:
                }
            }

            Elements fandoms = info.select("a");
            for (Element v : fandoms) {
                String f_url = v.attr("href");
                String f_title = v.text();
                HashMap<String, Object> map = new HashMap<>();
                map.put("title", f_title);
                map.put("url", f_url);
                FicFandoms.add(map);
            }

            Elements authors = hat.select(".hat-creator-container");
            for (Element v : authors) {
                final String a_name = v.select("a.creator-nickname").text();
                String a_url = v.select("img").attr("src");
                String a_role = v.select("i").text();
                String a_id = v.select("a").attr("href").replace("/authors/", "");

                l_authors.post(() -> {
                    ViewAuthorWidget add_author = new ViewAuthorWidget(this);
                    add_author.setAuthorInfo(a_id, a_name, a_role, a_url);
                    l_authors.addView(add_author);
                });

                //authors_list = authors_list + a_name + " ";
            }

            f_direction = info.select("section").select(".badge-with-icon").get(0).text().trim();
            bundle_info.putString("rating", info.select("section").select(".badge-with-icon").get(1).text().trim());
            bundle_info.putString("status", info.select("section").select(".badge-with-icon").get(2).text().trim());

            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-gen")) {
                dir_color = context.getResources().getColor(R.color.fbg_gen);
            }
            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-het")) {
                dir_color = context.getResources().getColor(R.color.fbg_het);
            }
            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-other")) {
                dir_color = context.getResources().getColor(R.color.fbg_other);
            }
            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-slash")) {
                dir_color = context.getResources().getColor(R.color.fbg_slash);
            }
            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-femslash")) {
                dir_color = context.getResources().getColor(R.color.fbg_femslash);
            }
            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-mixed")) {
                dir_color = context.getResources().getColor(R.color.fbg_mixed);
            }
            if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-article")) {
                dir_color = context.getResources().getColor(R.color.fbg_article);
            }

            l_direction.setBackgroundColor(dir_color);
            tv_direction.setText(f_direction);

            ActiveAndroid.beginTransaction();

            String last_part_id = Parts.getLastReadedPartId(id);
            Log.i("LPID", last_part_id + "!");

            int pos = 0;
            HashMap<String, Object> map;
            for (Element v : toc) {
                String topic_title = v.select("a").get(0).text().trim();
                String topic_time = v.select("span").text().trim();
                String topic_id = v.select("a").attr("href").replace("readfic", "").replace(id, "").replace("/", "").replace("#part_content", "");

                map = new HashMap<>();
                map.put("title", topic_title);
                map.put("time", topic_time);
                map.put("fanfic_id", id);
                map.put("part_id", topic_id);

                Parts col = Parts.getPart(id, topic_id);
                if (col == null) {
                    col = new Parts();
                    col.title = topic_title;
                    col.fanfic_id = id;
                    col.nid = topic_id;
                } else {
                    col.title = topic_title;
                }
                col.created = topic_time;
                col.position = pos;
                col.save();
                pos++;
                FicParts.add(map);
            }

            if (FicParts.size() == 0) {
                map = new HashMap<>();
                map.put("title", "Главы не определены");
                map.put("time", "Можно читать весь фанфик");
                map.put("fanfic_id", id);
                map.put("part_id", "");
                FicParts.add(map);
            }

            bundle_info.putSerializable("fandoms", FicFandoms);
            bundle_info.putSerializable("genres", FicGenres);
            bundle_info.putSerializable("characters", FicCharacters);
            bundle_info.putSerializable("pairings", FicPairings);
            bundle_info.putSerializable("cautions", FicCautions);
            bundle_info.putSerializable("tags", FicTags);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ActiveAndroid.inTransaction()) {
                ActiveAndroid.setTransactionSuccessful();
                ActiveAndroid.endTransaction();
            }
        }


        fragment_info.setArguments(bundle_info);
        fragment_reviews.setArguments(bundle_reviews);
        fragment_parts.setArguments(bundle_parts);
        fragment_stat.setArguments(bundle_stat);
        fragment_collections.setArguments(bundle_collections);

        fragments.add(fragment_info);
        fragments.add(fragment_parts);
        fragments.add(fragment_reviews);
        fragments.add(fragment_collections);
        fragments.add(fragment_stat);

        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(new EditorMainPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }
}
