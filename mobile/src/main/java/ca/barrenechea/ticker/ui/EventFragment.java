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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.event.OnEventDelete;
import ca.barrenechea.ticker.event.OnEventEdit;
import ca.barrenechea.ticker.widget.EpisodeAdapter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventFragment extends BaseFragment implements RealmChangeListener, View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

    private static final String TAG = "EventFragment";

    private static final long DURATION = 175;
    private static final String KEY_ID = "Event.Id";

    private static final int FLAGS = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;

    private static enum Status {CANCELLED, SUCCESS, FAILURE}

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.text_name)
    TextView mTextName;
    @InjectView(R.id.text_note)
    TextView mTextNote;
    @InjectView(R.id.text_time)
    TextView mTextTime;
    @InjectView(R.id.edit_name)
    EditText mEditName;
    @InjectView(R.id.edit_note)
    EditText mEditNote;
    @InjectView(R.id.list)
    ListView mListView;
    @InjectView(R.id.empty)
    View mEmptyView;

    private String mId;
    private boolean mIsDirty = false;
    private boolean mIsEditing = false;

    private EpisodeAdapter mAdapter;
    private Event mEvent;

    public static EventFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(KEY_ID, id);

        EventFragment f = new EventFragment();
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Event id cannot be null!");
        }

        mId = args.getString(KEY_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        ButterKnife.inject(this, view);

        mToolbar.setNavigationIcon(R.drawable.ic_navigation_back);
        mToolbar.setNavigationOnClickListener(this);

        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.inflateMenu(R.menu.fragment_event);

        mTextName.setOnClickListener(v -> startEdit(mEditName));
        mTextNote.setOnClickListener(v -> startEdit(mEditNote));

        mAdapter = new EpisodeAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyView);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event, menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mBus.post(new OnEventDelete(mEvent.getId()));
                return true;

            case R.id.action_reset:
                if (mIsEditing) {
                    int message = R.string.changes_discarded;
                    if (saveEdit()) {
                        message = R.string.event_saved;
                    }

                    transitionViews(false);
                    Toast.makeText(this.getActivity(), message, Toast.LENGTH_LONG).show();
                } else {
                    //TODO: re-enable event reset
//                mEvent.reset();
                }
                mIsDirty = true;
                bindEventData();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        loadData();
        Realm.getInstance(this.getActivity()).addChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        Realm.getInstance(this.getActivity()).removeChangeListener(this);

        if (mIsDirty) {
            mBus.post(new OnEventEdit(mEvent.getId()));
            mIsDirty = false;
        }
    }

    @Override
    public void onChange() {
        loadData();
    }

    private void startEdit(View view) {
        if (mEvent != null) {
            transitionViews(true);
            view.requestFocus();
        }
    }

    @Override
    public void onClick(View v) {
        if (mIsEditing) {
            resetForm();
            transitionViews(false);
            Toast.makeText(this.getActivity(), R.string.event_not_saved, Toast.LENGTH_LONG).show();
        } else {
            this.getActivity().onBackPressed();
        }
    }

    private void transitionViews(final boolean editing) {
        mIsEditing = editing;
        final ObjectAnimator hideName = ObjectAnimator.ofFloat(mTextName, "alpha", editing ? 0 : 1);
        final ObjectAnimator hideNote = ObjectAnimator.ofFloat(mTextNote, "alpha", editing ? 0 : 1);
        final ObjectAnimator editName = ObjectAnimator.ofFloat(mEditName, "alpha", editing ? 1 : 0);
        final ObjectAnimator editNote = ObjectAnimator.ofFloat(mEditNote, "alpha", editing ? 1 : 0);

        final AnimatorSet set = new AnimatorSet();
        set.playTogether(hideName, hideNote, editName, editNote);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (editing) {
                    mTextName.setVisibility(View.INVISIBLE);
                    mTextNote.setVisibility(View.INVISIBLE);
                } else {
                    mEditName.setVisibility(View.INVISIBLE);
                    mEditNote.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (editing) {
            mEditName.setVisibility(View.VISIBLE);
            mEditNote.setVisibility(View.VISIBLE);
        } else {
            mTextName.setVisibility(View.VISIBLE);
            mTextNote.setVisibility(View.VISIBLE);
            mTextNote.requestFocus();
        }

        set.setDuration(DURATION);
        set.start();
    }

    private void loadData() {
        Observable.create(
                subscriber -> {
                    final Realm realm = Realm.getInstance(this.getActivity());
                    RealmQuery<Event> query = realm.where(Event.class).equalTo(Event.COLUMN_ID, mId);

                    subscriber.onNext(query.findAll());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(t -> Log.e(TAG, "Error loading event.", t))
                .doOnNext(
                        o -> {
                            if (o instanceof RealmResults) {
                                final Object item = ((RealmResults) o).first();
                                if (item instanceof Event) {
                                    mEvent = (Event) item;
                                    bindEventData();
                                    mAdapter.setData(mEvent.getEpisodes());
                                }
                            }
                        })
                .subscribe();
    }

    private boolean saveEdit() {
        final CharSequence name = mEditName.getText();
        final CharSequence note = mEditNote.getText();

        if (TextUtils.isEmpty(name)) {
            // ABORT! can't save event without a name!
            return false;
        }

        mEvent.setName(name.toString());
        mEvent.setNote(note.toString());
        mBus.post(new OnEventEdit(mEvent.getId()));

        return true;
    }

    private void resetForm() {
        mEditName.setText("");
        mEditName.append(mEvent.getName());

        final String note = mEvent.getNote();
        if (!TextUtils.isEmpty(note)) {
            mEditNote.setText("");
            mEditNote.append(note);
        }
    }

    private void bindEventData() {
        final String name = mEvent.getName();
        mTextName.setText(name);
        mEditName.setText("");
        mEditName.append(name);

        final String note = mEvent.getNote();
        if (!TextUtils.isEmpty(note)) {
            mTextNote.setText(note);
            mEditNote.setText("");
            mEditNote.append(note);
        }

        mTextTime.setText(DateUtils.formatDateTime(getActivity(), mEvent.getStarted(), FLAGS));
        mAdapter.notifyDataSetChanged();
    }
}
