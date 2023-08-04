package zlobniyslaine.ru.ficbook;


import static zlobniyslaine.ru.ficbook.Application.FICLET_HOST;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionFavourite;
import zlobniyslaine.ru.ficbook.controls.MLRoundedImageView;
import zlobniyslaine.ru.ficbook.models.Authors;
import zlobniyslaine.ru.ficbook.pagers.AuthorPagerAdapter;


public class ActivityAuthorProfile extends AppCompatActivity {

    private String UrlTemplate;
    private String author_name;
    private Context context;
    private String id;
    private Boolean like = false;

    private Fragment fragment_works;

    ProgressBar pb1;
    RelativeLayout l_shadow;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView tv_author_name;
    MLRoundedImageView iv_author_avatar;
    ImageView iv_like;

    public void openChat() {
        Intent intent = new Intent(context, ActivityChatThread.class);
        intent.putExtra("user_id", id);
        context.startActivity(intent);
    }

    public void setFav() {
        AjaxActionFavourite action = new AjaxActionFavourite();
        if (!like) {
            action.Do(id, true);

            CoordinatorLayout mRoot = findViewById(R.id.coordl);
            Snackbar snackbar = Snackbar.make(mRoot, "Автор «" + author_name + "» добавлен в избранное", Snackbar.LENGTH_LONG);
            snackbar.show();

            iv_like.setImageResource(R.drawable.ic_favorite_w24);
        } else {
            action.Do(id, false);

            CoordinatorLayout mRoot = findViewById(R.id.coordl);
            Snackbar snackbar = Snackbar.make(mRoot, "Автор «" + author_name + "» удален из избранного", Snackbar.LENGTH_LONG);
            snackbar.show();
            iv_like.setImageResource(R.drawable.ic_favorite_border_w24);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_author_profile);

        pb1 = findViewById(R.id.pb1);
        l_shadow = findViewById(R.id.l_shadow);
        viewPager = findViewById(R.id.my_view_pager);
        tabLayout = findViewById(R.id.my_tab_layout);
        tv_author_name = findViewById(R.id.tv_author_name);
        iv_author_avatar = findViewById(R.id.iv_author_avatar);
        iv_like = findViewById(R.id.iv_like);

        findViewById(R.id.iv_chat).setOnClickListener(v -> openChat());
        findViewById(R.id.fabFetch).setOnClickListener(v -> {
            if (!Application.isGuest()) {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Application.downloadList(((FragmentFanficList) fragment_works).getFics(), false);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Скачать все фанфики автора?\nБудет скачано и помещено в кэш " + ((FragmentFanficList) fragment_works).getCount() + " файлов.").setPositiveButton("Да", dialogClickListener)
                        .setNegativeButton("Нет", dialogClickListener).show();
            }
        });

        iv_like.setOnClickListener(v -> setFav());

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.addTab(tabLayout.newTab().setText("Информация"));
        tabLayout.addTab(tabLayout.newTab().setText("Работы"));
        tabLayout.addTab(tabLayout.newTab().setText("Соавтор"));
        tabLayout.addTab(tabLayout.newTab().setText("Бета"));
        tabLayout.addTab(tabLayout.newTab().setText("Отзывы"));
        tabLayout.addTab(tabLayout.newTab().setText("В избранном"));

        Intent intent = getIntent();

        id = intent.getStringExtra("id");

        UrlTemplate = "https://ficbook.net/authors/" + id;

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        l_shadow.setVisibility(View.VISIBLE);

        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityAuthorProfile> activityReference;

        fetcher_main(ActivityAuthorProfile context) {
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
        Log.d("PARSE", "Start");

        Bundle bundle_reviews = new Bundle();
        Bundle bundle_info = new Bundle();
        Bundle bundle_works = new Bundle();
        Bundle bundle_coworks = new Bundle();
        Bundle bundle_beta = new Bundle();
        Bundle bundle_favs = new Bundle();

        bundle_reviews.putString("id", id);
        bundle_reviews.putString("url", "https://ficbook.net/authors/" + id + "/comments?p=@");
        bundle_works.putString("url", "https://ficbook.net/authors/" + id + "/profile/works?p=@");
        bundle_coworks.putString("url", "https://ficbook.net/authors/" + id + "/profile/coauthor?p=@");
        bundle_beta.putString("url", "https://ficbook.net/authors/" + id + "/profile/beta?p=@");
        bundle_works.putString("furl", FICLET_HOST + "?id=" + id);

        try {
            author_name = doc.select("div.user-name").text();
            String a_avatar_url = "";
            if (doc.select("div.profile-header div.avatar-cropper img").size() > 0) {
                a_avatar_url = doc.select("div.profile-header div.avatar-cropper img").attr("src");
            }
            tv_author_name.setText(author_name);

            Picasso.with(context)
                    .load(a_avatar_url)
                    .into(iv_author_avatar);

            Authors col = new Authors();
            col.name = author_name;
            col.avatar_url = a_avatar_url;
            col.nid = id;
            col.save();

            Elements fav = doc.select("div.favourites");
            if (fav.select(".favourited").attr("style").contains("none")) {
                like = false;
                iv_like.setImageResource(R.drawable.ic_favorite_border_w24);
            }
            if (fav.select(".add_to_favorites").attr("style").contains("none")) {
                like = true;
                iv_like.setImageResource(R.drawable.ic_favorite_w24);
            }
            String a_about = doc.select("section.mb-30").get(0).html();

            bundle_info.putString("id", id);
            bundle_info.putString("about", a_about);
            bundle_favs.putString("id", id);

            FragmentManager fm = getSupportFragmentManager();
            List<Fragment> fragments = new Vector<>();

            Fragment fragment_info = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentAuthor_Info.class.getName());
            Fragment fragment_reviews = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFic_Review.class.getName());
            fragment_works = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFanficList.class.getName());
            Fragment fragment_coworks = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFanficList.class.getName());
            Fragment fragment_beta = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentFanficList.class.getName());
            Fragment fragment_favs = fm.getFragmentFactory().instantiate(ClassLoader.getSystemClassLoader(), FragmentProfile_Favourites.class.getName());

            fragment_info.setArguments(bundle_info);
            fragment_reviews.setArguments(bundle_reviews);
            fragment_works.setArguments(bundle_works);
            fragment_coworks.setArguments(bundle_coworks);
            fragment_beta.setArguments(bundle_beta);
            fragment_favs.setArguments(bundle_favs);

            fragments.add(fragment_info);
            fragments.add(fragment_works);
            fragments.add(fragment_coworks);
            fragments.add(fragment_beta);
            fragments.add(fragment_reviews);
            fragments.add(fragment_favs);

            viewPager.setAdapter(new AuthorPagerAdapter(getSupportFragmentManager(), fragments));
            viewPager.setOffscreenPageLimit(10);
            tabLayout.setupWithViewPager(viewPager);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        } catch (Exception e) {
            Log.e("PARSE", e.toString());
        }
        l_shadow.animate()
                .alpha(0.0f)
                .setDuration(500);

        pb1.setVisibility(View.GONE);
    }
}
