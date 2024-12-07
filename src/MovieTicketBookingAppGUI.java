import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class MovieTicketBookingAppGUI extends Application {
    private BookingSys BS;
    private final int rows = 5;
    private final int cols = 5;
    private final Map<String, Button> seatButtonMap = new HashMap<>();
    // had to make this hash map to link seats button to the concurrent booking test result

    @Override
    public void start(Stage primaryStage) {
        this.BS = new BookingSys(this.rows, this.cols);
        GridPane gridPane = getGridPane(); // seats grid...

        Scene scene = new Scene(gridPane, 800, 600); // scene here is the main window of the app
        primaryStage.setTitle("Movie Ticket Booking System");
        primaryStage.setScene(scene);
        primaryStage.show();

        testConcurrentBookings(); // just for testing the concurrent booking...
    }

    private GridPane getGridPane() {
        GridPane gridPane = new GridPane();

        gridPane.setHgap(10); // Horizontal spacing between buttons
        gridPane.setVgap(10); // Vertical spacing between buttons
        gridPane.setPadding(new javafx.geometry.Insets(300, 10, 10, 100));

        // setting buttons on seats grid
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {
                Button seatButton = new Button("Seat " + (row + 1) + "," + (col + 1));
                seatButton.setStyle(
                        "-fx-background-color: lightgreen; " +
                        "-fx-border-color: darkgreen; " +
                        "-fx-border-width: 3px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-weight: bold;" +
                        "-fx-min-width: 110px;"
                );

                final int r = row;
                final int c = col;

                seatButton.setOnAction(event -> handleSeatBooking(seatButton, r, c));
                seatButtonMap.put(r + "," + c, seatButton);
                gridPane.add(seatButton, col, row); // now after button is ready we add it to le grid
            }
        }
        return gridPane;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void changeSeatColor(Button seatButton, boolean success){ // updating seats color/info if booking succeeded...
        Platform.runLater(() -> {
            if (success) {
                seatButton.setText("Booked");
                seatButton.setStyle(
                        "-fx-background-color: red; " +
                        "-fx-border-color: darkred; " +
                        "-fx-border-width: 3px; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px; " +
                        "-fx-font-weight: bold;" +
                        "-fx-min-width: 110px;"
                );
                showAlert("Success", "Seat booked successfully!");
            } else {
                showAlert("Error", "Seat already booked.");
            }
        });
    }

    private void handleSeatBooking(Button seatButton, int row, int col) {
        Thread bookingThread = new Thread(() -> { // separate new thread to handle the booking
            boolean success = BS.bookSeat(row, col);
            changeSeatColor(seatButton, success);
        });

        bookingThread.start();
    }

    private void testConcurrentBookings() { // representing users bookings as runnable tasks

        int row = 2, col = 2;
        Runnable user1Task = () -> {
            boolean success = BS.bookSeat(row, col);
            updateGUIWithResult("User1", row, col, success);
        };

        Runnable user2Task = () -> {
            boolean success = BS.bookSeat(row, col); // now both users are trying to book the same seat concurrentlyyy
            updateGUIWithResult("User2", row, col, success);
        };

        Thread user1Thread = new Thread(user1Task);
        Thread user2Thread = new Thread(user2Task);

        user1Thread.start();
        user2Thread.start();
    }

    private void updateGUIWithResult(String user, int row, int col, boolean success) {
        Platform.runLater(() -> {
            String seatKey = row + "," + col;
            Button seatButton = seatButtonMap.get(seatKey);

            if (seatButton != null) {
                changeSeatColor(seatButton, success);
                String result = success
                        ? user + " successfully booked Seat (" + (row + 1) + ", " + (col + 1) + ")"
                        : user + " failed to book Seat (" + (row + 1) + ", " + (col + 1) + ")";
                System.out.println(result);
            }
        });
    }
}


//    private void logBookingResult(String user, int row, int col, boolean success) {
//        Platform.runLater(() -> {
//            String result = success
//                    ? user + " successfully booked Seat (" + (row + 1) + ", " + (col + 1) + ")"
//                    : user + " failed to book Seat (" + (row + 1) + ", " + (col + 1) + ")";
//            System.out.println(result);
//        });
//    }