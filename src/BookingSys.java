import java.util.concurrent.locks.ReentrantLock;

public class BookingSys {
    private final boolean[][] seats;
    private final ReentrantLock lock;

    public BookingSys(int rows, int cols) {
        seats = new boolean[rows][cols];
        lock = new ReentrantLock();
    }

    public boolean bookSeat(int row, int col) {
        lock.lock();
        try {
            if (!seats[row][col]) {
                seats[row][col] = true;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}
