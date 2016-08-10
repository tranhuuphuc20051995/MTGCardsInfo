package com.dbottillo.mtgsearchfree.dagger;

import com.dbottillo.mtgsearchfree.MTGApp;
import com.dbottillo.mtgsearchfree.interactors.CardFilterInteractor;
import com.dbottillo.mtgsearchfree.interactors.CardsInteractor;
import com.dbottillo.mtgsearchfree.interactors.DecksInteractor;
import com.dbottillo.mtgsearchfree.interactors.PlayerInteractor;
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor;
import com.dbottillo.mtgsearchfree.mapper.DeckMapper;
import com.dbottillo.mtgsearchfree.model.storage.CardsPreferences;
import com.dbottillo.mtgsearchfree.model.storage.CardsStorage;
import com.dbottillo.mtgsearchfree.model.storage.DecksStorage;
import com.dbottillo.mtgsearchfree.model.storage.GeneralPreferences;
import com.dbottillo.mtgsearchfree.model.storage.PlayersStorage;
import com.dbottillo.mtgsearchfree.model.storage.SetsStorage;
import com.dbottillo.mtgsearchfree.presenter.CardFilterPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.CardsPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.DecksPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.MemoryStorage;
import com.dbottillo.mtgsearchfree.presenter.PlayerPresenterImpl;
import com.dbottillo.mtgsearchfree.presenter.RunnerFactory;
import com.dbottillo.mtgsearchfree.presenter.SetsPresenterImpl;
import com.dbottillo.mtgsearchfree.view.activities.BasicActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AndroidModule.class, DataModule.class, InteractorsModule.class})
public interface AppComponent {

    CardFilterInteractor getCardFilterInteractor();

    CardsInteractor getCardsInteractor();

    MemoryStorage getCardsMemoryStorage();

    CardsStorage getCardsStorage();

    SetsInteractor getSetsInteractor();

    SetsStorage getSetsStorage();

    PlayerInteractor getPlayerInteractor();

    PlayersStorage getPlayerStorage();

    DecksInteractor getDecksInteractor();

    DecksStorage getDecksStorage();

    RunnerFactory getRxWrapperFactory();

    DeckMapper getDeckMapper();

    CardsPreferences getCardsPreferences();

    GeneralPreferences getGeneralPreferences();

    void inject(MTGApp app);

    void inject(BasicActivity mainActivity);

    void inject(SetsPresenterImpl setsPresenter);

    void inject(CardFilterPresenterImpl cardFilterPresenter);

    void inject(PlayerPresenterImpl playerPresenter);

    void inject(DecksPresenterImpl decksPresenter);

    void inject(CardsPresenterImpl cardsPresenter);
}
