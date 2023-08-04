package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
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

import zlobniyslaine.ru.ficbook.ActivityFanfic;
import zlobniyslaine.ru.ficbook.ActivityRequest;
import zlobniyslaine.ru.ficbook.R;


public class AdapterFicByReq extends RecyclerView.Adapter<AdapterFicByReq.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> Fics;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_fic_title;
        final TextView p_request_title;
        final TextView p_date;

        public ViewHolder(View v) {
            super(v);
            p_fic_title = v.findViewById(R.id.f_fic_title);
            p_request_title = v.findViewById(R.id.f_request_title);
            p_date = v.findViewById(R.id.f_date);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterFicByReq(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        Fics = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterFicByReq.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fic_by_request, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityFanfic.class);
                intent.putExtra("id", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("fic_id")).toString());
                intent.putExtra("title", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("fic_title")).toString());
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        holder.p_request_title.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityRequest.class);
                intent.putExtra("id", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("request_id")).toString());
                intent.putExtra("title", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("request_title")).toString());
                context.startActivity(intent);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            holder.p_fic_title.setText(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("fic_title")).toString());
            holder.p_request_title.setText(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("request_title")).toString());
            holder.p_date.setText(String.format("%s по заявке:", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("date")).toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return Fics.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}