package me.grechka.yamobilization.translator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.presenters.TranslationListPresenter;
import me.grechka.yamobilization.translator.views.TranslationListView;

/* Фрагмент, отображающий RecyclerView */

public class TranslationListFragment extends Fragment implements TranslationListView {
    private static final String POSITION = "position";
    private static TranslationListPresenter presenter;
    private TranslationListAdapter adapter;

    public static TranslationListFragment newInstance(int position) {
        TranslationListFragment fragment = new TranslationListFragment();
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int position;
        if (getArguments() != null)
            position = getArguments().getInt(POSITION);
        else
            position = 0;
        presenter = new TranslationListPresenter(position);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_view_layout, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TranslationListAdapter();
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.unbindView();
    }

    @Override
    public void showTranslations(List<Translation> translations) {
        adapter.clearAndAddAll(translations);
    }
}