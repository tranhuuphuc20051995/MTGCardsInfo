package com.dbottillo.mtgsearchfree.ui.views

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dbottillo.mtgsearchfree.R

class BottomTabs : LinearLayout {

    interface BottomTabsListener{
        fun tabSelected(selection: Int) : Unit
    }

    val scaleDefault: Float = 1.0f
    val scaleSelected: Float = 1.2f
    val alphaDefault: Float = 0.7f
    val alphaSelected: Float = 1.0f

    @BindView(R.id.home_tab)
    internal lateinit var homeTab: LinearLayout

    @BindView(R.id.decks_tab)
    internal lateinit var decksTab: LinearLayout

    @BindView(R.id.saved_tab)
    internal lateinit var savedTab: LinearLayout

    @BindView(R.id.life_counter_tab)
    internal lateinit var lifeCounterTab: LinearLayout


    @BindView(R.id.home_tab_image)
    internal lateinit var homeTabImage: ImageView

    @BindView(R.id.decks_tab_image)
    internal lateinit var decksTabImage: ImageView

    @BindView(R.id.saved_tab_image)
    internal lateinit var savedTabImage: ImageView

    @BindView(R.id.life_counter_tab_image)
    internal lateinit var lifeCounterTabImage: ImageView

    var currentSelection = 0
    var listener : BottomTabsListener? = null

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        val view = inflate(context, R.layout.bottom_tabs, this)

        orientation = LinearLayout.HORIZONTAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_primary))

        ButterKnife.bind(this, view)

        refreshUI()
    }

    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()
        return BottomTabsState(parcelable, currentSelection)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        val bottomTabState = state as BottomTabsState
        currentSelection = bottomTabState.selection
        refreshUI()
    }

    fun setBottomTabsListener(listener: BottomTabsListener){
        this.listener = listener
    }

    private fun setSelection(selected: Int) {
        currentSelection = selected
        refreshUI()
    }

    private fun refreshUI() {
        updateTab(homeTab, homeTabImage, selected = currentSelection == 0)
        updateTab(decksTab, decksTabImage, selected = currentSelection == 1)
        updateTab(savedTab, savedTabImage, selected = currentSelection == 2)
        updateTab(lifeCounterTab, lifeCounterTabImage, selected = currentSelection == 3)
    }

    private fun updateTab(tab: LinearLayout, image: ImageView, selected: Boolean) {
        if (selected) {
            tab.alpha = alphaSelected
            image.scaleX = scaleSelected
            image.scaleY = scaleSelected
        } else {
            tab.alpha = alphaDefault
            image.scaleX = scaleDefault
            image.scaleY = scaleDefault
        }
    }

    @OnClick(R.id.home_tab)
    fun homeTabTapped() {
        setSelection(0)
        listener?.tabSelected(0)
    }

    @OnClick(R.id.decks_tab)
    fun decksTabTapped() {
        setSelection(1)
        listener?.tabSelected(1)
    }

    @OnClick(R.id.saved_tab)
    fun savedTabTapped() {
        setSelection(2)
        listener?.tabSelected(2)
    }

    @OnClick(R.id.life_counter_tab)
    fun lifeCounterTabTapped() {
        setSelection(3)
        listener?.tabSelected(3)
    }

    class BottomTabsState : BaseSavedState {

        val selection: Int

        constructor(state: Parcelable, selection: Int) : super(state) {
            this.selection = selection
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeInt(selection)
        }

        constructor(state: Parcel) : super(state) {
            this.selection = state.readInt()
        }

        companion object {

            val CREATOR = object : Parcelable.Creator<BottomTabsState> {
                override fun createFromParcel(`in`: Parcel) = BottomTabsState(`in`)
                override fun newArray(size: Int): Array<BottomTabsState?> = arrayOfNulls(size)
            }
        }
    }
}