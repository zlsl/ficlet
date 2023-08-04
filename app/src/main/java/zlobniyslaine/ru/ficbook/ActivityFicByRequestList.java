package zlobniyslaine.ru.ficbook;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ActivityFicByRequestList extends AppCompatActivity {

    String UrlTemplate;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_ficbyrequestlist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UrlTemplate = intent.getStringExtra("url");
        title = intent.getStringExtra("title");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        Bundle bundle_list = new Bundle();

        bundle_list.putString("url", UrlTemplate);
        Fragment fragment_list = new FragmentFicByReqList();
        fragment_list.setArguments(bundle_list);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_fragment, fragment_list)
                .commit();
    }
}
