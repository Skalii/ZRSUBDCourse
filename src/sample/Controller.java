package sample;

import com.jfoenix.controls.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import org.intellij.lang.annotations.Language;

import javax.swing.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Pattern;

public class Controller {
    private DBConnection dbConnection = new DBConnection();

    @FXML
    private TableView<String[]> tableQuery, tableQuerySD, tableSearch, tableOrder;

    @FXML
    private AnchorPane anchorSearch, anchorQuery;

    @FXML
    private JFXComboBox<String> comboTables, combo1, combo2, combo3, comboSearchManufacturer, comboSearchModel,
            comboSearchDiagonal, comboSearchResolution, comboSearchProcessor, comboSearchVideo, comboSearchRAM,
            comboSearchHDD, comboSearchDVD, comboSearchBattery, comboSearchOS;

    @FXML
    private JFXButton buttonAdd, buttonDelete, buttonLogin, buttonExit,
            buttonOrderCreate, buttonOrderAdd, buttonOrderDelete, buttonExec;

    @FXML
    private JFXTextField textAdd1, textAdd2, textAdd3, textAdd4, textAdd5, textAdd6,
            textAdd7, textAdd8, textAdd9, textAdd10, textAdd11, textAdd12, textAdd13,
            textLogin, textClientName, textClientEmail, textClientPhone, textSearch, textAdminSearch;

    @FXML
    private JFXDatePicker datePicker;

    @FXML
    private JFXPasswordField textPassword;

    @FXML
    private JFXCheckBox checkSearchExt;

    @FXML
    private Tab tabAdmin;

    @FXML
    private Label labelAdmin, labelErrorLogin,
            labelSumUnit, labelModelQuantity, labelSumOrder, labelInfoSum, labelInfoSumOrder;

    private boolean login = false;

