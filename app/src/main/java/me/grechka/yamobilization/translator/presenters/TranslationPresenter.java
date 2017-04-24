package me.grechka.yamobilization.translator.presenters;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.views.TranslationView;

import static me.grechka.yamobilization.translator.TranslaterApp.*;



public class TranslationPresenter extends BasePresenter<Translation, TranslationView> {
    private UpdateFavoriteTask task;

    @Override
    protected void updateView() {
        view().setTextRequest(model.getRequest());
        view().setTextResult(model.getResult());
        String langs = langsCode[model.getLang(LANG1)] + " - " + langsCode[model.getLang(LANG2)];
        view().setLangs(langs);
        view().setIsFavorite(model.getIsFavorite());
    }

    @Override
    public void unbindView() {
        super.unbindView();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    public void onTranslationClick() {
        view().goToMainView(model);
    }

    public void onIsFavoriteClick() {
        updateFavorite(model);
    }

    private void updateFavorite(Translation translation) {
        String query;
        if (translation.getIsFavorite())
            query = "DELETE FROM " + FAVORITES_TABLE_NAME +
                    " WHERE " + KEY_ID_TRANSLATION + "=(SELECT _id FROM " + HISTORY_TABLE_NAME +
                    " WHERE " + KEY_REQUEST + "='" + translation.getRequest() + "' AND " +
                    KEY_RESULT + "='" + translation.getResult() + "' AND " +
                    KEY_LANG1 + "=" + translation.getLang(LANG1) + " AND " +
                    KEY_LANG2 + "=" + translation.getLang(LANG2) + ")";
        else
            query = "INSERT INTO " + FAVORITES_TABLE_NAME +
                    " (" + KEY_ID_TRANSLATION + ") VALUES ((SELECT _id FROM " + HISTORY_TABLE_NAME +
                    " WHERE " + KEY_REQUEST + "='" + translation.getRequest() + "' AND " +
                    KEY_RESULT + "='" + translation.getResult() + "' AND " +
                    KEY_LANG1 + "=" + translation.getLang(LANG1) + " AND " +
                    KEY_LANG2 + "=" + translation.getLang(LANG2) + "))";
        task = new UpdateFavoriteTask();
        task.execute(query);
    }

    private class UpdateFavoriteTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... query) {
            SQLiteDatabase translaterDB = translaterDBHelper.getWritableDatabase();
            translaterDB.execSQL(query[0]);
            translaterDB.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            model.setIsFavorite(!model.getIsFavorite());
            view().setIsFavorite(model.getIsFavorite());
        }
    }
}
