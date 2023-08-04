package zlobniyslaine.ru.ficbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.pagers.EditorPagerAdapter;


public class ActivityPartEditor extends AppCompatActivity {

    private String UrlTemplate;
    private String insert_after = "";

    private Bundle bundle_info;
    private Bundle bundle_editor;
    private boolean saved = false;

    FloatingActionButton fab;
    ProgressBar pb1;
    ViewPager viewPager;
    TabLayout tabLayout;

    void savePart() {
        saved = true;
        if (!id.isEmpty()) {
            new save_part(this).execute();
        } else {
            new add_part(this).execute();
        }
    }

    private FragmentEditorInfo fragment_info;
    private FragmentEditorPart fragment_editor;
    private String fanfic_id, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_part_editor);

        fab = findViewById(R.id.fab);
        pb1 = findViewById(R.id.pb1);
        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);

        fab.setOnClickListener(v -> savePart());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Информация"));
        tabLayout.addTab(tabLayout.newTab().setText("Редактор"));

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

        fanfic_id = intent.getStringExtra("fanfic_id");
        if (intent.hasExtra("part_id")) {
            id = intent.getStringExtra("part_id");
            UrlTemplate = "https://ficbook.net/home/myfics/" + fanfic_id + "/parts/" + id;
        } else {
            UrlTemplate = "https://ficbook.net/home/myfics/" + fanfic_id + "/addpart";
            id = "";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bundle_info = new Bundle();
        bundle_editor = new Bundle();

        FragmentManager fm = getSupportFragmentManager();

        fragment_info = (FragmentEditorInfo) fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentEditorInfo.class.getName());
        fragment_editor = (FragmentEditorPart) fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentEditorPart.class.getName());

        new fetcher_main(this).execute();

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Внимание");
        alertDialog.setMessage("Редактор в разработке. Делайте копии частей перед работой!\nВозврат из редактора - свайп направо!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }


    static class save_part extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityPartEditor> activityReference;

        save_part(ActivityPartEditor context) {
            activityReference = new WeakReference<>(context);
        }

        private String data = "";

        @Override
        protected Void doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .build();

                client.followRedirects();
                RequestBody formBody;

                if (activityReference.get().fragment_info.getDraft()) {
                    formBody = new FormBody.Builder()
                            .add("fanfic_id", activityReference.get().fanfic_id)
                            .add("part_id", activityReference.get().id)
                            .add("title", activityReference.get().fragment_info.getTitle())
                            .add("content", activityReference.get().fragment_editor.getContent())
                            .add("comment_before", activityReference.get().fragment_info.getCommentsBefore())
                            .add("comment_after", activityReference.get().fragment_info.getCommentsAter())
                            .add("change_description", activityReference.get().fragment_info.getChangesComments())
                            .add("status", activityReference.get().fragment_info.getStatus())
                            .add("not_published", "1")
                            .add("auto_pub", "0")
                            .add("auto_pub_day", "31")
                            .add("auto_pub_month", "12")
                            .add("auto_pub_year", "2018")
                            .add("auto_pub_hour", "12")
                            .add("auto_pub_minute", "30")
                            .build();
                } else {
                    formBody = new FormBody.Builder()
                            .add("fanfic_id", activityReference.get().fanfic_id)
                            .add("part_id", activityReference.get().id)
                            .add("title", activityReference.get().fragment_info.getTitle())
                            .add("content", activityReference.get().fragment_editor.getContent())
                            .add("comment_before", activityReference.get().fragment_info.getCommentsBefore())
                            .add("comment_after", activityReference.get().fragment_info.getCommentsAter())
                            .add("change_description", activityReference.get().fragment_info.getChangesComments())
                            .add("status", activityReference.get().fragment_info.getStatus())
                            .add("auto_pub", "0")
                            .add("auto_pub_day", "31")
                            .add("auto_pub_month", "12")
                            .add("auto_pub_year", "2018")
                            .add("auto_pub_hour", "12")
                            .add("auto_pub_minute", "30")
                            .build();
                }


                Request request = new Request.Builder()
                        .url("https://ficbook.net/home/fanfics/partauthoredit_save")
                        .header("User-Agent", Application.USER_AGENT2)
                        .addHeader("Cookie", Application.getCookies())
                        .addHeader("origin", "https://ficbook.net")
                        .addHeader("x-requested-with", "XMLHttpRequest")
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    data = body.string();
                }
                response.close();
                Log.i("Save", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (data.contains("result\":true")) {
                    Toast.makeText(activityReference.get(), "Глава сохранена", Toast.LENGTH_LONG).show();
                } else {
                    data = data.replace("{\"result\":false,\"error\":{", "").replace("}}", "").replace("\",\"", "\"\n\"");
                    Toast.makeText(activityReference.get(), "Ошибка сохранений главы\n" + YO.unescape(data), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class add_part extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityPartEditor> activityReference;

        add_part(ActivityPartEditor context) {
            activityReference = new WeakReference<>(context);
        }

        private String data = "";

        @Override
        protected Void doInBackground(String... params) {

            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .build();

                client.followRedirects();
                /*
                fanfic_id:5603402
title:test
content:111
comment_direction:0
comment:
insert_after:7
status:1
not_published:1
auto_pub:0
auto_pub_day:18
auto_pub_month:1
auto_pub_year:2018
auto_pub_hour:11
auto_pub_minute:0
                 */

                FormBody.Builder bodyBuilder = new FormBody.Builder();

                bodyBuilder
                        .add("fanfic_id", activityReference.get().fanfic_id)
                        .add("title", activityReference.get().fragment_info.getTitle())
                        .add("content", activityReference.get().fragment_editor.getContent())
                        .add("comment_before", activityReference.get().fragment_info.getCommentsBefore())
                        .add("comment_after", activityReference.get().fragment_info.getCommentsAter())
                        .add("status", "1")
                        .add("not_published", "1") // fragment_info.getDraft()
                        .add("auto_pub", "0")
                        .add("auto_pub_day", "31")
                        .add("auto_pub_month", "12")
                        .add("auto_pub_year", "2018")
                        .add("auto_pub_hour", "12")
                        .add("auto_pub_minute", "30");

                if (!activityReference.get().insert_after.isEmpty()) {
                    bodyBuilder.add("insert_after", activityReference.get().insert_after);
                }

                RequestBody formBody = bodyBuilder.build();

                Request request = new Request.Builder()
                        .url("https://ficbook.net/home/fanfics/partadd_save")
                        .header("User-Agent", Application.USER_AGENT2)
                        .addHeader("Cookie", Application.getCookies())
                        .addHeader("origin", "https://ficbook.net")
                        .addHeader("x-requested-with", "XMLHttpRequest")
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    data = body.string();
                }
                response.close();
                Log.i("Save", data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (data.contains("result\":true")) {
                    Toast.makeText(activityReference.get(), "Глава добавлена", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activityReference.get(), "Ошибка добавления главы", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityPartEditor> activityReference;

        fetcher_main(ActivityPartEditor context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            try {
                Response response = Application.httpclient.newCall(Application.getRequestBuilder(activityReference.get().UrlTemplate)).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    doc = Jsoup.parse(body.string());
//                    Application.logLargeString(doc.toString());
                }
                response.close();
            } catch (SocketTimeoutException e) {
                Application.displayPopup("Сервер не отвечает");
            } catch (UnknownHostException e) {
                Application.displayPopup("Проблемы с соединением");
            } catch (IOException e) {
                Application.displayPopup("Ошибка загрузки");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                activityReference.get().ParsePart(doc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ParsePart(Document doc) {
        try {
            if (!id.isEmpty()) {
                try {
                    bundle_info.putString("fanfic_id", doc.getElementById("savePartForm").select("a").attr("href").replace("/home/myfics/", ""));
                    bundle_info.putString("fanfic_title", doc.getElementById("savePartForm").select("div.form-text").select("a").text());
                    bundle_info.putString("part_title", doc.getElementById("titleInput").attr("value"));
                    bundle_info.putString("comments_before", doc.getElementById("commentBeforeInput").text());
                    bundle_info.putString("comments_after", doc.getElementById("commentAfterInput").text());
                    bundle_info.putBoolean("draft", doc.getElementById("not_published_chb").attr("value").equals("1"));
                    Log.i("SSSS", doc.select("select[id=statusInput] option[selected]").val() + "!");
                    bundle_info.putInt("status", Integer.parseInt(doc.select("select[id=statusInput] option[selected]").val()));
                    String content = doc.select("combined-editor").attr("initial-value");
                    fragment_editor.setContent(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else { // new part
                bundle_info.putString("fanfic_id", fanfic_id);
                bundle_info.putString("fanfic_title", "");
                bundle_info.putString("part_title", "Новая часть");
                bundle_info.putString("comments_before", "");
                bundle_info.putString("comments_after", "");
                bundle_info.putBoolean("draft", true);
                fragment_editor.setContent("");
                if (doc.getElementById("insertAfterInput") != null) {
                    insert_after = doc.getElementById("insertAfterInput").select("option[selected]").val();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fragment_info.setArguments(bundle_info);
        fragment_editor.setArguments(bundle_editor);

        List<Fragment> fragments = new Vector<>();
        fragments.add(fragment_info);
        fragments.add(fragment_editor);

        viewPager.setAdapter(new EditorPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        pb1.setVisibility(View.GONE);
    }

    private void animateFab(int position) {
        switch (position) {
            case 0:
                fab.show();
                tabLayout.setVisibility(View.VISIBLE);
                break;
            case 1:
                fab.hide();
                tabLayout.setVisibility(View.GONE);
                break;
            default:
                fab.hide();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            if (saved) {
                super.onBackPressed();
            } else {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                savePart();
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                finish();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Сохранить изменения?").setPositiveButton("Да", dialogClickListener)
                        .setNegativeButton("Нет", dialogClickListener).show();
            }
        }
    }

}
