package Maze;

import Agent.*;
import Turns.*;
import java.util.Random;

public class MazeManager {
    private MazeTile[][] grid;
    private CircularLinkedList[] corridors;
    private AgentList agents;
    private int height, width;
    private Random random;
    private int goalX, goalY;

    public MazeManager(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new MazeTile[height][width];
        this.corridors = new CircularLinkedList[height];
        for (int i = 0; i < height; i++) {
            corridors[i] = new CircularLinkedList();
        }
        this.agents = new AgentList();
        this.random = new Random();
    }

    public void generateMaze() {
        // Step 1: Initialize grid with empty tiles
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = new MazeTile(i, j, 'E', this);
                corridors[i].add(grid[i][j]);
            }
        }

        // Step 2: Place the goal
        do {
            goalX = random.nextInt(height);
            goalY = random.nextInt(width);
        } while (goalX == 0 && goalY == 0); // Avoid (0,0) for goal
        grid[goalX][goalY] = new MazeTile(goalX, goalY, 'G', this);
        updateCorridorTile(goalX, goalY, grid[goalX][goalY]);

        // Step 3: Ensure (0,0) is empty
        grid[0][0] = new MazeTile(0, 0, 'E', this);
        updateCorridorTile(0, 0, grid[0][0]);

        // Step 4: Carve a path from (0,0) to goal to ensure solvability
        carvePathToGoal(0, 0, goalX, goalY);

        // Step 5: Add random walls, traps, and power-ups
        int totalTiles = width * height;
        int numWalls = (int) (totalTiles * 0.20);
        int numTraps = (int) (totalTiles * 0.15);
        int numPowerUps = (int) (totalTiles * 0.10);

        for (int i = 0; i < numWalls; i++) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (grid[x][y].getType() != 'E' || (x == 0 && y == 0) || (x == goalX && y == goalY));
            grid[x][y] = new MazeTile(x, y, 'W', this);
            updateCorridorTile(x, y, grid[x][y]);
        }

        for (int i = 0; i < numTraps; i++) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (grid[x][y].getType() != 'E' || (x == 0 && y == 0) || (x == goalX && y == goalY));
            grid[x][y] = new MazeTile(x, y, 'T', this);
            updateCorridorTile(x, y, grid[x][y]);
        }

        for (int i = 0; i < numPowerUps; i++) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (grid[x][y].getType() != 'E' || (x == 0 && y == 0) || (x == goalX && y == goalY));
            grid[x][y] = new MazeTile(x, y, 'P', this);
            updateCorridorTile(x, y, grid[x][y]);
        }

        // Step 6: Validate maze solvability
        if (findPathToGoal(0, 0).isEmpty()) {
            carvePathToGoal(0, 0, goalX, goalY);
        }

        // Step 7: Ensure goal tile is not overwritten
        if (grid[goalX][goalY].getType() != 'G') {
            System.out.println("Warning: Goal tile overwritten at (" + goalX + "," + goalY + "). Restoring.");
            grid[goalX][goalY] = new MazeTile(goalX, goalY, 'G', this);
            updateCorridorTile(goalX, goalY, grid[goalX][goalY]);
        }

        System.out.println("Generated maze size: " + grid.length + "x" + grid[0].length);
    }

    private void carvePathToGoal(int startX, int startY, int goalX, int goalY) {
        int currentX = startX;
        int currentY = startY;
        AuxLL<int[]> path = new AuxLL<>();
        path.add(new int[]{currentX, currentY});

        while (currentX != goalX || currentY != goalY) {
            AuxLL<int[]> possibleMoves = new AuxLL<>();
            if (currentX > 0 && currentX - 1 >= 0) possibleMoves.add(new int[]{currentX - 1, currentY}); // Up
            if (currentX < height - 1) possibleMoves.add(new int[]{currentX + 1, currentY}); // Down
            if (currentY > 0 && currentY - 1 >= 0) possibleMoves.add(new int[]{currentX, currentY - 1}); // Left
            if (currentY < width - 1) possibleMoves.add(new int[]{currentX, currentY + 1}); // Right

            if (possibleMoves.isEmpty()) break;

            int[] nextMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            currentX = nextMove[0];
            currentY = nextMove[1];

            if (grid[currentX][currentY].getType() == 'W' && !(currentX == goalX && currentY == goalY)) {
                grid[currentX][currentY] = new MazeTile(currentX, currentY, 'E', this);
                updateCorridorTile(currentX, currentY, grid[currentX][currentY]);
            }
            path.add(new int[]{currentX, currentY});
        }

        for (int i = 0; i < path.size(); i++) {
            int[] pos = path.get(i);
            int x = pos[0], y = pos[1];
            if (x == goalX && y == goalY) continue;
            if (grid[x][y].getType() == 'W') {
                grid[x][y] = new MazeTile(x, y, 'E', this);
                updateCorridorTile(x, y, grid[x][y]);
            }
        }
    }

    private AuxLL<int[]> findPathToGoal(int startX, int startY) {
        boolean[][] visited = new boolean[height][width];
        Queue<int[]> queue = new Queue<>();
        AuxLL<int[]> path = new AuxLL<>();
        queue.enqueue(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[][][] parent = new int[height][width][2];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                parent[i][j] = null;
            }
        }

        while (!queue.isEmpty()) {
            int[] current = queue.dequeue();
            int x = current[0], y = current[1];

            if (x == goalX && y == goalY) {
                int[] pos = current;
                while (pos != null && !(pos[0] == startX && pos[1] == startY)) {
                    path.add(pos);
                    int px = parent[pos[0]][pos[1]][0];
                    int py = parent[pos[0]][pos[1]][1];
                    pos = new int[]{px, py};
                    if (px == 0 && py == 0 && !(startX == 0 && startY == 0)) {
                        pos = null;
                    }
                }
                path.add(new int[]{startX, startY});
                AuxLL<int[]> reversedPath = new AuxLL<>();
                for (int i = path.size() - 1; i >= 0; i--) {
                    reversedPath.add(path.get(i));
                }
                return reversedPath;
            }

            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (newX >= 0 && newX < height && newY >= 0 && newY < width && !visited[newX][newY] && grid[newX][newY].isTraversable()) {
                    queue.enqueue(new int[]{newX, newY});
                    visited[newX][newY] = true;
                    parent[newX][newY] = new int[]{x, y};
                }
            }
        }
        return new AuxLL<>();
    }

    public void updateCorridorTile(int x, int y, MazeTile newTile) {
        CircularLinkedList.Node current = corridors[x].getHead();
        int pos = 0;
        while (pos < y) {
            current = current.next;
            pos++;
        }
        if (current != null) {
            current.tile = newTile;
        }
    }

    public void addAgent(Agent a) {
        agents.add(a);
        grid[a.getCurrentX()][a.getCurrentY()].setHasAgent(true);
        ensureValidStart(a.getCurrentX(), a.getCurrentY());
    }

    public void moveAgent(Agent a, String direction) {
        int oldX = a.getCurrentX();
        int oldY = a.getCurrentY();
        int newX = oldX;
        int newY = oldY;

        switch (direction.toUpperCase()) {
            case "UP": newX--; break;
            case "DOWN": newX++; break;
            case "LEFT": newY--; break;
            case "RIGHT": newY++; break;
            default:
                System.out.println("Invalid direction");
                return;
        }

        if (isValidMove(oldX, oldY, direction)) {
            a.setX(newX);
            a.setY(newY);
            updateAgentLocation(a, oldX, oldY);
            System.out.println("Agent " + a.getId() + " moved to (" + newX + "," + newY + ")");
            System.out.println("After Agent " + a.getId() + "'s move:");
            printMazeSnapshot();
            if (grid[goalX][goalY].getType() != 'G') {
                System.out.println("Error: Goal tile at (" + goalX + "," + goalY + ") changed to " + grid[goalX][goalY].getType());
            }
        } else {
            System.out.println("Invalid move: Tile is out of bounds, a wall, or occupied by another agent");
        }
    }

    public void updateAgentLocation(Agent a, int oldX, int oldY) {
        boolean otherAgentsAtOldPos = false;
        for (int i = 0; i < agents.size(); i++) {
            Agent other = agents.get(i);
            if (other != a && other.getCurrentX() == oldX && other.getCurrentY() == oldY && !other.isFinished()) {
                otherAgentsAtOldPos = true;
                break;
            }
        }
        if (!otherAgentsAtOldPos) {
            grid[oldX][oldY].setHasAgent(false);
        }
        // Only set hasAgent for non-goal tiles
        if (a.getCurrentX() != goalX || a.getCurrentY() != goalY) {
            grid[a.getCurrentX()][a.getCurrentY()].setHasAgent(true);
        }
    }

    public void rotateCorridor(int rowId) {
        if (rowId < 0 || rowId >= height) {
            System.out.println("Invalid row " + rowId + " for rotation");
            return;
        }
        System.out.println("Before rotating corridor row " + rowId + ":");
        printMazeSnapshot();
        corridors[rowId].rotate();
        for (int j = 0; j < width; j++) {
            grid[rowId][j].setHasAgent(false);
        }
        CircularLinkedList.Node current = corridors[rowId].getHead();
        for (int j = 0; j < width; j++) {
            grid[rowId][j] = current.tile;
            current = current.next;
        }
        boolean agentsMoved = false;
        for (int i = 0; i < agents.size(); i++) {
            Agent a = agents.get(i);
            if (a.getCurrentX() == rowId && !a.isFinished()) {
                int oldY = a.getCurrentY();
                int newY = (oldY - 1 + width) % width;
                a.setY(newY);
                if (rowId != goalX || newY != goalY) {
                    grid[rowId][newY].setHasAgent(true);
                }
                System.out.println("Agent " + a.getId() + " moved from (" + rowId + "," + oldY + ") to (" + rowId + "," + newY + ") due to corridor rotation");
                agentsMoved = true;
            }
        }
        System.out.println("Agent positions after rotation:");
        for (int i = 0; i < agents.size(); i++) {
            Agent a = agents.get(i);
            if (!a.isFinished()) {
                System.out.println("Agent " + a.getId() + " at (" + a.getCurrentX() + "," + a.getCurrentY() + ")");
            }
        }
        System.out.println("Tiles with hasAgent=true:");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (grid[i][j].gethasAgent()) {
                    System.out.println("Tile (" + i + "," + j + ") hasAgent=true");
                }
            }
        }
        System.out.println("After rotating corridor row " + rowId + ":");
        printMazeSnapshot();
        System.out.println("Verifying toString for hasAgent tiles:");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (grid[i][j].gethasAgent()) {
                    System.out.println("Tile (" + i + "," + j + ") toString: " + grid[i][j].toString());
                }
            }
        }
        if (!agentsMoved) {
            System.out.println("No agents were in row " + rowId + "; only tiles rotated.");
        }
    }

    public void applyEffects(Agent a) {
        MazeTile tile = grid[a.getCurrentX()][a.getCurrentY()];
        tile.applyEffect(a);
    }

    public MazeTile getTile(int x, int y) {
        if (x >= 0 && x < height && y >= 0 && y < width) {
            return grid[x][y];
        }
        return null;
    }

    public boolean isValidMove(int x, int y, String direction) {
        int newX = x;
        int newY = y;
        switch (direction.toUpperCase()) {
            case "UP": newX--; break;
            case "DOWN": newX++; break;
            case "LEFT": newY--; break;
            case "RIGHT": newY++; break;
            default: return false;
        }
        MazeTile tile = getTile(newX, newY);
        return tile != null && tile.isTraversable() && (tile.getType() == 'G' || !tile.hasAgent());
    }

    public int[] findRandomValidPosition() {
        int x, y;
        int attempts = 0;
        do {
            x = random.nextInt(height);
            y = random.nextInt(width);
            attempts++;
            if (attempts > 100) return null;
        } while (grid[x][y] == null || grid[x][y].getType() == 'W' || grid[x][y].hasAgent() || (x == goalX && y == goalY));
        int[] pos = new int[]{x, y};
        ensureValidStart(x, y);
        return pos;
    }

    private void ensureValidStart(int x, int y) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        boolean hasValidMove = false;
        for (int[] dir : directions) {
            int newX = x + dir[0], newY = y + dir[1];
            MazeTile tile = getTile(newX, newY);
            if (tile != null && tile.isTraversable() && (tile.getType() == 'E' || tile.getType() == 'P' || tile.getType() == 'G')) {
                hasValidMove = true;
                break;
            }
        }
        if (!hasValidMove) {
            AuxLL<int[]> possibleDirs = new AuxLL<>();
            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (newX >= 0 && newX < height && newY >= 0 && newY < width && grid[newX][newY].getType() == 'W') {
                    possibleDirs.add(new int[]{newX, newY});
                }
            }
            if (!possibleDirs.isEmpty()) {
                int[] chosen = possibleDirs.get(random.nextInt(possibleDirs.size()));
                grid[chosen[0]][chosen[1]] = new MazeTile(chosen[0], chosen[1], 'E', this);
                updateCorridorTile(chosen[0], chosen[1], grid[chosen[0]][chosen[1]]);
            } else {
                for (int[] dir : directions) {
                    int newX = x + dir[0], newY = y + dir[1];
                    if (newX >= 0 && newX < height && newY >= 0 && newY < width && grid[newX][newY].getType() != 'G') {
                        grid[newX][newY] = new MazeTile(newX, newY, 'E', this);
                        updateCorridorTile(newX, newY, grid[newX][newY]);
                        break;
                    }
                }
            }
        }
    }

    public void printMazeSnapshot() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public AgentList getAgentList() {
        return agents;
    }

    public int getHeight() {
        return height;
    }
    public int getWidth()
    {
        return width;
    }
    public int getGoalX()
    {
        return goalX;
    }
    public int getGoalY()
    {
        return goalY;
    }
}