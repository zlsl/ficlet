package zlobniyslaine.ru.ficbook;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.util.SQLiteUtils;
import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import zlobniyslaine.ru.ficbook.models.Authors;
import zlobniyslaine.ru.ficbook.models.Category;
import zlobniyslaine.ru.ficbook.models.Fandoms;
import zlobniyslaine.ru.ficbook.models.Fanfic;
import zlobniyslaine.ru.ficbook.models.Feeds;
import zlobniyslaine.ru.ficbook.models.Tags;
import zlobniyslaine.ru.ficbook.models.TagsCategory;

@SuppressLint("ObsoleteSdkInt")
public class Application extends com.activeandroid.app.Application {

    public static final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(100000, TimeUnit.MILLISECONDS)
            .readTimeout(100000, TimeUnit.MILLISECONDS)
            .writeTimeout(100000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    requestBuilder.addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                    requestBuilder.addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                    requestBuilder.addHeader("cache-control", "max-age=0");
                    requestBuilder.addHeader("upgrade-insecure-requests", "1");

                    Request request = requestBuilder.build();

                    long t1 = 0;
                    if (Application.debug) {
                        t1 = System.nanoTime();
                        Log.i("OKHTTP", String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));

                        RequestBody requestBody = request.body();
                        if (requestBody != null) {
                            try {
                                String subtype = Objects.requireNonNull(requestBody.contentType()).subtype();
                                if (subtype.contains("form")) {
                                    String form = "";
                                    try {
                                        final Buffer buffer = new Buffer();
                                        requestBody.writeTo(buffer);
                                        form = buffer.readUtf8();
                                    } catch (final IOException e) {
                                        e.printStackTrace();
                                    }

                                    Log.i("OKHTTP", String.format("FORM %s", form));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Response response = chain.proceed(request);
                    if (Application.debug) {
                        long t2 = System.nanoTime();
                        Log.i("OKHTTP", String.format("%s Received response for %s in %.1fms%n%s", response.code(), response.request().url(), (t2 - t1) / 1e6d, response.headers()));
                    }

                    needCaptcha = false;
                    error503 = false;
                    if (response.code() == 403) {
                        needCaptcha = true;
                    }
                    if (response.code() == 503) {
                        error503 = true;
                    }

                    return response;
                }
            })
            .build();

    public static String SIG = "%";

    public static final String FICLET_HOST = "https://localhost/"; 

    public static final boolean debug = false;
    public static String download_token = "";
    public static String chat_last_id = "0";
    public static String installerPackage = "";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String UPDATE_URL_XML = "";
    public static final String UPDATE_URL_SQL = "";

    public static SharedPreferences sPref;
    private static Typeface iconFont;
    private static Application instance;
    private static StringBuilder sbMessage = new StringBuilder();
    private static String vk_auth_url = "https://oauth.vk.com/authorize?scope=email&state=IgMCiaL1GMIuIUsC4DkqcZLtlgZ3etwc&response_type=code&approval_prompt=auto&client_id=5739535&redirect_uri=https%3A%2F%2Fficbook.net%2Fsocial_connect%3FsocialType%3D2";

    public static String device_id = "";
    public static boolean device_ok = false;
    public static boolean host_ficlet = false;
    public static boolean host_ficbook = false;

    public static ClearableCookieJar cookieJar;
    public static String C_phpsessid;
    public static String C_cfduid;
    public static String C_cfclearance;
    public static String C_remme;
    public static String C_remme2;
    public static Boolean vk_login = false;

    public static String A_login;
    public static String A_password;
    public static String user_id = "";
    public static String user_name = "";
    public static String user_avatar = "";
    public static String social_login = "";

    public static final FicAuth ficAuth = new FicAuth();

    private static Boolean guest = true;
    private static Boolean needCaptcha = false;
    private static Boolean error503 = false;

    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3346.8 Safari/537.36 Ficlet/" + BuildConfig.VERSION_NAME;
    public static final String USER_AGENT2 = "AppleWebKit/605." + BuildConfig.VERSION_NAME;
    public static final String USER_AGENT_APP = get_s_mdl() + " Ficlet/" + BuildConfig.VERSION_NAME;

    public static NotificationManager notificationManager;
    public static final String CHANNEL_ID = "zlobniyslaine.ru.ficbook.NOTIFY";
    public static final String CHANNEL_UPDATE_ID = "zlobniyslaine.ru.ficbook.UPDATE";
    public static final String CHANNEL_FETCH_ID = "zlobniyslaine.ru.ficbook.FETCH";
    public static final int NOTIFICATION_FAVOURITES = 10001;
    public static final int NOTIFICATION_PARTICIPATED = 10002;
    public static final int NOTIFICATION_DIALOG = 10003;
    public static final int NOTIFICATION_NEW_PART = 10004;
    public static final int NOTIFICATION_FANFIC_BY_INT = 10005;
    public static final int NOTIFICATION_NEW_VERSION = 10100;
    public static final int NOTIFICATION_NEWS = 10200;

    public static Context getContext() {
        return instance;
    }

    public static TextToSpeech tts;
    private static String ttsEngine = "com.google.android.tts";

    public static String getUser_name() {
        return user_name;
    }

    public static void setUser_name(String user_name) {
        Application.user_name = user_name;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        if (apsi(this)) {
            SIG = ",";
        }

        try {
            installerPackage = getInstallerPackageName(this);
            if (installerPackage == null) {
                installerPackage = "sideload";
            }
            Log.i("I", installerPackage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        httpclient.followRedirects();
        httpclient.retryOnConnectionFailure();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final NotificationManager mNotific = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                int imp = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Уведомления", imp);
                mChannel.setDescription("notifications");
                mChannel.setLightColor(Color.YELLOW);
                mChannel.canShowBadge();
                mChannel.setShowBadge(true);
                mChannel.setSound(null, null);
                if (mNotific != null) {
                    mNotific.createNotificationChannel(mChannel);
                }

                imp = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel2 = new NotificationChannel(CHANNEL_UPDATE_ID, "Обновления", imp);
                mChannel2.setDescription("update");
                mChannel2.setLightColor(Color.YELLOW);
                mChannel2.canShowBadge();
                mChannel2.setShowBadge(true);
                mChannel2.setSound(null, null);
                if (mNotific != null) {
                    mNotific.createNotificationChannel(mChannel2);
                }

                NotificationChannel mChannel3 = new NotificationChannel(CHANNEL_FETCH_ID, "Загрузки", imp);
                mChannel3.setDescription("update");
                mChannel3.setLightColor(Color.YELLOW);
                mChannel3.canShowBadge();
                mChannel3.setShowBadge(true);
                mChannel3.setSound(null, null);
                if (mNotific != null) {
                    mNotific.createNotificationChannel(mChannel3);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        iconFont = Typeface.createFromAsset(getAssets(), "glyphs.otf");

        ActiveAndroid.initialize(this);
        Category.Create();

        cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));

        sPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        C_phpsessid = sPref.getString("PHPSESSID", "");
        C_remme = sPref.getString("remme", "");
        C_remme2 = sPref.getString("remme", "");
        C_cfclearance = sPref.getString("cf_clearance", "");
        C_cfduid = sPref.getString("cfduid", "");
        ttsEngine = sPref.getString("voice_engine", "com.google.android.tts");
        device_id = sPref.getString("device_id", "");
        device_ok = sPref.getBoolean("device_ok", false);
        user_id = sPref.getString("user_id", "");
        user_name = sPref.getString("user_name", "");
        user_avatar = sPref.getString("user_avatar", "");

        if (device_id.isEmpty()) {
            device_id = UUID.randomUUID().toString();
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.putString("device_id", device_id);
            ed.apply();
        }

        Log.i("ID", device_id);

        try {
            Picasso picasso = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(this, 25000000))
                    .memoryCache(new LruCache(this))
                    .build();
            Picasso.setSingletonInstance(picasso);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("FICLET", BuildConfig.VERSION_NAME);
//        copyDB();
    }

    public static void StartWorkNotificationCheck() {
        Log.i("WORK", "Notification check worker init");
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(false)
                    .build();

            PeriodicWorkRequest requestUpdateNotifications = new PeriodicWorkRequest.Builder(FicletWorker.class, 15, TimeUnit.MINUTES, 10, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(getContext()).enqueueUniquePeriodicWork("ficlet-notification-check", ExistingPeriodicWorkPolicy.KEEP, requestUpdateNotifications);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Request getRequestBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT2)
                .addHeader("Cookie", getCookies())
                .addHeader("referer", Base64.encodeToString(url.getBytes(), Base64.NO_WRAP))
                .addHeader("origin", "https://ficbook.net")
                .build();
    }

    public static Request getRequestBuilder(String url, RequestBody formBody) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT2)
                .addHeader("Cookie", getCookies())
                .addHeader("referer", Base64.encodeToString(url.getBytes(), Base64.NO_WRAP))
                .addHeader("origin", "https://ficbook.net")
                .post(formBody)
                .build();
    }

