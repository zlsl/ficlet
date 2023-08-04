package zlobniyslaine.ru.ficbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterAuthors;
import zlobniyslaine.ru.ficbook.moshis.Author;
import zlobniyslaine.ru.ficbook.moshis.Authors;


@SuppressWarnings("WeakerAccess")
public class ActivityAuthorsSearch extends AppCompatActivity {

    private ArrayList<HashMap<String, Object>> AuthorsList;

    EditText e_search;
    RecyclerView rv1;
    ProgressBar pb1;

    void do_seacrh() {
        find = e_search.getText().toString();
        AuthorsList.clear();
        try {
            Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (!find.isEmpty()) {
            page = 1;
            pb1.setVisibility(View.VISIBLE);
            new fetcher_authors(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private final ActivityAuthorsSearch context = this;
    private String find = "";
    private Integer page = 1;
    private Boolean more = false;
    private Boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_authors_search);

        e_search = findViewById(R.id.e_search);
        rv1 = findViewById(R.id.rv1);
        pb1 = findViewById(R.id.pb1);
        e_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                do_seacrh();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AuthorsList = new ArrayList<>();

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(this);

        rv1.setLayoutManager(mLayoutManager);

        AdapterAuthors rv_adapter = new AdapterAuthors(this, AuthorsList);
        rv1.setAdapter(rv_adapter);

        rv1.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {

            @Override
            public void onLoadMore() {
                if (more && !loading) {
                    pb1.setVisibility(View.VISIBLE);
                    new fetcher_authors(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }

    static class fetcher_authors extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityAuthorsSearch> activityReference;

        fetcher_authors(ActivityAuthorsSearch context) {
            activityReference = new WeakReference<>(context);
        }

        Response response;
        ResponseBody body;
        String bodyx = "";

        @Override
        protected Void doInBackground(String... params) {
            try {
                activityReference.get().loading = true;


                RequestBody formBody = new FormBody.Builder()
                        .add("q", activityReference.get().find)
                        .add("page", activityReference.get().page.toString())
                        .build();

//                response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/authors/search?q=" + activityReference.get().find + "&page=" + activityReference.get().page)).execute();
                response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/authors/search", formBody)).execute();

                body = response.body();
                if (body != null) {
                    bodyx = body.string();
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
                if (activityReference.get() != null) {
                    activityReference.get().ParseAuthorsList(bodyx);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseAuthorsList(String json) {
        Authors authors = null;

//        Application.logLargeString(json);

        try {
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<Authors> jsonAdapter = moshi.adapter(Authors.class);
            authors = jsonAdapter.fromJson(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (page == 1) {
            AuthorsList.clear();
        }
        HashMap<String, Object> map;

        if (authors != null) {
            for (Author a : authors.data.result) {
                zlobniyslaine.ru.ficbook.models.Authors col = new zlobniyslaine.ru.ficbook.models.Authors();
                col.name = a.nickname;
                col.avatar_url = a.avatar_path;
                col.nid = a.id.toString();
                col.save();

                map = new HashMap<>();
                map.put("name", a.nickname);
                map.put("author_id", a.id.toString());
                AuthorsList.add(map);
            }
            more = authors.data.more;
            if (more) {
                page++;
            }
        }

        Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
        pb1.setVisibility(View.GONE);
        loading = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        new fetcher_authors(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
