/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mysterychess.model;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.DateFormatter;
import mysterychess.network.dto.PieceDto;
import mysterychess.network.dto.TableDto;

/**
 *
 * @author Bui Huy Tin
 */
public class GameTracker {

    private String name;
    private List<MatchState> states = new ArrayList<MatchState>();

    public GameTracker() {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-hhmmss");
        name = "Match-" + f.format(new Date()) + ".mchess";
    }

    public String getName() {
        return name;
    }

    public List<MatchState> getStates() {
        return states;
    }

    public void addState(MatchState state) {
        states.add(state);
        saveGame();
    }
    
    private static final String DEFAULT_BASE_DIRECTORY = System.getProperty("user.dir");

    private void saveGame() {
        try {
            File f = new File(DEFAULT_BASE_DIRECTORY, name);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fo);
            os.writeObject(states);
            os.flush();
            os.close();
        } catch (Exception e) {
            Logger.getLogger(Match.class.getName()).severe("Fail to save file");
        }
    }

    public static class MatchState  implements Serializable {
        private static final long serialVersionUID = 1;
        private TableDto table;
        private PieceDto[] capturedPieces;
        private PieceDto[] lostPieces;

        public PieceDto[] getCapturedPieces() {
            return capturedPieces;
        }

        public PieceDto[] getLostPieces() {
            return lostPieces;
        }
        private Point lastMoveTo;
        private Point lastMoveFrom;

        public MatchState(TableDto table, 
                PieceDto[] capturedPieces,
                PieceDto[] lostPieces, 
                Point lastMoveFrom, Point lastMoveTo) {
            this.table = table;
            this.capturedPieces = capturedPieces;
            this.lostPieces = lostPieces;
            this.lastMoveFrom = lastMoveFrom;
            this.lastMoveTo = lastMoveTo;
        }

        public TableDto getTable() {
            return table;
        }

        public Point getLastMoveFrom() {
            return lastMoveFrom;
        }
        
        public Point getLastMoveTo() {
            return lastMoveTo;
        }
    }
}
