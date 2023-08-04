package zlobniyslaine.ru.ficbook;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class ActivityOauth extends AppCompatActivity {


    WebView wv_oauth;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_oauth);
        wv_oauth = findViewById(R.id.wv_oauth);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WebSettings webSettings = wv_oauth.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        wv_oauth.setWebChromeClient(new WebChromeClient());

        wv_oauth.setBackgroundColor(Color.TRANSPARENT);
        wv_oauth.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        wv_oauth.setVerticalScrollBarEnabled(false);
        wv_oauth.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        wv_oauth.setWebViewClient(new android.webkit.WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(wv_oauth, url);
                Log.i("OPF", url);
                if ((url.contains("https://ficbook.net/")) && (!url.contains("social"))) {
                    try {
                        String cookies = CookieManager.getInstance().getCookie(url);
                        Log.i("OPF", "All the cookies in a string:" + cookies);

                        SharedPreferences.Editor ed = Application.sPref.edit();
                        ed.putString("PHPSESSID", Application.getCookie("PHPSESSID"));
                        ed.putString("remme", Application.getCookie("remme"));
                        ed.putString("cfduid", Application.getCookie("__cfduid"));
                        ed.putString("login", "VK");
                        ed.putString("password", "vk_oauth");
                        ed.putString("user_name", "VK");
                        ed.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }
        });

        wv_oauth.loadUrl(Application.getVKAuth());
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
