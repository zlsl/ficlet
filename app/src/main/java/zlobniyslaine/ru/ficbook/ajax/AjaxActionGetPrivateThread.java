package zlobniyslaine.ru.ficbook.ajax;


import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.Application;

public class AjaxActionGetPrivateThread {

    private OnTokenReceived onTokenReceived;

    public interface OnTokenReceived {
        void onToken(String token);
    }

    public void setOnTokenReceived(OnTokenReceived listener) {
        onTokenReceived = listener;
    }

    private String user_id = "1925988";

    public void Do(String u) {
        user_id = u;
        new send_request(this).execute();
    }

    static class send_request extends AsyncTask<String, Void, Void> {
        private final WeakReference<AjaxActionGetPrivateThread> activityReference;

        send_request(AjaxActionGetPrivateThread context) {
            activityReference = new WeakReference<>(context);
        }

        Response response;

        @Override
        protected Void doInBackground(String... params) {
            try {
                JSONObject j = new JSONObject();
                j.put("user_id", activityReference.get().user_id);

                response = Application.httpclient.newCall(Application.getJSONRequestBuilderB("https://ficbook.net/home/messaging/getorcreateprivatethread", j)).execute();
                ResponseBody body = response.body();
                String token = "";
                if (body != null) {
                    token = body.string();
                }
                response.close();

                Pattern pattern = Pattern.compile("id\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(token);
                if (matcher.find()) {
                    token = matcher.group(1);
                }

                activityReference.get().onTokenReceived.onToken(token);
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