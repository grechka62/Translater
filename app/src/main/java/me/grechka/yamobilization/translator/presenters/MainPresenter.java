package me.grechka.yamobilization.translator.presenters;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.json.JSONObject;

import me.grechka.yamobilization.translator.TranslaterApp;
import me.grechka.yamobilization.translator.models.ResultFromHttp;
import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.views.MainView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.grechka.yamobilization.translator.TranslaterApp.*;

public class MainPresenter extends BasePresenter<Translation, MainView> {
    private static final int SHOW_TYPE_LOADING = 1;
    private int code = CODE_EMPTY;
    private Call<ResultFromHttp> call;
    private SaveDataTask task;
    private Translation newModel;

    @Override
    protected void updateView() {
        if (setupDone()) {
            view().setLang(LANG1, langsFull[model.getLang(LANG1)]);
            view().setLang(LANG2, langsFull[model.getLang(LANG2)]);
            view().setTextRequest(model.getRequest());
            if ((model.getResult() != null) && (!model.getResult().equals("")))
                code = CODE_SUCCESS;
            view().showResult(code, model);
        }
    }

    @Override
    public void unbindView() {
        super.unbindView();
        if (call != null) {
            call.cancel();
            call = null;
        }
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    /* В попытке сделать модель неизменяемым объектом
     * при каждом запросе создается новый объект. */
    public void onModelChanged() {
        String request = view().getTextRequest();
        newModel = createNewModel();
        if (request.length() == 0) {
            code = CODE_EMPTY;
            setModel(newModel);
        } else {
            view().showResult(SHOW_TYPE_LOADING, model);
            translate(newModel);
        }
    }

    public void swapLangs() {
        newModel = new Translation();
        if (model.getResult().length() != 0)
            newModel.setRequest(model.getResult());
        else
            newModel.setRequest(model.getRequest());
        newModel.setLang(LANG1, model.getLang(LANG2));
        newModel.setLang(LANG2, model.getLang(LANG1));
        setModel(newModel);
        onModelChanged();
    }

    private int getLangId(String langFull) {
        for (int i = 0; i < langsFull.length; i++)
            if (langsFull[i].equalsIgnoreCase(langFull))
                return i;
        return 60;
    }

    private Translation createNewModel() {
        newModel = new Translation();
        newModel.setRequest(view().getTextRequest());
        newModel.setLang(LANG1, getLangId(view().getLang(LANG1)));
        newModel.setLang(LANG2, getLangId(view().getLang(LANG2)));
        newModel.setIsFavorite(false);
        return newModel;
    }

    /* При получении нового запроса наличие подходящего
     * результата сначала ищется в кэше. Если такого нет,
     * отправляется запрос по сети. */
    private void translate(Translation model) {
        String request = model.getRequest();
        String langs = langsCode[model.getLang(LANG1)] + "-" + langsCode[model.getLang(LANG2)];
        String result = getResultFromCache(request, langs);
        if (result != null) {
            model.setResult(result);
            saveToDatabase(request, result, model.getLang(LANG1), model.getLang(LANG2));
        } else
            getResultFromHttp(model, request, langs);
    }

    private void afterTranslate(Translation model, String request, String result, String langs) {
        addToCache(request, result, langs);
        saveToDatabase(request, result, model.getLang(LANG1), model.getLang(LANG2));
    }

    private String getResultFromCache(String request, String langs) {
        for (String[] item: translationCache)
            if (item[0].equals(request) && item[2].equals(langs)) {
                return item[1];
            }
        return null;
    }

    private void getResultFromHttp(final Translation model, final String request, final String langs) {
        call = getTranslateApi().getResult(TranslaterApp.KEY, request, langs);
        call.enqueue(new Callback<ResultFromHttp>() {

                    @Override
                    public void onResponse(Call<ResultFromHttp> call, Response<ResultFromHttp> response) {
                        if (response.body() != null) {
                            ResultFromHttp resultFromHttp = response.body();
                            code = resultFromHttp.getCode();
                            String[] result = resultFromHttp.getResult();
                            model.setResult(result[0]);
                            afterTranslate(model, request, result[0], langs);
                        } else {
                            try {
                                code = new JSONObject(response.errorBody().string()).getInt("code");
                            } catch (Exception e) {
                                code = CODE_WRONG_KEY;
                            }
                            setModel(model);
                        }
                    }
                    @Override
                    public void onFailure(Call<ResultFromHttp> call, Throwable t) {
                        code = CODE_CONNECTION_ERROR;
                        setModel(model);
                    }
                });
    }

    private void addToCache(String request, String result, String langs) {
        String[] cache = new String[3];
        cache[0] = request;
        cache[1] = result;
        cache[2] = langs;

        //размер кэша ограничен, чтобы сэкономить ресурсы и не увеличивать время поиска
        if (translationCache.size() == 20)
            for (int i = 0; i < 5; i++)
                translationCache.remove(19-i);
        translationCache.add(0, cache);
    }

    /* Так как при отображении истории и избранного списки должны располагаться
     * в порядке, обратном добавлению, и чтобы не хранить в базе лишнюю информацию,
     * сортовка происходит по id запроса. Если в базе уже существует аналогичный запрос,
     * перед добавлением нового с большим id старый должен быть удален.
     * Перед удалением проверяется его наличие в таблице избранного. Если он был
     * добавлен в избранное, то после вставки нового запроса в историю запись в избранном обновляется.
     * Использование SQLite идеально соответствует характеру сохраняемых данных
     * (множество однотипных объектов). Кроме того, у меня есть некоторый опыт работы
     * с SQL, поэтому был выбран именно такой способ хранения.*/
    private void saveToDatabase(String request, String result, int lang1, int lang2) {
        task = new SaveDataTask();
        task.execute(request, result, String.valueOf(lang1), String.valueOf(lang2));
    }

    private class SaveDataTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... param) {
            SQLiteDatabase translaterDB = translaterDBHelper.getWritableDatabase();
            translaterDB.beginTransaction();
            String query = "SELECT _id FROM " + HISTORY_TABLE_NAME +
                    " WHERE " + KEY_REQUEST + "='" + param[0].trim() + "' AND " +
                    KEY_RESULT + "='" + param[1].trim() + "' AND " +
                    KEY_LANG1 + "=" + param[2].trim() + " AND " + KEY_LANG2 + "=" + param[3].trim();
            Cursor cursor = translaterDB.rawQuery(query, null);
            int id_history = -1;
            int id_favorite = -1;
            if (cursor.moveToFirst())
                id_history = cursor.getInt(0);
            if (id_history > -1) {
                query = "SELECT _id FROM " + FAVORITES_TABLE_NAME +
                        " WHERE " + KEY_ID_TRANSLATION + "=" + id_history;
                cursor = translaterDB.rawQuery(query, null);
                if (cursor.moveToFirst())
                    id_favorite = cursor.getInt(0);
                query = "DELETE FROM " + HISTORY_TABLE_NAME +
                        " WHERE " + KEY_REQUEST + "='" + param[0].trim() + "' AND " +
                        KEY_RESULT + "='" + param[1].trim() + "' AND " +
                        KEY_LANG1 + "=" + param[2].trim() + " AND " + KEY_LANG2 + "=" + param[3].trim();
                translaterDB.rawQuery(query, null);
            }
            query = "INSERT INTO " + HISTORY_TABLE_NAME +
                    " (" + KEY_REQUEST + ", " + KEY_RESULT + ", " + KEY_LANG1 + ", " + KEY_LANG2 +
                    ") VALUES ('" + param[0].trim() + "', '" + param[1].trim() + "', " +
                    param[2].trim() + ", " + param[3].trim() +")";
            translaterDB.rawQuery(query, null);
            query = "SELECT _id FROM " + HISTORY_TABLE_NAME +
                    " WHERE " + KEY_REQUEST + "='" + param[0].trim() + "' AND " +
                    KEY_RESULT + "='" + param[1].trim() + "' AND " +
                    KEY_LANG1 + "=" + param[2].trim() + " AND " + KEY_LANG2 + "=" + param[3].trim();
            cursor = translaterDB.rawQuery(query, null);
            if (cursor.moveToFirst())
                id_history = cursor.getInt(0);
            if (id_favorite > -1) {
                query = "UPDATE " + FAVORITES_TABLE_NAME + " SET " + KEY_ID_TRANSLATION +
                        "=" + id_history + " WHERE _id=" + id_favorite;
                newModel.setIsFavorite(true);
            }
            translaterDB.rawQuery(query, null);
            translaterDB.setTransactionSuccessful();
            translaterDB.endTransaction();
            translaterDB.close();
            return null;
        }

        // пользователь может увидеть только результат, уже сохраненный в БД
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setModel(newModel);
        }
    }
}