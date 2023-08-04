package zlobniyslaine.ru.ficbook.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityChanges;
import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.ActivityFicByRequestList;
import zlobniyslaine.ru.ficbook.ActivityMessagingList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.MainActivity;
import zlobniyslaine.ru.ficbook.R;


public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> NotificationsList;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView iv_icon;
        final TextView tv_text;

        public ViewHolder(View v) {
            super(v);
            iv_icon = v.findViewById(R.id.iv_icon);
            tv_text = v.findViewById(R.id.tv_text);
        }
    }

    public AdapterNotifications(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        //noinspection unchecked
        NotificationsList = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterNotifications.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final ViewHolder holder, final int position) {
        try {
            holder.itemView.setOnClickListener(v -> {
                Intent notificationIntent;

                switch (Integer.parseInt(Objects.requireNonNull(NotificationsList.get(holder.getAdapterPosition()).get("type")).toString())) {
                    case Application.NOTIFICATION_FAVOURITES:
                        notificationIntent = new Intent(context, ActivityFanficList.class);
                        notificationIntent.putExtra("url", "https://ficbook.net/home/favourites?p=@&updatelist");
                        notificationIntent.putExtra("title", "Лента избранных");
                        notificationIntent.setAction(Intent.ACTION_DEFAULT);
                        notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        break;

                    case Application.NOTIFICATION_PARTICIPATED:
                        notificationIntent = new Intent(context, ActivityChanges.class);
                        notificationIntent.putExtra("url", "https://ficbook.net/home/versions/participated?p=@");
                        notificationIntent.putExtra("title", "Изменения");
                        notificationIntent.setAction(Intent.ACTION_MAIN);
                        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        break;

                    case Application.NOTIFICATION_DIALOG:
                        notificationIntent = new Intent(context, ActivityMessagingList.class);
                        notificationIntent.setAction(Intent.EXTRA_TEXT);
                        notificationIntent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                        break;

                    case Application.NOTIFICATION_NEW_PART:
                        notificationIntent = new Intent(context, ActivityFanficList.class);
                        notificationIntent.putExtra("url", "https://ficbook.net/home/collections?type=update&p=@");
                        notificationIntent.putExtra("title", "Лента избранных");
                        notificationIntent.setAction(Intent.ACTION_DEFAULT);
                        notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        break;

                    case Application.NOTIFICATION_FANFIC_BY_INT:
                        notificationIntent = new Intent(context, ActivityFicByRequestList.class);
                        notificationIntent.putExtra("url", "https://ficbook.net/home/liked_requests?p=@");
                        notificationIntent.putExtra("title", "Фанфики по интересным заявкам");
                        notificationIntent.setAction(Intent.ACTION_DEFAULT);
                        notificationIntent.addCategory(Intent.CATEGORY_DEFAULT);
                        break;

                    default:
                        notificationIntent = new Intent(context, MainActivity.class);
                        notificationIntent.setAction(Intent.ACTION_MAIN);
                        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                }
                context.startActivity(notificationIntent);
            });

            holder.tv_text.setText(Objects.requireNonNull(NotificationsList.get(holder.getAdapterPosition()).get("text")).toString());
            switch (Integer.parseInt(Objects.requireNonNull(NotificationsList.get(holder.getAdapterPosition()).get("type")).toString())) {
                case Application.NOTIFICATION_FAVOURITES:
                    holder.iv_icon.setImageResource(R.drawable.ic_favorite_black_24dp);
                    break;

                case Application.NOTIFICATION_PARTICIPATED:
                    holder.iv_icon.setImageResource(R.drawable.ic_grade_black_24dp);
                    break;

                case Application.NOTIFICATION_DIALOG:
                    holder.iv_icon.setImageResource(R.drawable.ic_announcement_black_24dp);
                    break;

                case Application.NOTIFICATION_NEW_PART:
                    holder.iv_icon.setImageResource(R.drawable.ic_receipt_black_24dp);
                    break;

                default:
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return NotificationsList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}