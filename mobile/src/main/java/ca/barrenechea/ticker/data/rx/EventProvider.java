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
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.event.OnEventDelete;
import ca.barrenechea.ticker.event.OnEventEdit;

@Singleton
public class EventProvider extends DataProvider<Event> {

    @Inject
    public EventProvider(Bus bus, RuntimeExceptionDao<Event, Long> dao) {
        super(dao);

        bus.register(this);
    }

    @Subscribe
    public void notify(OnEventEdit e) {
        if (e.event.isHistoryDirty()) {
            e.event.packHistory();
        }

        this.createOrUpdate(e.event);
    }

    @Subscribe
    public void notify(OnEventDelete e) {
        this.delete(e.event);
    }
}
