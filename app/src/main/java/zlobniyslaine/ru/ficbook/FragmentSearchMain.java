package zlobniyslaine.ru.ficbook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterAutoSuggest;
import zlobniyslaine.ru.ficbook.adapters.AdapterFandomsSearch;
import zlobniyslaine.ru.ficbook.adapters.AdapterPairings;
import zlobniyslaine.ru.ficbook.adapters.RecyclerItemClickListener;
import zlobniyslaine.ru.ficbook.controls.MultiSpinner;
import zlobniyslaine.ru.ficbook.models.Category;
import zlobniyslaine.ru.ficbook.models.Fandoms;
import zlobniyslaine.ru.ficbook.models.Tags;
import zlobniyslaine.ru.ficbook.models.TagsCategory;
import zlobniyslaine.ru.ficbook.moshis.Characters;

public class FragmentSearchMain extends Fragment {

    private ArrayList<String> array_category;
    private ArrayList<String> array_tcategory;
    private ArrayList<String> array_fandoms;
    private ArrayList<String> array_tags_ta;
    private ArrayList<HashMap<String, Object>> CharactersList;
    private ArrayList<String> character_names;
    private ArrayList<String> character_ids;

    private ArrayList<HashMap<String, Object>> SearchFandoms;
    private ArrayList<HashMap<String, Object>> PairingsAllow;
    private ArrayList<HashMap<String, Object>> PairingsDeny;

    private ArrayList<HashMap<String, Object>> SearchTags;
    private ArrayList<HashMap<String, Object>> SelectTags;

    private AdapterPairings a_pairings_allow;
    private AdapterPairings a_pairings_deny;
    private AdapterFandomsSearch a_fandoms;
    private AdapterFandomsSearch a_tags;

    protected AppCompatActivity mActivity;
    protected final FragmentSearchMain mFragment = this;

    private String fandom_id;
    private String fandom_cnt;
    private String f_id;
    private String t_id;
    private String tcat_id;
    private String tcat_name;
    private String translate_id;
    private String status_id;
    private String sort_id;
    private String rating_ids = "&ratings[]=5&ratings[]=6&ratings[]=7&ratings[]=8&ratings[]=9";
    private String size_ids = "&sizes[]=1&sizes[]=2&sizes[]=3&sizes[]=4";
    private String direction_ids = "&directions[]=1&directions[]=2&directions[]=3&directions[]=4&directions[]=7&directions[]=6&directions[]=5";

    TextView tv_searchtext;
    Spinner spinner_category_filter;
    Spinner spinner_translate;
    Spinner spinner_status;
    Spinner spinner_sort;
    Spinner spinner_search_mode;
    MultiSpinner spinner_size;
    MultiSpinner spinner_rating;
    MultiSpinner spinner_direction;
    TextView tv_pages_min;
    TextView tv_pages_max;
    TextView tv_likes_min;
    TextView tv_likes_max;
    SwitchCompat cb_readed;
    SwitchCompat cb_denyother;
    RecyclerView rv_pairings_allow;
    RecyclerView rv_pairings_deny;
    CardView cv_allow;
    CardView cv_deny;
    LinearLayout cv_group;
    CardView cv_fandoms_filter;
    RecyclerView rv_fandoms;
    RecyclerView rv_tags;
    Button btn_fandom_allow;
    Button btn_fandom_deny;


