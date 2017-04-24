package me.grechka.yamobilization.translator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.presenters.ActivityPresenter;
import me.grechka.yamobilization.translator.views.ActivityView;

/* Единственная активити отвечает за отображение всех фрагментов и
 * передает в них данные для отображения. Из-за этого состояние элементов
 * интерфейса не восстанавливается стандартными средствами.
 * Поэтому вся эта информация:
 * - позиция основного TabLayout'а (текущий перевод или список запросов);
 * - позиция ViewPager'а, который отображает историю и избранное;
 * - текущий перевод
 * должна быть сохранена и восстановлена вручную.
 * Хотя активити напрямую не отображает перевод, презентер нужен для передачи
 * модели, которая в свою очередь может быть получена из истории/избранного. */

public class MainActivity extends AppCompatActivity implements ActivityView {
    private static final String FRAGMENT_TAG = "fragment_main";
    private static final String PRESENTER = "presenter";
    private static final String MODEL = "model";
    private static final String POS_ACTIVITY = "position_activity";
    private static final String POS_PAGER = "position_pager";

    private static final int POS_MAIN = 0;
    private static final int POS_HISTORY = 0;

    public static ActivityPresenter activityPresenter;
    private Translation model;
    private int posActivity, posPager;

    private MainFragment mainFragment;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            activityPresenter = new ActivityPresenter();
            posActivity = POS_MAIN;
            posPager = POS_HISTORY;
        }
        else {
            activityPresenter = (ActivityPresenter) savedInstanceState.getSerializable(PRESENTER);
            model = (Translation) savedInstanceState.getSerializable(MODEL);
            activityPresenter.setModel(model);
            posActivity = savedInstanceState.getInt(POS_ACTIVITY);
            posPager = savedInstanceState.getInt(POS_PAGER);
        }

        tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                posActivity = tab.getPosition();
                tabAction();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                tabAction();
            }
        });

        final TextView link = (TextView) findViewById(R.id.yandex_link);
        link.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        link.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                        break;
                    case MotionEvent.ACTION_CANCEL: case MotionEvent.ACTION_OUTSIDE:
                        link.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorTextBlack));
                        break;
                    case MotionEvent.ACTION_UP:
                        link.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                        Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.yandex_link)));
                        startActivity(browseIntent);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (model != null)
            activityPresenter.setModel(model);
        if (posActivity == POS_MAIN) {
            activityPresenter.bindView(this);
        } else {
            TabLayout.Tab tab = tabLayout.getTabAt(posActivity);
            if (tab != null) tab.select();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activityPresenter.unbindView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(PRESENTER, activityPresenter);
        outState.putSerializable(MODEL, model);
        outState.putInt(POS_ACTIVITY, posActivity);
        outState.putInt(POS_PAGER, posPager);
    }

    /* Нажатие на кнопку Назад должно возвращать на главный фрагмент
     * (на котором находится текущий перевод), поэтому складывать
     * фрагменты в бэк-стек смысла нет. Нужно проверить текущее состояние
     * экрана и, если надо, перейти на первую вкладку.*/
    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() != POS_MAIN) {
            posActivity = POS_MAIN;
            TabLayout.Tab tab = tabLayout.getTabAt(POS_MAIN);
            if (tab != null) tab.select();
        } else
            super.onBackPressed();
    }

    private void tabAction() {
        if (posActivity == POS_MAIN) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            mainFragment,
                            FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            TranslationPagerFragment.newInstance(posPager),
                            FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void showMain(Translation translation) {
        mainFragment = MainFragment.newInstance(translation);
        model = translation;
        TabLayout.Tab tab = tabLayout.getTabAt(POS_MAIN);
        if (tab != null) tab.select();
    }

    public void setModel(Translation model) {
        this.model = model;
    }

    public void setPagerPosition(int pos) {
        posPager = pos;
    }
}