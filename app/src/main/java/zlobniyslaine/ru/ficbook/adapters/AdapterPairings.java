package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.R;


public class AdapterPairings extends RecyclerView.Adapter<AdapterPairings.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> Pairings;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_name;

        public ViewHolder(View v) {
            super(v);
            p_name = v.findViewById(R.id.tv_name);
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterPairings(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        Pairings = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterPairings.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pairing_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(v -> {
            try {
                Pairings.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            if ((int) Pairings.get(holder.getAdapterPosition()).get("count") == 1) {
                holder.p_name.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_person), null, ContextCompat.getDrawable(context, com.ceylonlabs.imageviewpopup.R.drawable.ic_close_black_24dp), null);
            } else {
                if ((int) Pairings.get(holder.getAdapterPosition()).get("count") == 2) {
                    holder.p_name.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_people), null, ContextCompat.getDrawable(context, com.ceylonlabs.imageviewpopup.R.drawable.ic_close_black_24dp), null);
                } else {
                    holder.p_name.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_group), null, ContextCompat.getDrawable(context, com.ceylonlabs.imageviewpopup.R.drawable.ic_close_black_24dp), null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.p_name.setText(Objects.requireNonNull(Objects.requireNonNull(Pairings.get(holder.getAdapterPosition()).get("title"))).toString());
    }

    @Override
    public int getItemCount() {
        return Pairings.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}