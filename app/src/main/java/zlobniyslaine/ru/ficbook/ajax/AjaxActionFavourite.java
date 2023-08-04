package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlobniyslaine.ru.ficbook.Application;

public class AjaxActionFavourite {

    private String author_id;
    private Boolean like;

    public void Do(String id, Boolean like) {
        author_id = id;
        this.like = like;
        new send_request(this).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final WeakReference<AjaxActionFavourite> activityReference;

        send_request(AjaxActionFavourite context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String action;
                if (activityReference.get().like) {
                    action = "add";
                } else {
                    action = "remove";
                }

                RequestBody formBody = new FormBody.Builder()
                        .add("author_id", activityReference.get().author_id)
                        .add("action", action)
                        .build();

                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/ajax/favourite", formBody)).execute();
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

        }
    }
}