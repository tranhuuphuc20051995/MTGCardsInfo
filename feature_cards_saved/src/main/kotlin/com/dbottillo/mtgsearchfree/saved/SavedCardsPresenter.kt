package com.dbottillo.mtgsearchfree.saved

import com.dbottillo.mtgsearchfree.model.MTGCard

interface SavedCardsPresenter {
    fun init(view: SavedCardsView)
    fun load()
    fun removeFromFavourite(card: MTGCard)
    fun toggleCardTypeViewPreference()
    fun onPause()
}