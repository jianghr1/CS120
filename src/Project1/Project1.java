package Project1;

import java.io.IOException;
import java.util.Scanner;

public class Project1 {
    public Project1() {

    }

    public void showTasks() throws InterruptedException, IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Currently on Project1.");

//        Task2 task2 = new Task2();
//        System.out.println("Going to show Task2. Enter to start");
//        if (scanner.hasNextLine()) {
//            scanner.nextLine();
//            task2.run();
//        }

        Task3 task3 = new Task3(126);

        task3.run();
//        System.out.println("Going to show Task3. Enter to start");
//        if (scanner.hasNextLine()) {
//            scanner.nextLine();
//        }
        scanner.close();
    }
}
