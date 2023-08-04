package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
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


public class AdapterAuthors extends RecyclerView.Adapter<AdapterAuthors.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> AuthorsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_name;
        final TextView p_role;
        final MLRoundedImageView iv_author_avatar;

        public ViewHolder(View v) {
            super(v);
            p_name = v.findViewById(R.id.tv_author_name);
            p_role = v.findViewById(R.id.tv_author_role);
            iv_author_avatar = v.findViewById(R.id.iv_author_avatar);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterAuthors(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        AuthorsList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterAuthors.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_author, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityAuthorProfile.class);
                intent.putExtra("id", Objects.requireNonNull(AuthorsList.get(holder.getAdapterPosition()).get("author_id")).toString());
                intent.putExtra("url", "https://ficbook.net/authors/" + Objects.requireNonNull(AuthorsList.get(holder.getAdapterPosition()).get("author_id")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            holder.p_name.setText(Objects.requireNonNull(AuthorsList.get(holder.getAdapterPosition()).get("name")).toString());
            holder.p_role.setVisibility(View.GONE);

            holder.iv_author_avatar.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_noavatar));
            if (AuthorsList.get(holder.getAdapterPosition()).containsKey("avatar_url")) {
                if (!Objects.requireNonNull(AuthorsList.get(holder.getAdapterPosition()).get("avatar_url")).toString().isEmpty()) {
                    try {
                        Picasso.with(context)
                                .load(Objects.requireNonNull(AuthorsList.get(holder.getAdapterPosition()).get("avatar_url")).toString())
                                .into(holder.iv_author_avatar);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Authors a = new Select()
                        .from(Authors.class)
                        .where("nid = ?", Objects.requireNonNull(AuthorsList.get(holder.getAdapterPosition()).get("author_id")).toString())
                        .executeSingle();

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return AuthorsList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}