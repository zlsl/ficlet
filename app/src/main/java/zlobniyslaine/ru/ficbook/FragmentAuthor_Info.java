package zlobniyslaine.ru.ficbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FragmentAuthor_Info extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_author_info, container, false);

        WebView wv_info = rootView.findViewById(R.id.wv_info);

        if (getArguments() != null) {

            String text = "";
            String in = getArguments().getString("about");
            String id = getArguments().getString("id");
            if (in != null) {
                text = in;
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
                text = text + "<br><br>ID: " + id;
            }
            text = "<style>* {text-align: justify;} .alert-danger{background: #f2dede; color: #a94442; width: 100%;} .alert-success{background: #dff0d8; color: #3c763d; width: 100%;};a {background: #999; color: #FFF; text-decoration: none; padding: 2px; border-radius: 3px;font-size: 16px;}</style>" + text;
            wv_info.setWebChromeClient(new WebChromeClient());
            wv_info.loadDataWithBaseURL("http://ficlet.app", text, "text/html", "UTF-8", "");
            wv_info.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Application.openUrl(url, getContext());
                    return true;
                }
            });
        }

        return rootView;
    }
}