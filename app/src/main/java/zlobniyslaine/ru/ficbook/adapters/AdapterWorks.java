package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import zlobniyslaine.ru.ficbook.ActivityEditor;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;


public class AdapterWorks extends RecyclerView.Adapter<AdapterWorks.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> Works;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final TextView p_sup;
        final TextView p_handle;
        final TextView p_parts;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.f_title);
            p_sup = v.findViewById(R.id.f_sup);
            p_handle = v.findViewById(R.id.tv_handle);
            p_parts = v.findViewById(R.id.tv_parts);

            p_handle.setTypeface(Application.getIconFont());
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterWorks(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        Works = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterWorks.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Log.i("FF", "editor");
                Intent intent = new Intent(context, ActivityEditor.class);
                intent.putExtra("id", Objects.requireNonNull(Works.get(holder.getAdapterPosition()).get("fanfic_id")).toString());
                intent.putExtra("title", Objects.requireNonNull(Works.get(holder.getAdapterPosition()).get("title")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        holder.p_parts.setOnClickListener(v -> {
            try {
                Log.i("FF", "editor");
                Intent intent = new Intent(context, ActivityEditor.class);
                intent.putExtra("id", Objects.requireNonNull(Works.get(holder.getAdapterPosition()).get("fanfic_id")).toString());
                intent.putExtra("title", Objects.requireNonNull(Works.get(holder.getAdapterPosition()).get("title")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        try {
            holder.p_title.setText(Objects.requireNonNull(Works.get(position).get("title")).toString());
            holder.p_sup.setText(Objects.requireNonNull(Works.get(position).get("votes")).toString());
            holder.p_parts.setText(Objects.requireNonNull(Works.get(position).get("parts")).toString().replace("<br>", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return Works.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}