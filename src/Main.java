// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
import Project0.Task1;

public class Main {
    public static void main(String[] args) {
        Task1 thisTask = new Task1();
        Thread thisTaskThread = new Thread(thisTask);
        thisTaskThread.start();
    }
}