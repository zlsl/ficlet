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

public class AjaxActionRead {

    private String fanfic_id;
    private Boolean read;

    public void Do(String id, Boolean read) {
        fanfic_id = id;
        this.read = read;
        new send_request(this).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final WeakReference<AjaxActionRead> activityReference;

        send_request(AjaxActionRead context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                String url;
                if (activityReference.get().read) {
                    url = "https://ficbook.net/ajax/fanfic/read";
                } else {
                    url = "https://ficbook.net/ajax/fanfic/unread";
                }
                RequestBody formBody = new FormBody.Builder()
                        .add("fanfic_id", activityReference.get().fanfic_id)
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