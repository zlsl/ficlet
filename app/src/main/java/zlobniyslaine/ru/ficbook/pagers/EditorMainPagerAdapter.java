package zlobniyslaine.ru.ficbook.pagers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class EditorMainPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments;

    public EditorMainPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Информация";
            case 1:
                return "Содержание";
            case 2:
                return "Отзывы";
            case 3:
                return "Сборники";
            case 4:
                return "Статистика";
            default:
                return "";
        }
    }
}