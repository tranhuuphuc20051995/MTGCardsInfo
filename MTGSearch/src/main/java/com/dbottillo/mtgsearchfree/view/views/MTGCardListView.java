package com.dbottillo.mtgsearchfree.view.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dbottillo.mtgsearchfree.R;
import com.dbottillo.mtgsearchfree.view.adapters.CardListAdapter;
import com.dbottillo.mtgsearchfree.view.adapters.OnCardListener;
import com.dbottillo.mtgsearchfree.model.database.CardDataSource;
import com.dbottillo.mtgsearchfree.model.MTGCard;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class MTGCardListView extends RelativeLayout implements OnCardListener {

    @Bind(R.id.card_list)
    ListView listView;
    @Bind(R.id.empty_view)
    TextView emptyView;
    @Bind(R.id.progress)
    SmoothProgressBar progressBar;

    private ArrayList<MTGCard> cards = new ArrayList<>();
    private CardListAdapter adapter;

    public MTGCardListView(Context ctx) {
        this(ctx, null);
    }

    public MTGCardListView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, -1);
    }

    public MTGCardListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_set, this, true);
        ButterKnife.bind(this, view);

        adapter = new CardListAdapter(context, cards, false, R.menu.card_option);
        listView.setAdapter(adapter);

    }

    public void loadCards(List<MTGCard> newCards, OnCardListener listener){
        adapter.setOnCardListener(listener);
        cards.clear();
        cards.addAll(newCards);
        adapter.notifyDataSetChanged();
        emptyView.setVisibility((adapter.getCount() == 0) ? View.VISIBLE : View.GONE);
        listView.smoothScrollToPosition(0);

        if (cards.size() == CardDataSource.LIMIT) {
            View footer = LayoutInflater.from(getContext()).inflate(R.layout.search_bottom, listView, false);
            TextView moreResult = (TextView) footer.findViewById(R.id.more_result);
            moreResult.setText(getResources().getQuantityString(R.plurals.search_limit, CardDataSource.LIMIT, CardDataSource.LIMIT));
            listView.addFooterView(footer);
        }
        progressBar.setVisibility(View.GONE);

        emptyView.setVisibility((adapter.getCount() == 0) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCardSelected(MTGCard card, int position) {

    }

    @Override
    public void onOptionSelected(MenuItem menuItem, MTGCard card, int position) {

    }
}
