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
import zlobniyslaine.ru.ficbook.adapters.AdapterCollectionsFic;


public class FragmentFic_Collections extends Fragment {

    private ArrayList<HashMap<String, Object>> FicCollections = new ArrayList<>();
    private AdapterCollectionsFic rv_adapter;
    private String UrlTemplate = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_collections, container, false);

        RecyclerView rv_collections = rootView.findViewById(R.id.lv_collections);
        FicCollections = new ArrayList<>();

        rv_collections.setHasFixedSize(true);
        rv_collections.setLayoutManager(new WrapContentLinearLayoutManager(rootView.getContext()));
        rv_adapter = new AdapterCollectionsFic(rootView.getContext(), FicCollections);
        rv_collections.setAdapter(rv_adapter);

        if (getArguments() != null) {
            String id = getArguments().getString("id");
            UrlTemplate = "https://ficbook.net/collections/" + id + "/list";
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        FicCollections.clear();
        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentFic_Collections> activityReference;

        fetcher_main(FragmentFic_Collections context) {
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
                    response.close();
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
                Elements toc = doc.select("div.collection-thumb");
                HashMap<String, Object> map;
                for (Element v : toc) {
                    String id = v.select("div.collection-thumb-info a").attr("href").replace("/collections/", "");
                    String author_name = v.select("div.collection-thumb-author a").text();
                    String title = v.select("div.collection-thumb-info a").text().trim().replace(author_name, "");
                    String count = v.select("div.collection-thumb-info").text().replace(author_name, "").replace(title, "").replace("(", "").replace(")", "");
                    String author_id = v.select("div.collection-thumb-author a").attr("href").replace("/authors/", "");


                    Log.e("t", title);
                    Log.e("au", author_name);
                    Log.e("co", count);

                    map = new HashMap<>();
                    map.put("id", id);
                    map.put("title", title);
                    map.put("count", count);
                    map.put("locked", "0");
                    map.put("author_id", author_id);
                    map.put("author_name", author_name);
                    if (activityReference.get() != null) {
                        activityReference.get().FicCollections.add(map);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (activityReference.get() != null) {
                activityReference.get().rv_adapter.notifyDataSetChanged();
            }
            Application.firePopup();
        }
    }

}