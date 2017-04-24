package me.grechka.yamobilization.translator;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.presenters.ResultPresenter;
import me.grechka.yamobilization.translator.views.ResultView;

import static me.grechka.yamobilization.translator.TranslaterApp.PRESENTER;

/* Фрагмент отображает результат перевода и передает презентеру запрос
 * на добавление/удаление избранного */

public class ResultFragment extends Fragment implements ResultView {
    private TextView result;
    private ImageButton isFavorite;

    private static ResultPresenter presenter;

    public static ResultFragment newInstance(Translation translation) {
        Bundle args = new Bundle();
        presenter = new ResultPresenter();
        presenter.setModel(translation);
        args.putSerializable(PRESENTER, presenter);
        ResultFragment resultFragment = new ResultFragment();
        resultFragment.setArguments(args);
        return resultFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = (ResultPresenter) getArguments().getSerializable(PRESENTER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translation, container, false);
        result = (TextView) view.findViewById(R.id.result);
        isFavorite = (ImageButton) view.findViewById(R.id.is_favorite_button);
        isFavorite.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onIsFavoriteClick();
            }
        });
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
    public void setTextResult(String resultValue) {
        result.setText(resultValue);
    }

    @Override
    public void setIsFavorite(boolean isFavoriteValue) {
        if (isFavoriteValue)
            isFavorite.setImageResource(android.R.drawable.ic_delete);
        else
            isFavorite.setImageResource(android.R.drawable.ic_input_add);
    }
}