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

package ca.barrenechea.ticker.data;

import android.text.TextUtils;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import ca.barrenechea.ticker.event.OnEventDelete;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

@Singleton
public class EventLoader implements RealmChangeListener {

    private Realm mRealm;
    private LinkedList<BehaviorSubject<Void>> mSubjectList;

    @Inject
    public EventLoader(Realm realm, Bus bus) {
        mRealm = realm;
        mRealm.addChangeListener(this);
        mSubjectList = new LinkedList<>();

        bus.register(this);
    }

    public RealmQuery<Event> getQuery() {
        return mRealm.where(Event.class);
    }

    public Observable<RealmResults<Event>> loadAll() {
        return load(getQuery());
    }

    public Observable<RealmResults<Event>> load(final RealmQuery<Event> query) {
        final BehaviorSubject<Void> queryHolder = BehaviorSubject.create();
        return queryHolder
                .map(o -> query.findAll())
                .doOnSubscribe(() -> {
                    mSubjectList.add(queryHolder);
                    queryHolder.onNext(null);
                })
                .doOnUnsubscribe(() -> mSubjectList.remove(queryHolder))
                .asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void notifyObservers() {
        for (BehaviorSubject<Void> subject : mSubjectList) {
            subject.onNext(null);
        }
    }

    public void create(String name, String note) {
        Observable.create(f -> {
            mRealm.beginTransaction();

            final Event e = mRealm.createObject(Event.class);
            e.setId(UUID.randomUUID().toString());
            e.setName(name);

            if (!TextUtils.isEmpty(note)) {
                e.setNote(note);
            }

            long milli = new DateTime().withMillisOfSecond(0).getMillis();
            e.setCreated(milli);
            e.setUpdated(milli);
            e.setStarted(milli);

            mRealm.commitTransaction();
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void onChange() {
        notifyObservers();
    }

    @Subscribe
    public void notify(OnEventDelete e) {
        Observable.create(f -> {
            final RealmResults<Event> results = mRealm.where(Event.class).equalTo("id", e.id).findAll();
            mRealm.beginTransaction();
            results.clear();
            mRealm.commitTransaction();
        }).subscribeOn(Schedulers.io()).subscribe();
    }
}
