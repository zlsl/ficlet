package zlobniyslaine.ru.ficbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import zlobniyslaine.ru.ficbook.models.Feeds;


public class ActivityFanficList extends AppCompatActivity {

    String UrlTemplate;
    String title;
    String cid = "";
    Fragment fragment_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_fanfic_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        UrlTemplate = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        cid = intent.getStringExtra("collection_id");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        Bundle bundle_list = new Bundle();

        bundle_list.putString("url", UrlTemplate);
        bundle_list.putString("collection_id", cid);
        fragment_list = new FragmentFanficList();
        fragment_list.setArguments(bundle_list);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_fragment, fragment_list)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!Application.isGuest()) {
            inflater.inflate(R.menu.ficlist, menu);
        } else {
            inflater.inflate(R.menu.ficlist_guest, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fetchall:
                if (!Application.isGuest()) {
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Application.downloadList(((FragmentFanficList) fragment_list).getFics(), false);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Скачать все фанфики в ленте?\nБудет скачано и помещено в кэш " + ((FragmentFanficList) fragment_list).getCount() + " файлов.").setPositiveButton("Да", dialogClickListener)
                            .setNegativeButton("Нет", dialogClickListener).show();

                }
                return true;

            case R.id.action_addfeed:
                try {
                    AlertDialog.Builder addFeedDialog = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();

                    View alertDialogView = inflater.inflate(R.layout.dialog_feedname, null);
                    addFeedDialog.setView(alertDialogView);

                    final TextView feed_name = alertDialogView.findViewById(R.id.feed_title);
                    feed_name.setText(title);

                    addFeedDialog.setPositiveButton("Ok", (dialog, which) -> {
                        if (!feed_name.getText().toString().isEmpty()) {
                            Feeds newFeed = new Feeds();
                            newFeed.title = feed_name.getText().toString();
                            newFeed.url = UrlTemplate;
                            newFeed.auto = "0";
                            newFeed.hash = "";
                            newFeed.save();
                            dialog.dismiss();
                        }
                    });
                    addFeedDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
