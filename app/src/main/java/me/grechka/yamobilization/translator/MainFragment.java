package me.grechka.yamobilization.translator;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import me.grechka.yamobilization.translator.models.Translation;
import me.grechka.yamobilization.translator.presenters.MainPresenter;
import me.grechka.yamobilization.translator.views.MainView;

import static me.grechka.yamobilization.translator.TranslaterApp.*;

/* "Главный фрагмент отображает текущий перевод.
 * При изменении модели (вводе нового запроса, изменении языков)
 * презентеру отправляется сообщение о том, что модель была изменена.
 * Вся обработка изменений происходит в презентере. Он возвращает
 * результат вызовом метода showResult(). */

public class MainFragment extends Fragment implements MainView {
    private static final String PRESENTER = "presenter";
    private static final String FRAGMENT_TAG = "result_fragment";
    private static final String MSG_TYPE = "message_type";
    private static final int SHOW_TYPE_LOADING = 1;

    private EditText request;
    private final Button[] lang = new Button[2];

    private static MainPresenter presenter;

    public static MainFragment newInstance(Translation translation) {
        Bundle args = new Bundle();
        presenter = new MainPresenter();
        presenter.setModel(translation);
        args.putSerializable(PRESENTER, presenter);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(args);
        return mainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = (MainPresenter) getArguments().getSerializable(PRESENTER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        request = (EditText) view.findViewById(R.id.request);
        request.setImeOptions(EditorInfo.IME_ACTION_DONE);
        request.setRawInputType(EditorInfo.TYPE_CLASS_TEXT);
        lang[LANG1] = (Button) view.findViewById(R.id.lang1_button);
        lang[LANG2] = (Button) view.findViewById(R.id.lang2_button);
        ImageButton swapLangs = (ImageButton) view.findViewById(R.id.swap_langs_button);

        request.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    presenter.onModelChanged();
                return false;
            }
        });

        View.OnClickListener langClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Button button = (Button) v;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.choose_lang))
                        .setItems(langsFull, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        button.setText(langsFull[which]);
                        presenter.onModelChanged();
                    }
                });
                builder.create().show();
            }
        };
        lang[LANG1].setOnClickListener(langClickListener);
        lang[LANG2].setOnClickListener(langClickListener);

        swapLangs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.swapLangs();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.bindView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.unbindView();

    }

    /* В зависимости от результата, полученого из презентера,
     * создаются разные фрагменты */
    @Override
    public void showResult(int type, Translation translation) {
        MainActivity activity = (MainActivity) getActivity();
        activity.setModel(translation);
        switch (type) {
            case CODE_SUCCESS:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.result_fragment_container,
                                ResultFragment.newInstance(translation),
                                FRAGMENT_TAG)
                        .commit();
                break;
            case CODE_EMPTY:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm.findFragmentByTag(FRAGMENT_TAG) != null)
                    fm.beginTransaction()
                            .remove(fm.findFragmentByTag(FRAGMENT_TAG))
                            .commit();
                break;
            case SHOW_TYPE_LOADING:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.result_fragment_container,
                                new LoadingFragment(),
                                FRAGMENT_TAG)
                        .commit();
                break;
            case CODE_CONNECTION_ERROR:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.result_fragment_container,
                                new ErrorFragment(),
                                FRAGMENT_TAG)
                        .commit();
                break;
            default:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.result_fragment_container,
                                MessageFragment.newInstance(type),
                                FRAGMENT_TAG)
                        .commit();
        }
    }

    @Override
    public void setTextRequest(String requestValue) {
        request.setText(requestValue);
    }

    @Override
    public String getTextRequest() {
        return request.getText().toString().trim();
    }

    @Override
    public String getLang(int position) {
        return lang[position].getText().toString();
    }

    @Override
    public void setLang(int position, String langValue) {
        lang[position].setText(langValue);
    }

    public static class ErrorFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_error, container, false);
            Button repeatButton = (Button) view.findViewById(R.id.repeat_button);
            repeatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.onModelChanged();
                }
            });
            return view;
        }
    }

    public static class LoadingFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_loading, container, false);
        }
    }

    public static class MessageFragment extends Fragment {
        private static int msgType;

        public static MessageFragment newInstance(int messageType) {
            Bundle args = new Bundle();
            msgType = messageType;
            args.putInt(MSG_TYPE, messageType);
            MessageFragment msgFragment = new MessageFragment();
            msgFragment.setArguments(args);
            return msgFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            msgType = getArguments().getInt(MSG_TYPE);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_message, container, false);
            TextView msgText = (TextView) v.findViewById(R.id.message);
            switch (msgType) {
                case CODE_DAILY_LIMIT_EXCEED:
                    msgText.setText(getString(R.string.msg_daily_limit_exceed));
                    break;
                case CODE_TEXT_LENGTH_EXCEED:
                    msgText.setText(getString(R.string.msg_text_length_exceed));
                    break;
                case CODE_UNABLE_TO_TRANSLATE:
                    msgText.setText(getString(R.string.msg_unable_to_translate));
                    break;
                case CODE_WRONG_DIRECTION:
                    msgText.setText(getString(R.string.msg_wrong_direction));
                    break;
                default: msgText.setText(getString(R.string.msg_call_support));
            }
            return v;
        }
    }
}
