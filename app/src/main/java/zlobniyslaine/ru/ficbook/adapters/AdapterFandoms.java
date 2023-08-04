package zlobniyslaine.ru.ficbook.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;


public class AdapterFandoms extends RecyclerView.Adapter<AdapterFandoms.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> FandomList;
    private final ArrayList<HashMap<String, Object>> FandomListCopy;
    private final Context context;
    private final Boolean search_sub;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                p_secondary_title.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }
        }
    }

    public AdapterFandoms(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        //noinspection unchecked
        FandomList = (ArrayList<HashMap<String, Object>>) data;
        FandomListCopy = new ArrayList<>();
        search_sub = Application.sPref.getBoolean("search_sub", true);
    }

    @NotNull
    @Override
    public AdapterFandoms.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fandoms_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {

                Intent intent = new Intent(context, ActivityFanficList.class);
                intent.putExtra("url", "https://ficbook.net/fanfiction/" + FandomList.get(holder.getAdapterPosition()).get("url") + "?p=@");
                intent.putExtra("title", "Фанфики по «" + FandomList.get(holder.getAdapterPosition()).get("title") + "»");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            holder.p_title.setText(Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("title")).toString());
            holder.p_secondary_title.setText(Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("secondary_title")).toString());
            holder.p_count.setText(Objects.requireNonNull(FandomList.get(holder.getAdapterPosition()).get("count")).toString());
        } catch (NullPointerException e) {
            holder.p_title.setText("");
        }
    }

    public void snapshot() {
        FandomListCopy.addAll(FandomList);
    }

    public void filter(String text) {
        FandomList.clear();
        if (text.isEmpty()) {
            FandomList.addAll(FandomListCopy);
        } else {
            text = text.toLowerCase(Locale.getDefault());
            try {
                for (HashMap<String, Object> item : FandomListCopy) {
                    if ((Objects.requireNonNull(item.get("title")).toString().toLowerCase(Locale.getDefault()).contains(text)) || (search_sub && (Objects.requireNonNull(item.get("secondary_title")).toString().toLowerCase(Locale.getDefault()).contains(text)))) {
                        FandomList.add(item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        notifyDataSetChanged();
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