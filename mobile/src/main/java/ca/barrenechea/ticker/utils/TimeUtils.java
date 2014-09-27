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

package ca.barrenechea.ticker.utils;

import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Minutes;

import ca.barrenechea.ticker.data.TimeSpan;

public class TimeUtils {

    public static TimeSpan getCurrentSpan(long start) {
        return getSpan(start, System.currentTimeMillis());
    }

    public static TimeSpan getSpan(long start, long end) {
        Interval interval = new Interval(start, end);

        Days days = Days.daysIn(interval);
        Hours hours = Hours.hoursIn(interval).minus(days.toStandardHours());
        Minutes minutes = Minutes.minutesIn(interval).minus(days.toStandardMinutes()).minus(hours.toStandardMinutes());

        return new TimeSpan(days.getDays(), hours.getHours(), minutes.getMinutes());
    }
}
