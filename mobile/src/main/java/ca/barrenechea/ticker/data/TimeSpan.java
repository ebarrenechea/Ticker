package ca.barrenechea.ticker.data;

public class TimeSpan {
    public final int days;
    public final int hours;
    public final int minutes;

    public TimeSpan(int days, int hours, int minutes) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }
}
