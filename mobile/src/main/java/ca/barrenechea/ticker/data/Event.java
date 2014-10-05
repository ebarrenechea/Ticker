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

package ca.barrenechea.ticker.data;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Event extends RealmObject {
    private String id;
    private String name;
    private long created;
    private long updated;
    private long started = -1;
    private String note;
    private RealmList<HistoryEntry> listHistory;

    protected void setId(String id) {
        this.id = id;
    }

    public String getId() {
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

    public RealmList<HistoryEntry> getListHistory() {
        return listHistory;
    }

    public void setListHistory(RealmList<HistoryEntry> listHistory) {
        this.listHistory = listHistory;
    }
}