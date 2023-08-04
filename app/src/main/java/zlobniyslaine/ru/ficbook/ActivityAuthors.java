package zlobniyslaine.ru.ficbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONObject;
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
import java.util.Objects;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterAuthors;
import zlobniyslaine.ru.ficbook.models.Authors;


public class ActivityAuthors extends AppCompatActivity {

    private ArrayList<HashMap<String, Object>> AuthorsList = new ArrayList<>();
    RecyclerView rv1;
    ProgressBar pb1;

    private String UrlTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_authors);
        rv1 = findViewById(R.id.rv1);
        pb1 = findViewById(R.id.pb1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UrlTemplate = intent.getStringExtra("url");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(intent.getStringExtra("title"));
        }

        AuthorsList = new ArrayList<>();

        rv1.setHasFixedSize(true);

        rv1.setLayoutManager(new WrapContentLinearLayoutManager(this));

        AdapterAuthors rv_adapter = new AdapterAuthors(this, AuthorsList);
        rv1.setAdapter(rv_adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAuthors();
    }

    public void fetchAuthors() {
        new Thread(
                () -> {
                    Document doc = null;
                    try {
                        Response response = Application.httpclient.newCall(Application.getRequestBuilder(UrlTemplate)).execute();
                        ResponseBody body = response.body();
                        if (body != null) {
                            doc = Jsoup.parse(body.string());
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

                    try {
                        if (doc != null) {
                            try {
                                Elements vid;
                                HashMap<String, Object> map;
                                if (UrlTemplate.contains("ficbook.net/authors")) {
                                    vid = doc.select("table.top-authors").select("tr");
                                    for (Element v : vid) {
                                        map = new HashMap<>();
                                        map.put("name", v.select("a").get(2).text());
                                        map.put("author_id", v.select("a").attr("href").replace("/authors/", ""));
                                        map.put("avatar_url", v.select("img").attr("src"));

                                        if (!AuthorsList.contains(map)) {
                                            AuthorsList.add(map);
                                        }

                                        Authors a = new Select()
                                                .from(Authors.class)
                                                .where("nid = ?", Objects.requireNonNull(map.get("author_id")).toString())
                                                .executeSingle();

                                        if (a != null) {
                                            a.avatar_url = Objects.requireNonNull(map.get("avatar_url")).toString();
                                            a.save();
                                        } else {
                                            a = new Authors();
                                            a.nid = Objects.requireNonNull(map.get("author_id")).toString();
                                            a.name = Objects.requireNonNull(map.get("name")).toString();
                                            a.avatar_url = Objects.requireNonNull(map.get("avatar_url")).toString();
                                        }
                                    }
                                } else {
                                    String c = doc.select("favourite-authors-list").attr(":init-authors-list");
                                    JSONArray jauthors = new JSONArray(c);
                                    for (int i = 0; i < jauthors.length(); i++) {
                                        JSONObject a = jauthors.getJSONObject(i);
                                        map = new HashMap<>();
                                        map.put("name", a.getString("nickname"));
                                        map.put("author_id", a.getString("user_id"));

                                        if (!AuthorsList.contains(map)) {
                                            AuthorsList.add(map);
                                        }
                                    }
                                }
                                new Handler(this.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        rv1.getAdapter().notifyDataSetChanged();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            new Handler(this.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    pb1.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Application.firePopup();
                }
        ).start();
    }
}
