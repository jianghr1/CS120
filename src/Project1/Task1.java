package Project1;

import Project0.Project0;

public class Task1  implements Runnable {
    private final Project0 project0 = new Project0();

    @Override
    public void run() {
        project0.showTasks();
    }
}
