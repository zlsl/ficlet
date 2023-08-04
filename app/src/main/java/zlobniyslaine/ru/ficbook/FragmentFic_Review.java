package zlobniyslaine.ru.ficbook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterFicReviews;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionReplyReview;
import zlobniyslaine.ru.ficbook.models.Parts;


public class FragmentFic_Review extends Fragment {

    private ArrayList<HashMap<String, Object>> FicReviews = new ArrayList<>();

    private Boolean Loading = false;
    private Boolean refresh = true;
    private Boolean FirstRun = true;
    private Integer StartPosition = 0;
    private Integer MaxPosition = 100000;
    private String UrlTemplate = "";

    RecyclerView rv1;
    ProgressBar pb1;
    SwipeRefreshLayout swipeContainer;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fic_reviews, container, false);

        rv1 = rootView.findViewById(R.id.rv1);
        pb1 = rootView.findViewById(R.id.pb1);
        swipeContainer = rootView.findViewById(R.id.swipeContainer);


        FicReviews = new ArrayList<>();

        if (getArguments() != null) {
            UrlTemplate = getArguments().getString("url", "");
            Log.i("F", UrlTemplate);
        }

        rv1.setHasFixedSize(true);
        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(rootView.getContext());
        rv1.setLayoutManager(mLayoutManager);
        AdapterFicReviews rv_adapter = new AdapterFicReviews(rootView.getContext(), FicReviews);
        rv1.setAdapter(rv_adapter);

        rv1.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore() {
                if (!Loading) {
                    if (((totalItemCount > 3) || (totalItemCount == 0)) && (StartPosition <= MaxPosition)) {
                        if (!FirstRun) {
                            runFetcher();
                        }
                    }
                }
            }
        });

        swipeContainer.setOnRefreshListener(() -> {
            StartPosition = 0;
            refresh = true;
            runFetcher();
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (Application.isInternetAvailable()) {
            runFetcher();
        }

        return rootView;
    }

    private void runFetcher() {
        FirstRun = false;
        int oldc = FicReviews.size();
        if (StartPosition == 0) {
            swipeContainer.setRefreshing(true);
            StartPosition = 1;
            FicReviews.clear();
            if (oldc > 0) {
                Objects.requireNonNull(rv1.getAdapter()).notifyItemRangeRemoved(0, oldc);
            }
            refresh = true;
        }
        if (StartPosition <= MaxPosition) {

            if (StartPosition > -1) {
                String u = UrlTemplate.replace("@", StartPosition.toString());
                if (!Loading) {
                    if (!refresh) {
                        pb1.setVisibility(View.VISIBLE);
                        refresh = false;
                    }
                }
                new fetcher_reviews(this).execute(u);
            }
        } else {
            Log.d("List", "END");
        }
    }

    static class fetcher_reviews extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentFic_Review> activityReference;

        fetcher_reviews(FragmentFic_Review context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;
        int oldc = 0;

        @Override
        protected Void doInBackground(String... params) {
            if (activityReference.get() == null) {
                return null;
            }
            try {
                oldc = activityReference.get().FicReviews.size();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            Log.i("URL", params[0]);
            try {
                if (activityReference.get().Loading) {
                    return null;
                }

                activityReference.get().Loading = true;
                Response response = Application.httpclient.newCall(Application.getRequestBuilder(params[0])).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    doc = Jsoup.parse(body.string());
                    response.close();
                }

                activityReference.get().MaxPosition = 1;
                if (!doc.select("nav.pagination-holder li.text b").isEmpty()) {
                    activityReference.get().MaxPosition = Integer.parseInt(doc.select("nav.pagination-holder li.text b").get(1).text());
                    Log.d("MAX P", "-A");
                }
                if (!doc.select("nav.pagenav div.paging-description b").isEmpty()) {
                    activityReference.get().MaxPosition = Integer.parseInt(doc.select("nav.pagenav div.paging-description b").get(1).text());
                    Log.d("MAX P", "-B");
                }
                if (!doc.select("li.text input").isEmpty()) {
                    activityReference.get().MaxPosition = Integer.parseInt(doc.select("li.text input").attr("max"));
                    Log.d("MAX P", "-C");
                }
                Log.d("MAX P", activityReference.get().MaxPosition + "!");

            } catch (SocketTimeoutException e) {
                Application.displayPopup("Сервер не отвечает");
            } catch (UnknownHostException e) {
                Application.displayPopup("Проблемы с соединением");
            } catch (IOException e) {
                Application.displayPopup("Ошибка загрузки");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Elements vid = doc.select("article.comment-container"); // _thumb
                HashMap<String, Object> map;

                for (Element v : vid) {
                    map = new HashMap<>();

                    String id = v.select("a").get(0).attr("name").replace("com", "");
                    String s_avatar_url = v.select("img").attr("src");
                    String s_author = v.select("a.comment_author").text();
                    String s_datetime = v.select("time").text();
                    String s_content2 = v.select("div.comment_message").html().trim().replace("h**p", "http");

                    String regex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(s_content2);
                    String s_content = s_content2;
                    while (m.find()) {
                        if ((m.group().contains("jpg")) || (m.group().contains("png"))) {
                            s_content = s_content.replace(m.group(), "<img src=\"" + m.group() + "\">");
                        } else {
                            s_content = s_content.replace(m.group(), "<a href='" + m.group() + "'>" + m.group() + "</a>");
                        }
                    }
                    String author_id = v.select("a.comment_author").attr("href").replace("/authors/", "");
                    String xfic_id = v.select("div.comment_link_to_fic a").attr("href").replace("/readfic/", "");
                    String fic_id = xfic_id.split("/")[0];
                    String part_id = xfic_id.split("/")[1].split("\\?")[0];
                    String fic_text = v.select("div.comment_link_to_fic a").text();

                    if (author_id.isEmpty()) {
                        String anon = v.select("span.comment_author").text();
                        s_content = "<b>" + anon + "</b><br><br>\n" + s_content;
                    }

                    map.put("id", id);
                    map.put("author_name", s_author);
                    map.put("author_id", author_id);
                    map.put("datetime", s_datetime);
                    map.put("author_avatar_url", s_avatar_url);
                    map.put("content", s_content);
                    map.put("part_id", part_id);
                    map.put("fic_id", fic_id);
                    map.put("fic_name", fic_text);
                    if (activityReference.get() != null) {
                        activityReference.get().FicReviews.add(map);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (activityReference.get() != null) {
                    int newi = activityReference.get().FicReviews.size() - oldc;
                    if (newi > 0) {
                        Objects.requireNonNull(activityReference.get().rv1.getAdapter()).notifyItemRangeInserted(oldc - 1, newi);
                    }
                    activityReference.get().StartPosition++;
                    activityReference.get().Loading = false;
                    activityReference.get().pb1.setVisibility(View.GONE);
                    activityReference.get().refresh = false;
                    activityReference.get().swipeContainer.setRefreshing(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    @SuppressLint("InflateParams")
    public void ReplyDialog(final String id) {
        try {
            AlertDialog.Builder replyDialog = new AlertDialog.Builder(requireActivity());
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View alertDialogView;
            if (inflater != null) {
                alertDialogView = inflater.inflate(R.layout.reply_dialog, null);
            } else {
                return;
            }
            replyDialog.setView(alertDialogView);

            final EditText reply_text = alertDialogView.findViewById(R.id.reply_text);
            final CheckBox cb_follow = alertDialogView.findViewById(R.id.cb_set_follow);
            final Spinner spinner_part = alertDialogView.findViewById(R.id.spinner_part);

            List<Parts> parts = Parts.getParts(id);
            final ArrayList<String> array_parts = new ArrayList<>();
            for (Parts p : parts) {
                array_parts.add(p.title);
            }

            ArrayAdapter<String> ad_parts = new ArrayAdapter<>(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_parts);
            ad_parts.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
            spinner_part.setAdapter(ad_parts);

            replyDialog.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

            replyDialog.setPositiveButton(
                    "Отправить",
                    (dialog, which) -> {
                        String name = "";
                        if (array_parts.size() > 0) {
                            name = array_parts.get(spinner_part.getSelectedItemPosition());
                        }
                        AjaxActionReplyReview action = new AjaxActionReplyReview();
                        action.Do(Parts.getIdByName(name, id), reply_text.getText().toString(), cb_follow.isChecked());
                    });
            replyDialog.setCancelable(false);
            replyDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}