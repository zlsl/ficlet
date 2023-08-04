package zlobniyslaine.ru.ficbook;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;


public class ActivityGlobalFilter extends AppCompatActivity {

    private SwitchCompat cbFilterGen;
    private SwitchCompat cbFilterHet;
    private SwitchCompat cbFilterOther;
    private SwitchCompat cbFilterSlash;
    private SwitchCompat cbFilterFemslash;
    private SwitchCompat cbFilterMixed;
    private SwitchCompat cbFilterArticle;
    private SwitchCompat cbFilterReaded;
    private SwitchCompat cbFilterCrit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_global_filter);

        cbFilterGen = findViewById(R.id.cb_filter_gen);
        cbFilterHet = findViewById(R.id.cb_filter_het);
        cbFilterOther = findViewById(R.id.cb_filter_other);
        cbFilterSlash = findViewById(R.id.cb_filter_slash);
        cbFilterFemslash = findViewById(R.id.cb_filter_femslash);
        cbFilterMixed = findViewById(R.id.cb_filter_mixed);
        cbFilterArticle = findViewById(R.id.cb_filter_article);
        cbFilterReaded = findViewById(R.id.cb_filter_readed);
        cbFilterCrit = findViewById(R.id.cb_filter_crit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cbFilterGen.setChecked(Application.sPref.getBoolean("filter_gen", false));
        cbFilterHet.setChecked(Application.sPref.getBoolean("filter_het", false));
        cbFilterOther.setChecked(Application.sPref.getBoolean("filter_other", false));
        cbFilterSlash.setChecked(Application.sPref.getBoolean("filter_slash", false));
        cbFilterFemslash.setChecked(Application.sPref.getBoolean("filter_femslash", false));
        cbFilterMixed.setChecked(Application.sPref.getBoolean("filter_mixed", false));
        cbFilterArticle.setChecked(Application.sPref.getBoolean("filter_article", false));
        cbFilterReaded.setChecked(Application.sPref.getBoolean("filter_readed", false));
        cbFilterCrit.setChecked(Application.sPref.getBoolean("filter_crit", false));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putBoolean("filter_gen", cbFilterGen.isChecked());
        ed.putBoolean("filter_het", cbFilterHet.isChecked());
        ed.putBoolean("filter_other", cbFilterOther.isChecked());
        ed.putBoolean("filter_slash", cbFilterSlash.isChecked());
        ed.putBoolean("filter_femslash", cbFilterFemslash.isChecked());
        ed.putBoolean("filter_mixed", cbFilterMixed.isChecked());
        ed.putBoolean("filter_article", cbFilterArticle.isChecked());
        ed.putBoolean("filter_readed", cbFilterReaded.isChecked());
        ed.putBoolean("filter_crit", cbFilterCrit.isChecked());
        ed.apply();
        super.onPause();
    }
}
