package me.grechka.yamobilization.translator;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import me.grechka.yamobilization.translator.models.TranslateApi;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/* Приложение разработано на основе архитектуры MVP.
 * Так как это был мой первый опыт работы с такой архитектурой,
 * при разработке опиралась на пример реализации
 * https://github.com/remind101/android-arch-sample . */

public class TranslaterApp extends Application {
    public static final String KEY = "trnsl.1.1.20170418T150123Z.89e5f22494de224f.fa5431c277bd8f180abc9d6f565d22e45379c6af";
    public static final int CODE_EMPTY = 100;
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_CONNECTION_ERROR = 300;
    public static final int CODE_WRONG_KEY = 401;
    //public static final int CODE_BLOCKED_KEY = 402;
    public static final int CODE_DAILY_LIMIT_EXCEED = 404;
    public static final int CODE_TEXT_LENGTH_EXCEED = 413;
    public static final int CODE_UNABLE_TO_TRANSLATE = 422;
    public static final int CODE_WRONG_DIRECTION = 501;
    public static final String PRESENTER = "presenter";
    public static final int LANG1 = 0;
    public static final int LANG2 = 1;
    public static final String HISTORY_TABLE_NAME = "history";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_RESULT = "result";
    public static final String KEY_LANG1 = "lang1";
    public static final String KEY_LANG2 = "lang2";
    public static final String FAVORITES_TABLE_NAME = "favorites";
    public static final String KEY_ID_TRANSLATION = "id_translation";

    public static String[] langsCode, langsFull; //массивы кодов и полных наименований используемых языков
    public static TranslationsOpenHelper translaterDBHelper;
    public static List<String[]> translationCache;
    private static TranslateApi translateApi;

    @Override
    public void onCreate() {
        super.onCreate();
        initRetrofit();
        initLangs();
        translaterDBHelper = new TranslationsOpenHelper(this);
        translationCache = new ArrayList<>();
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://translate.yandex.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        translateApi = retrofit.create(TranslateApi.class);
    }

    public static TranslateApi getTranslateApi() {
        return translateApi;
    }

    /* Обновление списка языков с использованием API Яндекс.Переводчика
     * не предусмотрено, так как это не было прямо указано в основном задании.
     * Языки инициализируются при запуске приложения из ресурсов.
     * Хранятся в виде массива строк в формате "[полное_наименование] [код]".
     * Модель запроса хранит индексы двух задействованных в переводе языков.
     * Это позволяет в большинстве случаев использования получать быстрый
     * доступ к нужному языку по индексу, не осуществляя поиск в БД или в хэш-таблице. */
    private void initLangs() {
        String[] langsArray = getResources().getStringArray(R.array.langs_array);
        int i = 0;
        langsCode = new String[langsArray.length];
        langsFull = new String[langsArray.length];
        for (String lang: langsArray) {
            String langSplit[] = lang.split(" ");
            if (langSplit.length > 2) {
                langsCode[i] = langSplit[2];
                langsFull[i++] = langSplit[0] + " " + langSplit[1];
            } else {
                langsCode[i] = langSplit[1];
                langsFull[i++] = langSplit[0];
            }
        }
    }

    /* Хранение истории запросов реализовано с помощью БД SQLite.
     * В базе данные хранятся в виде двух таблиц:
     * в таблице history хранится полная информация о переводе,
     * в таблице favorites - ссылки на строки в таблице history */
    public static class TranslationsOpenHelper extends SQLiteOpenHelper {

        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "translater_database";

        private static final String HISTORY_TABLE_CREATE =
                "CREATE TABLE " + HISTORY_TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_REQUEST + " TEXT, " +
                        KEY_RESULT + " TEXT, " +
                        KEY_LANG1 + " INTEGER, " +
                        KEY_LANG2 + " INTEGER)";

        private static final String FAVORITES_TABLE_CREATE =
                "CREATE TABLE " + FAVORITES_TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        KEY_ID_TRANSLATION + " INTEGER)";

        TranslationsOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(HISTORY_TABLE_CREATE);
            db.execSQL(FAVORITES_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
