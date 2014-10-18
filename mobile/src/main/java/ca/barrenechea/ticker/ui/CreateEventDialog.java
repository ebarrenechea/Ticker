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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.utils.ViewUtils;
import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CreateEventDialog extends BaseDialog {
    private static final String TAG = "CreateEventDialog";
    private static final int ANIMATION_DELAY = 325;

    @InjectView(R.id.edit_name)
    EditText mEditName;
    @InjectView(R.id.button_positive)
    Button mButtonPositive;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_event, container, false);

        ButterKnife.inject(this, view);

        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mButtonPositive.setEnabled(editable.length() > 0);
            }
        });

        mEditName.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (action == EditorInfo.IME_ACTION_DONE) {
                create();
                return true;
            }
            return false;
        });

        mButtonPositive.setEnabled(false);
        mButtonPositive.setOnClickListener(v -> create());

        return view;
    }

    private void create() {
        ViewUtils.hideSoftInput(mEditName);

        // wait for keyboard to dismiss
        final Handler h = new Handler();
        h.postDelayed(() -> {
            final String name = mEditName.getText().toString();

            if (!TextUtils.isEmpty(name)) {
                Observable.create(
                        subscriber -> {
                            Realm realm = Realm.getInstance(this.getActivity());
                            realm.beginTransaction();

                            final Event e = realm.createObject(Event.class);
                            e.setId(UUID.randomUUID().toString());
                            e.setName(name);

                            long milli = new DateTime().withMillisOfSecond(0).getMillis();
                            e.setCreated(milli);
                            e.setStarted(milli);

                            realm.commitTransaction();

                            subscriber.onCompleted();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(t -> Log.e(TAG, "Error creating event.", t))
                        .doOnCompleted(
                                () -> {
                                    Toast.makeText(this.getActivity(), R.string.event_created, Toast.LENGTH_SHORT).show();
                                    this.dismiss();
                                }
                        )
                        .subscribe();
            } else {
                Toast.makeText(this.getActivity(), R.string.event_not_created, Toast.LENGTH_SHORT).show();
            }
        }, ANIMATION_DELAY);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        ButterKnife.reset(this);
    }
}
