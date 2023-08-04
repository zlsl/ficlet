package zlobniyslaine.ru.ficbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
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

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterWorks;


public class FragmentWorks_List extends Fragment {

    private ArrayList<HashMap<String, Object>> Works = new ArrayList<>();

    private Boolean Loading = false;
    private Boolean refresh = true;
    private Boolean FirstRun = true;
    private Integer StartPosition = 0;
    private Integer MaxPosition = 100000;
    private String UrlTemplate;

    RecyclerView rv1;
    ProgressBar pb1;
    SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_works_list, container, false);

        rv1 = rootView.findViewById(R.id.rv1);
        pb1 = rootView.findViewById(R.id.pb1);
        swipeContainer = rootView.findViewById(R.id.swipeContainer);

        Works = new ArrayList<>();

        UrlTemplate = "https://ficbook.net/home/myfics";

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url", "");
            Log.i("URL", UrlTemplate);
        }

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(rootView.getContext());
        rv1.setLayoutManager(mLayoutManager);
        AdapterWorks rv_adapter = new AdapterWorks(rootView.getContext(), Works);
        rv1.setAdapter(rv_adapter);

        rv1.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {

                if ((!Loading) && ((totalItemCount > 3) || (totalItemCount == 0)) && (StartPosition <= MaxPosition)) {
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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        StartPosition = 0;
        refresh = true;
        runFetcher();
    }

    private void runFetcher() {
        FirstRun = false;
        if (StartPosition == 0) {
            swipeContainer.setRefreshing(true);
            StartPosition = 1;
            Works.clear();
            if (rv1.getAdapter() != null) {
                rv1.getAdapter().notifyDataSetChanged();
            }
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
                new fetcher_works(this).execute(u);
            }
        } else {
            Log.i("List", "END");
        }
    }

    static class fetcher_works extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentWorks_List> activityReference;

        fetcher_works(FragmentWorks_List context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            Log.i("URL", params[0]);
            try {
                if (activityReference.get().Loading) {
                    return null;
                }
                activityReference.get().Loading = true;

                Response response = Application.httpclient.newCall(Application.getRequestBuilder(params[0])).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    doc = Jsoup.parse(body.string());
                }
                response.close();

                activityReference.get().MaxPosition = 1;
                if (doc.select("ul.pagination").select("li.text").select("b").size() > 0) {
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
                activityReference.get().ParseWorks(doc);
                activityReference.get().swipeContainer.setRefreshing(false);
                Application.firePopup();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ParseWorks(Document doc) {
        try {

            Elements vid = doc.select("div.myfic");
            HashMap<String, Object> map;

//            ActiveAndroid.beginTransaction();

            for (Element v : vid) {
                map = new HashMap<>();

                String s_stitle = v.select("div.title a").first().text();
                String s_id = v.select("div.title a").first().attr("href").replace("/home/myfics/", "").replace("/readfic/", "");
                String s_votes = v.select("div.title sup").first().text();

                StringBuilder s_parts = new StringBuilder();
                Elements parts = v.select("div.part");
                for (Element p : parts) {
                    s_parts.append("&#8226; ").append(p.select("div.title").text()).append("<br>\n");
                }

                map.put("fanfic_id", s_id);
                map.put("title", s_stitle);
                map.put("votes", s_votes);
                map.put("parts", s_parts);
                Works.add(map);
            }
            if (rv1.getAdapter() != null) {
                rv1.getAdapter().notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        StartPosition++;
        Loading = false;
        pb1.setVisibility(View.GONE);
        refresh = false;
    }
}