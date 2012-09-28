package mysterychess.model;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mysterychess.util.Util;

/**
 *
 * @author Tin Bui-Huy
 */
public abstract class ChessTimer2 {

    public static class Time {
        private int minute;
        private int second;

        public Time(int minute, int second) {
            this.minute = minute;
            this.second = second;
        }
        
        public Time(long milliSeconds) {
            long s = milliSeconds / 1000;
            long m = s / 60;
            s = s % 60;
            
            minute = (int) m;
            second = (int) s;
        }
        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public int getSecond() {
            return second;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + this.minute;
            hash = 47 * hash + this.second;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Time other = (Time) obj;
            if (minute == other.minute && 
                    second == other.second) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Time{" + "minute=" + minute + ", second=" + second + '}';
        }
        
    }
    
    private long gameSpentTime;
    private long pieceMoveExpiredTime;
//    private boolean gameStarted = false;
    private boolean running = false;
    private boolean stop = false;
    private boolean paused = false;
    private long pausedTime = 0;
//    private long gameTimeLeft;
    private long pieceMoveTimeLeft = Util.PIECE_MOVE_EXPIRE_TIME;

//    public void startGame() {
//        gameStarted = true;
//    }

    public void reset() {
//        gameStarted = false;
        running = false;
        stop = false;
        gameSpentTime = 0;
        pieceMoveTimeLeft = Util.PIECE_MOVE_EXPIRE_TIME;
        onTimeChange(false, pieceMoveTimeLeft, Util.GAME_EXPIRE_TIME);
    }

    public boolean isRunning() {
        return running;
    }

//    public boolean isGameStarted() {
//        return gameStarted;
//    }

    public void startMovePiece() {
        pieceMoveExpiredTime = System.currentTimeMillis()
                + Util.PIECE_MOVE_EXPIRE_TIME;
        Thread t = new Thread() {
            public void run(){
                running = true;
                while (true) {
                    try {
                        sleep(100);
                        // Collect time
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ChessTimer2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (paused) {
                        continue;
                    }
                    
                    long t = getPieceMoveTimeLeft();
                    Time tm = new Time(t);
                    if (!tm.equals(new Time(pieceMoveTimeLeft))) {
                        pieceMoveTimeLeft = t;
                        onTimeChange(true, pieceMoveTimeLeft, getGameTimeLeft());
                    }
                    
//                    // Check status
//                    if (!gameStarted) {
//                        break;
//                    }
                    if (stop) {
                        pieceMoveTimeLeft = getPieceMoveTimeLeft();
                                gameSpentTime = gameSpentTime
                                + Util.PIECE_MOVE_EXPIRE_TIME
                                - pieceMoveTimeLeft;
                        onTimeChange(false, pieceMoveTimeLeft, getGameTimeLeft());
                        stop = false;
                        break;
                    }
                }
                running =false;
            } 
        };
        
        t.start();
    }
    
    protected abstract void onTimeChange(boolean active, long pieceMoveTimeLeft, long gameTimeLeft);
    
    public void stopMovePiece() {
//        gameSpentTime = gameSpentTime
//                + Util.PIECE_MOVE_EXPIRE_TIME
//                - getPieceMoveTimeLeft();
        stop = true;
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
//        if (running) {
            return pieceMoveExpiredTime - System.currentTimeMillis();
//        }

//        return Util.PIECE_MOVE_EXPIRE_TIME;
    }
    
    public void pause() {
        paused = true;
        pausedTime = System.currentTimeMillis();
    }
    
    public void unpause() {
        paused = false;
        long pausePeriod = System.currentTimeMillis() - pausedTime; 
        pieceMoveExpiredTime += pausePeriod;
        pausedTime = 0;
        
    }
//    
//    /**
//     * Switch the status from paused to unpause and vice versa
//     * 
//     * @return the new status 
//     */
//    public boolean togglePauseStatus() {
//        if (paused) {
//            unpause();
//        } else {
//            pause();
//        }
//        
//        return paused;
//    }
//    
//    public boolean isPaused() {
//        return paused;
//    }
//
    @Override
    public String toString() {
        long g =  getGameTimeLeft();
        long p =  getPieceMoveTimeLeft();
        return format(p, g);
    }
    
    public String format(long pieceMoveTimeLeft, long gameTimeLeft) {
        long g =  gameTimeLeft;
        if (g < 0) {
            g = 0;
        }
        long p =  pieceMoveTimeLeft;
        if (p < 0) {
            p = 0;
        }
        return formatTime(g) + "        " + formatTime(p);
    }
    
}
