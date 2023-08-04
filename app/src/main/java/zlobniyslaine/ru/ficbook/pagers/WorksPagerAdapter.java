package zlobniyslaine.ru.ficbook.pagers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class WorksPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragments;

    public WorksPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
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
                return "Работы";
            case 1:
                return "Соавтор";
            case 2:
                return "Бета";
            case 3:
                return "Части";
            case 4:
                return "Запросы";
            default:
                return "";
        }
    }
}
