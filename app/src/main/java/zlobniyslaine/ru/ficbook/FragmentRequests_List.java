package zlobniyslaine.ru.ficbook;

//TODO: tags search view

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterRequests;
import zlobniyslaine.ru.ficbook.models.Tags;


public class FragmentRequests_List extends Fragment implements FragmentManager.OnBackStackChangedListener {

    private ArrayList<HashMap<String, Object>> Requests = new ArrayList<>();

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

//        hash = Application.md5(UrlTemplate);

        Requests = new ArrayList<>();

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(context);

        rv1.setLayoutManager(mLayoutManager);

        AdapterRequests rv_adapter = new AdapterRequests(context, Requests);
        rv1.setAdapter(rv_adapter);

        EndlessRecyclerViewScrollListener2 scrollListener = new EndlessRecyclerViewScrollListener2(mLayoutManager) {
            @Override
            public void onLoadMore() {
                runFetcher();
            }
        };
        rv1.addOnScrollListener(scrollListener);

        swipeContainer.setOnRefreshListener(() -> {
            Log.d("SWREFRESH", "S:" + Requests.size());
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
        StartPosition = 0;
        refresh = true;
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
                new fetcher_requests(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            Log.d("List", "END");
        }
    }

    static class fetcher_requests extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentRequests_List> activityReference;

        fetcher_requests(FragmentRequests_List context) {
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
                    Log.i("RQ LOAD", url);

                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                    if (response.isSuccessful()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            doc = Jsoup.parse(body.string());
                        }
                    }
                    response.close();
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

            if (activityReference.get().refresh) {
                activityReference.get().Requests.clear();
            }
            try {
                Elements vid = doc.select("article.request-thumb");
                HashMap<String, Object> map;

                for (Element v : vid) {
                    map = new HashMap<>();
                    String title = v.select("h3").text().trim();
                    String id = v.select("a.visit-link").attr("href").replace("/requests/", "");
                    String like = v.select("div.container-counter").get(0).select("span.int-count").text().trim();
                    String bookmark = v.select("div.container-counter").get(1).select("span.int-count").text().trim();
                    String fics = "";
                    if (!v.select("a.container-counter").isEmpty()) {
                        fics = v.select("a.container-counter").select("span").text().trim();
                        if (fics.contains("!")) {
                            fics = "";
                        }
                    }
                    String description = v.select("div.post-content").html();
                    String fandoms = v.select("strong.title").text();

                    String s_content = YO.createTags(description);

                    List<String> listDirection = Arrays.asList(activityReference.get().getResources().getStringArray(R.array.array_direction));
                    List<String> listRating = Arrays.asList(activityReference.get().getResources().getStringArray(R.array.array_rating));

                    StringBuilder rating = new StringBuilder();
                    StringBuilder genres = new StringBuilder();
                    StringBuilder caution = new StringBuilder();
                    StringBuilder directions = new StringBuilder();
                    StringBuilder tags = new StringBuilder();

                    Elements etags = v.select("div.tags");
                    try {
                        for (Element g : etags.select("a")) {
                            String gg = g.text().replaceAll("<[^>]*>", "");
                            if (g.hasClass("tag-adult")) {
                                gg = gg + " \uD83D\uDD1E";
                            }
                            if (g.hasClass("disliked-parameter-link")) {
                                gg = "<b><font color=\"#8a2525\">" + gg + "</font></b>";
                            }
                            if (g.hasClass("liked-parameter-link")) {
                                gg = "<b><font color=\"#086e00\">" + gg + "</font></b>";
                            }

                            String tag_id = g.attr("href").replace("/tags/", "").replace("/requests", "");
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

                    Elements rq = v.select("p.request-description>span.help");
                    for (Element e : rq) {
                        String el = e.text();
                        if (activityReference.get().inArray(listDirection, el)) {
                            if (directions.length() > 0) {
                                directions.append(", ");
                            }
                            if (e.hasClass("disliked-parameter-link")) {
                                el = "<b><font color=\"#8a2525\">" + el + "</font></b>";
                            }
                            if (e.hasClass("liked-parameter-link")) {
                                el = "<b><font color=\"#086e00\">" + el + "</font></b>";
                            }
                            directions.append(el);
                        }
                        if (activityReference.get().inArray(listRating, el)) {
                            if (rating.length() > 0) {
                                rating.append(", ");
                            }
                            if (e.hasClass("disliked-parameter-link")) {
                                el = "<b><font color=\"#8a2525\">" + el + "</font></b>";
                            }
                            if (e.hasClass("liked-parameter-link")) {
                                el = "<b><font color=\"#086e00\">" + el + "</font></b>";
                            }
                            rating.append(el);
                        }
                    }
                    if (genres.length() > 2) {
                        genres = new StringBuilder(genres.substring(0, genres.length() - 2));
                    }
                    if (caution.length() > 2) {
                        caution = new StringBuilder(caution.substring(0, caution.length() - 2));
                    }

                    map.put("id", id);
                    map.put("title", title);
                    map.put("like", like);
                    map.put("bookmark", bookmark);
                    map.put("description", s_content);
                    map.put("fandoms", fandoms);
                    map.put("rating", rating.toString());
                    map.put("genres", genres.toString());
                    map.put("cautions", caution.toString());
                    map.put("direction", directions.toString());
                    map.put("fics", fics);
                    activityReference.get().Requests.add(map);
                }

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
//                    } else {
//                        activityReference.get().ParseRequestsList(doc);
                    }
                }
                if (activityReference.get() != null) {
                    Objects.requireNonNull(activityReference.get().rv1.getAdapter()).notifyDataSetChanged();
                    activityReference.get().StartPosition++;
                    activityReference.get().Loading = false;
                    activityReference.get().pb1.setVisibility(View.GONE);
                    activityReference.get().refresh = false;
                    activityReference.get().swipeContainer.setRefreshing(false);

                    if (activityReference.get().Requests.isEmpty()) {
                        if (activityReference.get() != null) {
                            LinearLayout mRoot = activityReference.get().requireActivity().findViewById(R.id.content_main);
                            if (mRoot != null) {
                                Snackbar snackbar = Snackbar.make(mRoot, "Ничего не найдено :(", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private Boolean inArray(List<String> list, String search) {
        for (String str : list) {
            if (str.equals(search)) {
                return true;
            }
        }
        return false;
    }
}



