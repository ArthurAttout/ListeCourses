package masi.henallux.be.listecourses.touchhelper;

import android.app.ActivityManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import masi.henallux.be.listecourses.ItemListActivity;

/**
 * Created by Arthur on 10-10-17.
 */

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemListActivity.ItemAdapter mAdapter;

    public ItemTouchHelperCallback(ItemListActivity.ItemAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = 0;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        mAdapter.removeItem(viewHolder.getAdapterPosition());
    }


}
