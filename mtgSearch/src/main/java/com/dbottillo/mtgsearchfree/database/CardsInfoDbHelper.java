package com.dbottillo.mtgsearchfree.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.VisibleForTesting;

/**
 * Helper for access the use database that contains:
 * - cards (that are in decks and favourites)
 * - decks
 * - favourites
 * - players (for life counter)
 */
public final class CardsInfoDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "cardsinfo.db";
    protected static final int DATABASE_VERSION = 4;

    private static CardsInfoDbHelper instance;

    public static synchronized CardsInfoDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CardsInfoDbHelper(context);
        }
        return instance;
    }

    @VisibleForTesting
    public CardsInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CardDataSource.generateCreateTable());
        db.execSQL(DeckDataSource.generateCreateTable());
        db.execSQL(DeckDataSource.generateCreateTableJoin());
        db.execSQL(PlayerDataSource.generateCreateTable());
        db.execSQL(FavouritesDataSource.generateCreateTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion >= 2) {
            db.execSQL(CardDataSource.SQL_ADD_COLUMN_RULINGS);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL(PlayerDataSource.generateCreateTable());
            db.execSQL(FavouritesDataSource.generateCreateTable());
        }
        if (oldVersion < 4 && newVersion >= 4) {
            db.execSQL(CardDataSource.SQL_ADD_COLUMN_SET_CODE);
            db.execSQL(CardDataSource.SQL_ADD_COLUMN_NUMBER);
            db.execSQL(DeckDataSource.generateCreateTable());
            db.execSQL(DeckDataSource.generateCreateTableJoin());
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized void clear() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(CardDataSource.TABLE, null, null);
        db.delete(DeckDataSource.TABLE, null, null);
        db.delete(DeckDataSource.TABLE_JOIN, null, null);
        db.delete(PlayerDataSource.TABLE, null, null);
        db.delete(FavouritesDataSource.TABLE, null, null);
    }
}