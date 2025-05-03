package Agent;

import Maze.MazeManager;
import java.util.ArrayList;
import java.util.Scanner;

public class Agent {
    private int id;
    private int currentX, currentY;
    private StringStack moveHistory;
    private int totalMoves;
    private int backtracks;
    private int trapsTriggered;
    private int powerUpsUsed;
    private int maxStackDepth;
    private boolean hasPowerUp;
    private boolean hasReachedGoal;
    private boolean isStuck;
    private MazeManager maze;

    //constructor
    public Agent(int id, int startX, int startY, MazeManager maze) {
        this.id = id;
        this.currentX = startX;
        this.currentY = startY;
        this.moveHistory = new StringStack();
        this.hasReachedGoal = false;
        this.totalMoves = 0;
        this.backtracks = 0;
        this.trapsTriggered = 0;
        this.powerUpsUsed = 0;
        this.maxStackDepth = 0;
        this.hasPowerUp = false;
        this.isStuck = false;
        this.maze = maze;
        recordMove(startX, startY);
    }
    public int getId() { return id; }
    public int getCurrentX() { return currentX; }
    public int getCurrentY() { return currentY; }
    public boolean hasReachedGoal() { return hasReachedGoal; }
    public int getTotalMoves() { return totalMoves; }
    public int getBacktracks() { return backtracks; }
    public int getTrapsTriggered() { return trapsTriggered; }
    public void setTrapsTriggered(int trapsTriggered){this.trapsTriggered=trapsTriggered;}
    public void incrementTrapsTriggered() { trapsTriggered++; }
    public int getPowerUpsUsed() { return powerUpsUsed; }
    public int getMaxStackDepth() { return maxStackDepth; }
    public boolean hasPowerUp() { return hasPowerUp; }
    public void setHasPowerUp(boolean hasPowerUp) { this.hasPowerUp = hasPowerUp; }
    public void setHasReachedGoal(boolean hasReachedGoal) { this.hasReachedGoal = hasReachedGoal; }
    public boolean isFinished() { return hasReachedGoal || isStuck; }
    public void setX(int x) { this.currentX = x; }
    public void setY(int y) { this.currentY = y; }
    public boolean isStuck() { return isStuck; }
    public MazeManager getMazeManager() {
        return maze;
    }
    public int getMoveHistorySize() {
        return moveHistory.size();
    }

    //applying valid dir
    public void move(String direction) {
        int newX = currentX;
        int newY = currentY;

        switch (direction.toUpperCase()) {
            case "UP": newX--; break;
            case "DOWN": newX++; break;
            case "LEFT": newY--; break;
            case "RIGHT": newY++; break;
            default:
                System.out.println("Invalid direction");
                return;
        }

        if (isMoveValid(newX, newY)) {
            maze.moveAgent(this, direction);
            recordMove(newX, newY);
            totalMoves++;
        } else {
            System.out.println("Move to (" + newX + "," + newY + ") is invalid");
        }
    }
    //cheking the validty of the move
    private boolean isMoveValid(int x, int y) {
        return maze.getTile(x, y) != null && maze.getTile(x, y).isTraversable();
    }

    public void backtrack(int steps) {
        System.out.println("Agent " + id + " backtracking " + steps + " step(s) from (" + currentX + "," + currentY + ")");
        System.out.println("Move history before backtrack: " + getMoveHistoryAsString());

        // Adjust steps to available move history
        if (moveHistory.size() < steps) {
            steps = moveHistory.size();
        }

        if (steps == 0) {
            System.out.println("Agent " + id + " has no move history, finding new position");
            int[] newPos = maze.findRandomValidPosition();
            if (newPos != null) {
                int newX = newPos[0];
                int newY = newPos[1];
                String direction = currentX == newX ? (currentY < newY ? "RIGHT" : "LEFT") : (currentX < newX ? "DOWN" : "UP");
                if (isMoveValid(newX, newY)) {
                    maze.moveAgent(this, direction);
                    currentX = newX;
                    currentY = newY;
                    recordMove(newX, newY);
                    backtracks++;
                    System.out.println("Agent " + id + " backtracked to (" + newX + "," + newY + ")");
                } else {
                    System.out.println("Random position (" + newX + "," + newY + ") is invalid");
                }
            } else {
                isStuck = true;
                System.out.println("Agent " + id + " is stuck: no valid positions available");
            }
            return;
        }
        ArrayList<String> poppedPositions = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            if (!moveHistory.isEmpty()) {
                String position = moveHistory.pop();
                poppedPositions.add(position);
                System.out.println("Popped position " + i + ": " + position);
            }
        }

        // Select the position from 'steps' moves ago (top of stack after popping)
        String targetPosition = moveHistory.isEmpty() ? poppedPositions.get(poppedPositions.size() - 1) : moveHistory.peek();
        System.out.println("Move history after popping: " + getMoveHistoryAsString());
        System.out.println("Target position: " + targetPosition);

