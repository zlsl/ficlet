package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import zlobniyslaine.ru.ficbook.R;

public class AdapterAutoSuggest extends ArrayAdapter<String> {
    private final List<String> items;
    private final List<String> tempItems;
    private final List<String> suggestions;
    private final int min_chars;

    public AdapterAutoSuggest(Context context, int resource, List<String> items) {
        super(context, resource, 0, items);

        min_chars = 2;

        this.items = items;
        tempItems = new ArrayList<>(items);
        suggestions = new ArrayList<>();
    }

    public static class ViewHolder {
        TextView p_title;
        TextView p_subtitle;
        TextView p_count;
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fandom_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.p_title = convertView.findViewById(R.id.tv_title);
            viewHolder.p_subtitle = convertView.findViewById(R.id.tv_subtitle);
            viewHolder.p_count = convertView.findViewById(R.id.tv_count);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        try {
            String item = items.get(position);

            if (item != null) {
                String[] fandom = item.split("\\|");
                if (fandom.length > min_chars) {
                    viewHolder.p_title.setText(fandom[0]);
                    viewHolder.p_subtitle.setText(fandom[1]);
                    viewHolder.p_count.setText(fandom[2]);
                } else {
                    viewHolder.p_title.setText("");
                    viewHolder.p_subtitle.setText("");
                    viewHolder.p_count.setText("");
                }
            } else {
                viewHolder.p_title.setText("");
                viewHolder.p_subtitle.setText("");
                viewHolder.p_count.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @NotNull
    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    private final Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return (String) resultValue;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                if (constraint.length() > min_chars) {
                    CharSequence constraint_lo = constraint.toString().toLowerCase(Locale.getDefault());
                    for (String names : tempItems) {
                        if (names.toLowerCase(Locale.getDefault()).contains(constraint_lo)) { //contains
                            suggestions.add(names);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                if (results != null && results.count > 0) {
                    clear();

                    ArrayList<String> fitems = (ArrayList<String>) results.values;
                    for (String fitem : fitems) {
                        add(fitem);
                        notifyDataSetChanged();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}