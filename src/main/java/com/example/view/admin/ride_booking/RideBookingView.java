package com.example.view.admin.ride_booking;

import com.example.dto.RideBookingDto;
import com.example.dto.RideDto;
import com.example.model.BookingStatus;
import com.example.view.components.ActionButton;
import com.example.view.components.AppNotification;
import com.example.view.components.ConfirmDialog;
import com.example.view.components.PageHeader;
import com.example.view.components.StatCard;
import com.example.view.components.StatusBadge;
import com.vaadin.flow.component.Component; 
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.select.Select;
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
import java.util.UUID;
import java.util.List;

@Route("admin/bookings")
public class RideBookingView extends VerticalLayout {

    private final WebClient webClient;
    private final String baseUrl;
    private final Grid<RideBookingDto> grid;
    private final Binder<RideBookingDto> binder;

    @Autowired
    public RideBookingView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.binder = new Binder<>(RideBookingDto.class);
        
        addClassName("bookings-admin-view");
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
        
        PageHeader pageHeader = new PageHeader("Booking Management", VaadinIcon.CALENDAR_USER);
        
        ActionButton addButton = ActionButton.createPrimary("Add New Booking", VaadinIcon.PLUS);
        addButton.addClickListener(e -> {
            // create a new booking and open dialog
            RideBookingDto newBooking = new RideBookingDto();
            newBooking.setBookingStatus(BookingStatus.PENDING);
            openBookingDialog(newBooking);
        });
        
