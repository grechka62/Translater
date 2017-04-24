package me.grechka.yamobilization.translator.views;

import me.grechka.yamobilization.translator.models.Translation;

public interface TranslationView {

    void setTextRequest(String requestValue);

    void setLangs(String langsValue);

    void setTextResult(String resultValue);

    void setIsFavorite(boolean isFavoriteValue);

    void goToMainView(Translation translation);

}
