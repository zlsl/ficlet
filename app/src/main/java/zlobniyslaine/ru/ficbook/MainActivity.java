package zlobniyslaine.ru.ficbook;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import static zlobniyslaine.ru.ficbook.Application.PingHost;
import static zlobniyslaine.ru.ficbook.Application.getContext;
import static zlobniyslaine.ru.ficbook.Application.sPref;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import de.cketti.changelog.dialog.DialogChangeLog;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.controls.LocalWebView;
import zlobniyslaine.ru.ficbook.models.Authors;
import zlobniyslaine.ru.ficbook.models.Category;
import zlobniyslaine.ru.ficbook.models.Fandoms;
import zlobniyslaine.ru.ficbook.models.Fanfic;
import zlobniyslaine.ru.ficbook.models.FanficPage;
import zlobniyslaine.ru.ficbook.models.Feeds;
import zlobniyslaine.ru.ficbook.models.Parts;
import zlobniyslaine.ru.ficbook.models.Tags;
import zlobniyslaine.ru.ficbook.models.TagsCategory;
import zlobniyslaine.ru.ficbook.pagers.SectionPagerAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private final int DR_ID_FEEDS = 10001000;
    private final int DR_ID_RQ_SEARCH = 10001001;
    private final int DR_ID_RANDOM = 10001002;
    private final int DR_ID_CRIT = 10001005;
    private final int DR_ID_AUTHORS_POPULAR = 10001006;
    private final int DR_ID_AUTHORS_SEARCH = 10001007;

    private final int DR_ID_POPULAR_EX = 10001100;
    private final int DR_ID_POP_GEN = 10001101;
    private final int DR_ID_POP_GET = 10001102;
    private final int DR_ID_POP_SLASH = 10001103;
    private final int DR_ID_POP_FEMSLASH = 10001104;
    private final int DR_ID_POP_ARTICLE = 10001105;
    private final int DR_ID_POP_MIXED = 10001106;
    private final int DR_ID_POP_OTHER = 10001107;

    private final int DR_ID_PROFILE = 10002000;
    private final int DR_ID_MESSAGING = 10002001;

    private final int DR_ID_FAN_MY = 10002101;
    private final int DR_ID_FAN_TEXT = 10002102;
    private final int DR_ID_FAN_BETA = 10002103;
    private final int DR_ID_FAN_VIEWED = 10002104;
    private final int DR_ID_FAN_READED = 10002105;

    private final int DR_ID_REQ_MY = 10002201;
    private final int DR_ID_REQ_INT = 10002202;
    private final int DR_ID_REQ_FAV = 10002203;
    private final int DR_ID_REQ_LIK = 10002204;
    private final int DR_ID_REQ_MYF = 10002205;

    private final int DR_ID_LIKED = 10002004;
    private final int DR_ID_LIKED_AUTHORS = 10002005;
    private final int DR_ID_LIKED_FANFICS = 10002006;
    private final int DR_ID_FOLLOWS = 10002007;
    private final int DR_ID_LOGIN = 10005000;
    private final int DR_ID_LOGIN_VK = 10005001;
    private final int DR_ID_CHAT = 10007000;
    private final int DR_ID_SETTINGS = 10008888;
    private final int DR_ID_EXIT = 10009999;

    private Boolean auth_displayed = false;
    private Drawer mainDrawer;
    private PrimaryDrawerItem drChat;
    private ProfileDrawerItem ficbookProfile;

    private Fragment fragment_lenta;
    private Fragment fragment_cache;

    private AlertDialog captchaDialog;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Toolbar toolbar;

    private BroadcastReceiver mNetworkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        Application.adjustFontScale(this);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.my_tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Лента"));
        tabLayout.addTab(tabLayout.newTab().setText("Категории"));
        tabLayout.addTab(tabLayout.newTab().setText("Сборники"));
        tabLayout.addTab(tabLayout.newTab().setText("Кэш"));

        viewPager = findViewById(R.id.my_view_pager);

        InitSettings();

        InitInterface();
        if (Application.isInternetAvailable()) {
            StartLoaders();
            Application.SyncAuthorsFics();
        }

        new Thread(() -> {
            InitServices();
            Application.StartWorkNotificationCheck();
        }).start();
        bio();
    }

    private void InitSettings() {
        if (sPref != null) {
            Application.C_phpsessid = Application.sPref.getString("PHPSESSID", "");
            Application.C_remme = Application.sPref.getString("remme", "");
            Application.C_remme2 = Application.sPref.getString("remme2", "");
            Application.C_cfduid = Application.sPref.getString("cfduid", "");
            Application.A_login = Application.sPref.getString("login", "");
            Application.A_password = Application.sPref.getString("password", "");
            Application.user_avatar = Application.sPref.getString("user_avatar", "");
            Application.user_name = Application.sPref.getString("user_name", "");
            Application.vk_login = Application.sPref.getBoolean("vk_login", false);
            Application.social_login = Application.sPref.getString("social_login", "");
        }
        try {
            if (Fanfic.getCount() == 0) {
                if (Feeds.getCount() < 3) {
                    Application.AddDefaultFeeds();
                }

//                SharedPreferences.Editor ed = Application.sPref.edit();
//                ed.putBoolean("filter_slash", true);
//                ed.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void InitServices() {
        mNetworkReceiver = new NetworkChangeReceiver();
        registerNetworkBroadcastForNougat();
        Application.setNetworkAvailableListener(available -> Log.d("NETWORK", available.toString()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED
                    || (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    || (checkSelfPermission(Manifest.permission.INSTALL_SHORTCUT) != PackageManager.PERMISSION_GRANTED)) {
                int permissions_code = 42;
                String[] permissions = {Manifest.permission.WRITE_SETTINGS, WRITE_EXTERNAL_STORAGE, Manifest.permission.INSTALL_SHORTCUT};
                ActivityCompat.requestPermissions(this, permissions, permissions_code);
            }
        }

        if (Application.isInternetAvailable()) {
            if (!Application.isGuest()) {
                Application.checkNotifications();
            }

            if ( (!Application.sPref.contains("update" + BuildConfig.VERSION_NAME)) || (Tags.getCount() == 0) ) {
                if (!Application.sPref.contains("update" + BuildConfig.VERSION_NAME)) {
                    SharedPreferences.Editor ed = Application.sPref.edit();
                    ed.putBoolean("update" + BuildConfig.VERSION_NAME, true);
                    ed.apply();

                    if (Fanfic.getCount() > 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> new Thread(() -> new Thread(Application::refreshSync).start()).start(), 15000);
                    }
                }

                Application.updateBasesRemote();
            }

            new Thread(() -> {
                try {
                    Application.syncCache();
                    Log.d("SYNC", "pack");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                PingHost(this, "ficbook.net");
            }).start();
        } else {
            LinearLayout mRoot = findViewById(R.id.content_main);
            Snackbar snackbar = Snackbar.make(mRoot, "Нет подключения к сети!", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }


    private void setInterface(Boolean auth) {
        Log.d("INTERFACE", auth.toString());
        if (auth) {
            if (!auth_displayed) {
                LinearLayout mRoot = findViewById(R.id.content_main);
                Snackbar snackbar = Snackbar.make(mRoot, "Успешный вход", Snackbar.LENGTH_SHORT);
                snackbar.show();
                auth_displayed = true;
            }
            ((FragmentFanficList) fragment_lenta).reloadContent(Application.getMainFeed());

            UpdateFace();
            initDrawerFull();
        } else {
            UpdateFace();
            initDrawerGuest();

            LinearLayout mRoot = findViewById(R.id.content_main);
            Snackbar snackbar = Snackbar.make(mRoot, "Вход не произведен", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void InitInterface() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(getContext()).cancelRequest(imageView);
            }
        });

        ficbookProfile = new ProfileDrawerItem().withName("Гость").withEmail("Вход не выполнен");

        initDrawerGuest();

        Application.ficAuth.setOnTechListener(() -> Toast.makeText(getContext(), "На фикбуке технические работы!", Toast.LENGTH_LONG).show());
        Application.ficAuth.setOnAuthListener(success -> {
            if (Application.isInternetAvailable()) {
                try {
                    FetchProfile();
                    Log.i("RELOAD", "lenta");
                    new Handler(this.getMainLooper()).postDelayed(() -> ((FragmentFanficList) fragment_lenta).reloadContent(Application.getMainFeed()), 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setInterface(success);
        });
        Application.ficAuth.setOnCaptchaListener(this::ShowCaptchaDialog);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ActivitySearch.class);
            startActivity(intent);
        });

        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = new Vector<>();

        Bundle bundle_lenta = new Bundle();
        bundle_lenta.putString("url", Application.getMainFeed());
        fragment_lenta = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFanficList.class.getName());
        fragment_lenta.setArguments(bundle_lenta);

        Bundle bundle_cache = new Bundle();
        bundle_cache.putBoolean("cache", true);
        fragment_cache = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFanficList.class.getName());
        fragment_cache.setArguments(bundle_cache);

        Fragment fragment_collections = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentMain_Collections.class.getName());

        fragments.add(fragment_lenta);
        fragments.add(fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentMain_Category.class.getName()));
        fragments.add(fragment_collections);
        fragments.add(fragment_cache);

        viewPager.setOffscreenPageLimit(10);
        viewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);

        if (!Application.isGuest()) {
            UpdateFace();
        }
    }

    private void StartLoaders() {
        if (Application.vk_login) {
            Log.i("AUTH", Application.social_login);
            Application.ficAuth.ShowOauthDialog(this);
            FetchProfile();
        } else {
            if (!Objects.requireNonNull(sPref.getString("login", "")).isEmpty()) {
                Log.i("DO", "Login");
                Application.ficAuth.StartAuth();
            } else {
                FetchProfile();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        try {
            Field[] fields = menu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(menu);
                    Class<?> classPopupHelper = null;
                    if (menuPopupHelper != null) {
                        classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    }
                    Method setForceIcons;
                    if (classPopupHelper != null) {
                        setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ActivitySettings.class);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.action_global_filter) {
            Intent intent = new Intent(this, ActivityGlobalFilter.class);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.action_changelog) {
            try {
                DialogChangeLog cl = DialogChangeLog.newInstance(this);
                cl.getLogDialog().show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }


        if (id == R.id.action_open_by_id) {
            try {
                EditText fn = new EditText(this);
                fn.setRawInputType(Configuration.KEYBOARD_12KEY);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Открыть фанфик")
                        .setMessage("Введите ID фанфика (число)")
                        .setView(fn)
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            try {
                                Intent intent = new Intent(this, ActivityFanfic.class);
                                intent.putExtra("id", fn.getText().toString());
                                intent.putExtra("title", "");
                                if (Application.device_ok) {
                                    startActivity(intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        })
                        .setNegativeButton("Отмена", null)
                        .create();
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }


        if (id == R.id.action_about) {
            Intent intent = new Intent(this, ActivityAbout.class);
            this.startActivity(intent);
            return true;
        }

        if (id == R.id.action_flush) {
            ClearCache();
            return true;
        }

        if (id == R.id.action_update) {
            Application.updateBasesRemote();
            new Thread(Application::refreshSync).start();

            return true;
        }

        if (id == R.id.action_export) {
            final CharSequence[] xt = new CharSequence[] {"В одну папку","По авторам", "По фэндомам", "По направленностям"};
            AtomicInteger w = new AtomicInteger();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Выгрузка кэша")
                    .setSingleChoiceItems(xt, 0, (dialog, which) -> w.set(which))
                    .setPositiveButton("Выгрузить", (dialog, which) -> {
                        CopyCache(w.get());
                        dialog.dismiss();
                    });
            builder.create().show();
        }

        if (id == R.id.action_stats) {
            String stat_info = "";
            stat_info = stat_info.concat("Фэндомы: " + Fandoms.getCount("") + "\n");
            stat_info = stat_info.concat("Категории меток: " + TagsCategory.getCount() + "\n");
            stat_info = stat_info.concat("Метки: " + Tags.getCount() + "\n");
            stat_info = stat_info.concat("Категории: " + Category.getCount() + "\n");
            stat_info = stat_info.concat("------------------\n");
            stat_info = stat_info.concat("Авторы: " + Authors.getCount() + "\n");
            stat_info = stat_info.concat("Главы: " + Parts.getCount() + "\n");
            stat_info = stat_info.concat("Фанфики: " + Fanfic.getCount() + "\n");
            stat_info = stat_info.concat("Закладки: " + FanficPage.getCount() + "\n");
            stat_info = stat_info.concat("Ленты: " + Feeds.getCount() + "\n");

            int cache_cnt = 0;
            int cache_size = 0;
            File[] files = getFilesDir().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().contains("fb2.zip2")) {
                        cache_cnt = cache_cnt + 1;
                        cache_size = cache_size + (int) file.length();
                        Log.d("CACHE", file.getName() + " " + file.length());
                    }
                }
            }

            stat_info = stat_info + "В кэше: " + cache_cnt + " файлов\n";
            stat_info = stat_info + "Размер кэша: " + cache_size / 1024 / 1024 + " Мб\n";

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage(stat_info);
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    (dialog, id1) -> dialog.cancel());
            AlertDialog alert11 = builder1.create();
            alert11.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerItem(item.getItemId());

        try {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Application.isInternetAvailable()) {
            if (drChat != null) {
                if (!Application.chat_last_id.equals("0")) {
                    drChat.withBadge(Application.chat_last_id);
                } else {
                    drChat.withBadge("");
                }
                mainDrawer.updateItem(drChat);
            }
            try {
                Application.C_phpsessid = sPref.getString("PHPSESSID", "");
                Application.C_remme = sPref.getString("remme", "");
                Application.C_remme2 = sPref.getString("remme2", "");
                Application.C_cfduid = sPref.getString("cfduid", "");
                Application.A_login = sPref.getString("login", "");
                Application.A_password = sPref.getString("password", "");
                Application.user_avatar = sPref.getString("user_avatar", "");
                Application.user_name = sPref.getString("user_name", "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Application.A_password.equals("vk_oauth")) {
                Application.A_password = "vk_oauth_ok";
                Application.vk_login = true;
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("PHPSESSID", Application.C_phpsessid);
                ed.putString("remme", Application.C_remme);
                ed.putString("remme2", Application.C_remme2);
                ed.putString("cfduid", Application.C_cfduid);
                ed.putString("login", Application.A_login);
                ed.putString("password", Application.A_password);
                ed.putString("user_name", Application.user_name);
                ed.putBoolean("vk_login", true);
                ed.apply();

                Application.setGuest(false);
                initDrawerFull();
                Log.i("AUTH", "OK");

                if (!auth_displayed) {
                    LinearLayout mRoot = findViewById(R.id.content_main);
                    Snackbar snackbar = Snackbar.make(mRoot, "Успешный вход", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    auth_displayed = true;
                }

                Application.checkNotifications();
                ((FragmentFanficList) fragment_lenta).reloadContent(Application.getMainFeed());
                UpdateFace();
            }
            Application.FetchFeed("https://ficbook.net/find?fandom_filter=any&fandom_group_id=1&pages_range=1&pages_min=&pages_max=&transl=1&likes_min=&likes_max=&rewards_min=&date_create_min=&date_create_max=&date_update_min=&date_update_max=&title=&sort=3&rnd=767263295&find=%D0%9D%D0%B0%D0%B9%D1%82%D0%B8\\u0021&p=1");
        }

        if (fragment_cache != null) {
            ((FragmentFanficList) fragment_cache).UpdateData();
        }
    }

    private void ParseLogin(Boolean profile_block) {
        try {
            setInterface(profile_block);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateFace() {
        try {
            if (!Application.getUser_name().isEmpty()) {
                ficbookProfile.withName(Application.user_name);
                ficbookProfile.withIcon(Application.user_avatar);
                ficbookProfile.withEmail("");
            } else {
                ficbookProfile.withName("Гость");
                ficbookProfile.withEmail("Вход не выполнен");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ClearCache() {
        Application.ClearCache();
        LinearLayout mRoot = findViewById(R.id.content_main);
        Snackbar snackbar = Snackbar.make(mRoot, "Произведена очистка кэша", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void ShowCaptchaDialog() {
        try {
            Log.e("VALIDATE", "start");
            AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            View alertDialogView = inflater.inflate(R.layout.dialog_captcha, null);
            captchaDialogBuilder.setView(alertDialogView);

            final LocalWebView wv_captcha = alertDialogView.findViewById(R.id.wv_captcha);

            captchaDialogBuilder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

            WebSettings webSettings = wv_captcha.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setUserAgentString(Application.USER_AGENT);
            wv_captcha.setWebChromeClient(new WebChromeClient());
            wv_captcha.setBackgroundColor(Color.TRANSPARENT);
            wv_captcha.setVerticalScrollBarEnabled(false);
            wv_captcha.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

            wv_captcha.setWebViewClient(new android.webkit.WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(wv_captcha, url);
                    Log.i("OPF", url);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.i("OPF", "All the cookies in a string:" + cookies);

                    if (!url.equals("https://ficbook.net/?c") && (url.equals("https://ficbook.net/") || url.contains("cdn-cgi"))) {
                        Log.i("CAPTCHA", "success");
                        Application.C_phpsessid = Application.getCookie("PHPSESSID");
                        Application.C_cfduid = Application.getCookie("__cfduid");
                        Application.C_cfclearance = Application.getCookie("cf_clearance");
                        SharedPreferences.Editor ed = Application.sPref.edit();
                        ed.putString("PHPSESSID", Application.C_phpsessid);
                        ed.putString("cfduid", Application.C_cfduid);
                        ed.putString("cf_clearance", Application.C_cfclearance);
                        ed.apply();

                        if (Application.vk_login) {
                            //new fetcher_profile(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            FetchProfile();
                        } else {
                            if (!Objects.requireNonNull(sPref.getString("login", "")).isEmpty()) {
                                if (Application.isInternetAvailable()) {
                                    Application.ficAuth.StartAuth();
                                }
                            }
                        }
                        captchaDialog.dismiss();
                    }
                }
            });

            captchaDialog = captchaDialogBuilder.create();
            captchaDialog.show();

            wv_captcha.loadUrl("https://ficbook.net/");
            Log.e("VALIDATE", "exec");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void ShowValidateBrowserDialog() {
        try {
            AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            View alertDialogView = inflater.inflate(R.layout.dialog_validate, null);
            captchaDialogBuilder.setView(alertDialogView);

            final LocalWebView wv_captcha = alertDialogView.findViewById(R.id.wv_captcha);

            captchaDialogBuilder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

            WebChromeClient wc = new WebChromeClient();

            wv_captcha.setWebChromeClient(wc);
            WebSettings webSettings = wv_captcha.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setUserAgentString(Application.USER_AGENT2);

            wv_captcha.setBackgroundColor(Color.TRANSPARENT);
            wv_captcha.setVerticalScrollBarEnabled(false);
            wv_captcha.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
            wv_captcha.setWebViewClient(new android.webkit.WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    Log.i("VALIDATE", "Url " + url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(wv_captcha, url);
                    Log.i("OPF", url);
                    if (url.contains("https://ficbook.net/?__cf_chl")) {
                        wv_captcha.setVisibility(View.GONE);
                        Log.i("OPF", "HIDE");
                    }
                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.i("OPF", "All the cookies in a string:" + cookies);

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(() -> {
                        String cookies1 = CookieManager.getInstance().getCookie(url);
                        Log.i("OPF", "D All the cookies in a string:" + cookies1);
                        if (/*opf &&*/ (cookies1.contains("cf_clearance"))) {
                            Log.i("VALIDATE", "success");
                            Application.C_phpsessid = Application.getCookie("PHPSESSID");
                            Application.C_cfduid = Application.getCookie("__cfduid");
                            if (Application.getCookie("cf_clearance") != null) {
                                if (!Application.getCookie("cf_clearance").isEmpty()) {
                                    Application.C_cfclearance = Application.getCookie("cf_clearance");
                                }
                            }
                            SharedPreferences.Editor ed = Application.sPref.edit();
                            ed.putString("PHPSESSID", Application.C_phpsessid);
                            ed.putString("cfduid", Application.C_cfduid);
                            ed.putString("cf_clearance", Application.C_cfclearance);
                            ed.apply();
                            Application.clear503();
                            handler.postDelayed(() -> StartLoaders(),
                                    2000);
                            captchaDialog.dismiss();
                        } else {
                            Log.w("VALIDATE", "Fail, retry...");
                        }
                    }, 1000);
                }
            });

            captchaDialog = captchaDialogBuilder.create();
            captchaDialog.show();

            wv_captcha.loadUrl("https://ficbook.net/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            if (mNetworkReceiver != null) {
                unregisterReceiver(mNetworkReceiver);
                mNetworkReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterNetworkChanges();
    }

    public void initDrawerGuest() {
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        new ProfileDrawerItem().withName("Гость").withEmail("Вход не выполнен") //.withIcon(getResources().getDrawable(R.mipmap.ic_noavatar)
                )
                .withOnAccountHeaderListener((view, profile, currentProfile) -> {
                    FicAuth.login(getContext());
                    return false;
                })
                .build();

        mainDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Ленты").withIdentifier(DR_ID_FEEDS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_library_books, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Поиск заявок").withIdentifier(DR_ID_RQ_SEARCH).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_find_in_page, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Случайный фанфик").withIdentifier(DR_ID_RANDOM).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cached_black_24dp, null)).withSelectable(false),

                        new ExpandableDrawerItem().withName("Популярное").withIdentifier(DR_ID_POPULAR_EX).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_home_black_24dp, null)).withSubItems(
                                new SecondaryDrawerItem().withName("Джен").withIdentifier(DR_ID_POP_GEN).withSelectable(false),
                                new SecondaryDrawerItem().withName("Гет").withIdentifier(DR_ID_POP_GET).withSelectable(false),
                                new SecondaryDrawerItem().withName("Статьи").withIdentifier(DR_ID_POP_ARTICLE).withSelectable(false),
                                new SecondaryDrawerItem().withName("Смешанное").withIdentifier(DR_ID_POP_MIXED).withSelectable(false),
                                new SecondaryDrawerItem().withName("Другое").withIdentifier(DR_ID_POP_OTHER).withSelectable(false)
                        ).withSelectable(false),

//                        new PrimaryDrawerItem().withName("Ждут критики").withIdentifier(DR_ID_CRIT).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_announcement_black_24dp, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Популярные авторы").withIdentifier(DR_ID_AUTHORS_POPULAR).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_face, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Поиск автора").withIdentifier(DR_ID_AUTHORS_SEARCH).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_face, null)).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Настройки").withIdentifier(DR_ID_SETTINGS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_settings, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Вход").withIdentifier(DR_ID_LOGIN).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_account_circle_black_24dp, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Вход ВКонтакте").withIdentifier(DR_ID_LOGIN_VK).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.vk, null)).withSelectable(false)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    Log.d("DRI", drawerItem.getIdentifier() + "!");
                    drawerItem(drawerItem.getIdentifier());
                    return false;
                })
                .build();
    }

    public void initDrawerFull() {
        if (mainDrawer != null) {
            try {
                mainDrawer.closeDrawer();
                mainDrawer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        drChat = new PrimaryDrawerItem().withName("Чат").withIdentifier(DR_ID_CHAT).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_forum_black_24dp, null)).withSelectable(false);
        AccountHeader mainHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        ficbookProfile
                )
                .withOnAccountHeaderListener((view, profile, currentProfile) -> {
                    if (!Application.isGuest()) {
                        Intent intent = new Intent(getContext(), ActivityProfile.class);
                        startActivity(intent);
                    }

                    return false;
                })
                .build();

        int DR_ID_REQUESTS_EX = 10002200;
        int DR_ID_FANFIC_EX = 10002100;
        mainDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(mainHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Ленты").withIdentifier(DR_ID_FEEDS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_library_books, null)).withSelectable(false),
                        drChat,
                        new PrimaryDrawerItem().withName("Поиск заявок").withIdentifier(DR_ID_RQ_SEARCH).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_find_in_page, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Случайный фанфик").withIdentifier(DR_ID_RANDOM).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cached_black_24dp, null)).withSelectable(false),

                        new ExpandableDrawerItem().withName("Популярное").withIdentifier(DR_ID_POPULAR_EX).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_thumb_up_black_24dp, null)).withSubItems(
                                new SecondaryDrawerItem().withName("Джен").withIdentifier(DR_ID_POP_GEN).withSelectable(false),
                                new SecondaryDrawerItem().withName("Гет").withIdentifier(DR_ID_POP_GET).withSelectable(false),
                                new SecondaryDrawerItem().withName("Статьи").withIdentifier(DR_ID_POP_ARTICLE).withSelectable(false),
                                new SecondaryDrawerItem().withName("Смешанное").withIdentifier(DR_ID_POP_MIXED).withSelectable(false),
                                new SecondaryDrawerItem().withName("Другое").withIdentifier(DR_ID_POP_OTHER).withSelectable(false)
                        ).withSelectable(false),

//                        new PrimaryDrawerItem().withName("Ждут критики").withIdentifier(DR_ID_CRIT).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_announcement_black_24dp, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Популярные авторы").withIdentifier(DR_ID_AUTHORS_POPULAR).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_face, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Поиск автора").withIdentifier(DR_ID_AUTHORS_SEARCH).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_face, null)).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Профиль").withIdentifier(DR_ID_PROFILE).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_sentiment_satisfied, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Личные сообщения").withIdentifier(DR_ID_MESSAGING).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_message_black_24dp, null)).withSelectable(false),

                        new ExpandableDrawerItem().withName("Фанфики").withIdentifier(DR_ID_FANFIC_EX).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_home_black_24dp, null)).withSubItems(
                                new SecondaryDrawerItem().withName("Мои фанфики").withIdentifier(DR_ID_FAN_MY).withSelectable(false),
                                new SecondaryDrawerItem().withName("История - Мои тексты").withIdentifier(DR_ID_FAN_TEXT).withSelectable(false),
                                new SecondaryDrawerItem().withName("История - Бета / Соавтор").withIdentifier(DR_ID_FAN_BETA).withSelectable(false),
                                new SecondaryDrawerItem().withName("Просмотренные фанфики").withIdentifier(DR_ID_FAN_VIEWED).withSelectable(false),
                                new SecondaryDrawerItem().withName("Прочитанные фанфики").withIdentifier(DR_ID_FAN_READED).withSelectable(false)
                        ).withSelectable(false),

                        new ExpandableDrawerItem().withName("Заявки").withIdentifier(DR_ID_REQUESTS_EX).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_hourglass_empty, null)).withSubItems(
                                new SecondaryDrawerItem().withName("Мои заявки").withIdentifier(DR_ID_REQ_MY).withSelectable(false),
                                new SecondaryDrawerItem().withName("Интересные мне").withIdentifier(DR_ID_REQ_INT).withSelectable(false),
                                new SecondaryDrawerItem().withName("Заявки в закладках").withIdentifier(DR_ID_REQ_FAV).withSelectable(false),
                                new SecondaryDrawerItem().withName("Фанфики по интересным заявкам").withIdentifier(DR_ID_REQ_LIK).withSelectable(false),
                                new SecondaryDrawerItem().withName("Фанфики по моим заявкам").withIdentifier(DR_ID_REQ_MYF).withSelectable(false)
                        ).withSelectable(false),

                        new PrimaryDrawerItem().withName("Понравившиеся работы").withIdentifier(DR_ID_LIKED).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_grade_black_24dp, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Избранные авторы").withIdentifier(DR_ID_LIKED_AUTHORS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_favorite_black_24dp, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Лента избранных").withIdentifier(DR_ID_LIKED_FANFICS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_loyalty_black_24dp, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Обсуждения").withIdentifier(DR_ID_FOLLOWS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_rate_review, null)).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Настройки").withIdentifier(DR_ID_SETTINGS).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_settings, null)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Выход").withIdentifier(DR_ID_EXIT).withIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_exit, null)).withSelectable(false)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    Log.d("DRI", drawerItem.getIdentifier() + "!");
                    drawerItem(drawerItem.getIdentifier());
                    return false;
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        if (drChat != null) {
                            if (!Application.chat_last_id.equals("0")) {
                                drChat.withBadge(Application.chat_last_id);
                            } else {
                                drChat.withBadge("");
                            }
                            mainDrawer.updateItem(drChat);
                        }
                    }
                })
                .build();
    }

    public void drawerItem(long id) {
        if (id == R.id.nav_notifications) {
            Application.checkNotifications();
            Intent intent = new Intent(this, ActivityNotifications.class);
            this.startActivity(intent);
        }

        if ((id == R.id.nav_settings) || (id == DR_ID_SETTINGS)) {
            Intent intent = new Intent(this, ActivitySettings.class);
            this.startActivity(intent);
        }
        if (id == DR_ID_CHAT) {
            if (Application.getUser_name().isEmpty()) {
                Toast.makeText(this, "Не выполнен вход", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Application.device_ok) {
                Toast.makeText(this, "Инвайт не подтверждён", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ActivityChatChannels.class);
            this.startActivity(intent);
        }

        if ((id == R.id.nav_login) || (id == DR_ID_LOGIN)) {
            FicAuth.login(this);
        }

        if ((id == R.id.nav_logoff) || (id == DR_ID_EXIT)) {
            Application.ficAuth.logoff();
        }

        if ((id == R.id.nav_login_social_vk) || (id == DR_ID_LOGIN_VK)) {
            Application.social_login = "vk";
            Application.ficAuth.ShowOauthDialog(this);
        }

        if ((id == R.id.nav_random) || (id == DR_ID_RANDOM)) {
            Intent intent = new Intent(this, ActivityFanfic.class);
            intent.putExtra("random", "random");
            this.startActivity(intent);
        }

        if ((id == R.id.nav_requests_search) || (id == DR_ID_RQ_SEARCH)) {
            Intent intent3 = new Intent(this, ActivityRequestsSearch.class);
            this.startActivity(intent3);
        }

        if ((id == R.id.nav_feeds) || (id == DR_ID_FEEDS)) {
            Intent intent = new Intent(this, ActivityFeeds.class);
            this.startActivity(intent);
        }

        if ((id == R.id.nav_authors_search) || (id == DR_ID_AUTHORS_SEARCH)) {
            Intent intent = new Intent(this, ActivityAuthorsSearch.class);
            this.startActivity(intent);
        }

        if ((id == R.id.nav_profile) || (id == DR_ID_PROFILE)) {
            if (!Application.isGuest()) {
                Intent intent = new Intent(this, ActivityProfile.class);
                this.startActivity(intent);
            }
        }

        if (id == DR_ID_FAN_MY) {
            Intent intent0 = new Intent(this, ActivityWorks.class);
            this.startActivity(intent0);
        }
        if (id == DR_ID_FAN_TEXT) {
            Intent intent1 = new Intent(this, ActivityChanges.class);
            intent1.putExtra("url", "https://ficbook.net/home/versions?p=@");
            intent1.putExtra("title", "История ваших текстов");
            this.startActivity(intent1);
        }
        if (id == DR_ID_FAN_BETA) {
            Intent intent2 = new Intent(this, ActivityChanges.class);
            intent2.putExtra("url", "https://ficbook.net/home/versions/participated?p=@");
            intent2.putExtra("title", "История бета / соавтор");
            this.startActivity(intent2);
        }
        if (id == DR_ID_FAN_READED) {
            Intent intent4 = new Intent(this, ActivityFanficList.class);
            intent4.putExtra("url", "https://ficbook.net/home/readedList?p=@");
            intent4.putExtra("title", "Прочитанные работы");
            this.startActivity(intent4);
        }
        if (id == DR_ID_FAN_VIEWED) {
            Intent intent3 = new Intent(this, ActivityFanficList.class);
            intent3.putExtra("url", "https://ficbook.net/home/visitedList?p=@");
            intent3.putExtra("title", "Просмотренные работы");
            this.startActivity(intent3);
        }

        int DR_ID_FANFIC = 10002002;
        if ((id == R.id.nav_fanfic_root) || (id == DR_ID_FANFIC)) {
            String[] items = {"Мои фанфики", "История - Мои тексты", "История - Бета / Соавтор", "Просмотреные фанфики", "Прочитанные фанфики"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Фанфики");
            builder.setItems(items, (dialog, item1) -> {
                switch (item1) {
                    case 0:
                        Intent intent0 = new Intent(this, ActivityWorks.class);
                        this.startActivity(intent0);
                        break;

                    case 1:
                        Intent intent1 = new Intent(this, ActivityChanges.class);
                        intent1.putExtra("url", "https://ficbook.net/home/versions?p=@");
                        intent1.putExtra("title", "История ваших текстов");
                        this.startActivity(intent1);
                        break;

                    case 2:
                        Intent intent2 = new Intent(this, ActivityChanges.class);
                        intent2.putExtra("url", "https://ficbook.net/home/versions/participated?p=@");
                        intent2.putExtra("title", "История бета / соавтор");
                        this.startActivity(intent2);
                        break;

                    case 3:
                        Intent intent3 = new Intent(this, ActivityFanficList.class);
                        intent3.putExtra("url", "https://ficbook.net/home/visitedList?p=@");
                        intent3.putExtra("title", "Просмотренные работы");
                        this.startActivity(intent3);
                        break;

                    case 4:
                        Intent intent4 = new Intent(this, ActivityFanficList.class);
                        intent4.putExtra("url", "https://ficbook.net/home/readedList?p=@");
                        intent4.putExtra("title", "Прочитанные работы");
                        this.startActivity(intent4);
                        break;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if (id == DR_ID_REQ_MY) {
            Intent intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
            intent.putExtra("url", "https://ficbook.net/home/my_requests?p=@");
            intent.putExtra("title", "Мои заявки");
            startActivity(intent);
        }
        if (id == DR_ID_REQ_INT) {
            Intent intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
            intent.putExtra("url", "https://ficbook.net/home/liked_requests/list?p=@");
            intent.putExtra("title", "Интересные мне заявки");
            startActivity(intent);
        }
        if (id == DR_ID_REQ_FAV) {
            Intent intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
            intent.putExtra("url", "https://ficbook.net/home/bookmarked_requests?p=@");
            intent.putExtra("title", "Заявки в закладках");
            startActivity(intent);
        }
        if (id == DR_ID_REQ_LIK) {
            Intent intent = new Intent(getApplicationContext(), ActivityFicByRequestList.class);
            intent.putExtra("url", "https://ficbook.net/home/liked_requests?p=@");
            intent.putExtra("title", "Фанфики по интересным заявкам");
            startActivity(intent);
        }
        if (id == DR_ID_REQ_MYF) {
            Intent intent = new Intent(getApplicationContext(), ActivityFicByRequestList.class);
            intent.putExtra("url", "https://ficbook.net/home/my_requests_fanfics?p=@");
            intent.putExtra("title", "Фанфики по моим заявкам");
            startActivity(intent);
        }

        int DR_ID_REQUESTS = 10002003;
        if ((id == R.id.nav_requests) || (id == DR_ID_REQUESTS)) {
            String[] items = {"Мои заявки", "Интересные мне", "Заявки в закладках", "Фанфики по интересным заявкам", "Фанфики по моим заявкам"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Заявки");
            builder.setItems(items, (dialog, item12) -> {
                Intent intent;
                switch (item12) {
                    case 0:
                        intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
                        intent.putExtra("url", "https://ficbook.net/home/my_requests?p=@");
                        intent.putExtra("title", "Мои заявки");
                        startActivity(intent);
                        break;

                    case 1:
                        intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
                        intent.putExtra("url", "https://ficbook.net/home/liked_requests/list?p=@");
                        intent.putExtra("title", "Интересные мне заявки");
                        startActivity(intent);
                        break;

                    case 2:
                        intent = new Intent(getApplicationContext(), ActivityRequestsList.class);
                        intent.putExtra("url", "https://ficbook.net/home/bookmarked_requests?p=@");
                        intent.putExtra("title", "Заявки в закладках");
                        startActivity(intent);
                        break;

                    case 3:
                        intent = new Intent(getApplicationContext(), ActivityFicByRequestList.class);
                        intent.putExtra("url", "https://ficbook.net/home/liked_requests?p=@");
                        intent.putExtra("title", "Фанфики по интересным заявкам");
                        startActivity(intent);
                        break;

                    case 4:
                        intent = new Intent(getApplicationContext(), ActivityFicByRequestList.class);
                        intent.putExtra("url", "https://ficbook.net/home/my_requests_fanfics?p=@");
                        intent.putExtra("title", "Фанфики по моим заявкам");
                        startActivity(intent);

                        break;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if ((id == R.id.nav_liked_authors) || (id == DR_ID_LIKED_AUTHORS)) {
            Intent intent = new Intent(this, ActivityAuthors.class);
            intent.putExtra("url", "https://ficbook.net/home/favourites/authors");
            intent.putExtra("title", "Избранные авторы");
            this.startActivity(intent);
        }

        if ((id == R.id.nav_popular_authors) || (id == DR_ID_AUTHORS_POPULAR)) {
            Intent intent = new Intent(this, ActivityAuthors.class);
            intent.putExtra("url", "https://ficbook.net/authors");
            intent.putExtra("title", "Популярные авторы");
            this.startActivity(intent);
        }

        if ((id == R.id.nav_liked) || (id == DR_ID_LIKED)) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/home/liked_fanfics?p=@");
            intent.putExtra("title", "Понравившиеся работы");
            this.startActivity(intent);
        }

        if ((id == R.id.nav_liked_fanfics) || (id == DR_ID_LIKED_FANFICS)) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/home/favourites?p=@");
            intent.putExtra("title", "Лента избранных");
            this.startActivity(intent);
        }

        if ((id == R.id.nav_premium) || (id == DR_ID_CRIT)) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/premium_fanfiction?p=@");
            intent.putExtra("title", "Ждут критики");
            this.startActivity(intent);
        }

        if (id == DR_ID_POP_GEN) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/popular/gen");
            intent.putExtra("title", "Популярное - Джен");
            this.startActivity(intent);
        }
        if (id == DR_ID_POP_GET) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/popular/het");
            intent.putExtra("title", "Популярное - Гет");
            this.startActivity(intent);
        }
        if (id == DR_ID_POP_ARTICLE) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/popular/article");
            intent.putExtra("title", "Популярное - Статьи");
            this.startActivity(intent);
        }
        if (id == DR_ID_POP_MIXED) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/popular/mixed");
            intent.putExtra("title", "Популярное - Смешанное");
            this.startActivity(intent);
        }
        if (id == DR_ID_POP_OTHER) {
            Intent intent = new Intent(this, ActivityFanficList.class);
            intent.putExtra("url", "https://ficbook.net/popular/other");
            intent.putExtra("title", "Популярное - Прочее");
            this.startActivity(intent);
        }

        int DR_ID_POPULAR = 10001003;
        if ((id == R.id.nav_popular) || (id == DR_ID_POPULAR)) {
            String[] items = {"Джен", "Гет", "Статьи", "Смешанное", "Другое"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Популярное в категории");
            builder.setItems(items, (dialog, item13) -> {
                Intent intent;
                switch (item13) {
                    case 0:
                        intent = new Intent(this, ActivityFanficList.class);
                        intent.putExtra("url", "https://ficbook.net/popular/gen");
                        intent.putExtra("title", "Популярное");
                        this.startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(this, ActivityFanficList.class);
                        intent.putExtra("url", "https://ficbook.net/popular/het");
                        intent.putExtra("title", "Популярное");
                        this.startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(this, ActivityFanficList.class);
                        intent.putExtra("url", "https://ficbook.net/popular/article");
                        intent.putExtra("title", "Популярное");
                        this.startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(this, ActivityFanficList.class);
                        intent.putExtra("url", "https://ficbook.net/popular/mixed");
                        intent.putExtra("title", "Популярное");
                        this.startActivity(intent);
                        break;
                    case 4:
                        intent = new Intent(this, ActivityFanficList.class);
                        intent.putExtra("url", "https://ficbook.net/popular/other");
                        intent.putExtra("title", "Популярное");
                        this.startActivity(intent);
                        break;
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if ((id == R.id.nav_messaging) || (id == DR_ID_MESSAGING)) {
            Intent intent = new Intent(this, ActivityMessagingList.class);
            this.startActivity(intent);
        }

        if ((id == R.id.nav_follows) || (id == DR_ID_FOLLOWS)) {
            Intent intent = new Intent(this, ActivityFollows.class);
            this.startActivity(intent);
        }
    }

    public void FetchProfile() {
        ExecutorService executors = Executors.newFixedThreadPool(1);
        Runnable runnable = () -> {
            try {
                String bodyx = "";
                boolean profile_block = false;

                if (!Application.isInternetAvailable()) {
                    return;
                }
                try {
                    Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net")).execute();
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            bodyx = body.string();
                            response.close();
                        }
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

                try {
                    if (Application.getIs503()) {
                        Application.C_cfclearance = "";
                        runOnUiThread(this::ShowValidateBrowserDialog);
                    }
                    if (Application.getNeedCaptcha()) {
                        ShowCaptchaDialog();
                    } else {
                        Document doc = Jsoup.parse(bodyx);
                        if (!bodyx.isEmpty()) {
                            Elements vid = doc.select("div.profile-holder");

                            if (vid.size() > 0) {
                                profile_block = true;
                                Application.setUser_name(vid.select("span.text").text().trim());
                                Application.user_avatar = vid.select("img").attr("src");
                                if (vid.select("div.profile-area").size() > 0) {
                                    String user_id = Objects.requireNonNull(vid.select("div.profile-area").select("ul").first()).select("li").get(1).select("a").attr("href").replace("/authors/", "");
                                    SharedPreferences.Editor ed = Application.sPref.edit();
                                    ed.putString("user_name", Application.getUser_name());
                                    ed.putString("user_avatar", Application.user_avatar);
                                    ed.putString("user_id", user_id);
                                    ed.apply();
                                    Application.user_id = user_id;
                                    Log.w("PROFILE", user_id);
                                } else {
                                    Log.w("PROFILE", "no info");
                                }
                            }
                            Elements lg = doc.select("div.social-auth-login");
                            if (lg.select("a").size() > 1) {
                                String code = lg.select("a").get(1).attr("href");
                                Log.d("VK token", code);
                                //Application.setVKAuth(code);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Application.firePopup();

                Handler uiThread = new Handler(Looper.getMainLooper());
                boolean finalProfile_block = profile_block;
                uiThread.post(() -> ParseLogin(finalProfile_block));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        executors.submit(runnable);
    }

    void bio() {
        if (!Application.sPref.getBoolean("fingerprint", false)) {
            return;
        }

        try {
            if (BiometricManager.from(this).canAuthenticate(BIOMETRIC_WEAK | DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
                Log.w("BIO", "Not supported");
                return;
            }

            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Log.e("BIO", "ERROR " + errString);
                    finish();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Log.i("BIO", "OK");
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Log.e("BIO", "FAILED");
                    finish();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                    .setTitle("Разблокировка приложения")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CopyCache(int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                int permissions_code = 42;
                String[] permissions = {WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions, permissions_code);
            }
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), Application.CHANNEL_FETCH_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Выгрузка кэша")
                .setContentText("Обработка...")
                .setAutoCancel(true);
        new Thread(
                () -> {
                    final int FETCH_NOTIFICATION_ID = 2526;

                    try {
                        builder.setChannelId(Application.CHANNEL_FETCH_ID);
                        builder.setSound(null);
                        builder.setOnlyAlertOnce(true);
                        Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());

                        builder.setProgress(1, 1, true);
                        builder.setChannelId(Application.CHANNEL_UPDATE_ID);
                        builder.setSound(null);
                        builder.setOnlyAlertOnce(true);

                        Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String fn;
                    String id;
                    File[] files = getContext().getFilesDir().listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().contains("_.fb2.zip")) {
                                id = file.getName().replace("_.fb2.zip", "");
                                Fanfic f = Fanfic.getById(id);
                                if (f != null) {
                                    fn = f.title + ".fb2.zip";
                                    Log.i("Export", id + " - " + fn);

                                    String path = "/Books/";
                                    switch (mode) {
                                        case 0:
                                            path = "/Books/";
                                            break;
                                        case 1:
                                            path = "/Books/Авторы/" + f.authors;
                                            break;
                                        case 2:
                                            path = "/Books/Фэндомы/" + f.fandom;
                                            break;
                                        case 3:
                                            path = "/Books/Направленности/" + f.direction;
                                            break;
                                    }

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        ContentResolver cr = getContext().getContentResolver();

                                        ContentValues cv = new ContentValues();
                                        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, fn);
                                        cv.put(MediaStore.MediaColumns.MIME_TYPE, "application/fb2+zip");
                                        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + path);

                                        Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);

                                        if (uri != null) {
                                            try {
                                                InputStream inputStream = new FileInputStream(new File(Application.getContext().getFilesDir(), file.getName()));
                                                OutputStream outputStream = cr.openOutputStream(uri, "wt");
                                                Application.copyFile(inputStream, outputStream);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.w("Export", "failed");
                                        }
                                    } else {
                                        try {
                                            InputStream inputStream = new FileInputStream(new File(Application.getContext().getFilesDir(), file.getName()));
                                            OutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + path), fn));
                                            Application.copyFile(inputStream, outputStream);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    try {
                        builder.setContentTitle("Выгрузка завершена");
                        builder.setChannelId(Application.CHANNEL_FETCH_ID);
                        builder.setSound(null);
                        builder.setOnlyAlertOnce(true);
                        builder.setProgress(0, 0, false);
                        builder.setContentText("Файлы книг в Загрузки/Books");
                        Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        ).start();
    }

}
