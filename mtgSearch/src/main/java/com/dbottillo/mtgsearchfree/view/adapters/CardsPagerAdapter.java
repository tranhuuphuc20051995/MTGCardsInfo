package com.dbottillo.mtgsearchfree.view.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.dbottillo.mtgsearchfree.resources.MTGCard;

import java.util.ArrayList;

public class CardsPagerAdapter extends PagerAdapter {

    Context context;
    boolean deck;
    boolean showImage;
    ArrayList<MTGCard> cards;

    CardsPagerAdapter(Context context, boolean deck, boolean showImage, ArrayList<MTGCard> cards) {
        this.context = context;
        this.deck = deck;
        this.showImage = showImage;
        this.cards = cards;
    }

    public int getCount() {
        return cards.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        MTGCardView view = new MTGCardView(context);
        view.load(cards.get(position), showImage);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    public CharSequence getPageTitle(int position) {
        MTGCard card = cards.get(position);
        if (deck) {
            return card.getName() + " (" + card.getQuantity() + ")";
        }
        return card.getName();
    }

    public MTGCard getItem(int currentItem) {
        return cards.get(currentItem);
    }

}