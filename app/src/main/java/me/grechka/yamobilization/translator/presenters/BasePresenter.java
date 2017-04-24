package me.grechka.yamobilization.translator.presenters;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public abstract class BasePresenter<M, V> implements Serializable {
    M model;
    private WeakReference<V> view;

    public void setModel(M model) {
        this.model = model;
        if (setupDone()) {
            updateView();
        }
    }

    public void bindView(@NonNull V view) {
        this.view = new WeakReference<>(view);
        if (setupDone()) {
            updateView();
        }
    }

    public void unbindView() {
        this.view = null;
    }

    V view() {
        if (view == null) {
            return null;
        } else {
            return view.get();
        }
    }

    protected abstract void updateView();

    boolean setupDone() {
        return (view() != null) && (model != null);
    }
}
