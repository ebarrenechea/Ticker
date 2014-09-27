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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import ca.barrenechea.ticker.event.OnEventOpen;
import ca.barrenechea.ticker.widget.EventAdapter;
import rx.Observer;
import rx.Subscription;

public class EventListFragment extends BaseFragment implements Observer<List<Event>> {

    private static final String TAG = "EventListFragment";

    @InjectView(R.id.list)
    ListView mListView;
    @InjectView(R.id.empty)
    View mEmptyView;

    @Inject
    EventProvider mEventProvider;

    private EventAdapter mAdapter;
    private Subscription mSubscription;

    private String mSortColumn = "name";
    private boolean mAscending = true;

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
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setOnItemClickListener((adapterView, v, i, l) -> mBus.post(new OnEventOpen(mAdapter.getItem(i))));

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
                mSortColumn = "name";
                mAscending = true;
                break;

            case R.id.sort_name_desc:
                mSortColumn = "name";
                mAscending = false;
                break;

            case R.id.sort_start_asc:
                mSortColumn = "started";
                mAscending = false;     // we want to see oldest last
                break;

            case R.id.sort_start_desc:
                mSortColumn = "started";
                mAscending = true;      // we want to see oldest first
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        reloadData();
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
        return queryBuilder.orderBy(mSortColumn, mAscending).prepare();
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
        mAdapter.setList(events);
    }
}
