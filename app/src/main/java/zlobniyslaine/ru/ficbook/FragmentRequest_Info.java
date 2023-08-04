package zlobniyslaine.ru.ficbook;

import static zlobniyslaine.ru.ficbook.Application.logLargeString;

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

import org.apmem.tools.layouts.FlowLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.controls.ViewBadgeWidget;


public class FragmentRequest_Info extends Fragment {

    FlowLayout l_fandoms;
    FlowLayout l_directions;
    FlowLayout l_ratings;
    WebView wv_description;

    private String UrlTemplate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_request_info, container, false);

        l_fandoms = rootView.findViewById(R.id.l_fandoms);
        l_directions = rootView.findViewById(R.id.l_directions);
        l_ratings = rootView.findViewById(R.id.l_ratings);
        wv_description = rootView.findViewById(R.id.wv_description);

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url").replace("?p=@", "");
            new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return rootView;
    }


    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentRequest_Info> activityReference;

        fetcher_main(FragmentRequest_Info context) {
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
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseProfile(Document doc) {
        try {
            Elements c;
            if (doc.select("section.request-content").size() > 0) {
                c = doc.select("section.request-content");
            } else {
                c = doc.select("section.content-section");
            }

            String title = c.select("h1").text().trim();

            String author_name = c.select("div.avatar-container a.avatar-nickname").text();
            String author_id = c.select("div.avatar-container a.avatar-nickname").attr("href").replace("/authors/", "");
            String avatar_url = c.select("div.avatar-container img").attr("src");

            ((ActivityRequest) requireActivity()).setInfo(title, author_id, author_name, avatar_url);

            String info = c.select("div.word-break").html().replace("\n", "<br>");
            Elements header = doc.select("section.request-content>div:eq(3)>div:eq(1)");

            String text = info;
            String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(info);
            while (m.find()) {
                if ((m.group().contains("jpg")) || (m.group().contains("png"))) {
                    text = text.replace(m.group(), "<img style='width:100%;' src='" + m.group() + "'>");
                } else {
                    text = text.replace(m.group(), "<a href='" + m.group() + "'>" + m.group() + "</a>");
                }
            }

            text = "<style>.alert-danger{background: #f2dede; color: #a94442; width: 100%;} .alert-success{background: #dff0d8; color: #3c763d; width: 100%;};a {background: #999; color: #FFF; text-decoration: none; padding: 2px; border-radius: 3px;font-size: 16px;}</style>" + text;
            wv_description.setWebChromeClient(new WebChromeClient());
            wv_description.loadDataWithBaseURL("http://ficlet.app", text, "text/html", "UTF-8", "");
            wv_description.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Application.openUrl(url, getContext());
                    return true;
                }
            });


            List<String> listDirection = Arrays.asList(getResources().getStringArray(R.array.array_direction));
            List<String> listRating = Arrays.asList(getResources().getStringArray(R.array.array_rating));

            String hilight;

            Elements fq = header.select("a");
            for (Element e : fq) {
                ViewBadgeWidget add_fandom = new ViewBadgeWidget(this.getContext());
                add_fandom.setWidgetInfo("fandom", e.text(), "https://ficbook.net" + e.attr("href"), "");
                l_fandoms.addView(add_fandom);
            }


            Elements rq = header.select("span.help");
            for (Element e : rq) {
                String el = e.text();
                if (el == null) {
                    el = "";
                }
                if (inArray(listDirection, el)) {
                    hilight = "";
                    ViewBadgeWidget add_direction = new ViewBadgeWidget(this.getContext());
                    if (e.hasClass("disliked-parameter-link")) {
                        hilight = "]";
                    }
                    if (e.hasClass("liked-parameter-link")) {
                        hilight = "[";
                    }
                    add_direction.setWidgetInfo("direction", el + hilight, "", "");
                    l_directions.addView(add_direction);
                }
                if (inArray(listRating, el)) {
                    hilight = "";
                    ViewBadgeWidget add_rating = new ViewBadgeWidget(this.getContext());
                    if (e.hasClass("disliked-parameter-link")) {
                        hilight = "]";
                    }
                    if (e.hasClass("liked-parameter-link")) {
                        hilight = "[";
                    }
                    add_rating.setWidgetInfo("rating", el + hilight, "", "");
                    l_ratings.addView(add_rating);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean inArray(List<String> list, String search) {
        for (String str : list) {
            if (str.equals(search)) {
                return true;
            }
        }
        return false;
    }
}