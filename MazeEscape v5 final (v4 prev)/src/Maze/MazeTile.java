package Maze;

import Agent.*;


public class MazeTile {
    private int x, y;
    private char type;
    private boolean hasAgent;
    private MazeManager maze;

    public MazeTile(int x, int y, char type, MazeManager maze) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.hasAgent = false;
        this.maze = maze;
    }
    public boolean gethasAgent() {
        return hasAgent;
    }

    public void applyEffect(Agent a) {
        switch (type) {
            case 'T':
                a.setTrapsTriggered(a.getTrapsTriggered() + 1);
                System.out.println("Agent " + a.getId() + " triggered a trap at (" + x + "," + y + ")!");
                a.backtrack(2);
                break;
            case 'P':
                a.setHasPowerUp(true);
                System.out.println("Agent " + a.getId() + " collected a teleport power-up at (" + x + "," + y + ")");
                type = 'E';
                break;
            case 'G':
                a.setHasReachedGoal(true);
                System.out.println("Agent " + a.getId() + " reached the goal at (" + x + "," + y + ")!");
                hasAgent = false; // Agent disappears from the goal tile
                break;
        }
    }

    public boolean isTraversable() {
        return type != 'W';
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public boolean hasAgent() {
        return hasAgent;
    }

    public void setHasAgent(boolean hasAgent) {
        this.hasAgent = hasAgent;
    }

    @Override
    public String toString() {
        if (hasAgent) {
            for (int i = 0; i < maze.getAgentList().size(); i++) {
                Agent a = maze.getAgentList().get(i);
                if (a.getCurrentX() == x && a.getCurrentY() == y && !a.isFinished()) {
                    return "A" + a.getId();
                }
            }
        }
        return String.valueOf(type);
    }


}