import java.util.concurrent.locks.ReentrantLock;

public class BookingSys {
    private final boolean[][] seats; // made it boolean to represent the seat availability...
    private final ReentrantLock lock; // will be used to ensure thread safety, in other words to prevent STARVATION!!

    public BookingSys(int rows, int cols) {
        this.seats = new boolean[rows][cols];
        this.lock = new ReentrantLock();
    }

    public boolean bookSeat(int row, int col) {
        this.lock.lock(); // locking to ensure thread safety
        try {
            if (!this.seats[row][col]) {
                this.seats[row][col] = true;
                return true;
            }
            return false;
        } finally {
            this.lock.unlock(); // releasing the lock, this step is so important
        }
    }
}
