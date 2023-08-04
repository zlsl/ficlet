package zlobniyslaine.ru.ficbook;

import static zlobniyslaine.ru.ficbook.Application.FICLET_HOST;
import static zlobniyslaine.ru.ficbook.Application.SIG;
import static zlobniyslaine.ru.ficbook.Application.download_token;
import static zlobniyslaine.ru.ficbook.Application.genToken;
import static zlobniyslaine.ru.ficbook.Application.getFicletRequest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionContinueVotes;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionRead;
import zlobniyslaine.ru.ficbook.breader.bookParser.BaseBook;
import zlobniyslaine.ru.ficbook.breader.bookParser.Fb2Book;
import zlobniyslaine.ru.ficbook.breader.bookParser.FontData;
import zlobniyslaine.ru.ficbook.breader.common.Pair;
import zlobniyslaine.ru.ficbook.breader.textReader.ReaderView;
import zlobniyslaine.ru.ficbook.controls.FloatSeekBar;
import zlobniyslaine.ru.ficbook.controls.LocalWebView;
import zlobniyslaine.ru.ficbook.models.FanficPage;
import zlobniyslaine.ru.ficbook.models.Parts;

@SuppressLint("ObsoleteSdkInt")
public class ActivityReader extends AppCompatActivity implements View.OnTouchListener {

    private static final int HEADER_DEFAULT = 48;

    private ReaderView m_readerView;
    private BaseBook m_book;
    private androidx.appcompat.app.AlertDialog captchaDialog;

    private float m_fontSize = 22;
    private String m_fontFace = "#sansserif";
    private final int m_rotation = ReaderView.ORIENTATION_NORMAL;
    private boolean m_inverse = false;
    private float m_extraStroke = 0;
    private int m_lineSpace = 50;
    private final int m_footer = ReaderView.FOOTER_TICKS;
    private int m_header = HEADER_DEFAULT;
    private int m_textColor = 0xFF000000;
    private int m_backColor = 0xFFFFFFFF;
    private final int m_refreshMode = 0;

    private final ActivityReader activity = this;
    private String filePath = "";
    private String id = "";
    private String title;
    private String part_id = "";
    private String url;
    private String data = "";
    private Integer last_page = 0;
    private Integer last_position = 0;
    private Integer typefaceId;
    private Boolean fast_menu = false;
    private Boolean pages_menu = false;
    private Boolean set_brightnesss = true;
    private Boolean night_mode = false;
    private Boolean hide_header = false;
    private String nextPart = "";
    private Boolean no_parts = false;

    private float x1;
    static final int MIN_DISTANCE = 150;

    private float brightness = 0.8f;
    private float distanceTotal = 0;

    private FrameLayout eink;
    private FrameLayout select;
    private TextView tv_pages;
    private TextView tv_clock;
    private TextView tv_brightness;
    private TextView tv_battery;
    private TextView tv_chapter;
    private ProgressBar pb1;
    private RelativeLayout l_main;
    private LinearLayout reader_bar;
    private FloatingActionButton fab;
    private NumberPicker np_font_size1;
    private NumberPicker np_font_size2;

    private CardView cv_menu;
    private CardView cv_pagesmenu;
    private TextView tv_topage;
    private TextView tv_select;
    private SeekBar sb_pageposition;
    private FloatSeekBar sb_brightness;
    private FloatSeekBar sb_contrast;
    private ImageView btn_settings;
    private View color_fg;
    private View color_bg;

    private List<Pair<String, Float>> m_chapters;

    final Handler lp_handler = new Handler();
    final Runnable mLongPressed = new Runnable() {
        public void run() {
            select.setVisibility(View.VISIBLE);
            select.setBackgroundColor(m_backColor);
            tv_select.setTextColor(m_textColor);
            reader_bar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            tv_select.setText(m_readerView.getPageText());
        }
    };

