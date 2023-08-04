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

public class AjaxActionReplyReview {

    private String follow;
    private String part_id;
    private String comment;

    public void Do(String part_id, String comment, Boolean follow) {
        this.part_id = part_id;
        this.comment = comment;
        if (follow) {
            this.follow = "1";
        } else {
            this.follow = "0";
        }
        new send_request(this).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final WeakReference<AjaxActionReplyReview> activityReference;

        send_request(AjaxActionReplyReview context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                RequestBody formBody = new FormBody.Builder()
                        .add("part_id", activityReference.get().part_id)
                        .add("comment", activityReference.get().comment)
                        .add("follow", activityReference.get().follow)
                        .build();
                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/comment/add", formBody)).execute();
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