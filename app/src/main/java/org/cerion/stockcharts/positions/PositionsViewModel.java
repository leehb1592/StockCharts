package org.cerion.stockcharts.positions;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Log;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.web.CachedDataAPI;

import java.util.List;

public class PositionsViewModel {
    private static final String TAG = PositionsViewModel.class.getSimpleName();

    public ObservableList<PositionItemViewModel> positions = new ObservableArrayList<>();
    public ObservableField<Boolean> loading = new ObservableField<>(false);

    private PositionRepository repo;
    private CachedDataAPI api;

    public PositionsViewModel(Context context) {
        repo = new PositionRepository(context);
        api = Injection.getAPI(context);
    }

    public void delete(Position position) {
        Log.d(TAG, "removing " + position.getSymbol());
        repo.delete(position);

        load();
    }

    public void load() {
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                positions.clear();

                List<Position> list = repo.getAll();
                for(Position p : list)
                    positions.add(new PositionItemViewModel(p));
            }

            @Override
            public void onFinish() {
                loading.set(false);
            }
        });

        task.execute();
    }

    public void update() {
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {

            /*
private Map<String,Quote> getQuotes() {
    Set<String> symbols = new HashSet<>();
    for(Position p : positions)
        symbols.add(p.getSymbol());

    return api.getQuotes(symbols);
}
*/
            @Override
            public void run() {
                //Map<String,Quote> quotes = getQuotes();
                for(PositionItemViewModel vm : positions) {
                    String symbol = vm.symbol();

                    //Quote q = quotes.get(symbol);
                    // Always do this since quotes not working
                    // if(p.IsDividendsReinvested())

                    PriceList list;
                    {
                        try {
                            list = api.getPrices(symbol, Interval.DAILY, 500);
                            //p.setPriceHistory(list);

                            List<Dividend> dividends = api.getDividends(symbol);

                            vm.setData(list, dividends);
                        } catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                loading.set(false);
            }
        });

        task.execute();
    }
}
