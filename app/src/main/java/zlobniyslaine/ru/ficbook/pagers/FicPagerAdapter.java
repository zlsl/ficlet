package zlobniyslaine.ru.ficbook.pagers;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class FicPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragments;

    private String reviews_count = "";

    public FicPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
    }

    public void setReviewsCount(String count) {
        this.reviews_count = count;
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
                return "Оглавление";
            case 2:
                if (reviews_count.isEmpty()) {
                    return "Отзывы";
                } else {
                    return reviews_count;
                }
            case 3:
                return "В сборниках";
            default:
                return "";
        }
    }
}