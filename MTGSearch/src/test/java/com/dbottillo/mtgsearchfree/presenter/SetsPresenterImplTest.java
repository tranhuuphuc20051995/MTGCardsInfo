package com.dbottillo.mtgsearchfree.presenter;

import com.dbottillo.mtgsearchfree.BaseTest;
import com.dbottillo.mtgsearchfree.interactors.SetsInteractor;
import com.dbottillo.mtgsearchfree.model.MTGSet;
import com.dbottillo.mtgsearchfree.model.storage.CardsPreferences;
import com.dbottillo.mtgsearchfree.view.SetsView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import rx.Observable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SetsPresenterImplTest extends BaseTest {

    private SetsView view;
    private SetsInteractor interactor;
    private SetsPresenter presenter;

    @Mock
    List<MTGSet> sets;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        interactor = mock(SetsInteractor.class);
        view = mock(SetsView.class);
        when(interactor.load()).thenReturn(Observable.just(sets));
        presenter = new SetsPresenterImpl(interactor, new TestRxWrapper<List<MTGSet>>(),
                mock(CardsPreferences.class), mock(MemoryStorage.class));
        presenter.init(view);
    }

    @Test
    public void testLoadSets() throws Exception {
        presenter.loadSets();
        verify(interactor).load();
        verify(view).setsLoaded(sets);
    }

}