/**
 *
 * @author  NHC
 * @version 1.2
 * @since   2023-10-26
 */
package com.example.mobilefinal;

public class GameSession implements Comparable<GameSession> {
    private int id;
    private int score;
    private long playtimeMillisecond;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getPlaytimeMillisecond() {
        return playtimeMillisecond;
    }

    public void setPlaytime(long playtimeMillisecond) {
        this.playtimeMillisecond = playtimeMillisecond;
    }

    @Override
    public int compareTo(GameSession gameSession) {
        long comparePlaytime = gameSession.getPlaytimeMillisecond();

        // Ascending order
        return Math.toIntExact(this.playtimeMillisecond - comparePlaytime); // toIntExact() throws an exception if the result overflows an int.
    }
}
