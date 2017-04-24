package me.grechka.yamobilization.translator.views;

import me.grechka.yamobilization.translator.models.Translation;

public interface MainView {

    void showResult(int type, Translation translation);

    void setTextRequest(String requestValue);

    void setLang(int position, String langValue);

    String getTextRequest();

    String getLang(int position);

}