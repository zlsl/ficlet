package zlobniyslaine.ru.ficbook.controls;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FicWebView extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}