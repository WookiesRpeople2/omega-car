package com.example.View.Admin.Ride;

import com.example.Dto.RideDto;
import com.example.Dto.CarDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Route("admin/rides")
public class RideView extends VerticalLayout {
    private final WebClient webClient;
    private final String baseUrl;
    private final Grid<RideDto> grid;
    private final Binder<RideDto> rideBinder;

    @Autowired
    public RideView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.rideBinder = new Binder<>(RideDto.class);

        addClassName("ride-admin-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createHeader());

        add(createStatsCards());

        Div contentArea = new Div();
        contentArea.addClassName("content-area");
        contentArea.setSizeFull();

        grid = createGrid();
        contentArea.add(grid);

        add(contentArea);

        applyStyles();

        refreshGrid();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("header");
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout titleSection = new HorizontalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setSpacing(true);

        Icon carIcon = VaadinIcon.CAR.create();
        carIcon.setSize("32px");
        carIcon.getStyle().set("color", "#6366f1");

        H1 title = new H1("Rides Management");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "28px")
                .set("font-weight", "700")
                .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
                .set("color", "white")
                .set("-webkit-background-clip", "text");

        titleSection.add(carIcon, title);

        Button addButton = new Button("Add New Ride", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        addButton.getStyle()
                .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
                .set("border", "none")
                .set("box-shadow", "0 4px 12px rgba(99, 102, 241, 0.3)")
                .set("transition", "all 0.3s ease");

        addButton.addClickListener(e -> openRideDialog(new RideDto()));

        header.add(titleSection, addButton);
        return header;
    }

    private Component createStatsCards() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("stats-container");
        statsLayout.setWidthFull();
        statsLayout.setPadding(true);
        statsLayout.setSpacing(true);

        int totalRides = getAllRides().size();


        statsLayout.add(
                createStatCard("Total Rides", String.valueOf(totalRides), VaadinIcon.CAR, "#6366f1")
        );

