package zlobniyslaine.ru.ficbook;


import static zlobniyslaine.ru.ficbook.Application.SIG;
import static zlobniyslaine.ru.ficbook.Application.download_token;
import static zlobniyslaine.ru.ficbook.Application.genToken;
import static zlobniyslaine.ru.ficbook.Application.logLargeString;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import zlobniyslaine.ru.ficbook.adapters.AdapterParagraphs;
import zlobniyslaine.ru.ficbook.controls.FloatSeekBar;
import zlobniyslaine.ru.ficbook.models.FanficPage;

public class ActivityVoiceReaderPro extends AppCompatActivity {

    private String data = "";
    private String id;
    private int current = 0;
    private Boolean run = true;
    private List<String> paragraphs;

    private ArrayList<String> array_voices;
    List<String> listInstalledEnginesName;

    PowerManager.WakeLock wakeLock;

    ProgressBar pb1;
    Toolbar toolbar;
    RecyclerView rv_paragraphs;
    ImageButton btn_playstop;
    FloatSeekBar sb_speech_rate;
    FloatSeekBar sb_speech_pitch;
    TextView tv_speech_rate;
    TextView tv_speech_pitch;
    Spinner spinner_voices;
    Spinner spinner_engines;


    void resetRate() {
        sb_speech_rate.setValue(1f);
    }

    void resetPitch() {
        sb_speech_pitch.setValue(1f);
    }

    void ttsRunStop() {
        try {
            if (Application.tts.isSpeaking()) {
                Application.stopSpeak();
                btn_playstop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_playback_play));
                run = false;
                try {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                run = true;
                btn_playstop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_playback_pause));
                current--;
                try {
                    wakeLock.acquire(5000);
                    Log.i("VREADER", "wakelock");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                speechNext(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voicereader_pro);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Ficlet::VoiceReader");

        spinner_voices = findViewById(R.id.spinner_voices);
        spinner_engines = findViewById(R.id.spinner_engines);

        if (Application.tts == null) {
            Application.initTTS();
        }

        int tts_idx = 0;
        listInstalledEnginesName = new ArrayList<>();
        try {
            for (int i = 0; i < Application.getTtsEngines().size(); i++) {
                listInstalledEnginesName.add(Application.getTtsEngines().get(i).label);
                if (Application.getTTSEngine().equals(Application.getTtsEngines().get(i).name)) {
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
                Log.e("!!!Setting", Application.getTtsEngines().get(position).name);
                Editor ed = Application.sPref.edit();
                ed.putString("voice_engine", Application.getTTSEngine());
                ed.apply();
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
                if (Application.tts.getVoices() != null) {
                    if (Application.tts.getVoices().size() > 0) {
                        for (Voice tmpVoice : Application.tts.getVoices()) {
                            if (tmpVoice.getName().equals(array_voices.get(position))) {
                                Application.stopSpeak();
                                Application.tts.setVoice(tmpVoice);
                                Editor ed = Application.sPref.edit();
                                ed.putString("voice", tmpVoice.getName());
                                ed.apply();
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

        findViewById(R.id.btn_tts).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btn_tts_get).setOnClickListener(v -> {
            final CharSequence[] xt = new CharSequence[] {"Яндекс (Лучшее качество, требует интернет)", "Синтезатор Google", "RHVoice", "Большой каталог на 4pda.ru"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Скачать голоса")
                    .setSingleChoiceItems(xt, 0, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                break;
                            case 1:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")));
                                break;
                            case 2:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.github.olga_yakovleva.rhvoice.android")));
                                break;
                            case 3:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://4pda.ru/forum/index.php?showtopic=200728")));
                                break;
                        }
                    })
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.create().show();

        });


        pb1 = findViewById(R.id.pb1);
        toolbar = findViewById(R.id.toolbar);
        rv_paragraphs = findViewById(R.id.rv_paragraphs);
        btn_playstop = findViewById(R.id.btn_playstop);

        sb_speech_rate = findViewById(R.id.sb_speech_rate);
        sb_speech_pitch = findViewById(R.id.sb_speech_pitch);
        tv_speech_rate = findViewById(R.id.tv_speech_rate);
        tv_speech_pitch = findViewById(R.id.tv_speech_pitch);

        tv_speech_rate.setOnClickListener(v -> resetRate());
        tv_speech_pitch.setOnClickListener(v -> resetPitch());

        btn_playstop.setOnClickListener(v -> ttsRunStop());

        sb_speech_rate.setValue(Application.sPref.getFloat("speech_rate", 1f));
        tv_speech_rate.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_rate.getValue() * 100)));
        sb_speech_pitch.setValue(Application.sPref.getFloat("speech_pitch", 1f));
        tv_speech_pitch.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_pitch.getValue() * 100)));

