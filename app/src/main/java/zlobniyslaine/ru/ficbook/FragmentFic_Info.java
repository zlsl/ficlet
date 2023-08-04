package zlobniyslaine.ru.ficbook;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zlobniyslaine.ru.ficbook.controls.FicWebView;
import zlobniyslaine.ru.ficbook.controls.ViewBadgeWidget;


public class FragmentFic_Info extends Fragment {

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fic_info, container, false);

        FlowLayout l_fandoms = rootView.findViewById(R.id.l_fandoms);
        FlowLayout l_genres = rootView.findViewById(R.id.l_genres);
        FlowLayout l_characters = rootView.findViewById(R.id.l_characters);
        FlowLayout l_pairings = rootView.findViewById(R.id.l_pairings);
        FlowLayout l_cautions = rootView.findViewById(R.id.l_cautions);
        FlowLayout l_tags = rootView.findViewById(R.id.l_tags);
        WebView wv_description = rootView.findViewById(R.id.wv_description);

        if (getArguments() != null && getArguments().containsKey("description")) {
            String in = getArguments().getString("size") +
                    "<b>Рейтинг:</b> " + getArguments().getString("rating") +
                    "<br><b>Статус: </b>" + getArguments().getString("status");
            if (getArguments().containsKey("publication")) {
                in = in + "<br>" + getArguments().getString("publication");
            }
            in = in + "<br>" + getArguments().getString("description");
            if (getArguments().containsKey("belong")) {
                in = in + "<br>" + getArguments().getString("belong");
            }
            if (getArguments().containsKey("request")) {
                in = in + "<br>" + getArguments().getString("request");
            }
            if (getArguments().containsKey("author_comment")) {
                in = in + "<br>" + getArguments().getString("author_comment");
            }

//            in = in.replace("/uploads.ru/", "/s0.uploads.ru/i/");
            String text = in + "<br><br>ID: " + getArguments().getString("id");

            String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(in);
            while (m.find()) {
                if ((m.group().contains("jpg")) || (m.group().contains("png"))) {
                    text = text.replace(m.group(), "<img style='width:100%;' src='" + m.group() + "'>" + m.group() + " ");
                } else {
                    text = text.replace(m.group(), "<a href='" + m.group() + "'>" + m.group() + "</a>");
                }
            }

            text = "<!DOCTYPE HTML><style>* {text-align: justify;} a {background: #999; color: #FFF; text-decoration: none; padding: 2px; border-radius: 3px;font-size: 16px;}</style>" + text;
            wv_description.getSettings().setJavaScriptEnabled(true);
            wv_description.setWebViewClient(new FicWebView());
            wv_description.setWebChromeClient(new WebChromeClient());
            wv_description.addJavascriptInterface(this, "FicInfo");
            wv_description.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Application.openUrl(url, getContext());
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d("ficinfo", "done");
                    //wv_description.loadUrl("javascript:FicInfo.resize(document.body.getBoundingClientRect().height)");
                    super.onPageFinished(view, url);
                }
            });
            wv_description.loadDataWithBaseURL("http://ficlet.app", text, "text/html", "UTF-8", "");
        }

        if (getArguments() != null && getArguments().containsKey("fandoms")) {
            ArrayList<HashMap<String, Object>> ficFandoms = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("fandoms");
            if (ficFandoms != null) {
                for (HashMap<String, Object> v : ficFandoms) {
                    ViewBadgeWidget add_fandom = new ViewBadgeWidget(this.getContext());
                    add_fandom.setWidgetInfo("fandom", Objects.requireNonNull(v.get("title")).toString(), "https://ficbook.net" + Objects.requireNonNull(v.get("url")).toString(), "");
                    l_fandoms.addView(add_fandom);
                }
            }
        }

        if (getArguments() != null && getArguments().containsKey("genres")) {
            ArrayList<HashMap<String, Object>> ficGenres = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("genres");
            if (ficGenres != null) {
                for (HashMap<String, Object> v : ficGenres) {
                    ViewBadgeWidget add_genre = new ViewBadgeWidget(this.getContext());
                    add_genre.setWidgetInfo("genre", Objects.requireNonNull(v.get("title")).toString(), "https://ficbook.net/tags/" + Objects.requireNonNull(v.get("url")).toString(), "");
                    l_genres.addView(add_genre);
                }
            }
        }

        if (getArguments() != null && getArguments().containsKey("cautions")) {
            ArrayList<HashMap<String, Object>> ficCautions = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("cautions");
            if (ficCautions != null) {
                for (HashMap<String, Object> v : ficCautions) {
                    ViewBadgeWidget add_caution = new ViewBadgeWidget(this.getContext());
                    add_caution.setWidgetInfo("caution", Objects.requireNonNull(v.get("title")).toString(), "https://ficbook.net/tags/" + Objects.requireNonNull(v.get("url")).toString(), Objects.requireNonNull(v.get("hint")).toString().replaceAll("<.*?>", ""));
                    l_cautions.addView(add_caution);
                }
            }
        }

        if (getArguments() != null && getArguments().containsKey("tags")) {
            ArrayList<HashMap<String, Object>> ficTags = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("tags");
            if (ficTags != null) {
                for (HashMap<String, Object> v : ficTags) {
                    ViewBadgeWidget add_tag = new ViewBadgeWidget(this.getContext());
                    add_tag.setWidgetInfo("tag", Objects.requireNonNull(v.get("title")).toString(), "https://ficbook.net/tags/" + Objects.requireNonNull(v.get("url")).toString(), ""/*v.get("hint").toString().replaceAll("<.*?>", "")*/);
                    l_tags.addView(add_tag);
                }
            }
        }

        if (getArguments() != null && getArguments().containsKey("characters")) {
            ArrayList<HashMap<String, Object>> ficCharacters = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("characters");
            if (ficCharacters != null) {
                for (HashMap<String, Object> v : ficCharacters) {
                    ViewBadgeWidget add_character = new ViewBadgeWidget(this.getContext());
                    add_character.setWidgetInfo("character", Objects.requireNonNull(v.get("title")).toString(), "https://ficbook.net/pairings/" + Objects.requireNonNull(v.get("url")).toString(), "");
                    l_characters.addView(add_character);
                }
            }
        }

        if (getArguments() != null && getArguments().containsKey("pairings")) {
            ArrayList<HashMap<String, Object>> ficPairings = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("pairings");
            if (ficPairings != null) {
                for (HashMap<String, Object> v : ficPairings) {
                    ViewBadgeWidget add_pairing = new ViewBadgeWidget(this.getContext());
                    add_pairing.setWidgetInfo("pairing", Objects.requireNonNull(v.get("title")).toString(), "https://ficbook.net/pairings/" + Objects.requireNonNull(v.get("url")).toString().replace("/", "---"), "");
                    l_pairings.addView(add_pairing);
                }
            }
        }

        return rootView;
    }
}