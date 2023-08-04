package zlobniyslaine.ru.ficbook;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import zlobniyslaine.ru.ficbook.databinding.ActivityAboutBinding;

public class ActivityAbout extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);

        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.tvFicletTitle.setText(getString(R.string.ficlet_v, pInfo.versionName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.tvFicletTitle.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityDebug.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
