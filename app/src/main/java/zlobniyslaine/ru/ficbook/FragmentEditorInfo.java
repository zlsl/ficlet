package zlobniyslaine.ru.ficbook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class FragmentEditorInfo extends Fragment {

    Spinner spinner_work_publish;
    Spinner spinner_work_status;
    EditText e_part_name;
    EditText e_comments_before;
    EditText e_comments_after;
    EditText e_changes_comments;
    CheckBox cb_draft;
    TextView tv_title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editor_info, container, false);

        spinner_work_publish = rootView.findViewById(R.id.spinner_work_publish);
        spinner_work_status = rootView.findViewById(R.id.spinner_work_status);
        e_part_name = rootView.findViewById(R.id.e_part_name);
        e_comments_before = rootView.findViewById(R.id.e_comments_before);
        e_comments_after = rootView.findViewById(R.id.e_comments_after);
        e_changes_comments = rootView.findViewById(R.id.e_changes_comments);
        cb_draft = rootView.findViewById(R.id.cb_draft);
        tv_title = rootView.findViewById(R.id.tv_title);


        Context context = this.getContext();
        if (getArguments() != null) {
            e_part_name.setText(getArguments().getString("part_title"));
            tv_title.setText(getArguments().getString("fanfic_title"));
            e_comments_before.setText(getArguments().getString("comments_before"));
            e_comments_after.setText(getArguments().getString("comments_after"));
            cb_draft.setChecked(getArguments().getBoolean("draft"));
        }

        assert context != null;

        ArrayAdapter<CharSequence> ad_work_status = ArrayAdapter.createFromResource(context, R.array.array_work_status, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_work_status.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_work_status.setAdapter(ad_work_status);
        spinner_work_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinner_work_status.setSelection(getArguments().getInt("status") - 1);

        ArrayAdapter<CharSequence> ad_work_publish = ArrayAdapter.createFromResource(context, R.array.array_work_publish, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        ad_work_publish.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner_work_publish.setAdapter(ad_work_publish);
        spinner_work_publish.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
//        spinner_work_status.setSelection(getArguments().getInt("publish"));

        return rootView;
    }

    public String getTitle() {
        return e_part_name.getText().toString();
    }

    public String getCommentsBefore() {
        return e_comments_before.getText().toString();
    }

    public String getCommentsAter() {
        return e_comments_after.getText().toString();
    }

    public String getChangesComments() {
        return e_changes_comments.getText().toString();
    }

    public Boolean getDraft() {
        return cb_draft.isChecked();
    }

    public String getStatus() {
        return (spinner_work_status.getSelectedItemId() + 1) + "";
    }
}
