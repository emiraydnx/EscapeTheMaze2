package Controlls;

import Agent.*;
import Maze.*;
import Turns.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GameController {
    private MazeManager maze;
    private TurnManager turns;
    private int maxTurns;
    private int turnCount;
    private AuxLL<Integer> goalOrder; // Track order of agents reaching the goal

    public GameController(MazeManager maze, int numAgents, int maxTurns) {
        this.maze = maze;
        this.turns = new TurnManager();
        this.maxTurns = maxTurns;
        this.turnCount = 0;
        this.goalOrder = new AuxLL<>();
        initializeGame(numAgents);
    }

    public void initializeGame(int numAgents) {
        for (int i = 0; i < numAgents; i++) {
            int[] pos = maze.findRandomValidPosition();
            if (pos == null) {
                System.out.println("Could not find valid position for Agent " + i);
                continue;
            }
            Agent agent = new Agent(i, pos[0], pos[1], maze);
            maze.addAgent(agent);
            turns.addAgent(agent);
            System.out.println("Agent " + i + " initialized at (" + pos[0] + "," + pos[1] + ")");
        }
    }

    public void runSimulation() {
        System.out.println("Starting Maze Escape Simulation...");
        turnCount = 0;
        while (turnCount < maxTurns && !turns.isQueueEmpty() && !allAgentsFinished()) {
            turnCount++;
            System.out.println("\n=== Round " + turnCount + " ===");
            System.out.println("Maze Snapshot:");
            maze.printMazeSnapshot();
            System.out.println("Current Turn Order: " + getTurnOrderString());

            Queue turnOrder = turns.getAgentQueue();
            Queue nextTurnOrder = new Queue(); // Temporary queue for next turn
            while (!turnOrder.isEmpty()) {
                Agent agent = (Agent) turnOrder.dequeue();
                if (!agent.isFinished()) {
                    String action = processAgentAction(agent);
                    System.out.println("Agent " + agent.getId() + " action: " + action);
                    MazeTile tile = maze.getTile(agent.getCurrentX(), agent.getCurrentY());
                    checkTileEffect(agent, tile);
                    System.out.println("Agent " + agent.getId() + " last 5 moves: " + getLastFiveMoves(agent));
                    if (agent.hasReachedGoal() && !goalOrder.contains(agent.getId())) {
                        goalOrder.add(agent.getId());
                    }
                    if (!agent.isFinished()) {
                        nextTurnOrder.enqueue(agent); // Queue for next turn
                    } else {
                        System.out.println("Agent " + agent.getId() + " is finished!");
                    }
                } else {
                    System.out.println("Agent " + agent.getId() + " is finished, skipping turn.");
                }
            }

            // Repopulate agentQueue for next turn
            while (!nextTurnOrder.isEmpty()) {
                turns.addAgent((Agent) nextTurnOrder.dequeue());
            }
            // Debug queue state
            System.out.println("Queue empty: " + turns.isQueueEmpty() + ", All finished: " + allAgentsFinished());
            // Rotate corridor
            Random random = new Random();
            int rowToRotate = random.nextInt(maze.getHeight());
            maze.rotateCorridor(rowToRotate);
        }

        // Final output
        System.out.println("\nFinal Maze State:");
        maze.printMazeSnapshot();
        if (turnCount >= maxTurns) {
            System.out.println("Game Over: Maximum turns reached!");
        } else {
            System.out.println("Game Over: All agents are finished or queue is empty!");
        }
        printFinalStatistics();
        logGameSummaryToFile("game_summary.txt");
    }

    private String getTurnOrderString() {
        StringBuilder sb = new StringBuilder();
        Queue tempQueue = new Queue();
        while (!turns.getAgentQueue().isEmpty()) {
            Agent a = (Agent) turns.getAgentQueue().dequeue();
            sb.append("A").append(a.getId()).append(" ");
            tempQueue.enqueue(a);
        }
        while (!tempQueue.isEmpty()) {
            turns.getAgentQueue().enqueue(tempQueue.dequeue());
        }
        return sb.length() > 0 ? sb.toString().trim() : "None";
    }

    private String getLastFiveMoves(Agent agent) {
        String moves = agent.getLastFiveMoves(); // Use Agent's existing method
        return moves.length() > 0 ? moves : "None";
    }

    public String processAgentAction(Agent a) {
        return a.takeAction(maze); // Assumes takeAction returns a string
    }

    public void checkTileEffect(Agent a, MazeTile tile) {
        tile.applyEffect(a); // Event messages are logged in MazeTile.applyEffect
    }

    public void printFinalStatistics() {
        System.out.println("\n=== Final Statistics ===");
        System.out.printf("%-10s %-15s %-10s %-10s %-10s %-15s %-15s\n",
                "Agent", "Reached Goal", "Moves", "Backtracks", "Traps", "Power-ups", "Max Depth");
        AgentList agents = maze.getAgentList();
        int totalMoves = 0;
        for (int i = 0; i < agents.size(); i++) {
            Agent a = agents.get(i);
            totalMoves += a.getTotalMoves();
            System.out.printf("%-10s %-15s %-10d %-10d %-10d %-15d %-15d\n",
                    "A" + a.getId(),
                    a.hasReachedGoal() ? "Yes" : "No",
                    a.getTotalMoves(),
                    a.getBacktracks(),
                    a.getTrapsTriggered(),
                    a.getPowerUpsUsed(),
                    a.getMaxStackDepth());
        }
        double avgMoves = agents.size() > 0 ? (double) totalMoves / agents.size() : 0;
        System.out.println("\nTotal Turns Executed: " + turnCount);
        System.out.printf("Average Moves per Agent: %.2f\n", avgMoves);
        System.out.println("Winner: " + (goalOrder.size() > 0 ? "A" + goalOrder.get(0) : "None"));
        System.out.print("Rank (in order): ");
        if (goalOrder.size() == 0) {
            System.out.println("None");
        } else {
            for (int i = 0; i < goalOrder.size(); i++) {
                System.out.print("A" + goalOrder.get(i) + " ");
            }
            System.out.println();
        }
    }

    public void logGameSummaryToFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("Maze Escape Simulation Summary\n");
            writer.write("Maze Size: " + maze.getHeight() + "x" + maze.getWidth() + "\n");
            writer.write("Max Turns: " + maxTurns + "\n");
            writer.write("Turns Executed: " + turnCount + "\n");
            AgentList agents = maze.getAgentList();
            int totalMoves = 0;
            for (int i = 0; i < agents.size(); i++) {
                Agent a = agents.get(i);
                totalMoves += a.getTotalMoves();
                writer.write("Agent " + a.getId() + ":\n");
                writer.write("  Final Position: (" + a.getCurrentX() + "," + a.getCurrentY() + ")\n");
                writer.write("  Reached Goal: " + a.hasReachedGoal() + "\n");
                writer.write("  Total Moves: " + a.getTotalMoves() + "\n");
                writer.write("  Backtracks: " + a.getBacktracks() + "\n");
                writer.write("  Traps Triggered: " + a.getTrapsTriggered() + "\n");
                writer.write("  Power-ups Used: " + a.getPowerUpsUsed() + "\n");
                writer.write("  Max Stack Depth: " + a.getMaxStackDepth() + "\n");
            }
            double avgMoves = agents.size() > 0 ? (double) totalMoves / agents.size() : 0;
            writer.write("Average Moves per Agent: " + String.format("%.2f", avgMoves) + "\n");
            writer.write("Winner: " + (goalOrder.size() > 0 ? "A" + goalOrder.get(0) : "None") + "\n");
            writer.write("Agents Who Reached Goal: ");
            if (goalOrder.size() == 0) {
                writer.write("None\n");
            } else {
                for (int i = 0; i < goalOrder.size(); i++) {
                    writer.write("A" + goalOrder.get(i) + " ");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving game summary: " + e.getMessage());
        }
    }

    private boolean allAgentsFinished() {
        AgentList agents = maze.getAgentList();
        if (agents.isEmpty()) return true;
        for (int i = 0; i < agents.size(); i++) {
            if (!agents.get(i).isFinished()) return false;
        }
        return true;
    }
}