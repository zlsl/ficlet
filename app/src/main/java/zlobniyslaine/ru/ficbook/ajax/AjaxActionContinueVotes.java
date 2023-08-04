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

public class AjaxActionContinueVotes {

    private String part_id;
    private Boolean add;

    public void Do(String id, Boolean add) {
        part_id = id;
        this.add = add;
        new send_request(this).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final WeakReference<AjaxActionContinueVotes> activityReference;

        send_request(AjaxActionContinueVotes context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String url;

                if (activityReference.get().add) {
                    url = "https://ficbook.net/fanfics/continue_votes/add";
                } else {
                    url = "https://ficbook.net/fanfics/continue_votes/remove";
                }

                RequestBody formBody = new FormBody.Builder()
                        .add("part_id", activityReference.get().part_id)
                        .build();

                Response response = Application.httpclient.newCall(Application.getRequestBuilder(url, formBody)).execute();
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