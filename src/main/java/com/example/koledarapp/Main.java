package com.example.koledarapp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.time.LocalDate;

public class Main extends Application {

    private ComboBox<String> monthComboBox;
    private TextField yearField;
    private GridPane calendarGrid;
    private Calendar calendar;
    private String[] monthName = {"januar", "februar", "marec", "april", "maj", "junij", "julij", "avgust", "september", "oktober", "november", "december"};
    private Map<String, Integer> monthNameToValueMap;
    public static void main(String[] args) { launch(); }

    @Override
    public void start(Stage stage) throws Exception {
        monthNameToValueMap = new HashMap<>();
        for (int i = 0; i < monthName.length; i++) {
            monthNameToValueMap.put(monthName[i], i + 1);
        }

        Set<Holiday> holidays = loadHolidays("holidaysList.txt");
        calendar = new Calendar(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), holidays);

        BorderPane root = new BorderPane();

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);
        monthComboBox = new ComboBox<>();
        yearField = new TextField();
        Button updateButton = new Button("Posodobi");

        // combo box for the months
        monthComboBox.getItems().addAll(monthName);
        monthComboBox.setValue(monthName[LocalDate.now().getMonthValue()- 1]);
        yearField.setText(String.valueOf(LocalDate.now().getYear()));

        updateButton.setOnAction(e -> updateCalendar());

        header.getChildren().addAll(new Label("Mesec:"), monthComboBox, new Label("Leto:"), yearField, updateButton);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);

        root.setTop(header);
        root.setCenter(calendarGrid);

        updateCalendar();

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("Koledar");
        stage.show();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        int month = monthNameToValueMap.get(monthComboBox.getValue());
        int year = Integer.parseInt(yearField.getText());

        calendar.setYear(year);
        calendar.setMonth(month);

        int daysInMonth = calendar.getDaysInMonth();
        int firstDayInMonth = calendar.firstDayOfTheMonth();

        String[] daysOfWeek = {"pon.", "tor.", "sre.", "čet.", "pet.", "sob.", "ned."};
        for (int i = 0; i < 7; i++) {
            Label label = new Label(daysOfWeek[i]);
            if (i == 6) label.setStyle("-fx-text-fill: red;");

            calendarGrid.add(label, i, 0);
        }

        int day = 1;
        for (int i = firstDayInMonth; i < firstDayInMonth + daysInMonth; i++) {
            int row = i / 7 + 1;
            int col = i % 7;

            Label dayLabel = new Label(String.valueOf(day));

            // colouring of sundays
            if (col == 6) {
                dayLabel.setStyle("-fx-text-fill: red;");
            }
            if (calendar.isHoliday(day)) {
                dayLabel.setStyle("-fx-background-color: green;");
            }

            calendarGrid.add(dayLabel, col, row);
            day++;
        }
    }

    private Set<Holiday> loadHolidays(String fileName) {
        Set<Holiday> holidays = new HashSet<>();
        try(InputStream inputStream = getClass().getResourceAsStream("/" + fileName); BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                String[] dateParts = parts[0].split("-");
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                boolean annual = false;
                if (parts[1].equals("da")) annual = true;
                else if (parts[1].equals("ne")) annual = false;

                LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
                holidays.add(new Holiday(date, annual));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return holidays;
    }
}
