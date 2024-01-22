// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.

import Project1.Project1;

public class Main {
    public static void main(String[] args) {
        Project1 project2 = new Project1();
        try {
            project2.showTasks();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}