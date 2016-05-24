package firestormsoftwarestudio.suppression;

import java.util.ArrayList;

/**
 * Created by bcoffelt on 11/3/2015.
 */
public class Space {
    private boolean isUnavailable;
    private boolean isClaimed;
    private boolean isClaimedByPlayer;
    private boolean canCopy;
    private boolean canMove;
    private boolean isSelected;

    private int row;
    private int col;

    private ArrayList<Location> adjacent;
    private ArrayList<Location> movable;

    public Space(int rowNum, int colNum) {
        row = rowNum;
        col = colNum;
        isUnavailable = false;
        isClaimed = false;
        isClaimedByPlayer = false;
        canCopy = false;
        canMove = false;
        isSelected = false;
        adjacent = fillLocations(1);
        movable = fillLocations(2);
    }

    public int getCol() {
        return col;
    }

    public boolean isUnavailable() {
        return isUnavailable;
    }

    public boolean isClaimed() {
        return isClaimed;
    }

    public boolean isClaimedByPlayer() {
        return isClaimedByPlayer;
    }

    public int getRow() {
        return row;
    }

    public boolean isSelected() { return isSelected; }

    public void setIsClaimedByPlayer(boolean isClaimedByPlayer) {
        this.isClaimedByPlayer = isClaimedByPlayer;
    }

    public void setIsClaimed(boolean isClaimed) {
        this.isClaimed = isClaimed;
    }

    public void setIsUnavailable(boolean isUnavailable) {
        this.isUnavailable = isUnavailable;
    }

    public void setIsSelected(boolean isSelected) { this.isSelected = isSelected; }

    public boolean canMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean canCopy() {
        return canCopy;
    }

    public void setCanCopy(boolean canCopy) {
        this.canCopy = canCopy;
    }

    public ArrayList<Location> getAdjacent() {
        return adjacent;
    }

    public ArrayList<Location> getMovable() {
        return movable;
    }

    public void deselect() {
        this.canCopy = false;
        this.canMove = false;
        this.isSelected = false;
    }

    private int[] getMoveBounds(int distance) {
        int minY;
        int minX;
        int maxY;
        int maxX;
        int[] bounds = new int[4];

        minY = row - distance;
        maxY = row + distance;
        minX = col - distance;
        maxX = col + distance;

        bounds[0] = minY;
        bounds[1] = maxY;
        bounds[2] = minX;
        bounds[3] = maxX;

        return bounds;
    }

    private ArrayList<Location> fillLocations(int distance) {
        ArrayList<Location> locations = new ArrayList<Location>();
        int[] bounds = getMoveBounds(distance);
        for (int y = bounds[0]; y <= bounds[1]; y++) {
            for (int x = bounds[2]; x <= bounds[3]; x++) {
                if (y >= 0 && y <= 7 && x >= 0 && x <= 7) {
                    if (y >= this.row - 1 && y <= this.row + 1
                            && x >= this.col - 1 && x <= this.col + 1) {
                        if (y == this.row && x == this.col) {
                            //DO NOT ADD
                        }
                        else {
                            locations.add(new Location(y, x, true));
                        }
                    }
                    else {
                        locations.add(new Location(y, x, false));
                    }
                }
            }
        }
        return locations;
    }

}

