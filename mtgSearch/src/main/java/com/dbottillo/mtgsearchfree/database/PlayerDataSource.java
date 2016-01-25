package com.dbottillo.mtgsearchfree.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dbottillo.mtgsearchfree.helper.LOG;
import com.dbottillo.mtgsearchfree.resources.Player;

import java.util.ArrayList;

public final class PlayerDataSource {

    public static final String TABLE = "MTGPlayer";

    public enum COLUMNS {
        NAME("name", "TEXT"),
        LIFE("life", "INT"),
        POISON("poison", "INT");

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

    private PlayerDataSource() {
    }

    public static String generateCreateTable() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        builder.append(TABLE).append(" (_id INTEGER PRIMARY KEY, ");
        for (COLUMNS column : COLUMNS.values()) {
            builder.append(column.name).append(' ').append(column.type);
            if (column != COLUMNS.POISON) {
                builder.append(',');
            }
        }
        builder.append(')');
        return builder.toString();
    }

    public static long savePlayer(SQLiteDatabase db, Player player) {
        ContentValues values = new ContentValues();
        values.put("_id", player.getId());
        values.put(COLUMNS.LIFE.getName(), player.getLife());
        values.put(COLUMNS.POISON.getName(), player.getPoisonCount());
        values.put(COLUMNS.NAME.getName(), player.getName());
        return db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static ArrayList<Player> getPlayers(SQLiteDatabase db) {
        String query = "SELECT * FROM " + TABLE + " order by _ID ASC";
        LOG.d("[getPlayers] query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Player> players = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                players.add(fromCursor(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return players;
    }

    public static void removePlayer(SQLiteDatabase db, Player player) {
        String[] args = new String[]{player.getId() + ""};
        String query = "DELETE FROM " + TABLE + " where _id=? ";
        LOG.d("[getPlayers] query: " + query + " with args: " + player.getId());
        db.rawQuery(query, args).moveToFirst();
    }

    public static Player fromCursor(Cursor cursor) {
        Player player = new Player();
        player.setId(cursor.getInt(cursor.getColumnIndex("_id")));
        player.setLife(cursor.getInt(cursor.getColumnIndex(COLUMNS.LIFE.getName())));
        player.setPoisonCount(cursor.getInt(cursor.getColumnIndex(COLUMNS.POISON.getName())));
        player.setName(cursor.getString(cursor.getColumnIndex(COLUMNS.NAME.getName())));
        return player;
    }
}