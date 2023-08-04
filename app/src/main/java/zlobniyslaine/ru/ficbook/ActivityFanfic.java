package zlobniyslaine.ru.ficbook;


import static zlobniyslaine.ru.ficbook.Application.FICLET_HOST;
import static zlobniyslaine.ru.ficbook.Application.getFicletRequest;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.activeandroid.ActiveAndroid;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionCollectionAdd;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionLike;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionRead;
import zlobniyslaine.ru.ficbook.controls.ViewAuthorWidget;
import zlobniyslaine.ru.ficbook.models.Collections;
import zlobniyslaine.ru.ficbook.models.Fanfic;
import zlobniyslaine.ru.ficbook.models.Parts;
import zlobniyslaine.ru.ficbook.models.Tags;
import zlobniyslaine.ru.ficbook.moshis.JSONChatMessages;
import zlobniyslaine.ru.ficbook.moshis.JSONFicletFanfic;
import zlobniyslaine.ru.ficbook.pagers.FicPagerAdapter;


public class ActivityFanfic extends AppCompatActivity {

    private String UrlTemplate;
    private ArrayList<HashMap<String, Object>> FicParts = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> FicFandoms = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> FicGenres = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> FicPairings = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> FicCharacters = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> FicCautions = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> FicTags = new ArrayList<>();
    private String id;
    private String title;
    private final StringBuilder authors_list = new StringBuilder();
    private Boolean like = false;
    private Boolean fanfic_fullui = true;
    private Boolean smart_readed = false;

    TextView tv_title;
    TextView tv_direction;
    LinearLayout l_direction;
    LinearLayout l_btnbar;
    ProgressBar pb1;
    RelativeLayout l_shadow;
    LinearLayout l_authors;
    FloatingActionButton fab;
    FloatingActionButton fabReply;
    ViewPager viewPager;
    TabLayout tabLayout;
    Button btn_rate;
    Button btn_mark;

    private Fragment fragment_parts;
    private Fragment fragment_info;
    private Fragment fragment_reviews;
    private Fragment fragment_collections;
    private List<Fragment> fragments;
    private Context context;

    void spoilerDirection() {
        if (tv_direction.getMaxLines() == 1) {
            tv_direction.setMaxLines(5);
        } else {
            tv_direction.setMaxLines(1);
        }
    }

    public void reply_part() {
        ((FragmentFic_Review) fragment_reviews).ReplyDialog(id);
    }

