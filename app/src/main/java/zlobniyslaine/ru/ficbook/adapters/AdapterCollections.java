package zlobniyslaine.ru.ficbook.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionDropCollection;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionRead;
import zlobniyslaine.ru.ficbook.models.Collections;


public class AdapterCollections extends RecyclerView.Adapter<AdapterCollections.ViewHolder> implements View.OnClickListener {

    private final List<Collections> CollectionsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView p_title;
        final TextView p_count;
        final TextView p_author;
        final TextView p_locked;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_title);
            p_count = v.findViewById(R.id.tv_count);
            p_locked = v.findViewById(R.id.tv_locked);
            p_author = v.findViewById(R.id.tv_author_name);
        }
    }

    public AdapterCollections(Context context, List<Collections> data) {
        this.context = context;
        CollectionsList = data;
    }

    @NotNull
    @Override
    public AdapterCollections.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);

        vh.p_locked.setTypeface(Application.getIconFont());

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                String sortmode = Application.sPref.getString("collections_sort_mode", "updated");
                Intent intent = new Intent(context, ActivityFanficList.class);
                intent.putExtra("url", "https://ficbook.net/collections/" + CollectionsList.get(holder.getAdapterPosition()).nid + "?p=@&sort=" + 1);
                intent.putExtra("title", "Сборник «" + CollectionsList.get(holder.getAdapterPosition()).title + "»");
                intent.putExtra("collection_id", CollectionsList.get(holder.getAdapterPosition()).nid);
                Collections.prepare(CollectionsList.get(holder.getAdapterPosition()).nid);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Удалить сборник " + CollectionsList.get(position).title + "?");
                builder.setPositiveButton("Да", (dialog, which) -> {
                    AjaxActionDropCollection action = new AjaxActionDropCollection();
                    action.Do(CollectionsList.get(position).nid);
                    CollectionsList.remove(position);
                    notifyItemRemoved(position);
                });
                builder.setNegativeButton("Нет", null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        try {
            holder.p_title.setText(CollectionsList.get(position).title);
            holder.p_author.setText(CollectionsList.get(position).author);
            holder.p_count.setText(CollectionsList.get(position).count);

            if (CollectionsList.get(position).locked.equals("0")) {
                holder.p_locked.setTextColor(Color.parseColor("#086e00"));
                holder.p_locked.setText(String.valueOf((char) 0xeb51));
            } else {
                holder.p_locked.setTextColor(Color.parseColor("#ad0000"));
                holder.p_locked.setText(String.valueOf((char) 0xeb50));
            }

            if (CollectionsList.get(position).author.equals(Application.user_name)) {
                holder.p_locked.setBackground(null);
            } else {
                holder.p_locked.setBackgroundColor(Color.parseColor("#FFC5CAE9"));
            }
        } catch (Exception e) {
            holder.p_title.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return CollectionsList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}