        // Move to the target position
        if (targetPosition != null) {
            String[] position = targetPosition.split(",");
            int newX = Integer.parseInt(position[0]);
            int newY = Integer.parseInt(position[1]);

            // Skip move if target is current position
            if (newX == currentX && newY == currentY) {
                System.out.println("Agent " + id + " is already at target position (" + currentX + "," + currentY + ")");
                backtracks++;
                return;
            }

            // Validate target position
            if (!isMoveValid(newX, newY)) {
                System.out.println("Target position (" + newX + "," + newY + ") is invalid: out of bounds, a wall, or occupied");
                backtracks++;
                return;
            }

            // Calculate path to target (chain moves)
            int tempX = currentX;
            int tempY = currentY;
            boolean moveSuccess = true;

            // Move vertically (row changes)
            while (tempX != newX && moveSuccess) {
                String direction = tempX < newX ? "DOWN" : "UP";
                int nextX = tempX + (tempX < newX ? 1 : -1);
                System.out.println("Attempting vertical move to (" + nextX + "," + tempY + ") with direction: " + direction);
                if (isMoveValid(nextX, tempY)) {
                    maze.moveAgent(this, direction);
                    tempX = nextX;
                    currentX = tempX; // Update agent's position
                } else {
                    System.out.println("Vertical move to (" + nextX + "," + tempY + ") is invalid");
                    moveSuccess = false;
                }
            }

            // Move horizontally (column changes)
            while (tempY != newY && moveSuccess) {
                String direction = tempY < newY ? "RIGHT" : "LEFT";
                int nextY = tempY + (tempY < newY ? 1 : -1);
                System.out.println("Attempting horizontal move to (" + tempX + "," + nextY + ") with direction: " + direction);
                if (isMoveValid(tempX, nextY)) {
                    maze.moveAgent(this, direction);
                    tempY = nextY;
                    currentY = tempY; // Update agent's position
                } else {
                    System.out.println("Horizontal move to (" + tempX + "," + nextY + ") is invalid");
                    moveSuccess = false;
                }
            }

            if (moveSuccess) {
                backtracks++;
                System.out.println("Agent " + id + " backtracked to (" + currentX + "," + currentY + ")");
            } else {
                System.out.println("Failed to backtrack to (" + newX + "," + newY + "): one or more moves invalid");
            }

            // Check if agent is stuck
            boolean hasValidMove = isMoveValid(currentX - 1, currentY) || isMoveValid(currentX + 1, currentY) ||
                    isMoveValid(currentX, currentY - 1) || isMoveValid(currentX, currentY + 1);
            if (!hasValidMove) {
                isStuck = true;
                System.out.println("Agent " + id + " is stuck at (" + currentX + "," + currentY + "): no valid moves");
            } else {
                isStuck = false;
            }
        } else {
            System.out.println("No valid target position for backtrack");
        }
    }

    // Default backtrack for manual action (1 step)
    public void backtrack() {
        backtrack(1);
    }

    public void applyPowerUp() {
        if (hasPowerUp) {
            System.out.println("Power-up applied by Agent " + id);
            powerUpsUsed++;
            hasPowerUp = false;
            int[] newPos = maze.findRandomValidPosition(); // Get a random spot
            if (newPos != null) {
                int oldX = currentX; // Save old position
                int oldY = currentY;
                int newX = newPos[0]; // New position
                int newY = newPos[1];
                if (isMoveValid(newX, newY)) { // Check if the new spot is valid
                    // Clear the old position if no other agents are there
                    boolean otherAgentsAtOldPos = false;
                    AgentList agents = maze.getAgentList();
                    for (int i = 0; i < agents.size(); i++) {
                        Agent other = agents.get(i);
                        if (other != this && other.getCurrentX() == oldX && other.getCurrentY() == oldY && !other.isFinished()) {
                            otherAgentsAtOldPos = true;
                            break;
                        }
                    }
                    if (!otherAgentsAtOldPos) {
                        maze.getTile(oldX, oldY).setHasAgent(false);
                    }
                    // Update to the new position
                    currentX = newX;
                    currentY = newY;
                    // Set hasAgent at new position (skip if it’s the goal, if that’s your rule)
                    if (newX != maze.getGoalX() || newY != maze.getGoalY()) {
                        maze.getTile(newX, newY).setHasAgent(true);
                    }
                    recordMove(newX, newY); // Log the move
                    System.out.println("Agent " + id + " teleported to (" + newX + "," + newY + ")");
                    maze.printMazeSnapshot(); // Show the updated maze
                } else {
                    System.out.println("Random position (" + newX + "," + newY + ") is invalid");
                }
            } else {
                System.out.println("No valid position found for teleport");
            }
        } else {
            System.out.println("No power-up for Agent " + id);
        }
    }

    private void recordMove(int x, int y) {
        moveHistory.push(x + "," + y);
        maxStackDepth = Math.max(maxStackDepth, moveHistory.size());
    }

    public String getLastFiveMoves() {
        StringBuilder sb = new StringBuilder();
        StringStack temp = new StringStack();
        int count = 0;
        while (!moveHistory.isEmpty() && count < 5) {
            String move = moveHistory.pop();
            temp.push(move);
            sb.append(move).append(" ");
            count++;
        }
        while (!temp.isEmpty()) {
            moveHistory.push(temp.pop());
        }
        return sb.toString().trim();
    }

    public String getMoveHistoryAsString() {
        return getLastFiveMoves();
    }



    public String takeAction(MazeManager maze) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Agent " + id + " → move (UP/DOWN/LEFT/RIGHT), BACKTRACK, or USEPOWERUP: ");
        String cmd = "";
        try {
            cmd = scanner.nextLine().trim().toUpperCase();
        } catch (Exception e) {
            return "Skipped turn (input error)";
        }
        if (cmd.isEmpty()) {
            return "Skipped turn (empty input)";
        }
        if ("BACKTRACK".equals(cmd)) {
            backtrack(1);
            return "Backtracked 1 step";
        } else if ("USEPOWERUP".equals(cmd)) {
            if (hasPowerUp) {
                applyPowerUp();
                return "Used power-up and teleported to (" + currentX + "," + currentY + ")";
            } else {
                applyPowerUp(); // Still call to log "No power-up available"
                return "No power-up available";
            }
        } else if (maze.isValidMove(currentX, currentY, cmd)) {
            move(cmd);
            return "Moved " + cmd + " to (" + currentX + "," + currentY + ")";
        } else {
            return "Invalid move or command";
        }
    }
}