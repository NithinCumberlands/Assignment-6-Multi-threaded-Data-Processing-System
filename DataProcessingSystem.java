import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class TaskQueue {
    private final Queue<String> queue = new LinkedList<>();

    public synchronized void addTask(String task) {
        queue.add(task);
        notifyAll();
    }

    public synchronized String getTask() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

class Worker implements Runnable {
    private final TaskQueue queue;
    private final String workerName;

    public Worker(TaskQueue queue, String name) {
        this.queue = queue;
        this.workerName = name;
    }

    @Override
    public void run() {
        System.out.println(workerName + " started.");

        while (true) {
            try {
                if (queue.isEmpty()) {
                    break;
                }

                String task = queue.getTask();
                System.out.println(workerName + " processing: " + task);

                Thread.sleep(1000);

                writeResult(workerName + " completed: " + task);

            } catch (InterruptedException e) {
                System.err.println(workerName + " interrupted: " + e.getMessage());
                break;
            } catch (IOException e) {
                System.err.println("File error: " + e.getMessage());
            }
        }

        System.out.println(workerName + " finished.");
    }

    private synchronized void writeResult(String result) throws IOException {
        try (FileWriter writer = new FileWriter("java_output.txt", true)) {
            writer.write(result + "\n");
        }
    }
}

public class DataProcessingSystem {
    public static void main(String[] args) throws InterruptedException {
        TaskQueue queue = new TaskQueue();

        for (int i = 1; i <= 10; i++) {
            queue.addTask("Task-" + i);
        }

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.execute(new Worker(queue, "Worker-1"));
        executor.execute(new Worker(queue, "Worker-2"));
        executor.execute(new Worker(queue, "Worker-3"));

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("All tasks completed.");
    }
}