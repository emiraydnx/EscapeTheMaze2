package Turns;

public class Queue<T> {
    // Inner Node class to hold data of type T
    private class Node {
        T data;
        Node next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    // Constructor
    public Queue() {
        head = null;
        tail = null;
        size = 0;
    }

    // Add an item to the queue
    public void enqueue(T item) {
        Node newNode = new Node(item);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    // Remove and return the first item
    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T item = head.data;
        head = head.next;
        size--;
        if (isEmpty()) {
            tail = null;
        }
        return item;
    }

    // Check if the queue is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // Get the size of the queue
    public int size() {
        return size;
    }

    // Optional: String representation for debugging
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node current = head;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }
}