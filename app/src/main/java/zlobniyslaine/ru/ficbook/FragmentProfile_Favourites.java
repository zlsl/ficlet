package zlobniyslaine.ru.ficbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;

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


public class FragmentProfile_Favourites extends Fragment {

    RecyclerView rv1;

    private ArrayList<HashMap<String, Object>> Favourites = new ArrayList<>();
    private AdapterAuthors rv_adapter;
    private String UrlTemplate = "";

    private final int MAX_FAVS = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_favourites, container, false);

        rv1 = rootView.findViewById(R.id.rv_favs);

        Favourites = new ArrayList<>();

        rv1.setLayoutManager(new WrapContentLinearLayoutManager(rootView.getContext()));
        rv_adapter = new AdapterAuthors(rootView.getContext(), Favourites);
        rv1.setAdapter(rv_adapter);

        if (getArguments() != null) {
            String id = getArguments().getString("id");
            UrlTemplate = "https://ficbook.net/authors/" + id;
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Favourites.clear();
        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentProfile_Favourites> activityReference;

        fetcher_main(FragmentProfile_Favourites context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            if (activityReference.get() == null) {
                return null;
            }
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

            try {
                int added = 0;
                HashMap<String, Object> map;
                Elements toc = doc.select("div.columned a");
                for (Element v : toc) {
                    added++;
                    String id = v.attr("href").replace("/authors/", "");
                    String name = v.text();

                    map = new HashMap<>();

                    map.put("name", name);
                    map.put("author_id", id);
                    map.put("avatar_url", "");

                    Authors a = new Select()
                            .from(Authors.class)
                            .where("nid = ?", Objects.requireNonNull(map.get("author_id")).toString())
                            .executeSingle();

                    if (a != null) {
                        map.put("avatar_url", a.avatar_url);
                    } else {
                        a = new Authors();
                        a.nid = Objects.requireNonNull(map.get("author_id")).toString();
                        a.name = Objects.requireNonNull(map.get("name")).toString();
                        a.avatar_url = Objects.requireNonNull(map.get("avatar_url")).toString();
                        a.save();
                    }

                    activityReference.get().Favourites.add(map);
                    if (added > activityReference.get().MAX_FAVS) {
                        try {
                            Toast.makeText(Application.getContext(), "У автора " + toc.size() + " подписчиков. Отображается только " + activityReference.get().MAX_FAVS, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (activityReference.get() != null) {
                    activityReference.get().rv_adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

}