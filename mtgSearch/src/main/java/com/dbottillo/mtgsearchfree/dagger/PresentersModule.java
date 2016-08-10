package com.dbottillo.mtgsearchfree.dagger;

import com.dbottillo.mtgsearchfree.interactors.CardFilterInteractor;
import com.dbottillo.mtgsearchfree.interactors.CardsInteractor;
import com.dbottillo.mtgsearchfree.interactors.DecksInteractor;
import com.dbottillo.mtgsearchfree.interactors.PlayerInteractor;
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor;
import com.dbottillo.mtgsearchfree.mapper.DeckMapper;
import com.dbottillo.mtgsearchfree.model.storage.CardsPreferences;
import com.dbottillo.mtgsearchfree.model.storage.GeneralPreferences;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenter;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.CardsPresenter;
import com.dbottillo.mtgsearchfree.presenter.CardsPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.DecksPresenter;
import com.dbottillo.mtgsearchfree.presenter.DecksPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.MemoryStorage;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenter;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.RxWrapperFactory;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenter;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenterImpl;
import com.dbottillo.mtgsearchfree.view.helpers.CardsHelper;

import dagger.Module;
import dagger.Provides;

@Module
public class PresentersModule {

    @Provides
    CardFilterPresenter provideCardFilterPresenter(CardFilterInteractor interactor, RxWrapperFactory factory) {
        return new CardFilterPresenterImpl(interactor, factory);
    }

    @Provides
    CardsPresenter provideCardsPresenter(CardsInteractor interactor, DeckMapper deckMapper, GeneralPreferences generalPreferences,
                                         RxWrapperFactory factory, MemoryStorage memoryStorage) {
        return new CardsPresenterImpl(interactor, deckMapper, generalPreferences, factory, memoryStorage);
    }

    @Provides
    SetsPresenter provideSetsPresenter(SetsInteractor interactor, RxWrapperFactory factory,
                                       CardsPreferences cardsPreferences, MemoryStorage memoryStorage) {
        return new SetsPresenterImpl(interactor, factory, cardsPreferences, memoryStorage);
    }

    @Provides
    PlayerPresenter providePlayerPresenter(PlayerInteractor interactor, RxWrapperFactory factory) {
        return new PlayerPresenterImpl(interactor, factory);
    }

    @Provides
    DecksPresenter provideDecksPresenter(DecksInteractor interactor, DeckMapper deckMapper, RxWrapperFactory factory) {
        return new DecksPresenterImpl(interactor, deckMapper, factory);
    }

    @Provides
    CardsHelper provideCardsHelper(CardsPreferences cardsPreferences) {
        return new CardsHelper(cardsPreferences);
    }

}