package zlobniyslaine.ru.ficbook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import zlobniyslaine.ru.ficbook.adapters.AdapterAutoSuggest;
import zlobniyslaine.ru.ficbook.adapters.AdapterFandomsSearch;
import zlobniyslaine.ru.ficbook.adapters.RecyclerItemClickListener;
import zlobniyslaine.ru.ficbook.controls.MultiSpinner;
import zlobniyslaine.ru.ficbook.models.Category;
import zlobniyslaine.ru.ficbook.models.Fandoms;
import zlobniyslaine.ru.ficbook.models.Tags;
import zlobniyslaine.ru.ficbook.models.TagsCategory;

public class FragmentRequestsSearchMain extends Fragment {

    private ArrayList<String> array_category;
    private ArrayList<String> array_tcategory;
    private ArrayList<String> array_fandoms;
    private ArrayList<String> array_tags_ta;

    private ArrayList<HashMap<String, Object>> SearchFandoms;
    private ArrayList<HashMap<String, Object>> SearchTags;
    private ArrayList<HashMap<String, Object>> SelectTags;
    private AdapterFandomsSearch a_fandoms;
    private AdapterFandomsSearch a_tags;
    private AppCompatActivity mActivity;

    private String fandom_id;
    private String f_id;
    private String t_id;
    private String tcat_id;
    private String tcat_name;
    private String sort_id = "1";
    private String type_id = "0";
    private String rating_ids = "&ratings[]=5&ratings[]=6&ratings[]=7&ratings[]=8&ratings[]=9";
    private String direction_ids = "&directions[]=1&directions[]=2&directions[]=3&directions[]=4&directions[]=7&directions[]=6&directions[]=5";

    private TextView tv_searchtext;

    private void addAllowFandom() {
        showAddFandom(true);
    }

    private void addDenyFandom() {
        showAddFandom(false);
    }

    private void addAllowTag() {
        showAddTags(true);
    }

    private void addDenyTag() {
        showAddTags(false);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_requests_search_main, container, false);

        tv_searchtext = rootView.findViewById(R.id.tv_searchtext);
        Spinner spinner_sort = rootView.findViewById(R.id.spinner_sort);
        Spinner spinner_type = rootView.findViewById(R.id.spinner_type);
        MultiSpinner spinner_rating = rootView.findViewById(R.id.spinner_rating);
        MultiSpinner spinner_direction = rootView.findViewById(R.id.spinner_direction);
        RecyclerView rv_fandoms = rootView.findViewById(R.id.rv_fandoms);
        RecyclerView rv_tags = rootView.findViewById(R.id.rv_tags);
        Button btn_fandom_allow = rootView.findViewById(R.id.btn_fandom_allow);
        Button btn_fandom_deny = rootView.findViewById(R.id.btn_fandom_deny);

        btn_fandom_allow.setOnClickListener(v -> addAllowFandom());
        btn_fandom_deny.setOnClickListener(v -> addDenyFandom());

        array_fandoms = new ArrayList<>();
        array_tags_ta = new ArrayList<>();
        SearchFandoms = new ArrayList<>();
        SearchTags = new ArrayList<>();
        SelectTags = new ArrayList<>();

        rv_fandoms.setLayoutManager(new WrapContentLinearLayoutManager(rootView.getContext()));
        rv_tags.setLayoutManager(new WrapContentLinearLayoutManager(rootView.getContext()));

        a_fandoms = new AdapterFandomsSearch(rootView.getContext(), SearchFandoms, true);
        rv_fandoms.setAdapter(a_fandoms);

        a_tags = new AdapterFandomsSearch(rootView.getContext(), SearchTags, true);
        rv_tags.setAdapter(a_tags);

        rootView.findViewById(R.id.btn_tag_allow).setOnClickListener(v -> addAllowTag());
        rootView.findViewById(R.id.btn_tag_deny).setOnClickListener(v -> addDenyTag());



        List<Category> category = Category.getAll();
        array_category = new ArrayList<>();
        for (Category cx : category) {
            array_category.add(cx.name);
        }

