/*
 *
 *  * Copyright (C) 2014 Eduardo Barrenechea
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
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

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.rx.EventProvider;
import ca.barrenechea.ticker.utils.ViewUtils;
import ca.barrenechea.ticker.widget.EventAdapter;
import rx.Observer;
import rx.Subscription;

public class EventListFragment extends BaseFragment implements Observer<List<Event>> {

    private static final String TAG = "EventListFragment";
    private static final String NAME_ASC = "name COLLATE NOCASE ASC";
    private static final String NAME_DESC = "name COLLATE NOCASE DESC";
    private static final String STARTED_ASC = "started ASC";
    private static final String STARTED_DESC = "started DESC";
    private static final int INITIAL_LOAD_DELAY = 500;

    @InjectView(R.id.list)
    RecyclerView mRecyclerView;
    @InjectView(R.id.loading)
    View mLoadingView;
    @InjectView(R.id.empty)
    View mEmptyView;

    @Inject
    EventProvider mEventProvider;

    private EventAdapter mAdapter;
    private Subscription mSubscription;

    private String mOrderBy = NAME_ASC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_event, container, false);

        ButterKnife.inject(this, view);

        mAdapter = new EventAdapter(getActivity(), null);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_list_event, menu);
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
            case R.id.sort_name_desc:
            case R.id.sort_start_asc:
            case R.id.sort_start_desc:
                setSortStrategy(id);
                item.setChecked(true);
                reloadData();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSortStrategy(int id) {
        switch (id) {
            case R.id.sort_name_asc:
                mOrderBy = NAME_ASC;
                break;

            case R.id.sort_name_desc:
                mOrderBy = NAME_DESC;
                break;

            case R.id.sort_start_asc:
                mOrderBy = STARTED_ASC;
                break;

            case R.id.sort_start_desc:
                mOrderBy = STARTED_DESC;
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // data loads too fast and flashes the screen
        final Handler h = new Handler();
        h.postDelayed(() -> reloadData(), INITIAL_LOAD_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();

        resetSubscription();
    }

    private void reloadData() {
        try {
            PreparedQuery<Event> query = getQuery();

            resetSubscription();

            mSubscription = mEventProvider.query(query).subscribe(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void resetSubscription() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    private PreparedQuery<Event> getQuery() throws SQLException {
        QueryBuilder<Event, Long> queryBuilder = mEventProvider.queryBuilder();
        return queryBuilder.orderByRaw(mOrderBy).prepare();
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
    public void onNext(List<Event> events) {
        if (events.size() > 0) {
            mAdapter.setList(events);
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
