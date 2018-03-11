package com.dbottillo.mtgsearchfree.ui.cards

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import com.dbottillo.mtgsearchfree.R
import com.dbottillo.mtgsearchfree.model.CardFilter
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.ui.views.MTGLoader
import com.dbottillo.mtgsearchfree.util.gone
import com.dbottillo.mtgsearchfree.util.show

abstract class CardViewHolder(row: View) : RecyclerView.ViewHolder(row) {
    var parent: View = row.findViewById(R.id.card_parent)
}

class ListCardViewHolder(row: View) : CardViewHolder(row) {
    var name: TextView = row.findViewById(R.id.card_name)
    var setName: TextView = row.findViewById(R.id.card_set_name)
    var rarity: TextView = row.findViewById(R.id.card_rarity)
    var cost: TextView = row.findViewById(R.id.card_cost)
    var indicator: View = row.findViewById(R.id.card_indicator)
    var more: ImageButton = row.findViewById(R.id.card_more_option)

    fun bind(card: MTGCard, isASearch: Boolean, context: Context) {
        name.text = context.getString(R.string.row_card_name, "", card.name)

        val rarityColor =
                when {
                    card.rarity.equals(CardFilter.FILTER_UNCOMMON, ignoreCase = true) -> R.color.uncommon
                    card.rarity.equals(CardFilter.FILTER_RARE, ignoreCase = true) -> R.color.rare
                    card.rarity.equals(CardFilter.FILTER_MYHTIC, ignoreCase = true) -> R.color.mythic
                    else -> R.color.common
                }
        rarity.setTextColor(ContextCompat.getColor(context, rarityColor))
        if (card.rarity.isNotEmpty()) {
            rarity.text = card.rarity
        } else {
            rarity.text = ""
        }

        if (!card.manaCost.isEmpty()) {
            cost.text = card.manaCost.replace("{", "").replace("}", "")
            cost.setTextColor(card.getMtgColor(context))
        } else {
            cost.text = "-"
        }

        if (isASearch) {
            setName.show()
            setName.text = card.set?.name
        } else {
            setName.gone()
        }

        val indicator = indicator.background as GradientDrawable
        indicator.setColor(card.getMtgColor(context))
    }

    fun setupMore(context: Context,
                  card: MTGCard,
                  position: Int,
                  menuRes: Int,
                  onCardListener: OnCardListener?) {
        if (menuRes > 0 && onCardListener != null) {
            more.setOnClickListener {
                val popupMenu = PopupMenu(context, more)
                val menu = popupMenu.menu

                popupMenu.menuInflater.inflate(menuRes, menu)
                if (menu.size() > 4) {
                    val moveOne = menu.getItem(3)
                    moveOne.setTitle(if (card.isSideboard) R.string.move_card_to_deck else R.string.move_card_to_sideboard)
                    val moveAll = menu.getItem(4)
                    moveAll.setTitle(if (card.isSideboard) R.string.move_all_card_to_deck else R.string.move_all_card_to_sideboard)
                }
                popupMenu.setOnMenuItemClickListener { item ->
                    onCardListener.onOptionSelected(item, card, position)
                    false
                }
                popupMenu.show()
            }
        } else {
            more.gone()
        }
    }
}

class GridCardViewHolder(row: View) : CardViewHolder(row) {
    var image: ImageView = row.findViewById(R.id.grid_item_card_image)
    var loader: MTGLoader = row.findViewById(R.id.grid_item_card_loader)
}