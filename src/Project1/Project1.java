package Project1;

import java.util.Scanner;

public class Project1 {
    public final Task1 task1 = new Task1();
    public final Task2 task2 = new Task2();
    public Project1 () {

    }

    public void showTasks() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currently on Project1.");

        System.out.println("Going to show Task1. Enter to start");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
            task1.run();
        }

        System.out.println("Going to show Task2. Enter to start");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
            task2.run();
        }
        scanner.close();

    }
}
