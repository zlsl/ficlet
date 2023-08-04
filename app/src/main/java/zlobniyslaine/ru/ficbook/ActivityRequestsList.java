package zlobniyslaine.ru.ficbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class ActivityRequestsList extends AppCompatActivity {

    String UrlTemplate;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_requests_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UrlTemplate = intent.getStringExtra("url");
        title = intent.getStringExtra("title");


        if (UrlTemplate.contains("liked_requests")) {
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.remove("notify_icon-lamp8");
            ed.apply();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        Bundle bundle_list = new Bundle();

        bundle_list.putString("url", UrlTemplate);
        Fragment fragment_list = new FragmentRequests_List();
        fragment_list.setArguments(bundle_list);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_fragment, fragment_list)
                .commit();
    }
}
