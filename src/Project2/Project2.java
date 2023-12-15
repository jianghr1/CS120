package Project2;

import java.io.IOException;
import java.util.Scanner;

public class Project2 {

    public void showTasks() throws InterruptedException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currently on Project1.");

        Task2 task3 = new Task2();

        System.out.println("Going to show Task3. Enter to start");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
            task3.run(true,true);
        }
        scanner.close();
    }
}
