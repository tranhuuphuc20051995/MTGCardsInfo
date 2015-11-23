package com.dbottillo.communication;

import com.dbottillo.communication.events.PlayersEvent;
import com.dbottillo.database.CardsInfoDbHelper;
import com.dbottillo.database.MTGDatabaseHelper;
import com.dbottillo.database.PlayerDataSource;

import de.greenrobot.event.EventBus;

class PlayersOperation extends Operation {

    @Override
    protected void execute(MTGDatabaseHelper helper, CardsInfoDbHelper cardsInfoDbHelper, Object... params) {
        PlayersEvent cardsEvent = new PlayersEvent(PlayerDataSource.getPlayers(cardsInfoDbHelper.getReadableDatabase()));
        EventBus.getDefault().postSticky(cardsEvent);
    }
}