    public static Request getJSONRequestBuilder(String url, RequestBody formBody) {
        RequestBody body = RequestBody.create("", JSON);

        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT2)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Cookie", getCookies())
                .addHeader("referer", Base64.encodeToString(url.getBytes(), Base64.NO_WRAP))
                .addHeader("origin", "https://ficbook.net")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request getJSONRequestBuilder(String url) {
        Log.e("gjrb1", url);
        Map<String, String> params = new HashMap<>();
        JSONObject parameter = new JSONObject(params);
        RequestBody body = RequestBody.create(parameter.toString(), JSON);

        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT2)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Cookie", getCookies())
                .addHeader("referer", Base64.encodeToString(url.getBytes(), Base64.NO_WRAP))
                .addHeader("origin", "https://ficbook.net")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static Request getJSONRequestBuilderB(String url, JSONObject j) {
        Log.e("gjrb2", url);
        RequestBody body = RequestBody.create(j.toString(), JSON);

        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT2)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("Cookie", getCookies())
                .addHeader("referer", Base64.encodeToString(url.getBytes(), Base64.NO_WRAP))
                .addHeader("origin", "https://ficbook.net")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }


    public static void displayPopup(String msg) {
        try {
            if (msg == null) {
                return;
            }
            if (sbMessage.length() == 0) {
                sbMessage.append(msg);
            } else {
                if (!sbMessage.toString().contains(msg)) {
                    sbMessage.append("\n").append(msg);
                }
            }
            Log.i("POPUP", msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void firePopup() {
        try {
            if (sbMessage.length() > 0) {
                try {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(getContext(), sbMessage.toString(), Toast.LENGTH_LONG).show();
                        sbMessage = new StringBuilder();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logLargeString(String str) {
        if (str == null) {
            return;
        }

        if (str.length() > 3000) {
            Log.d("LL", str.substring(0, 3000));
            logLargeString(str.substring(3000));
        } else {
            Log.d("LL", str);
        }
    }

    public static Typeface getIconFont() {
        return iconFont;
    }

    public static void checkNotifications() {
        new fetch_notifications().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class fetch_notifications extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());

                Response response = httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/notifications")).execute();
                Elements notifications;
                String body = "";
                ResponseBody bodyx = response.body();
                if (bodyx != null) {
                    body = bodyx.string();
                }
                response.close();

                if (!body.contains("notifications-sidebar-nav")) {
                    Log.w("NT", "no block");
                    return null;
                } else {
                    notifications = Jsoup.parse(body).select("ul.notifications-sidebar-nav li");
                }

                int i = 0;
                int n_count;
                int n_type_id;
                String n_text = "";
                String n_type;

                for (Element n : notifications) {
//                    n_text = n.select("a").attr("title");
                    try {
                        n_type = n.select("svg").attr("class");
                    } catch (Exception e) {
                        n_type = "";
                        e.printStackTrace();
                    }

                    n_count = Integer.parseInt(n.select("div.badge").text());

                    String tmp = sPref.getString("notify_" + n_type, "");

                    switch (n_type) {
                        case "icon-star-empty":
                        case "ic_star-empty icon":
                            n_type_id = Application.NOTIFICATION_FAVOURITES;
                            n_text = n_count + " обновлений в работах избранных авторов";
                            break;
                        case "icon-history":
                        case "ic_history":
                            n_type_id = Application.NOTIFICATION_PARTICIPATED;
                            break;
                        case "icon-envelop3":
                        case "ic_envelop3 icon":
                            n_type_id = Application.NOTIFICATION_DIALOG;
                            n_text = n_count + " новых сообщений";
                            break;
                        case "icon-stack-plus":
                        case "ic_stack-plus":
                        case "ic_stack-star icon":
                            n_type_id = Application.NOTIFICATION_NEW_PART;
                            n_text = n_count + " новых глав в сборниках";
                            break;
                        case "icon-lamp8":
                        case "ic_lamp8":
                            n_type_id = Application.NOTIFICATION_FANFIC_BY_INT;
                            break;
                        case "icon-newspaper":
                        case "ic_newspaper":
                            n_type_id = Application.NOTIFICATION_NEWS;
                            break;

                        default:
                            n_type_id = i;
                    }
                    Log.i("NT", tmp + " - " + n_type + " - " + n_text + " " + n_count);

                    if (!tmp.equals(n_text) && (n_type_id != Application.NOTIFICATION_NEWS)) {
                        Application.sendNotification("Ficlet", n_text, "", n_count, n_type_id);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putString("notify_" + n_type, n_text);
                        ed.apply();
                    }

                    i++;
                }
                Log.i("NT", "count: " + i);

                if (i == 0) {
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.remove("notify_icon-star-empty");
                    ed.remove("notify_icon-history");
                    ed.remove("notify_icon-envelop3");
                    ed.remove("notify_icon-stack-plus");
                    ed.remove("notify_icon-lamp8");
                    ed.remove("notify_icon-newspaper");
                    ed.remove("notify_ic_bubble-heart");
                    ed.remove("notify_ic_star-empty icon");
                    ed.remove("notify_ic_history");
                    ed.remove("notify_ic_envelop3 icon");
                    ed.remove("notify_ic_stack-plus");
                    ed.remove("notify_ic_stack-star icon");
                    ed.remove("notify_ic_lamp8");
                    ed.remove("notify_ic_newspaper");
                    ed.apply();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    public static void sendNotification(String Ticker, String Title, String Text, Integer Number, Integer id) {
        Intent notificationIntent;

        switch (id) {
            case NOTIFICATION_FAVOURITES:
                notificationIntent = new Intent(getContext(), ActivityFanficList.class);
                notificationIntent.putExtra("url", "https://ficbook.net/home/favourites?p=@&updatelist");
                notificationIntent.putExtra("title", "Лента избранных");
                notificationIntent.setAction(Intent.ACTION_DEFAULT);
                notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
                break;

            case NOTIFICATION_PARTICIPATED:
                notificationIntent = new Intent(getContext(), ActivityChanges.class);
                notificationIntent.putExtra("url", "https://ficbook.net/home/versions/participated?p=@");
                notificationIntent.putExtra("title", "Изменения");
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                break;

            case NOTIFICATION_DIALOG:
                notificationIntent = new Intent(getContext(), ActivityMessagingList.class);
                notificationIntent.setAction(Intent.EXTRA_TEXT);
                notificationIntent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                break;

            case NOTIFICATION_NEW_PART:
                notificationIntent = new Intent(getContext(), ActivityFanficList.class);
                notificationIntent.putExtra("url", "https://ficbook.net/home/collections?type=update&p=@");
                notificationIntent.putExtra("title", "Лента избранных");
                notificationIntent.setAction(Intent.ACTION_DEFAULT);
                notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
                break;

            case NOTIFICATION_FANFIC_BY_INT:
                notificationIntent = new Intent(getContext(), ActivityFicByRequestList.class);
                notificationIntent.putExtra("url", "https://ficbook.net/home/liked_requests?p=@");
                notificationIntent.putExtra("title", "Фанфики по интересным заявкам");
                notificationIntent.setAction(Intent.ACTION_DEFAULT);
                notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
                break;

            case NOTIFICATION_NEW_VERSION:
                final String appPackageName = getContext().getPackageName();
                notificationIntent = new Intent(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                break;

            default:
                notificationIntent = new Intent(getContext(), MainActivity.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(Ticker)
                .setContentTitle(Title)
                .setContentText(Text)
                .setNumber(Number)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        Notification notification;
        notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static Boolean getNeedCaptcha() {
        return needCaptcha;
    }

    public static Boolean getIs503() {
        return error503;
    }
    public static void clear503() {
        error503 = false;
    }

    public static String getCookies() {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        StringBuilder coo = new StringBuilder();
        C_phpsessid = sPref.getString("PHPSESSID", "");
        C_remme = sPref.getString("remme", "");
        C_remme2 = sPref.getString("remme2", "");
        C_cfduid = sPref.getString("cfduid", "");
        C_cfclearance = sPref.getString("cf_clearance", "11111");

        if (!C_phpsessid.isEmpty()) {
            coo.append("PHPSESSID=").append(C_phpsessid).append(";");
        }
        if (!C_remme.isEmpty()) {
            coo.append("remme=").append(C_remme).append(";");
        }
        if (!C_remme2.isEmpty()) {
            coo.append("remme2=").append(C_remme2).append(";");
        }
        if (!C_cfduid.isEmpty()) {
            coo.append("__cfduid=").append(C_cfduid).append(";");
        }
        if (C_cfclearance != null && !C_cfclearance.isEmpty()) {
            coo.append("cf_clearance=").append(C_cfclearance).append(";");
        }

        return coo.toString();
    }

    public static String getCookie(String CookieName) {
        String CookieValue = null;
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookies = cookieManager.getCookie("https://ficbook.net");
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(CookieName)) {
                    String[] temp1 = ar1.split("=");
                    CookieValue = temp1[1];
                    break;
                }
            }
        } catch (Exception e) {
            CookieValue = "";
            e.printStackTrace();
        }
        return CookieValue;
    }

    public static void setVKAuth(String url) {
        vk_auth_url = url;
    }

    public static String getVKAuth() {
        return vk_auth_url;
    }

    public static void openUrl(String url, Context ctx) {
        boolean parsed = false;

        Log.i("OPEN URL", url);

        if (url.contains("ficbook.net/readfic")) {
            String id = url.replace("https://ficbook.net/readfic/", "");
            Intent intent = new Intent(getContext(), ActivityFanfic.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id", id);
            intent.putExtra("title", "");
            getContext().startActivity(intent);
            parsed = true;
        }

        if (url.contains("ficbook.net/requests")) {
            String id = url.replace("https://ficbook.net/requests/", "");
            Intent intent = new Intent(getContext(), ActivityRequest.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id", id);
            intent.putExtra("title", "");
            getContext().startActivity(intent);
            parsed = true;
        }

        if (url.contains("ficlet.app/requests")) {
            String id = url.replace("http://ficlet.app/requests/", "");
            Intent intent = new Intent(getContext(), ActivityRequest.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id", id);
            intent.putExtra("title", "");
            getContext().startActivity(intent);
            parsed = true;
        }

        if (url.contains("ficbook.net/collections")) {
            Intent intent = new Intent(getContext(), ActivityFanficList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("url", url + "?p=@");
            intent.putExtra("title", "Сборник");
            getContext().startActivity(intent);
            parsed = true;
        }

        if (url.contains("ficbook.net/authors")) {
            Intent intent = new Intent(getContext(), ActivityAuthorProfile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("id", url.replace("https://ficbook.net/authors/", ""));
            intent.putExtra("url", url);
            getContext().startActivity(intent);
            parsed = true;
        }

        if (url.contains("jpg") || url.contains("png")) {
            ImagePopup imagePopup = new ImagePopup(ctx);
            imagePopup.setBackgroundColor(Color.BLACK);
            imagePopup.setHideCloseIcon(true);
            imagePopup.setImageOnClickClose(true);
            imagePopup.initiatePopupWithPicasso(url);
            imagePopup.viewPopup();
            parsed = true;
        }

        if (!parsed) {
            Log.w("OpenUrl", url);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        }
    }

    public static void setGuest(Boolean mode) {
        guest = mode;
    }

    public static Boolean isGuest() {
        return guest;
    }

// --Commented out by Inspection START (03.11.20 16:24):
//    private void copyDB() {
//        try {
//            Collections.Create();
//            Parts.Create();
//            FanficPage.Create();
//            Fanfic.Create();
//            Authors.Create();
//
//            final InputStream inputStream = new FileInputStream(getDatabasePath("ficbookz.db"));
//            final OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/ficbookz.db");
//
//            byte[] buffer = new byte[8192];
//            int length;
//
//            while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
//                output.write(buffer, 0, length);
//            }
//
//            output.flush();
//            output.close();
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
// --Commented out by Inspection STOP (03.11.20 16:24)

    public static void speakText(String text) {
        try {
            if (tts != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    //noinspection deprecation
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void speakText(String text, Boolean ut) {
        try {
            if (tts != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MessageId");
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
                    //noinspection deprecation
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setTTSEngine(String engine) {
        ttsEngine = engine;
        initTTS();
    }

    public static String getTTSEngine() {
        return ttsEngine;
    }

    public static void stopSpeak() {
        try {
            if (tts != null) {
                tts.stop();
                if (tts.isSpeaking()) {
                    Log.i("TTS", "Stop speak");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initTTS() {
        try {
            Log.i("TTS", "init with " + ttsEngine);
            tts = new TextToSpeech(Application.getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    Log.e("TTS DEF", tts.getDefaultEngine());
                    Log.e("TTS ENG", tts.getEngines().toString());

                    if (tts.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage())) == TextToSpeech.LANG_AVAILABLE) {
                        tts.setLanguage(new Locale(Locale.getDefault().getLanguage()));
                        Log.e("TTS", "Default locale");
                    } else {
                        tts.setLanguage(Locale.US);
                        Log.e("TTS", "US locale");
                    }

                    Locale locale = new Locale("ru-RU");

                    int result = tts.setLanguage(locale);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w("TTS", "Извините, этот язык не поддерживается");
                        Log.w("TTS", result + "!");
                    } else {
                        Log.i("TTS", "Русский :)");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            Log.i("TTS", "Max: " + TextToSpeech.getMaxSpeechInputLength());
                        }
                        tts.setSpeechRate((sPref.getFloat("speech_rate", 1f)));
                        tts.setPitch((sPref.getFloat("speech_pitch", 1f)));
                        if (!Objects.requireNonNull(sPref.getString("voice", "")).isEmpty()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (Application.tts.getVoices() != null) {
                                    try {
                                        for (Voice tmpVoice : Application.tts.getVoices()) {
                                            if (tmpVoice != null) {
                                                if (tmpVoice.getName().equals(sPref.getString("voice", ""))) {
                                                    Application.tts.setVoice(tmpVoice);
                                                    SharedPreferences.Editor ed = sPref.edit();
                                                    ed.putString("voice", tmpVoice.getName());
                                                    ed.apply();
                                                    Log.i("TTS", "Voice set: " + tmpVoice.getName());
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            try {
                                if (tts != null) {
                                    if (tts.getAvailableLanguages() != null) {
                                        Log.i("TTS", tts.getAvailableLanguages().toString());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    Log.e("TTS", "Ошибка!");
                }
            }, ttsEngine);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<TextToSpeech.EngineInfo> getTtsEngines() {
        return tts.getEngines();
    }

    public static String getDefaultTTSEngine() {
        try {
            return tts.getDefaultEngine();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isInternetAvailable() {
        boolean ok = true;
        try {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                ok = netInfo != null && netInfo.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok;
    }

    public static void saveCacheObject(String object_name, Object object) {
        try {
            FileOutputStream fos = Application.getContext().openFileOutput(object_name, Context.MODE_PRIVATE);
            GZIPOutputStream gz = new GZIPOutputStream(fos) {{
                def.setLevel(Deflater.BEST_COMPRESSION);
            }};
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            gz.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object loadCacheObject(String object_name) {
        File ff = new File(getContext().getFilesDir() + File.separator + object_name);
        if (!ff.exists()) {
            return null;
        }

        try {
            Object tmp;
            FileInputStream fis = Application.getContext().openFileInput(object_name);
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            tmp = ois.readObject();
            ois.close();
            return tmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getCacheObjectSize(String object_name) {
        File ff = new File(getContext().getFilesDir() + File.separator + object_name);
        if (!ff.exists()) {
            return -1;
        }

        try {
            String tmp;
            FileInputStream fis = Application.getContext().openFileInput(object_name);
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            tmp = (String) ois.readObject();
            ois.close();
            return tmp.length();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void adjustFontScale(Context ctx) {
        float font_scale = ctx.getResources().getConfiguration().fontScale;
        try {
            switch (sPref.getInt("font_scale", 2)) {
                case 0:
                    font_scale = 0.7f;
                    break;
                case 1:
                    font_scale = 0.8f;
                    break;
                case 2:
                    return;
                case 3:
                    font_scale = 1.1f;
                    break;
                case 4:
                    font_scale = 1.2f;
                    break;
            }

            ctx.getResources().getConfiguration().fontScale = font_scale;

            DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) ctx.getSystemService(WINDOW_SERVICE);
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(metrics);
                metrics.scaledDensity = ctx.getResources().getConfiguration().fontScale * metrics.density;
                ctx.getResources().updateConfiguration(ctx.getResources().getConfiguration(), metrics);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static NetworkAvailableListener mOnEventListener;

    public static void setNetworkAvailableListener(NetworkAvailableListener listener) {
        mOnEventListener = listener;
    }

    public interface NetworkAvailableListener {
        void onNetworkChange(Boolean available);
    }

    public static void doNetworkChange() {
        if (mOnEventListener != null)
            mOnEventListener.onNetworkChange(isInternetAvailable());
    }

    public static void updateBasesRemote() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_UPDATE_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Обновление баз")
                .setContentText("Подключение к серверу...")
                .setAutoCancel(true);
        SyncAuthorsFics();
    }

    public static void SyncAuthorsFics() {
        new Thread(
                () -> {
                    try {
                        if ( (Authors.getCount() == 0) && (Fanfic.getCount() == 0) ) {
                            return;
                        }

                        StringBuilder sb = new StringBuilder();
                        List<Authors> alist = Authors.getAll();
                        for (Authors a: alist) {
                            sb.append(a.nid).append("|").append(a.name).append("|").append(a.avatar_url).append("\n");
                        }

                        StringBuilder sb2 = new StringBuilder();
                        List<Fanfic> flist = Fanfic.getAll();
                        for (Fanfic f: flist) {
                            sb2.append(f.nid).append("|").append(f.parts).append("|").append(f.sup).append("\n");
                        }

                        OkHttpClient client = new OkHttpClient.Builder()
                                .cookieJar(Application.cookieJar)
                                .build();

                        client.followRedirects();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        ).start();
    }

    public static void ClearCache() {
        try {
            SharedPreferences.Editor editor = Application.sPref.edit();
            Map<String, ?> allEntries = Application.sPref.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                if (entry.getKey().contains("notify_")) {
                    editor.remove(entry.getKey());
                }
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileUtils.deleteQuietly(getContext().getFilesDir());
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static String getMainFeed() {
        if (Application.sPref.contains("main_feed")) {
            return sPref.getString("main_feed", "https://ficbook.net/popular");
        } else {
            if (!Application.C_remme.equals("")) {
                return "https://ficbook.net/home/collections?type=update&p=@";
            } else {
                return "https://ficbook.net/popular";
            }
        }
    }


    public static void uploadCache(RequestBody requestBody) {
    }

    public static void ClearCache2() {
        try {
            File[] files = Application.getContext().getFilesDir().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("fb2.zip2")) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    public static void syncCache() {
    }

    public static void refreshSync() {
    }

    public static void downloadList(ArrayList<HashMap<String, Object>> Fics, boolean quiet) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_FETCH_ID);
        if (!quiet) {
            Log.i("Download list", "size: " + Fics.size());
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Загрузка подборки фанфиков")
                    .setContentText("Подключение к серверу...")
                    .setAutoCancel(true);
        }
        new Thread(
                () -> {
                    final int FETCH_NOTIFICATION_ID = 2505;
                    int pos = 0;
                    int fb404_count = 0;
                    StringBuilder fb404 = new StringBuilder();

                    for (HashMap<String, Object> f : Fics) {
                        pos++;
                        try {
                            if (!quiet) {
                                Log.i("FIC", Objects.requireNonNull(f.get("id")) + " " + Objects.requireNonNull(f.get("title")));
                                builder.setChannelId(CHANNEL_FETCH_ID);
                                builder.setSound(null);
                                builder.setOnlyAlertOnce(true);
                                Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());

                                builder.setProgress(Fics.size(), pos, false);
                                builder.setContentTitle("Загрузка " + pos + " / " + Fics.size());
                                builder.setChannelId(CHANNEL_UPDATE_ID);
                                builder.setSound(null);
                                builder.setOnlyAlertOnce(true);

                                builder.setContentText(Objects.requireNonNull(f.get("title")).toString());
                                Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());
                            }

                            File test = new File(Application.getContext().getFilesDir() + "/" + f.get("id") + "_.fb2.zip");
                            if (!test.exists()) {
                                test = new File(Application.getContext().getFilesDir() + "/" + f.get("id") + "_.fb2.zip2");
                            }
                            if (!test.exists()) {
                                String data = "";
                                try {
                                    getToken(Objects.requireNonNull(f.get("id")).toString());
                                    RequestBody formBody = new FormBody.Builder()
                                            .add("fanfic_id", Objects.requireNonNull(f.get("id")).toString())
                                            .add("tokenn", download_token)
                                            .add(genToken(Objects.requireNonNull(f.get("id")).toString()).get(0),genToken(Objects.requireNonNull(f.get("id")).toString()).get(1))
                                            .build();
                                    String url = "https://ficbook,net/fanfic_download/".replace(SIG, ".") + Objects.requireNonNull(f.get("id")).toString()+ "/fb2";
//                                    Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/fanfic_download/fb2", formBody)).execute();
                                    Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                                    ResponseBody body = response.body();
                                    if (body != null) {
                                        data = body.string();
                                        body.close();
                                        response.close();
                                    }

                                    if (!response.isSuccessful()) {
                                        if (!quiet) {
                                            Log.e("FETCH", "Сетевая ошибка " + response.code() + " " + response.message() + " " + Objects.requireNonNull(f.get("id")));
                                        }
                                        if (response.code() == 404) {
                                            fb404_count++;
                                            fb404.append(Objects.requireNonNull(f.get("id"))).append("\n");
                                        }
                                    }

                                    data = Application.cleanFB2(data);

                                    FileWriter out = new FileWriter(new File(Application.getContext().getFilesDir(), f.get("id") + "_.fb2"));
                                    out.write(data);
                                    out.close();
                                    if (!quiet) {
                                        zip(Application.getContext().getFilesDir() + "/" + f.get("id") + "_.fb2", Application.getContext().getFilesDir() + "/" + f.get("id") + "_.fb2.zip");
                                    } else {
                                        zip(Application.getContext().getFilesDir() + "/" + f.get("id") + "_.fb2", Application.getContext().getFilesDir() + "/" + f.get("id") + "_.fb2.zip2");
                                    }
                                    if (!new File(Application.getContext().getFilesDir(), f.get("id") + "_.fb2").delete()) {
                                        Log.w("TMP", "delete failed");
                                    }
                                } catch (SocketTimeoutException e) {
                                    if (!quiet) {
                                        Application.displayPopup("Сервер не отвечает");
                                    }
                                } catch (UnknownHostException e) {
                                    if (!quiet) {
                                        Application.displayPopup("Проблемы с соединением");
                                    }
                                } catch (IOException e) {
                                    if (!quiet) {
                                        Application.displayPopup("Ошибка загрузки");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    Thread.sleep(1000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (fb404_count > 0) {
                        Log.w("PREFETCH", "404 = " + fb404_count);
                    }

                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Application.syncCache();
                        Log.d("SYNC", "after dlist");
                    }).start();

                    if (!quiet) {
                        try {
                            builder.setContentTitle("Загрузка завершена");
                            builder.setChannelId(CHANNEL_UPDATE_ID);
                            builder.setSound(null);
                            builder.setOnlyAlertOnce(true);
                            builder.setProgress(0, 0, false);
                            builder.setContentText("");
                            Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
    }

    public static void downloadFanfic(String id, String title, String format, String download_filename, boolean open) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_FETCH_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Загрузка фанфика")
                .setContentText("Подключение к серверу...")
                .setAutoCancel(true);
        new Thread(
                () -> {
                    final int FETCH_NOTIFICATION_ID = 2505;

                    Log.i("FIC", title);
                    try {
                        builder.setChannelId(CHANNEL_FETCH_ID);
                        builder.setSound(null);
                        builder.setOnlyAlertOnce(true);
                        Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());

                        builder.setProgress(1, 1, true);
                        builder.setContentTitle("Загрузка");
                        builder.setChannelId(CHANNEL_UPDATE_ID);
                        builder.setSound(null);
                        builder.setOnlyAlertOnce(true);

                        builder.setContentText(title);
                        Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());

                        String data = "";
                        try {
                            RequestBody formBody = new FormBody.Builder()
                                    .add("fanfic_id", id)
                                    .add("tokenn", download_token)
                                    .add(genToken(id).get(0),genToken(id).get(1))
                                    .build();

                            String url = "https://ficbook,net/fanfic_download/".replace(SIG, ".") + id + "/" + format;
                            //Response response = Application.httpclient.newCall(Application.getRequestBuilder(url, formBody)).execute();
                            Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();

                            if (!response.isSuccessful()) {
                                Log.e("FETCH", "Сетевая ошибка " + response.code() + " " + response.message());
                            }

                            if (format.equals("fb2")) {
                                ResponseBody body = response.body();
                                if (body != null) {
                                    data = body.string();
                                    body.close();
                                    response.close();
                                }
                                data = Application.cleanFB2(data);

                                FileWriter out = new FileWriter(new File(Application.getContext().getFilesDir(), id + "_." + format));
                                out.write(data);
                                out.close();

                                zip(Application.getContext().getFilesDir() + "/" + id + "_.fb2", Application.getContext().getFilesDir() + "/" + id + "_.fb2.zip");
                            } else {
                                File downloadedFile = new File(Application.getContext().getFilesDir(), id + "_." + format);
                                BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
                                sink.writeAll(Objects.requireNonNull(response.body()).source());
                                sink.close();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (format.equals("fb2")) {
                        new Thread(() -> {
                            Application.syncCache();
                            Log.d("SYNC", "pack");
                        }).start();
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Log.i("Q_SAVE", download_filename);
                        ContentResolver cr = getContext().getContentResolver();

                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.MediaColumns.DISPLAY_NAME, download_filename);
                        cv.put(MediaStore.MediaColumns.MIME_TYPE, "application/" + format);
                        cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                        if (uri != null) {
                            try {
                                InputStream inputStream = new FileInputStream(new File(Application.getContext().getFilesDir(), id + "_." + format));
                                OutputStream outputStream = cr.openOutputStream(uri);
                                copyFile(inputStream, outputStream);

                                if (open) {
                                    Intent i = new Intent( Intent.ACTION_VIEW );
                                    i.setDataAndType( uri, "application/" + format );
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getContext().startActivity(i);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.i("SAVE", download_filename);
                        try {
                            InputStream inputStream = new FileInputStream(new File(Application.getContext().getFilesDir(), id + "_." + format));
                            OutputStream outputStream = new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), download_filename));

                            copyFile(inputStream, outputStream);

                            if (open) {
                                Intent i = new Intent( Intent.ACTION_VIEW );
                                i.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), download_filename)) , "application/" + format );
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getContext().startActivity(i);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!new File(Application.getContext().getFilesDir(), id + "_." + format).delete()) {
                        Log.w("TMP", "delete failed");
                    }

                    try {
                        builder.setContentTitle("Загрузка завершена");
                        builder.setChannelId(CHANNEL_UPDATE_ID);
                        builder.setSound(null);
                        builder.setOnlyAlertOnce(true);
                        builder.setProgress(0, 0, false);
                        builder.setContentText(download_filename);
                        Application.notificationManager.notify(FETCH_NOTIFICATION_ID, builder.build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        ).start();
    }

    public static void zip(String file, String zipFileName) {
        try {
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data = new byte[32768];

            FileInputStream fi = new FileInputStream(file);
            BufferedInputStream origin = new BufferedInputStream(fi, 32768);

            ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, 32768)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchDownloadToken() {
        new fetch_download_token().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void getToken(String id) {
        Response response = null;
        try {
            response = httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/readfic/" + id + "/download")).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String body = "";
        ResponseBody bodyx;
        if (response != null) {
            bodyx = response.body();
        } else {
            return;
        }
        if (bodyx != null) {
            try {
                body = bodyx.string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        response.close();

        Elements tokens = Jsoup.parse(body).select("input[name=tokenn]");

        if (tokens.size() > 0) {
            download_token = tokens.get(0).attr("value");
            Log.i("download_token", download_token);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("download_token", download_token);
            ed.apply();
        } else {
            Log.e("download_token", "can't get");
            SharedPreferences.Editor ed = sPref.edit();
            ed.remove("download_token");
            ed.apply();
        }
    }

    private static class fetch_download_token extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            getToken("1");
            return null;
        }
    }

    public static void copyFile(InputStream input, OutputStream output) {
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                output.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static String cleanFB2(String data) {
        String tmp = data.replaceAll("(?s)<binary id=\"ficbook_logo.png\" content-type=\"image/png\">.*?</binary>", "");
        tmp = tmp.replace("<program-used>ficbook.net</program-used>", "");
        tmp = tmp.replaceAll("(?s)<stylesheet.*?</stylesheet>", "");
        tmp = tmp.replace("<image xlink:href=\"#ficbook_logo.png\"/>", "");

        return tmp;
    }

    static ArrayList<String> genToken(String id) {
        ArrayList<String> ret = new ArrayList<>();
        if ((id == null) || (download_token == null)) {
            ret.add("");
            ret.add("");
            return ret;
        }
        String rt = download_token.replaceAll("[\\D]+","");
        int n = 0;
        for (int i = 0; i < rt.length(); i++) { //n=[...this.elements.tokenn.value].map((t=>parseInt(t, 8))).filter((t=>!isNaN(t)&&t>0)).reduce(((t, e)=>t+e));
            int i1 = Integer.parseInt(String.valueOf(rt.charAt(i)));
            if (i1 < 8) {
                n = n + i1;
            }
        }

        int a = Integer.parseInt(id) + n; //name=String(fanfic_id+n);
        int b = Integer.parseInt(id) ^ n + 5; //value=String(fanfic_id^n+5)

        ret.add(Integer.toString(a));
        ret.add(Integer.toString(b));

        return ret;
    }

    public static void PingHost(Context context, String ip) {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            int timeoutMs = 2000;
            Socket sock = new Socket();
            SocketAddress sockaddr = new InetSocketAddress(ip, 443);

            sock.connect(sockaddr, timeoutMs);
            sock.close();
            Log.i("PING", ip + " ok");
            if (ip.equals("")) {
                host_ficlet = true;
            }
            if (ip.equals("ficbook.net")) {
                host_ficbook = true;
            }
        } catch (IOException ioException) {
            Log.i("PING", ip + " fail");
            Runnable myRunnable = () -> {
                try {
                    if (ip.equals("")) {
                        host_ficlet = false;
                        Toast.makeText(context, "Сервер обновлений недоступен!", Toast.LENGTH_SHORT).show();
                    }
                    if (ip.equals("ficbook.net")) {
                        host_ficbook = false;
                        Toast.makeText(context, "Сервер фикбука недоступен!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            try {
                new Handler(context.getMainLooper()).post(myRunnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String get_s_auth() {
        String auth = Application.user_name.replace("\r", "").replace("\n", "").trim() + "|" + Application.A_login;// + "|" + Application.A_password;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            byte[] data = auth.getBytes(StandardCharsets.UTF_8);
            auth = Base64.encodeToString(data, Base64.DEFAULT).replace("=", "");
            auth = auth.replace("\r", "").replace("\n", "");
        }
        return auth;
    }

    private static String get_s_mdl() {
        return Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT].getName();
    }

    public static Request getFicletRequest(String url, RequestBody formBody) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT_APP)
                .post(formBody)
                .build();
    }

    static Request getFicletRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT_APP)
                .build();
    }

    public static boolean apsi(Context context) {
        return true;
    }

    private static String getInstallerPackageName(Context ctx) {
        return "";
    }

    public static void AddDefaultFeeds() {
        try {
            Feeds.Create();

            Feeds newFeed = new Feeds();
            newFeed.title = "Обновления в сборниках";
            newFeed.url = "https://ficbook.net/home/collections?type=update&p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Новые части в работе";
            newFeed.url = "https://ficbook.net/notifications?type=19&p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Новые работы по понравившимся заявкам";
            newFeed.url = "https://ficbook.net/notifications?type=3&p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Изменения где я бета/гамма/соавтор";
            newFeed.url = "https://ficbook.net/notifications?type=8&p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Новые работы в сборниках";
            newFeed.url = "https://ficbook.net/notifications?type=18&p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Обновления в избранных авторов";
            newFeed.url = "https://ficbook.net/notifications?type=17&p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Лента избранных";
            newFeed.url = "https://ficbook.net/home/favourites?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Понравившиеся работы";
            newFeed.url = "https://ficbook.net/home/liked_fanfics?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "История подписок";
            newFeed.url = "https://ficbook.net/home/followList?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Последние прочитанные работы";
            newFeed.url = "https://ficbook.net/home/readedList?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Понравившиеся работы";
            newFeed.url = "https://ficbook.net/home/liked_fanfics?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Избранные авторы";
            newFeed.url = "https://ficbook.net/home/favourites?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Ждут критики";
            newFeed.url = "https://ficbook.net/premium_fanfiction?p=@";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное";
            newFeed.url = "https://ficbook.net/popular";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное в джен";
            newFeed.url = "https://ficbook.net/popular/gen";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное в гет";
            newFeed.url = "https://ficbook.net/popular/het";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное в статьях";
            newFeed.url = "https://ficbook.net/popular/article";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное в смешанном";
            newFeed.url = "https://ficbook.net/popular/mixed";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное в другом";
            newFeed.url = "https://ficbook.net/popular/other";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();

            newFeed = new Feeds();
            newFeed.title = "Популярное в другом";
            newFeed.url = "https://ficbook.net/popular/other";
            newFeed.auto = "0";
            newFeed.hash = "";
            newFeed.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void FetchFeed(String url) {
            ExecutorService executors = Executors.newFixedThreadPool(1);
            Runnable runnable = () -> {
                Document doc = null;
                try {
                    if (!url.isEmpty()) {
                        Log.i("PREFETCH", url);

                        Response response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            String bodyx;

                            if (body != null) {
                                bodyx = body.string();
                                response.close();

                                if (bodyx.contains("Не удалось найти ничего с указанными вами параметрами.\n")) {
                                    return;
                                }

                                doc = Jsoup.parse(bodyx);
                            } else {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    if (doc == null) {
                        return;
                    }

                    try {
                        Elements vid;
                        if (doc.html().contains("js-item-wrapper")) { //coll
                            vid = doc.select("div.js-item-wrapper");
                            if (vid == null) {
                                vid = doc.select("article.block");
                            }
                        } else {
                            vid = doc.select(".fanfic-thumb-block article.fanfic-inline");
                            if (vid.size() == 0) {
                                vid = doc.select("article.fanfic-inline");
                            }
                        }
                        if (vid == null) {
                            Log.e("FLIST", "no container");
                            return;
                        }

                        ActiveAndroid.beginTransaction();

                        Log.i("FLIST", vid.size() + " items");

                        for (Element v : vid) {
                            String direction = "";

                            if (v.select("div.direction-before-het").first() != null) {
                                direction = "het";
                            } else {
                                if (v.select("div.direction-before-gen").first() != null) {
                                    direction = "gen";
                                } else {
                                    if (v.select("div.direction-before-mixed").first() != null) {
                                        direction = "mixed";
                                    } else {
                                        if (v.select("div.direction-before-slash").first() != null) {
                                            direction = "slash";
                                        } else {
                                            if (v.select("div.direction-before-femslash").first() != null) {
                                                direction = "femslash";
                                            } else {
                                                if (v.select("div.direction-before-article").first() != null) {
                                                    direction = "article";
                                                } else {
                                                    if (v.select("div.direction-before-other").first() != null) {
                                                        direction = "other";
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            String title = v.select("h3").text().trim();
                            String id = v.select("h3.fanfic-inline-title a").attr("href").replace("/readfic/", "");
                            String sup = v.select("div.badge-like").text().trim();
                            String authors = v.select("span.author").text().trim();
                            String trophy;

                            if (id.contains("?")) {
                                String[] ids = id.split("\\?");
                                id = ids[0];
                            }

                            String new_parts;
                            if (!v.select("span.new-parts-in-title").isEmpty()) {
                                new_parts = v.select("span.new-parts-in-title").text();
                            } else {
                                new_parts = "";
                            }

                            if (!v.select("span.badge-reward").isEmpty()) {
                                trophy = v.select("span.badge-reward").text();
                            } else {
                                trophy = "";
                            }

                            Elements description = v.select("div.fanfic-description-text");
                            Elements infod = v.select("dl.fanfic-inline-info");

                            String date_changes = "";
                            String new_content = "";

                            Elements read_notify = v.select("div.read-notification");
                            if (!read_notify.isEmpty()) {
                                if (!read_notify.select("span.hidden-xs span").isEmpty()) {
                                    date_changes = read_notify.select("span.hidden-xs span").attr("title");
                                    if (!read_notify.select("span.new-content").isEmpty()) {
                                        new_content = read_notify.select("span.new-content").attr("title").replace("Обновлялась ", "");
                                        if (new_content.contains(".")) {
                                            String[] t = new_content.split("\\.");
                                            new_content = t[0];
                                        }
                                    } else {
                                        new_content = "";
                                    }
                                }
                            }
                            if (!read_notify.select("span.new-parts-in-title").isEmpty()) {
                                new_content = read_notify.select("span.new-parts-in-title").text();
                            }
                            if (new_content == null) {
                                new_content = "";
                            }

                            StringBuilder fandom = new StringBuilder();
                            StringBuilder rating = new StringBuilder();
                            StringBuilder genres = new StringBuilder();
                            StringBuilder caution = new StringBuilder();
                            StringBuilder tags = new StringBuilder();
                            String size = "";
                            String sizetype = "";
                            String pages = "";
                            String parts = "";
                            String status = "";
                            String pairings = "";
                            String collection_id;
                            String bad = "";
                            String critic = "";
                            Element icontent;
                            int idx = 0;

                            if (!v.select("div.badge-status-frozen").isEmpty()) {
                                status = "заморожен";
                            }
                            if (!v.select("svg.ic_in-progress").isEmpty()) {
                                status = "в процессе";
                            }
                            if (!v.select("div.badge-status-finished").isEmpty()) {
                                status = "закончен";
                            }

                            for (Element i : infod) {
                                icontent = infod.select("dd").get(idx);
                                switch (i.select("dt").text()) {
                                    case "Фэндом:":
                                        fandom = new StringBuilder();
                                        for (Element f : icontent.select("a")) {
                                            fandom.append(f.text()).append(", ");
                                        }
                                        if (fandom.length() > 2) {
                                            fandom = new StringBuilder(fandom.substring(0, fandom.length() - 2));
                                        }
                                        break;
                                    case "Размер:":
                                        String sz = icontent.select("span.size-title").text();
                                        String xz = "";

                                        if (sz.contains("Драббл")) {
                                            xz = "Драббл";
                                        }
                                        if (sz.contains("Мини")) {
                                            xz = "Мини";
                                        }
                                        if (sz.contains("Миди")) {
                                            xz = "Миди";
                                        }
                                        if (sz.contains("Макси")) {
                                            xz = "Макси";
                                        }

                                        String[] szz = icontent.text().split(",");
                                        if (szz.length == 2) {
                                            pages = szz[0].replaceAll("[^0-9.,]+", "");
                                            parts = szz[1].replaceAll("[^0-9.,]+", "");
                                        } else if (szz.length == 3) {
                                            pages = szz[1].replaceAll("[^0-9.,]+", "");
                                            parts = szz[2].replaceAll("[^0-9.,]+", "");
                                        } else {
                                            parts = "";
                                            pages = "";
                                        }

                                        if (xz.isEmpty() && !pages.isEmpty()) {
                                            switch (Integer.parseInt(pages) / 10) {
                                                case 0:
                                                case 1:
                                                    xz = "Драббл";
                                                    break;
                                                case 2:
                                                    xz = "Мини";
                                                    break;
                                                case 3:
                                                case 4:
                                                case 5:
                                                case 6:
                                                    xz = "Миди";
                                                    break;
                                                default:
                                                    xz = "Макси";
                                            }
                                        }

                                        size = parts + "/" + pages;
                                        sizetype = xz;
                                        break;
                                    case "Пэйринг и персонажи:":
                                    case "Пэйринг или персонажи:":
                                        pairings = icontent.text();
                                        break;
                                    case "Другие метки:":
                                    case "Метки:":
                                    case "Отношения:":
                                    case "Жанры:":
                                    case "Предупреждения:":
                                        try {
                                            for (Element g : icontent.select("a")) {
                                                String gg = g.text().replaceAll("<[^>]*>", "");
                                                if (g.hasClass("tag-adult")) {
                                                    gg = gg + " \uD83D\uDD1E";
                                                }
                                                if (g.hasClass("disliked-parameter-link")) {
                                                    gg = "<b><font color=\"#8a2525\">" + gg + "</font></b>";
                                                    bad = "!";
                                                }
                                                if (g.hasClass("liked-parameter-link")) {
                                                    gg = "<b><font color=\"#086e00\">" + gg + "</font></b>";
                                                }

                                                String tag_id = g.attr("href").replace("/tags/", "");
                                                if (!tag_id.equals("")) {
                                                    String tc_id = Tags.getCategoryId(tag_id);
                                                    switch (tc_id) {
                                                        case "25":
                                                            genres.append(gg).append(", ");
                                                            break;
                                                        case "26":
                                                            caution.append(gg).append(", ");
                                                            break;
                                                        default:
                                                            tags.append(gg).append(", ");
                                                    }
                                                }
                                            }
                                            if (tags.length() > 2) {
                                                tags = new StringBuilder(tags.substring(0, tags.length() - 2));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        break;

                                    default:
                                        break;
                                }
                                idx++;
                            }

                            if (!v.select("span.notice-yellow").isEmpty()) {
                                critic = "1";
                            }

                            if (!v.select("strong.badge-rating-PG-13").isEmpty()) {
                                rating.append("PG-13");
                            }
                            if (!v.select("strong.badge-rating-R").isEmpty()) {
                                rating.append("R");
                            }
                            if (!v.select("strong.badge-rating-NC-21").isEmpty()) {
                                rating.append("NC-21");
                            }
                            if (!v.select("strong.badge-rating-G").isEmpty()) {
                                rating.append("G");
                            }
                            if (!v.select("strong.badge-rating-NC-17").isEmpty()) {
                                rating.append("NC-17");
                            }

                            Elements itags = v.select("div.tags a");
                            for (Element g : itags) {
                                try {
                                    String gg = g.text().replaceAll("<[^>]*>", "");
                                    if (g.hasClass("tag-adult")) {
                                        gg = gg + " \uD83D\uDD1E";
                                    }
                                    if (g.hasClass("disliked-parameter-link")) {
                                        gg = "<b><font color=\"#8a2525\">" + gg + "</font></b>";
                                        bad = "!";
                                    }
                                    if (g.hasClass("liked-parameter-link")) {
                                        gg = "<b><font color=\"#086e00\">" + gg + "</font></b>";
                                    }

                                    String tag_id = g.attr("href").replace("/tags/", "");
                                    if (!tag_id.equals("")) {
                                        String tc_id = Tags.getCategoryId(tag_id);
                                        switch (tc_id) {
                                            case "25":
                                                genres.append(gg).append(", ");
                                                break;
                                            case "26":
                                                caution.append(gg).append(", ");
                                                break;
                                            default:
                                                tags.append(gg).append(", ");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            collection_id = Fanfic.getCollectionId(id);
                            if (v.select("fanfic-thumb-tool-panel").isEmpty()) {
                                if (v.select("div.fanfic-thumb-tool-panel").select("a").hasAttr("data-collection-id")) {
                                    collection_id = v.select("div.fanfic-thumb-tool-panel").select("a").attr("data-collection-id");
                                }
                            }


                            String s_info = YO.createTags(description.text());

                            Fanfic old_f = Fanfic.getById(id);
                            Fanfic f = new Fanfic();

                            f.nid = id;
                            f.title = title;
                            f.sup = sup;
                            f.authors = authors;
                            f.direction = direction;
                            f.pairings = pairings;
                            f.fandom = fandom.toString();
                            f.rating = rating.toString();
                            f.genres = genres.toString();
                            f.cautions = caution.toString();
                            f.tags = tags.toString();
                            f.size = size;
                            f.sizetype = sizetype;
                            f.pages = pages;
                            f.parts = parts;
                            f.status = status;
                            f.info = s_info; //description.text();
                            f.collection_id = collection_id;
                            f.date_changes = date_changes;
                            f.new_content = new_content;
                            f.bad = bad;
                            f.new_part = new_parts;
                            f.critic = critic;
                            f.trophy = trophy;
                            if (old_f != null) {
                                if (old_f.new_part != null) {
                                    if (!old_f.new_part.isEmpty()) {
                                        f.new_part = old_f.new_part;
                                    }
                                }
                            }
                            f.save();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            executors.submit(runnable);
    }
}
