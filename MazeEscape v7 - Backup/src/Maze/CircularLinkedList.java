package Maze;

public class CircularLinkedList {
    private Node head;
    private Node tail;

    public static class Node {
        MazeTile tile;
        Node next;
        Node(MazeTile tile) { this.tile = tile; }
    }

    public CircularLinkedList() {
        head = null;
        tail = null;
    }

    public void add(MazeTile tile) {
        Node newNode = new Node(tile);
        if (head == null) {
            head = newNode;
            tail = newNode;
            newNode.next = head;
        } else {
            tail.next = newNode;
            tail = newNode;
            tail.next = head;
        }
    }

    public void rotate() {
        if (head != null) {
            head = head.next;
            tail = tail.next;
        }
    }

    public Node getHead() { return head; }
}