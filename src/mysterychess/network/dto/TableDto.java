package mysterychess.network.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import mysterychess.model.Advisor;
import mysterychess.model.Cannon;
import mysterychess.model.Chariot;
import mysterychess.model.ChessType;
import mysterychess.model.Elephant;
import mysterychess.model.General;
import mysterychess.model.Horse;
import mysterychess.model.Match;
import mysterychess.model.Piece;
import mysterychess.model.PieceName;
import mysterychess.model.Role;
import mysterychess.model.Soldier;
import mysterychess.model.SuperAdvisor;
import mysterychess.model.SuperElephant;
import mysterychess.model.Team;
import mysterychess.model.Team.TeamColor;
import mysterychess.model.Team.TeamPosition;
import mysterychess.util.Util;

/**
 *
 * @author Tin Bui-Huy
 */
public class TableDto extends DataDto implements Serializable {

    /**
     * A version number for this class so that serialization can occur
     * without worrying about the underlying class changing between
     * serialization and deserialization.
     */
    private static final long serialVersionUID = 1;
    public ChessType chessType;
    public PieceDto[] blackTeam = new PieceDto[16];
    public PieceDto[] whiteTeam = new PieceDto[16];
    public TeamColor myTeam;
    public TeamColor activeTeam;

    public static TableDto toDtoTable(Match match) {
        TableDto table = new TableDto();
//        if (TeamPosition.TOP == match.getActiveTeam().getPosition()) {
//            table.myTeam = TeamColor.WHITE;
//        } else {
//            table.myTeam = TeamColor.BLACK;
//        }
        
        // This is the my team in the perspective of the other player
        table.myTeam = match.getTeam(TeamPosition.TOP).getColor();

        table.blackTeam = getDtoTeam(match.getBlackTeam().getPieces());
        table.whiteTeam = getDtoTeam(match.getWhiteTeam().getPieces());
        table.chessType = match.getType();
        table.activeTeam = match.getActiveTeam().getColor();
        return table;
    }

    public static PieceDto[] getDtoTeam(List<Piece> pieces) {
        List<PieceDto> ps = new ArrayList<PieceDto>();
        for (int i = 0; i < pieces.size(); i++) {
            ps.add(i, PieceDto.toDto(pieces.get(i)));
        }
        return ps.toArray(new PieceDto[0]);
    }

    public Match toMatch() {
        Team black = createTeam(this.blackTeam, this.chessType);
        Team white = createTeam(this.whiteTeam, this.chessType);
        if (this.myTeam == Team.TeamColor.BLACK) {
            black.setPosition(Team.TeamPosition.BOTTOM);
            white.setPosition(Team.TeamPosition.TOP);
        } else {
            black.setPosition(Team.TeamPosition.TOP);
            white.setPosition(Team.TeamPosition.BOTTOM);
        }
        black.setColor(Team.TeamColor.BLACK);
        white.setColor(Team.TeamColor.WHITE);
        Match m = new Match();
        m.setData(this.chessType, white, black, m.getTeam(this.activeTeam));
        return m;
    }

    /**
     * Create team and also transform its position. 
     * 
     * @param ps
     * @param chessType
     * @return 
     */
    private Team createTeam(PieceDto[] ps, ChessType chessType) {
        Team team = new Team();
        List<Piece> pieces = new ArrayList<Piece>();
        boolean mystery = (chessType == ChessType.MYSTERY_CHESS) ? true : false;
        for (PieceDto p : ps) {
            Piece p1 = new Piece(
                    team,
                    Util.transform(p.pos),
                    createRole(p.currentType, false),
                    createRole(p.actualType, mystery),
                    p.turnedUp);
            pieces.add(p1);
        }
        team.setPieces(pieces);
        return team;
    }

    private Role createRole(PieceName name, Boolean mysteryRole) {
        Role r = null;
        switch (name) {
            case advisor:
                if (mysteryRole) {
                    r = new SuperAdvisor();
                } else {
                    r = new Advisor();
                }
                break;
            case cannon:
                r = new Cannon();
                break;
            case chariot:
                r = new Chariot();
                break;
            case elephant:
                if (mysteryRole) {
                    r = new SuperElephant();
                } else {
                    r = new Elephant();
                }
                break;
            case general:
                r = new General();
                break;
            case horse:
                r = new Horse();
                break;
            case soldier:
                r = new Soldier();
                break;
        }
        return r;
    }
}
