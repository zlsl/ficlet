package zlobniyslaine.ru.ficbook;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterAutoSuggest;
import zlobniyslaine.ru.ficbook.adapters.AdapterFandomsSearch;
import zlobniyslaine.ru.ficbook.adapters.AdapterPairings;
import zlobniyslaine.ru.ficbook.models.Category;
import zlobniyslaine.ru.ficbook.models.Fandoms;
import zlobniyslaine.ru.ficbook.moshis.Characters;


@SuppressWarnings("WeakerAccess")
public class ActivityFanficNew extends AppCompatActivity {

    private Context context;
    private String fandom_id;
    private String f_id;
    private String rating_id = "5";
    private String size_id = "1";
    private String direction_id = "1";
    private FormBody formBody;

    private ArrayList<String> array_category;
    private ArrayList<String> array_fandoms;
    private ArrayList<HashMap<String, Object>> CharactersList;
    private ArrayList<String> character_names;
    private ArrayList<String> character_ids;

    private ArrayList<HashMap<String, Object>> FicFandoms;
    private ArrayList<HashMap<String, Object>> Pairings;

    private AdapterPairings a_pairings;
    private AdapterFandomsSearch a_fandoms;

    FloatingActionButton fab;
    Spinner spinner_type;
    Spinner spinner_fanfom_type;
    LinearLayout l_realauthor;
    LinearLayout l_fandom;
    LinearLayout l_original;
    Spinner spinner_size;
    Spinner spinner_rating;
    Spinner spinner_direction;
    RecyclerView rv_pairings;
    RecyclerView rv_fandoms;
    TextView e_real_author;
    TextView e_original_ink;
    TextView e_pairings;
    TextView e_title;
    TextView e_description;
    TextView e_comments;
    TextView e_belong;
    Button btn_char;
    Button btn_fandom_allow;
    Button btn_pairing;

    void createFanfic() {
        FormBody.Builder bodyBuilder = new FormBody.Builder();

        bodyBuilder
                .add("title", e_title.getText().toString())

                .add("continue_enable", "1") // показывать кнопку «жду продолжения!»
                .add("continue_required_votes", "0") // необходимые голоса для написания продолжения 0 - dont show
                .add("enable_comments", "1") // Включить отзывы
                .add("enable_marks", "1") // Включить оценки «Нравится»
                .add("publication", "1") // allow on other and etc
                .add("dedication", e_belong.getText().toString()) //
                .add("author_comment", e_comments.getText().toString()) //
                .add("description", e_description.getText().toString()) //

                .add("public_beta", "0") // public_beta: 0 - dis, 1 - on all, 2 - on registered

                .add("size", size_id)
                .add("rating", rating_id)
                .add("direction", direction_id)

                .add("agree", "1");

        switch (spinner_type.getSelectedItemPosition()) {
            case 0:
                bodyBuilder.add("fic_type", "author_fic");
                break;
            case 1:
                bodyBuilder.add("fic_type", "translation");
                bodyBuilder.add("real_author", e_real_author.getText().toString());
                bodyBuilder.add("original_link", e_original_ink.getText().toString());
                break;
        }

        switch (spinner_fanfom_type.getSelectedItemPosition()) {
            case 0:
                bodyBuilder.add("fandom_type", "original");
                bodyBuilder.add("pairing", e_pairings.getText().toString());
                break;
            case 1:
                bodyBuilder.add("fandom_type", "fandom");
                bodyBuilder.add("pairing", "");
                for (HashMap<String, Object> map : FicFandoms) {
                    bodyBuilder.add("fandom_ids[]", Objects.requireNonNull(map.get("id")).toString());
                }
                int idx = -1;
                for (HashMap<String, Object> map : Pairings) {
                    idx++;
                    for (int i = 0; i < (int) map.get("count"); i++) {
                        bodyBuilder.add("pairings[" + idx + "][chars][]", Objects.requireNonNull(map.get("character" + (i + 1) + "_id")).toString());
                    }
                    bodyBuilder.add("pairings[" + idx + "][pairing]", Objects.requireNonNull(map.get("name")).toString());
                }
                break;
        }

        formBody = bodyBuilder.build();

        new new_fanfic((ActivityFanficNew) context).execute();
    }

