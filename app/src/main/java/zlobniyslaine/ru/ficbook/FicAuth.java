package zlobniyslaine.ru.ficbook;

import static zlobniyslaine.ru.ficbook.Application.getContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.controls.LocalWebView;

class FicAuth {

    private OnAuthListener onAuthListener;
    private OnCaptchaListener onCaptchaListener;
    private OnTechListener onTechListener;

    private AlertDialog oauthDialog;

    public interface OnAuthListener {
        void onAuthListener(Boolean success);
    }

    void setOnAuthListener(OnAuthListener listener) {
        onAuthListener = listener;
    }

    public interface OnCaptchaListener {
        void onCaptchaListener();
    }

    void setOnCaptchaListener(OnCaptchaListener listener) {
        onCaptchaListener = listener;
    }

    public interface OnTechListener {
        void onTechListener();
    }

    void setOnTechListener(OnTechListener listener) {
        onTechListener = listener;
    }


    void StartAuth() {
        new auth_ficbook(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class auth_ficbook extends AsyncTask<String, Void, Void> {
        private final WeakReference<FicAuth> ref;

        auth_ficbook(FicAuth context) {
            ref = new WeakReference<>(context);
        }

        Response response;
        ResponseBody body;
        String bodyx = "";
        Boolean auth = false;

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (Application.vk_login) {
                    return null;
                }

                if (Application.A_login.isEmpty() || Application.A_password.isEmpty()) {
                    return null;
                }

                Application.cookieJar.clear();

                OkHttpClient client = new OkHttpClient.Builder()
                        .cookieJar(Application.cookieJar)
                        .build();

                client.followRedirects();

                RequestBody formBody = new FormBody.Builder()
                        .add("login", Application.A_login)
                        .add("password", Application.A_password)
                        .add("remember", "on")
                        .build();

                response = client.newCall(Application.getRequestBuilder("https://ficbook.net/login_check", formBody)).execute();

                body = response.body();
                if (body != null) {
                    bodyx = body.string();
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
//            Application.logLargeString(bodyx);
            try {
                if (bodyx.contains("captcha-bypass")) {
                    if (ref.get().onCaptchaListener != null) {
                        ref.get().onCaptchaListener.onCaptchaListener();
                    }
                } else {
                    if (bodyx.contains("<title>Технические работы")) {
                        if (ref.get().onTechListener != null) {
                            ref.get().onTechListener.onTechListener();
                        }
                    } else {
                        if (Application.cookieJar != null) {
                            List<Cookie> c = Application.cookieJar.loadForRequest(Objects.requireNonNull(HttpUrl.parse("https://ficbook.net/register")));
//                            Application.logLargeString(Application.cookieJar.toString());
                            for (Cookie temp : c) {
                                if (temp.name().contains("__cfduid")) {
                                    Application.C_cfduid = temp.value();
                                }
                                if (temp.name().contains("PHPS")) {
                                    Application.C_phpsessid = temp.value();
                                }
                                if (temp.name().contains("remme2")) {
                                    Application.C_remme2 = temp.value();
                                }
                                if (temp.name().contains("remme")) {
                                    Application.C_remme = temp.value();
                                    auth = true;
                                }
                            }
                        }

                        if (auth) {
                            SharedPreferences.Editor ed = Application.sPref.edit();
                            try {
                                ed.putString("PHPSESSID", Application.C_phpsessid);
                                ed.putString("remme", Application.C_remme);
                                ed.putString("remme2", Application.C_remme2);
                                ed.putString("cfduid", Application.C_cfduid);
                                ed.putString("login", Application.A_login);
                                ed.putString("password", Application.A_password);
                                ed.putString("user_name", Application.user_name);
                                ed.putBoolean("vk_login", Application.vk_login);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ed.apply();

                            Application.setGuest(false);
//                            Application.setUser_name(Application.user_name);
                            Log.i("AUTH", "SUCCESS");
                            Application.fetchDownloadToken();
                            Application.checkNotifications();
                        } else {
                            SharedPreferences.Editor ed = Application.sPref.edit();
                            ed.putString("PHPSESSID", Application.C_phpsessid);
                            ed.putString("remme", "");
                            ed.putString("remme2", "");
                            ed.putString("cfduid", "");
                            Application.setUser_name("");
                            Application.user_avatar = "";
                            Application.social_login = "";
                            ed.putString("user_avatar", "");
                            ed.putString("social_login", "");
                            ed.putBoolean("vk_login", false);
                            ed.apply();
                            Application.setGuest(true);
                            Log.i("AUTH", "ERROR");
                        }

                        if (ref.get().onAuthListener != null) {
                            ref.get().onAuthListener.onAuthListener(auth);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    void logoff() {
        CookieManager.getInstance().removeAllCookies(null);
        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putString("PHPSESSID", Application.C_phpsessid);
        ed.putString("remme", "");
        ed.putString("remme2", "");
        ed.putString("cfduid", "");
        Application.A_login = "";
        Application.A_password = "";
        ed.putString("PHPSESSID", "");
        ed.putString("login", "");
        ed.putString("password", "");
        ed.putString("user_name", "");
        Application.setUser_name("");
        Application.user_avatar = "";
        Application.social_login = "";
        ed.putString("user_avatar", "");
        ed.putString("social_login", "");
        ed.putBoolean("vk_login", false);
        ed.apply();
        Application.setGuest(true);
        if (onAuthListener != null) {
            onAuthListener.onAuthListener(false);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    void ShowOauthDialog(Context context) {
        Handler mainHandler = new Handler(getContext().getMainLooper());

        Runnable myRunnable = new Runnable() {
            @SuppressLint("InflateParams")
            @Override
            public void run() {
                try {
                    AlertDialog.Builder oauthDialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    View alertDialogView;
                    if (inflater != null) {
                        alertDialogView = inflater.inflate(R.layout.dialog_oauth, null);
                    } else {
                        return;
                    }
                    oauthDialogBuilder.setView(alertDialogView);

                    final LocalWebView wv_oauth = alertDialogView.findViewById(R.id.wv_oauth);
                    final ProgressBar pb1 = alertDialogView.findViewById(R.id.pb1);

                    oauthDialogBuilder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

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
                            if (url.startsWith("https://ficbook.net/social_connect")) {
                                try {
                                    String cookies = CookieManager.getInstance().getCookie(url);
                                    Log.i("OPF", "All the cookies in a string:" + cookies);

                                    Application.C_phpsessid = Application.getCookie("PHPSESSID");
                                    Application.C_remme = Application.getCookie("remme");
                                    Application.C_remme2 = Application.getCookie("remme2");
                                    Application.C_cfduid = Application.getCookie("__cfduid");
//                                    Application.A_login = "VK";
                                    Application.A_password = "vk_oauth_ok";
                                    Application.vk_login = true;

                                    SharedPreferences.Editor ed = Application.sPref.edit();
                                    ed.putString("PHPSESSID", Application.C_phpsessid);
                                    ed.putString("remme", Application.C_remme);
                                    ed.putString("remme", Application.C_remme2);
                                    ed.putString("cfduid", Application.C_cfduid);
                                    ed.putString("login", Application.A_login);
                                    ed.putString("password", Application.A_password);
//                                    ed.putString("user_name", "VK");
                                    ed.putString("social_login", Application.social_login);
                                    ed.putBoolean("vk_login", true);
                                    ed.apply();

                                    Application.setGuest(false);

                                    if (onAuthListener != null) {
                                        onAuthListener.onAuthListener(false);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    if (oauthDialog != null) {
                                        if (oauthDialog.getOwnerActivity().isDestroyed()) {
                                            return;
                                        }
                                        if (oauthDialog.getOwnerActivity().isFinishing()) {
                                            return;
                                        }
                                        if (oauthDialog.isShowing()) {
                                            oauthDialog.cancel();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                pb1.setVisibility(View.GONE);
                            }
                        }
                    });

                    oauthDialog = oauthDialogBuilder.create();
                    oauthDialog.show();

                    wv_oauth.loadUrl(Application.getVKAuth());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mainHandler.post(myRunnable);
    }

/*
    @SuppressLint("SetJavaScriptEnabled")
    void ShowCaptchaDialog(Context context) {
        try {
            AlertDialog.Builder captchaDialogBuilder = new AlertDialog.Builder(context);
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View alertDialogView = inflater.inflate(R.layout.dialog_captcha, null);
            captchaDialogBuilder.setView(alertDialogView);

            final LocalWebView wv_captcha = alertDialogView.findViewById(R.id.wv_captcha);

            captchaDialogBuilder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

            WebSettings webSettings = wv_captcha.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setUserAgentString(Application.USER_AGENT);
            wv_captcha.setWebChromeClient(new WebChromeClient());
            wv_captcha.setBackgroundColor(Color.TRANSPARENT);
            wv_captcha.setVerticalScrollBarEnabled(false);
            wv_captcha.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

            wv_captcha.setWebViewClient(new android.webkit.WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(wv_captcha, url);
                    Log.i("OPF", url); // _ym_uid=1520499540764508028; _ym_isad=2
                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.i("OPF", "All the cookies in a string:" + cookies);

                    if (!url.equals("https://ficbook.net/?c") && (url.equals("https://ficbook.net/") || url.contains("cdn-cgi"))) {
                        Log.i("CAPTCHA", "success");
                        Application.C_phpsessid = Application.getCookie("PHPSESSID");
                        Application.C_cfduid = Application.getCookie("__cfduid");
                        Application.C_cfclearance = Application.getCookie("cf_clearance");
                        SharedPreferences.Editor ed = Application.sPref.edit();
                        ed.putString("PHPSESSID", Application.C_phpsessid);
                        ed.putString("cfduid", Application.C_cfduid);
                        ed.putString("cf_clearance", Application.C_cfclearance);
                        ed.apply();

                        if (Application.vk_login) {
                            if (onAuthListener != null) {
                                onAuthListener.onAuthListener(true);
                            }
                        } else {
                            if (!Objects.requireNonNull(Application.sPref.getString("login", "")).isEmpty()) {
                                if (Application.isInternetAvailable()) {
                                    StartAuth();
                                }
                            }
                        }
                        captchaDialog.dismiss();
                    }
                }
            });

            captchaDialog = captchaDialogBuilder.create();
            captchaDialog.show();

            wv_captcha.loadUrl("https://ficbook.net/?c");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/


    public static void login(Context context) {
        try {
            AlertDialog.Builder loginDialog = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

            View alertDialogView = inflater.inflate(R.layout.login_dialog, null);
            loginDialog.setView(alertDialogView);

            final TextView login_name = alertDialogView.findViewById(R.id.login_name);
            final TextView login_password = alertDialogView.findViewById(R.id.login_password);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                login_name.setAutofillHints(View.AUTOFILL_HINT_USERNAME);
                login_password.setAutofillHints(View.AUTOFILL_HINT_PASSWORD);
            }

            login_name.setText(Application.A_login);
            login_password.setText(Application.A_password);

            loginDialog.setPositiveButton("Ok", (dialog, which) -> {
                Application.A_login = login_name.getText().toString();
                Application.A_password = login_password.getText().toString();
                Application.vk_login = false;
                Application.ficAuth.StartAuth();
                dialog.dismiss();
            });
            loginDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
