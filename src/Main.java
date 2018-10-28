import java.util.ArrayList;

class Container {
    private ArrayList<String> queue = new ArrayList<String>();
    private String productRed;
    private String productBlue;
    private boolean redAvailable = false;
    private boolean blueAvailable = false;
    public void addToQueue(String consumerName) {
        if (!this.queue.contains(consumerName)) {
            this.queue.add(consumerName);
        }
    }
    public boolean isAvailable(String consumerName) {
        return this.redAvailable && this.blueAvailable;

    }
    public int getQueuePosition(String consumerName) {
        return this.queue.indexOf(consumerName);
    }
    public void removeFromQueue() {
        this.queue.remove(0);
    }
    synchronized public String takeRed(String consumerName) {
        if (!this.redAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        System.out.println(this.productRed + " taken from container by " + consumerName);
        this.redAvailable = false;
        notify();
        return this.productRed;
    }
    synchronized public String takeBlue(String consumerName) {
        if (!this.blueAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        System.out.println(this.productBlue + " taken from container by " + consumerName);
        this.blueAvailable = false;
        notify();
        return this.productBlue;
    }
    synchronized public void insertRed(String product) {
        if (this.redAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        System.out.println(product + " inserted to container");
        this.redAvailable = true;
        this.productRed = product;
        notify();
    }
    synchronized public void insertBlue(String product) {
        if (this.blueAvailable) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        System.out.println(product + " inserted to container");
        this.blueAvailable = true;
        this.productBlue = product;
        notify();
    }
}

class Producer implements Runnable {
    private Container container;
    public Producer(Container container) {
        this.container = container;
    }
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep((int) (100 * Math.random()));
            } catch (InterruptedException e) {}
            container.insertRed("red");
            try {
                Thread.sleep((int) (100 * Math.random()));
            } catch (InterruptedException e) {}
            container.insertBlue("blue");
        }
    }
}

class Consumer implements Runnable {
    private Container container;
    private String name;
    public Consumer(String name, Container container) {
        this.name = name;
        this.container = container;
    }
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep((int) (100 * Math.random()));
            } catch (InterruptedException e) {}
            container.addToQueue(this.name);
            if (this.checkIsAvailable() && this.container.getQueuePosition(this.name) == 0) {
                container.takeRed(this.name);
                container.takeBlue(this.name);
                this.container.removeFromQueue();
            }
        }
    }
    private boolean checkIsAvailable() {
        return (this.container.isAvailable(this.name));
    }
}


public class Main {

    public static void main(String[] args) {
        // containers
        Container container = new Container();
        // consumers
        Consumer consumer1 = new Consumer("John", container);
        Consumer consumer2 = new Consumer("Harry", container);
        Consumer consumer3 = new Consumer("Alice", container);
        // producers
        Producer producer = new Producer(container);
        // threads
        Thread[] threads = {
                new Thread(consumer1),
                new Thread(consumer2),
                new Thread(consumer3),
                new Thread(producer),
        };
        // start threads
        for (int i = 0; i < threads.length; i++) {
            Thread thread = threads[i];
            thread.start();
        }
    }
}
