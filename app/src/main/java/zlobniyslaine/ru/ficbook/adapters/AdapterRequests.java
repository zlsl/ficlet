package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Html;
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

import zlobniyslaine.ru.ficbook.ActivityRequest;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.controls.PicassoImageGetter;


public class AdapterRequests extends RecyclerView.Adapter<AdapterRequests.ViewHolder> implements View.OnClickListener {

    private ArrayList<HashMap<String, Object>> Requests;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final TextView p_info;
        final TextView p_bookmark;
        final TextView p_like;
        final TextView p_fandoms;
        final TextView p_genres_icon;
        final TextView p_fandom_icon;
        final TextView p_cautions_icon;
        final TextView p_rating;
        final TextView p_directions;
        final TextView p_genres;
        final TextView p_fics;
        final TextView p_cautions;
        final LinearLayout l_genres;
        final LinearLayout l_cautions;
        final LinearLayout l_holder;
        final TextView p_directions_icon;

        public ViewHolder(View v) {
            super(v);
            p_title = v.findViewById(R.id.tv_title);
            p_info = v.findViewById(R.id.tv_info);
            p_bookmark = v.findViewById(R.id.tv_bookmark);
            p_like = v.findViewById(R.id.tv_like);
            p_fandoms = v.findViewById(R.id.tv_fandoms);
            p_rating = v.findViewById(R.id.tv_rating);
            p_directions = v.findViewById(R.id.tv_directions);
            p_genres = v.findViewById(R.id.tv_genres);
            p_fics = v.findViewById(R.id.tv_fics);
            p_cautions = v.findViewById(R.id.tv_cautions);
            l_genres = v.findViewById(R.id.l_genres);
            l_cautions = v.findViewById(R.id.l_cautions);
            l_holder = v.findViewById(R.id.l_holder);
            p_genres_icon = v.findViewById(R.id.tv_genres_icon);
            p_fandom_icon = v.findViewById(R.id.tv_fandom_icon);
            p_cautions_icon = v.findViewById(R.id.tv_cautions_icon);
            p_directions_icon = v.findViewById(R.id.tv_directions_icon);

            p_genres_icon.setTypeface(Application.getIconFont());
            p_fandom_icon.setTypeface(Application.getIconFont());
            p_cautions_icon.setTypeface(Application.getIconFont());
            p_directions_icon.setTypeface(Application.getIconFont());
            p_genres_icon.setText(String.valueOf((char) 0xed3b));
            p_fandom_icon.setText(String.valueOf((char) 0xe903));
            p_cautions_icon.setText(String.valueOf((char) 0xed4d));
            p_directions_icon.setText(String.valueOf((char) 0xe9fb));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                p_info.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterRequests(Context context, List<? extends Map<String, Object>> data) {
        this.context = context;
        try {
            Requests = (ArrayList<HashMap<String, Object>>) data;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public AdapterRequests.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActivityRequest.class);
            intent.putExtra("id", Objects.requireNonNull(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("id"))).toString());
            intent.putExtra("title", Objects.requireNonNull(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("title"))).toString());
            context.startActivity(intent);
        });

        try {
            holder.p_title.setText(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("title")).toString());
            holder.p_like.setText(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("like")).toString());
            holder.p_bookmark.setText(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("bookmark")).toString());
            holder.p_fandoms.setText(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("fandoms")).toString());

            if (Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("genres")).toString().isEmpty()) {
                holder.l_genres.setVisibility(View.GONE);
            } else {
                holder.l_genres.setVisibility(View.VISIBLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    holder.p_genres.setText(Html.fromHtml(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("genres")).toString(), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    holder.p_genres.setText(Html.fromHtml(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("genres")).toString()));
                }
            }

            if (Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("cautions")).toString().isEmpty()) {
                holder.l_cautions.setVisibility(View.GONE);
            } else {
                holder.l_cautions.setVisibility(View.VISIBLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    holder.p_cautions.setText(Html.fromHtml(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("cautions")).toString(), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    holder.p_cautions.setText(Html.fromHtml(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("cautions")).toString()));
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                holder.p_rating.setText(Html.fromHtml(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("rating")).toString(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.p_rating.setText(Html.fromHtml(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("rating")).toString()));
            }

            holder.p_directions.setText(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("direction")).toString());

            String prepared = Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("description")).toString();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                prepared = prepared.replace("&gt;", "<span style=\"background-color:#dddddd;\">").replace("\n", "</span>\n").replace("<br> <span style=\"background-color:#dddddd;\"></span>", "");
                holder.p_info.setText(Html.fromHtml(prepared, Html.FROM_HTML_MODE_LEGACY, new PicassoImageGetter(holder.p_info), null));
            } else {
                prepared = prepared.replace("&gt;", "<font color='#999999'>").replace("\n", "</font>\n").replace("<br> <font color='#999999'></font>", "");
                holder.p_info.setText(Html.fromHtml(prepared, new PicassoImageGetter(holder.p_info), null));
            }

            if (Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("fics")).toString().isEmpty()) {
                holder.p_fics.setVisibility(View.GONE);
                holder.l_holder.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.fbg_request));
            } else {
                holder.p_fics.setVisibility(View.VISIBLE);
                holder.p_fics.setText(Objects.requireNonNull(Requests.get(holder.getAdapterPosition()).get("fics")).toString());
                holder.l_holder.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.fbg_request_done));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return Requests.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}