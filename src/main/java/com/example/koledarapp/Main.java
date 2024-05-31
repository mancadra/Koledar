package com.example.koledarapp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDate;
import java.time.format.TextStyle;

public class Main extends Application {

    private ComboBox<String> monthComboBox;
    private TextField yearField;
    private TextField dateInputField;
    private Label dayNameLabel;
    private GridPane calendarGrid;
    private Calendar calendar;
    private String[] monthName = {"januar", "februar", "marec", "april", "maj", "junij", "julij", "avgust", "september", "oktober", "november", "december"};
    private Map<String, Integer> monthNameToValueMap;
    private boolean updatingFromDateInput = false;

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

        VBox topContainer = new VBox(10);
        topContainer.setAlignment(Pos.CENTER_LEFT);

        dayNameLabel = new Label();
        updateDayNameLabel(LocalDate.now());

        dateInputField = new TextField();
        updateDateInputField(LocalDate.now());
        dateInputField.setOnAction(e -> updateCalendarFromDateInput());

        HBox dateInputContainer = new HBox(10);
        dateInputContainer.setAlignment(Pos.CENTER_LEFT);
        dateInputContainer.getChildren().addAll(dayNameLabel, dateInputField);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER);
        monthComboBox = new ComboBox<>();
        yearField = new TextField();

        // combo box for the months
        monthComboBox.getItems().addAll(monthName);
        monthComboBox.setValue(monthName[LocalDate.now().getMonthValue()- 1]);
        yearField.setText(String.valueOf(LocalDate.now().getYear()));

        // if we change the month in the combo box the input date text gets reset to todays date
        monthComboBox.setOnAction(e -> {
            if (!updatingFromDateInput) { // only if its changed from the combobox and not when we chamge the values in combo-box and yearTextField according to the date input
                resetDateInputFieldToToday();
                updateCalendar();
            }
            updatingFromDateInput = false;
        });
        yearField.setOnAction(e -> {
            if (!updatingFromDateInput) {
                resetDateInputFieldToToday();
                updateCalendar();
            }
            updatingFromDateInput = false;
        });

        // yearField accepts numbers only
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // dateInputField accepts numbers, space and fullstops only
        dateInputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9 .]*")) {
                dateInputField.setText(newValue.replaceAll("[^0-9 .]", ""));
            }
        });

        header.getChildren().addAll(monthComboBox, yearField);
        header.getStyleClass().add("header");

        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.getStyleClass().add("calendar-grid");

        topContainer.getChildren().addAll(dateInputContainer, header);
        root.setTop(topContainer);
        root.setCenter(calendarGrid);

        updateCalendar();

        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

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
            if (i == 6) label.getStyleClass().add("sunday");

            calendarGrid.add(label, i, 0);
        }

        int day = 1;
        for (int i = firstDayInMonth; i < firstDayInMonth + daysInMonth; i++) {
            int row = i / 7 + 1;
            int col = i % 7;

            Label dayLabel = new Label(String.valueOf(day));

            // colouring of sundays
            if (col == 6) {
                dayLabel.getStyleClass().add("sunday");
            }
            if (calendar.isHoliday(day)) {
                dayLabel.getStyleClass().add("holiday");
            }

            calendarGrid.add(dayLabel, col, row);
            day++;
        }
    }

    private void updateCalendarFromDateInput() {

        try {
            updatingFromDateInput = true;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. M. yyyy", new Locale("sl"));
            LocalDate date = LocalDate.parse(dateInputField.getText(), formatter);

            monthComboBox.setValue(monthName[date.getMonthValue() - 1]);
            yearField.setText(String.valueOf(date.getYear()));
            calendar.setYear(date.getYear());
            calendar.setMonth(date.getMonthValue());

            updateDayNameLabel(date);
            updateCalendar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetDateInputFieldToToday() {
        updateDateInputField(LocalDate.now());
        updateDayNameLabel(LocalDate.now());
    }

    private void updateDateInputField(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. M. yyyy", new Locale("sl"));
        dateInputField.setText(date.format(formatter));
    }

    private void updateDayNameLabel(LocalDate date) {
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("sl"));
        dayNameLabel.setText(dayName + ", ");
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
