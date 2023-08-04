package zlobniyslaine.ru.ficbook;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.speech.tts.Voice;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;

import zlobniyslaine.ru.ficbook.controls.FloatSeekBar;

@SuppressLint("ObsoleteSdkInt")
public class ActivitySettings extends AppCompatActivity {

    SwitchCompat cb_brightness;
    SeekBar sb_textsize;
    SwitchCompat cb_night_mode;
    SwitchCompat cb_show_covers;
    SwitchCompat cb_search_sub;
    SwitchCompat cb_fullui;
    SwitchCompat cb_fingerprint;
    SwitchCompat cb_rev_parts;
    SwitchCompat cb_smartreaded;
    TextView tv_sampletext;
    TextView tv_fontsize;
    Spinner spinner_collections_sort;
    Spinner spinner_voices;
    FloatSeekBar sb_speech_rate;
    FloatSeekBar sb_speech_pitch;
    TextView tv_speech_rate;
    TextView tv_speech_pitch;
    Spinner spinner_fontscale;
    Spinner spinner_engines;
    Spinner spinner_typeface;
    TextView tv_yowarning;
    SwitchCompat cb_yofix;
    SwitchCompat cb_typograf;

    final int REQUEST_DIRECTORY = 515;

    void resetRate() {
        sb_speech_rate.setValue(1f);
    }

    void resetPitch() {
        sb_speech_pitch.setValue(1f);
    }

    void playSampleVoice() {
        Application.stopSpeak();
        Application.speakText(tv_sampletext.getText().toString());
    }

    private int old_font_size;
    private int old_typeface;
    private int font_scale;
    private int typeface;
    private Boolean old_brightness;
    private String csort_id = "";
    private ArrayList<String> array_voices;
    List<String> listInstalledEnginesName;
    String ttsEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.adjustFontScale(this);
        setContentView(R.layout.activity_settings);

        cb_brightness = findViewById(R.id.cb_brightness);
        cb_show_covers = findViewById(R.id.cb_showcovers);
        cb_fingerprint = findViewById(R.id.cb_fingerprint);
        sb_textsize = findViewById(R.id.sb_textsize);
        cb_night_mode = findViewById(R.id.cb_night_mode);
        cb_search_sub = findViewById(R.id.cb_search_sub);
        cb_rev_parts = findViewById(R.id.cb_rev_part_list);
        cb_fullui = findViewById(R.id.cb_fullui);
        cb_smartreaded = findViewById(R.id.cb_smartreaded);
        tv_sampletext = findViewById(R.id.tv_sampletext);
        tv_fontsize = findViewById(R.id.tv_fontsize);
        spinner_collections_sort = findViewById(R.id.spinner_collections_sort);
        spinner_voices = findViewById(R.id.spinner_voices);
        spinner_engines = findViewById(R.id.spinner_engines);

        sb_speech_rate = findViewById(R.id.sb_speech_rate);
        sb_speech_pitch = findViewById(R.id.sb_speech_pitch);
        tv_speech_rate = findViewById(R.id.tv_speech_rate);
        tv_speech_pitch = findViewById(R.id.tv_speech_pitch);
        spinner_fontscale = findViewById(R.id.spinner_font_scale);
        spinner_typeface = findViewById(R.id.spinner_typeface);

        tv_yowarning = findViewById(R.id.tv_yowarning);
        cb_yofix = findViewById(R.id.cb_yofix);
        cb_typograf = findViewById(R.id.cb_typograf);

