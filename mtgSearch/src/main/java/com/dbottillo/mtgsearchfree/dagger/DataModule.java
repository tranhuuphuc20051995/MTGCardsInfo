package com.dbottillo.mtgsearchfree.dagger;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.dbottillo.mtgsearchfree.model.database.CardDataSource;
import com.dbottillo.mtgsearchfree.model.database.DeckDataSource;
import com.dbottillo.mtgsearchfree.model.database.FavouritesDataSource;
import com.dbottillo.mtgsearchfree.model.database.MTGCardDataSource;
import com.dbottillo.mtgsearchfree.model.database.PlayerDataSource;
import com.dbottillo.mtgsearchfree.model.database.SetDataSource;
import com.dbottillo.mtgsearchfree.model.storage.CardsPreferences;
import com.dbottillo.mtgsearchfree.model.storage.CardsPreferencesImpl;
import com.dbottillo.mtgsearchfree.model.storage.CardsStorage;
import com.dbottillo.mtgsearchfree.model.storage.DecksStorage;
import com.dbottillo.mtgsearchfree.model.storage.GeneralData;
import com.dbottillo.mtgsearchfree.model.storage.GeneralPreferences;
import com.dbottillo.mtgsearchfree.model.storage.PlayersStorage;
import com.dbottillo.mtgsearchfree.presenter.MemoryStorage;
import com.dbottillo.mtgsearchfree.util.AppInfo;
import com.dbottillo.mtgsearchfree.util.FileUtil;
import com.dbottillo.mtgsearchfree.util.Logger;
import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context){
        return context.getSharedPreferences("General", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    AppInfo providesAppInfo(Context context){
        return new AppInfo(context);
    }

    @Provides
    @Singleton
    GeneralData providesGeneralData(SharedPreferences sharedPreferences, AppInfo appInfo) {
        return new GeneralPreferences(sharedPreferences, appInfo);
    }

    @Provides
    @Singleton
    CardsPreferences provideGeneralPreferences(Context context) {
        return new CardsPreferencesImpl(context);
    }

    @Provides
    @Singleton
    CardDataSource providesCardDataSource(@Named("storageDB") SQLiteDatabase database, Gson gson){
        return new CardDataSource(database, gson);
    }

    @Provides
    @Singleton
    FavouritesDataSource provideFavouritesDataSource(@Named("storageDB") SQLiteDatabase database, CardDataSource cardDataSource) {
        return new FavouritesDataSource(database, cardDataSource);
    }

    @Provides
    @Singleton
    CardsStorage provideCardsStorage(MTGCardDataSource mtgCardDataSource, DeckDataSource deckDataSource, FavouritesDataSource favouritesDataSource, Logger logger) {
        return new CardsStorage(mtgCardDataSource, deckDataSource, favouritesDataSource, logger);
    }

    @Provides
    @Singleton
    SetDataSource provideSetDataSource(@Named("cardsDB") SQLiteDatabase database) {
        return new SetDataSource(database);
    }

    @Provides
    @Singleton
    PlayerDataSource providePlayerDataSource(@Named("storageDB") SQLiteDatabase database) {
        return new PlayerDataSource(database);
    }

    @Provides
    @Singleton
    PlayersStorage providePlayerStorage(PlayerDataSource playerDataSource, Logger logger) {
        return new PlayersStorage(playerDataSource, logger);
    }

    @Provides
    @Singleton
    DeckDataSource provideDeckDataSource(@Named("storageDB") SQLiteDatabase database, CardDataSource cardDataSource, MTGCardDataSource mtgCardDataSource) {
        return new DeckDataSource(database, cardDataSource, mtgCardDataSource);
    }

    @Provides
    @Singleton
    DecksStorage provideDecksStorage(FileUtil fileUtil, DeckDataSource deckDataSource, Logger logger) {
        return new DecksStorage(fileUtil, deckDataSource, logger);
    }

    @Provides
    @Singleton
    MemoryStorage provideMemoryStorage(Logger logger) {
        return new MemoryStorage(logger);
    }

    @Provides
    @Singleton
    Gson providesGson(){
        return new Gson();
    }
}