        header.add(pageHeader, addButton);
        return header;
    }

    private Component createStatsCards() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("stats-container");
        statsLayout.setWidthFull();
        statsLayout.setPadding(true);
        statsLayout.setSpacing(true);
        
        List<RideBookingDto> allBookings = getAllBookings();
        int totalBookings = allBookings.size();
        long confirmedBookings = allBookings.stream()
            .filter(booking -> BookingStatus.CONFIRMED.equals(booking.getBookingStatus()))
            .count();
        long pendingBookings = allBookings.stream()
            .filter(booking -> BookingStatus.PENDING.equals(booking.getBookingStatus()))
            .count();
        
        statsLayout.add(
            new StatCard("Total Bookings", String.valueOf(totalBookings), VaadinIcon.CALENDAR_USER, "#6366f1"),
            new StatCard("Confirmed", String.valueOf(confirmedBookings), VaadinIcon.CHECK_CIRCLE, "#10b981"),
            new StatCard("Pending", String.valueOf(pendingBookings), VaadinIcon.CLOCK, "#f59e0b")
        );
        
        return statsLayout;
    }

    private Grid<RideBookingDto> createGrid() {
        Grid<RideBookingDto> gridLocal = new Grid<>(RideBookingDto.class, false);
        gridLocal.addClassName("bookings-grid");
        gridLocal.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        gridLocal.setHeight("100%");
        
        gridLocal.addColumn(RideBookingDto::getRideId)
            .setHeader("Ride ID")
            .setSortable(true)
            .setFlexGrow(1);
        
        gridLocal.addColumn(RideBookingDto::getSeatsBooked)
            .setHeader("Seats Booked")
            .setSortable(true)
            .setFlexGrow(1);
        
        gridLocal.addComponentColumn(booking -> new StatusBadge(booking.getBookingStatus()))
            .setHeader("Status").setFlexGrow(1);
        
        gridLocal.addComponentColumn(booking -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            
            ActionButton editBtn = ActionButton.createIconButton(VaadinIcon.EDIT, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.getStyle().set("color", "#6366f1");
            editBtn.addClickListener(e -> openBookingDialog(booking));
            
            ActionButton deleteBtn = ActionButton.createIconButton(VaadinIcon.TRASH, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.getStyle().set("color", "#ef4444");
            deleteBtn.addClickListener(e -> deleteBooking(booking));
            
            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setFlexGrow(0).setWidth("120px");
        
        return gridLocal;
    }

    private void openBookingDialog(RideBookingDto booking) {
        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setCloseOnOutsideClick(false);
        
        boolean isUpdate = booking.getId() != null;
        
        H3 dialogTitle = new H3(isUpdate ? "Edit Booking" : "Add New Booking");
        dialogTitle.getStyle()
            .set("margin", "0")
            .set("color", "#1f2937")
            .set("font-weight", "700");
        
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        // Ride selector
        com.vaadin.flow.component.combobox.ComboBox<RideDto> rideSelect = new com.vaadin.flow.component.combobox.ComboBox<>("Ride");
        try {
            rideSelect.setItems(getAllRides());
        } catch (Exception ignored) {
        }
        rideSelect.setItemLabelGenerator(r -> r.getId().toString() + " — " + r.getPickUp() + " → " + r.getDropOff());

        TextField rideIdField = new TextField("Ride ID");
        rideIdField.setWidthFull();
        rideIdField.setReadOnly(true);
        rideIdField.setValue(booking.getRideId() != null ? booking.getRideId().toString() : "");

        // Car model and trajet display (read-only)
        TextField carModelField = new TextField("Car");
        carModelField.setReadOnly(true);
        carModelField.setWidthFull();
        TextField trajetField = new TextField("Trajet");
        trajetField.setReadOnly(true);
        trajetField.setWidthFull();

        // when a ride is selected, update rideId and display fields
        rideSelect.addValueChangeListener(ev -> {
            RideDto selected = ev.getValue();
            if (selected != null) {
                rideIdField.setValue(selected.getId() != null ? selected.getId().toString() : "");
                // Note: CarDto doesn't have nested car info, so we'll just show car ID
                if (selected.getCarId() != null) {
                    carModelField.setValue("Car ID: " + selected.getCarId().toString());
                } else {
                    carModelField.setValue("");
                }
                trajetField.setValue(selected.getPickUp() + " → " + selected.getDropOff());
            } else {
                rideIdField.setValue("");
                carModelField.setValue("");
                trajetField.setValue("");
            }
        });
        
        IntegerField seatsBookedField = new IntegerField("Seats Booked");
        seatsBookedField.setWidthFull();
        seatsBookedField.setMin(1);
        
        Select<BookingStatus> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems(BookingStatus.values());
        statusSelect.setWidthFull();
        
        binder.forField(seatsBookedField)
            .asRequired("Number of seats is required")
            .bind(RideBookingDto::getSeatsBooked, RideBookingDto::setSeatsBooked);
        
        binder.forField(statusSelect)
            .asRequired("Status is required")
            .bind(RideBookingDto::getBookingStatus, RideBookingDto::setBookingStatus);
        
        binder.forField(rideIdField)
            .asRequired("Ride ID is required")
            .withConverter(
                uuid -> uuid != null ? UUID.fromString(uuid) : null,
                uuid -> uuid != null ? uuid.toString() : ""
            )
            .bind(RideBookingDto::getRideId, RideBookingDto::setRideId);

        // bind vehicleType and destinationCity to existing fields (if present)
        
        binder.readBean(booking);
        
        formLayout.add(rideSelect, rideIdField, carModelField, trajetField, seatsBookedField, statusSelect);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.getStyle().set("margin-top", "20px");
        
        ActionButton cancelButton = ActionButton.createSecondary("Cancel", VaadinIcon.CLOSE);
        cancelButton.addClickListener(e -> dialog.close());
        
        ActionButton saveButton = ActionButton.createPrimary("Save", VaadinIcon.CHECK);
        
        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(booking);
                
                if (isUpdate) {
                    updateBooking(booking.getId().toString(), booking);
                    AppNotification.success("Booking updated successfully!");
                } else {
                    createBooking(booking);
                    AppNotification.success("Booking created successfully!");
                }
                
                refreshGrid();
                dialog.close();
            } catch (ValidationException ex) {
                AppNotification.error("Please fix the errors");
            } catch (Exception ex) {
                AppNotification.error("Error saving booking: " + ex.getMessage());
            }
        });
        
        buttonLayout.add(cancelButton, saveButton);
        
        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, formLayout, buttonLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteBooking(RideBookingDto booking) {
        ConfirmDialog.show(
            "Confirm Deletion",
            "Are you sure you want to delete this booking?",
            "Delete",
            () -> {
                deleteBookingById(booking.getId().toString());
                refreshGrid();
                AppNotification.success("Booking deleted successfully");
            });
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

    private List<RideBookingDto> getAllBookings() {
        try {
            String token = getAuthToken();
            Flux<RideBookingDto> bookingsFlux = webClient.get()
                .uri(baseUrl + "/api/bookings")
                .header("Authorization", token != null ? "Bearer " + token : "")
                .retrieve()
                .bodyToFlux(RideBookingDto.class);
            return bookingsFlux.collectList().block();
        } catch (Exception e) {
            AppNotification.error("Error loading bookings: " + e.getMessage());
            return List.of();
        }
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
            AppNotification.error("Error loading rides: " + e.getMessage());
            return List.of();
        }
    }

    private void createBooking(RideBookingDto booking) {
        try {
            String token = getAuthToken();
            webClient.post()
                .uri(baseUrl + "/api/bookings")
                .header("Authorization", token != null ? "Bearer " + token : "")
                .bodyValue(booking)
                .retrieve()
                .bodyToMono(RideBookingDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Error creating booking: " + e.getMessage(), e);
        }
    }

    private void updateBooking(String id, RideBookingDto booking) {
        try {
            String token = getAuthToken();
            webClient.put()
                .uri(baseUrl + "/api/bookings/" + id)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .bodyValue(booking)
                .retrieve()
                .bodyToMono(RideBookingDto.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException("Error updating booking: " + e.getMessage(), e);
        }
    }

    private void deleteBookingById(String id) {
        try {
            String token = getAuthToken();
            webClient.delete()
                .uri(baseUrl + "/api/bookings/" + id)
                .header("Authorization", token != null ? "Bearer " + token : "")
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            AppNotification.error("Error deleting booking: " + e.getMessage());
        }
    }

    private void refreshGrid() {
        grid.setItems(getAllBookings());
    }

   

    private void applyStyles() {
        getStyle()
            .set("background", "#f9fafb");
        
        getElement().executeJs(
            "const style = document.createElement('style');" +
            "style.textContent = `" +
            ".bookings-admin-view .header {" +
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
            ".bookings-grid {" +
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