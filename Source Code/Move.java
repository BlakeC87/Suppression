package firestormsoftwarestudio.suppression;

import java.util.Random;

/**
 * Created by Blake on 12/5/2015.
 */
public class Move {
    private Space startingSpace;
    private Space endingSpace;
    private int score;

    public Move() {
        startingSpace = null;
        endingSpace = null;
        score = 0;
    }

    public void resetMove() {
        this.startingSpace = null;
        this.endingSpace = null;
        this.score = 0;
    }

    public void addStart(Space start) {
        this.startingSpace = start;
    }

    public void addEnd(Space end) {
        this.endingSpace = end;
    }

    public Move (Space start, Space end) {
        startingSpace = start;
        endingSpace = end;
        score = 0;
    }

    public void calculateScore(Space space, boolean isPlayer, int aiSetting) {
        int score;
        int gained = 0;
        int saved = 0;
        int lost = 0;
        int losingRow;
        int losingCol;
        int row;
        int col;
        int startRow = this.getStartingSpace().getRow();
        int startCol = this.getStartingSpace().getCol();

        for (Location losingLoc : this.getStartingSpace().getAdjacent()) {
            losingRow = losingLoc.getRow();
            losingCol = losingLoc.getCol();
            if (isPlayer) {
                if (SuppressionToDo.spaces[losingRow][losingCol].isClaimed()
                        && SuppressionToDo.spaces[losingRow][losingCol].isClaimedByPlayer()) {
                    lost += 1;
                }
            }
            else {
                if (SuppressionToDo.spaces[losingRow][losingCol].isClaimed()) {
                    lost += 1;
                }
            }

        }

        Space tempSpace;
        boolean isCopy = false;
        for (Location loc : space.getAdjacent()) {
            row = loc.getRow();
            col = loc.getCol();
            if (startRow == row && startCol == col) {
                isCopy = true;
            }
            tempSpace = SuppressionToDo.spaces[row][col];

            if (isPlayer) {
                if (tempSpace.isClaimed() && !tempSpace.isClaimedByPlayer()) {
                    gained += 1;
                }
                else if (tempSpace.isClaimedByPlayer()) {
                    saved += 1;
                }
            }
            else {
                if (tempSpace.isClaimedByPlayer()) {
                    gained += 1;
                }
                else if (tempSpace.isClaimed()) {
                    saved += 1;
                }
            }
        }

        // default
        if (aiSetting == 1) {
            score = (saved * 3) + (gained * 7);

            if (!isCopy) {
                score -= (lost * 4);
            }
        }
        // aggressive
        else if (aiSetting == 2) {
            score = (saved * 2) + (gained * 8);

            if (!isCopy) {
                score -= (lost * 3);
            }
        }
        // cautious
        else if (aiSetting == 0){
            score = (saved * 3) + (gained * 6);

            if (!isCopy) {
                score -= (lost * 6);
            }
        }
        // reckless
        else if (aiSetting == 3) {
            score = (saved * 1) + (gained * 8);

            if (!isCopy) {
                score -= (lost * 1);
            }
        }
        // pacifist
        else if (aiSetting == 4) {
            score = (saved * 6) + (gained * 1);

            if (!isCopy) {
                score -= (lost * 4);
            }
        }
        // confused
        else {
            Random rand = new Random();
            score = (saved * rand.nextInt(6)) + (gained * rand.nextInt(6));

            if (isCopy) {
                score -=(lost * rand.nextInt(2));
            }

        }

        this.score = score;
    }

    public Space getStartingSpace() {
        return startingSpace;
    }

    public Space getEndingSpace() {
        return endingSpace;
    }

    public int getScore() {
        return score;
    }
}