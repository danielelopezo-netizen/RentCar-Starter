package edu.rentcar.controller;

import edu.rentcar.app.ApplicationServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Label customerCountLabel;
    @FXML private Label vehicleCountLabel;
    @FXML private Label confirmedCountLabel;

    @FXML
    public void initialize() {
        customerCountLabel.setText(String.valueOf(ApplicationServices.customerRepository().count()));
        vehicleCountLabel.setText(String.valueOf(ApplicationServices.vehicleService().getVehicleCount()));
        if (confirmedCountLabel != null) {
            confirmedCountLabel.setText(String.valueOf(ApplicationServices.rentalFacade().getConfirmedReservationCount()));
        }
    }
}
