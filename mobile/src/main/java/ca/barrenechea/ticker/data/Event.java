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

package ca.barrenechea.ticker.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

@DatabaseTable(tableName = "Events")
public class Event {

    public static final long INVALID_ID = -1;

    private static final Gson sGson = new Gson();

    @DatabaseField(columnName = "_id", generatedId = true)
    private long id;
    @DatabaseField(canBeNull = false, index = true)
    private String name;
    @DatabaseField(canBeNull = false)
    private long created;
    @DatabaseField(canBeNull = false)
    private long updated;
    @DatabaseField(canBeNull = false)
    private long started = -1;
    @DatabaseField(canBeNull = true)
    private String note;
    @DatabaseField(canBeNull = false, defaultValue = "[]")
    private String history;

    private boolean historyDirty = false;
    private List<HistoryEntry> listHistory;

    public Event() {
        final long milli = getPreparedTime().getMillis();
        id = INVALID_ID;
        created = milli;
        updated = milli;
        started = milli;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public void start() {
        this.setStarted(System.currentTimeMillis());
    }

    public void reset() {
        historyDirty = true;
        HistoryEntry entry = new HistoryEntry(started, getPreparedTime().getMillis(), note);
        getHistory().add(0, entry);

        start();
    }

    public long getStarted() {
        return started;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHistoryJson() {
        return history;
    }

    public boolean isHistoryDirty() {
        return historyDirty;
    }

    public List<HistoryEntry> getHistory() {
        if (listHistory == null) {
            Type listType = new TypeToken<LinkedList<HistoryEntry>>() {
            }.getType();
            listHistory = sGson.fromJson(history, listType);
        }

        return listHistory;
    }

    public void packHistory() {
        history = sGson.toJson(listHistory);
    }

    public int getHistorySize() {
        return getHistory().size();
    }

    private DateTime getPreparedTime() {
        return new DateTime().withMillisOfSecond(0);
    }
}
