package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.activeandroid.query.Select;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityAuthorProfile;
import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.controls.MLRoundedImageView;
import zlobniyslaine.ru.ficbook.models.Authors;


public class AdapterCollectionsFic extends RecyclerView.Adapter<AdapterCollectionsFic.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> CollectionsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView p_title;
        final TextView p_count;
        final TextView p_locked;
        final TextView p_author_name;
        final MLRoundedImageView iv_author_avatar;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_title);
            p_count = v.findViewById(R.id.tv_count);
            p_locked = v.findViewById(R.id.tv_locked);
            p_author_name = v.findViewById(R.id.tv_author_name);
            iv_author_avatar = v.findViewById(R.id.iv_author_avatar);
        }
    }

    public AdapterCollectionsFic(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        //noinspection unchecked
        CollectionsList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterCollectionsFic.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_ficitem, parent, false);
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
                intent.putExtra("url", "https://ficbook.net/collections/" + Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("id")).toString() + "?p=@&sort=" + 1);
                intent.putExtra("title", "Сборник «" + Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("title")).toString() + "»");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        holder.p_author_name.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityAuthorProfile.class);
                intent.putExtra("id", Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("author_id")).toString());
                intent.putExtra("url", "https://ficbook.net/authors/" + Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("author_id")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            holder.p_title.setText(Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("title")).toString());
            holder.p_count.setText(Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("count")).toString());
            holder.p_author_name.setText(Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("author_name")).toString());
        } catch (Exception e) {
            holder.p_title.setText("");
        }
        try {
            if (Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("locked")).toString().equals("0")) {
                holder.p_locked.setTextColor(Color.parseColor("#086e00"));
                holder.p_locked.setText(String.valueOf((char) 0xeb51));
            } else {
                holder.p_locked.setTextColor(Color.parseColor("#ad0000"));
                holder.p_locked.setText(String.valueOf((char) 0xeb50));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Authors a = new Select()
                    .from(Authors.class)
                    .where("nid = ?", Objects.requireNonNull(CollectionsList.get(holder.getAdapterPosition()).get("author_id")).toString())
                    .executeSingle();
            holder.iv_author_avatar.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_noavatar));
            if (a != null) {
                if (!a.avatar_url.isEmpty()) {
                    try {
                        Picasso.with(context)
                                .load(a.avatar_url)
                                .into(holder.iv_author_avatar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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