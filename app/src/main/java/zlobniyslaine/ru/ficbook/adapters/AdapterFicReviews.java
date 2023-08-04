package zlobniyslaine.ru.ficbook.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import zlobniyslaine.ru.ficbook.ActivityAuthorProfile;
import zlobniyslaine.ru.ficbook.ActivityFanfic;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.ajax.AjaxActionReplyReview;
import zlobniyslaine.ru.ficbook.controls.PicassoImageGetter;
import zlobniyslaine.ru.ficbook.controls.TextViewLinkHandler;
import zlobniyslaine.ru.ficbook.models.Parts;


public class AdapterFicReviews extends RecyclerView.Adapter<AdapterFicReviews.ViewHolder> implements View.OnClickListener {

    private final ArrayList<HashMap<String, Object>> FicReviews;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_author_name;
        final TextView p_datetime;
        final TextView p_content;
        final TextView p_fic;
        final ImageView i_avatar;
        final ImageView i_reply;

        public ViewHolder(View v) {
            super(v);
            p_author_name = v.findViewById(R.id.f_author_name);
            p_datetime = v.findViewById(R.id.f_datretime);
            p_content = v.findViewById(R.id.f_content);
            p_fic = v.findViewById(R.id.f_ficname);
            i_avatar = v.findViewById(R.id.i_author_avatar);
            i_reply = v.findViewById(R.id.iv_reply);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                p_content.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public AdapterFicReviews(Context context, List<? extends Map<String, ?>> data) {
        this.context = context;
        FicReviews = (ArrayList<HashMap<String, Object>>) data;
    }

    @NotNull
    @Override
    public AdapterFicReviews.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fic_review, parent, false);

        return new ViewHolder(v);
    }

    @SuppressLint("InflateParams")
    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.itemView.setOnClickListener(v -> {
        });
        try {
            holder.p_author_name.setText(Objects.requireNonNull(FicReviews.get(position).get("author_name")).toString());
            holder.p_datetime.setText(Objects.requireNonNull(FicReviews.get(position).get("datetime")).toString());
            holder.p_fic.setText(Objects.requireNonNull(FicReviews.get(position).get("fic_name")).toString());

            holder.p_fic.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(context, ActivityFanfic.class);
                    intent.putExtra("id", Objects.requireNonNull(FicReviews.get(position).get("fic_id")).toString());
                    intent.putExtra("title", Objects.requireNonNull(FicReviews.get(position).get("fic_name")).toString());
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.p_author_name.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(context, ActivityAuthorProfile.class);
                    intent.putExtra("id", Objects.requireNonNull(FicReviews.get(position).get("author_id")).toString());
                    intent.putExtra("url", "https://ficbook.net/authors/" + Objects.requireNonNull(FicReviews.get(position).get("author_id")).toString());
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.i_avatar.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(context, ActivityAuthorProfile.class);
                    intent.putExtra("id", Objects.requireNonNull(FicReviews.get(position).get("author_id")).toString());
                    intent.putExtra("url", "https://ficbook.net/authors/" + Objects.requireNonNull(FicReviews.get(position).get("author_id")).toString());
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (!Application.isGuest()) {
                holder.i_reply.setOnClickListener(v -> {
                    try {
                        AlertDialog.Builder replyDialog = new AlertDialog.Builder(context);
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        View alertDialogView;
                        if (inflater != null) {
                            alertDialogView = inflater.inflate(R.layout.reply_dialog, null);
                        } else {
                            return;
                        }
                        replyDialog.setView(alertDialogView);

                        final EditText reply_text = alertDialogView.findViewById(R.id.reply_text);
                        final CheckBox cb_follow = alertDialogView.findViewById(R.id.cb_set_follow);
                        final Spinner spinner_part = alertDialogView.findViewById(R.id.spinner_part);

                        String part_id = Objects.requireNonNull(FicReviews.get(position).get("part_id")).toString();
                        List<Parts> parts = Parts.getParts(Objects.requireNonNull(FicReviews.get(position).get("fic_id")).toString());
                        final ArrayList<String> array_parts = new ArrayList<>();
                        int idx = 0;
                        int init_idx = 0;
                        for (Parts p : parts) {
                            array_parts.add(p.title);
                            if (p.nid.equals(part_id)) {
                                init_idx = idx;
                            }
                            idx++;
                        }

                        spinner_part.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                String pid = Parts.getIdByName(array_parts.get(pos), Objects.requireNonNull(FicReviews.get(position).get("fic_id")).toString());
                                FicReviews.get(position).put("part_id", pid);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                            }
                        });

                        ArrayAdapter<String> ad_parts = new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, array_parts);
                        ad_parts.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
                        spinner_part.setAdapter(ad_parts);
                        spinner_part.setSelection(init_idx);

                        String quoted = ">**" + Objects.requireNonNull(FicReviews.get(position).get("author_name")).toString() + "**\n";
                        quoted = quoted + ">" + Objects.requireNonNull(FicReviews.get(position).get("content")).toString().replaceAll("\n", "\n>").replaceAll("<br>", "") + "\n\n";

                        reply_text.setText(quoted);
                        reply_text.setSelection(reply_text.getText().length());

                        replyDialog.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

                        replyDialog.setPositiveButton(
                                "Отправить",
                                (dialog, which) -> {
                                    AjaxActionReplyReview action = new AjaxActionReplyReview();
                                    try {
                                        action.Do(Objects.requireNonNull(FicReviews.get(position).get("part_id")).toString(), reply_text.getText().toString(), cb_follow.isChecked());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                        replyDialog.setCancelable(false);
                        replyDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                holder.i_reply.setVisibility(View.VISIBLE);
            } else {
                holder.i_reply.setVisibility(View.GONE);
            }

            String prepared = Objects.requireNonNull(FicReviews.get(position).get("content")).toString();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                prepared = prepared.replace("&gt;", "<span style=\"background-color:#dddddd;\">").replace("\n", "</span>\n").replace("<br> <span style=\"background-color:#dddddd;\"></span>", "");
                holder.p_content.setText(Html.fromHtml(prepared, Html.FROM_HTML_MODE_LEGACY, new PicassoImageGetter(holder.p_content), null));
            } else {
                prepared = prepared.replace("&gt;", "<font color='#999999'>").replace("\n", "</font>\n").replace("<br> <font color='#999999'></font>", "");
                holder.p_content.setText(Html.fromHtml(prepared, new PicassoImageGetter(holder.p_content), null));
            }

            holder.p_content.setMovementMethod(new TextViewLinkHandler() {
                @Override
                public void onLinkClick(String url) {
                    Application.openUrl(url, context);
                }
            });

            if (!Objects.requireNonNull(FicReviews.get(position).get("author_avatar_url")).toString().isEmpty()) {
                Picasso.with(context)
                        .load(Objects.requireNonNull(FicReviews.get(position).get("author_avatar_url")).toString())
                        .into(holder.i_avatar);
                holder.i_avatar.setVisibility(View.VISIBLE);
            } else {
                holder.i_avatar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return FicReviews.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

}
