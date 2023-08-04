package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlobniyslaine.ru.ficbook.Application;

public class AjaxActionCollectionMove {

    public void Do(String id, String c_old_id, String c_new_id) {
        new send_request(id, c_old_id, c_new_id).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {

        private final String fanfic_id;
        private final String old_collection_id;
        private final String new_collection_id;

        send_request(String f_id, String id1, String id2) {
            this.fanfic_id = f_id;
            this.old_collection_id = id1;
            this.new_collection_id = id2;
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                RequestBody formBody = new FormBody.Builder()
                        .add("fic_id", fanfic_id)
                        .add("old_collection_id", old_collection_id)
                        .add("new_collection_id", new_collection_id)
                        .add("action", "move")
                        .build();

                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/ajax/collection", formBody)).execute();
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