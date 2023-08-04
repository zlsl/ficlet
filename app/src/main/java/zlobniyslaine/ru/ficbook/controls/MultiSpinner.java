package zlobniyslaine.ru.ficbook.controls;


import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.AlertDialog;

public class MultiSpinner extends androidx.appcompat.widget.AppCompatTextView implements OnMultiChoiceClickListener {

    public enum AllSelectedDisplayMode {
        DisplayAllItems
    }

    private SpinnerAdapter mAdapter;
    private boolean[] mOldSelection;
    private boolean[] mSelected;
    private String mDefaultText;
    private String mAllText;
    private boolean mAllSelected;
    private AllSelectedDisplayMode mAllSelectedDisplayMode;
    private MultiSpinnerListener mListener;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context context, AttributeSet attr) {
        this(context, attr, androidx.appcompat.R.attr.spinnerStyle);
    }

    public MultiSpinner(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        mSelected[which] = isChecked;
    }

    private final OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            String[] choices = new String[mAdapter.getCount()];

            for (int i = 0; i < choices.length; i++) {
                choices[i] = mAdapter.getItem(i).toString();
            }

            System.arraycopy(mSelected, 0, mOldSelection, 0, mSelected.length);

            builder.setMultiChoiceItems(choices, mSelected, MultiSpinner.this);

            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);

                dialog.dismiss();
            });

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                refreshSpinner();
                mListener.onItemsSelected(mSelected);
                dialog.dismiss();
            });

            builder.show();
        }
    };

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            // all selected by default
            mOldSelection = new boolean[mAdapter.getCount()];
            mSelected = new boolean[mAdapter.getCount()];
            for (int i = 0; i < mSelected.length; i++) {
                mOldSelection[i] = false;
                mSelected[i] = mAllSelected;
            }
        }
    };


    public void setAdapter(SpinnerAdapter adapter, boolean allSelected, MultiSpinnerListener listener) {
        SpinnerAdapter oldAdapter = this.mAdapter;

        setOnClickListener(null);

        this.mAdapter = adapter;
        this.mListener = listener;
        this.mAllSelected = allSelected;

        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(dataSetObserver);
        }

        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(dataSetObserver);

            // all selected by default
            mOldSelection = new boolean[mAdapter.getCount()];
            mSelected = new boolean[mAdapter.getCount()];
            for (int i = 0; i < mSelected.length; i++) {
                mOldSelection[i] = false;
                mSelected[i] = allSelected;
            }

            setOnClickListener(onClickListener);
        }

        setText(mAllText);
    }

    public interface MultiSpinnerListener {
        void onItemsSelected(boolean[] selected);
    }

    public void setSelected(boolean[] selected) {
        if (this.mSelected.length != selected.length)
            return;

        this.mSelected = selected;

        refreshSpinner();
    }

    private void refreshSpinner() {
        // refresh text on spinner
        StringBuilder spinnerBuffer = new StringBuilder();
        boolean someUnselected = false;
        boolean allUnselected = true;

        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mSelected[i]) {
                spinnerBuffer.append(mAdapter.getItem(i).toString());
                spinnerBuffer.append(", ");
                allUnselected = false;
            } else {
                someUnselected = true;
            }
        }

        String spinnerText;

        if (!allUnselected) {
            if ((someUnselected && !(mAllText != null && mAllText.length() > 0)) || mAllSelectedDisplayMode == AllSelectedDisplayMode.DisplayAllItems) {
                spinnerText = spinnerBuffer.toString();
                if (spinnerText.length() > 2)
                    spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
            } else {
                spinnerText = mAllText;
            }
        } else {
            spinnerText = mDefaultText;
        }

        setText(spinnerText);
    }

    public void setDefaultText(String defaultText) {
        this.mDefaultText = defaultText;
    }

    public void setAllText(String allText) {
        this.mAllText = allText;
    }

    public void setAllSelectedDisplayMode(AllSelectedDisplayMode allSelectedDisplayMode) {
        this.mAllSelectedDisplayMode = allSelectedDisplayMode;
    }
}