        return statsLayout;
    }

    private Component createStatCard(String label, String value, VaadinIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("stat-card");
        card.setPadding(true);
        card.setSpacing(false);

        HorizontalLayout iconContainer = new HorizontalLayout();
        iconContainer.addClassName("icon-container");
        iconContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        iconContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Icon cardIcon = icon.create();
        cardIcon.setSize("24px");
        cardIcon.getStyle().set("color", color);
        iconContainer.add(cardIcon);
        iconContainer.getStyle()
                .set("background", color + "20")
                .set("border-radius", "12px")
                .set("width", "48px")
                .set("height", "48px")
                .set("margin-bottom", "12px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "32px")
                .set("font-weight", "700")
                .set("color", "#1f2937")
                .set("line-height", "1");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "14px")
                .set("color", "#6b7280")
                .set("margin-top", "4px");

        card.add(iconContainer, valueSpan, labelSpan);
        return card;
    }

    private Grid<RideDto> createGrid() {
        Grid<RideDto> grid = new Grid<>(RideDto.class, false);
        grid.addClassName("cars-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("100%");

        grid.addColumn(ride -> {
            if (ride.getCarId() == null) return "N/A";
            // Fetch car details if needed
            return "Car ID: " + ride.getCarId().toString();
        })
                .setHeader("Vehicle")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(RideDto::getPickUp)
                .setHeader("Pick up address")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(RideDto::getDropOff)
                .setHeader("Drop off address")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(RideDto::getDepartureTime)
                .setHeader("Departure time")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addColumn(RideDto::getPrice)
                .setHeader("Price")
                .setSortable(true)
                .setFlexGrow(1);

        grid.addComponentColumn(ride -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getStyle().set("color", "#6366f1");
            editBtn.addClickListener(e -> openRideDialog(ride));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.getStyle().set("color", "#ef4444");
            deleteBtn.addClickListener(e -> deleteRide(ride));

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setWidth("120px");

        return grid;
    }

    private void openRideDialog(RideDto ride) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setCloseOnOutsideClick(false);

        boolean isUpdate = ride.getId() != null;

        H3 dialogTitle = new H3(isUpdate ? "Edit Ride" : "Add New Ride");
        dialogTitle.getStyle()
                .set("margin", "0")
                .set("color", "#1f2937")
                .set("font-weight", "700");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        ComboBox<CarDto> carSelect = new ComboBox<>("Vehicle");
        carSelect.setWidthFull();
        carSelect.getStyle().set("margin-top", "8px");
        carSelect.setItemLabelGenerator(c -> c == null ? "" : c.getMake() + " " + c.getModel() + " — " + c.getLicensePlate());
        try {
            carSelect.setItems(getAllCars());
        } catch (Exception e) {
            // fallback to empty list if service fails
            carSelect.setItems();
        }

        TextField pickupField = new TextField("Pick up address");
        pickupField.setWidthFull();

        TextField dropoffField = new TextField("Drop off address");
        dropoffField.setWidthFull();

        DateTimePicker departureTimeField = new DateTimePicker();
        departureTimeField.setLabel("Departure time");

        NumberField priceField = new NumberField("Price");
        priceField.setWidthFull();

        rideBinder.forField(pickupField)
                .asRequired("Pick up address is required")
                .bind(RideDto::getPickUp, RideDto::setPickUp);

        rideBinder.forField(dropoffField)
                .asRequired("Drop off address is required")
                .bind(RideDto::getDropOff, RideDto::setDropOff);

        rideBinder.forField(departureTimeField)
                .asRequired("Departure time is required")
                .bind(RideDto::getDepartureTime, RideDto::setDepartureTime);

        rideBinder.forField(priceField)
                .asRequired("Price is required")
                .bind(RideDto::getPrice, RideDto::setPrice);

        // Handle car selection separately
        if (ride.getCarId() != null && !carSelect.isEmpty()) {
            carSelect.setValue(getAllCars().stream()
                .filter(c -> c.getId().equals(ride.getCarId()))
                .findFirst()
                .orElse(null));
        }

        rideBinder.readBean(ride);

        formLayout.add(carSelect, pickupField, dropoffField, departureTimeField, priceField);

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.getStyle().set("margin-top", "20px");

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> dialog.close());

        Button saveButton = new Button("Save", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle()
                .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
                .set("border", "none");

        saveButton.addClickListener(e -> {
            try {
                rideBinder.writeBean(ride);
                
                // Set carId from carSelect
                CarDto selectedCar = carSelect.getValue();
                if (selectedCar != null) {
                    ride.setCarId(selectedCar.getId());
                }

                if (isUpdate) {
                    updateRide(ride.getId().toString(), ride);
                    showNotification("Ride updated successfully!", NotificationVariant.LUMO_SUCCESS);
                } else {
                    createRide(ride);
                    showNotification("Ride created successfully!", NotificationVariant.LUMO_SUCCESS);
                }

                refreshGrid();
                dialog.close();
            } catch (ValidationException ex) {
                showNotification("Please fix the errors", NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                showNotification("Error saving ride: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        buttonLayout.add(cancelButton, saveButton);

        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, formLayout, buttonLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteRide(RideDto ride) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setWidth("400px");

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Confirm Deletion");
        title.getStyle().set("margin", "0").set("color", "#1f2937");

        Span message = new Span("Are you sure you want to delete this ride?");
        message.getStyle().set("color", "#6b7280");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> confirmDialog.close());

        Button deleteBtn = new Button("Delete");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            deleteRideById(ride.getId().toString());
            refreshGrid();
            confirmDialog.close();
            showNotification("Ride deleted successfully", NotificationVariant.LUMO_SUCCESS);
        });

        buttons.add(cancelBtn, deleteBtn);
        layout.add(title, message, buttons);

        confirmDialog.add(layout);
        confirmDialog.open();
    }

    private String getAuthToken() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                .filter(cookie -> "AUTH".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    private List<RideDto> getAllRides() {
        try {
            String token = getAuthToken();
            Flux<RideDto> ridesFlux = webClient.get()
                .uri(baseUrl + "/api/rides")
                .header("Authorization", token != null ? "Bearer " + token : "")
                .retrieve()
                .bodyToFlux(RideDto.class);
            return ridesFlux.collectList().block();
        } catch (Exception e) {
            showNotification("Error loading rides: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            return List.of();
        }
    }

    private List<CarDto> getAllCars() {
        try {
            String token = getAuthToken();
            Flux<CarDto> carsFlux = webClient.get()
                .uri(baseUrl + "/api/cars")
                .header("Authorization", token != null ? "Bearer " + token : "")
                .retrieve()
                .bodyToFlux(CarDto.class);
            return carsFlux.collectList().block();
        } catch (Exception e) {
            showNotification("Error loading cars: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            return List.of();
        }
    }

    private void createRide(RideDto ride) {
        try {
            String token = getAuthToken();
            webClient.post()
                .uri(baseUrl + "/api/rides")
                .header("Authorization", token != null ? "Bearer " + token : "")
                .bodyValue(ride)
                .retrieve()
                .bodyToMono(RideDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Error creating ride: " + e.getMessage(), e);
        }
    }

    private void updateRide(String id, RideDto ride) {
        try {
            String token = getAuthToken();
            webClient.put()
                .uri(baseUrl + "/api/rides/" + id)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .bodyValue(ride)
                .retrieve()
                .bodyToMono(RideDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Error updating ride: " + e.getMessage(), e);
        }
    }

    private void deleteRideById(String id) {
        try {
            String token = getAuthToken();
            webClient.delete()
                .uri(baseUrl + "/api/rides/" + id)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            showNotification("Error deleting ride: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshGrid() {
        grid.setItems(getAllRides());
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_END);
        notification.open();
    }

    private void applyStyles() {
        getStyle()
                .set("background", "#f9fafb");

        getElement().executeJs(
                "const style = document.createElement('style');" +
                        "style.textContent = `" +
                        ".cars-admin-view .header {" +
                        "  background: white;" +
                        "  border-bottom: 1px solid #e5e7eb;" +
                        "  box-shadow: 0 1px 3px rgba(0,0,0,0.1);" +
                        "}" +
                        ".stats-container {" +
                        "  background: transparent;" +
                        "}" +
                        ".stat-card {" +
                        "  background: white;" +
                        "  border-radius: 16px;" +
                        "  box-shadow: 0 1px 3px rgba(0,0,0,0.1);" +
                        "  flex: 1;" +
                        "  transition: transform 0.2s, box-shadow 0.2s;" +
                        "}" +
                        ".stat-card:hover {" +
                        "  transform: translateY(-2px);" +
                        "  box-shadow: 0 8px 16px rgba(0,0,0,0.15);" +
                        "}" +
                        ".content-area {" +
                        "  background: white;" +
                        "  margin: 0 24px 24px 24px;" +
                        "  border-radius: 16px;" +
                        "  box-shadow: 0 1px 3px rgba(0,0,0,0.1);" +
                        "  padding: 24px;" +
                        "}" +
                        ".cars-grid {" +
                        "  border: none !important;" +
                        "}" +
                        "vaadin-button[theme~='primary']:hover {" +
                        "  transform: translateY(-2px);" +
                        "  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4) !important;" +
                        "}" +
                        "`;" +
                        "document.head.appendChild(style);"
        );
    }


}
