package com.dbottillo.mtgsearchfree.model.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dbottillo.mtgsearchfree.model.CardsBucket;
import com.dbottillo.mtgsearchfree.model.Deck;
import com.dbottillo.mtgsearchfree.model.MTGCard;
import com.dbottillo.mtgsearchfree.util.LOG;

import java.util.ArrayList;

public final class DeckDataSource {

    private DeckDataSource() {

    }

    public static final String TABLE = "decks";
    public static final String TABLE_JOIN = "deck_card";


    public enum COLUMNS {
        NAME("name", "TEXT not null"),
        COLOR("color", "TEXT"),
        ARCHIVED("archived", "INT");

        private String name;
        private String type;

        COLUMNS(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }
    }

    public enum COLUMNSJOIN {
        DECK_ID("deck_id", "INT not null"),
        CARD_ID("card_id", "INT not null"),
        QUANTITY("quantity", "INT not null"),
        SIDE("side", "INT");

        private String name;
        private String type;

        COLUMNSJOIN(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }
    }

    public static String generateCreateTable() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        builder.append(TABLE).append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT, ");
        for (COLUMNS column : COLUMNS.values()) {
            builder.append(column.name).append(' ').append(column.type);
            if (column != COLUMNS.ARCHIVED) {
                builder.append(',');
            }
        }
        builder.append(')');
        return builder.toString();
    }

    public static String generateCreateTableJoin() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        builder.append(TABLE_JOIN).append(" (");
        for (COLUMNSJOIN column : COLUMNSJOIN.values()) {
            builder.append(column.name).append(' ').append(column.type);
            if (column != COLUMNSJOIN.SIDE) {
                builder.append(',');
            }
        }
        builder.append(')');
        return builder.toString();
    }

    public static long addDeck(SQLiteDatabase db, String name) {
        ContentValues values = new ContentValues();
        values.put(COLUMNS.NAME.getName(), name);
        values.put(COLUMNS.ARCHIVED.getName(), 0);
        return db.insert(TABLE, null, values);
    }

    public static long addDeck(MTGCardDataSource cardDataSource, SQLiteDatabase db, CardsBucket bucket) {
        long deckId = addDeck(db, bucket.getKey());
        if (bucket.getCards().isEmpty()) {
            return deckId;
        }
        for (MTGCard card : bucket.getCards()) {
            MTGCard realCard = cardDataSource.searchCard(card.getName());
            if (realCard != null) {
                realCard.setSideboard(card.isSideboard());
                addCardToDeck(db, deckId, realCard, card.getQuantity());
            }
        }
        return deckId;
    }

    public static void addCardToDeck(SQLiteDatabase db, long deckId, MTGCard card, int quantity) {
        int sid = card.isSideboard() ? 1 : 0;
        int currentCard = 0;
        String query = "select H.*,P.* from MTGCard P inner join deck_card H on (H.card_id = P.multiVerseId and H.deck_id = ? and P.multiVerseId = ? and H.side == ?)";
        LOG.query(query, deckId + "", card.getMultiVerseId() + "", sid + "");
        Cursor cardsCursor = db.rawQuery(query, new String[]{deckId + "", card.getMultiVerseId() + "", sid + ""});
        if (cardsCursor.getCount() > 0) {
            LOG.d("card already in the database");
            cardsCursor.moveToFirst();
            currentCard = cardsCursor.getInt(cardsCursor.getColumnIndex(COLUMNSJOIN.QUANTITY.getName()));
        }
        if (currentCard + quantity < 0) {
            LOG.d("the quantity is negative and is bigger than the current quantity so needs to be removed");
            cardsCursor.close();
            removeCardFromDeck(db, deckId, card);
            return;
        }
        if (currentCard > 0) {
            // there is already some cards there! just need to add the quantity
            LOG.d("just need to update the quantity");
            ContentValues values = new ContentValues();
            values.put(COLUMNSJOIN.QUANTITY.getName(), currentCard + quantity);
            String[] args = new String[]{deckId + "", card.getMultiVerseId() + "", sid + ""};
            db.update(TABLE_JOIN, values, COLUMNSJOIN.DECK_ID.getName() + " = ? and " + COLUMNSJOIN.CARD_ID.getName() + " = ? and " + COLUMNSJOIN.SIDE.getName() + " = ?", args);
            cardsCursor.close();
            return;
        }
        cardsCursor.close();
        addCardToDeckWithoutCheck(db, deckId, card, quantity);
    }

    public static void addCardToDeckWithoutCheck(SQLiteDatabase db, long deckId, MTGCard card, int quantity) {
        String query = "select * from MTGCard where multiVerseId=?";
        LOG.query(query, card.getMultiVerseId() + "");
        Cursor current = db.rawQuery(query, new String[]{card.getMultiVerseId() + ""});
        if (current.getCount() > 0) {
            // card already added
            if (current.getCount() > 1) {
                // there is a duplicate
                current.moveToFirst();
                current.moveToNext();
                while (!current.isAfterLast()) {
                    String query2 = "delete from MTGCard where _id=?";
                    LOG.query(query2, current.getString(0));
                    db.rawQuery(query2, new String[]{current.getString(0)}).moveToFirst();
                    current.moveToNext();
                }
            }
        } else {
            // need to add the card
            long cardId = CardDataSource.saveCard(db, card);
        }
        current.close();
        ContentValues values = new ContentValues();
        values.put(COLUMNSJOIN.CARD_ID.getName(), card.getMultiVerseId());
        values.put(COLUMNSJOIN.DECK_ID.getName(), deckId);
        values.put(COLUMNSJOIN.QUANTITY.getName(), quantity);
        values.put(COLUMNSJOIN.SIDE.getName(), card.isSideboard() ? 1 : 0);
        db.insert(TABLE_JOIN, null, values);
    }

    public static ArrayList<Deck> getDecks(SQLiteDatabase db) {
        ArrayList<Deck> decks = new ArrayList<>();
        String query = "Select * from decks";
        LOG.query(query);
        Cursor deckCursor = db.rawQuery(query, null);
        deckCursor.moveToFirst();
        while (!deckCursor.isAfterLast()) {
            Deck newDeck = fromCursor(deckCursor);
            decks.add(newDeck);
            deckCursor.moveToNext();
        }
        deckCursor.close();
        setQuantityOfCards(db, decks, false);   // standard cards
        setQuantityOfCards(db, decks, true);    // sideboard cards
        return decks;
    }

    private static void setQuantityOfCards(SQLiteDatabase db, ArrayList<Deck> decks, boolean side) {
        // need to loadSet cards now
        //select SUM(quantity),* from deck_card DC left join decks D on (D._id = DC.deck_id) where side= 0 group by DC.deck_id
        //Cursor cursor = database.rawQuery("Select * from decks D left join deck_card DC on (D._id = DC.deck_id) where DC.side=0", null);
        //Cursor cursor = db.rawQuery("select SUM(quantity),D._id from deck_card DC left join decks D on (D._id = DC.deck_id) where side= 0 group by DC.deck_id", null);
        String query = "select SUM(quantity),D._id from deck_card DC left join decks D on (D._id = DC.deck_id) where side=" + (side ? 1 : 0) + " group by DC.deck_id";
        LOG.query(query);
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            for (Deck deck : decks) {
                if (deck.getId() == cursor.getInt(1)) {
                    if (side) {
                        deck.setSizeOfSideboard(cursor.getInt(0));
                    } else {
                        deck.setNumberOfCards(cursor.getInt(0));
                    }
                    break;
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    public static ArrayList<MTGCard> getCards(SQLiteDatabase db, Deck deck) {
        return getCards(db, deck.getId());
    }

    public static ArrayList<MTGCard> getCards(SQLiteDatabase db, long deckId) {
        ArrayList<MTGCard> cards = new ArrayList<>();
        String query = "select H.*,P.* from MTGCard P inner join deck_card H on (H.card_id = P.multiVerseId and H.deck_id = ?)";
        LOG.query(query, deckId + "");
        Cursor cursor = db.rawQuery(query, new String[]{deckId + ""});

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MTGCard card = CardDataSource.fromCursor(cursor);
            int quantity = cursor.getInt(cursor.getColumnIndex(COLUMNSJOIN.QUANTITY.getName()));
            card.setQuantity(quantity);
            int sideboard = cursor.getInt(cursor.getColumnIndex(COLUMNSJOIN.SIDE.getName()));
            card.setSideboard(sideboard == 1);
            cards.add(card);
            cursor.moveToNext();
        }
        cursor.close();
        return cards;
    }

    public static void removeCardFromDeck(SQLiteDatabase db, long deckId, MTGCard card) {
        int sid = card.isSideboard() ? 1 : 0;
        String[] args = new String[]{deckId + "", card.getMultiVerseId() + "", sid + ""};
        String query = "DELETE FROM deck_card where deck_id=? and card_id=? and side =?";
        LOG.query(query, args);
        db.rawQuery(query, args).moveToFirst();
    }

    public static void deleteDeck(SQLiteDatabase db, Deck deck) {
        String[] args = new String[]{deck.getId() + ""};
        String query = "DELETE FROM deck_card where deck_id=? ";
        LOG.query(query, args);
        db.rawQuery(query, args).moveToFirst();
        String query2 = "DELETE FROM decks where _id=? ";
        db.rawQuery(query2, args).moveToFirst();
    }

    public static void deleteAllDecks(SQLiteDatabase db) {
        String query = "DELETE FROM deck_card";
        LOG.query(query);
        db.rawQuery(query, null).moveToFirst();
        String query2 = "DELETE FROM decks";
        LOG.query(query2);
        db.rawQuery(query2, null).moveToFirst();
    }

    public static int renameDeck(SQLiteDatabase db, long deckId, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMNS.NAME.getName(), name);
        return db.update(TABLE, contentValues, "_id=" + deckId, null);
    }

    public static Deck fromCursor(Cursor cursor) {
        Deck deck = new Deck(cursor.getLong(cursor.getColumnIndex("_id")));
        deck.setName(cursor.getString(cursor.getColumnIndex(COLUMNS.NAME.getName())));
        deck.setArchived(cursor.getInt(cursor.getColumnIndex(COLUMNS.ARCHIVED.getName())) == 1);
        return deck;
    }
}
