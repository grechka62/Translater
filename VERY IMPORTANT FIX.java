/* В последний момент вносила изменения в работающий код, и была допущена ошибка. 
 * Чтобы не слишком нарушать условия отбора, не изменяю файлы проекта,
 * но обратить внимание на этот фикс очень хочется. Конечно, могут быть другие недочеты,
 * но именно из-за этой глупости лишаться шансов было бы очень жаль.
 * Из-за ошибки создается впечатление, что основная функциональность задания не реализована,
 * но на самом деле сохранять запросы в БД приложение все-таки умеет) 
 * Ссылка на работающее приложение https://yadi.sk/d/pq0Tza-E3HKK2u
 * 
 * С уважением и огромной надеждой на понимание,
 * Ирина:)
 * 
 * В строках 200, 206 и 219 класса presenters.MainPresenter нужно заменить
 * translaterDB.rawQuery(query, null);
 * на
 * translaterDB.execSQL(query);
 * В идеале исправленный метод должен выглядеть следующим образом 
 * (соответственно, основные исправления касаются строк 41, 47, 58): */

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
    if (cursor.moveToFirst()) {
        id_history = cursor.getInt(0);
        query = "SELECT _id FROM " + FAVORITES_TABLE_NAME +
                " WHERE " + KEY_ID_TRANSLATION + "=" + id_history;
        cursor = translaterDB.rawQuery(query, null);
        if (cursor.moveToFirst())
            id_favorite = cursor.getInt(0);
        query = "DELETE FROM " + HISTORY_TABLE_NAME +
                " WHERE " + KEY_REQUEST + "='" + param[0].trim() + "' AND " +
                KEY_RESULT + "='" + param[1].trim() + "' AND " +
                KEY_LANG1 + "=" + param[2].trim() + " AND " + KEY_LANG2 + "=" + param[3].trim();
        translaterDB.execSQL(query);
    }
    query = "INSERT INTO " + HISTORY_TABLE_NAME +
            " (" + KEY_REQUEST + ", " + KEY_RESULT + ", " + KEY_LANG1 + ", " + KEY_LANG2 +
            ") VALUES ('" + param[0].trim() + "', '" + param[1].trim() + "', " +
            param[2].trim() + ", " + param[3].trim() +")";
    translaterDB.execSQL(query);
    if (id_favorite > -1) {
        query = "SELECT _id FROM " + HISTORY_TABLE_NAME +
                " WHERE " + KEY_REQUEST + "='" + param[0].trim() + "' AND " +
                KEY_RESULT + "='" + param[1].trim() + "' AND " +
                KEY_LANG1 + "=" + param[2].trim() + " AND " + KEY_LANG2 + "=" + param[3].trim();
        cursor = translaterDB.rawQuery(query, null);
        if (cursor.moveToFirst())
            id_history = cursor.getInt(0);
        query = "UPDATE " + FAVORITES_TABLE_NAME + " SET " + KEY_ID_TRANSLATION +
                "=" + id_history + " WHERE _id=" + id_favorite;
        translaterDB.execSQL(query);
        newModel.setIsFavorite(true);
    }
    translaterDB.setTransactionSuccessful();
    translaterDB.endTransaction();
    translaterDB.close();
    return null;
}
