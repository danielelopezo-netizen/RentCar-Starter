package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import edu.rentcar.builder.ReservationRequestBuilder;
import edu.rentcar.composite.ServiceCatalog;
import edu.rentcar.composite.ServiceComponent;
import edu.rentcar.facade.RentalFacade;
import edu.rentcar.factory.AssistancePolicy;
import edu.rentcar.factory.CoveragePolicy;
import edu.rentcar.factory.RatePolicy;
import edu.rentcar.factory.RentalModeFactory;
import edu.rentcar.factory.RentalModeFactoryProvider;
import edu.rentcar.model.Customer;
import edu.rentcar.model.PricingBreakdown;
import edu.rentcar.model.RentalMode;
import edu.rentcar.model.RentalOperationResult;
import edu.rentcar.model.Reservation;
import edu.rentcar.model.ReservationRequest;
import edu.rentcar.model.ReservationStatus;
import edu.rentcar.model.Vehicle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * JavaFX Controller for the Rental Reservation view.
 * Delegates request construction to {@link ReservationRequestBuilder}, modality inspection to
 * {@link RentalModeFactoryProvider}, and workflow execution to {@link RentalFacade}.
 */
public class ReservationController {
    private final RentalFacade rentalFacade = ApplicationServices.rentalFacade();
    private boolean processingOperation = false;

    @FXML private ComboBox<Customer> customerCombo;
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<RentalMode> rentalModeCombo;
    @FXML private ComboBox<String> paymentTokenCombo;

    @FXML private Label availabilityStatusLabel;

    @FXML private CheckBox gpsCheckBox;
    @FXML private CheckBox childSeatCheckBox;
    @FXML private CheckBox additionalDriverCheckBox;
    @FXML private CheckBox wifiCheckBox;
    @FXML private CheckBox tollPassCheckBox;

    @FXML private CheckBox familyPackageCheckBox;
    @FXML private CheckBox executivePackageCheckBox;
    @FXML private CheckBox allInclusivePackageCheckBox;

    @FXML private Label ratePolicyLabel;
    @FXML private Label coveragePolicyLabel;
    @FXML private Label assistancePolicyLabel;
    @FXML private Label includedServicesLabel;

    @FXML private Label rentalDaysLabel;
    @FXML private Label baseRateCostLabel;
    @FXML private Label modalityFeeCostLabel;
    @FXML private Label coverageCostLabel;
    @FXML private Label assistanceCostLabel;
    @FXML private Label singleServicesCostLabel;
    @FXML private Label packagesCostLabel;
    @FXML private Label totalCostLabel;
    @FXML private Label requestDescriptionLabel;

    @FXML private Button calculateButton;
    @FXML private Button confirmRentalButton;
    @FXML private Label operationResultBanner;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> resIdColumn;
    @FXML private TableColumn<Reservation, String> resCustomerColumn;
    @FXML private TableColumn<Reservation, String> resVehicleColumn;
    @FXML private TableColumn<Reservation, LocalDate> resStartColumn;
    @FXML private TableColumn<Reservation, LocalDate> resEndColumn;
    @FXML private TableColumn<Reservation, String> resModeColumn;
    @FXML private TableColumn<Reservation, BigDecimal> resTotalColumn;
    @FXML private TableColumn<Reservation, String> resStatusColumn;
    @FXML private TableColumn<Reservation, String> resMessageColumn;

    @FXML
    public void initialize() {
        configureComboBoxes();
        configureReservationTable();
        configureListeners();
        resetForm();
        refreshReservationTable();
    }

    private void configureComboBoxes() {
        customerCombo.getItems().setAll(ApplicationServices.customerRepository().findAll());
        customerCombo.setCellFactory(listView -> createCustomerCell());
        customerCombo.setButtonCell(createCustomerCell());

        vehicleCombo.getItems().setAll(ApplicationServices.vehicleService().getAllVehicles());
        vehicleCombo.setCellFactory(listView -> createVehicleCell());
        vehicleCombo.setButtonCell(createVehicleCell());

        rentalModeCombo.getItems().setAll(RentalMode.values());

        paymentTokenCombo.getItems().setAll(
                "tok_approved",
                "tok_declined",
                "tok_api_error"
        );
    }

    private void configureReservationTable() {
        resIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        resCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        resVehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleName"));
        resStartColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        resEndColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        resModeColumn.setCellValueFactory(new PropertyValueFactory<>("modalityName"));
        resTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        resStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        resMessageColumn.setCellValueFactory(new PropertyValueFactory<>("statusMessage"));
    }

