package me.grechka.yamobilization.translator;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import me.grechka.yamobilization.translator.presenters.BasePresenter;

abstract class MvpViewHolder<P extends BasePresenter> extends RecyclerView.ViewHolder {
    P presenter;

    MvpViewHolder(View itemView) {
        super(itemView);
    }

    void bindPresenter(P presenter) {
        this.presenter = presenter;
        presenter.bindView(this);
    }

    void unbindPresenter() {
        presenter = null;
    }
}
