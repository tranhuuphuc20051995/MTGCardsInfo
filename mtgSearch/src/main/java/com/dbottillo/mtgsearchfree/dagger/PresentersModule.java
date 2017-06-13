package com.dbottillo.mtgsearchfree.dagger;

import com.dbottillo.mtgsearchfree.interactors.CardFilterInteractor;
import com.dbottillo.mtgsearchfree.interactors.CardsInteractor;
import com.dbottillo.mtgsearchfree.interactors.DecksInteractor;
import com.dbottillo.mtgsearchfree.interactors.PlayerInteractor;
import com.dbottillo.mtgsearchfree.interactors.SavedCardsInteractor;
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor;
import com.dbottillo.mtgsearchfree.mapper.DeckMapper;
import com.dbottillo.mtgsearchfree.model.storage.CardsPreferences;
import com.dbottillo.mtgsearchfree.model.storage.GeneralData;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenter;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.CardPresenter;
import com.dbottillo.mtgsearchfree.presenter.CardPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.DecksPresenter;
import com.dbottillo.mtgsearchfree.presenter.DecksPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.MemoryStorage;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenter;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.RunnerFactory;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenter;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.cards.CardsActivityPresenter;
import com.dbottillo.mtgsearchfree.ui.cards.CardsActivityPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.cardsCoonfigurator.CardsConfiguratorPresenter;
import com.dbottillo.mtgsearchfree.ui.cardsCoonfigurator.CardsConfiguratorPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.lucky.CardsLuckyPresenter;
import com.dbottillo.mtgsearchfree.ui.lucky.CardsLuckyPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.saved.SavedCardsPresenter;
import com.dbottillo.mtgsearchfree.ui.saved.SavedCardsPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.search.SearchPresenter;
import com.dbottillo.mtgsearchfree.ui.search.SearchPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.sets.SetPickerPresenter;
import com.dbottillo.mtgsearchfree.ui.sets.SetPickerPresenterImpl;
import com.dbottillo.mtgsearchfree.ui.sets.SetsFragmentPresenter;
import com.dbottillo.mtgsearchfree.ui.sets.SetsFragmentPresenterImpl;
import com.dbottillo.mtgsearchfree.util.Logger;
import com.dbottillo.mtgsearchfree.view.helpers.CardsHelper;

import dagger.Module;
import dagger.Provides;

@Module
public class PresentersModule {

    @Provides
    CardFilterPresenter provideCardFilterPresenter(CardFilterInteractor interactor,
                                                   RunnerFactory factory,
                                                   MemoryStorage memoryStorage, Logger logger) {
        return new CardFilterPresenterImpl(interactor, factory, memoryStorage, logger);
    }

    @Provides
    SetsPresenter provideSetsPresenter(SetsInteractor interactor,
                                       RunnerFactory factory,
                                       CardsPreferences cardsPreferences,
                                       MemoryStorage memoryStorage, Logger logger) {
        return new SetsPresenterImpl(interactor, factory, cardsPreferences, memoryStorage, logger);
    }

    @Provides
    PlayerPresenter providePlayerPresenter(PlayerInteractor interactor,
                                           RunnerFactory factory, Logger logger) {
        return new PlayerPresenterImpl(interactor, factory, logger);
    }

    @Provides
    DecksPresenter provideDecksPresenter(DecksInteractor interactor,
                                         DeckMapper deckMapper,
                                         RunnerFactory factory, Logger logger) {
        return new DecksPresenterImpl(interactor, deckMapper, factory, logger);
    }

    @Provides
    CardsHelper provideCardsHelper(CardsPreferences cardsPreferences) {
        return new CardsHelper(cardsPreferences);
    }

    @Provides
    CardPresenter provideCardPresenter(CardsInteractor interactor, Logger logger, RunnerFactory runnerFactory){
        return new CardPresenterImpl(interactor, logger, runnerFactory);
    }

    @Provides
    SavedCardsPresenter provideSavedCardsPresenter(SavedCardsInteractor interactor,
                                                   GeneralData generalData,
                                                   Logger logger,
                                                   RunnerFactory runnerFactory){
        return new SavedCardsPresenterImpl(interactor, runnerFactory, generalData, logger);
    }

    @Provides
    SetsFragmentPresenter providesSetsFragmentPresenter(SetsInteractor setsInteractor,
                                                        CardsInteractor cardsInteractor,
                                                        CardsPreferences cardsPreferences,
                                                        RunnerFactory runnerFactory,
                                                        GeneralData generalData,
                                                        Logger logger){
        return new SetsFragmentPresenterImpl(setsInteractor, cardsInteractor, cardsPreferences, runnerFactory, generalData, logger);
    }

    @Provides
    CardsConfiguratorPresenter providesCardsConfiguratorPresenter(CardFilterInteractor interactor,
                                                                  RunnerFactory runnerFactory){
        return new CardsConfiguratorPresenterImpl(interactor, runnerFactory);
    }

    @Provides
    SetPickerPresenter providesSetPickerPresenter(SetsInteractor setsInteractor,
                                                     CardsPreferences cardsPreferences,
                                                     RunnerFactory runnerFactory,
                                                     Logger logger){
        return new SetPickerPresenterImpl(setsInteractor, cardsPreferences, runnerFactory, logger);
    }

    @Provides
    CardsActivityPresenter providesCardsActivityPresenter(CardsInteractor cardsInteractor,
                                                      SavedCardsInteractor savedCardsInteractor,
                                                      DecksInteractor decksInteractor,
                                                      CardsPreferences cardsPreferences,
                                                      RunnerFactory runnerFactory,
                                                      Logger logger){
        return new CardsActivityPresenterImpl(cardsInteractor, savedCardsInteractor, decksInteractor, cardsPreferences, runnerFactory, logger);
    }

    @Provides
    SearchPresenter providesSearchActivityPresenter(SetsInteractor setsInteratcor,
                                                   CardsInteractor cardsInteractor,
                                                   GeneralData generalData,
                                                   RunnerFactory runnerFactory,
                                                   Logger logger){
        return new SearchPresenterImpl(setsInteratcor, cardsInteractor, generalData, runnerFactory, logger);
    }

    @Provides
    CardsLuckyPresenter providesCardsLuckyActivityPresenter(CardsInteractor cardsInteractor,
                                                        CardsPreferences cardsPreferences,
                                                        RunnerFactory runnerFactory,
                                                        Logger logger){
        return new CardsLuckyPresenterImpl(cardsInteractor, cardsPreferences, runnerFactory, logger);
    }
}