    void addCharacter() {
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                if (!Pairings.contains(map)) {
                    Pairings.add(map);
                }
            }
            a_pairings.notifyDataSetChanged();
            Objects.requireNonNull(rv_pairings.getLayoutManager()).requestLayout();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    void showAddFandom() {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
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

        ArrayAdapter<String> ad_category = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_category);
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
                    ta_fandom.setAdapter(new AdapterAutoSuggest(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, array_fandoms));
                    ta_fandom.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ta_fandom.setAdapter(new AdapterAutoSuggest(this, R.layout.item_fandom_list, array_fandoms));
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
                map.put("mode", "allow");
                map.put("title", f.title);
                map.put("secondary_title", f.sec_title);
                map.put("count", f.count);
                FicFandoms.add(map);
                a_fandoms.notifyDataSetChanged();
            }

            Pairings.clear();
            a_pairings.notifyDataSetChanged();
            new fetcher_characters((ActivityFanficNew) context).execute();

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

    void addPairing() {
        final ArrayList<Integer> selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

            if (!Pairings.contains(map)) {
                Pairings.add(map);
            }
            a_pairings.notifyDataSetChanged();
            Objects.requireNonNull(rv_pairings.getLayoutManager()).requestLayout();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_fanfic_new);

        fab = findViewById(R.id.fab);
        spinner_type = findViewById(R.id.spinner_type);
        spinner_fanfom_type = findViewById(R.id.spinner_fandom_type);
        spinner_size = findViewById(R.id.spinner_size);
        spinner_rating = findViewById(R.id.spinner_rating);
        spinner_direction = findViewById(R.id.spinner_direction);
        l_realauthor = findViewById(R.id.l_realauthor);
        l_fandom = findViewById(R.id.l_fandom);
        l_original = findViewById(R.id.l_original);
        rv_pairings = findViewById(R.id.rv_pairings);
        rv_fandoms = findViewById(R.id.rv_fandoms);
        e_real_author = findViewById(R.id.e_real_author);
        e_original_ink = findViewById(R.id.e_original_ink);
        e_pairings = findViewById(R.id.e_pairings);
        e_title = findViewById(R.id.e_title);
        e_description = findViewById(R.id.e_description);
        e_comments = findViewById(R.id.e_comments);
        e_belong = findViewById(R.id.e_belong);
        btn_char = findViewById(R.id.btn_char);
        btn_fandom_allow = findViewById(R.id.btn_fandom_allow);
        btn_pairing = findViewById(R.id.btn_pairing);

        fab.setOnClickListener(v -> createFanfic());
        btn_char.setOnClickListener(v -> addCharacter());
        btn_fandom_allow.setOnClickListener(v -> showAddFandom());
        btn_pairing.setOnClickListener(v -> addPairing());

        context = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CharactersList = new ArrayList<>();
        character_ids = new ArrayList<>();
        character_names = new ArrayList<>();
        array_fandoms = new ArrayList<>();
        Pairings = new ArrayList<>();
        FicFandoms = new ArrayList<>();

        rv_pairings.setLayoutManager(new WrapContentLinearLayoutManager(this));
        rv_fandoms.setLayoutManager(new WrapContentLinearLayoutManager(this));

        a_pairings = new AdapterPairings(this, Pairings);
        rv_pairings.setAdapter(a_pairings);

        a_fandoms = new AdapterFandomsSearch(this, FicFandoms, true);
        rv_fandoms.setAdapter(a_fandoms);

        ArrayAdapter<CharSequence> ad_fanfic_type = ArrayAdapter.createFromResource(context, R.array.array_fanfic_type, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_fanfic_type.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_type.setAdapter(ad_fanfic_type);
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        l_realauthor.setVisibility(View.GONE);
                        break;
                    case 1:
                        l_realauthor.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_fandom_type = ArrayAdapter.createFromResource(context, R.array.array_fandom_type, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_fandom_type.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_fanfom_type.setAdapter(ad_fandom_type);
        spinner_fanfom_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        l_fandom.setVisibility(View.GONE);
                        l_original.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        l_fandom.setVisibility(View.VISIBLE);
                        l_original.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_size = ArrayAdapter.createFromResource(context, R.array.array_size, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_size.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_size.setAdapter(ad_size);
        spinner_size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        size_id = "1";
                        break;
                    case 1:
                        size_id = "2";
                        break;
                    case 2:
                        size_id = "3";
                        break;
                    case 3:
                        size_id = "4";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_rating = ArrayAdapter.createFromResource(context, R.array.array_rating, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_rating.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_rating.setAdapter(ad_rating);
        spinner_rating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        rating_id = "5";
                        break;
                    case 1:
                        rating_id = "6";
                        break;
                    case 2:
                        rating_id = "7";
                        break;
                    case 3:
                        rating_id = "8";
                        break;
                    case 4:
                        rating_id = "9";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayAdapter<CharSequence> ad_direction = ArrayAdapter.createFromResource(context, R.array.array_direction, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_direction.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_direction.setAdapter(ad_direction);
        spinner_direction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        direction_id = "1";
                        break;
                    case 1:
                        direction_id = "2";
                        break;
                    case 2:
                        direction_id = "3";
                        break;
                    case 3:
                        direction_id = "4";
                        break;
                    case 4:
                        direction_id = "7";
                        break;
                    case 5:
                        direction_id = "6";
                        break;
                    case 6:
                        direction_id = "5";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    static class fetcher_characters extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityFanficNew> activityReference;

        fetcher_characters(ActivityFanficNew context) {
            activityReference = new WeakReference<>(context);
        }

        Response response;
        ResponseBody body;
        String bodyx = "";

        @Override
        protected Void doInBackground(String... params) {
            try {

                FormBody.Builder b = new FormBody.Builder();

                for (HashMap<String, Object> map : activityReference.get().FicFandoms) {
                    b.add("fandomIds[]", Objects.requireNonNull(map.get("id")).toString());
                }

                RequestBody formBody = b.build();
                response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/ajax/fandoms/characters", formBody)).execute();

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
            try {
                if (activityReference.get() != null) {
                    activityReference.get().ParseCharacterList(bodyx);
                }
            } catch (Exception e) {
                Log.e("E", e.toString());
                e.printStackTrace();
            }
            Application.firePopup();
        }
    }

    private void ParseCharacterList(String json) {
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
                    for (int x = 0; x < characters.get(i).chars.size(); x++) {
                        map = new HashMap<>();
                        map.put("name", characters.get(i).chars.get(x).name);
                        map.put("id", characters.get(i).chars.get(x).id.toString());
                        CharactersList.add(map);
                    }
                }
            }

            for (int i = 0; i < CharactersList.size(); i++) {
                character_names.add(Objects.requireNonNull(CharactersList.get(i).get("name")).toString());
                character_ids.add(Objects.requireNonNull(CharactersList.get(i).get("id")).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class new_fanfic extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityFanficNew> activityReference;

        new_fanfic(ActivityFanficNew context) {
            activityReference = new WeakReference<>(context);
        }

        private String data = "";

        @Override
        protected Void doInBackground(String... params) {

            try {

/*
beta_ids_can_edit[]: 2154522
beta_ids_all[]: 2154522
beta_ids_can_edit[]: 1925988
beta_ids_all[]: 1925988

coauthor_ids_can_edit[]: 2154522
coauthor_ids_all[]: 2154522

https://ficbook.net/home/myfics/6613667/delete
*/

                Response response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/home/addfic_save", activityReference.get().formBody)).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    data = body.string();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                Log.i("Save", data);
                if (data.contains("true")) {
                    //{"result":true,"data":{"redirect":"\/home\/myfics\/6628503\/addpart"}}
                    activityReference.get().finish();
                } else {
                    data = data.replace("{\"result\":false,\"error\":{", "").replace("}}", "").replace("\",\"", "\"\n\"");
                    Toast.makeText(Application.getContext(), YO.unescape(data), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
