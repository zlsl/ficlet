package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.FandomsActivity;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.models.Category;
import zlobniyslaine.ru.ficbook.models.Fandoms;


public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> implements View.OnClickListener {

    private List<Category> CategoryList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView p_title;
        final TextView p_load;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_category_title);
            p_load = v.findViewById(R.id.tv_load);
            p_load.setTypeface(Application.getIconFont());
        }
    }

    public AdapterCategory(Context context, List<Category> data) {
        this.context = context;
        try {
            CategoryList = data;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public AdapterCategory.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                if (CategoryList.get(holder.getAdapterPosition()).nid.equals("7")) {
                    Intent intent = new Intent(context, ActivityFanficList.class);
                    intent.putExtra("url", "https://ficbook.net/fanfiction/no_fandom/originals?p=@");
                    intent.putExtra("title", "Ориджиналы");
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, FandomsActivity.class);
                    intent.putExtra("name", CategoryList.get(holder.getAdapterPosition()).name);
                    intent.putExtra("group_id", CategoryList.get(holder.getAdapterPosition()).nid);
                    intent.putExtra("group_url", CategoryList.get(holder.getAdapterPosition()).url);
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            holder.p_title.setText(CategoryList.get(holder.getAdapterPosition()).name);
            if (Fandoms.getCount(CategoryList.get(holder.getAdapterPosition()).nid) < 1) {
                holder.p_load.setVisibility(View.VISIBLE);
            } else {
                holder.p_load.setVisibility(View.GONE);
            }
            if (CategoryList.get(holder.getAdapterPosition()).nid.equals("7")) {
                holder.p_load.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.p_title.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return CategoryList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}