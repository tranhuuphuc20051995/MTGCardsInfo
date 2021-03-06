package com.dbottillo.mtgsearchfree.dagger

import com.dbottillo.mtgsearchfree.ActivityScope
import com.dbottillo.mtgsearchfree.decks.DecksFragment
import com.dbottillo.mtgsearchfree.decks.DecksFragmentPresenter
import com.dbottillo.mtgsearchfree.decks.DecksFragmentPresenterImpl
import com.dbottillo.mtgsearchfree.decks.addToDeck.AddToDeckFragment
import com.dbottillo.mtgsearchfree.decks.addToDeck.AddToDeckInteractor
import com.dbottillo.mtgsearchfree.decks.addToDeck.AddToDeckPresenter
import com.dbottillo.mtgsearchfree.decks.addToDeck.AddToDeckPresenterImpl
import com.dbottillo.mtgsearchfree.decks.deck.DeckFragment
import com.dbottillo.mtgsearchfree.decks.startingHand.DeckStartingHandFragment
import com.dbottillo.mtgsearchfree.interactors.CardFilterInteractor
import com.dbottillo.mtgsearchfree.interactors.CardsInteractor
import com.dbottillo.mtgsearchfree.interactors.DecksInteractor
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor
import com.dbottillo.mtgsearchfree.sets.SetsFragment
import com.dbottillo.mtgsearchfree.sets.SetsFragmentPresenter
import com.dbottillo.mtgsearchfree.sets.SetsFragmentPresenterImpl
import com.dbottillo.mtgsearchfree.settings.SettingsFragment
import com.dbottillo.mtgsearchfree.storage.CardsPreferences
import com.dbottillo.mtgsearchfree.storage.GeneralData
import com.dbottillo.mtgsearchfree.ui.cardsConfigurator.CardsConfiguratorFragment
import com.dbottillo.mtgsearchfree.ui.cardsConfigurator.CardsConfiguratorPresenter
import com.dbottillo.mtgsearchfree.ui.cardsConfigurator.CardsConfiguratorPresenterImpl
import com.dbottillo.mtgsearchfree.util.Logger
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilder {
    @ActivityScope
    @ContributesAndroidInjector(modules = [SetsFragmentModule::class])
    abstract fun contributeSetsFragmentInjector(): SetsFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [DecksFragmentModule::class])
    abstract fun contributeDecksFragmentInjector(): DecksFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeDeckFragmentInjector(): DeckFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeDeckStartingHandFragmentInjector(): DeckStartingHandFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [AddToDeckFragmentModule::class])
    abstract fun contributeAddToDeckFragmentInjector(): AddToDeckFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [CardsConfiguratorFragmentModule::class])
    abstract fun contributeCardsConfiguratorFragmentInjector(): CardsConfiguratorFragment

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun contributeSettingsFragment(): SettingsFragment
}

@Module
class SetsFragmentModule {
    @Provides
    fun providesSetsFragmentPresenter(
        setsInteractor: SetsInteractor,
        cardsInteractor: CardsInteractor,
        cardsPreferences: CardsPreferences,
        generalData: GeneralData,
        logger: Logger
    ): SetsFragmentPresenter {
        return SetsFragmentPresenterImpl(setsInteractor, cardsInteractor, cardsPreferences, generalData, logger)
    }
}

@Module
class DecksFragmentModule {
    @Provides
    fun providesDecksFragmentPresenter(
        interactor: DecksInteractor,
        logger: Logger
    ): DecksFragmentPresenter {
        return DecksFragmentPresenterImpl(interactor, logger)
    }
}

@Module
class AddToDeckFragmentModule {
    @Provides
    fun provideDecksPresenter(interactor: AddToDeckInteractor, logger: Logger): AddToDeckPresenter {
        return AddToDeckPresenterImpl(interactor, logger)
    }
}

@Module
class CardsConfiguratorFragmentModule {
    @Provides
    fun providesCardsConfiguratorPresenter(
        interactor: CardFilterInteractor,
        logger: Logger
    ): CardsConfiguratorPresenter {
        return CardsConfiguratorPresenterImpl(interactor, logger)
    }
}