    private void configureListeners() {
        customerCombo.valueProperty().addListener((obs, oldV, newV) -> updateSummaryAndPreview());
        vehicleCombo.valueProperty().addListener((obs, oldV, newV) -> {
            updateSummaryAndPreview();
            updateAvailabilityIndicator();
        });
        startDatePicker.valueProperty().addListener((obs, oldV, newV) -> {
            updateSummaryAndPreview();
            updateAvailabilityIndicator();
        });
        endDatePicker.valueProperty().addListener((obs, oldV, newV) -> {
            updateSummaryAndPreview();
            updateAvailabilityIndicator();
        });
        rentalModeCombo.valueProperty().addListener((obs, oldV, newV) -> {
            updateModalityDetails(newV);
            updateSummaryAndPreview();
        });

        List<CheckBox> checkBoxes = List.of(
                gpsCheckBox, childSeatCheckBox, additionalDriverCheckBox, wifiCheckBox, tollPassCheckBox,
                familyPackageCheckBox, executivePackageCheckBox, allInclusivePackageCheckBox
        );
        for (CheckBox box : checkBoxes) {
            box.selectedProperty().addListener((obs, oldV, newV) -> updateSummaryAndPreview());
        }
    }

    @FXML
    public void resetForm() {
        if (!customerCombo.getItems().isEmpty()) {
            customerCombo.getSelectionModel().selectFirst();
        }
        vehicleCombo.getItems().setAll(ApplicationServices.vehicleService().getAllVehicles());
        if (!vehicleCombo.getItems().isEmpty()) {
            vehicleCombo.getSelectionModel().selectFirst();
        }
        startDatePicker.setValue(LocalDate.now().plusDays(1));
        endDatePicker.setValue(LocalDate.now().plusDays(4));
        rentalModeCombo.getSelectionModel().select(RentalMode.STANDARD);
        paymentTokenCombo.getSelectionModel().select("tok_approved");

        gpsCheckBox.setSelected(false);
        childSeatCheckBox.setSelected(false);
        additionalDriverCheckBox.setSelected(false);
        wifiCheckBox.setSelected(false);
        tollPassCheckBox.setSelected(false);
        familyPackageCheckBox.setSelected(false);
        executivePackageCheckBox.setSelected(false);
        allInclusivePackageCheckBox.setSelected(false);

        updateModalityDetails(rentalModeCombo.getValue());
        updateSummaryAndPreview();
        updateAvailabilityIndicator();
        setBanner("status-banner-neutral", "Ready to validate and process rental reservation.");
    }

    @FXML
    public void checkVehicleAvailability() {
        updateAvailabilityIndicator();
    }

    private void updateAvailabilityIndicator() {
        Vehicle vehicle = vehicleCombo.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (vehicle == null || start == null || end == null) {
            availabilityStatusLabel.setText("Select vehicle, pick-up date and return date.");
            availabilityStatusLabel.setStyle("-fx-text-fill: #64748b;");
            return;
        }
        if (!end.isAfter(start)) {
            availabilityStatusLabel.setText("Invalid dates: return date must be after pick-up date.");
            availabilityStatusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            return;
        }

        boolean available = rentalFacade.isVehicleAvailable(vehicle.getId(), start, end);
        if (available) {
            availabilityStatusLabel.setText("AVAILABLE: " + vehicle.getDisplayName() + " is free for " + start + " to " + end + ".");
            availabilityStatusLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold;");
        } else {
            availabilityStatusLabel.setText("UNAVAILABLE: " + vehicle.getDisplayName() + " has an overlapping confirmed reservation.");
            availabilityStatusLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        }
    }

    private void updateModalityDetails(RentalMode mode) {
        if (mode == null) {
            return;
        }
        RentalModeFactory factory = RentalModeFactoryProvider.getFactory(mode);
        RatePolicy rate = factory.createRatePolicy();
        CoveragePolicy coverage = factory.createCoveragePolicy();
        AssistancePolicy assistance = factory.createAssistancePolicy();

        ratePolicyLabel.setText("• Tariff: " + rate.getPolicyName() + " — " + rate.getConditionsDescription());
        coveragePolicyLabel.setText("• Coverage (" + formatCurrency(coverage.getDailyCoverageFee()) + "/day): "
                + coverage.getCoverageName() + " — " + coverage.getCoverageDescription());
        assistancePolicyLabel.setText("• Assistance (" + formatCurrency(assistance.getDailyAssistanceFee()) + "/day): "
                + assistance.getAssistanceName() + " — " + assistance.getAssistanceDescription());

        Set<String> includedCodes = coverage.getIncludedServiceCodes();
        if (includedCodes.isEmpty()) {
            includedServicesLabel.setText("Included optional services: None");
        } else {
            includedServicesLabel.setText("Included at no extra charge: " + String.join(", ", includedCodes));
        }
    }

    @FXML
    public void updateSummaryAndPreview() {
        try {
            ReservationRequest request = buildRequestFromForm();
            PricingBreakdown breakdown = rentalFacade.previewPricing(request);

            rentalDaysLabel.setText(breakdown.getRentalDays() + " day(s)");
            baseRateCostLabel.setText(formatCurrency(breakdown.getBaseVehicleCost()));
            modalityFeeCostLabel.setText(formatCurrency(breakdown.getModalityFee()));
            coverageCostLabel.setText(formatCurrency(breakdown.getCoverageCost()));
            assistanceCostLabel.setText(formatCurrency(breakdown.getAssistanceCost()));
            singleServicesCostLabel.setText(formatCurrency(breakdown.getSingleServicesCost()));
            packagesCostLabel.setText(formatCurrency(breakdown.getPackagesCost()));
            totalCostLabel.setText(formatCurrency(breakdown.getTotalCost()));

            String desc = "Summary: " + breakdown.getFullDescription();
            if (!breakdown.getSkippedComplimentaryServices().isEmpty()) {
                desc += " [Complimentary/deduplicated: "
                        + String.join(", ", breakdown.getSkippedComplimentaryServices()) + "]";
            }
            requestDescriptionLabel.setText(desc);
        } catch (IllegalArgumentException exception) {
            clearBreakdownLabels();
            requestDescriptionLabel.setText("Validation Notice: " + exception.getMessage());
        }
    }

