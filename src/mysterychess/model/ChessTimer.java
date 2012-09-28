package mysterychess.model;

import mysterychess.util.Util;

/**
 *
 * @author Tin Bui-Huy
 */
public class ChessTimer {

    private long gameSpentTime;
    private long pieceMoveExpiredTime;
    private boolean gameStarted = false;
    private boolean running = false;
    private boolean paused;
    private long pauseTime;

    public void startGame() {
        gameStarted = true;
    }

    public void reset() {
        gameStarted = false;
        running = false;
        gameSpentTime = 0;
        paused = false;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void startMovePiece() {
        pieceMoveExpiredTime = System.currentTimeMillis()
                + Util.PIECE_MOVE_EXPIRE_TIME;
        running = true;
    }

    public void stopMovePiece() {
        gameSpentTime = gameSpentTime
                + Util.PIECE_MOVE_EXPIRE_TIME
                - getPieceMoveTimeLeft();
        running = false;
    }

    public synchronized void pause() {
        if (!running) {
            return;
        }
        paused = true;
        pauseTime = System.currentTimeMillis();
    }
    
    public synchronized void unpause() {
        if (!running) {
            return;
        }
        long pausePeriod = System.currentTimeMillis() - pauseTime;
        pieceMoveExpiredTime += pausePeriod; 
        paused = false;
    }
    
    private String formatTime(long t) {
        long second = t / 1000;
        long minutes = second / 60;
        second = second % 60;
        return minutes + ":" + second;
    }

    public long getGameTimeLeft() {
        return Util.GAME_EXPIRE_TIME
                - gameSpentTime
                - Util.PIECE_MOVE_EXPIRE_TIME
                + getPieceMoveTimeLeft();
    }

    public long getPieceMoveTimeLeft() {
        if (running) {
            long pausePeriod = 0;
            if (paused) {
                pausePeriod = System.currentTimeMillis() - pauseTime;
            }
            return pieceMoveExpiredTime - System.currentTimeMillis() + pausePeriod;
        }

        return Util.PIECE_MOVE_EXPIRE_TIME;
    }

    public String toString() {
        long g =  getGameTimeLeft();
        if (g < 0) {
            g = 0;
        }
        long p =  getPieceMoveTimeLeft();
        if (p < 0) {
            p = 0;
        }
        return formatTime(g) + "        " + formatTime(p);
    }
}