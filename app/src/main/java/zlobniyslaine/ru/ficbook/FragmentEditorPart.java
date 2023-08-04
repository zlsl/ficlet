package zlobniyslaine.ru.ficbook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.fragment.app.Fragment;

import io.github.mthli.knife.KnifeText;
import zlobniyslaine.ru.ficbook.controls.VerticalSeekBar;


public class FragmentEditorPart extends Fragment implements SpellCheckerSession.SpellCheckerSessionListener {

    private String content = "";

    public String getContent() {
        return e_content.getText().toString();
    }

    @Override
    public void onResume() {
        Log.e("VVV", "Visible");
        super.onResume();
    }

    public void setContent(String content) {
        this.content = content;
        if (e_content != null) {
            try {
                e_content.scrollTo(0, e_content.getTop());
                sb_position.setMax(getEditorHeight());
                sb_position.setProgress(getEditorHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void clickContent() {
        sb_position.setMax(getEditorHeight());
        sb_position.setProgress(getEditorHeight());
    }

    KnifeText e_content;
    VerticalSeekBar sb_position;
    ImageButton ib_bold;
    ImageButton ib_italic;
    ImageButton ib_striked;
    ImageButton ib_centered;
    ImageButton ib_righted;
    ImageButton ib_divider;
    ImageButton ib_tab;
    ImageButton ib_undo;

    void makeBold() {
        wrapSelection("b");
    }

    void makeItalic() {
        wrapSelection("i");
    }

    void makeStriked() {
        wrapSelection("s");
    }

    void makeCentered() {
        wrapSelection("center");
    }

    void makeRighted() {
        wrapSelection("right");
    }

    void makeDivider() {
        e_content.getText().insert(e_content.getSelectionStart(), "\n<center>***</center>\n");
    }

    void makeTab() {
        e_content.getText().insert(e_content.getSelectionStart(), "<tab>");
    }

    void undoOp() {
        e_content.undo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_editor_part, container, false);

        e_content = rootView.findViewById(R.id.e_content);
        sb_position = rootView.findViewById(R.id.sb_position);
        ib_bold = rootView.findViewById(R.id.bold);
        ib_italic = rootView.findViewById(R.id.italic);
        ib_striked = rootView.findViewById(R.id.striked);
        ib_centered = rootView.findViewById(R.id.centered);
        ib_righted = rootView.findViewById(R.id.righted);
        ib_divider = rootView.findViewById(R.id.divider);
        ib_tab = rootView.findViewById(R.id.tab);
        ib_undo = rootView.findViewById(R.id.undo);

        ib_bold.setOnClickListener(v -> makeBold());
        ib_italic.setOnClickListener(v -> makeItalic());
        ib_striked.setOnClickListener(v -> makeStriked());
        ib_centered.setOnClickListener(v -> makeCentered());
        ib_righted.setOnClickListener(v -> makeRighted());
        ib_divider.setOnClickListener(v -> makeDivider());
        ib_tab.setOnClickListener(v -> makeTab());
        ib_undo.setOnClickListener(v -> undoOp());
        e_content.setOnClickListener(v -> clickContent());

        e_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sb_position.setMax(getEditorHeight());
            }
        });

        //e_content.setCustomSelectionActionModeCallback(new StyleCallback());
        e_content.setText(content);
        e_content.setSelection(e_content.getText().length());
        sb_position.setMax(getEditorHeight());


        sb_position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                e_content.scrollTo(0, getEditorHeight() - progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return rootView;
    }

    private void wrapSelection(String tag) {
        String contents = e_content.getText().toString();

        int selectionStart = e_content.getSelectionStart();
        int selectionEnd = e_content.getSelectionEnd();
        String selectedText = contents.substring(selectionStart, selectionEnd);
        selectedText = "<" + tag + ">" + selectedText + "</" + tag + ">";

        String newText = contents.substring(0, e_content.getSelectionStart()) + selectedText + contents.substring(e_content.getSelectionEnd());
        e_content.setText(newText);
        e_content.setSelection(selectionStart);
    }

    private int getEditorHeight() {
        return Math.round((e_content.getLineCount() * (e_content.getLineHeight() + e_content.getLineSpacingExtra()) * e_content.getLineSpacingMultiplier())) + e_content.getCompoundPaddingTop() + e_content.getCompoundPaddingBottom();
    }

    @Override
    public void onGetSuggestions(SuggestionsInfo[] suggestionsInfos) {

    }

    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] sentenceSuggestionsInfos) {

    }

// --Commented out by Inspection START (16.07.20 22:34):
//    static class StyleCallback implements ActionMode.Callback {
//
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            MenuInflater inflater = mode.getMenuInflater();
//            inflater.inflate(R.menu.style, menu);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                menu.removeItem(android.R.id.shareText);
//            }
//            menu.removeItem(android.R.id.cut);
//            return true;
//        }
//
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return false;
//        }
//
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            CharacterStyle cs;
//            switch (item.getItemId()) {
//                case R.id.bold:
//                    return true;
//
//                case R.id.italic:
//                    return true;
//            }
//            return false;
//        }
//
//        public void onDestroyActionMode(ActionMode mode) {
//        }
//    }
// --Commented out by Inspection STOP (16.07.20 22:34)
}

