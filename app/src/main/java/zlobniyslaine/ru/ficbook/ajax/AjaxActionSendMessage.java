package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlobniyslaine.ru.ficbook.Application;

public class AjaxActionSendMessage {

    private String thread_id;
    private String message;


    public void Do(String thread_id, String message) {
        this.thread_id = thread_id;
        this.message = message;
        new send_request(this).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final WeakReference<AjaxActionSendMessage> activityReference;

        send_request(AjaxActionSendMessage context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {

                JSONObject j = new JSONObject();
                j.put("thread_id", activityReference.get().thread_id);
                j.put("text", activityReference.get().message);

                Response response = Application.httpclient.newCall(Application.getJSONRequestBuilderB("https://ficbook.net/home/messaging/send", j)).execute();
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