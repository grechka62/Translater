package me.grechka.yamobilization.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.grechka.yamobilization.translator.presenters.BasePresenter;

abstract class MvpRecyclerListAdapter<M, P extends BasePresenter, VH extends MvpViewHolder<P>> extends MvpRecyclerAdapter<M, P, VH> {
    private final List<M> models;

    MvpRecyclerListAdapter() {
        models = new ArrayList<>();
    }

    void clearAndAddAll(Collection<M> data) {
        models.clear();
        presenters.clear();
        for (M item : data) {
            addInternal(item);
        }
        notifyDataSetChanged();
    }

    private void addInternal(M item) {
        models.add(item);
        presenters.put(getModelId(item), createPresenter(item));
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @Override
    protected M getItem(int position) {
        return models.get(position);
    }
}