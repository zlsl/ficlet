package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import zlobniyslaine.ru.ficbook.R;


public class AdapterParagraphs extends RecyclerView.Adapter<AdapterParagraphs.ViewHolder> implements View.OnClickListener {

    private final List<String> Paragraphs;
    private final Context context;
    private int itemSelected = 0;

    private AdapterParagraphs.OnClickListener onClickListener;


    public interface OnClickListener {
        void onClickEvent(Integer position);
    }

    public void setOnClickListener(AdapterParagraphs.OnClickListener listener) {
        onClickListener = listener;
    }

    public void setCurrentItem(int item) {
        try {
            notifyItemChanged(itemSelected);
        } catch (Exception e) {
            e.printStackTrace();
        }
        itemSelected = item;
        notifyItemChanged(itemSelected);
    }

// --Commented out by Inspection START (16.07.20 21:55):
//    public int getCurrentItem() {
//        return itemSelected;
//    }
// --Commented out by Inspection STOP (16.07.20 21:55)

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_text;

        public ViewHolder(View v) {
            super(v);
            p_text = v.findViewById(R.id.tv_text);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                p_text.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }
        }
    }

    public AdapterParagraphs(Context context, List<String> data) {
        this.context = context;
        Paragraphs = data;
    }

    @NotNull
    @Override
    public AdapterParagraphs.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.paragraph_list_item, parent, false);
        v.setClickable(true);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.itemView.setSelected(itemSelected == position);

        holder.itemView.setOnClickListener(v -> {
            itemSelected = holder.getAdapterPosition();
            onClickListener.onClickEvent(holder.getAdapterPosition());
        });
        try {
            holder.p_text.setText(String.format("\t\t\t\t\t\t%s", Paragraphs.get(holder.getAdapterPosition())));
        } catch (Exception e) {
            holder.p_text.setText("");
        }

        if (position == itemSelected) {
            holder.p_text.setTextColor(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.p_text.setTextColor(context.getResources().getColor(R.color.text_day_color));
        }
    }

    @Override
    public int getItemCount() {
        return Paragraphs.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}