package com.dbottillo.mtgsearchfree.ui.decks

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.dbottillo.mtgsearchfree.R
import com.dbottillo.mtgsearchfree.model.Deck
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.ui.BasicFragment
import com.dbottillo.mtgsearchfree.util.LOG
import com.dbottillo.mtgsearchfree.util.TrackingManager
import javax.inject.Inject

class AddToDeckFragment : BasicFragment(), AddToDeckView {

    lateinit var chooseDeck: Spinner
    lateinit var chooseQuantity: Spinner
    lateinit var sideboard: CheckBox
    lateinit var cardNameInputLayout: TextInputLayout
    lateinit var deckName: EditText
    lateinit var cardQuantityInputLayout: TextInputLayout
    lateinit var cardQuantity: EditText

    internal var decksChoose: MutableList<String> = mutableListOf()
    internal var quantityChoose: Array<String> = arrayOf()

    internal var decks: List<Deck> = mutableListOf()
    lateinit var card: MTGCard

    @Inject
    lateinit var presenter: AddToDeckPresenter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_add_to_deck, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chooseDeck = view.findViewById(R.id.choose_deck) as Spinner
        chooseQuantity = view.findViewById(R.id.choose_quantity) as Spinner
        sideboard = view.findViewById(R.id.add_to_deck_sideboard) as CheckBox
        cardNameInputLayout = view.findViewById(R.id.new_deck_name_input_layout) as TextInputLayout
        cardQuantityInputLayout = view.findViewById(R.id.new_deck_quantity_input_layout) as TextInputLayout
        deckName = view.findViewById(R.id.new_deck_name) as EditText
        cardQuantity = view.findViewById(R.id.new_deck_quantity) as EditText
        view.findViewById(R.id.add_to_deck_save).setOnClickListener { addToDeck() }

        card = arguments.getParcelable<MTGCard>("card")
        cardQuantity.filters = arrayOf<InputFilter>(InputFilterMinMax(1, 30))

        setupQuantitySpinner()

        mtgApp.uiGraph.inject(this)
        presenter.init(this)
        presenter.loadDecks()
    }

    private fun setupQuantitySpinner() {
        LOG.d()
        quantityChoose = arrayOf(getString(R.string.deck_choose_quantity), "1", "2", "3", "4", getString(R.string.deck_specify))
        val adapter = ArrayAdapter<CharSequence>(activity, R.layout.add_to_deck_spinner_item, quantityChoose)
        adapter.setDropDownViewResource(R.layout.add_to_deck_dropdown_item)
        chooseQuantity.adapter = adapter
        chooseQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                LOG.d()
                if (position == 5) {
                    chooseQuantity.visibility = View.GONE
                    cardQuantityInputLayout.visibility = View.VISIBLE
                    cardQuantity.requestFocus()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LOG.d()
        val dialog = super.onCreateDialog(savedInstanceState)
        // request a window without the title
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun getPageTrack(): String {
        return "/add_to_deck"
    }

    private fun setupDecksSpinner(decks: List<Deck>) {
        LOG.d()
        this.decks = decks
        decksChoose.clear()
        decksChoose.add(getString(R.string.deck_choose))
        decks.forEach { decksChoose.add(it.name) }
        decksChoose.add(getString(R.string.deck_new))
        val adapter = ArrayAdapter<CharSequence>(activity, R.layout.add_to_deck_spinner_item, decksChoose.toTypedArray())
        adapter.setDropDownViewResource(R.layout.add_to_deck_dropdown_item)
        chooseDeck.adapter = adapter
        chooseDeck.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == decks.size + 1) {
                    chooseDeck.visibility = View.GONE
                    cardNameInputLayout.visibility = View.VISIBLE
                    deckName.requestFocus()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    internal fun addToDeck() {
        LOG.d()
        var quantity = -1
        if (chooseQuantity.visibility == View.VISIBLE && chooseQuantity.selectedItemPosition > 0) {
            quantity = Integer.parseInt(quantityChoose[chooseQuantity.selectedItemPosition])
        }
        if (chooseQuantity.visibility == View.GONE && cardQuantity.text.isNotEmpty()) {
            quantity = Integer.parseInt(cardQuantity.text.toString())
        }
        if (quantity > -1) {
            if (chooseDeck.visibility == View.VISIBLE && chooseDeck.selectedItemPosition > 0) {
                val deck = decks[chooseDeck.selectedItemPosition - 1]
                val side = sideboard.isChecked
                saveCard(quantity, deck, side)
                dismiss()
            }
            if (chooseDeck.visibility == View.GONE && deckName.text.isNotEmpty()) {
                val side = sideboard.isChecked
                saveCard(quantity, deckName.text.toString(), side)
                dismiss()
            }
        }
    }

    private fun saveCard(quantity: Int, deck: Deck, side: Boolean) {
        LOG.d()
        card.isSideboard = side
        presenter.addCardToDeck(deck, card, quantity)
        TrackingManager.trackAddCardToDeck(quantity.toString() + " - existing")
    }

    private fun saveCard(quantity: Int, deck: String, side: Boolean) {
        LOG.d()
        card.isSideboard = side
        presenter.addCardToDeck(deck, card, quantity)
        TrackingManager.trackNewDeck(deck)
        TrackingManager.trackAddCardToDeck(quantity.toString() + " - existing")
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun decksLoaded(decks: List<Deck>) {
        LOG.d()
        setupDecksSpinner(decks)
    }

    class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(min, max, input)) {
                    return null
                }
            } catch (ignored: NumberFormatException) {
                return ""
            }

            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    companion object {

        fun newInstance(card: MTGCard): DialogFragment {
            val instance = AddToDeckFragment()
            val args = Bundle()
            args.putParcelable("card", card)
            instance.arguments = args
            return instance
        }
    }

}