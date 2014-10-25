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
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.TimeSpan;
import ca.barrenechea.ticker.event.OnEventView;
import ca.barrenechea.ticker.utils.TimeUtils;
import io.realm.RealmResults;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private static final int FLAGS = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;

    private LayoutInflater mInflater;
    private RealmResults<Event> mData;
    private Context mContext;
    private Bus mBus;

    public EventAdapter(Context context, Bus bus) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mBus = bus;
    }

    public void setData(RealmResults<Event> data) {
        mData = data;
    }

    public void sort(String column, boolean order) {
        if (mData != null) {
            mData = mData.sort(column, order);
            this.notifyDataSetChanged();
        }
    }

    public RealmResults<Event> getData() {
        return mData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        final View view = mInflater.inflate(R.layout.adapter_item_event, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            final Event event = mData.get(holder.getPosition());
            mBus.post(new OnEventView(event.getId()));
        });

//        view.setOnLongClickListener(v -> {
//            final Event event = mData.get(holder.getPosition());
//            mBus.post(new OnEventSelect(event));
//        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Event event = mData.get(i);

        TimeSpan s = TimeUtils.getCurrentSpan(event.getStart());

        holder.name.setText(event.getName());
        holder.days.setText(String.valueOf(s.days));
        holder.time.setText(s.hours + ":" + (s.minutes < 10 ? "0" : "") + s.minutes);
        holder.date.setText(DateUtils.formatDateTime(mContext, event.getStart(), FLAGS));
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }

        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.text_name)
        TextView name;
        @InjectView(R.id.text_date)
        TextView date;
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
