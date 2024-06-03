package com.example.koledarapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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
        dayNameLabel.getStyleClass().add("regular-text");

        dateInputField = new TextField();
        updateDateInputField(LocalDate.now());
        dateInputField.getStyleClass().add("input-field");
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
        monthComboBox.getStyleClass().add("input-field");

        // if we change the month in the combo box the input date text gets reset to today's date
        monthComboBox.setOnAction(e -> {
            if (!updatingFromDateInput) { // only if it's changed from the combobox and not when we change the values in combo-box and yearTextField according to the date input
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
        yearField.getStyleClass().add("input-field");

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

        topContainer.getChildren().addAll(dateInputContainer, header);
        topContainer.getStyleClass().add("header");
        root.setTop(topContainer);
        root.setCenter(calendarGrid);

        updateCalendar();

        Scene scene = new Scene(root, 500, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene.getRoot().getStyleClass().add("root");

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setScene(scene);
        stage.setTitle("Koledar");
        stage.show();
    }


    // updates the calendar grid according to the value in month combo-box and yearField
    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        int month = monthNameToValueMap.get(monthComboBox.getValue());
        int year = Integer.parseInt(yearField.getText());

        calendar.setYear(year);
        calendar.setMonth(month);

        int daysInMonth = calendar.getDaysInMonth();
        int firstDayInMonth = calendar.firstDayOfTheMonth();

        String[] daysOfWeek = {"PON", "TOR", "SRE", "ČET", "PET", "SOB", "NED"};
        for (int i = 0; i < 7; i++) {
            Label label = new Label(daysOfWeek[i]);
            label.setMinWidth(40);
            label.setMinHeight(40);
            label.setStyle("-fx-alignment: center;");
            if (i == 6) label.getStyleClass().add("sunday");
            else {
                label.getStyleClass().add("other-days");
            }
            GridPane.setMargin(label, new Insets(8));
            calendarGrid.add(label, i, 0);
        }

        int day = 1;
        for (int i = firstDayInMonth; i < firstDayInMonth + daysInMonth; i++) {
            int row = i / 7 + 1;
            int col = i % 7;

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setMinWidth(40);
            dayLabel.setMinHeight(40);
            dayLabel.setStyle("-fx-alignment: center;");

            // colouring of sundays
            if (col == 6) {
                dayLabel.getStyleClass().add("sunday");
            }
            else if (calendar.isHoliday(day)) {
                dayLabel.getStyleClass().add("holiday");
                if (col == 6) {
                    dayLabel.getStyleClass().add("holiday-sunday");
                }
            } else {
                dayLabel.getStyleClass().add("label-day");
            }
            GridPane.setMargin(dayLabel, new Insets(8));
            calendarGrid.add(dayLabel, col, row);
            day++;
        }
    }


    // updates the calendar grid and the combo-box value for the month and yearField based on the input in dateInputField
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


    // when choosing a month in combo-box or year in yearField we call this function to reset the dateInputField and dayNameLabel to todays date
    private void resetDateInputFieldToToday() {
        updateDateInputField(LocalDate.now());
        updateDayNameLabel(LocalDate.now());
    }


    // updates the date in the dateInputField
    private void updateDateInputField(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. M. yyyy", new Locale("sl"));
        dateInputField.setText(date.format(formatter));
    }


    // updates the name of the day, for the choosen date in dateInputField
    private void updateDayNameLabel(LocalDate date) {
        String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("sl"));
        dayNameLabel.setText(dayName + ", ");
    }


    // Reads holidays from the .txt file and adds them to the holidays hashset
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
