package com.dbottillo.mtgsearchfree.view.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dbottillo.mtgsearchfree.MTGApp;
import com.dbottillo.mtgsearchfree.R;
import com.dbottillo.mtgsearchfree.model.CardsBucket;
import com.dbottillo.mtgsearchfree.model.MTGCard;
import com.dbottillo.mtgsearchfree.model.MTGSet;
import com.dbottillo.mtgsearchfree.presenter.CardsPresenter;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenter;
import com.dbottillo.mtgsearchfree.util.DialogUtil;
import com.dbottillo.mtgsearchfree.util.LOG;
import com.dbottillo.mtgsearchfree.util.TrackingManager;
import com.dbottillo.mtgsearchfree.view.CardsView;
import com.dbottillo.mtgsearchfree.view.SetsView;
import com.dbottillo.mtgsearchfree.view.activities.CardsActivity;
import com.dbottillo.mtgsearchfree.view.activities.MainActivity;
import com.dbottillo.mtgsearchfree.view.adapters.GameSetAdapter;
import com.dbottillo.mtgsearchfree.view.adapters.OnCardListener;
import com.dbottillo.mtgsearchfree.view.helpers.CardsHelper;
import com.dbottillo.mtgsearchfree.view.helpers.DialogHelper;
import com.dbottillo.mtgsearchfree.view.views.MTGCardListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment extends BasicFragment implements DialogUtil.SortDialogListener,
        MainActivity.MainActivityListener, OnCardListener, CardsView, SetsView {

    @Inject
    CardsPresenter cardsPresenter;
    @Inject
    SetsPresenter setsPresenter;

    private MTGSet gameSet;
    private ArrayList<MTGSet> sets = new ArrayList<>();
    private CardsBucket cardBucket;
    private GameSetAdapter setAdapter;
    private int currentSetPosition = -1;
    MainActivity mainActivity;

    @Bind(R.id.set_arrow)
    ImageView setArrow;
    @Bind(R.id.set_list_bg)
    View setListBg;
    @Bind(R.id.set_list)
    ListView setList;
    @Bind(R.id.set_chooser_name)
    TextView chooserName;
    @Bind(R.id.cards_list_view)
    MTGCardListView mtgCardListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setActionBarTitle(getString(R.string.app_long_name));

        MTGApp.uiGraph.inject(this);
        cardsPresenter.init(this);
        setsPresenter.init(this);

        setAdapter = new GameSetAdapter(getActivity().getApplicationContext(), sets);
        setAdapter.setCurrent(currentSetPosition);
        setList.setAdapter(setAdapter);
        setList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentSetPosition != position) {
                    currentSetPosition = position;
                    showHideSetList(true);
                } else {
                    showHideSetList(false);
                }
            }
        });

        view.findViewById(R.id.set_chooser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideSetList(false);
            }
        });

        setsPresenter.loadSets();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        mainActivity.setMainActivityListener(this);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentSetPosition", currentSetPosition);
        outState.putParcelableArrayList("SET", sets);
    }

    @OnClick(R.id.cards_sort)
    public void onSortClicked(View view) {
        LOG.d();
        DialogUtil.chooseSortDialog(getContext(), sharedPreferences, this);
    }

    @OnClick(R.id.set_list_bg)
    public void onSetListBgClicked(View view) {
        LOG.d();
        if (setList.getHeight() > 0) {
            showHideSetList(false);
        }
    }

    private void showHideSetList(final boolean loadSet) {
        LOG.d();
        final int startHeight = setList.getHeight();
        final int targetHeight = ((startHeight == 0)) ? mtgCardListView.getHeight() : 0;
        final float startRotation = setArrow.getRotation();
        final Animation animation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);
                if (targetHeight > startHeight) {
                    int newHeight = (int) (startHeight + (interpolatedTime * targetHeight));
                    setHeightView(setList, newHeight);
                    setHeightView(setListBg, newHeight);
                    setArrow.setRotation(startRotation + (180 * interpolatedTime));
                } else {
                    int newHeight = (int) (startHeight - startHeight * interpolatedTime);
                    setHeightView(setList, newHeight);
                    setHeightView(setListBg, newHeight);
                    setArrow.setRotation(startRotation - (180 * interpolatedTime));
                }
            }
        };
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation) {
                if (loadSet) {
                    TrackingManager.trackSet(gameSet, sets.get(currentSetPosition));
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("setPosition", currentSetPosition);
                    editor.apply();
                    setAdapter.setCurrent(currentSetPosition);
                    setAdapter.notifyDataSetChanged();
                    loadSet();
                }
            }

            public void onAnimationRepeat(Animation animation) {

            }
        });
        getView().startAnimation(animation);
    }

    public void setHeightView(View view, int value) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.height = value;
        view.setLayoutParams(params);
    }

    public void favIdLoaded(int[] favourites) {
        // favourites are not needed in this fragment
        throw new UnsupportedOperationException();
    }

    public void setsLoaded(List<MTGSet> sets) {
        LOG.d();
        currentSetPosition = sharedPreferences.getInt("setPosition", 0);
        setAdapter.setCurrent(currentSetPosition);
        this.sets.clear();
        for (MTGSet set : sets) {
            this.sets.add(set);
        }
        setAdapter.notifyDataSetChanged();
        loadSet();
    }

    public void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void loadSet() {
        LOG.d();
        gameSet = sets.get(currentSetPosition);
        chooserName.setText(gameSet.getName());
        cardsPresenter.loadCards(gameSet);
    }

    public void cardLoaded(CardsBucket bucket) {
        LOG.d();
        cardBucket = bucket;
        updateContent();
    }

    public void updateContent() {
        LOG.d();
        ArrayList<MTGCard> cards = new ArrayList<>();
        CardsHelper.filterCards(mainActivity.getCurrentFilter(), cardBucket.getCards(), cards);
        boolean wubrgSort = sharedPreferences.getBoolean(BasicFragment.PREF_SORT_WUBRG, true);
        CardsHelper.sortCards(wubrgSort, cards);
        mtgCardListView.loadCards(cards, this);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onSortSelected() {

        LOG.d();
        loadSet();
    }

    @Override
    public void onCardSelected(MTGCard card, int position) {
        LOG.d();
        TrackingManager.trackCard(gameSet, position);
        startActivity(CardsActivity.newInstance(getContext(), gameSet, position));
    }

    @Override
    public void onOptionSelected(MenuItem menuItem, MTGCard card, int position) {
        if (menuItem.getItemId() == R.id.action_add_to_deck) {
            DialogHelper.open(dbActivity, "add_to_deck", AddToDeckFragment.newInstance(card));
        } else {
            cardsPresenter.saveAsFavourite(card, true);
        }
    }

    public String getPageTrack() {
        return "/set";
    }

}