package com.dbottillo.mtgsearchfree.model;

import java.util.ArrayList;
import java.util.List;

public class CardsBucket {

    String key;
    private List<MTGCard> cards;

    public CardsBucket() {
        cards = new ArrayList<>();
    }

    public CardsBucket(MTGSet set, List<MTGCard> cards) {
        this.key = set.getName();
        this.cards = cards;
    }

    public CardsBucket(String key, List<MTGCard> cards) {
        this.key = key;
        this.cards = cards;
    }

    public List<MTGCard> getCards() {
        return cards;
    }

    public boolean isValid(String otherKey) {
        return key.equals(otherKey);
    }

    public void setCards(List<MTGCard> cards) {
        this.cards = cards;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "CardsBucket{"
                + "key='" + key + '\''
                + ", cards=" + cards
                + '}';
    }
}
