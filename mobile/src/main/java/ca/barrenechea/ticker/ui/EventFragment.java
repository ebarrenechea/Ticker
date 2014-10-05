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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ActionMode;
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

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.EventLoader;
import ca.barrenechea.ticker.event.OnEventDelete;
import ca.barrenechea.ticker.event.OnEventEdit;
import ca.barrenechea.ticker.widget.HistoryAdapter;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observer;
import rx.Subscription;

public class EventFragment extends BaseFragment implements Observer<RealmResults<Event>> {

    private static final String TAG = "EventFragment";

    private static final long DURATION = 175;
    private static final String KEY_ID = "Event.Id";

    private static final int FLAGS = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;

    private static enum Status {CANCELLED, SUCCESS, FAILURE}

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

    @Inject
    EventLoader mEventLoader;

    private String mId;
    private boolean mIsDirty = false;

    private HistoryAdapter mAdapter;
    private Event mEvent;
    private Subscription mSubscription;
    private ActionMode mActionMode;
    private ActionMode.Callback mCallback = new ActionMode.Callback() {

        private Status mEditState;

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            if (mActionMode == null) {
                mActionMode = actionMode;
                mEditState = Status.CANCELLED;
                return true;
            }

            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.actionmode_event, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_save) {
                if (saveEdit()) {
                    mEditState = Status.SUCCESS;
                } else {
                    mEditState = Status.FAILURE;
                }

                actionMode.finish();
                transitionViews(false);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;

            String message = null;
            switch (mEditState) {
                case FAILURE:
                    message = "Invalid changes discarded.";
                    resetForm();
                    break;
                case CANCELLED:
                    message = "Edit cancelled.";
                    resetForm();
                    break;
                case SUCCESS:
                    message = "Changes saved.";
                    break;
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            transitionViews(false);
        }
    };

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

        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        ButterKnife.inject(this, view);

        mTextName.setOnClickListener(v -> startEdit(mEditName));
        mTextNote.setOnClickListener(v -> startEdit(mEditNote));

        mAdapter = new HistoryAdapter(getActivity(), null);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mBus.post(new OnEventDelete(mEvent));
                return true;

            case R.id.action_reset:
//                mEvent.reset();
                mIsDirty = true;
                bindEventData();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        resetSubscription();
        registerEvent();
    }

    private void resetSubscription() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    private void startEdit(View view) {
        if (mEvent != null) {
            transitionViews(true);
            view.requestFocus();
        }
    }

    private void transitionViews(final boolean editing) {
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

                    getActivity().startActionMode(mCallback);
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

    private void registerEvent() {
        RealmQuery<Event> query = mEventLoader.getQuery().equalTo("id", mId);
        mSubscription = mEventLoader.load(query).subscribe(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        resetSubscription();

        if (mIsDirty) {
            mBus.post(new OnEventEdit(mEvent));
            mIsDirty = false;
        }
    }

    private boolean saveEdit() {
        // we still don't have an id to listen to changes on save
        final CharSequence name = mEditName.getText();
        final CharSequence note = mEditNote.getText();

        if (TextUtils.isEmpty(name)) {
            // ABORT! can't save event without a name!
            return false;
        }

        mEvent.setName(name.toString());
        mEvent.setNote(note.toString());
        mBus.post(new OnEventEdit(mEvent));

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

    @Override
    public void onCompleted() {
        // do nothing
    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "Error loading data!", e);
    }

    @Override
    public void onNext(RealmResults<Event> result) {
        if (result != null) {
            mEvent = result.first();
            bindEventData();
            mAdapter.setEvent(mEvent);
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
