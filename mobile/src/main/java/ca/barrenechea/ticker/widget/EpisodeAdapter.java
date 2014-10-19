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

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.barrenechea.ticker.R;
import ca.barrenechea.ticker.data.Episode;
import ca.barrenechea.ticker.data.TimeSpan;
import ca.barrenechea.ticker.utils.TimeUtils;

public class EpisodeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Episode> mList;

    public EpisodeAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<Episode> data) {
        mList = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Episode getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mList.get(i).getStart();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder h = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.adapter_item_episode, viewGroup, false);

            h = new ViewHolder(view);
            view.setTag(h);
        }

        if (h == null) {
            h = (ViewHolder) view.getTag();
        }

        final Episode entry = mList.get(i);
        final long start = entry.getStart();
        final long end = start + entry.getDuration();
        h.start.setText(DateUtils.formatDateTime(mContext, start, DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME));

        TimeSpan s = TimeUtils.getSpan(start, end);
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
