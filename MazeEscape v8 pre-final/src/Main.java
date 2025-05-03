import Maze.*;
import Controlls.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("######## Maze Esacpe Game ########");
        System.out.print("Enter maze width: ");
        int width = scanner.nextInt();
        System.out.print("Enter maze height: ");
        int height = scanner.nextInt();
        System.out.print("Enter number of agents: ");
        int numAgents = scanner.nextInt();
        System.out.print("Enter maximum turns: ");
        int maxTurns = scanner.nextInt();
        MazeManager maze = new MazeManager(width, height);
        maze.generateMaze();
        GameController controller = new GameController(maze, numAgents, maxTurns);
        controller.runSimulation();
        scanner.close();
    }
}