    private void updateFontSpinner() {
        m_fontSize = np_font_size1.getValue() + (float) np_font_size2.getValue() / 10;
        try {
            updateFonts();
            m_readerView.doInvalidate();
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.putFloat("new_font_size", m_fontSize);
            ed.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPagesMenu() {
        pages_menu = !pages_menu;

        if (pages_menu) {
            try {
                int pn = m_readerView.getPageNumber();
                sb_pageposition.setMax(m_readerView.getTotalPages());
                sb_pageposition.setProgress(pn);

                cv_pagesmenu.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            cv_pagesmenu.setVisibility(View.GONE);
        }
    }

    private void showFastMenu() {
        UpdateBar();
        fast_menu = !fast_menu;
        if (fast_menu) {
            cv_menu.setVisibility(View.VISIBLE);
        } else {
            cv_menu.setVisibility(View.GONE);
        }
    }

    private void closeFastMenu() {
        cv_menu.setVisibility(View.GONE);
        fast_menu = false;
    }

    private void closePagesMenu() {
        cv_pagesmenu.setVisibility(View.GONE);
        pages_menu = false;
    }

    private void nightModeSwitch(boolean checked) {
        night_mode = checked;
        m_inverse = night_mode;
        prepareNightMode();
    }

    private void hideHeaderSwitch(boolean checked) {
        hide_header = checked;
        if (checked) {
            m_header = 0;
        } else {
            m_header = HEADER_DEFAULT;
        }

        prepareNightMode();
    }

    private void brightnessControlSwitch(boolean checked) {
        set_brightnesss = checked;
        prepareBrightness();
        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putBoolean("brightness", checked);
        ed.apply();
        set_brightnesss = checked;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        set_brightnesss = Application.sPref.getBoolean("brightness", true);
        night_mode = Application.sPref.getBoolean("night_mode", false);
        hide_header = Application.sPref.getBoolean("hide_header", false);
        typefaceId = Application.sPref.getInt("typeface_id", 0);
        m_lineSpace = Application.sPref.getInt("line_space", 50);

        m_textColor = Application.sPref.getInt("color_fg", 0xFF000000);
        m_backColor = Application.sPref.getInt("color_bg", 0xFFFFFFFF);

        try {
            m_fontSize = Application.sPref.getFloat("new_font_size", 22);
            Log.i("FONTSIZE", "1");
        } catch (Exception e) {
            m_fontSize = Application.sPref.getInt("new_font_size", 22);
            Log.e("FONTSIZE", "2");
        }

        m_extraStroke = Application.sPref.getFloat("contrast", 0);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().getDecorView().setSystemUiVisibility(flags);

            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            });
        } else {
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_reader_new);

        l_main = findViewById(R.id.l_main);
        tv_pages = findViewById(R.id.tv_pages);
        tv_clock = findViewById(R.id.tv_clock);
        tv_brightness = findViewById(R.id.tv_brightness);
        tv_battery = findViewById(R.id.tv_battery);
        tv_chapter = findViewById(R.id.tv_chapter);
        pb1 = findViewById(R.id.pb1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        reader_bar = findViewById(R.id.reader_bar);
        fab = findViewById(R.id.fab);
        cv_menu = findViewById(R.id.cv_menu);
        cv_pagesmenu = findViewById(R.id.cv_pagesmenu);
        tv_topage = findViewById(R.id.tv_topage);
        sb_pageposition = findViewById(R.id.sb_pageposition);
        SeekBar sb_line_interval = findViewById(R.id.sb_line_interval);
        SwitchCompat cb_brightness = findViewById(R.id.cb_brightness);
        SwitchCompat cb_night_mode = findViewById(R.id.cb_night_mode);
        SwitchCompat cb_hide_header = findViewById(R.id.cb_hide_header);
        sb_brightness = findViewById(R.id.sb_brightness);
        sb_contrast = findViewById(R.id.sb_contrast);
        btn_settings = findViewById(R.id.btn_settings);
        Button btn_closemenu = findViewById(R.id.btn_closemenu);
        Button btn_closemenupages = findViewById(R.id.btn_closemenupages);
        eink = findViewById(R.id.eink);
        select = findViewById(R.id.select);
        np_font_size1 = findViewById(R.id.np_font_size1);
        np_font_size2 = findViewById(R.id.np_font_size2);
        Spinner spinner_typeface = findViewById(R.id.spinner_typeface);
        ImageView iv_reload = findViewById(R.id.iv_reload);
        color_bg = findViewById(R.id.color_bg);
        color_fg = findViewById(R.id.color_fg);
        tv_select = findViewById(R.id.tv_select);

        tv_select.setMovementMethod(LinkMovementMethod.getInstance());

        cv_menu.setVisibility(View.GONE);

        color_bg.setBackgroundColor(m_backColor);
        color_fg.setBackgroundColor(m_textColor);

        color_bg.setOnClickListener((v) -> new ColorPickerDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Цвет фона")
                .setPreferenceName("color_bg")
                .attachAlphaSlideBar(false)
                .setPositiveButton("OK",
                        (ColorEnvelopeListener) (envelope, fromUser) -> {
                            m_backColor = envelope.getColor();
                            updateFonts();
                            m_readerView.invalidate();
                            color_bg.setBackgroundColor(m_backColor);
                        })

                .attachAlphaSlideBar(true) // default is true. If false, do not show the AlphaSlideBar.
                .attachBrightnessSlideBar(true)  // default is true. If false, do not show the BrightnessSlideBar.
                .show());
        color_fg.setOnClickListener((v) -> new ColorPickerDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("Цвет текста")
                .setPreferenceName("color_fg")
                .attachAlphaSlideBar(false)
                .setPositiveButton("OK",
                        (ColorEnvelopeListener) (envelope, fromUser) -> {
                            m_textColor = envelope.getColor();
                            updateFonts();
                            m_readerView.invalidate();
                            color_fg.setBackgroundColor(m_textColor);
                        })
                .attachAlphaSlideBar(true) // default is true. If false, do not show the AlphaSlideBar.
                .attachBrightnessSlideBar(true)  // default is true. If false, do not show the BrightnessSlideBar.
                .show());

        findViewById(R.id.btn_select).setOnClickListener(view -> {
            if (select.getVisibility() == View.VISIBLE) {
                select.setVisibility(View.GONE);
                prepareNightMode();
            }
        });

        iv_reload.setOnClickListener(view -> {
            m_lineSpace = 50;
            try {
                updateFonts();
            } catch (Exception e) {
                e.printStackTrace();
            }
            m_readerView.doInvalidate();
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.putInt("line_space", m_lineSpace);
            ed.apply();
        });

        ArrayAdapter<CharSequence> ad_typeface = ArrayAdapter.createFromResource(this, R.array.array_typefaces, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_typeface.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_typeface.setAdapter(ad_typeface);
        spinner_typeface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        m_fontFace = "#sansserif";
                        break;
                    case 1:
                        m_fontFace = "#serif";
                        break;
                    default:
                        m_fontFace = "#internal";
                        break;
                }
                typefaceId = position;
                if ((m_readerView != null) && (m_book != null)) {
                    updateFonts();
                    m_readerView.doInvalidate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinner_typeface.setSelection(typefaceId);

        np_font_size1.setMinValue(5);
        np_font_size1.setMaxValue(40);
        np_font_size2.setMinValue(0);
        np_font_size2.setMaxValue(9);
        np_font_size1.setValue((int) m_fontSize);
        int v2 = (int) ((m_fontSize - (int) m_fontSize) * 10);
        np_font_size2.setValue(v2);

        np_font_size1.setOnValueChangedListener((numberPicker, i, i1) -> updateFontSpinner());
        np_font_size2.setOnValueChangedListener((numberPicker, i, i1) -> updateFontSpinner());

        tv_pages.setOnClickListener(v -> showPagesMenu());
        btn_settings.setOnClickListener(v -> showFastMenu());
        btn_closemenu.setOnClickListener(v -> closeFastMenu());
        btn_closemenupages.setOnClickListener(v -> closePagesMenu());

        cb_night_mode.setOnCheckedChangeListener((buttonView, isChecked) -> nightModeSwitch(isChecked));
        cb_brightness.setOnCheckedChangeListener((buttonView, isChecked) -> brightnessControlSwitch(isChecked));
        cb_hide_header.setOnCheckedChangeListener((buttonView, isChecked) -> hideHeaderSwitch(isChecked));

        last_page = 0;
        setSupportActionBar(toolbar);
        cb_brightness.setChecked(set_brightnesss);
        cb_night_mode.setChecked(night_mode);
        cb_hide_header.setChecked(hide_header);

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(mTimeChangedReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        tv_battery.setText(getBatteryLevel(this));

        Intent intent = getIntent();

        if (intent != null) {
            final android.net.Uri data = intent.getData();
            if (data != null) {
                filePath = data.getEncodedPath();
            }
        }

        try {
            if (intent != null) {
                if (intent.hasExtra("id")) {
                    id = intent.getStringExtra("id");
                    if (id != null) {
                        Log.i("ID", id);
                        if (id.contains("?")) {
                            String[] ids = id.split("\\?");
                            id = ids[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        title = "";
        try {
            if (intent != null) {
                if (intent.hasExtra("title")) {
                    title = intent.getStringExtra("title");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        if (filePath.isEmpty()) {
            if (intent != null) {
                if (!intent.hasExtra("part_id")) {
                    part_id = "";
                    try {
                        last_page = FanficPage.getLastPage(id).page_number;
                        last_position = FanficPage.getLastPage(id).scroll_position;
                    } catch (Exception e) {
                        last_page = 0;
                        last_position = 0;
                    }
                } else {
                    part_id = intent.getStringExtra("part_id");
                    url = "https://ficbook.net/readfic/" + id + "/" + part_id;
                    try {
                        last_page = Parts.getPart(id, part_id).page_number;
                        nextPart = Parts.getNextPart(id, part_id);
                    } catch (Exception e) {
                        last_page = 0;
                    }
                    Log.d("URL PART", url);
                }
            }
            if (intent.hasExtra("no_parts")) {
                url = "https://ficbook.net/readfic/" + id;
                Log.d("URL FULLTEXT", url);
                part_id = "";
                no_parts = true;
            }


        } else {
            Log.i("URL", "Local file: " + filePath);
        }

        if (last_page == null) {
            last_page = 0;
        }
        if (last_position == null) {
            last_position = 0;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fab.setOnClickListener(view -> {

            if (nextPart.isEmpty()) {
                final String[] items = {"Прочитано", "Жду продолжения"};
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                boolean[] ch_mod = new boolean[2];
                ch_mod[0] = true;

                builder.setTitle("Конец фанфика")
                        .setMultiChoiceItems(items, ch_mod, (dialog, item, isChecked) -> {
                            switch (item) {
                                case 0:
                                    MarkReaded(isChecked);
                                    break;
                                case 1:
                                    MarkContinueVote(isChecked);
                                    break;
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                            dialog.dismiss();
                            finish();
                        });

                builder.create().show();

                AjaxActionRead action = new AjaxActionRead();
                action.Do(id, true);
                CoordinatorLayout mRoot = findViewById(R.id.coordl);
                Snackbar snackbar = Snackbar.make(mRoot, "Фанфик отмечен как прочитанный", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                try {
                    part_id = nextPart;
                    url = "https://ficbook.net/readfic/" + id + "/" + part_id;
                    last_page = 0;
                    nextPart = Parts.getNextPart(id, part_id);
                    fab.hide();
                    if (Application.isInternetAvailable()) {
                        m_readerView.clear();
                        new fetcher_part(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        fab.hide();

        prepareNightMode();

        tv_pages.setText("Загрузка...");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
        tv_clock.setText(df.format(Calendar.getInstance().getTime()));

        sb_contrast.setValue(m_extraStroke);
        sb_contrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_extraStroke = sb_contrast.getValue();
                try {
                    updateFonts();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m_readerView.doInvalidate();
                SharedPreferences.Editor ed = Application.sPref.edit();
                ed.putFloat("contrast", m_extraStroke);
                ed.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_line_interval.setMax(200);
        sb_line_interval.setProgress(m_lineSpace);
        sb_line_interval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_lineSpace = progress;
                try {
                    updateFonts();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                m_readerView.doInvalidate();
                SharedPreferences.Editor ed = Application.sPref.edit();
                ed.putInt("line_space", m_lineSpace);
                ed.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        prepareBrightness();

        if (filePath.isEmpty()) {
            if (part_id.isEmpty() && !no_parts) {
                new fetcher_fb2(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new fetcher_part(activity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        sb_pageposition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                tv_topage.setText(getString(R.string.page_n_of, progress + 1, sb_pageposition.getMax() + 1));

                float percent = (float) progress / (float) sb_pageposition.getMax();

                if (m_chapters == null || m_chapters.size() == 0) {
                    tv_chapter.setVisibility(View.GONE);
                } else {
                    String chapterName = "";
                    for (int i = 0; i < m_chapters.size(); i++) {
                        if (percent * 100.0f < m_chapters.get(i).second) {
                            chapterName = m_chapters.get(i == 0 ? 0 : i - 1).first.trim();
                            break;
                        }
                    }
                    tv_chapter.setVisibility(View.VISIBLE);
                    tv_chapter.setText(chapterName);
                }
                m_readerView.gotoPage(progress, percent);
                UpdateBar();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Log.d("RV", "init");
        FrameLayout eink = findViewById(R.id.eink);
        m_readerView = new ReaderView(this, eink, 1.0f);
        m_readerView.setBackground(null);
        m_readerView.setOnTouchListener(this);
        m_readerView.setOnPageRenderedListener(() -> {
            Log.d("PAGE", "rendered");
            UpdateBar();
        });

        if (!filePath.isEmpty()) {
            id = filePath;
            try {
                last_page = FanficPage.getLastPage(id).page_number;
                last_position = FanficPage.getLastPage(id).scroll_position;
            } catch (Exception e) {
                last_page = 0;
                last_position = 0;
            }
            pb1.setVisibility(View.GONE);
            init();
        }
    }

    private void prepareBrightness() {
        if (set_brightnesss) {
            findViewById(R.id.l_brightness).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_brightness).setVisibility(View.VISIBLE);

            try {
                if (Application.sPref.contains("brightness_level")) {
                    brightness = Application.sPref.getFloat("brightness_level", 0.8f);
                    if (brightness > 1.0f) {
                        brightness = 0.8f;
                    }
                }

                setBrightness();
                sb_brightness.setValue(brightness);
                sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        brightness = sb_brightness.getValue();
                        setBrightness();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            tv_brightness.setText(getString(R.string.brightness_n, Math.round(brightness * 100)));
        } else {
            findViewById(R.id.l_brightness).setVisibility(View.GONE);
            findViewById(R.id.tv_brightness).setVisibility(View.GONE);
            try {
                Log.i("BR", "Default");
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
                getWindow().setAttributes(layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareNightMode() {
        if (m_readerView != null) {
            m_inverse = night_mode;
            m_readerView.changeSettings(m_extraStroke, m_rotation, m_header, m_footer, m_inverse, m_textColor, m_backColor, m_refreshMode, false);
            m_readerView.doInvalidate();
        }
        if (night_mode) {
            l_main.setBackgroundColor(getResources().getColor(R.color.reader_bg_night));
            select.setBackgroundColor(getResources().getColor(R.color.reader_bg_night));
            reader_bar.setBackgroundColor(getResources().getColor(R.color.rb_bg_night_color));

            btn_settings.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_settings_dark, null));

            tv_pages.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_library_books, 0, 0, 0);

            for (int count = 0; count < reader_bar.getChildCount(); count++) {
                View view = reader_bar.getChildAt(count);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.rb_fg_night_color));
                }
            }
        } else {
            l_main.setBackgroundColor(getResources().getColor(R.color.reader_bg_day));
            select.setBackgroundColor(getResources().getColor(R.color.reader_bg_day));
            reader_bar.setBackgroundColor(getResources().getColor(R.color.rb_bg_day_color));

            btn_settings.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_settings, null));
            tv_pages.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_library_books, 0, 0, 0);

            for (int count = 0; count < reader_bar.getChildCount(); count++) {
                View view = reader_bar.getChildAt(count);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.rb_fg_day_color));
                }
            }
        }

        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putBoolean("night_mode", night_mode);
        ed.putBoolean("hide_header", hide_header);
        ed.apply();
    }

    private float getDistance(float startX, float startY, MotionEvent ev) {
        float distanceSum = 0;
        final int historySize = ev.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            float hx = ev.getHistoricalX(0, h);
            float hy = ev.getHistoricalY(0, h);
            float dx = (hx - startX);
            float dy = (hy - startY);
            distanceSum += Math.sqrt(dx * dx + dy * dy);
            startX = hx;
            startY = hy;
        }
        float dx = (ev.getX(0) - startX);
        float dy = (ev.getY(0) - startY);
        distanceSum += Math.sqrt(dx * dx + dy * dy);
        return distanceSum;
    }

    static class fetcher_part extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityReader> activityReference;
        private Boolean changed = false;

        fetcher_part(ActivityReader context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (Application.isInternetAvailable()) {
                try {
                    Log.i("LOAD", activityReference.get().url);
                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(activityReference.get().url)).execute();

                    if (!response.isSuccessful()) {
                        Application.displayPopup("Сетевая ошибка " + response.code() + " " + response.message());
                    }

                    ResponseBody body = response.body();
                    if (body != null) {
                        activityReference.get().data = body.string();
                        body.close();
                        response.close();
                        changed = true;
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
            } else {
                changed = false;
            }

            if (!changed) {
                try {
                    File test = new File(Application.getContext().getFilesDir() + "/" + activityReference.get().id + "_" + activityReference.get().part_id + ".fb2.zip");
                    if (test.exists()) {
                        Log.d("LOAD", "fb2zip exists");
                        if (activityReference.get() != null) {
                            activityReference.get().init();
                        }
                        return null;
                    } else {
                        Log.d("LOAD", "fb2zip not exists");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if ((activityReference.get() == null)) {
                return null;
            }

            if ((activityReference.get().data == null)) {
                Log.e("LOAD", "Failed network/rawcache");
                return null;
            }

            Document doc = Jsoup.parse(activityReference.get().data);

            doc.outputSettings().prettyPrint(false);
            doc.select("script").remove();
            doc.select("div.in-text-ad-left").remove();

            if (!activityReference.get().part_id.isEmpty()) {
                activityReference.get().title = Parts.getPart(activityReference.get().id, activityReference.get().part_id).title;
                activityReference.get().data = "<h2>" + activityReference.get().title + "</h2>" + doc.select("#content").html();
            } else {
                activityReference.get().data = doc.select("#content").html();
            }
            if (Application.sPref.getBoolean("yo_fix", false)) {
                activityReference.get().runOnUiThread(() -> activityReference.get().tv_pages.setText("Ё-фикатор..."));
                activityReference.get().data = YO.fix(activityReference.get().data);
            }

            if (Application.sPref.getBoolean("typograf", false)) {
                activityReference.get().runOnUiThread(() -> activityReference.get().tv_pages.setText("Типограф..."));
            }
            activityReference.get().data = activityReference.get().data.replace("&nbsp;", " ");
            activityReference.get().data = YO.Typograf(activityReference.get().data);

            try {
                if (activityReference.get().data.length() < 40) {
                    Log.i("CONTENT", "Length: " + activityReference.get().data.length());
                    if (Application.isGuest()) {
                        Application.displayPopup("Весь текст фанфика недоступен. Читайте по главам или выполните вход.");
                    } else {
                        Application.displayPopup("Весь текст фанфика недоступен. Возможно проблемы на сервере или сбой авторизации.");
                    }
                    return null;
                }

                FileWriter out = new FileWriter(new File(Application.getContext().getFilesDir(), activityReference.get().id + "_" + activityReference.get().part_id + ".fb2"));
                out.write(YO.html2fb2(activityReference.get().data, activityReference.get().id));
                out.close();

                Application.zip(Application.getContext().getFilesDir() + "/" + activityReference.get().id + "_" + activityReference.get().part_id + ".fb2", Application.getContext().getFilesDir() + "/" + activityReference.get().id + "_" + activityReference.get().part_id + ".fb2.zip");
                if (!new File(Application.getContext().getFilesDir(), activityReference.get().id + "_" + activityReference.get().part_id + ".fb2").delete()) {
                    Log.w("TMP", "delete failed");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                activityReference.get().init();
                activityReference.get().tv_pages.setText("Готово");
                activityReference.get().UpdateBar();
                activityReference.get().pb1.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void LoadOld() {
        m_readerView.post(this::ShowCaptchaDialog);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void ShowCaptchaDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder captchaDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();

            View alertDialogView = inflater.inflate(R.layout.dialog_503, null);
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
                    try {
                        super.onPageFinished(wv_captcha, url);
                        Log.i("OPF", url); // _ym_uid=1520499540764508028; _ym_isad=2
                        String cookies = CookieManager.getInstance().getCookie(url);
                        Log.i("OPF", "All the cookies in a string:" + cookies);

                        if (!url.contains("_cf") || url.contains("login")) {
                            Log.i("503 CHECK", "success");
                            Application.C_cfduid = Application.getCookie("__cfduid");
                            Application.C_cfclearance = Application.getCookie("cf_clearance");
                            SharedPreferences.Editor ed = Application.sPref.edit();
                            ed.putString("cf_clearance", Application.C_cfclearance);
                            ed.apply();

                            captchaDialog.dismiss();
                            m_readerView.postDelayed(() -> new fetcher_fb2(ActivityReader.this).execute(), 2000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            captchaDialog = captchaDialogBuilder.create();
            captchaDialog.show();

            wv_captcha.loadUrl("https://ficbook.net/fanfic_download/fb2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class fetcher_fb2 extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityReader> activityReference;

        fetcher_fb2(ActivityReader context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.i("LOAD FB2", "start");
            String id = activityReference.get().id;
            String part_id = activityReference.get().part_id;
            String data = "";
            if (Application.isInternetAvailable()) {
                try {
                    Response response;
                    if (Application.isGuest()) {
                        Log.i("LOAD FB2", "rcache");
                    } else {
                        Log.i("LOAD FB2", "ficbook " + download_token);

                        RequestBody formBody = new FormBody.Builder()
                                .add("fanfic_id", id)
                                .add("tokenn", download_token)
                                .add(genToken(id).get(0), genToken(id).get(1))
                                .build();

                        String url = "https://ficbook.net/fanfic_download/".replace(SIG, ".") + id + "/fb2";
                        response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
//                        response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/fanfic_download/fb2", formBody)).execute();

                        if (!response.isSuccessful()) {
                            Log.e("RESP", "failed " + response.code());

                            if (response.code() == 404) {
                                Log.w("FANFIC", "404");
                            }
                        }
                    }

                    if (!response.isSuccessful() && !Application.isGuest()) {
                        Application.displayPopup("Сетевая ошибка " + response.code() + " " + response.message());
                        activityReference.get().LoadOld();
                        return null;
                    }

                    ResponseBody body = response.body();
                    if (body != null) {
                        data = body.string();
//                        Application.logLargeString(data);
                        body.close();
                        response.close();
                    }

                    File fb2zip = new File(Application.getContext().getFilesDir(), id + "_" + part_id + ".fb2.zip");
                    if (fb2zip.exists()) {
                        Log.i("FB2", "Exists");
                        if (fb2zip.length() == data.length()) {
                            Log.i("FB2", "Not changed");
                        } else {
                            Log.i("FB2", "Changed " + fb2zip.length() + " : " + data.length());
                        }
                    }
                } catch (SocketTimeoutException e) {
                    Application.displayPopup("Сервер не отвечает");
                    return null;
                } catch (UnknownHostException e) {
                    Application.displayPopup("Проблемы с соединением");
                    return null;
                } catch (IOException e) {
                    Application.displayPopup("Ошибка загрузки");
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return null;
            }

            if ((activityReference.get() == null)) {
                return null;
            }

            try {
                data = Application.cleanFB2(data);

                FileWriter out = new FileWriter(new File(Application.getContext().getFilesDir(), id + "_" + part_id + ".fb2"));
                out.write(data);
                out.close();
                if (activityReference.get() != null) {
                    activityReference.get().data = data;
                    Application.zip(Application.getContext().getFilesDir() + "/" + id + "_" + part_id + ".fb2", Application.getContext().getFilesDir() + "/" + id + "_" + part_id + ".fb2.zip");
                    if (!new File(Application.getContext().getFilesDir(), id + "_" + part_id + ".fb2").delete()) {
                        Log.w("TMP", "delete failed");
                    }

                    if (!Application.isGuest()) {
                        new Thread(() -> {
                            Application.syncCache();
                            Log.d("SYNC", "pack");
                        }).start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (activityReference.get() != null) {
                    activityReference.get().init();
                    activityReference.get().tv_pages.setText("Готово");
                    activityReference.get().UpdateBar();
                    activityReference.get().pb1.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void NextPage() {
        cv_menu.setVisibility(View.GONE);
        m_readerView.nextPage(true, true);
        UpdateBar();
    }

    private void PrevPage() {
        cv_menu.setVisibility(View.GONE);
        m_readerView.prevPage();
        UpdateBar();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    PrevPage();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    NextPage();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private void UpdateBar() {
        Log.d("mrv", m_readerView.getPageNumber() + ":" + m_readerView.getTotalPages() + ":" + m_readerView.getPosition() + ":" + m_readerView.getTotalLength());
        try {
            tv_pages.setText(getString(R.string.page_n_of, m_readerView.getRealPage(), m_readerView.getRealTotal()));
            if (m_readerView.isEndOfBook()) {
                if (nextPart.isEmpty()) {
                    fab.show();
                    fab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_action_tick));
                    Log.i("PART", "end of book");
                } else {
                    fab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_action_playback_next));
                    fab.show();
                    Log.i("PART", "next part");
                }
            } else {
                fab.hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_brightness.setText(getString(R.string.brightness_n, Math.round(brightness * 100)));
    }

    private void SavePagePosition() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        try {
            if (!part_id.equals("")) { // for fic part
                Parts p = Parts.getPart(id, part_id);
                if (p != null) {
                    p.file_size = data.length();
                    p.page_number = m_readerView.getPageNumber();
                    p.date_read = sdf.format(new Date());
                    p.save();
                }
            } else { // for fic full
                Log.i("SPage", "fic " + id);
                FanficPage fp = new FanficPage();
                fp.nid = id;
                if (data != null) {
                    fp.file_size = data.length();
                } else {
                    fp.file_size = 0;
                }
                fp.page_number = m_readerView.getPageNumber();
                fp.scroll_position = (int) m_readerView.getPosition();
                fp.page_count = m_readerView.getTotalPages();
                fp.date_read = sdf.format(new Date());
                fp.save();
                Log.i("SPage", "saved");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putBoolean("night_mode", night_mode);
        ed.apply();
    }

    @Override
    protected void onPause() {
        Application.stopSpeak();
        SavePagePosition();

        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putFloat("new_font_size", m_fontSize);
        ed.putInt("typeface_id", typefaceId);
        ed.putInt("line_space", m_lineSpace);
        ed.putInt("color_bg", m_backColor);
        ed.putInt("color_fg", m_textColor);
        ed.apply();

        super.onPause();
    }

    private void changeBrightness(float X, float Y, float x, float y, float distance) {
        if (x == X) {
            distance = distance / 500;
            if (y < Y) {
                commonBrightness(distance);
            } else {
                commonBrightness(-distance);
            }
        }
    }

    private void commonBrightness(float distance) {
        if (set_brightnesss) {
            if (getWindow().getAttributes().screenBrightness + distance <= 1f && getWindow().getAttributes().screenBrightness + distance >= -1f) {
                brightness = brightness + distance;
                setBrightness();
            }
        }
    }

    private void setBrightness() {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        if (brightness < -1) {
            brightness = -1;
        }
        if (brightness > 1) {
            brightness = 1;
        }

        if (brightness < 0) {
            layout.screenBrightness = 0;
        } else {
            layout.screenBrightness = brightness;
        }
        getWindow().setAttributes(layout);

        tv_brightness.setText(getString(R.string.brightness_n, Math.round(brightness * 100)));

        if (brightness < 0) {
            if (brightness > -0.5f) {
                l_main.setAlpha(brightness + 1f);
                eink.setAlpha(brightness + 1f);
                if (night_mode) {
                    reader_bar.setBackgroundColor(getResources().getColor(R.color.rb_bg_night_color));
                } else {
                    reader_bar.setBackgroundColor(getResources().getColor(R.color.rb_bg_day_color));
                }

            } else {
                l_main.setAlpha(brightness + 1f);
                eink.setAlpha(brightness + 1f);
                reader_bar.setBackgroundColor(getResources().getColor(R.color.black));

            }
        } else {
            if (night_mode) {
                reader_bar.setBackgroundColor(getResources().getColor(R.color.rb_bg_night_color));
            } else {
                reader_bar.setBackgroundColor(getResources().getColor(R.color.rb_bg_day_color));
            }
        }

        if (brightness < -0.2f) {
            reader_bar.setAlpha(brightness + 1f);
        }

        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putFloat("brightness_level", brightness);
        ed.apply();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle stateBundle) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            super.onSaveInstanceState(stateBundle);
        }
    }

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            int battery_level = i.getIntExtra("level", 0);
            tv_battery.setText(getString(R.string.brightness_n, battery_level));
        }
    };

    private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
            tv_clock.setText(df.format(Calendar.getInstance().getTime()));
        }
    };

    @Override
    protected void onDestroy() {
        if (mBatInfoReceiver != null) {
            unregisterReceiver(mBatInfoReceiver);
        }
        if (mTimeChangedReceiver != null) {
            unregisterReceiver(mTimeChangedReceiver);
        }
        super.onDestroy();
    }

    private String getBatteryLevel(Context context) {
        try {
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            if (batteryIntent != null) {
                int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                if (level > -1 && scale > 0) {
                    return getString(R.string.brightness_n, Math.round(((float) level / (float) scale) * 100.0f));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    private void init() {
        String m_filePath = "";
        if (filePath == null) {
            return;
        }

        if (id == null) {
            return;
        }

        if (part_id == null) {
            return;
        }

        if (filePath.isEmpty()) {
            m_filePath = Application.getContext().getFilesDir() + "/" + id + "_" + part_id + ".fb2.zip";
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Uri uri = getIntent().getData();
                    m_filePath = getPathFromUri(this, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        if (m_readerView == null) {
            return;
        }

        m_readerView.clear();
        if (m_filePath == null) {
            return;
        }
        File file = new File(m_filePath);

        if (!file.exists()) {
            Log.e("Reader", "Not exists " + m_filePath);
            return;
        }

        Log.i("Reader", "Loading file " + m_filePath);
        m_book = new Fb2Book(m_filePath);

        if (m_book.init(getCacheDir().getAbsolutePath())) {
            Log.e("init", "Error");
            return;
        }

        runOnUiThread(this::postInit);
    }

    private void postInit() {
        List<FontData> m_internalFonts = m_book.getFonts();

        switch (typefaceId) {
            case 0:
                m_fontFace = "#sansserif";
                break;
            case 1:
                m_fontFace = "#serif";
                break;
            default:
                m_fontFace = "#internal";
                break;
        }

        if (m_fontFace.equals("#internal") && (m_internalFonts == null || m_internalFonts.size() == 0)) {
            m_fontFace = "#sansserif";
        }

        updateFonts();

        List<Pair<String, Float>> chapterPositions = new ArrayList<>(m_book.getChapters().size());
        try {
            for (Pair<Long, String> chapter : m_book.getChapters()) {
                long cpos = chapter.first;
                float percent = m_book.getReader().getPercent(cpos);
                chapterPositions.add(new Pair<>(chapter.second, percent));
            }
        } catch (Exception ex) {
            Log.e("Reader", "Error parsing chapters " + ex);
        }
        m_readerView.init(m_book.getReader(), last_position, last_page, chapterPositions, m_book.getStyles(), m_book.getImages(), m_book.getNotes());
        m_chapters = m_readerView.getChapters();
    }

    private void updateFonts() {
        Typeface normal;
        Typeface bold;
        Typeface italic;
        Typeface boldItalic;

        try {
            Log.d("Reader", "Set font " + m_fontFace);
            normal = Typeface.create(m_fontFace.startsWith("#") ? m_fontFace.substring(1) : m_fontFace, Typeface.NORMAL);

            if (normal == null) {
                normal = Typeface.SERIF;
                m_fontFace = "#serif";
            }

            bold = Typeface.create(normal, Typeface.BOLD);
            if (bold == null) {
                bold = normal;
            }

            italic = Typeface.create(normal, Typeface.ITALIC);
            if (italic == null) {
                italic = normal;
            }

            boldItalic = Typeface.create(normal, Typeface.BOLD_ITALIC);
            if (boldItalic == null) {
                boldItalic = italic;
            }

            int m_paddingLeft = 10;
            int m_paddingTop = 5;
            int m_paddingBottom = 5;
            int m_paddingRight = 10;
            float lsp = (float) m_lineSpace / 100 + 0.5f;
            m_readerView.initFonts(m_fontSize, normal, bold, italic, boldItalic, m_rotation, m_extraStroke, m_inverse, m_textColor, m_backColor, lsp, m_book.getFirstLine() * m_fontSize / 30, m_header, m_footer, m_refreshMode, m_paddingLeft, m_paddingTop, m_paddingRight, m_paddingBottom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                lp_handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout() * 2L);
                x1 = event.getX();
                distanceTotal = 0;

                boolean isCenterX = event.getX() > view.getWidth() * 0.35 && event.getX() < view.getWidth() * 0.65;
                boolean isCenterY = event.getY() > view.getHeight() * 0.35 && event.getY() < view.getHeight() * 0.65;
                if (isCenterX && isCenterY) {
                    lp_handler.removeCallbacks(mLongPressed);
                    UpdateBar();
                    showFastMenu();
                    return false;
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX();
                final float y = event.getY();
                float distanceCovered = getDistance(x, y, event);
                distanceTotal = distanceTotal + distanceCovered;
                try {
                    if (event.getHistorySize() > 0) {
                        if (distanceCovered > 9) {
                            lp_handler.removeCallbacks(mLongPressed);
                            changeBrightness(event.getHistoricalX(0, 0), event.getHistoricalY(0, 0), x, y, distanceCovered);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case MotionEvent.ACTION_UP:
                lp_handler.removeCallbacks(mLongPressed);
                float x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (x2 < x1) {
                        NextPage();
                    } else {
                        PrevPage();
                    }
                }

                if (distanceTotal < 10) {
                    boolean forward;
                    forward = event.getX() > view.getWidth() / 2.0;

                    if (forward) {
                        NextPage();
                    } else {
                        PrevPage();
                    }
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (select.getVisibility() == View.VISIBLE) {
            select.setVisibility(View.GONE);
            prepareNightMode();
            return;
        }

        if (cv_menu.getVisibility() == View.VISIBLE) {
            cv_menu.setVisibility(View.GONE);
            return;
        }

        super.onBackPressed();
    }

    private void MarkReaded(Boolean mode) {
        AjaxActionRead action = new AjaxActionRead();
        action.Do(id, mode);
        CoordinatorLayout mRoot = findViewById(R.id.coordl);
        String txt;
        if (mode) {
            txt = "Фанфик отмечен как прочитанный";
        } else {
            txt = "Фанфик отмечен как непрочитанный";
        }
        Snackbar snackbar = Snackbar.make(mRoot, txt, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void MarkContinueVote(Boolean mode) {
        AjaxActionContinueVotes action = new AjaxActionContinueVotes();
        action.Do(part_id, mode);
        CoordinatorLayout mRoot = findViewById(R.id.coordl);
        String txt;
        if (mode) {
            txt = "Жду продолжения";
        } else {
            txt = "Не жду продолжения";
        }
        Snackbar snackbar = Snackbar.make(mRoot, txt, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        try {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri))
                    return uri.getLastPathSegment();

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


}