        tv_speech_rate.setOnClickListener(v -> resetRate());
        tv_speech_pitch.setOnClickListener(v -> resetPitch());
        tv_sampletext.setOnClickListener(v -> playSampleVoice());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        csort_id = Application.sPref.getString("collections_sort_mode", "updated");
        old_font_size = Application.sPref.getInt("font_size", 15);
        old_brightness = Application.sPref.getBoolean("brightness", false);
        old_typeface = Application.sPref.getInt("typeface_id", 0);
        try {
            font_scale = Application.sPref.getInt("font_scale", 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        typeface = old_typeface;
        cb_show_covers.setChecked(Application.sPref.getBoolean("show_covers", false));
        cb_fingerprint.setChecked(Application.sPref.getBoolean("fingerprint", false));
        cb_brightness.setChecked(Application.sPref.getBoolean("brightness", true));
        cb_night_mode.setChecked(Application.sPref.getBoolean("night_mode", false));
        cb_rev_parts.setChecked(Application.sPref.getBoolean("reverse_part_list", false));
        cb_search_sub.setChecked(Application.sPref.getBoolean("search_sub", true));
        cb_yofix.setChecked(Application.sPref.getBoolean("yo_fix", false));
        cb_smartreaded.setChecked(Application.sPref.getBoolean("smart_readed", false));
        cb_typograf.setChecked(Application.sPref.getBoolean("typograf", true));
        cb_fullui.setChecked(Application.sPref.getBoolean("fanfic_fullui", true));
        sb_textsize.setProgress(old_font_size);
        tv_sampletext.setTextSize(TypedValue.COMPLEX_UNIT_SP, old_font_size);
        tv_fontsize.setText(String.valueOf(old_font_size));

        sb_speech_rate.setValue(Application.sPref.getFloat("speech_rate", 1f));
        tv_speech_rate.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_rate.getValue() * 100)));
        sb_speech_pitch.setValue(Application.sPref.getFloat("speech_pitch", 1f));
        tv_speech_pitch.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_pitch.getValue() * 100)));

        initWarning();

        listInstalledEnginesName = new ArrayList<>();

        if (Application.tts == null) {
            Application.initTTS();
        }

        ttsEngine = Application.getTTSEngine();
        try {
            Log.i("Default TTS", Objects.requireNonNull(Application.getDefaultTTSEngine()));
            Log.i("Current TTS", ttsEngine);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int tts_idx = 0;
        try {
            for (int i = 0; i < Application.getTtsEngines().size(); i++) {
                listInstalledEnginesName.add(Application.getTtsEngines().get(i).label);
                Log.i("TTS E", Application.getTtsEngines().get(i).name + " (" + Application.getTtsEngines().get(i).label + ")");
                if (ttsEngine.equals(Application.getTtsEngines().get(i).name)) {
                    tts_idx = i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listInstalledEnginesName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_engines.setAdapter(adapter);
        spinner_engines.setSelection(tts_idx);
        spinner_engines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Application.stopSpeak();
                Application.setTTSEngine(Application.getTtsEngines().get(position).name);
                Log.i("Setting", Application.getTtsEngines().get(position).name);
                final Handler handler = new Handler();
                handler.postDelayed(() -> fillVoices(), 1000);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_voices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Application.stopSpeak();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Application.tts.getVoices() != null) {
                        if (Application.tts.getVoices().size() > 0) {
                            for (Voice tmpVoice : Application.tts.getVoices()) {
                                if (tmpVoice.getName().equals(array_voices.get(position))) {
                                    Application.tts.setVoice(tmpVoice);
                                    SharedPreferences.Editor ed = Application.sPref.edit();
                                    ed.putString("voice", tmpVoice.getName());
                                    ed.apply();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        fillVoices();

        cb_yofix.setOnClickListener(view -> initWarning());

        sb_textsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_sampletext.setTextSize(progress);
                tv_fontsize.setText(String.valueOf(progress));
                switch (typeface) {
                    case 1:
                        tv_sampletext.setTypeface(Typeface.SERIF);
                        break;
                    case 0:
                    default:
                        tv_sampletext.setTypeface(Typeface.SANS_SERIF);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_speech_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_speech_rate.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_rate.getValue() * 100)));
                Application.tts.setSpeechRate(sb_speech_rate.getValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_speech_pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_speech_pitch.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_pitch.getValue() * 100)));
                Application.tts.setPitch(sb_speech_pitch.getValue());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ArrayAdapter<CharSequence> ad_fscale = ArrayAdapter.createFromResource(this, R.array.array_font_scale, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_fscale.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_fontscale.setAdapter(ad_fscale);
        spinner_fontscale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                font_scale = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinner_fontscale.setSelection(font_scale);

        ArrayAdapter<CharSequence> ad_csort = ArrayAdapter.createFromResource(this, R.array.array_collection_sort_mode, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_csort.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_collections_sort.setAdapter(ad_csort);
        spinner_collections_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        csort_id = "updated";
                        break;
                    case 2:
                        csort_id = "added";
                        break;
                    default:
                        csort_id = "author";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        switch (csort_id) {
            case "updated":
                spinner_collections_sort.setSelection(1);
                break;
            case "added":
                spinner_collections_sort.setSelection(2);
                break;
            default:
                spinner_collections_sort.setSelection(0);
        }

        ArrayAdapter<CharSequence> ad_typeface = ArrayAdapter.createFromResource(this, R.array.array_typefaces, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_typeface.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_typeface.setAdapter(ad_typeface);
        spinner_typeface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeface = position;
                switch (typeface) {
                    case 1:
                        tv_sampletext.setTypeface(Typeface.SERIF);
                        break;
                    case 0:
                    default:
                        tv_sampletext.setTypeface(Typeface.SANS_SERIF);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        cb_fingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> bio(isChecked));
    }

    private void fillVoices() {
        spinner_voices.setVisibility(View.GONE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int idx = -1;
                int i = 0;
                array_voices = new ArrayList<>();
                if (Application.tts.getVoices() != null) {
                    for (Voice tmpVoice : Application.tts.getVoices()) {
                        if (tmpVoice.getName().contains("ru")) {
                            Log.d("VOICE", tmpVoice.toString());
                            array_voices.add(tmpVoice.getName());
                            if (tmpVoice.getName().equals(Application.sPref.getString("voice", ""))) {
                                idx = i;
                            }
                            i++;
                        }
                    }
                    if (idx > -1) {
                        spinner_voices.setSelection(idx);
                    }
                    if (Application.tts.getVoices().size() > 0) {
                        spinner_voices.setVisibility(View.VISIBLE);
                    } else {
                        Log.w("TTS", "no voices");
                    }
                }
                ArrayAdapter<String> ad_voices = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_voices);
                ad_voices.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                spinner_voices.setAdapter(ad_voices);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        Application.stopSpeak();
        SharedPreferences.Editor ed = Application.sPref.edit();
        ed.putBoolean("brightness", cb_brightness.isChecked());
        ed.putBoolean("night_mode", cb_night_mode.isChecked());
        ed.putBoolean("search_sub", cb_search_sub.isChecked());
        ed.putBoolean("fanfic_fullui", cb_fullui.isChecked());
        ed.putBoolean("yo_fix", cb_yofix.isChecked());
        ed.putBoolean("smart_readed", cb_smartreaded.isChecked());
        ed.putBoolean("typograf", cb_typograf.isChecked());
        ed.putBoolean("reverse_part_list", cb_rev_parts.isChecked());
        ed.putBoolean("show_covers", cb_show_covers.isChecked());

        ed.putInt("font_size", sb_textsize.getProgress());
        ed.putInt("typeface_id", typeface);
        ed.putInt("font_scale", font_scale);

        if (old_brightness != cb_brightness.isChecked()) {
            ed.putFloat("brightness_level", 0.8f);
        }

        if ( (old_typeface != typeface) || (old_font_size != sb_textsize.getProgress()) ) {
            File[] files = getFilesDir().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(".pagesz")) {
                        if (!file.delete()) {
                            Log.w("TMP", "delete failed");
                        }
                    }
                }
            }
        }

        ed.putString("collections_sort_mode", csort_id);
        ed.putFloat("speech_rate", sb_speech_rate.getValue());
        ed.putFloat("speech_pitch", sb_speech_pitch.getValue());
        ed.putString("voice_engine", Application.getTTSEngine());
        ed.apply();

        if (cb_yofix.isChecked()) {
            YO.init();
        }
        super.onPause();
    }

    private void initWarning() {
        if (cb_yofix.isChecked()) {
            tv_yowarning.setVisibility(View.VISIBLE);
        } else {
            tv_yowarning.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == REQUEST_DIRECTORY && resultCode == Activity.RESULT_OK) {
            Uri uri = Uri.parse(Objects.requireNonNull(resultData.getData()).toString());
            Log.i("URI", Objects.requireNonNull(getPath(this, uri)));
        }
    }

    public static String getPath(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }
            }
        }
        return null;
    }

    private void bio(Boolean status) {
        SharedPreferences.Editor ed = Application.sPref.edit();

        if (BiometricManager.from(this).canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
            Log.w("BIO", "Not supported");
            ed.putBoolean("fingerprint", false);
            ed.apply();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(ActivitySettings.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.e("BIO", "ERROR " + errString);
                cb_fingerprint.setChecked(!status);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.i("BIO", "OK");
                ed.putBoolean("fingerprint", status);
                ed.apply();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.e("BIO", "FAILED");
                cb_fingerprint.setChecked(!status);
                finish();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setAllowedAuthenticators(BIOMETRIC_WEAK | BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .setAllowedAuthenticators(BIOMETRIC_WEAK | DEVICE_CREDENTIAL)
                .setTitle("Разблокировка приложения")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

}