    public void DownloadFanfic(String format, Boolean open) {

//        openDirectory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                int permissions_code = 42;
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, permissions_code);
            }
        }

        String download_filename;
        try {
            download_filename = URLDecoder.decode(title
                            .replace(".", "")
                            .replace("?", "")
                            .replace(",", "")
                            .replace(":", "")
                            .replace("\\", "")
                            .replace("/", "")
                            .replace("#", "")
                    , "UTF-8") + "." + format;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            download_filename = title.replace(".", "").replace("?", "") + "." + format;
        }
        Log.i("filename", download_filename + " [" + format + "]");

        Application.downloadFanfic(id, title, format, download_filename, open);
    }

    public void openFanficMenu() {
        PopupMenu popup = new PopupMenu(this, fab);
        popup.getMenuInflater().inflate(R.menu.fanfic_menu, popup.getMenu());

        popup.getMenu().findItem(R.id.mDownload).setVisible(!Application.isGuest());
        popup.getMenu().findItem(R.id.mDownloadOpen).setVisible(!Application.isGuest());
        popup.getMenu().findItem(R.id.mMarkReaded).setVisible(!Application.isGuest());
        popup.getMenu().findItem(R.id.mUnReaded).setVisible(!Application.isGuest());

        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper;
                    if (menuPopupHelper != null) {
                        classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.mDownloadTXT:
                    DownloadFanfic("txt", false);
                    return true;
                case R.id.mDownloadFB2:
                    DownloadFanfic("fb2", false);
                    return true;
                case R.id.mDownloadPDF:
                    DownloadFanfic("pdf", false);
                    return true;
                case R.id.mDownloadEPUB:
                    DownloadFanfic("epub", false);
                    return true;

                case R.id.mOpenTXT:
                    DownloadFanfic("txt", true);
                    return true;
                case R.id.mOpenFB2:
                    DownloadFanfic("fb2", true);
                    return true;
                case R.id.mOpenPDF:
                    DownloadFanfic("pdf", true);
                    return true;
                case R.id.mOpenEPUB:
                    DownloadFanfic("epub", true);
                    return true;

                case R.id.mReader:
                    if (id != null && title != null) {
                        Intent iReader = new Intent(context, ActivityReader.class);
                        iReader.putExtra("id", id);
                        iReader.putExtra("title", title);
                        context.startActivity(iReader);
                    }
                    return true;
                case R.id.mAudioBook:
                    if (id != null) {
                        Intent iAudioBook = new Intent(context, ActivityVoiceReaderPro.class);
                        iAudioBook.putExtra("id", id);
                        context.startActivity(iAudioBook);
                    }
                    return true;
                case R.id.mMarkReaded:
                    if (id != null && title != null) {
                        AjaxActionRead action = new AjaxActionRead();
                        action.Do(id, true);
                        CoordinatorLayout mRoot = findViewById(R.id.coordl);
                        Snackbar snackbar = Snackbar.make(mRoot, "Фанфик «" + title + "» отмечен как прочитанный", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    return true;
                case R.id.mUnReaded:
                    if (id != null && title != null) {
                        AjaxActionRead action2 = new AjaxActionRead();
                        action2.Do(id, false);
                        CoordinatorLayout mRoot2 = findViewById(R.id.coordl);
                        Snackbar snackbar2 = Snackbar.make(mRoot2, "Фанфик «" + title + "» отмечен как непрочитанный", Snackbar.LENGTH_LONG);
                        snackbar2.show();
                    }
                    return true;
                case R.id.mShareLink:
                    if (authors_list != null && UrlTemplate != null && title != null) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Ficlet для Android");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n(" + authors_list + ")\n" + UrlTemplate + "\n");
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Отправить ссылку"));
                    }
                    return true;
                case R.id.mCreateShortcut:
                    if (id != null && title != null) {
                        Intent shortcutIntent = new Intent(getApplicationContext(), ActivityFanfic.class);
                        shortcutIntent.setAction(Intent.ACTION_MAIN);
                        shortcutIntent.putExtra("id", id);
                        shortcutIntent.putExtra("title", title);

                        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                            ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, id)
                                    .setIntent(shortcutIntent) // !!! intent's action must be set on oreo
                                    .setShortLabel(title)
                                    .setShortLabel(title)
                                    .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_shortcut))
                                    .build();

                            ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
                        } else {
                            Intent addIntent = new Intent();
                            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title + "!!!!!");
                            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher));

                            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                            addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
                            getApplicationContext().sendBroadcast(addIntent);
                        }
                    }
                    return true;
            }
            return false;
        });

        popup.show();
    }

    void toggleFullUI() {
        setUI(false);
    }

    public void rate_fanfic() {
        AjaxActionLike action = new AjaxActionLike();
        like = !like;
        action.Do(id, like);
        CoordinatorLayout mRoot = findViewById(R.id.coordl);
        String likeText = "Проголосовали за «" + title + "»";
        if (!like) {
            likeText = "Отменили голос за «" + title + "»";
            btn_rate.setTextColor(getResources().getColor(R.color.black));
            btn_rate.setCompoundDrawables(null, null, null, null);
        } else {
            btn_rate.setTextColor(getResources().getColor(R.color.green));
//            Drawable image = context.getResources().getDrawable(R.drawable.tri_like);
            Drawable image = ResourcesCompat.getDrawable(getResources(), R.drawable.tri_like, null);
            if (image != null) {
                int h = image.getIntrinsicHeight();
                int w = image.getIntrinsicWidth();
                image.setBounds(0, 0, w, h);
                btn_rate.setCompoundDrawables(image, null, null, null);
            }
        }
        Snackbar snackbar = Snackbar.make(mRoot, likeText, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void add_to_list() {
        final List<Collections> collections = Collections.getOwnAll();
        String[] items = new String[collections.size()];
        for (int i = 0; i < collections.size(); i++) {
            items[i] = collections.get(i).title;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить фанфик в сборник");
        builder.setItems(items, (dialog, item) -> {
            CoordinatorLayout mRoot = findViewById(R.id.coordl);
            Snackbar snackbar = Snackbar.make(mRoot, "Фанфик «" + title + "» добавлен в сборник «" + collections.get(item).title + "»", Snackbar.LENGTH_LONG);
            snackbar.show();
            try {
                AjaxActionCollectionAdd action = new AjaxActionCollectionAdd();
                action.Do(id, collections.get(item).nid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_fanfic);

        tv_title = findViewById(R.id.tv_title);
        tv_direction = findViewById(R.id.tv_direction);
        l_direction = findViewById(R.id.l_direction);
        l_btnbar = findViewById(R.id.l_btnbar);
        pb1 = findViewById(R.id.pb1);
        l_shadow = findViewById(R.id.l_shadow);
        l_authors = findViewById(R.id.l_authors_layout);
        fab = findViewById(R.id.fab);
        fabReply = findViewById(R.id.fab_reply);
        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);
        btn_rate = findViewById(R.id.btn_rate);

        btn_mark = findViewById(R.id.btn_mark);
        btn_mark.setTypeface(Application.getIconFont());

        tv_direction.setOnClickListener(v -> spoilerDirection());
        fabReply.setOnClickListener(v -> reply_part());
        fab.setOnClickListener(v -> openFanficMenu());
        tv_title.setOnClickListener(v -> toggleFullUI());
        btn_rate.setOnClickListener(v -> rate_fanfic());
        btn_mark.setOnClickListener(v -> add_to_list());

        if (Application.isGuest()) {
            btn_mark.setVisibility(View.GONE);
        }

        l_shadow.setVisibility(View.VISIBLE);

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fanfic_fullui = Application.sPref.getBoolean("fanfic_fullui", true);
        smart_readed = Application.sPref.getBoolean("smart_readed", false);

        tabLayout.addTab(tabLayout.newTab().setText("Информация"));
        tabLayout.addTab(tabLayout.newTab().setText("Оглавление"));
        tabLayout.addTab(tabLayout.newTab().setText("Отзывы"));
        tabLayout.addTab(tabLayout.newTab().setText("В сборниках"));

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

        setUI(true);
        StartLoaders();
    }

    private void StartLoaders() {
        FragmentManager fm = getSupportFragmentManager();
        fragments = new Vector<>();

        fragment_parts = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Parts.class.getName());
        fragment_info = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Info.class.getName());
        fragment_reviews = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Review.class.getName());
        fragment_collections = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Collections.class.getName());

        try {
            Intent intent = getIntent();

            if (intent.hasExtra("random")) {
                id = "0";
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Случайный фанфик");
                }
                UrlTemplate = "https://ficbook.net/randomfic/";
            } else {
                id = intent.getStringExtra("id");
                if (id == null) {
                    id = "";
                }
                if (id.contains("?")) {
                    String[] ids = id.split("\\?");
                    id = ids[0];
                }
                Log.i("ID", id);
                if (id != null) {
                    Fanfic.clearFlags(id);
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(intent.getStringExtra("title"));
                }
                UrlTemplate = "https://ficbook.net/readfic/" + id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FicParts = new ArrayList<>();
        FicFandoms = new ArrayList<>();
        FicPairings = new ArrayList<>();
        FicCharacters = new ArrayList<>();
        FicCautions = new ArrayList<>();
        FicTags = new ArrayList<>();
        FicGenres = new ArrayList<>();

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                tv_direction.setMaxLines(1);
                break;
        }

        Fanfic fanfic = Fanfic.getById(id);
        if (fanfic != null) {
            tv_title.setText(fanfic.title);
            title = fanfic.title;
        }

        new Thread(
                YO::init
        ).start();
        Log.i("FANFIC", "load info");
        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setUI(Boolean init) {
        if (!init) {
            fanfic_fullui = !fanfic_fullui;
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.putBoolean("fanfic_fullui", fanfic_fullui);
            ed.apply();
        }

        try {
            if (fanfic_fullui) {
                l_direction.setVisibility(View.VISIBLE);
                l_btnbar.setVisibility(View.VISIBLE);
            } else {
                l_direction.setVisibility(View.GONE);
                l_btnbar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateFab(int position) {
        switch (position) {
            case 0:
            case 1:
                fab.show();
                fabReply.hide();
                break;

            default:
                if (!Application.isGuest()) {
                    fabReply.show();
                }
                fab.hide();
                break;
        }
    }

    private final BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
        }
    };

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityFanfic> activityReference;

        fetcher_main(ActivityFanfic context) {
            activityReference = new WeakReference<>(context);
        }

        Document doca = null;
        String bodyx = null;
        String id;
        String f_reviews;
        String f_title;
        String f_rating;
        String f_direction;
        int dir_color;
        boolean ficlet = false;
        Elements doc;
        final Bundle bundle_info = new Bundle();
        final Bundle bundle_parts = new Bundle();
        final Bundle bundle_reviews = new Bundle();
        final Bundle bundle_collections = new Bundle();

        @Override
        protected Void doInBackground(String... params) {
            if (activityReference.get() == null) {
                return null;
            }
            id = activityReference.get().id;
            if (id == null) {
                return null;
            }
            if (Application.isInternetAvailable()) {
                try {
                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(activityReference.get().UrlTemplate)).execute();
                    ResponseBody body = response.body();

                    if (!response.isSuccessful()) {
                        Application.displayPopup("Невозможно открыть с сайта фикбука " + response.code() + " " + response.message());

                        if (response.code() == 404) {
                            Log.w("FANFIC", "404");
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .cookieJar(Application.cookieJar)
                                    .build();

                            client.followRedirects();
                        }

                        if (!response.isSuccessful()) {
//                            activityReference.get().pb1.setVisibility(View.GONE);
                            Application.displayPopup("Сетевая ошибка " + response.code() + " " + response.message());
                            return null;
                        }
                        ficlet = true;
                    }

                    if (body != null) {
                        bodyx = body.string();
                        Log.i("LOAD", "From network");
                    }
                    response.close();

                    if ((bodyx == null)) {
                        Log.w("LOAD", "Failed network/rawcache");
                        return null;
                    }

                    Application.saveCacheObject(id + ".info", bodyx);
                } catch (SocketTimeoutException e) {
                    Application.displayPopup("Сервер не отвечает");
                } catch (UnknownHostException e) {
                    Application.displayPopup("Проблемы с соединением");
                } catch (IOException e) {
                    Application.displayPopup("Ошибка загрузки");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.w("LOAD", "No internet, try from DB");
                if (Application.getCacheObjectSize(id + ".info") > 0) {
                    bodyx = (String) Application.loadCacheObject(id + ".info");
                } else {
                    Log.w("LOAD", "no info data in cache");
                    return null;
                }
            }


            if (ficlet) {
            } else {
                if (bodyx != null) {
                    doca = Jsoup.parse(bodyx);
                } else {
                    return null;
                }
                doca.select("div.fanfic-reward-container").remove();
                doca.select("a.start-reading").remove();
                doca.select("div.fanfic-hat-premium-notice").remove();
                doca.select("div.fb-ads-block").remove();

                doc = doca.select("#main");

                try {
                    if (id.equals("0")) {
                        if (doc.select("input[name=fanfic_id]").first() != null) {
                            id = doc.select("input[name=fanfic_id]").first().val();
                            activityReference.get().id = id;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (id.equals("0")) {
                    try {
                        id = doc.select("fanfic-collections-link").attr("url").replace("collections", "").replace("list", "").replace("/", "");
                        activityReference.get().id = id;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.w("GET ID", id);

                try {
                    Elements hat = doc.select("section.fanfic-hat");
                    Elements toc = doc.select("ul.list-of-fanfic-parts li");
                    Elements info = doc.select("div.fanfic-main-info");
                    Elements like_info = hat.select("span.js-like");

                    activityReference.get().like = (like_info.hasClass("btn-success"));

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
                                activityReference.get().FicGenres.add(map);
                                break;
                            case "26":
                                activityReference.get().FicCautions.add(map);
                                break;
                            default:
                                activityReference.get().FicTags.add(map);
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
                                    map.put("url", p.attr("href").replace("/pairings/", ""));
                                    activityReference.get().FicCharacters.add(map);
                                }
                                break;

                            case "Пэйринг:":
                            case "Пэйринг или персонажи:":
                            case "Пэйринг и персонажи:":
                                Elements pairs2 = i.select("a");
                                for (Element p : pairs2) {
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("title", p.text());
                                    map.put("url", p.attr("href").replace("/pairings/", ""));
                                    activityReference.get().FicCharacters.add(map);
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
                        activityReference.get().FicFandoms.add(map);
                    }

                    Elements authors = hat.select(".hat-creator-container");
                    for (Element v : authors) {
                        final String a_name = v.select("a.creator-nickname").text();
                        String a_url = v.select("img").attr("src");
                        String a_role = v.select("i").text();
                        String a_id = v.select("a.creator-nickname").attr("href").replace("/authors/", "");

                        activityReference.get().l_authors.post(() -> {
                            ViewAuthorWidget add_author = new ViewAuthorWidget(activityReference.get());
                            add_author.setAuthorInfo(a_id, a_name, a_role, a_url);
                            activityReference.get().l_authors.addView(add_author);
                        });

                        activityReference.get().authors_list.append(a_name).append(" ");
                    }

                    f_title = info.select("h1").get(0).ownText().trim();
                    f_direction = info.select("section").select(".badge-with-icon").get(0).text().trim();
                    if (info.select("section").select(".badge-with-icon").size() > 3) {
                        f_rating = info.select("section").select(".badge-with-icon").get(3).text().trim();
                    } else {
                        f_rating = "";
                    }
                    bundle_info.putString("rating", info.select("section").select(".badge-with-icon").get(1).text().trim());
                    bundle_info.putString("status", info.select("section").select(".badge-with-icon").get(2).text().trim());
                    f_reviews = hat.select("div.hat-actions-container").select("a").get(0).text();

                    if (f_reviews.equals("Скачать")) {
                        f_reviews = "НЕТ ОТЗЫВОВ";
                    }

                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-gen")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_gen);
                    }
                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-het")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_het);
                    }
                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-other")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_other);
                    }
                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-slash")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_slash);
                    }
                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-femslash")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_femslash);
                    }
                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-mixed")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_mixed);
                    }
                    if (info.select("section").select(".badge-with-icon").get(0).hasClass("small-direction-article")) {
                        dir_color = activityReference.get().context.getResources().getColor(R.color.fbg_article);
                    }

                    activityReference.get().title = f_title;

                    ActiveAndroid.beginTransaction();

                    String last_part_id = Parts.getLastReadedPartId(id);
                    Log.i("LPID", last_part_id + "!");
                    boolean last_part = (last_part_id == null);

                    int pos = 0;
                    HashMap<String, Object> map;
                    for (Element v : toc) {
                        String topic_title = v.select("a").get(0).text().trim();
                        String topic_time = v.select("span").text().trim();
                        String topic_id = v.select("a").attr("href").replace("readfic", "").replace(id, "").replace("/", "").replace("#part_content", "");

//                    Log.e("p", id + ":" + topic_id + ":" + topic_title + ":" + topic_time);

                        map = new HashMap<>();
                        map.put("title", topic_title);
                        map.put("time", topic_time);
                        map.put("fanfic_id", id);
                        map.put("part_id", topic_id);

                        if (activityReference.get().smart_readed && !last_part) {
                            map.put("readed", true);
                        }

                        if (topic_id.equals(last_part_id)) {
                            last_part = true;
                        }

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
                        activityReference.get().FicParts.add(map);
                    }

                    if (activityReference.get().FicParts.size() == 0) {
                        map = new HashMap<>();
                        map.put("title", "Главы не определены");
                        map.put("time", "Можно читать весь фанфик");
                        map.put("fanfic_id", id);
                        map.put("part_id", "");
                        activityReference.get().FicParts.add(map);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ActiveAndroid.inTransaction()) {
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                    }
                }

                bundle_collections.putString("id", id);
                bundle_reviews.putString("id", id);
                bundle_reviews.putString("url", "https://ficbook.net/readfic/" + id + "/comments?p=@");
                if (activityReference.get() == null) {
                    return null;
                }
                bundle_parts.putSerializable("parts", activityReference.get().FicParts);
                bundle_info.putString("id", id);
                bundle_info.putSerializable("fandoms", activityReference.get().FicFandoms);
                bundle_info.putSerializable("genres", activityReference.get().FicGenres);
                bundle_info.putSerializable("characters", activityReference.get().FicCharacters);
                bundle_info.putSerializable("pairings", activityReference.get().FicPairings);
                bundle_info.putSerializable("cautions", activityReference.get().FicCautions);
                bundle_info.putSerializable("tags", activityReference.get().FicTags);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if ((activityReference.get() != null) && (bodyx != null)) {
                    activityReference.get().tv_title.setText(f_title);
                    activityReference.get().l_direction.setBackgroundColor(dir_color);
                    if (f_rating.equals("0")) {
                        f_title = f_title.replace("0", "").trim();
                        activityReference.get().btn_rate.setText("Без оценки");
                    } else {
                        f_title = f_title.replace("+" + f_rating, "").trim();
                        activityReference.get().btn_rate.setText(activityReference.get().getString(R.string.rate_plus, f_rating));
                    }

                    activityReference.get().btn_mark.setText("\uE994 В СБОРНИК");
                    activityReference.get().tv_direction.setText(f_direction);

                    if (!activityReference.get().like) {
                        activityReference.get().btn_rate.setTextColor(activityReference.get().getResources().getColor(R.color.black));
                        activityReference.get().btn_rate.setCompoundDrawables(null, null, null, null);
                    } else {
                        activityReference.get().btn_rate.setTextColor(activityReference.get().getResources().getColor(R.color.green));
//                        Drawable image = activityReference.get().context.getResources().getDrawable(R.drawable.tri_like);
                        Drawable image = ResourcesCompat.getDrawable(activityReference.get().getResources(), R.drawable.tri_like, null);
                        if (image != null) {
                            int h = image.getIntrinsicHeight();
                            int w = image.getIntrinsicWidth();
                            image.setBounds(0, 0, w, h);
                            activityReference.get().btn_rate.setCompoundDrawables(image, null, null, null);
                        }
                    }

                    activityReference.get().fragment_info.setArguments(bundle_info);
                    activityReference.get().fragment_parts.setArguments(bundle_parts);
                    activityReference.get().fragment_reviews.setArguments(bundle_reviews);
                    activityReference.get().fragment_collections.setArguments(bundle_reviews);

                    activityReference.get().fragments.add(activityReference.get().fragment_info);
                    activityReference.get().fragments.add(activityReference.get().fragment_parts);
                    activityReference.get().fragments.add(activityReference.get().fragment_reviews);
                    activityReference.get().fragments.add(activityReference.get().fragment_collections);

                    activityReference.get().viewPager.setOffscreenPageLimit(10);
                    activityReference.get().viewPager.setAdapter(new FicPagerAdapter(activityReference.get().getSupportFragmentManager(), activityReference.get().fragments));
                    activityReference.get().tabLayout.setupWithViewPager(activityReference.get().viewPager);

                    activityReference.get().l_shadow.animate()
                            .alpha(0.0f)
                            .setDuration(500);
                    activityReference.get().pb1.setVisibility(View.GONE);

                    try {
                        if (activityReference.get().viewPager.getAdapter() != null) {
                            ((FicPagerAdapter) Objects.requireNonNull(activityReference.get().viewPager.getAdapter())).setReviewsCount(f_reviews);
                        }
                        Objects.requireNonNull(activityReference.get().viewPager.getAdapter()).notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent = activityReference.get().getIntent();
                    if (intent.hasExtra("tab")) {
                        if ("reviews".equals(Objects.requireNonNull(intent.getStringExtra("tab")))) {
                            Objects.requireNonNull(activityReference.get().tabLayout.getTabAt(2)).select();
                        }
                    }
                } else {
                    activityReference.get().pb1.setVisibility(View.GONE);
                    activityReference.get().fabReply.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(onNotificationClick);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int OPEN_DIRECTORY_REQUEST_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri directoryUri = data.getData();
            if (directoryUri == null) {
                return;
            }
            this.getContentResolver().takePersistableUriPermission(directoryUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
