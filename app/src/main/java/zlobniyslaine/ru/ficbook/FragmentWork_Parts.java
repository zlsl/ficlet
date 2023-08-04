package zlobniyslaine.ru.ficbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
import zlobniyslaine.ru.ficbook.adapters.AdapterWorkParts;


public class FragmentWork_Parts extends Fragment {

    RecyclerView rv1;

    private ArrayList<HashMap<String, Object>> FicParts = new ArrayList<>();
    private AdapterWorkParts rv_adapter;
    private String UrlTemplate = "";
    private String id = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_work_parts, container, false);

        rv1 = rootView.findViewById(R.id.rv_parts);

        FicParts = new ArrayList<>();

        rv1.setLayoutManager(new WrapContentLinearLayoutManager(rootView.getContext()));
        rv_adapter = new AdapterWorkParts(rootView.getContext(), FicParts);
        rv1.setAdapter(rv_adapter);

        if (getArguments() != null) {
            id = getArguments().getString("id");
            UrlTemplate = "https://ficbook.net/home/myfics/" + id;
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        FicParts.clear();
        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentWork_Parts> activityReference;

        fetcher_main(FragmentWork_Parts context) {
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
                activityReference.get().ParseParts(doc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseParts(Document doc) {
        try {
            HashMap<String, Object> map;
            Elements toc = doc.select("div.parts>div.part");
            for (Element v : toc) {
                String topic_title = v.select("a").text().trim();
                String topic_id = v.select("a").attr("href").replace("/home/myfics/" + id + "/parts/", "");

                map = new HashMap<>();
                map.put("title", topic_title);
                map.put("wait", v.select("sup.color-continue").text());
                map.put("fanfic_id", id);
                map.put("part_id", topic_id);
                map.put("draft", (v.select("span.draft").size() > 0));
                FicParts.add(map);
            }

        } catch (Exception e) {
            Log.e("PARSE", e.toString());
        }
        rv_adapter.notifyDataSetChanged();
    }
}