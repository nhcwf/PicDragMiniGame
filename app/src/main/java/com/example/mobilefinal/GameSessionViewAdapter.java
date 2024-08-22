/**
 *
 * @author  NHC
 * @version 1.2
 * @since   2023-10-26
 */
package com.example.mobilefinal;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class GameSessionViewAdapter extends BaseAdapter {
    public ArrayList<GameSession> gameSessions;
    private String usernameString;

    public GameSessionViewAdapter(ArrayList<GameSession> gameSessions, String usernameString) {
        this.gameSessions = gameSessions;
        this.usernameString = usernameString;
    }

    public GameSessionViewAdapter(ArrayList<GameSession> gameSessions) {
        this.gameSessions = gameSessions;
    }

    public ArrayList<GameSession> getGameSessions() {
        return gameSessions;
    }

    public void setUsernameString(String usernameString) {
        this.usernameString = usernameString;
    }

    @Override
    public int getCount() {
        return gameSessions.size();
    }

    @Override
    public Object getItem(int position) {
        return gameSessions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return gameSessions.get(position).getId();
    }

    // Returns a view containing a game session data (id, score, playtime) that is passed in with an index.
    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View sessionView = convertView;

        if (sessionView == null) {
            sessionView = View.inflate(parent.getContext(), R.layout.listview_play_session, null);
        }

        GameSession gameSession = (GameSession) getItem(position);
        ((TextView) sessionView.findViewById(R.id.tv_order)).setText(String.format("#%d - %s%s", gameSession.getId(), usernameString, (gameSession.getId() == getCount()) ? " (last attempt)" : ""));
        ((TextView) sessionView.findViewById(R.id.tv_score)).setText(String.format("Score: %d", gameSession.getScore()));
        ((TextView) sessionView.findViewById(R.id.tv_playtime)).setText(String.format("Duration: %d.%ds", gameSession.getPlaytimeMillisecond() / 1000, gameSession.getPlaytimeMillisecond() % 1000));

        return sessionView;
    }
}
