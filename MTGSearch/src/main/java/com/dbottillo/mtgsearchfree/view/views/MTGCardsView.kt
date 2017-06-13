package com.dbottillo.mtgsearchfree.view.views

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

import com.dbottillo.mtgsearchfree.R
import com.dbottillo.mtgsearchfree.model.CardFilter
import com.dbottillo.mtgsearchfree.model.MTGCard
import com.dbottillo.mtgsearchfree.model.database.CardDataSource
import com.dbottillo.mtgsearchfree.ui.cards.CardAdapterConfiguration
import com.dbottillo.mtgsearchfree.util.LOG
import com.dbottillo.mtgsearchfree.util.UIUtil
import com.dbottillo.mtgsearchfree.ui.cards.CardsAdapter
import com.dbottillo.mtgsearchfree.view.adapters.OnCardListener

import butterknife.BindView
import butterknife.ButterKnife

class MTGCardsView : RelativeLayout {

    internal var grid = false
    private var adapter: CardsAdapter? = null
    private var itemDecorator: GridItemDecorator? = null

    lateinit var listView: RecyclerView
    lateinit var emptyView: TextView
    lateinit var footer: View

    constructor(ctx: Context) : super(ctx) {
        init(ctx)
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        init(ctx)
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int) : super(ctx, attrs, defStyle) {
        init(ctx)
    }

    private fun init(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.fragment_set, this, true)
        listView = view.findViewById(R.id.card_list) as RecyclerView
        emptyView = view.findViewById(R.id.empty_view) as TextView
        footer = view.findViewById(R.id.search_bottom_container)

        itemDecorator = GridItemDecorator(resources.getDimensionPixelSize(R.dimen.cards_grid_space))
        listView.setHasFixedSize(true)
        setListOn() // default
    }

    fun setEmptyString(res: Int) {
        emptyView.setText(res)
    }

    fun loadCards(cards: List<MTGCard>, listener: OnCardListener, title: Int, titleImage: Int) {
        loadCards(cards, listener, null, CardAdapterConfiguration(
                title = context.getString(title),
                headerIconTitle = titleImage,
                isGrid = grid,
                menu = R.menu.card_option))
    }

    fun loadCards(cards: List<MTGCard>, listener: OnCardListener, title: String, titleImage: Int, cardFilter: CardFilter?, menuOption: Int) {
        loadCards(cards, listener, cardFilter, CardAdapterConfiguration(
                title = title,
                headerIconTitle = titleImage,
                isGrid = grid,
                menu = menuOption))
    }

    fun loadCards(cards: List<MTGCard>, listener: OnCardListener,
                  cardFilter: CardFilter?, configuration: CardAdapterConfiguration) {
        LOG.d()

        adapter = CardsAdapter(cards, listener, cardFilter, configuration)
        adapter?.let{
            listView.adapter = adapter
            it.notifyDataSetChanged()
        }

        if (cards.size == CardDataSource.LIMIT) {
            val moreResult = footer.findViewById(R.id.more_result) as TextView
            moreResult.text = resources.getQuantityString(R.plurals.search_limit, CardDataSource.LIMIT, CardDataSource.LIMIT)
            UIUtil.setHeight(footer, UIUtil.dpToPx(context, 60))
        } else {
            UIUtil.setHeight(footer, 0)
        }
        emptyView.visibility = if (adapter?.itemCount == 0) View.VISIBLE else View.GONE
    }

    fun setGridOn() {
        LOG.d()
        grid = true
        val columns = resources.getInteger(R.integer.cards_grid_column_count)
        val glm = GridLayoutManager(context, columns)
        glm.initialPrefetchItemCount = columns
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) glm.spanCount else 1
            }
        }
        listView.addItemDecoration(itemDecorator)
        listView.layoutManager = glm
        tryRefresh()
    }

    fun setListOn() {
        LOG.d()
        grid = false
        val llm = LinearLayoutManager(context)
        llm.initialPrefetchItemCount = 6
        listView.removeItemDecoration(itemDecorator)
        listView.layoutManager = llm
        tryRefresh()
    }

    private fun tryRefresh() {
        adapter?.let {
            val cards = it.cards
            val listener = it.listener
            val cardFilter = it.cardFilter
            val configuration = it.configuration.copy(isGrid = grid)
            loadCards(cards = cards, listener = listener, cardFilter = cardFilter, configuration = configuration)
        }
    }

    private inner class GridItemDecorator(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State?) {
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.left = 0
                outRect.right = 0
                outRect.bottom = 0
                outRect.top = 0
            } else {
                outRect.left = space / 2
                outRect.right = space / 2
                outRect.bottom = space / 2
                outRect.top = space
            }
        }
    }
}