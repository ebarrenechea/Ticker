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

package ca.barrenechea.ticker.data.rx;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class DataProvider<T> {

    private List<BehaviorSubject<Void>> mSubjectList;
    private RuntimeExceptionDao<T, Long> mDao;

    public DataProvider(RuntimeExceptionDao<T, Long> dao) {
        mDao = dao;
        mSubjectList = new LinkedList<>();
    }

    public void createOrUpdate(T data) {
        mDao.createOrUpdate(data);
        notifyObservers();
    }

    public void update(T data) {
        this.createOrUpdate(data);
    }

    public void delete(T data) {
        mDao.delete(data);
        notifyObservers();
    }

    public void delete(PreparedDelete<T> delete) {
        mDao.delete(delete);
        notifyObservers();
    }

    public DeleteBuilder<T, Long> deleteBuilder() {
        return mDao.deleteBuilder();
    }

    public QueryBuilder<T, Long> queryBuilder() {
        return mDao.queryBuilder();
    }

    private void notifyObservers() {
        for (BehaviorSubject<Void> subject : mSubjectList) {
            subject.onNext(null);
        }
    }

    public Observable<T> queryForId(final long id) {
        final BehaviorSubject<Void> queryHolder = BehaviorSubject.create();
        return queryHolder
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(o -> mDao.queryForId(id))
                .doOnSubscribe(() -> {
                    mSubjectList.add(queryHolder);
                    queryHolder.onNext(null);
                })
                .doOnUnsubscribe(() -> mSubjectList.remove(queryHolder))
                .asObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<T>> query(final PreparedQuery<T> query) {
        final BehaviorSubject<Void> queryHolder = BehaviorSubject.create();
        return queryHolder
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(o -> mDao.query(query))
                .doOnSubscribe(() -> {
                    mSubjectList.add(queryHolder);
                    queryHolder.onNext(null);
                })
                .doOnUnsubscribe(() -> mSubjectList.remove(queryHolder))
                .asObservable()
                .observeOn(AndroidSchedulers.mainThread());
    }
}
