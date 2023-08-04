package zlobniyslaine.ru.ficbook.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.R;


public class AdapterFandomsSearch extends RecyclerView.Adapter<AdapterFandomsSearch.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> FandomList;
    private final Context context;
    private final Boolean removable;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final TextView p_secondary_title;
        final TextView p_count;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_fandom_title);
            p_secondary_title = v.findViewById(R.id.tv_fandom_secondary_title);
            p_count = v.findViewById(R.id.tv_fandom_count);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterFandomsSearch(Context context, List<? extends Map<String, ?>> data, Boolean allowRemove) {
        this.context = context;
        this.removable = allowRemove;
        FandomList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterFandomsSearch.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fandoms_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            if (removable) {
                try {
                    FandomList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            if (Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("mode")).toString().equals("allow")) {
                holder.p_title.setTextColor(context.getResources().getColor(R.color.textColorDarkPrimary));
            } else {
                holder.p_title.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }
            holder.p_title.setText(Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("title")).toString());
            holder.p_secondary_title.setText(Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("secondary_title")).toString());
            holder.p_count.setText(Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("count")).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return FandomList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}