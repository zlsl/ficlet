package zlobniyslaine.ru.ficbook;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterChatThreads;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionTokens;
import zlobniyslaine.ru.ficbook.moshis.Chat;
import zlobniyslaine.ru.ficbook.moshis.ChatThreads;


@SuppressWarnings("WeakerAccess")
public class ActivityMessagingList extends AppCompatActivity {

    private List<Chat> threads;
    private RecyclerView rv1;
    private ProgressBar pb1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_messaging_list);
        rv1 = findViewById(R.id.rv1);
        pb1 = findViewById(R.id.pb1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        threads = new ArrayList<>();

        rv1.setHasFixedSize(true);
        rv1.setLayoutManager(new WrapContentLinearLayoutManager(this));
        AdapterChatThreads rv_adapter = new AdapterChatThreads(this, threads);
        rv1.setAdapter(rv_adapter);

        try {
            SharedPreferences.Editor editor = Application.sPref.edit();
            editor.remove("notify_icon-envelop3");
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        AjaxActionTokens action = new AjaxActionTokens();
        action.Do();
    }

    @Override
    public void onResume() {
        super.onResume();
        new fetcher_chats(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class fetcher_chats extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityMessagingList> activityReference;

        fetcher_chats(ActivityMessagingList context) {
            activityReference = new WeakReference<>(context);
        }

        Response response;
        ResponseBody body;
        String bodyx = "";

        @Override
        protected Void doInBackground(String... params) {
            try {
                JSONObject j = new JSONObject();
                j.put("skip", 0);

                response = Application.httpclient.newCall(Application.getJSONRequestBuilder("https://ficbook.net/home/messaging")).execute(); // clear list
                response = Application.httpclient.newCall(Application.getJSONRequestBuilderB("https://ficbook.net/home/messaging/threads", j)).execute();
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

            ChatThreads chatThreads = null;

            try {
                Moshi moshi = new Moshi.Builder().build();

                JsonAdapter<ChatThreads> jsonAdapter = moshi.adapter(ChatThreads.class);
                chatThreads = jsonAdapter.fromJson(bodyx);
            } catch (Exception e) {
                Log.e("E", e.toString());
                e.printStackTrace();
            }

            activityReference.get().threads.clear();
            if (chatThreads != null) {
                activityReference.get().threads.addAll(chatThreads.data.threads);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Application.firePopup();
            if (activityReference.get() != null) {
                Objects.requireNonNull(activityReference.get().rv1.getAdapter()).notifyDataSetChanged();
                activityReference.get().pb1.setVisibility(View.GONE);
            }
        }
    }

}
