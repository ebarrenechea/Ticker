/*
 * Copyright (C) 2014 Eduardo Barrenechea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.barrenechea.ticker.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.EventLoader;
import ca.barrenechea.ticker.utils.ViewUtils;
import ca.barrenechea.ticker.widget.EventAdapter;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observer;
import rx.Subscription;

public class EventListFragment extends BaseFragment implements Observer<RealmResults<Event>> {

    private static final String TAG = "EventListFragment";
    private static final int INITIAL_LOAD_DELAY = 500;

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;
    @InjectView(R.id.loading)
    View mLoadingView;
    @InjectView(R.id.empty)
    View mEmptyView;
    @InjectView(R.id.text_empty)
    TextView mTextEmpty;

    @Inject
    EventLoader mEventLoader;

    private Subscription mSubscription;
    private EventAdapter mAdapter;
    private SearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_event, container, false);

        ButterKnife.inject(this, view);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new EventAdapter(getActivity(), mBus);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_list_event, menu);

        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }

            private void search(String s) {
                final RealmQuery<Event> query = mEventLoader.getQuery();
                reloadData(query.contains("name", s));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                CreateEventDialog d = new CreateEventDialog();
                d.show(this.getFragmentManager(), "EditEvent");
                return true;

            case R.id.sort_name_asc:
                // string sorting not supported yet
//                mAdapter.sortBy("name", RealmResults.SORT_ORDER_ASCENDING);
//                item.setChecked(true);
                return true;

            case R.id.sort_name_desc:
                // string sorting not supported yet
//                mAdapter.sortBy("name", RealmResults.SORT_ORDER_DECENDING);
//                item.setChecked(true);
                return true;

            case R.id.sort_start_asc:
                mAdapter.sortBy("started", RealmResults.SORT_ORDER_ASCENDING);
                item.setChecked(true);
                return true;

            case R.id.sort_start_desc:
                mAdapter.sortBy("started", RealmResults.SORT_ORDER_DECENDING);
                item.setChecked(true);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // data loads too fast and flashes the screen
        final Handler h = new Handler();
        h.postDelayed(() -> reloadData(null), INITIAL_LOAD_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();

        resetSubscription();
    }

    private void reloadData(RealmQuery<Event> query) {
        resetSubscription();
        if (query == null) {
            query = mEventLoader.getQuery();
        }

        mSubscription = mEventLoader.load(query).subscribe(this);
    }

    private void resetSubscription() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        mSubscription = null;
    }

    @Override
    public void onCompleted() {
        // do nothing
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "Error loading data.", e);
    }

    @Override
    public void onNext(RealmResults<Event> results) {
        if (results.size() > 0) {
            mAdapter.setData(results);
            showList();
        } else {
            showEmpty();
        }
    }

    private void showList() {
        if (mRecyclerView.getVisibility() == View.INVISIBLE) {
            ViewUtils.fadeIn(mRecyclerView);

            if (mLoadingView.getVisibility() == View.VISIBLE) {
                ViewUtils.fadeOut(mLoadingView);
            }

            if (mEmptyView.getVisibility() == View.VISIBLE) {
                ViewUtils.fadeOut(mEmptyView);
            }
        }
    }

    private void showEmpty() {
        if (mSearchView.isIconified()) {
            mTextEmpty.setText(R.string.no_events);
        } else {
            final String msg = String.format(Locale.getDefault(), this.getString(R.string.nothing_found), mSearchView.getQuery());
            mTextEmpty.setText(msg);
        }

        if (mEmptyView.getVisibility() == View.INVISIBLE) {
            ViewUtils.fadeIn(mEmptyView);

            if (mLoadingView.getVisibility() == View.VISIBLE) {
                ViewUtils.fadeOut(mLoadingView);
            }

            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                ViewUtils.fadeOut(mRecyclerView);
            }
        }
    }
}
