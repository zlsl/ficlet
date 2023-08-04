package zlobniyslaine.ru.ficbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.ActiveAndroid;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterCollections;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionDropCollection;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionNewCollection;
import zlobniyslaine.ru.ficbook.models.Collections;

public class FragmentMain_Collections extends Fragment {

    private List<Collections> clist;
    private AdapterCollections rv_cadapter;
    private SwitchMaterial cb_own;

    private int MaxPosition = 1;
    private int page = 1;


    private void Filter(Boolean own) {
        clist.clear();
        if (own) {
            clist.addAll(Collections.getOwnAll());
            Log.i("COLL", "own " + clist.size());
        } else {
            clist.addAll(Collections.getAll());
            Log.i("COLL", "all " + clist.size());
        }
        rv_cadapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_collections, container, false);

        RecyclerView rv_collections = rootView.findViewById(R.id.lv_collections);
        cb_own = rootView.findViewById(R.id.cb_own);
        cb_own.setChecked(Application.sPref.getBoolean("coll_filter", false));

        clist = Collections.getAll();
        rv_cadapter = new AdapterCollections(getContext(), clist);

        cb_own.setOnCheckedChangeListener((compoundButton, b) -> {
            Filter(b);
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.putBoolean("coll_filter", b);
            ed.apply();
        });

        rootView.findViewById(R.id.btn_reload).setOnClickListener(view -> {
            Collections.Create();
            int sz = clist.size();
            clist.clear();
            rv_cadapter.notifyItemRangeRemoved(0, sz);
            page = 1;
            new fetcher_collections(FragmentMain_Collections.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new fetcher_collections2(FragmentMain_Collections.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });

        rootView.findViewById(R.id.btn_new_collection).setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("Новый сборник");
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.requestFocus();
            alert.setView(input);

            alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                AjaxActionNewCollection action = new AjaxActionNewCollection();
                action.Do(input.getText().toString());

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                            new fetcher_collections(FragmentMain_Collections.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            new fetcher_collections2(FragmentMain_Collections.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        },
                        2000);
            });

            alert.setNegativeButton("Отмена", (dialog, whichButton) -> {
            });

            alert.show();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        });


        rv_collections.setLayoutManager(new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv_collections.setAdapter(rv_cadapter);

        Filter(cb_own.isChecked());

        return rootView;
    }

    static class fetcher_collections extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentMain_Collections> activityReference;
        String bodyx = "";

        fetcher_collections(FragmentMain_Collections context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            if (!Application.isInternetAvailable()) {
                return null;
            }
            try {
                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/home/collections?p=" + activityReference.get().page)).execute();
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        bodyx = body.string();
                        response.close();
                    }
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

            try {
                if (!bodyx.isEmpty()) {
                    doc = Jsoup.parse(bodyx);
                    if (!doc.select("nav.pagination-holder li.text b").isEmpty()) {
                        activityReference.get().MaxPosition = Integer.parseInt(doc.select("nav.pagination-holder li.text b").get(1).text());
                    }
                    Log.i("COLL PG", activityReference.get().MaxPosition + "");
                    if ((doc != null) && (activityReference.get() != null)) {
                        Elements vid = doc.select("div.profile-holder");

                        Elements lg = doc.select("div.social-auth-login");
                        if (lg.select("a").size() > 1) {
                            String code = lg.select("a").get(1).attr("href");
                            Log.i("VKA", code);
                            Application.setVKAuth(code);
                        }

                        Elements collections = doc.select("div.collection-thumb");

                        ActiveAndroid.beginTransaction();

                        for (Element v : collections) {
                            Elements item = v.select("div.collection-thumb-info");
                            Elements author = v.select("div.collection-thumb-author");
                            String id = item.select("a").first().attr("href").replace("/collections/", "");

                            String[] ids = id.split("\\?");
                            id = ids[0];


                            String title = item.select("a").first().text();
                            String locked = item.select("svg").first().className();
                            String a = author.text();
                            String count = item.text().replace(title + " ", "").replace(" " + a, "").replace("(", "").replace(")", "");
                            if (locked.contains("unlocked")) {
                                locked = "0";
                            } else {
                                locked = "1";
                            }

                            Collections col = new Collections();
                            col.title = title;
                            col.locked = locked;
                            col.nid = id;
                            col.count = count;
                            col.author = a;
                            col.save();
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (activityReference.get() != null) {
                if (activityReference.get().page < activityReference.get().MaxPosition) {
                    activityReference.get().page++;
                    new fetcher_collections(activityReference.get()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    activityReference.get().Filter(activityReference.get().cb_own.isChecked());
                }
            }

            Application.firePopup();
        }
    }

    static class fetcher_collections2 extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentMain_Collections> activityReference;
        String bodyx = "";

        fetcher_collections2(FragmentMain_Collections context) {
            activityReference = new WeakReference<>(context);
        }

        Document doc = null;

        @Override
        protected Void doInBackground(String... params) {
            if (!Application.isInternetAvailable()) {
                return null;
            }
            try {
                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/home/collections?type=other&p=" + activityReference.get().page)).execute();
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        bodyx = body.string();
                        response.close();
                    }
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

            try {
                if (!bodyx.isEmpty()) {
                    doc = Jsoup.parse(bodyx);
                    if (!doc.select("nav.pagination-holder li.text b").isEmpty()) {
                        activityReference.get().MaxPosition = Integer.parseInt(doc.select("nav.pagination-holder li.text b").get(1).text());
                    }
                    Log.i("COLL PG", activityReference.get().MaxPosition + "");
                    if ((doc != null) && (activityReference.get() != null)) {
                        Elements collections = doc.select("div.collection-thumb");
                        ActiveAndroid.beginTransaction();

                        for (Element v : collections) {
                            Elements item = v.select("div.collection-thumb-info");
                            Elements author = v.select("div.collection-thumb-author");
                            String id = item.select("a").first().attr("href").replace("/collections/", "");

                            String[] ids = id.split("\\?");
                            id = ids[0];


                            String title = item.select("a").first().text();
                            String locked = item.select("svg").first().className();
                            String a = author.text();
                            String count = item.text().replace(title + " ", "").replace(" " + a, "").replace("(", "").replace(")", "");
                            if (locked.contains("unlocked")) {
                                locked = "0";
                            } else {
                                locked = "1";
                            }

                            if (!a.equals(Application.user_name)) {
                                Collections col = new Collections();
                                col.title = title;
                                col.locked = locked;
                                col.nid = id;
                                col.count = count;
                                col.author = a;
                                col.save();
                            }
                        }
                        ActiveAndroid.setTransactionSuccessful();
                        ActiveAndroid.endTransaction();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (activityReference.get() != null) {
                if (activityReference.get().page < activityReference.get().MaxPosition) {
                    activityReference.get().page++;
                    new fetcher_collections(activityReference.get()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    activityReference.get().Filter(activityReference.get().cb_own.isChecked());
                }
            }

            Application.firePopup();
        }
    }

}