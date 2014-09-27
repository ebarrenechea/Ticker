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
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Event;
import ca.barrenechea.ticker.data.HistoryEntry;
import ca.barrenechea.ticker.utils.TimeUtils;

public class HistoryAdapter extends BaseAdapter {

    private Context mContext;
    private Event mEvent;
    private LayoutInflater mInflater;

    public HistoryAdapter(Context context, Event event) {
        mContext = context;
        mEvent = event;
        mInflater = LayoutInflater.from(context);
    }

    public void setEvent(Event event) {
        mEvent = event;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mEvent != null) {
            return mEvent.getHistorySize();
        } else {
            return 0;
        }
    }

    @Override
    public HistoryEntry getItem(int i) {
        return mEvent.getHistory().get(i);
    }

    @Override
    public long getItemId(int i) {
        return mEvent.getHistory().get(i).getStart();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder h = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_item_history, viewGroup, false);

            h = new ViewHolder(view);
            view.setTag(h);
        }

        if (h == null) {
            h = (ViewHolder) view.getTag();
        }

        HistoryEntry entry = mEvent.getHistory().get(i);
        h.start.setText(DateUtils.formatDateTime(mContext, entry.getStart(), DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME));

        TimeUtils.Span s = TimeUtils.getSpan(entry.getStart(), entry.getEnd());
        h.end.setText(String.valueOf(s.days) + " days " + String.valueOf(s.hours) + ":" + String.valueOf(s.minutes));

        final String note = entry.getNote();
        if (!TextUtils.isEmpty(note)) {
            h.note.setText(note);
        } else {
            h.note.setText("");
        }

        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.text_start)
        TextView start;
        @InjectView(R.id.text_end)
        TextView end;
        @InjectView(R.id.text_note)
        TextView note;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
