package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityReader;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.models.Parts;


public class AdapterFicParts extends RecyclerView.Adapter<AdapterFicParts.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> FicParts;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final TextView p_time;
        final TextView p_read;
        final LinearLayout fcard;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_part_title);
            p_time = v.findViewById(R.id.tv_part_time);
            p_read = v.findViewById(R.id.tv_part_read);
            fcard = v.findViewById(R.id.fcard);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterFicParts(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        FicParts = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterFicParts.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fic_part_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnLongClickListener(view -> false);
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityReader.class);
                intent.putExtra("id", Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("fanfic_id")).toString());
                if (!Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("part_id")).toString().isEmpty()) {
                    intent.putExtra("part_id", Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("part_id")).toString());
                    intent.putExtra("title", Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("title")).toString());
                } else {
                    intent.putExtra("no_parts", true);
                }
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        try {
            Parts col = Parts.getPart(Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("fanfic_id")).toString(), Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("part_id")).toString());

            holder.fcard.setBackgroundColor(ContextCompat.getColor(context, R.color.ff_bg));

            holder.p_title.setTextColor(context.getResources().getColor(R.color.textColorDarkPrimary));
            holder.p_read.setText("");
            if (col != null) {
                if ((FicParts.get(holder.getAdapterPosition()).containsKey("readed")) || (col.date_read != null)) {
                    holder.fcard.setBackgroundColor(ContextCompat.getColor(context, R.color.ff_bg_readed));
                    holder.p_title.setTextColor(context.getResources().getColor(R.color.textColorDarkDisabled));
                    holder.p_read.setText(col.date_read);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            holder.p_title.setText(Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("title")).toString());
            holder.p_time.setText(Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("time")).toString());
            if (Objects.requireNonNull(FicParts.get(holder.getAdapterPosition()).get("part_id")).toString().isEmpty()) {
                holder.p_read.setText("");
            }

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