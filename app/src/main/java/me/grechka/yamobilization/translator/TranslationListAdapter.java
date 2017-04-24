package me.grechka.yamobilization.translator;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.presenters.TranslationPresenter;

public class TranslationListAdapter extends MvpRecyclerListAdapter<Translation, TranslationPresenter, TranslationViewHolder> {

    @Override
    public TranslationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.translation_row_item, parent, false);
        return new TranslationViewHolder(v);
    }

    @NonNull
    @Override
    protected TranslationPresenter createPresenter(@NonNull Translation model) {
        TranslationPresenter presenter = new TranslationPresenter();
        presenter.setModel(model);
        return presenter;
    }

    @Override
    protected int getModelId(@NonNull Translation model) {
        return model.getId();
    }
}
