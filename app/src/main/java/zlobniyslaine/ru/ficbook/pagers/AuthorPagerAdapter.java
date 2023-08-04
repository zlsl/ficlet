package zlobniyslaine.ru.ficbook.pagers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class AuthorPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragments;

    public AuthorPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
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
                return "Инфо";
            case 1:
                return "Работы";
            case 2:
                return "Соавтор";
            case 3:
                return "Бета";
            case 4:
                return "Отзывы";
            case 5:
                return "В избранном";
            case 6:
                return "Архив";
            default:
                return "";
        }
    }
}