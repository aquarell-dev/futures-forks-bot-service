package org.aquarell.forks;

public enum Direction {
    LONG_SHORT("LONG/SHORT"),
    SHORT_LONG("SHORT/LONG");

    public final String caption;

    private Direction(String caption) {
        this.caption = caption;
    }
}
