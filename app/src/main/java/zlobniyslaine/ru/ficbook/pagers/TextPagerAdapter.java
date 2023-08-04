package zlobniyslaine.ru.ficbook.pagers;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import zlobniyslaine.ru.ficbook.PageFragment;

public class TextPagerAdapter extends FragmentPagerAdapter {
    private List<CharSequence> pageTexts = new ArrayList<>();
    private final List<String> spages = new ArrayList<>();
    private Boolean night_mode = false;
    private Boolean loaded = false;
    private long baseId = 0;

    public TextPagerAdapter(FragmentManager fm, List<CharSequence> pageTexts) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.pageTexts.clear();
        if (pageTexts != null) {
            for (CharSequence s : pageTexts) {
                if (s.length() != 0) {
                    this.pageTexts.add(s);
                    loaded = true;
                }
            }
        }
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
    }

    @NotNull
    @Override
    public Fragment getItem(int i) {
        try {
            return PageFragment.newInstance(pageTexts.get(i), night_mode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getCount() {
        return pageTexts.size();
    }

    public void setPages(List<CharSequence> pageTexts) {
        this.pageTexts.clear();
        this.pageTexts = pageTexts;
        loaded = true;
    }

    public void setNightMode(Boolean mode) {
        night_mode = mode;
    }

    public void savePages(Context context, String id) {
        Log.d("FPAGE_CACHE", "Saving");
        try {
            spages.clear();
            FileOutputStream fos = context.openFileOutput(id + ".pagesz", Context.MODE_PRIVATE);
            GZIPOutputStream gz = new GZIPOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);
            for (CharSequence s : pageTexts) {
                if (s.length() > 0) {
                    spages.add(s.toString());
                }
            }
            oos.writeObject(spages);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadPages(Context context, String id) {
        Log.d("FPAGE_CACHE", "Loading");

        try {
            File ff = new File(context.getFilesDir() + File.separator + id + ".pagesz");
            if (!ff.exists()) {
                Log.d("FPAGE_CACHE", "no data");
                loaded = false;
                return;
            }

            spages.clear();
            pageTexts.clear();
            FileInputStream fis = context.openFileInput(id + ".pagesz");
            GZIPInputStream gz = new GZIPInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(gz);
            spages.addAll((List<String>) ois.readObject());
            pageTexts.addAll(spages);
            ois.close();
            notifyDataSetChanged(); // warn
            loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getItemId(int position) {
        return baseId + position;
    }

    public void notifyChangeInPosition(int n) {
        baseId += getCount() + n;
    }

    public Boolean isLoaded() {
        return loaded;
    }
}