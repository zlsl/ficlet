package zlobniyslaine.ru.ficbook;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ajax.AjaxActionCollectionMove;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionCollectionRemove;
import zlobniyslaine.ru.ficbook.controls.PicassoImageGetter;
import zlobniyslaine.ru.ficbook.models.Collections;
import zlobniyslaine.ru.ficbook.models.Fanfic;
import zlobniyslaine.ru.ficbook.models.FanficPage;


public class FicSimpleAdapter extends RecyclerView.Adapter<FicSimpleAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<HashMap<String, Object>> Fics;
    private final Context context;
    private final Boolean cache;
    private Boolean webreader = false;

    public void UpdateConfig() {
        webreader = Application.sPref.getBoolean("webview_reader", false);
    }

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_title;
        final ImageView p_cover;
        final TextView p_size;
        final TextView p_sizetype;
        final TextView p_rating;
        final TextView p_genres;
        final TextView p_fandom;
        final TextView p_pairings;
        final TextView p_cautions;
        final TextView p_tags;
        final TextView p_authors;
        final TextView p_info;
        final TextView p_direction;
        final TextView p_sup;
        final TextView p_collection;
        final TextView p_genres_icon;
        final TextView p_fandom_icon;
        final TextView p_pairings_icon;
        final TextView p_cautions_icon;
        final TextView p_tags_icon;
        final TextView p_new_content;
        final TextView p_new_parts;
        final TextView p_critic;
        final TextView p_trophy_icon;
        final TextView p_trophy;
        final LinearLayout l_genres;
        final LinearLayout l_pairings;
        final LinearLayout l_cautions;
        final LinearLayout l_tags;
        final ProgressBar read_progress;
        final LinearLayout layout_direction;
        final LinearLayout layout_bg;
        final CardView fcard;
        final View p_deleted;

        ViewHolder(View v) {
            super(v);

            p_title = v.findViewById(R.id.tv_title);
            p_cover = v.findViewById(R.id.cover);
            p_size = v.findViewById(R.id.tv_size);
            p_sizetype = v.findViewById(R.id.tv_sizetype);
            p_rating = v.findViewById(R.id.tv_rating);
            p_genres = v.findViewById(R.id.tv_genres);
            p_fandom = v.findViewById(R.id.tv_fandoms);
            p_pairings = v.findViewById(R.id.tv_pairings);
            p_cautions = v.findViewById(R.id.tv_cautions);
            p_tags = v.findViewById(R.id.tv_tags);
            p_authors = v.findViewById(R.id.tv_authors);
            p_info = v.findViewById(R.id.tv_info);
            p_direction = v.findViewById(R.id.tv_direction);
            p_sup = v.findViewById(R.id.tv_sup);
            p_collection = v.findViewById(R.id.tv_collection);
            p_genres_icon = v.findViewById(R.id.tv_genres_icon);
            p_fandom_icon = v.findViewById(R.id.tv_fandom_icon);
            p_pairings_icon = v.findViewById(R.id.tv_pairings_icon);
            p_cautions_icon = v.findViewById(R.id.tv_cautions_icon);
            p_tags_icon = v.findViewById(R.id.tv_tags_icon);
            p_new_content = v.findViewById(R.id.tv_new_content);
            p_new_parts = v.findViewById(R.id.tv_new_parts);
            p_critic = v.findViewById(R.id.tv_critic);
            l_genres = v.findViewById(R.id.l_genres);
            l_pairings = v.findViewById(R.id.l_pairings);
            l_cautions = v.findViewById(R.id.l_cautions);
            l_tags = v.findViewById(R.id.l_tags);
            read_progress = v.findViewById(R.id.read_progress);
            layout_direction = v.findViewById(R.id.layout_direction);
            layout_bg = v.findViewById(R.id.ff_bg);
            fcard = v.findViewById(R.id.fcard);
            p_trophy_icon = v.findViewById(R.id.tv_trophy_icon);
            p_trophy = v.findViewById(R.id.tv_trophy);
            p_deleted = v.findViewById(R.id.view_deleted);

            p_direction.setTypeface(Application.getIconFont());
            p_sizetype.setTypeface(Application.getIconFont());

            p_genres_icon.setTypeface(Application.getIconFont());
            p_fandom_icon.setTypeface(Application.getIconFont());
            p_pairings_icon.setTypeface(Application.getIconFont());
            p_cautions_icon.setTypeface(Application.getIconFont());
            p_tags_icon.setTypeface(Application.getIconFont());
            p_trophy_icon.setTypeface(Application.getIconFont());

            p_genres_icon.setText(String.valueOf((char) 0xed3b));
            p_fandom_icon.setText(String.valueOf((char) 0xe903));
            p_pairings_icon.setText(String.valueOf((char) 0xeafe));
            p_cautions_icon.setText(String.valueOf((char) 0xed4d));
            p_tags_icon.setText(String.valueOf((char) 0xecb5));
            p_trophy_icon.setText(String.valueOf((char) 0xeba4));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                p_info.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                p_tags.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                p_cautions.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                p_genres.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                p_pairings.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }
        }
    }

    @SuppressWarnings("unchecked")
    FicSimpleAdapter(Context context, List<? extends Map<String, Object>> data, Boolean cache_mode) {
        this.context = context;
        cache = cache_mode;

        UpdateConfig();

        try {
            Fics = (ArrayList<HashMap<String, Object>>) data;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public FicSimpleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fic, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final ViewHolder holder, final int position) {
        if (Fics.size() == 0) {
            return;
        }

        Fanfic fanfic;
        try {
            fanfic = Fanfic.getById(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (fanfic == null) {
            return;
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ActivityFanfic.class);
                if (Fics.size() >= holder.getAdapterPosition()) {
                    intent.putExtra("id", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString());
                    intent.putExtra("title", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("title")).toString());
                }
                if (!Application.device_ok) {
                    pinAlert();
                } else {
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            try {
                if (!cache) {
                    Intent intent = new Intent(context, ActivityReader.class);
                    intent.putExtra("id", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString());
                    intent.putExtra("title", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("title")).toString());
                    if (!Application.device_ok) {
                        pinAlert();
                    } else {
                        context.startActivity(intent);
                    }
                } else {
                    String[] items = {"Читалка", "Удалить из кэша"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("");
                    builder.setItems(items, (dialog, item) -> {
                        switch (item) {
                            case 0:
                                try {
                                    Intent intent = new Intent(context, ActivityReader.class);
                                    intent.putExtra("id", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString());
                                    intent.putExtra("title", Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("title")).toString());
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;

                            case 1:
                                try {
                                    File ff = new File(context.getFilesDir() + File.separator + Fics.get(holder.getAdapterPosition()).get("id") + "_.pagesz");
                                    if (!ff.delete()) {
                                        Log.w("TMP", "delete failed");
                                    }
                                    ff = new File(context.getFilesDir() + File.separator + Fics.get(holder.getAdapterPosition()).get("id") + "_.vpagesz");
                                    if (!ff.delete()) {
                                        Log.w("TMP", "delete failed");
                                    }
                                    ff = new File(context.getFilesDir() + File.separator + Fics.get(holder.getAdapterPosition()).get("id") + "_.rawz");
                                    if (!ff.delete()) {
                                        Log.w("TMP", "delete failed");
                                    }
                                    ff = new File(context.getFilesDir() + File.separator + Fics.get(holder.getAdapterPosition()).get("id") + "_.fb2.zip");
                                    if (!ff.delete()) {
                                        Log.w("TMP", "delete failed");
                                    }
                                    ff = new File(context.getFilesDir() + File.separator + Fics.get(holder.getAdapterPosition()).get("id") + "_.fb2");
                                    if (!ff.delete()) {
                                        Log.w("TMP", "delete failed");
                                    }

                                    Fics.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    });
                    AlertDialog alert = builder.create();
                    if (!Application.device_ok) {
                        pinAlert();
                    } else {
                        alert.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        try {
            Boolean deleted = false;
            if (fanfic.title.contains("[DELETED]")) {
                holder.p_deleted.setVisibility(View.VISIBLE);
                fanfic.title = fanfic.title.replace("[DELETED]", "");
            } else {
                holder.p_deleted.setVisibility(View.GONE);
            }
            holder.p_title.setText(fanfic.title);
            holder.p_authors.setText(fanfic.authors);

            if (fanfic.direction.equals("gen") || fanfic.direction.equals("Джен")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_gen));
                holder.p_direction.setText(String.valueOf((char) 0xec38));
            }
            if (fanfic.direction.equals("het") || fanfic.direction.equals("Гет")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_het));
                holder.p_direction.setText(String.valueOf((char) 0xecfa));
            }
            if (fanfic.direction.equals("other") || fanfic.direction.equals("Прочее")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_other));
                holder.p_direction.setText(String.valueOf((char) 0xee70));
            }
            if (fanfic.direction.equals("slash") || fanfic.direction.equals("Слэш")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_slash));
                holder.p_direction.setText(String.valueOf((char) 0xecfb));
            }
            if (fanfic.direction.equals("femslash") || fanfic.direction.equals("Фемслэш")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_femslash));
                holder.p_direction.setText(String.valueOf((char) 0xecfc));
            }
            if (fanfic.direction.equals("mixed") || fanfic.direction.equals("Смешанная")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_mixed));
                holder.p_direction.setText(String.valueOf((char) 0xedac));
            }
            if (fanfic.direction.equals("article") || fanfic.direction.equals("Статья")) {
                holder.layout_direction.setBackgroundColor(ContextCompat.getColor(context, R.color.fbg_article));
                holder.p_direction.setText(String.valueOf((char) 0xea6f));
            }

            if (fanfic.sizetype.equals("Драббл")) {
                holder.p_sizetype.setText(String.valueOf((char) 0xe998));
            }
            if (fanfic.sizetype.equals("Мини")) {
                holder.p_sizetype.setText(String.valueOf((char) 0xe997));
            }
            if (fanfic.sizetype.equals("Миди")) {
                holder.p_sizetype.setText(String.valueOf((char) 0xe996));
            }
            if (fanfic.sizetype.equals("Макси")) {
                holder.p_sizetype.setText(String.valueOf((char) 0xe993));
            }

            holder.p_title.setTextColor(ContextCompat.getColor(context, R.color.ftitle_finished));
            if (fanfic.status.equals("в процессе")) {
                holder.p_title.setTextColor(ContextCompat.getColor(context, R.color.ftitle_process));
            }
            if (fanfic.status.equals("заморожен")) {
                holder.p_title.setTextColor(ContextCompat.getColor(context, R.color.ftitle_frozed));
            }

            holder.p_fandom.setText(fanfic.fandom);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.p_rating.setText(Html.fromHtml(fanfic.rating, Html.FROM_HTML_MODE_LEGACY));
            } else {
                holder.p_rating.setText(Html.fromHtml(fanfic.rating));
            }

            holder.p_size.setText(fanfic.size);
            holder.p_sup.setText(fanfic.sup);

            if (fanfic.pairings.isEmpty()) {
                holder.p_pairings.setText("");
                holder.l_pairings.setVisibility(View.GONE);
            } else {
                holder.p_pairings.setText(fanfic.pairings);
                holder.l_pairings.setVisibility(View.VISIBLE);
            }

            if (fanfic.cautions.isEmpty()) {
                holder.l_cautions.setVisibility(View.GONE);
                holder.p_cautions.setText("");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.p_cautions.setText(Html.fromHtml(fanfic.cautions, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    holder.p_cautions.setText(Html.fromHtml(fanfic.cautions));
                }
                holder.l_cautions.setVisibility(View.VISIBLE);
            }

            try {
                if (fanfic.tags.isEmpty()) {
                    holder.l_tags.setVisibility(View.GONE);
                    holder.p_tags.setText("");
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        holder.p_tags.setText(Html.fromHtml(fanfic.tags, Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        holder.p_tags.setText(Html.fromHtml(fanfic.tags));
                    }
                    holder.l_tags.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (fanfic.genres.isEmpty()) {
                holder.l_genres.setVisibility(View.GONE);
                holder.p_genres.setText("");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.p_genres.setText(Html.fromHtml(fanfic.genres, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    holder.p_genres.setText(Html.fromHtml(fanfic.genres));
                }
                holder.l_genres.setVisibility(View.VISIBLE);
            }

            String prepared = fanfic.info;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                prepared = prepared
                        .replace("&gt;", "<span style=\"background-color:#dddddd;\">")
                        .replace("\n", "</span>\n")
                        .replace("font-size", "")
                        .replace("<br> <span style=\"background-color:#dddddd;\"></span>", "");
                holder.p_info.setText(Html.fromHtml(prepared, Html.FROM_HTML_MODE_LEGACY, new PicassoImageGetter(holder.p_info), null));
            } else {
                prepared = prepared
                        .replace("&gt;", "<font color='#999999'>")
                        .replace("\n", "</font>\n")
                        .replace("font-size", "")
                        .replace("<br> <font color='#999999'></font>", "");
                holder.p_info.setText(Html.fromHtml(prepared, new PicassoImageGetter(holder.p_info), null));
            }

            if (fanfic.collection_id.isEmpty()) {
                holder.p_collection.setVisibility(View.GONE);
                holder.p_collection.setText("");
            } else {
                holder.p_collection.setVisibility(View.VISIBLE);
                holder.p_collection.setText(Collections.getById(fanfic.collection_id));
                holder.p_collection.setOnClickListener(v -> {
                    try {
                        final List<Collections> collections = Collections.getAll();
                        String[] items = new String[collections.size() + 1];
                        items[0] = "Удалить из сборника";
                        for (int i = 0; i < collections.size(); i++) {
                            items[i + 1] = "В " + collections.get(i).title;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Операции с сборниками");
                        builder.setItems(items, (dialog, item) -> {
                            Log.i("itm", item + " ");
                            if (item == 0) {
                                AjaxActionCollectionRemove action = new AjaxActionCollectionRemove();
                                action.Do(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString(), Fanfic.getCollectionId(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString()));
                                Fanfic.setCollectionId(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString(), "");
                            } else {
                                AjaxActionCollectionMove action = new AjaxActionCollectionMove();
                                action.Do(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString(), Fanfic.getCollectionId(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString()), collections.get(item - 1).nid);
                                Fanfic.setCollectionId(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("id")).toString(), collections.get(item - 1).nid);
                            }
                            notifyDataSetChanged();
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            FanficPage fp = FanficPage.getLastPage(fanfic.nid);
            if (fp != null) {
                holder.read_progress.setMax(fp.page_count);
                holder.read_progress.setProgress(fp.page_number + 1);
                if (webreader) {
                    if ((fp.scroll_position != null) && (fp.scroll_max != null)) {
                        holder.read_progress.setMax(fp.scroll_max);
                        holder.read_progress.setProgress(fp.scroll_position);
                    }
                }
                holder.read_progress.setVisibility(View.VISIBLE);
            } else {
                holder.read_progress.setVisibility(View.GONE);
            }

            holder.p_new_content.setVisibility(View.GONE);
            holder.p_new_parts.setVisibility(View.GONE);
            holder.p_critic.setVisibility(View.GONE);

            if (!fanfic.critic.isEmpty()) {
                holder.p_critic.setVisibility(View.VISIBLE);
            }

            if (!fanfic.new_part.isEmpty()) {
                holder.p_new_parts.setVisibility(View.VISIBLE);
                holder.p_new_parts.setText(fanfic.new_part);
            } else {
                holder.p_new_parts.setText("");
            }

            if (!fanfic.date_changes.isEmpty()) {
                if (!fanfic.new_content.isEmpty()) {
                    holder.fcard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ff_bg_updated));
                    holder.p_new_content.setVisibility(View.VISIBLE);
                    holder.p_new_content.setText(String.format("Обновлено %s", fanfic.new_content));
                    holder.p_new_content.setTextColor(context.getResources().getColor(R.color.colorAccent));
                } else {
                    holder.fcard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ff_bg_readed));
                    holder.p_new_content.setVisibility(View.VISIBLE);
                    holder.p_new_content.setText(String.format("Прочитано %s", fanfic.date_changes));
                    holder.p_new_content.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                }
            } else {
                holder.fcard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ff_bg));
            }

            if (!fanfic.bad.isEmpty()) {
                holder.layout_bg.setAlpha(0.4f);
            } else {
                holder.layout_bg.setAlpha(1f);
            }

            holder.p_trophy_icon.setVisibility(View.GONE);
            holder.p_trophy.setVisibility(View.GONE);
            if (fanfic.trophy != null) {
                if ( (!fanfic.trophy.equals("")) && (!fanfic.trophy.equals("0")) ) {
                    holder.p_trophy_icon.setVisibility(View.VISIBLE);
                    holder.p_trophy.setVisibility(View.VISIBLE);
                    holder.p_trophy.setText(fanfic.trophy);
                }
            }

            if (Fics.size() <= holder.getAdapterPosition()) {
                return;
            }

            if ((Application.sPref.getBoolean("show_covers", false)) && (Fics.get(holder.getAdapterPosition()).containsKey("cover"))) {
                if (!Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("cover")).toString().isEmpty()) {
                    try {
                        Picasso.with(context)
                                .load(Objects.requireNonNull(Fics.get(holder.getAdapterPosition()).get("cover")).toString())
                                .into(holder.p_cover);
                        holder.p_cover.setClipToOutline(true);
                        holder.p_cover.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    holder.p_cover.setVisibility(View.GONE);
                }
            } else {
                holder.p_cover.setVisibility(View.GONE);
            }
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

    private void pinAlert() {
        androidx.appcompat.app.AlertDialog.Builder pinDialog = new androidx.appcompat.app.AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View alertDialogView = inflater.inflate(R.layout.dialog_pin, null);
        pinDialog.setView(alertDialogView);

        final TextView uuid = alertDialogView.findViewById(R.id.uuid);
        uuid.setText(Application.device_id);

        pinDialog.setPositiveButton("Ok", (dialog, which) -> new Thread(() -> {
            Application.syncCache();
            Log.d("SYNC", "pack");
        }).start());
        pinDialog.show();
    }
}