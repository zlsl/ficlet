package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;
import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.moshis.JsonResponse;

public class AjaxActionLike {

    public void Do(String id, Boolean like) {
        new send_request(id, like).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {

        final String fic_id;
        final Boolean like;

        send_request(String f_id, Boolean f_like) {
            this.fic_id = f_id;
            this.like = f_like;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String action;
                if (like) {
                    action = "add";
                } else {
                    action = "remove";
                }

                RequestBody formBody = new FormBody.Builder()
                        .add("fanfic_id", fic_id)
                        .add("action", action)
                        .build();

                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/ajax/mark", formBody)).execute();
                String re = Objects.requireNonNull(response.body()).string();
                response.close();

                JsonResponse jresp = null;
                try {
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<JsonResponse> jsonAdapter = moshi.adapter(JsonResponse.class);
                    jresp = jsonAdapter.fromJson(re);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("LIKE", fic_id + " " + re);
                if (jresp != null) {
                    if (!jresp.result) {
                        Application.displayPopup(jresp.error);
                    }
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }
}