package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.models.Feeds;


public class AdapterFeeds extends RecyclerView.Adapter<AdapterFeeds.ViewHolder> implements View.OnClickListener {

    private final List<Feeds> FeedsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final View p_auto;
        final ImageButton btn_remove;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_title);
            p_auto = v.findViewById(R.id.tv_auto);
            btn_remove = v.findViewById(R.id.btn_remove);
        }
    }

    public AdapterFeeds(Context context, List<Feeds> data) {
        this.context = context;
        FeedsList = data;
    }

    @NotNull
    @Override
    public AdapterFeeds.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feeds, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityFanficList.class);
                intent.putExtra("url", FeedsList.get(holder.getAdapterPosition()).url);
                intent.putExtra("title", "Лента «" + FeedsList.get(holder.getAdapterPosition()).title + "»");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            SharedPreferences.Editor ed = Application.sPref.edit();
            ed.putString("main_feed", FeedsList.get(holder.getAdapterPosition()).url);
            ed.apply();
            Toast.makeText(Application.getContext(), "Лента " + FeedsList.get(holder.getAdapterPosition()).title + " установлена как основная.", Toast.LENGTH_SHORT).show();
            notifyItemChanged(holder.getAdapterPosition());
            notifyDataSetChanged();
            return false;
        });


        holder.btn_remove.setOnClickListener(v -> {
            try {
                Feeds toRemove = Feeds.getById(FeedsList.get(holder.getAdapterPosition()).getId().intValue());
                toRemove.delete();
                FeedsList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        boolean currentFeed = Objects.requireNonNull(Application.sPref.getString("main_feed", "")).equals(FeedsList.get(holder.getAdapterPosition()).url);
        holder.p_title.setText(FeedsList.get(holder.getAdapterPosition()).title);

        if (currentFeed) {
            holder.p_auto.setBackgroundColor(Color.parseColor("#086e00"));
        } else {
            holder.p_auto.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return FeedsList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}