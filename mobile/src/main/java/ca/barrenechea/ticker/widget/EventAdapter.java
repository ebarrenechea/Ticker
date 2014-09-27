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

package ca.barrenechea.ticker.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.TimeSpan;
import ca.barrenechea.ticker.utils.TimeUtils;

public class EventAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<Event> mList;

    public EventAdapter(Context context, List<Event> list) {
        mInflater = LayoutInflater.from(context);
        mList = list;
    }

    public void setList(List<Event> list) {
        mList = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }

        return mList.size();
    }

    @Override
    public Event getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder h = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_item_event, viewGroup, false);

            h = new ViewHolder(view);
            view.setTag(h);
        }

        if (h == null) {
            h = (ViewHolder) view.getTag();
        }

        Event e = this.getItem(i);

        TimeSpan s = TimeUtils.getCurrentSpan(e.getStarted());

        h.name.setText(e.getName());
        h.days.setText(String.valueOf(s.days));
        h.time.setText(s.hours + ":" + (s.minutes < 10 ? "0" : "") + s.minutes);
        h.elapsed.setText(s.days + " days, " + s.hours + ":" + s.minutes);

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.text_name)
        TextView name;
        @InjectView(R.id.text_elapsed)
        TextView elapsed;
        @InjectView(R.id.text_days)
        TextView days;
        @InjectView(R.id.text_time)
        TextView time;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
