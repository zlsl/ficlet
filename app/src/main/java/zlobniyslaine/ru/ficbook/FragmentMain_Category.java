package zlobniyslaine.ru.ficbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import zlobniyslaine.ru.ficbook.adapters.AdapterCategory;
import zlobniyslaine.ru.ficbook.models.Category;


public class FragmentMain_Category extends Fragment {

    RecyclerView rv_category;

    private AdapterCategory rv_cadapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_category, container, false);

        rv_category = rootView.findViewById(R.id.rv_category);

        List<Category> clist = Category.getAll();

        rv_category.setLayoutManager(new WrapContentLinearLayoutManager(getContext()));
        rv_cadapter = new AdapterCategory(getContext(), clist);
        rv_category.setAdapter(rv_cadapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        rv_cadapter.notifyDataSetChanged();
    }
}