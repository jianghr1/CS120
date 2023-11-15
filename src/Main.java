// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
import Project2.Project2;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Project2 project2 = new Project2();
        try {
            project2.showTasks();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}