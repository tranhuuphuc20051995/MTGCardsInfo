package com.dbottillo.mtgsearchfree.presenter;

import com.dbottillo.mtgsearchfree.interactors.CardsInteractor;
import com.dbottillo.mtgsearchfree.mapper.DeckMapper;
import com.dbottillo.mtgsearchfree.model.CardsBucket;
import com.dbottillo.mtgsearchfree.model.CardsCollection;
import com.dbottillo.mtgsearchfree.model.Deck;
import com.dbottillo.mtgsearchfree.model.DeckBucket;
import com.dbottillo.mtgsearchfree.model.MTGCard;
import com.dbottillo.mtgsearchfree.model.MTGSet;
import com.dbottillo.mtgsearchfree.model.SearchParams;
import com.dbottillo.mtgsearchfree.model.storage.GeneralData;
import com.dbottillo.mtgsearchfree.util.Logger;
import com.dbottillo.mtgsearchfree.view.CardsView;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Function;


public class CardsPresenterImpl implements CardsPresenter {

    CardsInteractor interactor;
    private CardsView cardsView;
    private DeckMapper deckMapper;
    private GeneralData generalData;
    private Runner<List<MTGCard>> cardsWrapper;
    private Runner<CardsCollection> searchCardsWrapper;
    private Runner<int[]> favWrapper;
    private MemoryStorage memoryStorage;
    private boolean grid = true;
    private boolean firstTypeTypeCheck = true;

    private final Logger logger;

    @Inject
    public CardsPresenterImpl(CardsInteractor interactor, DeckMapper mapper, GeneralData generalData,
                              RunnerFactory runnerFactory, MemoryStorage memoryStorage, Logger logger) {
        this.logger = logger;
        logger.d("created");
        this.interactor = interactor;
        this.deckMapper = mapper;
        this.generalData = generalData;
        this.cardsWrapper = runnerFactory.simple();
        this.searchCardsWrapper = runnerFactory.simple();
        this.favWrapper = runnerFactory.simple();
        this.memoryStorage = memoryStorage;
    }

    public void getLuckyCards(int howMany) {
        logger.d("get lucky cards " + howMany);
        cardsWrapper.run(interactor.getLuckyCards(howMany), new Runner.RxWrapperListener<List<MTGCard>>() {
            @Override
            public void onNext(List<MTGCard> mtgCards) {
                logger.d();
                cardsView.cardsLoaded(new CardsBucket("lucky", mtgCards));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public void loadFavourites() {
        logger.d();
        cardsWrapper.run(interactor.getFavourites(), new Runner.RxWrapperListener<List<MTGCard>>() {
            @Override
            public void onNext(List<MTGCard> mtgCards) {
                logger.d();
                cardsView.cardsLoaded(new CardsBucket("fav", mtgCards));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private Function<CardsCollection, DeckBucket> mapper = new Function<CardsCollection, DeckBucket>() {
        @Override
        public DeckBucket apply(CardsCollection mtgCards) throws Exception {
            return deckMapper.map(mtgCards);
        }
    };

    @Override
    public void loadDeck(final Deck deck) {
        logger.d("loadDeck " + deck);
        /*deckWrapper.runAndMap(interactor.loadDeck(deck), mapper, new Runner.RxWrapperListener<DeckBucket>() {
            @Override
            public void onNext(DeckBucket bucket) {
                logger.d();
                bucket.setKey(deck.getName());
                cardsView.deckLoaded(bucket);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onCompleted() {

            }
        });*/
    }

    @Override
    public void doSearch(final SearchParams searchParams) {
        logger.d("do search " + searchParams);
        searchCardsWrapper.run(interactor.doSearch(searchParams), new Runner.RxWrapperListener<CardsCollection>() {
            @Override
            public void onNext(CardsCollection mtgCards) {
                logger.d();
                cardsView.cardsLoaded(new CardsBucket(searchParams.toString(), mtgCards.getList()));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @Override
    public void loadCardTypePreference() {
        logger.d();
        boolean isGrid = generalData.isCardsShowTypeGrid();
        if (firstTypeTypeCheck || grid != isGrid) {
            grid = isGrid;
            firstTypeTypeCheck = false;
            cardsView.cardTypePreferenceChanged(grid);
        } // else nothing has changed
    }

    @Override
    public void toggleCardTypeViewPreference() {
        logger.d();
        if (generalData.isCardsShowTypeGrid()) {
            generalData.setCardsShowTypeList();
            cardsView.cardTypePreferenceChanged(false);
        } else {
            generalData.setCardsShowTypeGrid();
            cardsView.cardTypePreferenceChanged(true);
        }
    }

    @Override
    public void removeFromFavourite(MTGCard card, boolean reload) {
        logger.d("remove " + card + " from fav");
        favWrapper.run(interactor.removeFromFavourite(card), reload ? idFavSubscriber : null);
    }

    public void saveAsFavourite(MTGCard card, boolean reload) {
        logger.d("save " + card + " as fav");
        favWrapper.run(interactor.saveAsFavourite(card), reload ? idFavSubscriber : null);
    }

    public void loadCards(final MTGSet set) {
        logger.d("loadSet cards of " + set);
        /*cardsWrapper.run(interactor.loadSet(set), new Runner.RxWrapperListener<List<MTGCard>>() {
            @Override
            public void onNext(List<MTGCard> mtgCards) {
                logger.d();

                cardsView.cardsLoaded(new CardsBucket(set, mtgCards));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onCompleted() {

            }
        });*/
    }

    public void init(CardsView view) {
        logger.d();
        cardsView = view;
    }

    public void loadIdFavourites() {
        logger.d();
        int[] currentFav = memoryStorage.getFavourites();
        if (currentFav != null) {
            cardsView.favIdLoaded(currentFav);
            return;
        }
        favWrapper.run(interactor.loadIdFav(), idFavSubscriber);
    }

    public void detachView() {
        logger.d();
    }

    private Runner.RxWrapperListener<int[]> idFavSubscriber = new Runner.RxWrapperListener<int[]>() {
        @Override
        public void onNext(int[] ints) {
            logger.d();
            memoryStorage.setFavourites(ints);
            cardsView.favIdLoaded(ints);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onCompleted() {

        }
    };

}