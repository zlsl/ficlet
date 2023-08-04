package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
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

import zlobniyslaine.ru.ficbook.ActivityPartEditor;
import zlobniyslaine.ru.ficbook.R;


public class AdapterWorkParts extends RecyclerView.Adapter<AdapterWorkParts.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> FicParts;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final TextView p_wait;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_part_title);
            p_wait = v.findViewById(R.id.tv_wait);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterWorkParts(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        FicParts = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterWorkParts.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_part_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityPartEditor.class);
                intent.putExtra("fanfic_id", Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("fanfic_id")).toString());
                intent.putExtra("part_id", Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("part_id")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            if ((Boolean) FicParts.get(holder.getAdapterPosition()).get("draft")) {
                holder.p_title.setTextColor(context.getResources().getColor(R.color.textColorDarkDisabled));
            } else {
                holder.p_title.setTextColor(context.getResources().getColor(R.color.textColorDarkPrimary));
            }
            holder.p_title.setText(Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("title")).toString());
            holder.p_wait.setText(Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("wait")).toString());
        } catch (Exception e) {
            holder.p_title.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return FicParts.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}