        ArrayAdapter<CharSequence> ad_direction = ArrayAdapter.createFromResource(mActivity, R.array.array_direction, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_direction.setAdapter(ad_direction, false, selected -> {
            direction_ids = "";
            if (selected[0]) {
                direction_ids = direction_ids + "&directions[]=1";
            }
            if (selected[1]) {
                direction_ids = direction_ids + "&directions[]=2";
            }
            if (selected[2]) {
                direction_ids = direction_ids + "&directions[]=3";
            }
            if (selected[3]) {
                direction_ids = direction_ids + "&directions[]=4";
            }
            if (selected[4]) {
                direction_ids = direction_ids + "&directions[]=7";
            }
            if (selected[5]) {
                direction_ids = direction_ids + "&directions[]=6";
            }
            if (selected[6]) {
                direction_ids = direction_ids + "&directions[]=5";
            }
        });
        boolean[] selectedItems3 = new boolean[ad_direction.getCount()];
        selectedItems3[0] = true;
        selectedItems3[1] = true;
        selectedItems3[2] = true;
        selectedItems3[3] = true;
        selectedItems3[4] = true;
        selectedItems3[5] = true;
        selectedItems3[6] = true;
        spinner_direction.setAllText("Все направленности");
        spinner_direction.setDefaultText("Направленность");
        spinner_direction.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);
        spinner_direction.setSelected(selectedItems3);

        ArrayAdapter<CharSequence> ad_rating = ArrayAdapter.createFromResource(mActivity, R.array.array_rating, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_rating.setAdapter(ad_rating, false, selected -> {
            rating_ids = "";
            if (selected[0]) {
                rating_ids = rating_ids + "&ratings[]=5";
            }
            if (selected[1]) {
                rating_ids = rating_ids + "&ratings[]=6";
            }
            if (selected[2]) {
                rating_ids = rating_ids + "&ratings[]=7";
            }
            if (selected[3]) {
                rating_ids = rating_ids + "&ratings[]=8";
            }
            if (selected[4]) {
                rating_ids = rating_ids + "&ratings[]=9";
            }
        });
        boolean[] selectedItems2 = new boolean[ad_rating.getCount()];
        selectedItems2[0] = true;
        selectedItems2[1] = true;
        selectedItems2[2] = true;
        selectedItems2[3] = true;
        selectedItems2[4] = true;
        spinner_rating.setAllText("Все рейтинги");
        spinner_rating.setDefaultText("Рейтинг фанфика");
        spinner_rating.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);
        spinner_rating.setSelected(selectedItems2);

        ArrayAdapter<CharSequence> ad_sort = ArrayAdapter.createFromResource(mActivity, R.array.array_sort_requests, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_sort.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_sort.setAdapter(ad_sort);
        spinner_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        sort_id = "2";
                        break;
                    case 0:
                    default:
                        sort_id = "1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_type = ArrayAdapter.createFromResource(mActivity, R.array.array_requests_type, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_type.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_type.setAdapter(ad_type);
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        type_id = "0";
                        break;
                    case 2:
                        type_id = "-1";
                        break;
                    case 1:
                    default:
                        type_id = "1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        return rootView;
    }