    public void initialize() {
        dbConnection.connection();

        generateTable(tableSearch,
                "SELECT ID_INVENTORY, VAL, QUANTITY, MANUFACTURER, MODEL, DIAGONAL, " +
                        "RESOLUTION, PROCESSOR, VIDEO, RAM, HDD_SSD, DVD, BATTERY, OS FROM INVENTORY " +
                        "ORDER BY ID_INVENTORY",
                new String[]{"", "", "", "Производитель", "Модель", "Диагональ", "Разрешение",
                        "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                null, null);
        tableSearch.getColumns().get(1).setVisible(false);
        tableSearch.getColumns().get(2).setVisible(false);

        ObservableList<JFXComboBox> comboSearch = FXCollections.observableArrayList(comboSearchManufacturer,
                comboSearchModel, comboSearchDiagonal, comboSearchResolution, comboSearchProcessor,
                comboSearchVideo, comboSearchRAM, comboSearchHDD, comboSearchDVD, comboSearchBattery, comboSearchOS);

        isCorrectValuesIntFloat(FXCollections.observableArrayList(textClientPhone), new int[]{0}, null);

        String genSearch = "SELECT ID_INVENTORY, VAL, QUANTITY, MANUFACTURER, MODEL, DIAGONAL, " +
                "RESOLUTION, PROCESSOR, VIDEO, RAM, HDD_SSD, DVD, BATTERY, OS FROM INVENTORY ";

        for (int i = 0; i < comboSearch.size(); i++) {
            comboSearch.get(i).getItems().add("-  не выбрано  -");
            comboSearch.get(i).getItems().addAll(setItemsCombo(
                    "SELECT + " + new String[]{"MANUFACTURER", "MODEL", "DIAGONAL", "RESOLUTION", "PROCESSOR",
                            "VIDEO", "RAM", "HDD_SSD", "DVD", "BATTERY", "OS"}[i] + " FROM INVENTORY"));
            int finalI = i;
            comboSearch.get(i).setOnAction(action -> {
                comboSearch.get(finalI).getSelectionModel().clearSelection(0);
                tableSearch.getColumns().remove(0, tableSearch.getColumns().size());
                String search = "";

                for (int j = 0; j < comboSearch.size(); j++)
                    if (comboSearch.get(j).getValue() != null
                            && !comboSearch.get(j).getSelectionModel().isSelected(0)) {
                        if (!Objects.equals(search, ""))
                            search = search.concat(" AND ");
                        search = search.concat(new String[]{"MANUFACTURER", "MODEL", "DIAGONAL", "RESOLUTION",
                                "PROCESSOR", "VIDEO", "RAM", "HDD_SSD", "DVD", "BATTERY", "OS"}[j] + " = '" +
                                comboSearch.get(j).getSelectionModel().getSelectedItem() + "'");
                    }
                generateTable(tableSearch,
                        genSearch + (!Objects.equals(search, "") ? "WHERE ".concat(search) : ""),
                        new String[]{"", "", "", "Производитель", "Модель", "Диагональ", "Разрешение",
                                "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                        null, null);
                tableSearch.getColumns().get(1).setVisible(false);
                tableSearch.getColumns().get(2).setVisible(false);
            });
        }

        ObservableList<String[]> listOrder = FXCollections.observableArrayList();
        generateTableOrder(tableOrder, new String[]{"Модель", "Количество", "Стоимость"});
        tableOrder.getColumns().get(0).setPrefWidth(176);
        buttonOrderAdd.setOnAction(action -> {
            if (tableSearch.getSelectionModel().isSelected(tableSearch.getSelectionModel().getSelectedIndex())) {

                if (!Objects.equals(tableSearch.getSelectionModel().getSelectedItem()[2], "0")) {
                    String[] quantity = new String[Integer.parseInt(tableSearch.getSelectionModel().getSelectedItem()[2])];

                    for (int i = 0; i < quantity.length; i++) quantity[i] = String.valueOf(i + 1);

                    Object quant = JOptionPane.showInputDialog(JOptionPane.getRootFrame(), "Выберите количество:",
                            "Количество", JOptionPane.PLAIN_MESSAGE, new ImageIcon(""), quantity, quantity[0]);

                    if (quant != null) {
                        tableSearch.getSelectionModel().getSelectedItem()[2] =
                                String.valueOf(Integer.parseInt(tableSearch.getSelectionModel().getSelectedItem()[2])
                                        - Integer.parseInt(String.valueOf(quant)));

                        if (Integer.parseInt(tableSearch.getSelectionModel().getSelectedItem()[2]) < 3)
                            labelModelQuantity.setText("На складе меньше 3 единиц");

                        double sum = Double.parseDouble(tableSearch.getSelectionModel().getSelectedItem()[1])
                                * Integer.parseInt(String.valueOf(quant));

                        listOrder.add(new String[]{tableSearch.getSelectionModel().getSelectedItem()[4],
                                String.valueOf(quant), String.valueOf(sum)});

                        tableOrder.setItems(listOrder);
                        buttonOrderDelete.setDisable(false);
                        tableSearch.getSelectionModel().clearSelection();
                        labelSumUnit.setText("Стоимость");

                        if (Objects.equals(labelSumOrder.getText(), "Сумма заказа")) labelSumOrder.setText("0");

                        double sumOrder = 0;
                        for (int i = 0; i < tableOrder.getItems().size(); i++) {
                            sumOrder += Double.parseDouble(tableOrder.getItems().get(i)[2]);
                            labelSumOrder.setText(String.valueOf(sumOrder));
                        }
                    }
                } else
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                            "Данного товара нет в наличии", "Товар закончился", JOptionPane.INFORMATION_MESSAGE);
            } else
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                        "Выбeрите товар", "Товар не выбран", JOptionPane.ERROR_MESSAGE);
        });
        buttonOrderDelete.setOnAction(action -> {
            if (tableOrder.getSelectionModel().isSelected(tableOrder.getSelectionModel().getSelectedIndex())) {

                if (tableOrder.getItems().size() >= 1) {

                    for (int i = 0; i < tableSearch.getItems().size(); i++)
                        if (Objects.equals(tableSearch.getItems().get(i)[4],
                                tableOrder.getSelectionModel().getSelectedItem()[0])) {

                            tableSearch.getItems().get(i)[2] =
                                    String.valueOf(Integer.parseInt(tableSearch.getItems().get(i)[2])
                                            + Integer.parseInt(tableOrder.getSelectionModel().getSelectedItem()[1]));

                            if (Integer.parseInt(tableSearch.getItems().get(i)[2]) > 3)
                                labelModelQuantity.setText("Есть в наличии");
                            break;
                        }

                    tableOrder.getItems().remove(tableOrder.getSelectionModel().getSelectedIndex());

                    double sumOrder = 0;
                    for (int i = 0; i < tableOrder.getItems().size(); i++) {
                        sumOrder += Double.parseDouble(tableOrder.getItems().get(i)[2]);
                        labelSumOrder.setText(String.valueOf(sumOrder));
                    }

                }
                if (tableOrder.getItems().size() == 0) {
                    labelSumOrder.setText("Сумма заказа");
                    buttonOrderDelete.setDisable(true);
                }
            } else
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                        "Выбeрите товар", "Товар не выбран", JOptionPane.ERROR_MESSAGE);
        });
        buttonOrderCreate.setOnAction(action -> {
            if (isSelectedField(
                    FXCollections.observableArrayList(textClientName, textClientEmail, textClientPhone),
                    null)) {
                Object seller = null, store = null;

                if (JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
                        "Желаете указать дополнительные сведения?\n\n\n" +
                                "(Вы можете указать магазин, который расположен ближе к Вам,\n" +
                                "а так же указать продавца, который консультировал Вас)",
                        "Дополнительные сведения",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == 0) {

                    ObservableList<String[]> list =
                            FXCollections.observableArrayList(dbConnection.createSQL("SELECT NAME, ADDRESS FROM STORES"));
                    list.remove(0);
                    String[] arr = new String[list.size()];
                    for (int i = 0; i < list.size(); i++)
                        arr[i] = list.get(i)[0] + ", " + list.get(i)[1];

                    seller = JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                            "Укажите продавца: ", "Дополнительные сведения",
                            JOptionPane.INFORMATION_MESSAGE, new ImageIcon(""),
                            setItemsCombo("SELECT NAME FROM SELLERS").toArray(), "");
                    store = JOptionPane.showInputDialog(JOptionPane.getRootFrame(),
                            "Укажите магазин: ", "Дополнительные сведения",
                            JOptionPane.INFORMATION_MESSAGE, new ImageIcon(""),
                            arr, "");
                }
                String order = "Заказ оформлен на: " + textClientName.getText() +
                        "\nКонтактные данные: " + textClientPhone.getText() + ", " + textClientEmail.getText() + "\n";

                for (int i = 0; i < tableOrder.getItems().size(); i++) {
                    buttonActionAdd(null,
                            "INSERT INTO ORDERS (DATA, CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE, " +
                                    "ID_INVENTORY, QUANTITY, TOTAL, ID_SELLER, ID_STORE) VALUES ('" +
                                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "', '" +
                                    textClientName.getText() + "', '" +
                                    textClientEmail.getText() + "', '" +
                                    textClientPhone.getText() + "', " +
                                    "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '" +
                                    tableOrder.getItems().get(i)[0] + "'), " +
                                    tableOrder.getItems().get(i)[1] + ", " +
                                    tableOrder.getItems().get(i)[2] + ", " +
                                    ((seller != null) ?
                                            "(SELECT ID_SELLER FROM SELLERS WHERE NAME ='" + seller + "')"
                                            : null) + ", " +
                                    ((store != null) ?
                                            "(SELECT ID_STORE FROM STORES WHERE NAME = '" +
                                                    store.toString().substring(0, store.toString().indexOf(",")) + "')"
                                            : null) + ")");
                    order = order.concat("\nМодель: " + tableOrder.getItems().get(i)[0] +
                            "\nВ количестве: " + tableOrder.getItems().get(i)[1] + " шт.\n");
                }

                if (seller != null) order = order.concat("\nКонсультирующий продавец: " + seller + "\n");
                if (store != null) order = order.concat("Магазин, предоставляющий заказ: " + store + "\n");
                order = order.concat("\nМы свяжемся с Вами по указанным контактным данным в ближайшее время");

                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                        order, "Ваш заказ", JOptionPane.INFORMATION_MESSAGE);
            } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                    "Введите контактные данные", "Ошибка", JOptionPane.ERROR_MESSAGE);
        });

        textSearch.setOnKeyReleased(event -> {
            tableSearch.getColumns().remove(0, tableSearch.getColumns().size());
            String search = textSearch.getText();

            if (Objects.equals(search, ""))
                generateTable(tableSearch, genSearch,
                        new String[]{"", "", "", "Производитель", "Модель", "Диагональ", "Разрешение",
                                "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                        null, null);
            else
                generateTable(tableSearch, genSearch + "WHERE " +
                                "MANUFACTURER CONTAINING '" + search + "' " +
                                "OR MODEL CONTAINING '" + search + "' " +
                                "OR RESOLUTION CONTAINING '" + search + "' " +
                                "OR PROCESSOR CONTAINING '" + search + "' " +
                                "OR VIDEO CONTAINING '" + search + "' " +
                                "OR HDD_SSD CONTAINING '" + search + "' " +
                                "OR DVD CONTAINING '" + search + "' " +
                                "OR OS CONTAINING '" + search + "' " +

                                (search.matches("^\\d*$") || search.contains(".")
                                        ? "OR DIAGONAL CONTAINING '" + search + "' " +
                                        "OR RAM CONTAINING '" + search + "' " +
                                        "OR BATTERY CONTAINING '" + search + "' "
                                        : " ") +

                                "ORDER BY ID_INVENTORY",
                        new String[]{"", "", "", "Производитель", "Модель", "Диагональ", "Разрешение",
                                "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                        null, null);

            tableSearch.getColumns().get(1).setVisible(false);
            tableSearch.getColumns().get(2).setVisible(false);
        });
        tableSearch.setOnMouseClicked(event -> {
            if (!tableSearch.getSelectionModel().isEmpty()) {
                labelSumUnit.setText(tableSearch.getSelectionModel().getSelectedItem()[1]);
                labelModelQuantity.setText(Integer.parseInt(tableSearch.getSelectionModel().getSelectedItem()[2]) >= 3
                        ? "Есть в наличии" : "На складе меньше 3 единиц");
            }
        });
        checkSearchExt.setOnAction(action -> {
            if (checkSearchExt.selectedProperty().getValue()) {
                tableSearch.setLayoutY(355);
                tableSearch.setPrefHeight(295);
                generateTable(tableSearch, genSearch,
                        new String[]{"", "", "", "Производитель", "Модель", "Диагональ", "Разрешение",
                                "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                        null, null);
                tableSearch.getColumns().get(1).setVisible(false);
                tableSearch.getColumns().get(2).setVisible(false);
                textSearch.setText("");
                textSearch.setDisable(true);
                anchorSearch.setVisible(true);
            } else {
                textSearch.setDisable(false);
                tableSearch.setLayoutY(235);
                tableSearch.setPrefHeight(415);

                for (JFXComboBox aComboSearch : comboSearch) aComboSearch.getSelectionModel().clearSelection();
                anchorSearch.setVisible(false);
            }
        });

        tabAdmin.setText("");
        tabAdmin.setDisable(true);
        labelAdmin.setOnMouseClicked(event -> {
            if (!login) {
                labelAdmin.setVisible(false);
                buttonLogin.setVisible(true);
                buttonExit.setVisible(true);
                textLogin.setVisible(true);
                textPassword.setVisible(true);

                labelInfoSum.setVisible(false);
                labelSumUnit.setVisible(false);
                labelModelQuantity.setVisible(false);
                labelInfoSumOrder.setVisible(false);
                labelSumOrder.setVisible(false);
            }
        });
        textLogin.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.getKeyCode("Enter")) login();
        });
        textPassword.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.getKeyCode("Enter")) login();
        });
        buttonLogin.setOnAction(action -> {
            login();
        });
        buttonExit.setOnAction(action -> {
            if (Objects.equals(buttonExit.getText(), "Закрыть")) {
                labelInfoSum.setVisible(true);
                labelSumUnit.setVisible(true);
                labelModelQuantity.setVisible(true);
                labelInfoSumOrder.setVisible(true);
                labelSumOrder.setVisible(true);
            }
            login = false;
            buttonLogin.setVisible(false);
            buttonExit.setLayoutX(630);
            buttonExit.setPrefWidth(62);
            buttonExit.setText("Закрыть");
            buttonExit.setVisible(false);
            textLogin.setText("");
            textLogin.setVisible(false);
            textPassword.setText("");
            textPassword.setVisible(false);
            tabAdmin.setText("");
            tabAdmin.setDisable(true);
            labelAdmin.setText("Вы Администратор?");
            labelAdmin.setVisible(true);
            labelErrorLogin.setVisible(false);
        });

        comboTables.getItems().setAll(
                "-  не выбрано  -", "Инвентарь", "Продажи / Детали", "Продавцы", "Магазины", "Заказы");
        comboTables.setOnAction(action -> {
            anchorQuery.setVisible(true);
            tableQuery.setPrefWidth(690);
            tableQuery.getColumns().remove(0, tableQuery.getColumns().size());
            tableQuery.setDisable(false);
            tableQuerySD.getItems().remove(0, tableQuerySD.getColumns().size());
            tableQuerySD.getColumns().remove(0, tableQuerySD.getColumns().size());
            tableQuerySD.setDisable(true);
            tableQuerySD.setVisible(false);
            buttonAdd.setDisable(false);
            buttonDelete.setDisable(false);
            buttonExec.setDisable(true);
            buttonExec.setVisible(false);
            datePicker.setVisible(false);
            datePicker.setValue(null);
            textAdminSearch.setDisable(false);
            textAdminSearch.setText("");

            ObservableList<JFXTextField> listTextField = FXCollections.observableArrayList(textAdd1, textAdd2, textAdd3,
                    textAdd4, textAdd5, textAdd6, textAdd7, textAdd8, textAdd9, textAdd10, textAdd11, textAdd12, textAdd13);
            ObservableList<JFXComboBox> listComboBox = FXCollections.observableArrayList(combo1, combo2, combo3);

            reCreationListTextField(listTextField, null, null);
            reCreationListComboBoxes(listComboBox, null, null, null);

            switch (comboTables.getSelectionModel().getSelectedIndex()) {

                case 0:
                    textAdminSearch.setDisable(true);
                    anchorQuery.setVisible(false);
                    comboTables.setValue(null);
                    buttonAdd.setDisable(true);
                    buttonDelete.setDisable(true);
                    tableQuery.setDisable(true);
                    break;

                case 1:
                    generateTable(tableQuery,
                            "SELECT * FROM INVENTORY ORDER BY ID_INVENTORY",
                            new String[]{"", "Производитель", "Модель", "Стоимость", "Количество", "Диагональ",
                                    "Разрешение", "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                            new String[]{"SELECT MANUFACTURER FROM INVENTORY", "SELECT DVD FROM INVENTORY"},
                            new int[]{1, 11});
                    reCreationListTextField(listTextField,
                            new JFXTextField[]{textAdd2, textAdd3, textAdd4, textAdd5, textAdd6, textAdd7,
                                    textAdd8, textAdd9, textAdd10, textAdd11, textAdd12, textAdd13},
                            new String[]{"Модель", "Стоимость", "Количество", "Диагональ", "Разрешение",
                                    "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"});
                    reCreationListComboBoxes(listComboBox, new JFXComboBox[]{combo1},
                            new String[]{"SELECT MANUFACTURER FROM INVENTORY"}, new String[]{"Производитель"});
                    valuesSelectedTable(tableQuery, null, null,
                            listTextField, new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13},
                            listComboBox, new int[]{1});
                    editCellTableSQL(tableQuery, "INVENTORY",
                            "ID_INVENTORY", "MANUFACTURER", "MODEL", "VAL", "QUANTITY", "DIAGONAL",
                            "RESOLUTION", "PROCESSOR", "VIDEO", "RAM", "HDD_SSD", "DVD", "BATTERY", "OS");
                    isCorrectValuesIntFloat(listTextField, new int[]{2, 7, 10}, new int[]{1, 3});

                    tableQuery.getColumns().get(11).setPrefWidth(57);
                    listComboBox.get(0).setPrefWidth(120);

                    textAdminSearch.setOnKeyReleased(event -> {
                        tableQuery.getColumns().remove(0, tableQuery.getColumns().size());
                        String search = textAdminSearch.getText();

                        if (Objects.equals(search, ""))
                            generateTable(tableQuery,
                                    "SELECT * FROM INVENTORY ORDER BY ID_INVENTORY",
                                    new String[]{"", "Производитель", "Модель", "Стоимость", "Количество", "Диагональ",
                                            "Разрешение", "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                                    new String[]{"SELECT MANUFACTURER FROM INVENTORY", "SELECT DVD FROM INVENTORY"},
                                    new int[]{1, 11});
                        else
                            generateTable(tableQuery,
                                    "SELECT * FROM INVENTORY WHERE " +
                                            "MANUFACTURER CONTAINING '" + search + "' " +
                                            "OR MODEL CONTAINING '" + search + "' " +
                                            "OR RESOLUTION CONTAINING '" + search + "' " +
                                            "OR PROCESSOR CONTAINING '" + search + "' " +
                                            "OR VIDEO CONTAINING '" + search + "' " +
                                            "OR HDD_SSD CONTAINING '" + search + "' " +
                                            "OR DVD CONTAINING '" + search + "' " +
                                            "OR OS CONTAINING '" + search + "' " +

                                            (search.matches("^\\d*$") || search.contains(".")
                                                    ? "OR VAL CONTAINING '" + search + "' " +
                                                    "OR QUANTITY CONTAINING '" + search + "' " +
                                                    "OR DIAGONAL CONTAINING '" + search + "' " +
                                                    "OR RAM CONTAINING '" + search + "' " +
                                                    "OR BATTERY CONTAINING '" + search + "' "
                                                    : " ") +

                                            "ORDER BY ID_INVENTORY",
                                    new String[]{"", "Производитель", "Модель", "Стоимость", "Количество", "Диагональ",
                                            "Разрешение", "Процессор", "Видео", "RAM", "HDD / SSD", "DVD", "Батарея", "ОС"},
                                    new String[]{"SELECT MANUFACTURER FROM INVENTORY", "SELECT DVD FROM INVENTORY"},
                                    new int[]{1, 11});
                    });
                    buttonAdd.setOnAction(actionAdd -> {
                        if (isSelectedField(listTextField, listComboBox)) {
                            buttonActionAdd(tableQuery,
                                    "INSERT INTO INVENTORY (MANUFACTURER, MODEL, VAL, QUANTITY, DIAGONAL, " +
                                            "RESOLUTION, PROCESSOR, VIDEO, RAM, HDD_SSD, DVD, BATTERY, OS) VALUES ('" +
                                            combo1.getSelectionModel().getSelectedItem() + "', '" + textAdd2.getText() + "', " +
                                            textAdd3.getText() + ", " + textAdd4.getText() + ", " + textAdd5.getText() +
                                            ", '" + textAdd6.getText() + "', '" + textAdd7.getText() + "', '" +
                                            textAdd8.getText() + "', " + textAdd9.getText() + ", '" + textAdd10.getText() +
                                            "', '" + textAdd11.getText() + "', " + textAdd12.getText() + ", '" +
                                            textAdd13.getText() + "')");
                            for (JFXTextField aList : listTextField) aList.setText("");
                            for (JFXComboBox aList : listComboBox) aList.setValue(null);
                        } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Введите данные", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    buttonDelete.setOnAction(actionDelete -> {
                        if (!tableQuery.getSelectionModel().isEmpty())
                            buttonActionDelete(tableQuery, "INVENTORY", "ID_INVENTORY");
                        else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Выберите запись", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    break;

                case 2:
                    tableQuery.setPrefWidth(438);
                    tableQuerySD.setDisable(false);
                    tableQuerySD.setVisible(true);
                    datePicker.setVisible(true);

                    generateTable(tableQuery,
                            "SELECT SA.ID_SALE, SE.NAME, ST.NAME, SA.DATA, SA.TOTAL " +
                                    "FROM SALES SA, SELLERS SE, STORES ST " +
                                    "WHERE SA.ID_SELLER = SE.ID_SELLER " +
                                    "AND SA.ID_STORE = ST.ID_STORE " +
                                    "ORDER BY SA.ID_SALE",
                            new String[]{"", "Продавец", "Магазин", "Дата", "Сумма"},
                            new String[]{"SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                            new int[]{1, 2});
                    generateTable(tableQuerySD,
                            "SELECT SD.ID_SALE, I.MODEL, SD.QUANTITY " +
                                    "FROM INVENTORY I, SALE_DETAILS SD " +
                                    "WHERE I.ID_INVENTORY = SD.ID_INVENTORY " +
                                    "ORDER BY SD.ID_SALE",
                            new String[]{"", "Модель", "Кол-во"},
                            new String[]{"SELECT MODEL FROM INVENTORY"},
                            new int[]{1});
                    reCreationListComboBoxes(listComboBox,
                            new JFXComboBox[]{combo1, combo2, combo3},
                            new String[]{"SELECT NAME FROM SELLERS",
                                    "SELECT NAME FROM STORES", "SELECT MODEL FROM INVENTORY"},
                            new String[]{"Продавец", "Магазин", "Модель"});
                    reCreationListTextField(listTextField,
                            new JFXTextField[]{textAdd3},
                            new String[]{"Количество"});
                    editCellTableSQLSalesSD(new TableView[]{tableQuery, tableQuerySD});

                    isCorrectValuesIntFloat(listTextField, new int[]{0}, (int[]) null);

                    tableQuery.getColumns().get(4).setEditable(false);
                    listComboBox.get(0).setPrefWidth(Region.USE_COMPUTED_SIZE);

                    tableQuery.setOnMouseClicked(actionClick -> {
                        if (!tableQuery.getSelectionModel().isEmpty()) {

                            for (int i = 0; i < tableQuerySD.getItems().size(); i++)
                                if (Objects.equals(tableQuerySD.getItems().get(i)[0], tableQuery.getSelectionModel().getSelectedItem()[0]))
                                    tableQuerySD.getSelectionModel().select(i);

                            datePicker.setValue(LocalDate.parse(tableQuery.getSelectionModel().getSelectedItem()[3]));

                            listComboBox.get(0).setValue(tableQuery.getSelectionModel().getSelectedItem()[1]);
                            listComboBox.get(1).setValue(tableQuery.getSelectionModel().getSelectedItem()[2]);

                            listTextField.get(0).setText(tableQuerySD.getSelectionModel().getSelectedItem()[2]);
                            listComboBox.get(2).setValue(tableQuerySD.getSelectionModel().getSelectedItem()[1]);
                        }
                    });
                    tableQuerySD.setOnMouseClicked(actionClick -> {
                        if (!tableQuerySD.getSelectionModel().isEmpty()) {

                            for (int i = 0; i < tableQuery.getItems().size(); i++)
                                if (Objects.equals(tableQuery.getItems().get(i)[0], tableQuerySD.getSelectionModel().getSelectedItem()[0]))
                                    tableQuery.getSelectionModel().select(i);

                            datePicker.setValue(LocalDate.parse(tableQuery.getSelectionModel().getSelectedItem()[3]));

                            listTextField.get(0).setText(tableQuerySD.getSelectionModel().getSelectedItem()[2]);
                            listComboBox.get(2).setValue(tableQuerySD.getSelectionModel().getSelectedItem()[1]);

                            listComboBox.get(0).setValue(tableQuery.getSelectionModel().getSelectedItem()[1]);
                            listComboBox.get(1).setValue(tableQuery.getSelectionModel().getSelectedItem()[2]);
                        }
                    });

                    textAdminSearch.setOnKeyReleased(event -> {
                        tableQuery.getColumns().remove(0, tableQuery.getColumns().size());
                        tableQuerySD.getColumns().remove(0, tableQuerySD.getColumns().size());

                        String search = textAdminSearch.getText();

                        if (Objects.equals(search, "")) {
                            generateTable(tableQuery,
                                    "SELECT SA.ID_SALE, SE.NAME, ST.NAME, SA.DATA, SA.TOTAL " +
                                            "FROM SALES SA, SELLERS SE, STORES ST " +
                                            "WHERE SA.ID_SELLER = SE.ID_SELLER " +
                                            "AND SA.ID_STORE = ST.ID_STORE " +
                                            "ORDER BY SA.ID_SALE",
                                    new String[]{"", "Продавец", "Магазин", "Дата", "Сумма"},
                                    new String[]{"SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                                    new int[]{1, 2});
                            generateTable(tableQuerySD,
                                    "SELECT SD.ID_SALE, I.MODEL, SD.QUANTITY " +
                                            "FROM INVENTORY I, SALE_DETAILS SD " +
                                            "WHERE I.ID_INVENTORY = SD.ID_INVENTORY " +
                                            "ORDER BY SD.ID_SALE",
                                    new String[]{"", "Модель", "Кол-во"},
                                    new String[]{"SELECT MODEL FROM INVENTORY"},
                                    new int[]{1});
                        } else {
                            generateTable(tableQuery,
                                    "SELECT SA.ID_SALE, SE.NAME, ST.NAME, SA.DATA, SA.TOTAL " +
                                            "FROM SALES SA, SELLERS SE, STORES ST " +
                                            "WHERE SA.ID_SELLER = SE.ID_SELLER " +
                                            "AND SA.ID_STORE = ST.ID_STORE " +
                                            "AND (SE.NAME CONTAINING '" + search + "' " +
                                            "OR ST.NAME CONTAINING '" + search + "' " +

                                            (search.matches("^\\d*$") || search.contains(".")
                                                    ? "OR SA.DATA CONTAINING '" + search + "' " +
                                                    "OR SA.TOTAL CONTAINING '" + search + "' "
                                                    : " ") +

                                            ") ORDER BY SA.ID_SALE",
                                    new String[]{"", "Продавец", "Магазин", "Дата", "Сумма"},
                                    new String[]{"SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                                    new int[]{1, 2});

                            generateTable(tableQuerySD,
                                    "SELECT SD.ID_SALE, I.MODEL, SD.QUANTITY " +
                                            "FROM INVENTORY I, SALE_DETAILS SD " +
                                            "WHERE I.ID_INVENTORY = SD.ID_INVENTORY " +
                                            "AND (I.MODEL CONTAINING '" + search + "' " +

                                            (search.matches("^\\d*$") || search.contains(".")
                                                    ? "OR SD.QUANTITY CONTAINING '" + search + "' "
                                                    : " ") +

                                            ") ORDER BY SD.ID_SALE",
                                    new String[]{"", "Модель", "Кол-во"},
                                    new String[]{"SELECT MODEL FROM INVENTORY"},
                                    new int[]{1});
                        }
                    });
                    buttonAdd.setOnAction(actionAdd -> {
                        if (isSelectedField(listTextField, listComboBox)) {
                            try {
                                dbConnection.connection.prepareStatement(
                                        "INSERT INTO SALES (ID_SELLER, ID_STORE, DATA, TOTAL) VALUES (" +
                                                "(SELECT ID_SELLER FROM SELLERS WHERE NAME = '" +
                                                combo1.getSelectionModel().getSelectedItem() + "'), " +
                                                "(SELECT ID_STORE FROM STORES WHERE NAME = '" +
                                                combo2.getSelectionModel().getSelectedItem() + "'), '" +
                                                datePicker.getValue() + "', " +
                                                "(SELECT VAL FROM INVENTORY WHERE MODEL = '" +
                                                combo3.getSelectionModel().getSelectedItem() + "') * " +
                                                textAdd3.getText() + ")").executeUpdate();
                                dbConnection.connection.prepareStatement(
                                        "INSERT INTO SALE_DETAILS (ID_SALE, ID_INVENTORY, QUANTITY) VALUES (" +
                                                "(SELECT MAX(ID_SALE) FROM SALES), " +
                                                "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '" +
                                                combo3.getSelectionModel().getSelectedItem() + "'), " +
                                                textAdd3.getText() + ")").executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            for (JFXTextField aList : listTextField) aList.setText("");
                            for (JFXComboBox aList : listComboBox) aList.setValue(null);
                            datePicker.setValue(null);
                            tableQuery.getItems().add(FXCollections.observableArrayList(dbConnection.createSQL(
                                    "SELECT SA.ID_SALE, SE.NAME, ST.NAME, SA.DATA, SA.TOTAL " +
                                            "FROM SALES SA, SELLERS SE, STORES ST " +
                                            "WHERE SA.ID_SELLER = SE.ID_SELLER " +
                                            "AND SA.ID_STORE = ST.ID_STORE " +
                                            "AND SA.ID_SALE = (SELECT MAX(ID_SALE) FROM SALES)")).get(1));
                            tableQuerySD.getItems().add(FXCollections.observableArrayList(dbConnection.createSQL(
                                    "SELECT SD.ID_SALE, I.MODEL, SD.QUANTITY " +
                                            "FROM SALE_DETAILS SD, INVENTORY I " +
                                            "WHERE SD.ID_INVENTORY = I.ID_INVENTORY " +
                                            "AND SD.ID_SALE = (SELECT MAX(ID_SALE) FROM SALE_DETAILS)")).get(1));
                        } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Введите данные", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    buttonDelete.setOnAction(actionDelete -> {
                        if (!tableQuery.getSelectionModel().isEmpty() || !tableQuerySD.getSelectionModel().isEmpty()) {
                            try {
                                dbConnection.connection.prepareStatement("DELETE FROM SALE_DETAILS WHERE ID_SALE = '" +
                                        tableQuerySD.getSelectionModel().getSelectedItem()[0] + "'").executeUpdate();
                                dbConnection.connection.prepareStatement("DELETE FROM SALES WHERE ID_SALE = '" +
                                        tableQuery.getSelectionModel().getSelectedItem()[0] + "'").executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            tableQuerySD.getItems().remove(tableQuerySD.getSelectionModel().getSelectedIndex());
                            tableQuery.getItems().remove(tableQuery.getSelectionModel().getSelectedIndex());
                            tableQuerySD.getSelectionModel().clearSelection();
                            tableQuery.getSelectionModel().clearSelection();
                        } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Выберите запись", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    break;

                case 3:
                    generateTable(tableQuery,
                            "SELECT * FROM SELLERS ORDER BY ID_SELLER",
                            new String[]{"", "ФИО", "Телефон", "Стаж", "Ставка"},
                            null, null);
                    reCreationListTextField(listTextField,
                            new JFXTextField[]{textAdd1, textAdd2, textAdd3, textAdd4},
                            new String[]{"ФИО", "Телефон", "Стаж", "Ставка"});
                    valuesSelectedTable(tableQuery, null, null,
                            listTextField, new int[]{1, 2, 3, 4}, null, null);
                    editCellTableSQL(tableQuery, "SELLERS",
                            "ID_SELLER", "NAME", "TELEPHONE", "EXPERIENCE", "SALARY");
                    isCorrectValuesIntFloat(listTextField, new int[]{1, 2}, new int[]{3});

                    textAdminSearch.setOnKeyReleased(event -> {
                        tableQuery.getColumns().remove(0, tableQuery.getColumns().size());
                        String search = textAdminSearch.getText();

                        if (Objects.equals(search, ""))
                            generateTable(tableQuery,
                                    "SELECT * FROM SELLERS ORDER BY ID_SELLER",
                                    new String[]{"", "ФИО", "Телефон", "Стаж", "Ставка"},
                                    null, null);
                        else
                            generateTable(tableQuery,
                                    "SELECT * FROM SELLERS WHERE " +
                                            "NAME CONTAINING '" + search + "' " +

                                            (search.matches("^\\d*$") || search.contains(".")
                                                    ? "OR TELEPHONE CONTAINING '" + search + "' " +
                                                    "OR EXPERIENCE CONTAINING '" + search + "' " +
                                                    "OR SALARY CONTAINING '" + search + "' "
                                                    : " ") +

                                            "ORDER BY ID_SELLER",
                                    new String[]{"", "ФИО", "Телефон", "Стаж", "Ставка"},
                                    null, null);
                    });
                    buttonAdd.setOnAction(actionAdd -> {
                        if (isSelectedField(listTextField, null)) {
                            buttonActionAdd(tableQuery,
                                    "INSERT INTO SELLERS(NAME, TELEPHONE, EXPERIENCE, SALARY) VALUES ('" +
                                            textAdd1.getText() + "', " + textAdd2.getText() + ", " +
                                            textAdd3.getText() + ", " + textAdd4.getText() + ")");
                            for (JFXTextField aList : listTextField) aList.setText("");
                        } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Введите данные", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    buttonDelete.setOnAction(actionDelete -> {
                        if (!tableQuery.getSelectionModel().isEmpty())
                            buttonActionDelete(tableQuery, "SELLERS", "ID_SELLER");
                        else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Выберите запись", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    break;

                case 4:
                    generateTable(tableQuery,
                            "SELECT * FROM STORES ORDER BY ID_STORE",
                            new String[]{"", "Название", "Адрес", "Телефон"},
                            null, null);
                    reCreationListTextField(listTextField,
                            new JFXTextField[]{textAdd1, textAdd2, textAdd3},
                            new String[]{"Название", "Адрес", "Телефон"});
                    valuesSelectedTable(tableQuery, null, null,
                            listTextField, new int[]{1, 2, 3}, null, null);
                    editCellTableSQL(tableQuery, "STORES",
                            "ID_STORE", "NAME", "ADDRESS", "TELEPHONE");
                    isCorrectValuesIntFloat(listTextField, new int[]{2}, (int[]) null);

                    textAdminSearch.setOnKeyReleased(event -> {
                        tableQuery.getColumns().remove(0, tableQuery.getColumns().size());
                        String search = textAdminSearch.getText();

                        if (Objects.equals(search, ""))
                            generateTable(tableQuery,
                                    "SELECT * FROM STORES ORDER BY ID_STORE",
                                    new String[]{"", "Название", "Адрес", "Телефон"},
                                    null, null);
                        else
                            generateTable(tableQuery,
                                    "SELECT * FROM STORES WHERE " +
                                            "NAME CONTAINING '" + search + "' " +
                                            "OR ADDRESS CONTAINING '" + search + "' " +

                                            (search.matches("^\\d*$") || search.contains(".")
                                                    ? "OR TELEPHONE CONTAINING '" + search + "' "
                                                    : " ") +

                                            "ORDER BY ID_STORE",
                                    new String[]{"", "Название", "Адрес", "Телефон"},
                                    null, null);
                    });
                    buttonAdd.setOnAction(actionAdd -> {
                        if (isSelectedField(listTextField, null)) {
                            buttonActionAdd(tableQuery,
                                    "INSERT INTO STORES(NAME, ADDRESS, TELEPHONE) VALUES ('" +
                                            textAdd1.getText() + "', '" + textAdd2.getText() + "', " + textAdd3.getText() + ")");
                            for (JFXTextField aList : listTextField) aList.setText("");
                        } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Введите данные", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    buttonDelete.setOnAction(actionDelete -> {
                        if (!tableQuery.getSelectionModel().isEmpty())
                            buttonActionDelete(tableQuery, "STORES", "ID_STORE");
                        else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Выберите запись", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    break;

                case 5:
                    datePicker.setVisible(true);
                    generateTable(tableQuery,
                            "SELECT O.ID_ORDER, O.DATA, O.CLIENT_NAME, O.CLIENT_EMAIL, O.CLIENT_PHONE, " +
                                    "I.MODEL, O.QUANTITY, O.TOTAL, SE.NAME, ST.NAME " +
                                    "FROM ORDERS O, INVENTORY I, SELLERS SE, STORES ST " +
                                    "WHERE O.ID_INVENTORY = I.ID_INVENTORY " +
                                    "AND O.ID_SELLER = SE.ID_SELLER " +
                                    "AND O.ID_STORE = ST.ID_STORE " +
                                    "ORDER BY ID_ORDER",
                            new String[]{"", "Дата", "Заказчик", "Email", "Телефон",
                                    "Модель", "Кол-во", "Сумма", "Продавец", "Магазин"},
                            new String[]{"SELECT MODEL FROM INVENTORY",
                                    "SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                            new int[]{5, 8, 9});
                    tableQuery.getColumns().get(7).setEditable(false);
                    reCreationListTextField(listTextField,
                            new JFXTextField[]{textAdd3, textAdd4, textAdd8, textAdd9},
                            new String[]{"Заказчик", "Email", "Телефон", "Количество"});
                    reCreationListComboBoxes(listComboBox,
                            new JFXComboBox[]{combo1, combo2, combo3},
                            new String[]{"SELECT MODEL FROM INVENTORY",
                                    "SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                            new String[]{"Модель", "Продавец", "Магазин"});
                    valuesSelectedTable(tableQuery,
                            datePicker, 1,
                            listTextField, new int[]{2, 3, 4, 6},
                            listComboBox, new int[]{5, 8, 9});
                    editCellTableSQLOrders(tableQuery);

                    isCorrectValuesIntFloat(listTextField, new int[]{2, 3}, (int[]) null);

                    buttonExec.setDisable(false);
                    buttonExec.setVisible(true);

                    textAdminSearch.setOnKeyReleased(event -> {
                        tableQuery.getColumns().remove(0, tableQuery.getColumns().size());
                        String search = textAdminSearch.getText();

                        if (Objects.equals(search, ""))
                            generateTable(tableQuery,
                                    "SELECT O.ID_ORDER, O.DATA, O.CLIENT_NAME, O.CLIENT_EMAIL, " +
                                            "O.CLIENT_PHONE, I.MODEL, O.QUANTITY, O.TOTAL, SE.NAME, ST.NAME " +
                                            "FROM ORDERS O, INVENTORY I, SELLERS SE, STORES ST " +
                                            "WHERE O.ID_INVENTORY = I.ID_INVENTORY " +
                                            "AND O.ID_SELLER = SE.ID_SELLER " +
                                            "AND O.ID_STORE = ST.ID_STORE " +
                                            "ORDER BY ID_ORDER",
                                    new String[]{"", "Дата", "Заказчик", "Email", "Телефон",
                                            "Модель", "Кол-во", "Сумма", "Продавец", "Магазин"},
                                    new String[]{"SELECT MODEL FROM INVENTORY",
                                            "SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                                    new int[]{5, 8, 9});
                        else
                            generateTable(tableQuery,
                                    "SELECT O.ID_ORDER, O.DATA, O.CLIENT_NAME, O.CLIENT_EMAIL, O.CLIENT_PHONE, " +
                                            "I.MODEL, O.QUANTITY, O.TOTAL, SE.NAME, ST.NAME " +
                                            "FROM ORDERS O, INVENTORY I, SELLERS SE, STORES ST " +
                                            "WHERE O.ID_INVENTORY = I.ID_INVENTORY " +
                                            "AND O.ID_SELLER = SE.ID_SELLER " +
                                            "AND O.ID_STORE = ST.ID_STORE " +
                                            "AND (O.CLIENT_NAME CONTAINING '" + search + "' " +
                                            "OR O.CLIENT_EMAIL CONTAINING '" + search + "' " +
                                            "OR I.MODEL CONTAINING '" + search + "' " +
                                            "OR SE.NAME CONTAINING '" + search + "' " +
                                            "OR ST.NAME CONTAINING '" + search + "' " +

                                            (search.matches("^\\d*$") || search.contains(".")
                                                    ? "OR O.DATA CONTAINING '" + search + "' " +
                                                    "OR O.CLIENT_PHONE CONTAINING '" + search + "' " +
                                                    "OR O.QUANTITY CONTAINING '" + search + "' " +
                                                    "OR O.TOTAL CONTAINING '" + search + "' "
                                                    : " ") +

                                            ") ORDER BY ID_ORDER",
                                    new String[]{"", "Дата", "Заказчик", "Email", "Телефон",
                                            "Модель", "Кол-во", "Сумма", "Продавец", "Магазин"},
                                    new String[]{"SELECT MODEL FROM INVENTORY",
                                            "SELECT NAME FROM SELLERS", "SELECT NAME FROM STORES"},
                                    new int[]{5, 8, 9});
                    });
                    buttonExec.setOnAction(actionExec -> {
                        if (!tableQuery.getSelectionModel().isEmpty()) {
                            buttonActionAdd(null,
                                    "INSERT INTO SALES(ID_SELLER, ID_STORE, DATA, TOTAL) VALUES (" +
                                            "(SELECT ID_SELLER FROM SELLERS WHERE NAME = '" + combo2.getSelectionModel().getSelectedItem() + "'), " +
                                            "(SELECT ID_STORE FROM STORES WHERE NAME = '" + combo3.getSelectionModel().getSelectedItem() + "'), '" +
                                            datePicker.getValue() + "', " +
                                            tableQuery.getSelectionModel().getSelectedItem()[7] + ")");
                            buttonActionAdd(null,
                                    "INSERT INTO SALE_DETAILS(ID_SALE, ID_INVENTORY, QUANTITY) VALUES (" +
                                            "(SELECT MAX(ID_SALE) FROM SALES), " +
                                            "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '" +
                                            combo1.getSelectionModel().getSelectedItem() + "'), " +
                                            textAdd9.getText() + ")");
                            try {
                                dbConnection.connection.prepareStatement(
                                        "UPDATE INVENTORY " +
                                                "SET QUANTITY = (QUANTITY - " + textAdd9.getText() + ") " +
                                                " WHERE ID_INVENTORY = " +
                                                "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '" +
                                                combo1.getSelectionModel().getSelectedItem() + "')").executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            buttonActionDelete(tableQuery, "ORDERS", "ID_ORDER");

                            for (JFXTextField aList : listTextField) aList.setText("");
                            for (JFXComboBox aList : listComboBox) aList.setValue(null);
                            datePicker.setValue(null);

                        } else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Выберите запись", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    buttonAdd.setOnAction(actionAdd -> {
                        if (isSelectedField(listTextField, listComboBox)) {
                            buttonActionAdd(tableQuery,
                                    "INSERT INTO ORDERS(DATA, CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE, " +
                                            "ID_INVENTORY, QUANTITY, TOTAL, ID_SELLER, ID_STORE) VALUES('" +
                                            datePicker.getValue() + "', '" +
                                            textAdd4.getText() + "', '" +
                                            textAdd8.getText() + "', " +
                                            textAdd9.getText() + ", " +
                                            "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '" +
                                            combo1.getSelectionModel().getSelectedItem() + "'), " +
                                            textAdd9.getText() + ", " +
                                            "(SELECT VAL FROM INVENTORY WHERE MODEL = '" +
                                            combo1.getSelectionModel().getSelectedItem() + "') * " +
                                            textAdd9.getText() + ", " +
                                            "(SELECT ID_SELLER FROM SELLERS WHERE NAME = '" +
                                            combo2.getSelectionModel().getSelectedItem() + "'), " +
                                            "(SELECT ID_STORE FROM STORES WHERE NAME = '" +
                                            combo3.getSelectionModel().getSelectedItem() + "'))");

                            tableQuery.getItems().set(tableQuery.getItems().size() - 1, new String[]{
                                    tableQuery.getItems().get(tableQuery.getItems().size() - 1)[0],
                                    datePicker.getValue().toString(),
                                    textAdd3.getText(), textAdd4.getText(), textAdd8.getText(),
                                    combo1.getSelectionModel().getSelectedItem(),
                                    textAdd9.getText(),
                                    tableQuery.getItems().get(tableQuery.getItems().size() - 1)[7],
                                    combo2.getSelectionModel().getSelectedItem(),
                                    combo3.getSelectionModel().getSelectedItem(),});
                            for (JFXTextField aList : listTextField) aList.setText("");
                            for (JFXComboBox aList : listComboBox) aList.setValue(null);
                            datePicker.setValue(null);
                        }
                    });
                    buttonDelete.setOnAction(actionDelete -> {
                        if (!tableQuery.getItems().isEmpty())
                            buttonActionDelete(tableQuery, "ORDERS", "ID_ORDER");
                        else JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                "Выберите запись", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    });
                    break;
            }
        });
    }

    private void editCellTableSQL(TableView<String[]> table, String tableName, String... field) {
        for (int i = 1; i < table.getColumns().size(); i++) {
            int finalI = i;
            table.getColumns().get(i).setOnEditCommit(actionEdit -> {
                table.getFocusModel().getFocusedItem()[finalI] = String.valueOf(actionEdit.getNewValue());
                try {
                    dbConnection.connection.prepareStatement(
                            "UPDATE " + tableName +
                                    " SET " + field[finalI] + " = '" + table.getFocusModel().getFocusedItem()[finalI] +
                                    "' WHERE " + field[0] + " = '" + table.getFocusModel().getFocusedItem()[0] + "'"
                    ).executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void editCellTableSQLSalesSD(TableView<String[]>[] table) {
        String[][] field = new String[][]{{
                "ID_SALE", "ID_SELLER", "ID_STORE", "DATA"},
                {"ID_SALE", "ID_INVENTORY", "QUANTITY"}};


        for (int j = 1; j < table[0].getColumns().size(); j++) {
            int finalJ = j;
            table[0].getColumns().get(j).setOnEditCommit(actionEdit -> {
                table[0].getFocusModel().getFocusedItem()[finalJ] = String.valueOf(actionEdit.getNewValue());
                try {
                    dbConnection.connection.prepareStatement(
                            "UPDATE SALES " +
                                    "SET " + field[0][finalJ] + " = " +
                                    (Objects.equals(field[0][finalJ], "ID_SELLER")
                                            ? "(SELECT ID_SELLER FROM SELLERS WHERE NAME = '"
                                            + table[0].getFocusModel().getFocusedItem()[finalJ] + "')"
                                            : Objects.equals(field[0][finalJ], "ID_STORE")
                                            ? "(SELECT ID_STORE FROM STORES WHERE NAME = '"
                                            + table[0].getFocusModel().getFocusedItem()[finalJ] + "')"
                                            : ("'" + table[0].getFocusModel().getFocusedItem()[finalJ] + "'"))
                                    + " WHERE " + field[0][0] + " = '" + table[0].getFocusModel().getFocusedItem()[0] + "'"
                    ).executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int j = 1; j < table[1].getColumns().size(); j++) {
            int finalJ = j;
            table[1].getColumns().get(j).setOnEditCommit(actionEdit -> {
                table[1].getFocusModel().getFocusedItem()[finalJ] = String.valueOf(actionEdit.getNewValue());
                try {
                    dbConnection.connection.prepareStatement(
                            "UPDATE SALE_DETAILS " +
                                    "SET " + field[1][finalJ] + " = " +
                                    (Objects.equals(field[1][finalJ], "ID_INVENTORY")
                                            ? "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '"
                                            + table[1].getFocusModel().getFocusedItem()[finalJ] + "')"
                                            : ("'" + table[1].getFocusModel().getFocusedItem()[finalJ] + "'"))
                                    + " WHERE " + field[1][0] + " = '" + table[1].getFocusModel().getFocusedItem()[0] + "'"
                    ).executeUpdate();

                    dbConnection.connection.prepareStatement(
                            "UPDATE SALES SET TOTAL = (SELECT VAL FROM INVENTORY WHERE MODEL = '" +
                                    table[1].getFocusModel().getFocusedItem()[1] + "') * " +
                                    table[1].getFocusModel().getFocusedItem()[2] +
                                    " WHERE " + field[1][0] + " = '" + table[1].getFocusModel().getFocusedItem()[0] + "'"
                    ).executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                table[0].getSelectionModel().getSelectedItem()[4] = String.valueOf(
                        FXCollections.observableArrayList(dbConnection.createSQL
                                ("SELECT TOTAL FROM SALES WHERE ID_SALE = '"
                                        + table[1].getFocusModel().getFocusedItem()[0] + "'")).get(1)[0]);
                table[0].refresh();
            });
        }
    }

    private void editCellTableSQLOrders(TableView<String[]> table) {
        String[] field = new String[]{"ID_ORDER", "DATA", "CLIENT_NAME", "CLIENT_EMAIL", "CLIENT_PHONE",
                "ID_INVENTORY", "QUANTITY", "TOTAL", "ID_SELLER", "ID_STORE"};

        for (int i = 1; i < table.getColumns().size(); i++) {
            int finalI = i;
            table.getColumns().get(i).setOnEditCommit(actionEdit -> {
                table.getFocusModel().getFocusedItem()[finalI] = String.valueOf(actionEdit.getNewValue());
                try {
                    dbConnection.connection.prepareStatement(
                            "UPDATE ORDERS SET " + field[finalI] + " = " +
                                    (Objects.equals(field[finalI], "ID_INVENTORY")
                                            ? "(SELECT ID_INVENTORY FROM INVENTORY WHERE MODEL = '"
                                            + table.getFocusModel().getFocusedItem()[finalI] + "')"
                                            : Objects.equals(field[finalI], "ID_SELLER")
                                            ? "(SELECT ID_SELLER FROM SELLERS WHERE NAME = '"
                                            + table.getFocusModel().getFocusedItem()[finalI] + "')"
                                            : Objects.equals(field[finalI], "ID_STORE")
                                            ? "(SELECT ID_STORE FROM STORES WHERE NAME = '"
                                            + table.getFocusModel().getFocusedItem()[finalI] + "')"
                                            : ("'" + table.getFocusModel().getFocusedItem()[finalI] + "'"))
                                    + " WHERE " + field[0] + " = '" + table.getFocusModel().getFocusedItem()[0] + "'"
                    ).executeUpdate();
                    if (Objects.equals(field[finalI], "ID_INVENTORY")
                            || Objects.equals(field[finalI], "QUANTITY")) {
                        dbConnection.connection.prepareStatement(
                                "UPDATE ORDERS SET TOTAL = (SELECT VAL FROM INVENTORY WHERE MODEL = '" +
                                        table.getFocusModel().getFocusedItem()[5] + "') * " +
                                        table.getFocusModel().getFocusedItem()[6] +
                                        " WHERE " + field[0] + " = '" + table.getFocusModel().getFocusedItem()[0] + "'"
                        ).executeUpdate();
                        table.getSelectionModel().getSelectedItem()[7] = String.valueOf(
                                FXCollections.observableArrayList(dbConnection.createSQL
                                        ("SELECT TOTAL FROM ORDERS WHERE ID_ORDER = '"
                                                + table.getFocusModel().getFocusedItem()[0] + "'")).get(1)[0]);
                        table.refresh();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void generateTable(TableView<String[]> table, @Language("SQL") String sqlGenTable, String[] colName,
                               String[] sqlGenCombo, int[] columnIndex) {
        for (int i = 0, j = 0; i < dbConnection.createSQL(sqlGenTable)[0].length; i++) {
            TableColumn<String[], String> tableColumn = new TableColumn<>(colName[i]);
            final int col = i;
            tableColumn.setCellValueFactory(
                    (CellDataFeatures<String[], String> param) -> new SimpleStringProperty(param.getValue()[col]));

            boolean combo = false;

            if (sqlGenCombo == null && columnIndex == null) {
                tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                tableColumn.setEditable(true);
            } else
                for (int aColumnIndex : columnIndex)
                    if (i == aColumnIndex) {
                        combo = true;
                        break;
                    }
            if (combo) tableColumn.setCellFactory(ComboBoxTableCell.forTableColumn(setItemsCombo(sqlGenCombo[j++])));
            else tableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            table.getColumns().add(tableColumn);
        }
        table.setItems(FXCollections.observableArrayList(dbConnection.createSQL(sqlGenTable)));
        table.getItems().remove(0);
        table.getColumns().get(0).setVisible(false);
        table.setEditable(true);
    }

    private void generateTableOrder(TableView<String[]> table, String[] colName) {
        for (int i = 0; i < 3; i++) {
            TableColumn<String[], String> tableColumn = new TableColumn<>(colName[i]);
            final int col = i;
            tableColumn.setCellValueFactory(
                    (CellDataFeatures<String[], String> param) -> new SimpleStringProperty(param.getValue()[col]));
            table.getColumns().add(tableColumn);
            table.getColumns().get(0).setPrefWidth(180);
        }
    }

    private void buttonActionAdd(TableView<String[]> table, @Language("SQL") String sqlQuery) {
        try {
            dbConnection.connection.prepareStatement(sqlQuery).executeUpdate();
        } catch (SQLException e2) {
            e2.printStackTrace();
        }
        if (table != null)
            table.getItems().add(FXCollections.observableArrayList(dbConnection.createSQL(
                    "SELECT * FROM " + String.valueOf(sqlQuery.subSequence(12, sqlQuery.indexOf("("))) +
                            " WHERE ID_" + String.valueOf(sqlQuery.subSequence(12, sqlQuery.indexOf("(") - 1)) +
                            " = (SELECT MAX(ID_" + String.valueOf(sqlQuery.subSequence(12, sqlQuery.indexOf("(") - 1)) +
                            ") FROM " + String.valueOf(sqlQuery.subSequence(12, sqlQuery.indexOf("("))) + ")"
            )).get(1));
    }

    private void buttonActionDelete(TableView<String[]> table, String tableName, String fieldName) {
        if (!Objects.equals(tableName, "ORDERS") &&
                FXCollections.observableArrayList(dbConnection.createSQL(
                        "SELECT " + fieldName + " FROM " +
                                (Objects.equals(tableName, "INVENTORY") ? "SALE_DETAILS" : "SALES") +
                                " WHERE " + fieldName +
                                " = '" + table.getSelectionModel().getSelectedItem()[0] + "'")).size() > 1)
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                    "Выбранную запись удалить невозможно.\n" +
                            "(запись связана с таблицами \"Продажи\" и/или \"Детали продаж\")",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        else {
            try {
                dbConnection.connection.prepareStatement(
                        "DELETE FROM " + tableName + " WHERE " + fieldName +
                                " = '" + table.getSelectionModel().getSelectedItem()[0] + "'").executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            table.getItems().remove(table.getSelectionModel().getSelectedIndex());
            table.getSelectionModel().clearSelection();
        }
    }

    private void reCreationListTextField(ObservableList<JFXTextField> list, JFXTextField[] text, String[] prompt) {
        for (JFXTextField aList : list) {
            aList.setVisible(false);
            aList.setText("");
            aList.setOnKeyReleased(null);
        }
        list.remove(0, list.size());
        if (text != null && prompt != null) {
            list.addAll(text);
            int i = 0;

            for (JFXTextField aText : list) {
                aText.setPromptText(prompt[i++]);
                aText.setVisible(true);
            }
        }
    }

    private void reCreationListComboBoxes(ObservableList<JFXComboBox> list, JFXComboBox[] combo, String[] sqlGenTable,
                                          String[] prompt) {
        for (JFXComboBox aList : list) {
            aList.getItems().remove(0, aList.getItems().size());
            aList.setVisible(false);
        }
        list.remove(0, list.size());
        if (combo != null && prompt != null) {
            list.addAll(combo);

            for (int i = 0; i < list.size(); i++) {
                list.get(i).getItems().addAll(setItemsCombo(sqlGenTable[i]));
                list.get(i).setPromptText(prompt[i]);
                list.get(i).setVisible(true);
            }
        }
    }

    private void valuesSelectedTable(TableView<String[]> table, JFXDatePicker date, Integer indSelectDate,
                                     ObservableList<JFXTextField> listText, int[] indColSelectText,
                                     ObservableList<JFXComboBox> listCombo, int[] indColSelectCombo) {
        table.setOnMouseClicked(action -> {

            if (!table.getSelectionModel().isEmpty()) {

                if (date != null && indSelectDate != null) {
                    for (int i = 0; i < table.getColumns().size(); i++)
                        if (i == indSelectDate)
                            date.setValue(LocalDate.parse(table.getSelectionModel().getSelectedItem()[i]));
                }

                if (listText != null && indColSelectText != null)
                    for (int t = 0; t < listText.size(); t++)
                        for (int j = 0; j < table.getColumns().size(); j++)
                            if (j == indColSelectText[t])
                                listText.get(t).setText(table.getSelectionModel().getSelectedItem()[j]);

                if (listCombo != null && indColSelectCombo != null)
                    for (int c = 0; c < listCombo.size(); c++)
                        for (int j = 0; j < table.getColumns().size(); j++)
                            if (j == indColSelectCombo[c])
                                listCombo.get(c).setValue(table.getSelectionModel().getSelectedItem()[j]);
            }
        });
    }

    private ObservableList setItemsCombo(@Language("SQL") String sqlGenTable) {
        String[] values = new String[FXCollections.observableArrayList(dbConnection.createSQL(sqlGenTable)).size() - 1];
        for (int i = 1; i <= values.length; i++)
            values[i - 1] = FXCollections.observableArrayList(dbConnection.createSQL(sqlGenTable)).get(i)[0];
        return FXCollections.observableArrayList(new HashSet<String>(Arrays.asList(values))).sorted();
    }

    private void login() {
        labelErrorLogin.setVisible(false);
        if (Objects.equals(textLogin.getText(), "SYSDBA")
                && Objects.equals(textPassword.getText(), "masterkey")) {
            login = true;

            labelAdmin.setVisible(true);
            labelAdmin.setText("Здравствуйте, " + textLogin.getText() + "!");

            tabAdmin.setText("Администратор");
            tabAdmin.setDisable(false);

            textLogin.setText("");
            textLogin.setVisible(false);

            textPassword.setText("");
            textPassword.setVisible(false);

            buttonExit.setLayoutX(580);
            buttonExit.setPrefWidth(110);
            buttonExit.setText("Выйти из системы");
            buttonLogin.setVisible(false);

            labelInfoSum.setVisible(true);
            labelSumUnit.setVisible(true);
            labelModelQuantity.setVisible(true);
            labelInfoSumOrder.setVisible(true);
            labelSumOrder.setVisible(true);
        } else {
            textLogin.setText("");
            textPassword.setText("");

            labelErrorLogin.setText("Неверный логин или пароль");
            labelErrorLogin.setVisible(true);
        }
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isNumeric(String s) {
        return Pattern.compile("^\\d+(?:\\.\\d+)?$").matcher(s).matches();
    }

    private boolean isSelectedField(ObservableList<JFXTextField> listText, ObservableList<JFXComboBox> listCombo) {

        for (int i = 1; i <= listText.size(); i++)
            if (Objects.equals(listText.get(i - 1).getText(), "")) return false;

        if (listCombo != null)
            for (int i = 1; i <= listCombo.size(); i++)
                if (Objects.equals(listCombo.get(i - 1).getSelectionModel().getSelectedItem(), "")) return false;

        return true;
    }

    private void isCorrectValuesIntFloat(ObservableList<JFXTextField> list, int[] indexInt, int[] indexFloat) {

        if (indexInt != null)
            for (int i = 0; i < list.size(); i++)
                for (int anIndexInt : indexInt)
                    if (i == anIndexInt) {
                        int finalI = i;
                        list.get(i).setOnKeyReleased(event -> {
                            if (!isInteger(list.get(finalI).getText()))
                                list.get(finalI).deletePreviousChar();
                        });
                    }

        if (indexFloat != null)
            for (int i = 0; i < list.size(); i++)
                for (int anIndexFloat : indexFloat)
                    if (i == anIndexFloat) {
                        int finalI = i;
                        list.get(i).setOnKeyReleased(event -> {
                            if (!isFloat(list.get(finalI).getText()))
                                list.get(finalI).deletePreviousChar();
                        });
                    }
    }
}