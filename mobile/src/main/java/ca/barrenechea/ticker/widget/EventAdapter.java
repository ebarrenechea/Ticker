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

package ca.barrenechea.ticker.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.TimeSpan;
import ca.barrenechea.ticker.event.OnEventOpen;
import ca.barrenechea.ticker.utils.TimeUtils;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Event> mList;
    private Bus mBus;

    public EventAdapter(Context context, List<Event> list, Bus bus) {
        mInflater = LayoutInflater.from(context);
        mList = list;
        mBus = bus;
    }

    public void setList(List<Event> list) {
        mList = list;
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.adapter_item_event, viewGroup, false);
        view.setOnClickListener(v -> mBus.post(new OnEventOpen(mList.get(i))));
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Event event = mList.get(i);

        TimeSpan s = TimeUtils.getCurrentSpan(event.getStarted());

        holder.name.setText(event.getName());
        holder.days.setText(String.valueOf(s.days));
        holder.time.setText(s.hours + ":" + (s.minutes < 10 ? "0" : "") + s.minutes);
        holder.elapsed.setText(s.days + " days, " + s.hours + ":" + s.minutes);
    }

    @Override
    public int getItemCount() {
        if (mList != null) {
            return mList.size();
        }

        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.text_name)
        TextView name;
        @InjectView(R.id.text_elapsed)
        TextView elapsed;
        @InjectView(R.id.text_days)
        TextView days;
        @InjectView(R.id.text_time)
        TextView time;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.inject(this, view);
        }
    }
}
