package com.dbottillo.mtgsearchfree.cards

import android.net.Uri
import com.dbottillo.mtgsearchfree.model.CardsCollection
import com.dbottillo.mtgsearchfree.ui.BaseCardsView

interface CardsActivityView : BaseCardsView {
    fun finish()
    fun updateTitle(name: String)
    fun updateTitle(resource: Int)
    fun updateAdapter(cards: CardsCollection, showImage: Boolean, startPosition: Int)
    fun showError(errorMessage: String)
    fun showLoading()
    fun hideLoading()
    fun shareUri(uri: Uri)
    fun renderPreview(preview: Boolean)
}