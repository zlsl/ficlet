package zlobniyslaine.ru.ficbook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import zlobniyslaine.ru.ficbook.adapters.AdapterFicParts;

public class FragmentFic_Parts extends Fragment {

    RecyclerView rv1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fic_parts, container, false);

        rv1 = rootView.findViewById(R.id.rv_parts);

        ArrayList<HashMap<String, Object>> ficParts = new ArrayList<>();

        if (getArguments() != null) {
            //noinspection unchecked
            ficParts = (ArrayList<HashMap<String, Object>>) getArguments().getSerializable("parts");
            if (Application.sPref.getBoolean("reverse_part_list", false)) {
                Collections.reverse(ficParts);
            }
        }

        rv1.setLayoutManager(new WrapContentLinearLayoutManager(rootView.getContext()));
        AdapterFicParts rv_adapter = new AdapterFicParts(rootView.getContext(), ficParts);
        rv1.setAdapter(rv_adapter);

        return rootView;
    }

}