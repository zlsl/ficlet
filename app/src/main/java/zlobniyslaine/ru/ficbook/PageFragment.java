package zlobniyslaine.ru.ficbook;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class PageFragment extends Fragment {
    private final static String PAGE_TEXT = "PAGE_TEXT";
    private static Boolean night_mode = false;
    private static AppCompatTextView pageView;

    public static PageFragment newInstance(CharSequence pageText, Boolean mode) {
        PageFragment frag = new PageFragment();
        Bundle args = new Bundle();
        args.putCharSequence(PAGE_TEXT, pageText);
        frag.setArguments(args);
        night_mode = mode;
        return frag;
    }

    public static void SetText(String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            pageView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        } else {
            pageView.setText(Html.fromHtml(text));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pageView = (AppCompatTextView) inflater.inflate(R.layout.page, container, false);
        int font_size = Application.sPref.getInt("font_size", 15);
        pageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size);

        switch (Application.sPref.getInt("typeface_id", 0)) {
            case 1:
                pageView.setTypeface(Typeface.SERIF);
                break;
            case 0:
            default:
                pageView.setTypeface(Typeface.SANS_SERIF);
                break;
        }

        if (getArguments() != null) {
            CharSequence text = getArguments().getCharSequence(PAGE_TEXT);
            if (text == null) {
                return pageView;
            }

            text = text.toString().replaceAll("\n", "<br>").replace("{", "&nbsp;");
            if (Application.sPref.getBoolean("yo_fix", false)) {
                SetText(YO.fix(text.toString()));
            } else {
                SetText(text.toString());
            }

            if (Application.sPref.getBoolean("open_links", false)) {
                if (text.toString().contains("http")) {
                    BetterLinkMovementMethod method = BetterLinkMovementMethod.linkify(Linkify.WEB_URLS, pageView).setOnLinkClickListener((textView, url) -> {
                        Application.openUrl(url, getContext());
                        return true;
                    });

                    pageView.setMovementMethod(method);
                }
            }

            try {

                if (night_mode) {
                    pageView.setTextColor(getResources().getColor(R.color.text_night_color));
                } else {
                    pageView.setTextColor(getResources().getColor(R.color.text_day_color));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return pageView;
    }

}

