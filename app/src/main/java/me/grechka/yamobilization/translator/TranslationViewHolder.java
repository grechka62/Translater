package me.grechka.yamobilization.translator;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.presenters.TranslationPresenter;
import me.grechka.yamobilization.translator.views.TranslationView;

public class TranslationViewHolder extends MvpViewHolder<TranslationPresenter> implements TranslationView {
    private final TextView request;
    private final TextView result;
    private final TextView langs;
    private final ImageButton isFavorite;

    public TranslationViewHolder(View v) {
        super(v);
        request = (TextView) v.findViewById(R.id.request_on_row);
        result = (TextView) v.findViewById(R.id.result_on_row);
        langs = (TextView) v.findViewById(R.id.langs_on_row);
        isFavorite = (ImageButton) v.findViewById(R.id.is_favorite_button_on_row);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onTranslationClick();
            }
        });

        isFavorite.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onIsFavoriteClick();
            }
        });
    }

    @Override
    public void setTextRequest(String requestValue) {
        request.setText(requestValue);
    }

    @Override
    public void setTextResult(String resultValue) {
        result.setText(resultValue);
    }

    @Override
    public void setLangs(String langsValue) {
        langs.setText(langsValue);
    }

    @Override
    public void setIsFavorite(boolean isFavoriteValue) {
        if (isFavoriteValue)
            isFavorite.setImageResource(android.R.drawable.ic_delete);
        else
            isFavorite.setImageResource(android.R.drawable.ic_input_add);
    }

    @Override
    public void goToMainView(Translation translation) {
        MainActivity.activityPresenter.setModel(translation);
    }
}
