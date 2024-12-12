import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieBookingAppGUI extends Application {
    private static int numberOfInstances;
    List<Map<String, Button>> seatButtonMaps = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Concurrent Booking Configuration");

        Label instructionLabel = new Label("Enter # of concurrent booking windows:");
        TextField instanceTextField = new TextField();
        Button submitButton = new Button("Test Concurrent Booking Sim");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(instructionLabel, instanceTextField, submitButton);

        Scene configScene = new Scene(vbox, 300, 200);
        primaryStage.setScene(configScene);
        primaryStage.show();

        submitButton.setOnAction(event -> {
            try {
                numberOfInstances = Integer.parseInt(instanceTextField.getText());

                if (numberOfInstances <= 0) {
                    Show_Alert("Error", "Positive numbers onlyyy!!");
                    return;
                }

                primaryStage.close();
                Launch_Multiple_Instances();
            } catch (NumberFormatException e) {
                Show_Alert("Error", "Enter a valid number!");
            }
        });
    }

    private void Launch_Multiple_Instances() {
        List<Stage> stages = new ArrayList<>();
        BookingSys bookingSys = new BookingSys(5, 5);

        for (int i = 0; i < numberOfInstances; i++) {
            Stage stage = new Stage();


            Map<String, Button> seatButtonMap = new HashMap<>();
            GridPane gridPanel = Get_Grid_Panel(bookingSys, seatButtonMap);

            Scene scene = new Scene(gridPanel, 800, 600);
            stage.setTitle("Movie Ticket Booking System | Booking " + (i + 1));
            stage.setScene(scene);
            stage.show();

            stages.add(stage);
            seatButtonMaps.add(seatButtonMap);
        }

        Test_Concurrent_Bookings(stages, bookingSys, seatButtonMaps);
    }

    private GridPane Get_Grid_Panel(BookingSys bookingSys, Map<String, Button> seatButtonMap) {
        GridPane gridPanel = new GridPane();

        gridPanel.setHgap(10);
        gridPanel.setVgap(10);
        gridPanel.setPadding(new javafx.geometry.Insets(300, 10, 10, 100));

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
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

                seatButton.setOnAction(event -> Handle_Seat_Booking(r, c, bookingSys));
                seatButtonMap.put(r + "," + c, seatButton);
                gridPanel.add(seatButton, col, row);
            }
        }
        return gridPanel;
    }

    private void Show_Alert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void Change_Seat_Color(Button seatButton) {
        Platform.runLater(() -> {
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
        });
    }

    private void Handle_Seat_Booking(int row, int col, BookingSys bookingSys) {
        Thread bookingThread = new Thread(() -> {
            boolean success = bookingSys.bookSeat(row, col);

            if (success) {
                Update_All_Instances(row, col);
                Show_Alert("Success", "Seat booked successfully!");
            } else {
                Show_Alert("Error", "Seat already booked.");
            }
        });

        bookingThread.start();
    }

    private void Update_All_Instances(int row, int col) {
        Platform.runLater(() -> {
            for (Map<String, Button> seatButtonMap : seatButtonMaps) {
                Button seatButton = seatButtonMap.get(row + "," + col);
                if (seatButton != null) {
                    Change_Seat_Color(seatButton);
                }
            }

        });
    }

    private void Test_Concurrent_Bookings(List<Stage> stages, BookingSys bookingSys, List<Map<String, Button>> seatButtonMaps) {
        int row = 2, col = 2;

        for (int i = 0; i < stages.size(); i++) {
            final int index = i;
            Runnable userTask = () -> {
                Map<String, Button> seatButtonMap = seatButtonMaps.get(index);

                boolean success = bookingSys.bookSeat(row, col);

                Platform.runLater(() -> {
                    Button seatButton = seatButtonMap.get(row + "," + col);
                    if (seatButton != null) {
                        Update_All_Instances(row, col);
                        if (success){
                            Show_Alert("Success", "Seat booked successfully!");
                        }else {
                            Show_Alert("Error", "Seat already booked.");
                        }
                        String result = success
                                ? "User " + (index+1) + " successfully booked Seat (" + (row+1) + ", " + (col+1) + ")"
                                : "User " + (index+1) + " failed to book Seat (" + (row+1) + ", " + (col+1) + ")";
                        System.out.println(result);
                    }
                });
            };

            new Thread(userTask).start();
        }
    }
}
