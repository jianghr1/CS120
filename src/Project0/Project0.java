package Project0;

import java.util.Scanner;

public class Project0 {
    public final Task1 task1 = new Task1();
    public final Task2 task2 = new Task2();
    public Project0 () {

    }

    public void showTasks() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currently on Project0.");

        System.out.println("Going to show Task1. Enter to start");
        if (scanner.hasNextLine()) {
            String dummy = scanner.nextLine();
            task1.run();
        }

        System.out.println("Going to show Task2. Enter to start");
        if (scanner.hasNextLine()) {
            String dummy = scanner.nextLine();
            task2.run();
        }
        scanner.close();

    }
}
