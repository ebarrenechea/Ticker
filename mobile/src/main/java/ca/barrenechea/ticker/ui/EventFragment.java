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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.event.OnEventClose;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventFragment extends BaseFragment implements RealmChangeListener, Toolbar.OnMenuItemClickListener {

    private static final long DURATION = 175;
    private static final String KEY_ID = "Event.Id";

    private static final int FLAGS = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;

    private Event mEvent;
    private String mName;
    private String mNote = "";
    private long mStart;

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

    private Realm mRealm;

    private String mId;
    private boolean mIsDirty = false;
    private boolean mIsEditing = false;

    private MenuItem mMenu;

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
        if (args != null) {
            mId = args.getString(KEY_ID);
        }

        mStart = DateTime.now().withMillisOfSecond(0).getMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        ButterKnife.inject(this, view);

        mToolbar.setNavigationOnClickListener(v -> {
            if (mIsEditing && (mEvent != null || mIsDirty)) {
                resetViews();
                transitionViews(false);
                Toast.makeText(this.getActivity(), R.string.changes_not_saved, Toast.LENGTH_LONG).show();
            } else {
                mBus.post(new OnEventClose());
            }
        });

        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.inflateMenu(R.menu.fragment_event);

        mMenu = mToolbar.getMenu().findItem(R.id.action_reset);

        mTextName.setOnClickListener(v -> startEdit(mEditName));
        mTextNote.setOnClickListener(v -> startEdit(mEditNote));

        mListView.setEmptyView(mEmptyView);

        if (TextUtils.isEmpty(mId)) {
            showForm();
            mIsEditing = true;
        } else {
            hideForm();
            mIsEditing = false;
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (!TextUtils.isEmpty(mId)) {
                    mRealm.removeChangeListener(this);
                    Observable.create(
                            s -> {
                                final Realm realm = Realm.getInstance(this.getActivity());
                                realm.beginTransaction();
                                RealmResults<Event> result = realm.where(Event.class)
                                        .equalTo(Event.COLUMN_ID, mId)
                                        .findAll();

                                result.clear();
                                realm.commitTransaction();

                                s.onCompleted();
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnCompleted(() -> {
                                mBus.post(new OnEventClose());
                            })
                            .subscribe();
                } else {
                    if (mIsDirty) {
                        mIsDirty = false;
                    }

                    mBus.post(new OnEventClose());
                }
                return true;

            case R.id.action_reset:
                if (mIsEditing) {
                    int message = R.string.changes_discarded;
                    if (apply()) {
                        message = R.string.changes_saved;
                        mIsDirty = true;
                    }

                    transitionViews(false);
                    Toast.makeText(this.getActivity(), message, Toast.LENGTH_LONG).show();
                }

                setViews();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mRealm = Realm.getInstance(this.getActivity());
        mRealm.addChangeListener(this);
        if (!TextUtils.isEmpty(mId)) {
            mEvent = Realm.getInstance(this.getActivity())
                    .where(Event.class)
                    .equalTo(Event.COLUMN_ID, mId)
                    .findFirst();

            onChange();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mRealm.removeChangeListener(this);

        if (mIsDirty) {
            mIsDirty = false;
            mRealm.beginTransaction();
            if (mEvent == null) {
                mEvent = mRealm.createObject(Event.class);
            }

            mEvent.setId(UUID.randomUUID().toString());
            mEvent.setName(mName);
            mEvent.setNote(mNote);
            mEvent.setStart(mStart);

            mRealm.commitTransaction();
        }
    }

    @Override
    public void onChange() {
        if (mEvent != null) {
            mName = mEvent.getName();
            mNote = mEvent.getNote();
            mStart = mEvent.getStart();

            this.setViews();
        }
    }

    private void startEdit(View view) {
        transitionViews(true);
        view.requestFocus();
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
                    showForm();
                } else {
                    hideForm();
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

    private void hideForm() {
        mEditName.setAlpha(0f);
        mEditNote.setAlpha(0f);
        mEditName.setVisibility(View.INVISIBLE);
        mEditNote.setVisibility(View.INVISIBLE);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_back);
        mMenu.setIcon(R.drawable.ic_action_reset);
    }

    private void showForm() {
        mTextName.setAlpha(0f);
        mTextNote.setAlpha(0f);
        mTextName.setVisibility(View.INVISIBLE);
        mTextNote.setVisibility(View.INVISIBLE);
        mToolbar.setNavigationIcon(R.drawable.ic_action_cancel);
        mMenu.setIcon(R.drawable.ic_action_accept);
    }

    private boolean apply() {
        final CharSequence name = mEditName.getText();
        final CharSequence note = mEditNote.getText();

        if (TextUtils.isEmpty(name)) {
            // ABORT! can't save event without a name!
            return false;
        }

        mName = name.toString();
        mNote = note.toString();

        mIsDirty = true;
        this.setViews();

        return true;
    }

    private void resetViews() {
        mEditName.setText("");
        mEditName.append(mName);

        if (!TextUtils.isEmpty(mNote)) {
            mEditNote.setText("");
            mEditNote.append(mNote);
        }
    }

    private void setViews() {
        mTextName.setText(mName);
        mEditName.setText("");
        mEditName.append(mName);

        if (!TextUtils.isEmpty(mNote)) {
            mTextNote.setText(mNote);
            mEditNote.setText("");
            mEditNote.append(mNote);
        } else {
            mTextNote.setText("");
            mEditNote.setText("");
        }

        mTextTime.setText(DateUtils.formatDateTime(getActivity(), mStart, FLAGS));
    }
}
