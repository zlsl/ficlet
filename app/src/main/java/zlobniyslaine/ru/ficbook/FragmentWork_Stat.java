package zlobniyslaine.ru.ficbook;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.Response;
import okhttp3.ResponseBody;


public class FragmentWork_Stat extends Fragment {

    WebView wv_stat;

    private String UrlTemplate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_work_stat, container, false);

        wv_stat = rootView.findViewById(R.id.wv_stat);

        UrlTemplate = "https://ficbook.net/home/myfics/5603402/stats";

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url");
        }

        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentWork_Stat> activityReference;

        fetcher_main(FragmentWork_Stat context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc;
        final StringBuilder scripts = new StringBuilder();

        @Override
        protected Void doInBackground(String... params) {
            try {
                Response response = Application.httpclient.newCall(Application.getRequestBuilder(activityReference.get().UrlTemplate)).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    doc = Jsoup.parse(body.string().replace("AdvManager", "").replace("yandex", "").replace("Metrika", ""));
                }
                response.close();

                doc.select("nav").remove();
                doc.select("h1").remove();
                doc.select(".if-you-were-premium").remove();

                for (Element scr : doc.select("script")) {
                    if (
                            (!scr.toString().contains("Yandex")) &&
                                    (!scr.toString().contains("AdvManager")) &&
                                    (!scr.toString().contains("pagead")) &&
                                    (!scr.toString().contains("Metrika"))
                    ) {
                        scripts.append(scr.toString());
                    }
                }
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

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected void onPostExecute(Void result) {
            try {
                if (activityReference.get() != null) {
                    WebSettings webSettings = activityReference.get().wv_stat.getSettings();
                    webSettings.setJavaScriptEnabled(true);
                    activityReference.get().wv_stat.setWebChromeClient(new WebChromeClient());

                    activityReference.get().wv_stat.loadDataWithBaseURL("https://ficbook.net", "<link href=\"https://static.ficbook.net/ficbook/assets/app.dc36bbe4.css\" rel=\"stylesheet\">" + scripts + doc.body().select("section.content-section").toString(), "text/html", "UTF-8", "");
                    activityReference.get().wv_stat.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            return true;
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }
}