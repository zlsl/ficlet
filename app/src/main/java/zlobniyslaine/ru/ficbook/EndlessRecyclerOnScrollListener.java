package zlobniyslaine.ru.ficbook;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    int visibleItemCount;
    int totalItemCount;

    private final WrapContentLinearLayoutManager mLinearLayoutManager;

    EndlessRecyclerOnScrollListener(WrapContentLinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        int visibleThreshold = 5;
        if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            try {
                onLoadMore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onLoadMore();
}