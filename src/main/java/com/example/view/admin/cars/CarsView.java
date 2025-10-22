package com.example.view.admin.cars;

import com.example.dto.CarDto;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
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

@Route("admin/cars")
public class CarsView extends VerticalLayout {

    private final WebClient webClient;
    private final String baseUrl;
    private final Grid<CarDto> grid;
    private final Binder<CarDto> binder;

    @Autowired
    public CarsView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.binder = new Binder<>(CarDto.class);
        
        addClassName("cars-admin-view");
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
        
        H1 title = new H1("Vehicle Management");
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "28px")
            .set("font-weight", "700")
            .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
            .set("color", "white")
            .set("-webkit-background-clip", "text");
        
        titleSection.add(carIcon, title);
        
        Button addButton = new Button("Add New Vehicle", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        addButton.getStyle()
            .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
            .set("border", "none")
            .set("box-shadow", "0 4px 12px rgba(99, 102, 241, 0.3)")
            .set("transition", "all 0.3s ease");
        
        addButton.addClickListener(e -> openCarDialog(new CarDto()));
        
        header.add(titleSection, addButton);
        return header;
    }

    private Component createStatsCards() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("stats-container");
        statsLayout.setWidthFull();
        statsLayout.setPadding(true);
        statsLayout.setSpacing(true);
        
        List<CarDto> allCars = getAllCars();
        int totalCars = allCars.size();
        long validatedCars = allCars.stream()
            .filter(CarDto::isCarValidated)
            .count();
        long pendingCars = totalCars - validatedCars;
        
        statsLayout.add(
            createStatCard("Total Vehicles", String.valueOf(totalCars), VaadinIcon.CAR, "#6366f1"),
            createStatCard("Validated", String.valueOf(validatedCars), VaadinIcon.CHECK_CIRCLE, "#10b981"),
            createStatCard("Pending Review", String.valueOf(pendingCars), VaadinIcon.CLOCK, "#f59e0b")
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

    private Grid<CarDto> createGrid() {
        Grid<CarDto> grid = new Grid<>(CarDto.class, false);
        grid.addClassName("cars-grid");
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("100%");
        
        grid.addColumn(CarDto::getMake)
            .setHeader("Make")
            .setSortable(true)
            .setFlexGrow(1);
        
        grid.addColumn(CarDto::getModel)
            .setHeader("Model")
            .setSortable(true)
            .setFlexGrow(1);
        
        grid.addColumn(CarDto::getLicensePlate)
            .setHeader("License Plate")
            .setSortable(true)
            .setFlexGrow(1);
        
        grid.addComponentColumn(car -> {
            Span badge = new Span();
            if (car.isCarValidated()) {
                badge.setText("✓ Validated");
                badge.getElement().getThemeList().add("badge success");
                badge.getStyle()
                    .set("background", "#d1fae5")
                    .set("color", "#065f46")
                    .set("padding", "4px 12px")
                    .set("border-radius", "12px")
                    .set("font-size", "13px")
                    .set("font-weight", "600");
            } else {
                badge.setText("⏱ Pending");
                badge.getElement().getThemeList().add("badge");
                badge.getStyle()
                    .set("background", "#fef3c7")
                    .set("color", "#92400e")
                    .set("padding", "4px 12px")
                    .set("border-radius", "12px")
                    .set("font-size", "13px")
                    .set("font-weight", "600");
            }
            return badge;
        }).setHeader("Status").setFlexGrow(1);
        
        grid.addComponentColumn(car -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getStyle().set("color", "#6366f1");
            editBtn.addClickListener(e -> openCarDialog(car));
            
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.getStyle().set("color", "#ef4444");
            deleteBtn.addClickListener(e -> deleteCar(car));
            
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setWidth("120px");
        
        return grid;
    }

    private void openCarDialog(CarDto car) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setCloseOnOutsideClick(false);
        
        boolean isUpdate = car.getId() != null;
        
        H3 dialogTitle = new H3(isUpdate ? "Edit Vehicle" : "Add New Vehicle");
        dialogTitle.getStyle()
            .set("margin", "0")
            .set("color", "#1f2937")
            .set("font-weight", "700");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        TextField makeField = new TextField("Make");
        makeField.setWidthFull();
        makeField.getStyle().set("margin-top", "8px");
        
        TextField modelField = new TextField("Model");
        modelField.setWidthFull();
        
        TextField licensePlateField = new TextField("License Plate");
        licensePlateField.setWidthFull();
        
        Checkbox validatedCheckbox = new Checkbox("Vehicle Validated");
        validatedCheckbox.getStyle().set("margin-top", "8px");
        
        binder.forField(makeField)
            .asRequired("Make is required")
            .bind(CarDto::getMake, CarDto::setMake);
        
        binder.forField(modelField)
            .asRequired("Model is required")
            .bind(CarDto::getModel, CarDto::setModel);
        
        binder.forField(licensePlateField)
            .asRequired("License plate is required")
            .bind(CarDto::getLicensePlate, CarDto::setLicensePlate);
        
        binder.forField(validatedCheckbox)
            .bind(CarDto::isCarValidated, CarDto::setCarValidated);
        
        binder.readBean(car);
        
        formLayout.add(makeField, modelField, licensePlateField, validatedCheckbox);
        
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
                binder.writeBean(car);
                
                if (isUpdate) {
                    updateCar(car.getId().toString(), car);
                    showNotification("Vehicle updated successfully!", NotificationVariant.LUMO_SUCCESS);
                } else {
                    createCar(car);
                    showNotification("Vehicle created successfully!", NotificationVariant.LUMO_SUCCESS);
                }
                
                refreshGrid();
                dialog.close();
            } catch (ValidationException ex) {
                showNotification("Please fix the errors", NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                showNotification("Error saving vehicle: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        
        buttonLayout.add(cancelButton, saveButton);
        
        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, formLayout, buttonLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteCar(CarDto car) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setWidth("400px");
        
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        
        H3 title = new H3("Confirm Deletion");
        title.getStyle().set("margin", "0").set("color", "#1f2937");
        
        Span message = new Span("Are you sure you want to delete this vehicle?");
        message.getStyle().set("color", "#6b7280");
        
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.addClickListener(e -> confirmDialog.close());
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            deleteCarById(car.getId().toString());
            refreshGrid();
            confirmDialog.close();
            showNotification("Vehicle deleted successfully", NotificationVariant.LUMO_SUCCESS);
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

    private void createCar(CarDto car) {
        try {
            String token = getAuthToken();
            webClient.post()
                .uri(baseUrl + "/api/cars")
                .header("Authorization", token != null ? "Bearer " + token : "")
                .bodyValue(car)
                .retrieve()
                .bodyToMono(CarDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Error creating car: " + e.getMessage(), e);
        }
    }

    private void updateCar(String id, CarDto car) {
        try {
            String token = getAuthToken();
            webClient.put()
                .uri(baseUrl + "/api/cars/" + id)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .bodyValue(car)
                .retrieve()
                .bodyToMono(CarDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Error updating car: " + e.getMessage(), e);
        }
    }

    private void deleteCarById(String id) {
        try {
            String token = getAuthToken();
            webClient.delete()
                .uri(baseUrl + "/api/cars/" + id)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            showNotification("Error deleting car: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshGrid() {
        grid.setItems(getAllCars());
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
