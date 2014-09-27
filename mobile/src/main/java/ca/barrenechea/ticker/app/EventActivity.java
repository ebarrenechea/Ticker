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

package ca.barrenechea.ticker.app;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.event.OnEventDelete;
import ca.barrenechea.ticker.ui.EventFragment;

public class EventActivity extends BaseActivity {

    public static final String KEY_ID = "EventActivity.Event.Id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);

        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager manager = this.getFragmentManager();
        Fragment f = manager.findFragmentById(R.id.frame_content);

        if (f == null) {
            f = EventFragment.newInstance(getIntent().getLongExtra(KEY_ID, 0));
            manager.beginTransaction().add(R.id.frame_content, f).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void notify(OnEventDelete e) {
        Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);
    }
}
