package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import zlobniyslaine.ru.ficbook.Application;

public class AjaxActionCollectionAdd {

    String fanfic_id;
    String collection_id;


    public void Do(String id, String c_id) {
        this.fanfic_id = id;
        this.collection_id = c_id;

        Log.i("ADD", id + " = " + fanfic_id + " " + collection_id);
        new send_request(id, c_id).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final String c_fanfic_id;
        private final String c_collection_id;

        send_request(String id, String c_id) {
            c_fanfic_id = id;
            c_collection_id = c_id;
        }

        @Override
        protected Void doInBackground(String... params) {
            Log.i("ADD", c_fanfic_id + " " + c_collection_id);
            try {
                RequestBody formBody = new FormBody.Builder()
                        .add("fanfic_id", c_fanfic_id)
                        .add("collection_id", c_collection_id)
                        .build();

                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/ajax/collections/addfanfic", formBody)).execute();
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