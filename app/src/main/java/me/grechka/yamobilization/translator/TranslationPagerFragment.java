package me.grechka.yamobilization.translator;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TranslationPagerFragment extends Fragment {

    private static final String POSITION = "position";

    private static int position;

    private static final int NUM_ITEMS = 2;
    private TranslationPagerAdapter mAdapter;
    private ViewPager mPager;

    public static TranslationPagerFragment newInstance(int pos) {
        Bundle args = new Bundle();
        position = pos;
        args.putInt(POSITION, position);
        TranslationPagerFragment fragment = new TranslationPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TranslationPagerAdapter(getActivity().getSupportFragmentManager());
        if (getArguments() != null)
            position = getArguments().getInt(POSITION);
        else
            position = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_pager, container, false);
        mPager = (ViewPager) myView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        TabLayout tl = (TabLayout) myView.findViewById(R.id.translations_tablayout);
        tl.setupWithViewPager(mPager);
        tl.setBackgroundResource(R.color.colorPrimary);
        TabLayout.Tab tab = tl.getTabAt(0);
        if (tab != null) tab.setText(getString(R.string.history));
        tab = tl.getTabAt(1);
        if (tab != null) tab.setText(getString(R.string.favorites));
        tab = tl.getTabAt(position);
        if (tab != null) tab.select();
        return myView;
    }

    /* передача в MainActivity данных о состоянии пейджера,
     * чтобы активити могла сохранить состояние. */
    @Override
    public void onPause() {
        super.onPause();
        MainActivity activity = (MainActivity) getActivity();
        activity.setPagerPosition(mPager.getCurrentItem());
    }

    /* Чтобы при каждом появлении фрагмента на экране загружался актуальный
     * список, фрагмент приходится пересоздавать. Поэтому использован FragmentStatePagerAdapter. */
    private static class TranslationPagerAdapter extends FragmentStatePagerAdapter {
        TranslationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int pos) {
            position = pos;
            return TranslationListFragment.newInstance(pos);
        }
    }
}