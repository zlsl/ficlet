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
import zlobniyslaine.ru.ficbook.adapters.AdapterChanges;


@SuppressWarnings("WeakerAccess")
public class ActivityChanges extends AppCompatActivity {

    private Boolean Loading = false;
    private Boolean refresh = true;
    private Boolean FirstRun = true;
    private Integer StartPosition = 0;
    private Integer MaxPosition = 100000;

    private ArrayList<HashMap<String, Object>> ChangesList = new ArrayList<>();

    RecyclerView rv1;
    ProgressBar pb1;
    SwipeRefreshLayout swipeContainer;

    private String UrlTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_changes);

        rv1 = findViewById(R.id.rv1);
        pb1 = findViewById(R.id.pb1);
        swipeContainer = findViewById(R.id.swipeContainer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UrlTemplate = intent.getStringExtra("url");
        if (UrlTemplate == null) {
            UrlTemplate = "";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(intent.getStringExtra("title"));
        }

        ChangesList = new ArrayList<>();

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(this);

        rv1.setLayoutManager(mLayoutManager);

        AdapterChanges rv_adapter = new AdapterChanges(this, ChangesList);
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
            ChangesList.clear();
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
        private final WeakReference<ActivityChanges> activityReference;

        fetcher_changes(ActivityChanges context) {
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
                activityReference.get().MaxPosition = Integer.parseInt(doc.select("ul.pagination").select("li.text").select("b").get(1).text());
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
                    if (doc != null) {
                        activityReference.get().ParseAuthorsList(doc);
                    }
                    activityReference.get().swipeContainer.setRefreshing(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseAuthorsList(Document doc) {
        if (doc != null) {
            Elements vid = doc.select("div.table-responsive>table.table>tbody>tr");
            HashMap<String, Object> map;

            try {
                for (Element v : vid) {
                    if (!v.toString().contains("\"200\"")) {
                        map = new HashMap<>();
                        map.put("author_id", v.select("td>a").get(0).attr("href").replace("/authors/", ""));
                        map.put("author_name", v.select("td>a").get(0).text());
                        map.put("date", v.select("td>span").get(0).text());
                        map.put("changes_url", "https://ficbook.net" + v.select("td>a").get(1).attr("href"));
                        map.put("changes_part", v.select("td>a").get(1).text());

                        if (v.select("td>span").size() > 1) {
                            map.put("draft", "1");
                        } else {
                            map.put("draft", "0");
                        }
                        ChangesList.add(map);
                    }
                }
                Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StartPosition++;
        Loading = false;
        pb1.setVisibility(View.GONE);
        refresh = false;
    }
}
