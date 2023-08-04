package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityFanfic;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionUnfallow;


public class AdapterFollows extends RecyclerView.Adapter<AdapterFollows.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> FollowsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_date;
        final TextView p_title;
        final ImageButton btn_remove;

        public ViewHolder(View v) {
            super(v);
            p_date = v.findViewById(R.id.tv_date);
            p_title = v.findViewById(R.id.tv_title);
            btn_remove = v.findViewById(R.id.btn_remove);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterFollows(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        FollowsList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterFollows.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follows, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityFanfic.class);
                intent.putExtra("id", Objects.requireNonNull(FollowsList.get(holder.getAdapterPosition()).get("fic_id")).toString());
                intent.putExtra("title", Objects.requireNonNull(FollowsList.get(holder.getAdapterPosition()).get("title")).toString());
                intent.putExtra("tab", "reviews");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        holder.btn_remove.setOnClickListener(v -> {
            try {
                AjaxActionUnfallow action = new AjaxActionUnfallow();
                action.Do(Objects.requireNonNull(FollowsList.get(holder.getAdapterPosition()).get("part_id")).toString());
                FollowsList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        try {
            holder.p_date.setText(Objects.requireNonNull(FollowsList.get(holder.getAdapterPosition()).get("date")).toString());
            holder.p_title.setText(Objects.requireNonNull(FollowsList.get(holder.getAdapterPosition()).get("title")).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return FollowsList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}