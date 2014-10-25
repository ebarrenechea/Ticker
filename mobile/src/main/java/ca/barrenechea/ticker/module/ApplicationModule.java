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

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import ca.barrenechea.ticker.TickerApp;
import ca.barrenechea.ticker.app.EventActivity;
import ca.barrenechea.ticker.app.MainActivity;
import ca.barrenechea.ticker.ui.EventFragment;
import ca.barrenechea.ticker.ui.EventListFragment;
import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                EventActivity.class,
                MainActivity.class,

                EventFragment.class,
                EventListFragment.class,
        },
        includes = {
                DataModule.class,
        }
)
public class ApplicationModule {
    private final TickerApp tickerApp;

    public ApplicationModule(TickerApp app) {
        tickerApp = app;
    }

    @Provides
    @ForApplication
    Context provideAppContext() {
        return tickerApp.getApplicationContext();
    }

    @Provides
    @Singleton
    Bus providesBus() {
        return new Bus();
    }
}
