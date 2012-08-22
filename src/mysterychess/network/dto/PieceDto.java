/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mysterychess.network.dto;

import java.awt.Point;
import java.io.Serializable;
import mysterychess.model.Piece;
import mysterychess.model.PieceName;

/**
 *
 * @author Tin Bui-Huy
 */
public class PieceDto implements Serializable {

    /**
     * A version number for this class so that serialization can occur
     * without worrying about the underlying class changing between
     * serialization and deserialization.
     */
    private static final long serialVersionUID = 1;
    public Point pos;
    public boolean turnedUp = false;
    public PieceName currentType;
    public PieceName actualType;

    public static PieceDto toDto(Piece piece) {
        PieceDto p = new PieceDto();
        p.actualType = piece.getActualRole().getName();
        p.currentType = piece.getCurrentRole().getName();
        p.turnedUp = piece.isTurnedUp();
        p.pos = piece.getPosition();
        return p;
    }
}
