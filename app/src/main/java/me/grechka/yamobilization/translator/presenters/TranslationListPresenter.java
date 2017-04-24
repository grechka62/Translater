package me.grechka.yamobilization.translator.presenters;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.views.TranslationListView;

import static me.grechka.yamobilization.translator.TranslaterApp.*;



public class TranslationListPresenter extends BasePresenter<List<Translation>, TranslationListView> {
    private boolean isLoadingData = false;
    private final int position;
    private LoadDataTask task;

    public TranslationListPresenter(int position) {
        this.position = position;
    }

    @Override
    protected void updateView() {
        if (model.size() > 0)
            view().showTranslations(model);
    }

    @Override
    public void bindView(@NonNull TranslationListView view) {
        super.bindView(view);
        if (!isLoadingData)
            loadData();
    }

    @Override
    public void unbindView() {
        super.unbindView();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    private void loadData() {
        isLoadingData = true;
        getTranslationListFromDB();
    }

    private void getTranslationListFromDB() {
        task = new LoadDataTask();
        task.execute();
    }

    // Список для отображения загружается из базы полностью, сортируясь по id записей.
    private class LoadDataTask extends AsyncTask<Void, Void, List<Translation>> {
        @Override
        protected List<Translation> doInBackground(Void... params) {
            SQLiteDatabase translaterDB = translaterDBHelper.getReadableDatabase();
            Cursor cursor;
            if (position == 0)
                cursor = translaterDB.rawQuery("SELECT h.*, (SELECT count(f._id) FROM " +
                        FAVORITES_TABLE_NAME + " AS f WHERE f." + KEY_ID_TRANSLATION +
                        "=h._id) AS count FROM " + HISTORY_TABLE_NAME + " AS h ORDER BY h._id DESC", null);
            else
                cursor = translaterDB.rawQuery("SELECT h.* FROM " + FAVORITES_TABLE_NAME +
                        " AS f LEFT JOIN " + HISTORY_TABLE_NAME + " AS h ON f." +
                        KEY_ID_TRANSLATION + "=h._id ORDER BY f._id DESC", null);
            List<Translation> translations = new ArrayList<>();
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Translation item = new Translation();
                    item.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                    item.setRequest(cursor.getString(cursor.getColumnIndex(KEY_REQUEST)));
                    item.setResult(cursor.getString(cursor.getColumnIndex(KEY_RESULT)));
                    item.setLang(LANG1, cursor.getInt(cursor.getColumnIndex(KEY_LANG1)));
                    item.setLang(LANG2, cursor.getInt(cursor.getColumnIndex(KEY_LANG2)));
                    if (position == 0) {
                        item.setIsFavorite(cursor.getInt(cursor.getColumnIndex("count")) > 0);
                    } else item.setIsFavorite(true);
                    translations.add(item);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            translaterDB.close();
            return translations;
        }

        @Override
        protected void onPostExecute(List<Translation> translations) {
            super.onPostExecute(translations);
            isLoadingData = false;
            setModel(translations);
        }
    }
}
