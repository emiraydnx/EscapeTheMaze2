package Turns;

import Agent.*;

public class TurnManager {
    private Queue<Agent> agentQueue; // Replaced LinkedList with Queue<Agent>
    private int currentRound;

    public TurnManager() {
        this.agentQueue = new Queue<>(); // Use custom Queue
        this.currentRound = 0;
    }

    public void addAgent(Agent a) {
        agentQueue.enqueue(a);
    }

    public void advanceTurn() {
        if (agentQueue.isEmpty()) return;
        Agent a = agentQueue.dequeue();
        currentRound++;
        logTurnSummary(a);
        if (!a.hasReachedGoal()) {
            agentQueue.enqueue(a);
        }
    }

    public Agent getCurrentAgent() {
        if (agentQueue.isEmpty()) return null;
        Agent a = agentQueue.dequeue();
        agentQueue.enqueue(a); // Re-enqueue to preserve order
        return a;
    }

    public boolean isQueueEmpty() {
        return agentQueue.isEmpty(); // Returns true if no agents remain
    }
    public Queue getAgentQueue() {
        return agentQueue;
    }

    public boolean allAgentsFinished() {
        Queue<Agent> temp = new Queue<>();
        boolean allFinished = true;
        while (!agentQueue.isEmpty()) {
            Agent a = agentQueue.dequeue();
            if (!a.hasReachedGoal()) allFinished = false;
            temp.enqueue(a);
        }
        while (!temp.isEmpty()) {
            agentQueue.enqueue(temp.dequeue());
        }
        return allFinished;
    }

    public void logTurnSummary(Agent a) {
        System.out.printf(
                "Round %d – Agent %d at (%d,%d), moves=%d, backtracks=%d%n",
                currentRound,
                a.getId(),
                a.getCurrentX(), a.getCurrentY(),
                a.getTotalMoves(),
                a.getBacktracks()
        );
    }
}