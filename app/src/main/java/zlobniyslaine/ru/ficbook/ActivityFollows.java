package zlobniyslaine.ru.ficbook;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import zlobniyslaine.ru.ficbook.adapters.AdapterFollows;


@SuppressWarnings("WeakerAccess")
public class ActivityFollows extends AppCompatActivity {

    private Boolean Loading = false;
    private Boolean refresh = true;
    private Boolean FirstRun = true;
    private Integer StartPosition = 0;
    private Integer MaxPosition = 100000;

    private ArrayList<HashMap<String, Object>> FollowsList = new ArrayList<>();
    RecyclerView rv1;
    ProgressBar pb1;
    SwipeRefreshLayout swipeContainer;

    private String UrlTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_follows);

        rv1 = findViewById(R.id.rv1);
        pb1 = findViewById(R.id.pb1);
        swipeContainer = findViewById(R.id.swipeContainer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UrlTemplate = "https://ficbook.net/home/fallows?p=@";

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(intent.getStringExtra("title"));
        }

        FollowsList = new ArrayList<>();

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(this);

        rv1.setLayoutManager(mLayoutManager);

        AdapterFollows rv_adapter = new AdapterFollows(this, FollowsList);
        rv1.setAdapter(rv_adapter);

        rv1.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {

            @Override
            public void onLoadMore() {

                if ((!Loading) && ((totalItemCount > 3) || (totalItemCount == 0)) && (StartPosition < MaxPosition)) {
                    if (!FirstRun) {
                        Log.i("LOADMORE", "items " + visibleItemCount + " on page " + StartPosition);
                        runFetcher();
                    }
                }
            }
        });

        swipeContainer.setOnRefreshListener(() -> {
            StartPosition = 0;
            refresh = true;
            runFetcher();
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    @Override
    public void onResume() {
        super.onResume();
        runFetcher();
    }

    private void runFetcher() {
        FirstRun = false;
        if (StartPosition == 0) {
            swipeContainer.setRefreshing(true);
            StartPosition = 1;
            FollowsList.clear();
            Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
            refresh = true;
        }
        if (StartPosition <= MaxPosition) {

            if (StartPosition > -1) {
                String u = UrlTemplate.replace("@", StartPosition.toString());
                if (!Loading) {
                    if (!refresh) {
                        pb1.setVisibility(View.VISIBLE);
                        refresh = false;
                    }
                }
                new fetcher_changes(this).execute(u);
            }
        } else {
            Log.i("List", "END");
        }
    }

    static class fetcher_changes extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityFollows> activityReference;

        fetcher_changes(ActivityFollows context) {
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
                }
                response.close();
                activityReference.get().MaxPosition = 1;
                if (doc.hasClass("pagination")) {
                    activityReference.get().MaxPosition = Integer.parseInt(doc.select("ul.pagination").select("li.text").select("b").get(1).text());
                }
                Log.i("MAX", activityReference.get().MaxPosition + "");
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
                if (activityReference.get() != null) {
                    activityReference.get().ParseAuthorsList(doc);
                    activityReference.get().swipeContainer.setRefreshing(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseAuthorsList(Document doc) {
        Elements vid = doc.select("div.data-table").select("div.row");
        HashMap<String, Object> map;

        for (Element v : vid) {
            if (v.toString().contains("readfic")) {
                try {
//                    Application.logLargeString(v.html());
                    map = new HashMap<>();
                    String title = v.select("a").get(0).text();
                    map.put("title", title);
                    String count = v.select("div:eq(0)").text().replace(title, "").trim();
                    map.put("count", count);
                    map.put("href", v.select("a").get(0).attr("href"));
                    String[] ur = v.select("a").get(0).attr("href").replace("/readfic/", "").split("/");
                    map.put("fic_id", ur[0]);
                    Log.i("P", ur[1] + "!");
                    String[] mur = ur[1].split("\\?");
                    map.put("part_id", mur[0]);
                    Log.i("P", mur[0] + "!");
                    map.put("date", v.select("span").get(0).text());
                    FollowsList.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();

        StartPosition++;
        Loading = false;
        pb1.setVisibility(View.GONE);
        refresh = false;
    }
}
