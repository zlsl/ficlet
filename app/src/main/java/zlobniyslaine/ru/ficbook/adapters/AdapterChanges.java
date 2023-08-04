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
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.controls.MLRoundedImageView;
import zlobniyslaine.ru.ficbook.models.Authors;


public class AdapterChanges extends RecyclerView.Adapter<AdapterChanges.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> ChangesList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_date;
        final TextView p_part;
        final TextView p_name;
        final MLRoundedImageView iv_author_avatar;

        public ViewHolder(View v) {
            super(v);
            p_date = v.findViewById(R.id.tv_date);
            p_part = v.findViewById(R.id.tv_part);
            p_name = v.findViewById(R.id.tv_author_name);
            iv_author_avatar = v.findViewById(R.id.iv_author_avatar);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterChanges(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        ChangesList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterChanges.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_changes, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityAuthorProfile.class);
                intent.putExtra("id", Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("author_id")).toString());
                intent.putExtra("url", "https://ficbook.net/authors/" + Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("author_id")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            holder.p_date.setText(Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("date")).toString());
            holder.p_part.setText(Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("changes_part")).toString());
            holder.p_name.setText(Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("author_name")).toString());

            if (Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("draft")).toString().equals("1")) {
                holder.p_part.setTextColor(Color.parseColor("#FF0000"));
            } else {
                holder.p_part.setTextColor(Color.parseColor("#000000"));
            }

            Authors a = new Select()
                    .from(Authors.class)
                    .where("nid = ?", Objects.requireNonNull(ChangesList.get(holder.getAdapterPosition()).get("author_id")).toString())
                    .executeSingle();

            if (a != null) {
                try {
                    Picasso.with(context)
                            .load(a.avatar_url)
                            .into(holder.iv_author_avatar);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.iv_author_avatar.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_launcher));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return ChangesList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}