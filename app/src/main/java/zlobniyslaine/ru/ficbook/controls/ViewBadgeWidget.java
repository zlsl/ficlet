package zlobniyslaine.ru.ficbook.controls;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import zlobniyslaine.ru.ficbook.ActivityFanficList;
import zlobniyslaine.ru.ficbook.Application;
import zlobniyslaine.ru.ficbook.R;

public class ViewBadgeWidget extends LinearLayout implements View.OnClickListener {

    TextView tv_text;
    private Context ctx;

    private String text, url, builder, hint;
    private GradientDrawable gradientDrawable;

    public ViewBadgeWidget(Context context) {
        super(context);
        init(context);
    }

    public ViewBadgeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setWidgetInfo(String builder, String text, String url, String hint) {
        this.text = text;
        this.hint = hint;
        this.url = url;
        this.builder = builder;

        if (text.contains("]")) {
            tv_text.setAlpha(0.5f);
            text = text.replace("]", "");
        }
        if (text.contains("[")) {
            tv_text.setTypeface(tv_text.getTypeface(), Typeface.BOLD);
            text = text.replace("[", "");
        }

        tv_text.setText(text);

        switch (builder) {
            case "fandom":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_fandom));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_fandom));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text_fandom));
                break;
            case "tag":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_genre));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_genre));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text));
                break;
            case "character":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_character));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_character));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text));
                break;
            case "pairing":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_pairing));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_pairing));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text));
                break;
            case "caution":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_caution));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_caution));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text));
                break;
            case "rating":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_rating));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_rating));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text));
                break;
            case "direction":
                tv_text.setBackgroundColor(ContextCompat.getColor(Application.getContext(), R.color.badge_direction));
                gradientDrawable.setColor(ContextCompat.getColor(Application.getContext(), R.color.badge_direction));
                tv_text.setTextColor(ContextCompat.getColor(Application.getContext(), R.color.badge_text));
                break;
        }
    }

    private void init(Context context) {
        View rootView;
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(5);
        rootView = inflate(context, R.layout.widget_badge, this);

        tv_text = rootView.findViewById(R.id.tv_text);
        setOnClickListener(this);

        ctx = context;
    }

    @Override
    public void onClick(View view) {
        if (hint.isEmpty()) {
            if (builder.equals("fandom")) {
                Intent intent = new Intent(ctx, ActivityFanficList.class);
                intent.putExtra("url", url + "?p=@");
                intent.putExtra("title", text);
                ctx.startActivity(intent);
            }
            if (builder.equals("genre")) {
                Intent intent = new Intent(ctx, ActivityFanficList.class);
                intent.putExtra("url", url + "?p=@");
                intent.putExtra("title", text);
                ctx.startActivity(intent);
            }
            if (builder.equals("pairing")) {
                Intent intent = new Intent(ctx, ActivityFanficList.class);
                intent.putExtra("url", url + "?p=@");
                intent.putExtra("title", text);
                ctx.startActivity(intent);
            }
            if (builder.equals("character")) {
                Intent intent = new Intent(ctx, ActivityFanficList.class);
                intent.putExtra("url", url + "?p=@");
                intent.putExtra("title", text);
                ctx.startActivity(intent);
            }
            if (builder.equals("tag")) {
                Intent intent = new Intent(ctx, ActivityFanficList.class);
                intent.putExtra("url", url + "?p=@");
                intent.putExtra("title", text);
                ctx.startActivity(intent);
            }
        } else {
            Toast.makeText(getContext(), hint, Toast.LENGTH_SHORT).show();
        }
    }
}