        sb_speech_pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    tv_speech_pitch.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_pitch.getValue() * 100)));
                    Application.tts.setPitch(sb_speech_pitch.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (Application.tts == null) {
                    return;
                }
                try {
                    Application.tts.setPitch(sb_speech_pitch.getValue());
                    SharedPreferences.Editor ed = Application.sPref.edit();
                    ed.putFloat("speech_pitch", sb_speech_pitch.getValue());
                    ed.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        sb_speech_rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    tv_speech_rate.setText(String.format("%s%%", String.format(Locale.getDefault(), "%.0f", sb_speech_rate.getValue() * 100)));
                    Application.tts.setSpeechRate(sb_speech_rate.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Application.tts.setSpeechRate(sb_speech_rate.getValue());
                SharedPreferences.Editor ed = Application.sPref.edit();
                ed.putFloat("speech_rate", sb_speech_rate.getValue());
                ed.apply();
            }
        });

        paragraphs = new ArrayList<>();

        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        try {
            current = 0;
            if (FanficPage.getLastPage(id) != null) {
                if (FanficPage.getLastPage(id) != null) {
                    current = FanficPage.getLastPage(id).audio_page_number;
                }
            }
        } catch (Exception e) {
            current = 0;
            e.printStackTrace();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setTTSListener();

        rv_paragraphs.setHasFixedSize(true);
        rv_paragraphs.setLayoutManager(new WrapContentLinearLayoutManager(this));
        AdapterParagraphs rv_adapter = new AdapterParagraphs(this, paragraphs);
        rv_adapter.setOnClickListener(position -> {
            current = position;
            Application.stopSpeak();
            run = true;
            btn_playstop.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_playback_pause));
            speechNext(true);
        });
        rv_paragraphs.setAdapter(rv_adapter);

        new fetcher_main(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void speechNext(Boolean increase) {
        Log.e("TTS", "next");
        if (!run) {
            return;
        }
        Log.e("TTS", "is run");
        if (current < paragraphs.size()) {
            try {
                if (!wakeLock.isHeld()) {
                    Log.i("VREADER", "wakelock");
                    wakeLock.acquire(5000);
                }
                String vr = paragraphs.get(current);
                if (Application.sPref.getBoolean("yo_fix", false)) {
                    vr = YO.fix(vr);
                }

                if (vr.isEmpty()) {
                    Log.d("TTS", "paragraph");
                    Application.tts.playSilentUtterance(100, TextToSpeech.QUEUE_ADD, "MessageId");
                } else {
                    Application.speakText(vr, true);
                }

                ((AdapterParagraphs) Objects.requireNonNull(rv_paragraphs.getAdapter())).setCurrentItem(current);
                rv_paragraphs.getAdapter().notifyItemChanged(current);

                Log.d("TTS", paragraphs.get(current));
                if (increase) {
                    current++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class fetcher_main extends AsyncTask<String, Void, Void> {
        private final WeakReference<ActivityVoiceReaderPro> activityReference;

        fetcher_main(ActivityVoiceReaderPro context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (Application.isInternetAvailable()) {
                    Response response;
                    if (Application.isGuest()) {
                        Log.i("LOAD FB2", "rcache");
                    } else {
                        Log.i("LOAD FB2", "ficbook");
                        RequestBody formBody = new FormBody.Builder()
                                .add("fanfic_id", activityReference.get().id)
                                .add("token", download_token)
                                .add(genToken(activityReference.get().id).get(0),genToken(activityReference.get().id).get(1))
                                .build();

//                        response = Application.httpclient.newCall(Application.getRequestBuilder("https://ficbook.net/fanfic_download/fb2", formBody)).execute();
                        String url = "https://ficbook.net/fanfic_download/".replace(SIG, ".") + activityReference.get().id + "/fb2";
                        response = Application.httpclient.newCall(Application.getRequestBuilder(url)).execute();
                    }
                    ResponseBody body = response.body();
                    if (body != null) {
                        activityReference.get().data = body.string().replace("*", " ");
                        response.close();
                    }
                }
                activityReference.get().runOnUiThread(() -> activityReference.get().pb1.setIndeterminate(false));
            } catch (SocketTimeoutException e) {
                Application.displayPopup("Сервер не отвечает");
            } catch (UnknownHostException e) {
                Application.displayPopup("Проблемы с соединением");
            } catch (IOException e) {
                Application.displayPopup("Ошибка загрузки");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Document doc = Jsoup.parse(activityReference.get().data);
                doc.outputSettings().prettyPrint(false);
                activityReference.get().data = doc.select("section").html();
                activityReference.get().data = org.jsoup.parser.Parser.unescapeEntities(activityReference.get().data, true);
                activityReference.get().data = YO.cleanRaw(activityReference.get().data.replace(" style=\"margin-bottom: 0px;\"", ""));
                activityReference.get().data = activityReference.get().data.replaceAll("https?://\\S+\\s?", "");
                activityReference.get().data = activityReference.get().data.replaceAll("<[^>]*>", "");

                String[] lines = activityReference.get().data.split("\n");
                for (String line : lines) {
                    if (line.length() > 2) {
                        String vr = YO.Typograf(line).replace("&nbsp;", "");
                        if ( (vr.equals("  ")) || (vr.equals(" ")) ) {
                            vr = "";
                        }
                        activityReference.get().paragraphs.add(vr);
                        activityReference.get().paragraphs.add("");
                    }
                }
                Log.i("PARSED", "Count: " + activityReference.get().paragraphs.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (activityReference.get() != null) {
                try {
                    Objects.requireNonNull(activityReference.get().rv_paragraphs.getAdapter()).notifyDataSetChanged();
                    activityReference.get().pb1.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.i("VREADER", "wakelock");
                activityReference.get().speechNext(true);
            }
            Application.firePopup();
        }
    }

    @Override
    protected void onPause() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        FanficPage fp;
        fp = FanficPage.getLastPage(id);
        if (fp == null) {
            fp = new FanficPage();
            fp.page_count = paragraphs.size();
            fp.nid = id;
            fp.page_number = 0;
            fp.file_size = data.length();
        }
        fp.audio_page_number = current;
        fp.date_read = sdf.format(new Date());
        fp.save();

        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        Application.stopSpeak();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

            FanficPage fp;
            fp = FanficPage.getLastPage(id);
            if (fp == null) {
                fp = new FanficPage();
                fp.page_count = paragraphs.size();
                fp.nid = id;
                fp.page_number = 0;
                fp.file_size = data.length();
            }
            fp.audio_page_number = current;
            fp.date_read = sdf.format(new Date());
            fp.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        speechNext(false);
        super.onResume();
    }

    private void fillVoices() {
        spinner_voices.setVisibility(View.GONE);
        try {
            int idx = -1;
            int i = 0;
            array_voices = new ArrayList<>();
            if (Application.tts.getVoices() != null) {
                for (Voice tmpVoice : Application.tts.getVoices()) {
                    if (tmpVoice.getName().contains("ru")) {
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
            setTTSListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTTSListener() {
        try {
            Application.tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d("TTS", "start " + utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d("TTS", "done " + utteranceId);
                    speechNext(true);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e("TTS", "error " + utteranceId);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
