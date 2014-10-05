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

package ca.barrenechea.ticker.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.event.OnEventView;
import ca.barrenechea.ticker.ui.EventListFragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = this.getFragmentManager();
        Fragment f = manager.findFragmentById(R.id.frame_content);

        if (f == null) {
            f = new EventListFragment();
            manager.beginTransaction().add(R.id.frame_content, f, "List").commit();
        }
    }

    @Subscribe
    public void notify(OnEventView e) {
        Intent intent = new Intent(this, EventActivity.class);

        if (e.event != null) {
            intent.putExtra(EventActivity.KEY_ID, e.event.getId());
        }

        this.startActivity(intent);
    }
}
