package zlobniyslaine.ru.ficbook;

import static zlobniyslaine.ru.ficbook.Application.FICLET_HOST;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.activeandroid.ActiveAndroid;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.models.Fanfic;
import zlobniyslaine.ru.ficbook.models.Tags;


public class FragmentFanficList extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private ArrayList<HashMap<String, Object>> Fics = new ArrayList<>();
    private FicSimpleAdapter rv_adapter;

    RecyclerView rv1;
    ProgressBar pb1;
    private SwipeRefreshLayout swipeContainer;

    private Boolean Loading = false;
    private Integer loaded = 0;
    private Integer added = 0;
    private Integer total_added = 0;
    private Boolean refresh = true;
    private Integer StartPosition = 0;
    private Integer MaxPosition = 100000;
    private String UrlTemplate = "";
    private String UrlTemplate2 = "";
    private Context context;
    private Boolean show_cache = false;
    private Boolean ficlet_loaded = false;
    private String cid = "";

    private Boolean filter_gen;
    private Boolean filter_het;
    private Boolean filter_other;
    private Boolean filter_slash;
    private Boolean filter_femslash;
    private Boolean filter_mixed;
    private Boolean filter_article;
    private Boolean filter_readed;
    private Boolean filter_crit;


    void UpdateData() {
        Log.d("FL", "Update");
        if (rv_adapter != null) {
            rv_adapter.notifyDataSetChanged();
            rv_adapter.UpdateConfig();
        }
    }

    public int getCount() {
        return Fics.size();
    }

    public ArrayList<HashMap<String, Object>> getFics() {
        return Fics;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fanfic_list, container, false);

        rv1 = rootView.findViewById(R.id.rv1);
        pb1 = rootView.findViewById(R.id.pb1);
        swipeContainer = rootView.findViewById(R.id.swipeContainer);

        rv1.setNestedScrollingEnabled(false);
        rv1.setItemViewCacheSize(10);
        context = this.getContext();

        filter_gen = Application.sPref.getBoolean("filter_gen", false);
        filter_het = Application.sPref.getBoolean("filter_het", false);
        filter_other = Application.sPref.getBoolean("filter_other", false);
        filter_slash = Application.sPref.getBoolean("filter_slash", false);
        filter_femslash = Application.sPref.getBoolean("filter_femslash", false);
        filter_mixed = Application.sPref.getBoolean("filter_mixed", false);
        filter_article = Application.sPref.getBoolean("filter_article", false);
        filter_readed = Application.sPref.getBoolean("filter_readed", false);
        filter_crit = Application.sPref.getBoolean("filter_crit", false);

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url", "");
            UrlTemplate2 = getArguments().getString("furl", "");
            if (UrlTemplate != null) {
                Log.d("url", UrlTemplate);
            }
            if (getArguments().getBoolean("cache", false)) {
                Log.d("CACHE", "loading");
                show_cache = true;
                UrlTemplate = "";
            }
            cid = getArguments().getString("collection_id");
            if (cid == null) {
                cid = "";
            }
        }

        Fics = new ArrayList<>();

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(context);

        rv1.setLayoutManager(mLayoutManager);

        rv_adapter = new FicSimpleAdapter(context, Fics, show_cache);
        rv1.setAdapter(rv_adapter);

        EndlessRecyclerViewScrollListener2 scrollListener = new EndlessRecyclerViewScrollListener2(mLayoutManager) {
            @Override
            public void onLoadMore() {
                runFetcher();
            }
        };
        rv1.addOnScrollListener(scrollListener);

        swipeContainer.setOnRefreshListener(() -> {
            StartPosition = 0;
            ficlet_loaded = false;
            loaded = 0;
            added = 0;
            total_added = 0;
            refresh = true;
            if (!show_cache) {
                runFetcher();
            } else {
                Log.i("CACHE", "LOAD");
                loadFromCache();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        reloadContent("");
        return rootView;
    }

    void reloadContent(String new_url) {
        Log.i("FLIST", "reload");
        if (!new_url.isEmpty()) {
            UrlTemplate = new_url;
        }
        StartPosition = 0;
        if (!show_cache) {
            runFetcher();
        } else {
            loadFromCache();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        filter_gen = Application.sPref.getBoolean("filter_gen", false);
        filter_het = Application.sPref.getBoolean("filter_het", false);
        filter_other = Application.sPref.getBoolean("filter_other", false);
        filter_slash = Application.sPref.getBoolean("filter_slash", false);
        filter_femslash = Application.sPref.getBoolean("filter_femslash", false);
        filter_mixed = Application.sPref.getBoolean("filter_mixed", false);
        filter_article = Application.sPref.getBoolean("filter_article", false);
        filter_readed = Application.sPref.getBoolean("filter_readed", false);
    }

    @Override
    public void onBackStackChanged() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Fragment fr = fragmentManager.findFragmentById(R.id.fl_fragment);
            if (fr != null) {
                fr.onResume();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void loadFromCache() {
        int total = Fics.size();
        int added = 0;
        Fics.clear();
        rv1.post(() -> rv_adapter.notifyItemRangeRemoved(0, total));
        if (getContext() != null) {
            File[] files = getContext().getFilesDir().listFiles();
            if (files != null) {
                try {
                    Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                for (File file : files) {
                    Fanfic f = null;
                    if (file.getName().contains("_.fb2.zip")) {
                        f = Fanfic.getById(file.getName().replace("_.fb2.zip", ""));
                    }

                    if (f != null) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("id", f.nid);
                        map.put("title", f.title);
                        boolean filter = false;
                        try {
                            filter = (
                                    (f.direction.equals("het") && filter_het) ||
                                            (f.direction.equals("gen") && filter_gen) ||
                                            (f.direction.equals("other") && filter_other) ||
                                            (f.direction.equals("slash") && filter_slash) ||
                                            (f.direction.equals("femslash") && filter_femslash) ||
                                            (f.direction.equals("mixed") && filter_mixed) ||
                                            (f.direction.equals("article") && filter_article) ||
                                            (!f.date_changes.isEmpty() && f.new_content.isEmpty() && filter_readed) ||
                                            (!f.critic.isEmpty() && filter_crit)
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!filter) {
                            if (!Fics.contains(map)) {
                                Fics.add(map);
                                added++;
                            }
                        }
                    }
                }
            }
        }

        pb1.setVisibility(View.GONE);
        swipeContainer.setRefreshing(false);
        try {
            rv_adapter.notifyItemRangeInserted(0, added);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runFetcher() {
        if (swipeContainer == null) {
            return;
        }
        if (Application.isInternetAvailable()) {
            if (!UrlTemplate2.isEmpty() && UrlTemplate.isEmpty()) {
                MaxPosition = 0;
                swipeContainer.setRefreshing(true);
                pb1.setVisibility(View.GONE);
                StartPosition = 1;
                refresh = true;
                FetchFicsFiclet();
            }
        }
        if (StartPosition == 0) {
            swipeContainer.setRefreshing(true);
            StartPosition = 1;
            refresh = true;
        }
        if (StartPosition <= MaxPosition) {
            if (StartPosition > -1) {
                if (!Loading) {
                    if (!refresh) {
                        pb1.setVisibility(View.VISIBLE);
                        refresh = false;
                    }
                }
                if (Application.isInternetAvailable()) {
                    if (!UrlTemplate.isEmpty()) {
                        if (!UrlTemplate.contains("notifications")) {
                            FetchFics();
                        } else {
                            FetchFicsNotifications();
                        }
                    }
                } else {
                    swipeContainer.setRefreshing(false);
                    pb1.setVisibility(View.GONE);
                }
            }
        } else {
            Log.d("List", "END");
            if (Application.isInternetAvailable()) {
                if (!UrlTemplate2.isEmpty() && !ficlet_loaded) {
                    Log.d("List", "Load ficlet " + UrlTemplate2);
                    MaxPosition = 0;
                    swipeContainer.setRefreshing(true);
                    pb1.setVisibility(View.GONE);
                    StartPosition = 1;
                    refresh = true;
                    FetchFicsFiclet();
                }
            }
        }
    }

    public void FetchFicsFiclet() {
        if (Loading || ficlet_loaded) {
            return;
        }
    }



    public void FetchFics() {
        if (Loading) {
            return;
        }
        Loading = true;

        ExecutorService executors = Executors.newFixedThreadPool(1);
        Runnable runnable = () -> {
            Document doc = null;
            int total = 0;

            try {
                String url = UrlTemplate.replace("@", StartPosition.toString());

                if (!url.isEmpty()) {
                    Log.i("LOAD", url);

                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        String bodyx;

                        if (body != null) {
                            bodyx = body.string();
                            response.close();

                            if (bodyx.contains("Не удалось найти ничего с указанными вами параметрами.\n")) {
                                Loading = false;
                                return;
                            }

                            doc = Jsoup.parse(bodyx);
                        } else {
                            Loading = false;
                            return;
                        }
                    } else {
                        Loading = false;
                    }
                } else {
                    return;
                }
            } catch (SocketTimeoutException e) {
                Loading = false;
                Application.displayPopup("Сервер не отвечает");
            } catch (UnknownHostException e) {
                Loading = false;
                Application.displayPopup("Проблемы с соединением");
            } catch (IOException e) {
                Loading = false;
                if (!Thread.interrupted()) {
                    Application.displayPopup("Ошибка загрузки " + e.toString());
                }
            } catch (Exception e) {
                Loading = false;
                e.printStackTrace();
            }

            try {
                if (doc != null) {
                    MaxPosition = 1;

                    if (UrlTemplate.contains("find")) {
                        MaxPosition = 10000;
                    }
                    if (!doc.select("nav.pagination-holder li.text b").isEmpty()) {
                        MaxPosition = Integer.parseInt(doc.select("nav.pagination-holder li.text b").get(1).text());
                        Log.i("MAX P", "-A");
                    }
                    if (!doc.select("nav.pagenav div.paging-description b").isEmpty()) {
                        MaxPosition = Integer.parseInt(doc.select("nav.pagenav div.paging-description b").get(1).text());
                        Log.i("MAX P", "-B");
                    }
                    if (!doc.select("li.text input").isEmpty()) {
                        MaxPosition = Integer.parseInt(doc.select("li.text input").attr("max"));
                        Log.i("MAX P", "-C");
                    }
                    Log.d("MAX P", MaxPosition + "!");
                    if (doc.select("title").text().equals("Технические работы")) {
                        Toast.makeText(context, "На фикбуке технические работы!", Toast.LENGTH_LONG).show();
                    }
                }

                loaded = 0;
                added = 0;
                total = Fics.size();
                if (refresh) {
                    Fics.clear();
                    int finalTotal1 = total;
                    rv1.post(() -> rv_adapter.notifyItemRangeRemoved(0, finalTotal1));
                }
                try {
                    Elements vid;
                    try {
                        if (doc == null || doc.html() == null) {
                            Log.e("DOC", "null");
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("DOC", "null");
                        return;
                    }
                    if (doc.html().contains("js-item-wrapper")) { //coll
                        vid = doc.select("div.js-item-wrapper");
                        if (vid == null) {
                            vid = doc.select("article.block");
                        }
                    } else {
                        vid = doc.select(".fanfic-thumb-block article.fanfic-inline");
                        if (vid.size() == 0) {
                            vid = doc.select("article.fanfic-inline");
                        }
                    }
                    if (vid == null) {
                        Log.e("FLIST", "no container");
                    }

                    HashMap<String, Object> map;

                    ActiveAndroid.beginTransaction();

                    if (vid != null) {
                        Log.i("FLIST", vid.size() + " items");
                        if (vid.size() == 0) {
                            Loading = false;
                            FetchFicsFiclet();
                        }

                    } else {
                        return;
                    }

                    for (Element v : vid) {
                        map = new HashMap<>();

                        String direction = "";

                        if (v.select("div.direction-before-het").first() != null) {
                            direction = "het";
                        } else {
                            if (v.select("div.direction-before-gen").first() != null) {
                                direction = "gen";
                            } else {
                                if (v.select("div.direction-before-mixed").first() != null) {
                                    direction = "mixed";
                                } else {
                                    if (v.select("div.direction-before-slash").first() != null) {
                                        direction = "slash";
                                    } else {
                                        if (v.select("div.direction-before-femslash").first() != null) {
                                            direction = "femslash";
                                        } else {
                                            if (v.select("div.direction-before-article").first() != null) {
                                                direction = "article";
                                            } else {
                                                if (v.select("div.direction-before-other").first() != null) {
                                                    direction = "other";
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        String title = v.select("h3").text().trim();
                        String id = v.select("h3.fanfic-inline-title a").attr("href").replace("/readfic/", "");
                        String sup = v.select("div.badge-like").text().trim();
                        String authors = v.select("span.author").text().trim();
                        String trophy;
                        String cover = "";

                        try {
                            cover = v.select("picture.fanfic-hat-cover-picture img").attr("src");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (id.contains("?")) {
                            String[] ids = id.split("\\?");
                            id = ids[0];
                        }

                        String new_parts;
                        if (!v.select("span.new-parts-in-title").isEmpty()) {
                            new_parts = v.select("span.new-parts-in-title").text();
                        } else {
                            new_parts = "";
                        }

                        if (!v.select("span.badge-reward").isEmpty()) {
                            trophy = v.select("span.badge-reward").text();
                        } else {
                            trophy = "";
                        }

                        Elements description = v.select("div.fanfic-description-text");
                        Elements infod = v.select("dl.fanfic-inline-info");

                        String date_changes = "";
                        String new_content = "";

                        Elements read_notify = v.select("div.read-notification");
                        if (!read_notify.isEmpty()) {
                            if (!read_notify.select("span.hidden-xs span").isEmpty()) {
                                date_changes = read_notify.select("span.hidden-xs span").attr("title");
                                if (!read_notify.select("span.new-content").isEmpty()) {
                                    new_content = read_notify.select("span.new-content").attr("title").replace("Обновлялась ", "");
                                    if (new_content.contains(".")) {
                                        String[] t = new_content.split("\\.");
                                        new_content = t[0];
                                    }
                                } else {
                                    new_content = "";
                                }
                            }
                        }
                        if (date_changes == null) {
                            date_changes = "";
                        }
                        if (!read_notify.select("span.new-parts-in-title").isEmpty()) {
                            new_content = read_notify.select("span.new-parts-in-title").text();
                        }
                        if (new_content == null) {
                            new_content = "";
                        }

                        StringBuilder fandom = new StringBuilder();
                        StringBuilder rating = new StringBuilder();
                        StringBuilder genres = new StringBuilder();
                        StringBuilder caution = new StringBuilder();
                        StringBuilder tags = new StringBuilder();
                        String size = "";
                        String sizetype = "";
                        String pages = "";
                        String parts = "";
                        String status = "";
                        String pairings = "";
                        String collection_id;
                        String bad = "";
                        String critic = "";
                        Element icontent;
                        int idx = 0;

                        if (!v.select("div.badge-status-frozen").isEmpty()) {
                            status = "заморожен";
                        }
                        if (!v.select("svg.ic_in-progress").isEmpty()) {
                            status = "в процессе";
                        }
                        if (!v.select("div.badge-status-finished").isEmpty()) {
                            status = "закончен";
                        }

                        for (Element i : infod) {
                            icontent = infod.select("dd").get(idx);
                            switch (i.select("dt").text()) {
                                case "Фэндом:":
                                    fandom = new StringBuilder();
                                    for (Element f : icontent.select("a")) {
                                        fandom.append(f.text()).append(", ");
                                    }
                                    if (fandom.length() > 2) {
                                        fandom = new StringBuilder(fandom.substring(0, fandom.length() - 2));
                                    }
                                    break;
                                case "Размер:":
                                    String sz = icontent.select("span.size-title").text();
                                    String xz = "";

                                    if (sz.contains("Драббл")) {
                                        xz = "Драббл";
                                    }
                                    if (sz.contains("Мини")) {
                                        xz = "Мини";
                                    }
                                    if (sz.contains("Миди")) {
                                        xz = "Миди";
                                    }
                                    if (sz.contains("Макси")) {
                                        xz = "Макси";
                                    }

                                    String[] szz = icontent.text().split(",");
                                    if (szz.length == 2) {
                                        pages = szz[0].replaceAll("[^0-9.,]+", "");
                                        parts = szz[1].replaceAll("[^0-9.,]+", "");
                                    } else if (szz.length == 3) {
                                        pages = szz[1].replaceAll("[^0-9.,]+", "");
                                        parts = szz[2].replaceAll("[^0-9.,]+", "");
                                    } else {
                                        parts = "";
                                        pages = "";
                                    }

                                    if (xz.isEmpty() && !pages.isEmpty()) {
                                        switch (Integer.parseInt(pages) / 10) {
                                            case 0:
                                            case 1:
                                                xz = "Драббл";
                                                break;
                                            case 2:
                                                xz = "Мини";
                                                break;
                                            case 3:
                                            case 4:
                                            case 5:
                                            case 6:
                                                xz = "Миди";
                                                break;
                                            default:
                                                xz = "Макси";
                                        }
                                    }

                                    size = parts + "/" + pages;
                                    sizetype = xz;
                                    break;
                                case "Пэйринг и персонажи:":
                                case "Пэйринг или персонажи:":
                                    pairings = icontent.text();
                                    break;
                                case "Другие метки:":
                                case "Метки:":
                                case "Отношения:":
                                case "Жанры:":
                                case "Предупреждения:":
                                    try {
                                        for (Element g : icontent.select("a")) {
                                            String gg = g.text().replaceAll("<[^>]*>", "");
                                            if (g.hasClass("tag-adult")) {
                                                gg = gg + " \uD83D\uDD1E";
                                            }
                                            if (g.hasClass("disliked-parameter-link")) {
                                                gg = "<b><font color=\"#8a2525\">" + gg + "</font></b>";
                                                bad = "!";
                                            }
                                            if (g.hasClass("liked-parameter-link")) {
                                                gg = "<b><font color=\"#086e00\">" + gg + "</font></b>";
                                            }

                                            String tag_id = g.attr("href").replace("/tags/", "");
                                            if (!tag_id.equals("")) {
                                                String tc_id = Tags.getCategoryId(tag_id);
                                                switch (tc_id) {
                                                    case "25":
                                                        genres.append(gg).append(", ");
                                                        break;
                                                    case "26":
                                                        caution.append(gg).append(", ");
                                                        break;
                                                    default:
                                                        tags.append(gg).append(", ");
                                                }
                                            }
                                        }
                                        if (tags.length() > 2) {
                                            tags = new StringBuilder(tags.substring(0, tags.length() - 2));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    break;

                                default:
                                    break;
                            }
                            idx++;
                        }

                        if (!v.select("span.notice-yellow").isEmpty()) {
                            critic = "1";
                        }

                        if (!v.select("strong.badge-rating-PG-13").isEmpty()) {
                            rating.append("PG-13");
                        }
                        if (!v.select("strong.badge-rating-R").isEmpty()) {
                            rating.append("R");
                        }
                        if (!v.select("strong.badge-rating-NC-21").isEmpty()) {
                            rating.append("NC-21");
                        }
                        if (!v.select("strong.badge-rating-G").isEmpty()) {
                            rating.append("G");
                        }
                        if (!v.select("strong.badge-rating-NC-17").isEmpty()) {
                            rating.append("NC-17");
                        }

                        Elements itags = v.select("div.tags a");
                        for (Element g : itags) {
                            try {
                                String gg = g.text().replaceAll("<[^>]*>", "");
                                if (g.hasClass("tag-adult")) {
                                    gg = gg + " \uD83D\uDD1E";
                                }
                                if (g.hasClass("disliked-parameter-link")) {
                                    gg = "<b><font color=\"#8a2525\">" + gg + "</font></b>";
                                    bad = "!";
                                }
                                if (g.hasClass("liked-parameter-link")) {
                                    gg = "<b><font color=\"#086e00\">" + gg + "</font></b>";
                                }

                                String tag_id = g.attr("href").replace("/tags/", "");
                                if (!tag_id.equals("")) {
                                    String tc_id = Tags.getCategoryId(tag_id);
                                    switch (tc_id) {
                                        case "25":
                                            genres.append(gg).append(", ");
                                            break;
                                        case "26":
                                            caution.append(gg).append(", ");
                                            break;
                                        default:
                                            tags.append(gg).append(", ");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        collection_id = Fanfic.getCollectionId(id);
                        if (v.select("fanfic-thumb-tool-panel").isEmpty()) {
                            if (v.select("div.fanfic-thumb-tool-panel").select("a").hasAttr("data-collection-id")) {
                                collection_id = v.select("div.fanfic-thumb-tool-panel").select("a").attr("data-collection-id");
                            }
                        }

                        if (!cid.isEmpty()) {
                            collection_id = cid;
                        }

                        map.put("id", id);
                        map.put("title", title);
                        map.put("cover", cover);

                        String s_info = YO.createTags(description.text());

                        Fanfic old_f = Fanfic.getById(id);
                        Fanfic f = new Fanfic();

                        f.nid = id;
                        f.title = title;
                        f.sup = sup;
                        f.authors = authors;
                        f.direction = direction;
                        f.pairings = pairings;
                        f.fandom = fandom.toString();
                        f.rating = rating.toString();
                        f.genres = genres.toString();
                        f.cautions = caution.toString();
                        f.tags = tags.toString();
                        f.size = size;
                        f.sizetype = sizetype;
                        f.pages = pages;
                        f.parts = parts;
                        f.status = status;
                        f.info = s_info; //description.text();
                        f.collection_id = collection_id;
                        f.date_changes = date_changes;
                        f.new_content = new_content;
                        f.bad = bad;
                        f.new_part = new_parts;
                        f.critic = critic;
                        f.trophy = trophy;
                        if (old_f != null) {
                            if (old_f.new_part != null) {
                                if (!old_f.new_part.isEmpty()) {
                                    f.new_part = old_f.new_part;
                                }
                            }
                        }

                        f.save();

                        boolean found = false;

                        for (HashMap<String, Object> hm : Fics) {
                            if (hm.containsKey("id")) {
                                if (Objects.requireNonNull(hm.get("id")).toString().equals(id)) {
                                    found = true;
                                    hm.putAll(map);
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            boolean filter = (
                                    (direction.equals("het") && filter_het) ||
                                            (direction.equals("gen") && filter_gen) ||
                                            (direction.equals("other") && filter_other) ||
                                            (direction.equals("slash") && filter_slash) ||
                                            (direction.equals("femslash") && filter_femslash) ||
                                            (direction.equals("mixed") && filter_mixed) ||
                                            (direction.equals("article") && filter_article) ||
                                            (!date_changes.isEmpty() && new_content.isEmpty() && filter_readed) ||
                                            (!critic.isEmpty() && filter_crit)
                            );
                            if (!filter && (Application.SIG.equals(","))) {
                                Fics.add(map);
                                added++;
                            }
                            loaded++;
                        }
                    }
                } catch (Exception e) {
                    Loading = false;
                    e.printStackTrace();
                }

                try {
                    ActiveAndroid.setTransactionSuccessful();
                    ActiveAndroid.endTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Loading = false;
                e.printStackTrace();
            }
            Application.firePopup();

            try {
                Handler uiThread = new Handler(Looper.getMainLooper());
                int finalTotal = total;
                uiThread.post(() -> {
                    try {
                        swipeContainer.setRefreshing(false);
                        Objects.requireNonNull(rv1.getAdapter()).notifyItemRangeInserted(finalTotal, added);

                        StartPosition++;
                        Loading = false;
                        pb1.setVisibility(View.GONE);
                        refresh = false;
                        boolean filter = false;
                        total_added = total_added + added;
                        if ((loaded > 0) && (added == 0)) {
                            Log.d("FILTER", "loaded " + loaded + " but added " + added);
                            if (total_added < 5) {
                                Log.i("SF", "Loading more");
                                filter = true;
                                runFetcher();
                            }
                        }

                        if ((Fics.isEmpty()) && (!filter)) {
                                if (getActivity() != null) {
                                    Log.i("NONO", UrlTemplate + ":" + Application.isGuest().toString());
                                    LinearLayout mRoot = requireActivity().findViewById(R.id.content_main);
                                    if ((!UrlTemplate.contains("collect")) &&(mRoot != null)) {
                                        Snackbar snackbar = Snackbar.make(mRoot, "Ничего не найдено :(", Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                }
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        executors.submit(runnable);
    }


    public void FetchFicsNotifications() {
        if (Loading) {
            return;
        }
        Loading = true;

        ExecutorService executors = Executors.newFixedThreadPool(1);
        Runnable runnable = () -> {
            try {
                String url = UrlTemplate.replace("@", StartPosition.toString());
                String bodyx;

                if (!url.isEmpty()) {
                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();

                        if (body != null) {
                            bodyx = body.string();
                            response.close();
                            Document doc = Jsoup.parse(bodyx);

                            if (UrlTemplate.contains("notifi")) {
                                //MaxPosition = 5;
                            }

                            if (!doc.select("div.page-arrow-next").isEmpty()) {
                                MaxPosition++;
                                Log.w("MAX","new page");
                            }

                            Elements vid = doc.select("div.notifications-content").select("a.notification-item");
                            if (StartPosition == 1) {
                                Fics.clear();
                            }

                            for (Element v : vid) {
                                HashMap<String, Object> map = new HashMap<>();
                                String id = v.attr("href").replace("/readfic/", "").split("/")[0];
                                String data = v.select("div.word-break").text();
                                String title = "";
                                String author = "";
                                String fandom = "";

                                Pattern pattern = Pattern.compile("Автор: (.*?), работа:");
                                Matcher matcher = pattern.matcher(data);
                                if (matcher.find()) {
                                    author = matcher.group(1);
                                }

                                pattern = Pattern.compile("работа: \"(.*?)\",");
                                matcher = pattern.matcher(data);
                                if (matcher.find()) {
                                    title = matcher.group(1);
                                }

                                pattern = Pattern.compile("фэндом: \"(.*?)\"");
                                matcher = pattern.matcher(data);
                                if (matcher.find()) {
                                    fandom = matcher.group(1);
                                }
                                map.put("id", id);
                                map.put("title", title);

                                if (!Fics.contains(map)) {
                                    Fics.add(map);

                                    Fanfic old_f = Fanfic.getById(id);
                                    if (old_f == null) {
                                        Fanfic f = new Fanfic();

                                        f.nid = id;
                                        f.title = title;
                                        f.sup = "";
                                        f.authors = author;
                                        f.direction = "";
                                        f.pairings = "";
                                        f.fandom = fandom;
                                        f.rating = "";
                                        f.genres = "";
                                        f.cautions = "";
                                        f.tags = "";
                                        f.size = "";
                                        f.sizetype = "";
                                        f.pages = "";
                                        f.parts = "";
                                        f.status = "";
                                        f.info = "";
                                        f.collection_id = "";
                                        f.date_changes = "";
                                        f.new_content = "";
                                        f.bad = "";
                                        f.new_part = "";
                                        f.critic = "";
                                        f.trophy = "";
                                        f.save();
                                    }
                                }
                            }
                        } else {
                            Loading = false;
                        }
                    } else {
                        Log.e("FNN", "!!!!" + response.code());
                        Loading = false;
                    }
                }
            } catch (Exception e) {
                Loading = false;
                e.printStackTrace();
            }
            try {
                Handler uiThread = new Handler(Looper.getMainLooper());
                uiThread.post(() -> {
                    try {
                        swipeContainer.setRefreshing(false);
                        Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
                        StartPosition++;
                        Loading = false;
                        pb1.setVisibility(View.GONE);
                        refresh = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        executors.submit(runnable);
    }


}



