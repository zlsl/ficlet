package zlobniyslaine.ru.ficbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;


public class FragmentProfile_Info extends Fragment {

    WebView wv_info;

    private String UrlTemplate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_info, container, false);

        wv_info = rootView.findViewById(R.id.wv_info);

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url");
            new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return rootView;
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentProfile_Info> activityReference;

        fetcher_main(FragmentProfile_Info context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            try {
                Response response = Application.httpclient.newCall(Application.getRequestBuilder(activityReference.get().UrlTemplate)).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    doc = Jsoup.parse(body.string());
                }
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
            try {
                if (activityReference.get() != null) {
                    activityReference.get().ParseProfile(doc);
                }
            } catch (Exception e) {
                Log.e("ERR", e.toString());
            }
            Application.firePopup();
        }
    }

    private void ParseProfile(Document doc) {
        if (doc == null) {
            return;
        }
        try {
            String text = doc.select("section.profile-container section").select("section.mb-30").get(0).html() + doc.select("section.profile-container section").select("section.mb-30").get(1).html();
            String in = text;
            String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(in);
            while (m.find()) {
                if ((m.group().contains("jpg")) || (m.group().contains("png"))) {
                    text = text.replace(m.group(), "<img style='width:100%;' src='" + m.group() + "'>");
                } else {
                    text = text.replace(m.group(), "<a href='" + m.group() + "'>" + m.group() + "</a>");
                }
            }
            text = "<style>.alert-danger{background: #f2dede; color: #a94442; width: 100%;} .alert-success{background: #dff0d8; color: #3c763d; width: 100%;};a {background: #999; color: #FFF; text-decoration: none; padding: 2px; border-radius: 3px;font-size: 16px;}</style>" + text;
            wv_info.setWebChromeClient(new WebChromeClient());
            wv_info.loadDataWithBaseURL("http://ficlet.app", text, "text/html", "UTF-8", "");
            wv_info.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Application.openUrl(url, getContext());
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}