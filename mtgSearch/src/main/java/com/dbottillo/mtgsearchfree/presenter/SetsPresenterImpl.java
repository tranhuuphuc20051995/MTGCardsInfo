package com.dbottillo.mtgsearchfree.presenter;

import com.dbottillo.mtgsearchfree.MTGApp;
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor;
import com.dbottillo.mtgsearchfree.model.MTGSet;
import com.dbottillo.mtgsearchfree.util.LOG;
import com.dbottillo.mtgsearchfree.view.SetsView;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class SetsPresenterImpl implements SetsPresenter, RxWrapper.RxWrapperListener<List<MTGSet>> {

    @Inject
    SetsInteractor interactor;

    SetsView setView;

    @Inject
    RxWrapper wrapper;

    public SetsPresenterImpl(SetsInteractor interactor) {
        LOG.d("created");
        MTGApp.graph.inject(this);
        this.interactor = interactor;
    }

    public void init(SetsView view) {
        LOG.d();
        setView = view;
    }

    public void loadSets() {
        LOG.d();
        if (SetsMemoryStorage.init) {
            setView.setsLoaded(SetsMemoryStorage.sets);
            return;
        }
        Observable<List<MTGSet>> obs = interactor.load();
        wrapper.run(obs, this);
    }

    @Override
    public void onNext(List<MTGSet> mtgSets) {
        LOG.d();
        SetsMemoryStorage.init = true;
        SetsMemoryStorage.sets = mtgSets;
        setView.setsLoaded(mtgSets);
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onCompleted() {

    }

}