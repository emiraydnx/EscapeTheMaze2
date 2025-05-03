package Turns;
import Agent.*;
import Maze.*;

public class TurnScheduler {
    private MazeManager maze;
    private Queue<Agent> agentQueue;

    public TurnScheduler(MazeManager maze, Queue agentQueue) {
        this.maze = maze;
        this.agentQueue = agentQueue;
    }

    public Queue<Agent> getTurnOrder() {
        return agentQueue;
    }

    public void addAgent(Agent a) {
        agentQueue.enqueue(a);
    }

    public void removeAgent(Agent a) {
        // Create a temporary queue to hold non-removed agents
        Queue<Agent> tempQueue = new Queue<>();
        while (!agentQueue.isEmpty()) {
            Agent current = agentQueue.dequeue();
            if (current != a) {
                tempQueue.enqueue(current);
            }
        }
        // Restore the queue with remaining agents
        while (!tempQueue.isEmpty()) {
            agentQueue.enqueue(tempQueue.dequeue());
        }
    }

    public boolean isQueueEmpty() {
        return agentQueue.isEmpty();
    }
}