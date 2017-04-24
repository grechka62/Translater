package me.grechka.yamobilization.translator;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import me.grechka.yamobilization.translator.presenters.BasePresenter;

abstract class MvpRecyclerAdapter<M, P extends BasePresenter, VH extends MvpViewHolder> extends RecyclerView.Adapter<VH> {
    final Map<Integer, P> presenters;

    MvpRecyclerAdapter() {
        presenters = new HashMap<>();
    }

    @NonNull
    private P getPresenter(@NonNull M model) {
        return presenters.get(getModelId(model));
    }

    @NonNull
    protected abstract P createPresenter(@NonNull M model);

    protected abstract int getModelId(@NonNull M model);


    @Override
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);
        holder.unbindPresenter();
    }

    @Override
    public boolean onFailedToRecycleView(VH holder) {
        holder.unbindPresenter();
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.bindPresenter(getPresenter(getItem(position)));
    }

    protected abstract M getItem(int position);
}
