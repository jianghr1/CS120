package Project2;

import Project2.Task1;

import java.io.IOException;
import java.util.Scanner;

public class Project2 {

    public void showTasks() throws InterruptedException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currently on Project1.");

        Task1 task1 = new Task1(64);

        System.out.println("Going to show Task3. Enter to start");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
            task1.run();
        }
        scanner.close();
    }
}
