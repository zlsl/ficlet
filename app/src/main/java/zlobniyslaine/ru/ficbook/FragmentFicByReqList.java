package zlobniyslaine.ru.ficbook;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
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
import zlobniyslaine.ru.ficbook.adapters.AdapterFicByReq;


public class FragmentFicByReqList extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private ArrayList<HashMap<String, Object>> Fics = new ArrayList<>();
    private AdapterFicByReq rv_adapter;

    RecyclerView rv1;
    ProgressBar pb1;
    SwipeRefreshLayout swipeContainer;

    private Boolean Loading = false;
    private Boolean refresh = true;
    private Integer StartPosition = 0;
    private Integer MaxPosition = 100000;
    private String UrlTemplate = "";
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fanfic_list, container, false);

        rv1 = rootView.findViewById(R.id.rv1);
        pb1 = rootView.findViewById(R.id.pb1);
        swipeContainer = rootView.findViewById(R.id.swipeContainer);

        context = this.getContext();

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url");
            if (UrlTemplate != null) {
                Log.d("url", UrlTemplate);
            }
        }

        Fics = new ArrayList<>();

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(context);

        rv1.setLayoutManager(mLayoutManager);

        rv_adapter = new AdapterFicByReq(context, Fics);
        rv1.setAdapter(rv_adapter);

        EndlessRecyclerViewScrollListener2 scrollListener = new EndlessRecyclerViewScrollListener2(mLayoutManager) {
            @Override
            public void onLoadMore() {
                runFetcher();
            }
        };
        rv1.addOnScrollListener(scrollListener);

        swipeContainer.setOnRefreshListener(() -> {
            Log.d("SWREFRESH", "S:" + Fics.size());
            StartPosition = 0;
            refresh = true;
            runFetcher();
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        runFetcher();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void runFetcher() {
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
                    new fetcher_fics(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    swipeContainer.setRefreshing(false);
                    pb1.setVisibility(View.GONE);
                }
            }
        } else {
            Log.d("List", "END");
        }
    }

    static class fetcher_fics extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentFicByReqList> activityReference;

        fetcher_fics(FragmentFicByReqList context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (activityReference.get() == null) {
                    return null;
                }
                if (activityReference.get().Loading) {
                    if (activityReference.get().Loading) {
                        return null;
                    }
                    activityReference.get().Loading = true;
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String url = activityReference.get().UrlTemplate.replace("@", activityReference.get().StartPosition.toString());
                if (!url.isEmpty()) {
                    Log.i("LOAD", url);

                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            doc = Jsoup.parse(body.string());
                        }
                        response.close();
                    }
                } else {
                    return null;
                }

                if (activityReference.get() == null) {
                    return null;
                }
                activityReference.get().MaxPosition = 1;

                if (activityReference.get().UrlTemplate.contains("find")) {
                    activityReference.get().MaxPosition = 10000;
                }

                if (!doc.select("nav.pagination-holder li.text b").isEmpty()) {
                    activityReference.get().MaxPosition = Integer.parseInt(doc.select("nav.pagination-holder li.text b").get(1).text());
                }

                if (!doc.select("li.text input").isEmpty()) {
                    activityReference.get().MaxPosition = Integer.parseInt(doc.select("li.text input").attr("max"));
                }

                Log.d("MAX P", activityReference.get().MaxPosition + "!");
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
                if ((doc != null) && (activityReference.get() != null)) {
                    if (doc.select("title").text().equals("Технические работы")) {
                        Toast.makeText(activityReference.get().context, "На фикбуке технические работы!", Toast.LENGTH_LONG).show();
                    } else {
                        activityReference.get().ParseFicList(doc);
                    }
                }
                if (activityReference.get() != null) {
                    activityReference.get().swipeContainer.setRefreshing(false);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseFicList(Document docx) {
        if (refresh) {
            Fics.clear();
            rv_adapter.notifyDataSetChanged();
        }
        try {
            Elements vid = docx.select("div.request-list div.row");
            HashMap<String, Object> map;
            for (Element v : vid) {
                map = new HashMap<>();
                if (v.select("a").size() > 0) {
                    String fic_title = v.select("a").get(1).text();
                    String fic_id = v.select("a").get(1).attr("href").split("/")[2];
                    String request_title = v.select("a").get(0).text();
                    String request_id = v.select("a").get(0).attr("href").replace("/requests/", "");
                    String date = v.select("small").text();

                    map.put("fic_title", fic_title);
                    map.put("fic_id", fic_id);
                    map.put("request_title", request_title);
                    map.put("request_id", request_id);
                    map.put("date", date);

//                    Application.logLargeString(map.toString());

                    Fics.add(map);
                }
            }
            Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

        StartPosition++;
        Loading = false;
        pb1.setVisibility(View.GONE);
        refresh = false;

        if (Fics.isEmpty()) {
            if (getActivity() != null) {
                LinearLayout mRoot = getActivity().findViewById(R.id.content_main);
                if (mRoot != null) {
                    Snackbar snackbar = Snackbar.make(mRoot, "Ничего не найдено :(", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }
    }
}



