package me.grechka.yamobilization.translator.presenters;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.views.ActivityView;
import static me.grechka.yamobilization.translator.TranslaterApp.*;

public class ActivityPresenter extends BasePresenter<Translation, ActivityView> {

    public ActivityPresenter() {
        super();
        Translation newModel = new Translation();
        newModel.setLang(LANG1, 60);
        newModel.setLang(LANG2, 3);
        setModel(newModel);
    }

    @Override
    protected void updateView() {
        view().showMain(model);
    }
}