    public String getUrl() {
        StringBuilder tmp = new StringBuilder("https://ficbook.net/requests?p=@");

        tmp.append("&status=").append(type_id);
        if (tv_searchtext.getText().length() > 0) {
            try {
                tmp.append("&title=").append(URLEncoder.encode(tv_searchtext.getText().toString(), "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            for (HashMap<String, Object> map : SearchFandoms) {
                if (Objects.requireNonNull(map.get("mode")).equals("allow")) {
                    tmp.append("&fandom_ids[]=").append(map.get("id"));
                } else {
                    tmp.append("&fandom_exclude_ids[]=").append(map.get("id"));
                }
            }

            for (HashMap<String, Object> map : SearchTags) {
                if (Objects.requireNonNull(map.get("mode")).equals("allow")) {
                    tmp.append("&tags[]=").append(map.get("id"));
                } else {
                    tmp.append("&tags_exclude[]=").append(map.get("id"));
                }
            }

            tmp.append(rating_ids);
            tmp.append(direction_ids);
            tmp.append("&sort=").append(sort_id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Random r = new Random();
        tmp.append("&rnd=").append(r.nextInt()).append("&find=Find");

        return tmp.toString();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            mActivity = (AppCompatActivity) context;
        }
    }

    void showAddFandom(final Boolean add_mode) {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = this.getLayoutInflater();

        View alertDialogView = inflater.inflate(R.layout.dialog_fandom_add, null);
        addDialog.setView(alertDialogView);

        final Spinner spinner_category = alertDialogView.findViewById(R.id.spinner_category);
        final AppCompatAutoCompleteTextView ta_fandom = alertDialogView.findViewById(R.id.ta_fandom);

        List<Category> category = Category.getAll();
        array_category = new ArrayList<>();
        for (Category cx : category) {
            array_category.add(cx.name);
        }

        ArrayAdapter<String> ad_category = new ArrayAdapter<>(mActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_category);
        ad_category.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_category.setAdapter(ad_category);
        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fandom_id = Category.getIdByName(array_category.get(position));

                if (!fandom_id.equals("")) {
                    ((ArrayAdapter) ta_fandom.getAdapter()).clear();
                    List<Fandoms> fandoms = Fandoms.getAll(fandom_id);

                    array_fandoms = new ArrayList<>();
                    for (Fandoms f : fandoms) {
                        array_fandoms.add(f.title + "|" + f.sec_title + "|" + f.count);
                    }
                    ta_fandom.setAdapter(new AdapterAutoSuggest(getActivity(), android.R.layout.simple_dropdown_item_1line, array_fandoms));
                    ta_fandom.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ta_fandom.setAdapter(new AdapterAutoSuggest(getActivity(), R.layout.item_fandom_list, array_fandoms));
        ta_fandom.setOnItemClickListener((parent, arg1, pos, id) -> {
            if (!ta_fandom.getText().toString().isEmpty()) {
                f_id = Fandoms.getFandomIdByNameFull(ta_fandom.getText().toString(), fandom_id);
            }
            ta_fandom.setSelection(0, 0);
        });
        ta_fandom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ta_fandom.getText().toString().length() > 2) {
                    if (Fandoms.getFandomIdByNameFull(s.toString(), fandom_id) == null) {
                        ta_fandom.setError("Фэндом нужно выбрать из списка!");
                    } else {
                        ta_fandom.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ta_fandom.setThreshold(1);

        addDialog.setPositiveButton("Ok", (dialog, which) -> {
            HashMap<String, Object> map = new HashMap<>();
            Fandoms f = Fandoms.getFandomById(f_id);

            if (f != null) {
                map.put("id", f.nid);
                if (add_mode) {
                    map.put("mode", "allow");
                } else {
                    map.put("mode", "deny");
                }
                map.put("title", f.title);
                map.put("secondary_title", f.sec_title);
                map.put("count", f.count);
                SearchFandoms.add(map);
                a_fandoms.notifyDataSetChanged();
            }

            dialog.dismiss();
        });

        AlertDialog dialog = addDialog.create();
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp;
        if (window != null) {
            wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);
        }

        dialog.show();
    }

    void showAddTags(final Boolean add_mode) {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(mActivity);
        final AlertDialog dialog;
        LayoutInflater inflater = this.getLayoutInflater();
        View alertDialogView = inflater.inflate(R.layout.dialog_tag_add, null);
        addDialog.setView(alertDialogView);
        addDialog.setPositiveButton("Ok", (dialog1, which) -> {
            HashMap<String, Object> map = new HashMap<>();
            Tags t = Tags.getById(t_id);
            if (t != null) {
                map.put("id", t.nid);
                if (add_mode) {
                    map.put("mode", "allow");
                    Log.i("allow", t.title);
                } else {
                    map.put("mode", "deny");
                    Log.i("deny", t.title);
                }
                map.put("title", t.title);
                map.put("secondary_title", t.description);
                map.put("count", "0");
                SearchTags.add(map);
                a_tags.notifyDataSetChanged();
            }
            dialog1.dismiss();
        });
        dialog = addDialog.create();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        final Spinner spinner_tcategory = alertDialogView.findViewById(R.id.spinner_category);
        final AppCompatAutoCompleteTextView ta_tag = alertDialogView.findViewById(R.id.ta_tag);
        final RecyclerView rv_ftags = alertDialogView.findViewById(R.id.rv_ftags);
        final AdapterFandomsSearch a_ftags;

        rv_ftags.setLayoutManager(new WrapContentLinearLayoutManager(this.getContext()));
        a_ftags = new AdapterFandomsSearch(this.getContext(), SelectTags, false);
        rv_ftags.setAdapter(a_ftags);

        rv_ftags.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), (view, position) -> {
                    t_id = Objects.requireNonNull(SelectTags.get(position).get("id")).toString();
                    HashMap<String, Object> map = new HashMap<>();
                    Tags t = Tags.getById(t_id);

                    if (t != null) {
                        map.put("id", t.nid);
                        if (add_mode) {
                            map.put("mode", "allow");
                            Log.i("allow", t.title);
                        } else {
                            map.put("mode", "deny");
                            Log.i("deny", t.title);
                        }
                        map.put("title", t.title);
                        map.put("secondary_title", t.description);
                        map.put("count", t.usage_count);
                        SearchTags.add(map);
                        a_tags.notifyDataSetChanged();
                    }
                    dialog.dismiss();
                })
        );

        List<TagsCategory> category = TagsCategory.getAll();
        array_tcategory = new ArrayList<>();
        for (TagsCategory cx : category) {
            array_tcategory.add(cx.title);
        }

        ta_tag.setDropDownHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ta_tag.setAdapter(new AdapterAutoSuggest(getActivity(), android.R.layout.simple_dropdown_item_1line, array_tags_ta));
        ta_tag.setText("");
        ((ArrayAdapter) ta_tag.getAdapter()).clear();
        List<Tags> tags = Tags.getAll();

        array_tags_ta = new ArrayList<>();
        for (Tags f : tags) {
            array_tags_ta.add(f.title + "|" + f.description + "|" + f.usage_count);
        }

        ArrayAdapter<String> ad_tcategory = new ArrayAdapter<>(mActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_tcategory);
        ad_tcategory.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_tcategory.setAdapter(ad_tcategory);
        spinner_tcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tcat_id = TagsCategory.getIdByName(array_tcategory.get(position));
                tcat_name = Objects.requireNonNull(TagsCategory.getById(tcat_id)).title;

                if (!tcat_id.equals("")) {
                    List<Tags> tags = Tags.getAllByCat(tcat_name);
                    SelectTags.clear();
                    for (Tags f : tags) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("id", f.nid);
                        map.put("mode", "allow");
                        map.put("title", f.title);
                        map.put("secondary_title", f.description);
                        map.put("count", f.usage_count);
                        SelectTags.add(map);
                    }
                    a_ftags.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ta_tag.setAdapter(new AdapterAutoSuggest(getActivity(), R.layout.item_fandom_list, array_tags_ta));
        ta_tag.setOnItemClickListener((parent, arg1, pos, id) -> {
            if (!ta_tag.getText().toString().isEmpty()) {
                t_id = Tags.getTagIdByName(ta_tag.getText().toString());
            }
            ta_tag.setSelection(0, 0);
            HashMap<String, Object> map = new HashMap<>();
            Tags t = Tags.getById(t_id);
            if (t != null) {
                map.put("id", t.nid);
                if (add_mode) {
                    map.put("mode", "allow");
                    Log.i("allow", t.title);
                } else {
                    map.put("mode", "deny");
                    Log.i("deny", t.title);
                }
                map.put("title", t.title);
                map.put("secondary_title", t.description);
                map.put("count", t.usage_count);
                SearchTags.add(map);
                a_tags.notifyDataSetChanged();
            }
            dialog.dismiss();
        });
        ta_tag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ta_tag.getText().toString().length() > 0) {
                    if (Tags.getTagIdByName(s.toString()) == null) {
                        ta_tag.setError("Метки нужно выбрать из списка!");
                    } else {
                        ta_tag.setError(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ta_tag.setThreshold(1);


        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp;
            wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);
        }

        dialog.show();
    }

}


