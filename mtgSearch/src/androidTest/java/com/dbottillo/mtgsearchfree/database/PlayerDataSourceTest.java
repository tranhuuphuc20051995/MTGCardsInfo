package com.dbottillo.mtgsearchfree.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;

import com.dbottillo.mtgsearchfree.resources.Player;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class PlayerDataSourceTest extends BaseDatabaseTest {

    @Test
    public void generate_table_is_correct() {
        String query = PlayerDataSource.generateCreateTable();
        assertNotNull(query);
        assertThat(query, is("CREATE TABLE IF NOT EXISTS MTGPlayer (_id INTEGER PRIMARY KEY, name TEXT,life INT,poison INT)"));
    }

    @Test
    public void player_can_be_saved_in_database() {
        Player player = generatePlayer();
        long id = PlayerDataSource.savePlayer(cardsInfoDbHelper.getWritableDatabase(), player);
        Cursor cursor = cardsInfoDbHelper.getReadableDatabase().rawQuery("select * from " + PlayerDataSource.TABLE + " where rowid =?", new String[]{id + ""});
        assertNotNull(cursor);
        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        Player playerFromDb = PlayerDataSource.fromCursor(cursor);
        assertNotNull(playerFromDb);
        assertPlayerSame(playerFromDb, player);
        cursor.close();
    }

    @Test
    public void player_can_be_removed_from_database() {
        Player player = generatePlayer();
        long id = PlayerDataSource.savePlayer(cardsInfoDbHelper.getWritableDatabase(), player);
        PlayerDataSource.removePlayer(cardsInfoDbHelper.getWritableDatabase(), player);
        Cursor cursor = cardsInfoDbHelper.getReadableDatabase().rawQuery("select * from " + PlayerDataSource.TABLE + " where rowid =?", new String[]{id + ""});
        assertNotNull(cursor);
        assertThat(cursor.getCount(), is(0));
        cursor.close();
    }

    @Test
    public void player_are_unique_in_database() {
        SQLiteDatabase db = cardsInfoDbHelper.getWritableDatabase();
        int uniqueId = 444;
        Player player = generatePlayer(uniqueId, "Jayce", 15, 2);
        long id = PlayerDataSource.savePlayer(db, player);
        Player player2 = generatePlayer(uniqueId, "Jayce", 18, 4);
        long id2 = PlayerDataSource.savePlayer(db, player2);
        assertThat(id, is(id2));
        Cursor cursor = db.rawQuery("select * from " + PlayerDataSource.TABLE + " where _id =?", new String[]{uniqueId + ""});
        assertNotNull(cursor);
        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        Player playerFromDb = PlayerDataSource.fromCursor(cursor);
        assertNotNull(playerFromDb);
        assertPlayerSame(playerFromDb, player2);
        cursor.close();
    }

    @Test
    public void test_cards_can_be_retrieved_from_database() {
        SQLiteDatabase db = cardsInfoDbHelper.getWritableDatabase();
        Player player1 = generatePlayer();
        PlayerDataSource.savePlayer(db, player1);
        Player player2 = generatePlayer(20, "Liliana", 10, 10);
        PlayerDataSource.savePlayer(db, player2);
        Player player3 = generatePlayer(30, "Garruck", 12, 3);
        PlayerDataSource.savePlayer(db, player3);
        List<Player> player = PlayerDataSource.getPlayers(db);
        assertNotNull(player);
        assertThat(player.size(), is(3));
        assertPlayerSame(player.get(0), player1);
        assertPlayerSame(player.get(1), player2);
        assertPlayerSame(player.get(2), player3);
    }

    private Player generatePlayer() {
        return generatePlayer(10, "Jayce", 15, 2);
    }

    private Player generatePlayer(int id, String name, int life, int poison) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setLife(life);
        player.setPoisonCount(poison);
        return player;
    }

    private void assertPlayerSame(Player one, Player two) {
        assertThat(one.getId(), is(two.getId()));
        assertThat(one.getName(), is(two.getName()));
        assertThat(one.getLife(), is(two.getLife()));
        assertThat(one.getPoisonCount(), is(two.getPoisonCount()));
    }
}