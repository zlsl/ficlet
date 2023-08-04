package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlobniyslaine.ru.ficbook.Application;

public class AjaxActionTokens {

    public void Do() {
        new send_request().execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {

        send_request() {
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                RequestBody formBody = new FormBody.Builder()
                        .build();

                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/home/messaging/gettoken", formBody)).execute();
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