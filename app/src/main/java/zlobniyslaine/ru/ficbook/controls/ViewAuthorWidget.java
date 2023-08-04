package zlobniyslaine.ru.ficbook.controls;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import zlobniyslaine.ru.ficbook.ActivityAuthorProfile;
import zlobniyslaine.ru.ficbook.R;
import zlobniyslaine.ru.ficbook.models.Authors;

public class ViewAuthorWidget extends LinearLayout implements View.OnClickListener {

    TextView tv_author_name;
    TextView tv_author_role;
    MLRoundedImageView iv_author_avatar;

    private Context ctx;

    private String author_id;

    public ViewAuthorWidget(Context context) {
        super(context);
        init(context);
    }

    public ViewAuthorWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setAuthorInfo(String id, String name, String role, String url) {
        this.author_id = id;

        tv_author_name.setText(name);
        tv_author_role.setText(role);


        if (url.isEmpty()) {
            Authors col = Authors.getById(author_id);
            if (col != null) {
                url = col.avatar_url;
            } else {
                Authors nw = new Authors();
                nw.name = name;
                nw.avatar_url = "";
                nw.nid = author_id;
                nw.save();
            }
        }

        Authors col = new Authors();
        col.name = name;
        col.avatar_url = url;
        col.nid = author_id;
        col.save();

        if (!url.isEmpty()) {
            try {
                Picasso.with(ctx)
                        .load(url)
                        .into(iv_author_avatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void init(Context context) {
        View rootView = inflate(context, R.layout.widget_author, this);
        tv_author_name = rootView.findViewById(R.id.tv_author_name);
        tv_author_role = rootView.findViewById(R.id.tv_author_role);
        iv_author_avatar = rootView.findViewById(R.id.iv_author_avatar);

        setOnClickListener(this);

        ctx = context;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(ctx, ActivityAuthorProfile.class);
        intent.putExtra("id", author_id);
        ctx.startActivity(intent);
    }
}