package zlobniyslaine.ru.ficbook;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import zlobniyslaine.ru.ficbook.adapters.AdapterNotifications;


@SuppressWarnings("WeakerAccess")
public class ActivityNotifications extends AppCompatActivity {

    RecyclerView rv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_notifications);

        rv1 = findViewById(R.id.rv1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<HashMap<String, Object>> notificationList = new ArrayList<>();

        if (Application.sPref.contains("notify_icon-star-empty")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", Application.sPref.getString("notify_icon-star-empty", ""));
            map.put("type", Application.NOTIFICATION_FAVOURITES);
            notificationList.add(map);
        }
        if (Application.sPref.contains("notify_icon-history")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", Application.sPref.getString("notify_icon-history", ""));
            map.put("type", Application.NOTIFICATION_PARTICIPATED);
            notificationList.add(map);
        }
        if (Application.sPref.contains("notify_icon-envelop3")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", Application.sPref.getString("notify_icon-envelop3", ""));
            map.put("type", Application.NOTIFICATION_DIALOG);
            notificationList.add(map);
        }
        if (Application.sPref.contains("notify_icon-stack-plus")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", Application.sPref.getString("notify_icon-stack-plus", ""));
            map.put("type", Application.NOTIFICATION_NEW_PART);
            notificationList.add(map);
        }
        if (Application.sPref.contains("notify_icon-lamp8")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", Application.sPref.getString("notify_icon-lamp8", ""));
            map.put("type", Application.NOTIFICATION_FANFIC_BY_INT);
            notificationList.add(map);
        }
        if (Application.sPref.contains("notify_icon-newspaper")) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("text", Application.sPref.getString("notify_icon-newspaper", ""));
            map.put("type", Application.NOTIFICATION_NEWS);
            notificationList.add(map);
        }

        rv1.setHasFixedSize(true);
        rv1.setLayoutManager(new WrapContentLinearLayoutManager(this));
        AdapterNotifications rv_adapter = new AdapterNotifications(this, notificationList);
        rv1.setAdapter(rv_adapter);
    }
}
