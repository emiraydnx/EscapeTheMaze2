package Turns;

import Agent.Agent;

public class TurnManager {
    private Queue<Agent> agentQueue;
    private int currentRound;

    public TurnManager() {
        this.agentQueue = new Queue<>();
        this.currentRound = 0;
    }

    public void addAgent(Agent a) {
        agentQueue.enqueue(a);
    }

    public void advanceTurn() {
        if (agentQueue.isEmpty()) return;
        Agent a = agentQueue.dequeue(); // Changed from poll
        currentRound++;
        logTurnSummary(a);
        if (!a.hasReachedGoal()) {
            agentQueue.enqueue(a);
        }
    }

    public Agent getCurrentAgent() {
        return agentQueue.peek();
    }

    public boolean allAgentsFinished() {
        Queue<Agent>.CustomIterator iterator = agentQueue.iterator();
        while (iterator.hasNext()) {
            Agent a = iterator.next();
            if (!a.hasReachedGoal()) return false;
        }
        return true;
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