    @FXML
    public void processRentalRequest() {
        if (processingOperation) {
            return;
        }
        processingOperation = true;
        confirmRentalButton.setDisable(true);

        try {
            ReservationRequest request = buildRequestFromForm();
            RentalOperationResult result = rentalFacade.processRental(request);

            refreshReservationTable();
            updateAvailabilityIndicator();
            updateSummaryAndPreview();

            if (result.isSuccess()) {
                setBanner("status-banner-success", "SUCCESS — " + result.getUserMessage());
            } else if (result.getReservation() != null
                    && result.getReservation().getReservationStatus() == ReservationStatus.REJECTED) {
                setBanner("status-banner-warning", "REJECTED — " + result.getUserMessage());
            } else {
                setBanner("status-banner-error", "ERROR — " + result.getUserMessage());
            }
        } catch (IllegalArgumentException exception) {
            setBanner("status-banner-warning", "VALIDATION ERROR — " + exception.getMessage());
        } catch (RuntimeException exception) {
            setBanner("status-banner-error", "UNEXPECTED ERROR — " + exception.getMessage());
        } finally {
            confirmRentalButton.setDisable(false);
            processingOperation = false;
        }
    }

    private ReservationRequest buildRequestFromForm() {
        ReservationRequestBuilder builder = new ReservationRequestBuilder()
                .withCustomer(customerCombo.getValue())
                .withVehicle(vehicleCombo.getValue())
                .withStartDate(startDatePicker.getValue())
                .withEndDate(endDatePicker.getValue())
                .withRentalMode(rentalModeCombo.getValue())
                .withPaymentToken(paymentTokenCombo.getValue());

        for (ServiceComponent component : collectSelectedServices()) {
            builder.addService(component);
        }

        return builder.build();
    }

    private List<ServiceComponent> collectSelectedServices() {
        List<ServiceComponent> selected = new ArrayList<>();
        // Add composite packages first so their bundled services take precedence over standalone checkboxes
        if (allInclusivePackageCheckBox.isSelected()) {
            selected.add(ServiceCatalog.allInclusiveTouringPackage());
        }
        if (familyPackageCheckBox.isSelected()) {
            selected.add(ServiceCatalog.familyPackage());
        }
        if (executivePackageCheckBox.isSelected()) {
            selected.add(ServiceCatalog.executivePackage());
        }

        if (gpsCheckBox.isSelected()) {
            selected.add(ServiceCatalog.gpsService());
        }
        if (childSeatCheckBox.isSelected()) {
            selected.add(ServiceCatalog.childSeatService());
        }
        if (additionalDriverCheckBox.isSelected()) {
            selected.add(ServiceCatalog.additionalDriverService());
        }
        if (wifiCheckBox.isSelected()) {
            selected.add(ServiceCatalog.portableWifiService());
        }
        if (tollPassCheckBox.isSelected()) {
            selected.add(ServiceCatalog.tollPassService());
        }
        return selected;
    }

    private void refreshReservationTable() {
        reservationTable.getItems().setAll(rentalFacade.getAllReservations());
    }

    private void clearBreakdownLabels() {
        rentalDaysLabel.setText("0 days");
        baseRateCostLabel.setText("0.00 COP");
        modalityFeeCostLabel.setText("0.00 COP");
        coverageCostLabel.setText("0.00 COP");
        assistanceCostLabel.setText("0.00 COP");
        singleServicesCostLabel.setText("0.00 COP");
        packagesCostLabel.setText("0.00 COP");
        totalCostLabel.setText("0.00 COP");
    }

    private void setBanner(String styleClass, String text) {
        operationResultBanner.getStyleClass().removeAll(
                "status-banner-neutral",
                "status-banner-success",
                "status-banner-warning",
                "status-banner-error"
        );
        operationResultBanner.getStyleClass().add(styleClass);
        operationResultBanner.setText(text);
    }

    private String formatCurrency(BigDecimal value) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value) + " COP";
    }

    private ListCell<Customer> createCustomerCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String authTag = item.isAuthorized() ? "Authorized" : "Unauthorized";
                    setText(item.getId() + " — " + item.getFullName() + " [" + authTag + "]");
                }
            }
        };
    }

    private ListCell<Vehicle> createVehicleCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getId() + " — " + item.getDisplayName()
                            + " (" + item.getCategory() + ", " + String.format(Locale.US, "%,.0f", item.getDailyRate()) + " COP/day)");
                }
            }
        };
    }
}
