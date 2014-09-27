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

package ca.barrenechea.ticker.module;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import javax.inject.Singleton;

import ca.barrenechea.ticker.data.DatabaseHelper;
import ca.barrenechea.ticker.data.Event;
import dagger.Module;
import dagger.Provides;

@Module(
        complete = false,
        library = true
)
public class DataModule {

    @Provides
    @Singleton
    DatabaseHelper providesDatabaseHelper(@ForApplication Context context) {
        return new DatabaseHelper(context);
    }

    @Provides
    @Singleton
    RuntimeExceptionDao<Event, Long> providesEventDao(DatabaseHelper helper) {
        return helper.getRuntimeExceptionDao(Event.class);
    }
}
