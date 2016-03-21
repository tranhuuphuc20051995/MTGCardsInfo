package com.dbottillo.mtgsearchfree.dagger;

import com.dbottillo.mtgsearchfree.interactors.CardFilterInteractor;
import com.dbottillo.mtgsearchfree.interactors.CardsInteractor;
import com.dbottillo.mtgsearchfree.interactors.PlayerInteractor;
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenter;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.CardsPresenter;
import com.dbottillo.mtgsearchfree.presenter.CardsPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenter;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenter;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class PresentersModule {

    @Provides
    CardFilterPresenter provideCardFilterPresenter(CardFilterInteractor interactor) {
        return new CardFilterPresenterImpl(interactor);
    }

    @Provides
    CardsPresenter provideCardsPresenter(CardsInteractor interactor) {
        return new CardsPresenterImpl(interactor);
    }

    @Provides
    SetsPresenter provideSetsPresenter(SetsInteractor interactor) {
        return new SetsPresenterImpl(interactor);
    }

    @Provides
    PlayerPresenter providePlayerPresenter(PlayerInteractor interactor) {
        return new PlayerPresenterImpl(interactor);
    }
}