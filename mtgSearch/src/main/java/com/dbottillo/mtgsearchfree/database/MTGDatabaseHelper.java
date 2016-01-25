package com.dbottillo.mtgsearchfree.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.dbottillo.mtgsearchfree.BuildConfig;
import com.dbottillo.mtgsearchfree.resources.MTGCard;
import com.dbottillo.mtgsearchfree.resources.MTGSet;
import com.dbottillo.mtgsearchfree.search.SearchParams;
import com.dbottillo.mtgsearchfree.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

/**
 * Helper for access the card database, only on read mode
 * the database is created from {@link CreateDatabaseHelper}
 * and then copied to database folder from the library {@link SQLiteAssetHelper}
 */
public class MTGDatabaseHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "mtgsearch.db";

    public MTGDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MTGDatabaseHelper(Context context) {
        this(context, DATABASE_NAME, null, BuildConfig.DATABASE_VERSION);
        setForcedUpgrade();
    }

    public ArrayList<MTGSet> getSets() {
        SQLiteDatabase db = getReadableDatabase();
        return SetDataSource.getSets(db);
    }

    public ArrayList<MTGCard> getSet(MTGSet set) {
        SQLiteDatabase db = getReadableDatabase();
        return MTGCardDataSource.getSet(db, set);
    }

    public ArrayList<MTGCard> searchCards(SearchParams searchParams) {
        SQLiteDatabase db = getReadableDatabase();
        return MTGCardDataSource.searchCards(db, searchParams);
    }

    public ArrayList<MTGCard> getRandomCard(int number) {
        SQLiteDatabase db = getReadableDatabase();
        return MTGCardDataSource.getRandomCard(db, number);
    }

}