    void addCharacterAllow() {
        if (getContext() == null) return;
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Добавить персонаж");
        builder.setMultiChoiceItems(character_names.toArray(new String[0]), null, (dialog, indexSelected, isChecked) -> {
            if (isChecked) {
                selectedItems.add(indexSelected);
            } else if (selectedItems.contains(indexSelected)) {
                selectedItems.remove(Integer.valueOf(indexSelected));
            }
        }).setPositiveButton("OK", (dialog, which) -> {
            HashMap<String, Object> map;
            for (int idx : selectedItems) {

                map = new HashMap<>();
                map.put("name", character_names.get(idx));
                map.put("title", character_names.get(idx));
                map.put("character1_id", character_ids.get(idx));
                map.put("character1_name", character_names.get(idx));
                map.put("count", 1);
                if (!PairingsAllow.contains(map)) {
                    PairingsAllow.add(map);
                }
            }
            a_pairings_allow.notifyDataSetChanged();
            Objects.requireNonNull(rv_pairings_allow.getLayoutManager()).requestLayout();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void addCharacterDeny() {
        if (getContext() == null) return;
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Исключить персонаж");
        builder.setMultiChoiceItems(character_names.toArray(new String[0]), null, (dialog, indexSelected, isChecked) -> {
            if (isChecked) {
                selectedItems.add(indexSelected);
            } else if (selectedItems.contains(indexSelected)) {
                selectedItems.remove(Integer.valueOf(indexSelected));
            }
        }).setPositiveButton("OK", (dialog, which) -> {
            HashMap<String, Object> map;
            for (int idx : selectedItems) {

                map = new HashMap<>();
                map.put("name", character_names.get(idx));
                map.put("title", character_names.get(idx));
                map.put("character1_id", character_ids.get(idx));
                map.put("character1_name", character_names.get(idx));
                map.put("count", 1);
                if (!PairingsDeny.contains(map)) {
                    PairingsDeny.add(map);
                }
            }
            a_pairings_deny.notifyDataSetChanged();
            Objects.requireNonNull(rv_pairings_deny.getLayoutManager()).requestLayout();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void addPairingAllow() {
        if (getContext() == null) return;
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Добавить пэйринг");
        builder.setMultiChoiceItems(character_names.toArray(new String[0]), null, (dialog, indexSelected, isChecked) -> {
            if (isChecked) {
                selectedItems.add(indexSelected);
            } else if (selectedItems.contains(indexSelected)) {
                selectedItems.remove(Integer.valueOf(indexSelected));
            }
        }).setPositiveButton("OK", (dialog, which) -> {
            HashMap<String, Object> map = new HashMap<>();
            StringBuilder name = new StringBuilder();
            StringBuilder title = new StringBuilder();
            int i = 0;
            for (int idx : selectedItems) {
                i++;
                map.put("character" + i + "_id", character_ids.get(idx));
                map.put("character" + i + "_name", character_names.get(idx));
                if (i > 1) {
                    title.append("/");
                    name.append("---");
                }
                title.append(character_names.get(idx));
                name.append(character_names.get(idx));
            }
            map.put("name", name);
            map.put("title", title);
            map.put("count", i);

            if (!PairingsAllow.contains(map)) {
                PairingsAllow.add(map);
            }
            a_pairings_allow.notifyDataSetChanged();
            Objects.requireNonNull(rv_pairings_allow.getLayoutManager()).requestLayout();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void addPairingDeny() {
        if (getContext() == null) return;
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Исключить пэйринг");
        builder.setMultiChoiceItems(character_names.toArray(new String[0]), null, (dialog, indexSelected, isChecked) -> {
            if (isChecked) {
                selectedItems.add(indexSelected);
            } else if (selectedItems.contains(indexSelected)) {
                selectedItems.remove(Integer.valueOf(indexSelected));
            }
        }).setPositiveButton("OK", (dialog, which) -> {
            HashMap<String, Object> map = new HashMap<>();
            StringBuilder name = new StringBuilder();
            StringBuilder title = new StringBuilder();
            int i = 0;
            for (int idx : selectedItems) {
                i++;
                map.put("character" + i + "_id", character_ids.get(idx));
                map.put("character" + i + "_name", character_names.get(idx));
                if (i > 1) {
                    title.append("/");
                    name.append("---");
                }
                title.append(character_names.get(idx));
                name.append(character_names.get(idx));
            }
            map.put("name", name);
            map.put("title", title);
            map.put("count", i);

            if (!PairingsDeny.contains(map)) {
                PairingsDeny.add(map);
            }
            a_pairings_deny.notifyDataSetChanged();
            Objects.requireNonNull(rv_pairings_deny.getLayoutManager()).requestLayout();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void addAllowTag() {
        showAddTags(true);
    }

    void addDenyTag() {
        showAddTags(false);
    }

    void addAllowFandom() {
        showAddFandom(true);
    }

    void addDenyFandom() {
        showAddFandom(false);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_main, container, false);

        tv_searchtext = rootView.findViewById(R.id.tv_searchtext);
        spinner_category_filter = rootView.findViewById(R.id.spinner_category_filter);
        spinner_translate = rootView.findViewById(R.id.spinner_translate);
        spinner_status = rootView.findViewById(R.id.spinner_status);
        spinner_sort = rootView.findViewById(R.id.spinner_sort);
        spinner_search_mode = rootView.findViewById(R.id.spinner_search_mode);
        spinner_size = rootView.findViewById(R.id.spinner_size);
        spinner_rating = rootView.findViewById(R.id.spinner_rating);
        spinner_direction = rootView.findViewById(R.id.spinner_direction);
        tv_pages_min = rootView.findViewById(R.id.tv_pages_min);
        tv_pages_max = rootView.findViewById(R.id.tv_pages_max);
        tv_likes_min = rootView.findViewById(R.id.tv_likes_min);
        tv_likes_max = rootView.findViewById(R.id.tv_likes_max);
        cb_readed = rootView.findViewById(R.id.cb_readed);
        cb_denyother = rootView.findViewById(R.id.cb_denyother);
        rv_pairings_allow = rootView.findViewById(R.id.rv_pairings_allow);
        rv_pairings_deny = rootView.findViewById(R.id.rv_pairings_deny);
        cv_allow = rootView.findViewById(R.id.cv_allow);
        cv_deny = rootView.findViewById(R.id.cv_deny);
        cv_group = rootView.findViewById(R.id.cv_group);
        cv_fandoms_filter = rootView.findViewById(R.id.cv_fandoms_filter);
        rv_fandoms = rootView.findViewById(R.id.rv_fandoms);
        rv_tags = rootView.findViewById(R.id.rv_tags);
        btn_fandom_allow = rootView.findViewById(R.id.btn_fandom_allow);
        btn_fandom_deny = rootView.findViewById(R.id.btn_fandom_deny);

        rootView.findViewById(R.id.btn_char_allow).setOnClickListener(v -> addCharacterAllow());
        rootView.findViewById(R.id.btn_char_deny).setOnClickListener(v -> addCharacterDeny());
        rootView.findViewById(R.id.btn_pairing_allow).setOnClickListener(v -> addPairingAllow());
        rootView.findViewById(R.id.btn_pairing_deny).setOnClickListener(v -> addPairingDeny());
        rootView.findViewById(R.id.btn_fandom_allow).setOnClickListener(v -> addAllowFandom());
        rootView.findViewById(R.id.btn_fandom_deny).setOnClickListener(v -> addDenyFandom());
        rootView.findViewById(R.id.btn_tag_allow).setOnClickListener(v -> addAllowTag());
        rootView.findViewById(R.id.btn_tag_deny).setOnClickListener(v -> addDenyTag());

        rv_pairings_allow.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        rv_pairings_deny.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        rv_fandoms.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        rv_tags.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));

        CharactersList = new ArrayList<>();
        character_ids = new ArrayList<>();
        character_names = new ArrayList<>();
        array_fandoms = new ArrayList<>();
        array_tags_ta = new ArrayList<>();
        PairingsAllow = new ArrayList<>();
        PairingsDeny = new ArrayList<>();
        SearchFandoms = new ArrayList<>();
        SearchTags = new ArrayList<>();
        SelectTags = new ArrayList<>();

        a_pairings_allow = new AdapterPairings(getContext(), PairingsAllow);
        a_pairings_deny = new AdapterPairings(getContext(), PairingsDeny);
        a_fandoms = new AdapterFandomsSearch(getContext(), SearchFandoms, true);
        a_tags = new AdapterFandomsSearch(getContext(), SearchTags, true);

        StartLoaders();


        ((ScrollView)rootView.findViewById(R.id.scroll)).setOnTouchListener((v, event) -> {
            if (event != null && event.getAction() == MotionEvent.ACTION_MOVE) {
                InputMethodManager imm = ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
                boolean isKeyboardUp = imm.isAcceptingText();

                if (isKeyboardUp)
                {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return false;
        });

        return rootView;
    }

    private void StartLoaders() {
        cv_allow.setVisibility(View.GONE);
        cv_deny.setVisibility(View.GONE);

        rv_pairings_allow.setAdapter(a_pairings_allow);
        rv_pairings_deny.setAdapter(a_pairings_deny);
        rv_fandoms.setAdapter(a_fandoms);
        rv_tags.setAdapter(a_tags);


        ArrayAdapter<CharSequence> ad_search_mode = ArrayAdapter.createFromResource(mActivity, R.array.array_search_mode, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_search_mode.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_search_mode.setAdapter(ad_search_mode);
        spinner_search_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SearchFandoms.clear();
                PairingsAllow.clear();
                PairingsDeny.clear();
                a_fandoms.notifyDataSetChanged();
                a_pairings_allow.notifyDataSetChanged();
                a_pairings_deny.notifyDataSetChanged();

                cv_fandoms_filter.setVisibility(View.GONE);
                cv_allow.setVisibility(View.GONE);
                cv_deny.setVisibility(View.GONE);
                cv_group.setVisibility(View.GONE);
                cb_denyother.setVisibility(View.GONE);

                switch (position) {
                    case 0: //any
                        cv_fandoms_filter.setVisibility(View.VISIBLE);
                        btn_fandom_allow.setVisibility(View.GONE);
                        btn_fandom_deny.setVisibility(View.VISIBLE);
                        break;
                    case 1: //originals
                        break;
                    case 2: //all in group
                        cv_group.setVisibility(View.VISIBLE);
                        break;
                    case 3: //specified
                        cv_fandoms_filter.setVisibility(View.VISIBLE);
                        btn_fandom_allow.setVisibility(View.VISIBLE);
                        btn_fandom_deny.setVisibility(View.VISIBLE);
                        cb_denyother.setVisibility(View.VISIBLE);
                        break;
                    default:
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinner_search_mode.setSelection(3);

        List<Category> category = Category.getAll();
        array_category = new ArrayList<>();
        for (Category cx : category) {
            array_category.add(cx.name);
        }

        ArrayAdapter<String> ad_category = new ArrayAdapter<>(mActivity, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_category);
        ad_category.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        spinner_category_filter.setAdapter(ad_category);
        spinner_category_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fandom_id = Category.getIdByName(array_category.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_translate = ArrayAdapter.createFromResource(mActivity, R.array.array_translate, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_translate.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_translate.setAdapter(ad_translate);
        spinner_translate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        translate_id = "2";
                        break;
                    case 2:
                        translate_id = "3";
                        break;
                    default:
                        translate_id = "1";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_status = ArrayAdapter.createFromResource(mActivity, R.array.array_status, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_translate.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_status.setAdapter(ad_status);
        spinner_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        status_id = "1"; //"in_progress";
                        break;
                    case 2:
                        status_id = "2"; //"finished";
                        break;
                    case 3:
                        status_id = "3"; //"frozen";
                        break;
                    default:
                        status_id = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_size = ArrayAdapter.createFromResource(mActivity, R.array.array_size, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_size.setAdapter(ad_size, false, selected -> {
            size_ids = "";
            if (selected[0]) {
                size_ids = size_ids + "&sizes[]=1";
            }
            if (selected[1]) {
                size_ids = size_ids + "&sizes[]=2";
            }
            if (selected[2]) {
                size_ids = size_ids + "&sizes[]=3";
            }
            if (selected[3]) {
                size_ids = size_ids + "&sizes[]=4";
            }
        });
        boolean[] selectedItems = new boolean[ad_size.getCount()];
        selectedItems[0] = true;
        selectedItems[1] = true;
        selectedItems[2] = true;
        selectedItems[3] = true;
        spinner_size.setAllText("Все размеры");
        spinner_size.setDefaultText("Размер фанфика");
        spinner_size.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);
        spinner_size.setSelected(selectedItems);

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

        ArrayAdapter<CharSequence> ad_sort = ArrayAdapter.createFromResource(mActivity, R.array.array_sort, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_sort.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_sort.setAdapter(ad_sort);
        spinner_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        sort_id = "2"; //comments";
                        break;
                    case 2:
                        sort_id = "3"; //last_update";
                        break;
                    case 3:
                        sort_id = "4"; //a4";
                        break;
                    case 4:
                        sort_id = "5"; //random";
                        break;
                    default:
                        sort_id = "1"; //marks";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public String getUrl() {
        String tmp = "https://ficbook.net/find?p=@";

        if (tv_searchtext.getText().length() > 0) {
            try {
                tmp = tmp + "&title=" + URLEncoder.encode(tv_searchtext.getText().toString(), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        switch (((int) spinner_search_mode.getSelectedItemId())) {
            case 0: //any + exclude ids
                tmp = tmp + "&fandom_filter=any";
                break;
            case 1: //originals
                tmp = tmp + "&fandom_filter=originals";
                break;
            case 2: //all in group + exclude ids
                tmp = tmp + "&fandom_filter=group";
                tmp = tmp + "&fandom_group_id=" + fandom_id;
                break;
            case 3: //specified
                tmp = tmp + "&fandom_filter=fandom";
                int idx = -1;
                StringBuilder ch_allow = new StringBuilder();
                for (HashMap<String, Object> map : PairingsAllow) {
                    idx++;
                    for (int i = 0; i < (int) map.get("count"); i++) {
                        ch_allow.append("&pairings[").append(idx).append("][chars][]=").append(map.get("character" + (i + 1) + "_id"));
                    }
                    ch_allow.append("&pairings[").append(idx).append("][pairing]=").append(map.get("name"));
                }
/*
URLEncoder.encode(q, StandardCharsets.UTF_8);
pairings%5B0%5D%5Bchars%5D%5B%5D=
                        ch_allow.append("&pairings[").append(idx).append("][chars][]=").append(map.get("character" + (i + 1) + "_id"));
                    }
                    ch_allow.append("&pairings[").append(idx).append("][pairing]=").append(map.get("name"));

                        ch_allow.append("&pairings%5B").append(idx).append("%5D%5Bchars%5D%5B%5D=").append(map.get("character" + (i + 1) + "_id"));
                    }
                    ch_allow.append("&pairings%5B").append(idx).append("%5D%5Bpairing%5D=").append(map.get("name"));


 */

                idx = -1;
                StringBuilder ch_deny = new StringBuilder();
                for (HashMap<String, Object> map : PairingsDeny) {
                    idx++;
                    Log.i("DX", Objects.requireNonNull(map.get("count")).toString());
                    for (int i = 0; i < (int) map.get("count"); i++) {
                        ch_deny.append("&pairings_exclude[").append(idx).append("][chars][]=").append(map.get("character" + (i + 1) + "_id"));
                    }
                    ch_deny.append("&pairings_exclude[").append(idx).append("][pairing]=").append(map.get("name"));
                }
                tmp = tmp + ch_allow.toString() + ch_deny.toString();

                if (cb_denyother.isChecked()) {
                    tmp = tmp + "&deny_other=1";
                }
                break;
            default:
        }

        try {
            StringBuilder fids = new StringBuilder();

            for (HashMap<String, Object> map : SearchFandoms) {
                if (Objects.requireNonNull(map.get("mode")).equals("allow")) {
                    fids.append("&fandom_ids[]=").append(map.get("id"));
                } else {
                    fids.append("&fandom_exclude_ids[]=").append(map.get("id"));
                }
            }

            StringBuilder tids = new StringBuilder();

            for (HashMap<String, Object> map : SearchTags) {
                if (Objects.requireNonNull(map.get("mode")).equals("allow")) {
                    tids.append("&tags_include[]=").append(map.get("id"));
                } else {
                    tids.append("&tags_exclude[]=").append(map.get("id"));
                }
            }

            tmp = tmp + fids;
            tmp = tmp + tids;

            tmp = tmp + size_ids;
            tmp = tmp + "&pages_min=" + tv_pages_min.getText() + "&pages_max=" + tv_pages_max.getText();
            tmp = tmp + rating_ids;
            tmp = tmp + "&transl=" + translate_id;

            switch (status_id) {
                case "1":
                    tmp = tmp + "&statuses[]=1";
                    break;
                case "2":
                    tmp = tmp + "&statuses[]=2";
                    break;
                case "3":
                    tmp = tmp + "&statuses[]=3";
                    break;
                default:
                    tmp = tmp + "&statuses[]=1&statuses[]=2&statuses[]=3";
            }

            tmp = tmp + "&status=" + status_id;

            tmp = tmp + direction_ids;
            tmp = tmp + "&likes_min=" + tv_likes_min.getText() + "&likes_max=" + tv_likes_max.getText();
            tmp = tmp + "&sort=" + sort_id;

            tmp = tmp + "&date=0";

            if (cb_readed.isChecked()) {
                tmp = tmp + "&filter_readed=1";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Random r = new Random();
        tmp = tmp + "&rnd=" + r.nextInt() + "&find=Find";

        return tmp;
    }

    static class fetcher_characters extends AsyncTask<String, Void, Void> {
        private final WeakReference<FragmentSearchMain> activityReference;

        fetcher_characters(FragmentSearchMain context) {
            activityReference = new WeakReference<>(context);
        }

        Response response;
        ResponseBody body;
        String bodyx = "";

        @Override
        protected Void doInBackground(String... params) {
            try {
                FormBody.Builder b = new FormBody.Builder();

                StringBuilder deb = new StringBuilder();

                for (HashMap<String, Object> map : activityReference.get().SearchFandoms) {
                    b.add("fandomIds[]", Objects.requireNonNull(map.get("id")).toString());
                    deb.append(Objects.requireNonNull(map.get("id")).toString()).append(", ");
                }
                Log.i("FETCHCHAR", "begin for id " + deb);
                RequestBody formBody = b.build();
                response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/ajax/fandoms/characters", formBody)).execute();

                body = response.body();
                if (body != null) {
                    bodyx = body.string();
                    response.close();
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (activityReference.get() != null) {
                    activityReference.get().ParseCharacterList(bodyx);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseCharacterList(String json) {
        Log.d("PARSECHAR", "begin");
        List<Characters> characters;
        CharactersList.clear();
        HashMap<String, Object> map;
        character_ids.clear();
        character_names.clear();

        map = new HashMap<>();
        map.put("name", "ОМП");
        map.put("id", "-1");
        CharactersList.add(map);
        map = new HashMap<>();
        map.put("name", "ОЖП");
        map.put("id", "-2");
        CharactersList.add(map);

        try {
            Moshi moshi = new Moshi.Builder().build();
            Type type = Types.newParameterizedType(List.class, Characters.class);
            JsonAdapter<List<Characters>> adapter = moshi.adapter(type);
            characters = adapter.fromJson(json);

            if (characters != null) {
                for (int i = 0; i < characters.size(); i++) {
                    Log.i("CHARS", "size: " + characters.get(i).id + " " + characters.get(i).title);
                    for (int x = 0; x < characters.get(i).chars.size(); x++) {
                        map = new HashMap<>();
                        map.put("name", characters.get(i).chars.get(x).name);
                        map.put("id", characters.get(i).chars.get(x).id.toString());
                        CharactersList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < CharactersList.size(); i++) {
                try {
                    character_names.add(Objects.requireNonNull(CharactersList.get(i).get("name")).toString());
                    character_ids.add(Objects.requireNonNull(CharactersList.get(i).get("id")).toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cv_allow.setVisibility(View.VISIBLE);
            cv_deny.setVisibility(View.VISIBLE);
        }
        Log.i("PARSECHAR", "end");
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
            } else {
                Log.e("FANDOM", "select NULL");
            }

            PairingsAllow.clear();
            PairingsDeny.clear();
            cv_allow.setVisibility(View.GONE);
            cv_deny.setVisibility(View.GONE);
            a_pairings_allow.notifyDataSetChanged();
            a_pairings_deny.notifyDataSetChanged();
            new fetcher_characters(mFragment).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
                    Log.i("TAG allow", t.title);
                } else {
                    map.put("mode", "deny");
                    Log.i("TAG deny", t.title);
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
                    try {
                        t_id = Objects.requireNonNull(SelectTags.get(position).get("id")).toString();
                        HashMap<String, Object> map = new HashMap<>();
                        Tags t = Tags.getById(t_id);

                        if (t != null) {
                            map.put("id", t.nid);
                            if (add_mode) {
                                map.put("mode", "allow");
                                Log.i("TAG _allow", t.title);
                            } else {
                                map.put("mode", "deny");
                                Log.i("TAG _deny", t.title);
                            }
                            map.put("title", t.title);
                            map.put("secondary_title", t.description);
                            map.put("count", t.usage_count);
                            SearchTags.add(map);
                            a_tags.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
                    Log.i("TAG __allow", t.title);
                } else {
                    map.put("mode", "deny");
                    Log.i("TAG __deny", t.title);
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