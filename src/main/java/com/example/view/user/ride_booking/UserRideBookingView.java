package com.example.view.user.ride_booking;

import com.example.dto.RideBookingDto;
import com.example.dto.RideDto;
import com.example.view.components.ActionButton;
import com.example.view.components.AppNotification;
import com.example.view.components.ConfirmDialog;
import com.example.view.components.StatusBadge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.component.UI;
import com.example.service.RideBookingUiBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RolesAllowed("User")
@Route(value = "bookings", layout = com.example.view.layout.MainLayout.class)
public class UserRideBookingView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {

    private final WebClient webClient;
    private final String baseUrl;
    private final Binder<RideBookingDto> binder;
    private Dialog currentDialog = null;

    // Filtres
    private TextField carModelFilter;
    private TextField trajetFilter;
    private NumberField minPriceFilter;
    private NumberField maxPriceFilter;

    private UUID pendingDriverIdParam = null;
    private RideBookingUiBroadcaster.Registration bookingUiReg;
    private boolean openDialogOnAttach = false;
    private UUID pendingCarIdParam = null;

    @Autowired
    public UserRideBookingView(WebClient webClient, @Value("${app.base-url}") String baseUrl, RideBookingUiBroadcaster uiBroadcaster) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.binder = new Binder<>(RideBookingDto.class);

        addClassName("user-bookings-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // Header moderne avec gradient
        Div header = createModernHeader();
        add(header);

        // Container principal avec effet carte
        Div mainContainer = new Div();
        mainContainer.addClassName("main-container");
        mainContainer.setSizeFull();
        mainContainer.getStyle()
                .set("padding", "24px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("min-height", "100vh");

        // Carte des filtres
        Div filterCard = createFilterCard();

        // Carte de la grille
        Div gridCard = createGridCard();

        mainContainer.add(filterCard, gridCard);
        add(mainContainer);

        applyGlobalStyles();
        refreshGrid();

        bookingUiReg = uiBroadcaster.register(evt -> {
            if (evt instanceof com.example.service.RideBookingUiBroadcaster.BookingStatusChangedEvent) {
                getUI().ifPresent(ui -> ui.access(this::refreshGrid));
            }
        });
    }

    private static class LocationSuggestion {
        private final String label;
        private final double lat;
        private final double lon;
        public LocationSuggestion(String label, double lat, double lon) {
            this.label = label; this.lat = lat; this.lon = lon;
        }
        public String getLabel() { return label; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }
        @Override public String toString() { return label; }
    }

    private List<LocationSuggestion> searchPlaces(String query) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&accept-language=en&q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            var arr = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(org.springframework.core.ParameterizedTypeReference.forType(java.util.List.class))
                .block();
            java.util.List<?> list = arr instanceof java.util.List ? (java.util.List<?>) arr : java.util.List.of();
            java.util.List<LocationSuggestion> out = new java.util.ArrayList<>();
            for (Object o : list) {
                if (o instanceof java.util.Map<?,?> m) {
                    Object display = m.get("display_name");
                    Object lat = m.get("lat");
                    Object lon = m.get("lon");
                    if (display != null && lat != null && lon != null) {
                        try {
                            out.add(new LocationSuggestion(String.valueOf(display), Double.parseDouble(String.valueOf(lat)), Double.parseDouble(String.valueOf(lon))));
                        } catch (Exception ignored) {}
                    }
                }
            }
            return out;
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

    private String reverseGeocodeLabel(double lat, double lon) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=json&accept-language=en&lat=" + lat + "&lon=" + lon;
            var m = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(org.springframework.core.ParameterizedTypeReference.forType(java.util.Map.class))
                .block();
            if (m instanceof java.util.Map<?,?> map) {
                Object display = map.get("display_name");
                if (display != null) return String.valueOf(display);
            }
        } catch (Exception ignored) {}
        return null;
    }

	// Client-side price computation removed; price is computed server-side

    private Div createModernHeader() {
        Div header = new Div();
        header.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "32px 24px")
                .set("box-shadow", "0 4px 20px rgba(102, 126, 234, 0.3)");

        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setAlignItems(FlexComponent.Alignment.CENTER);
        headerContent.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Titre avec icône
        HorizontalLayout titleSection = new HorizontalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setSpacing(true);

        Icon icon = VaadinIcon.CALENDAR_USER.create();
        icon.setSize("36px");
        icon.getStyle()
                .set("color", "white")
                .set("filter", "drop-shadow(0 2px 8px rgba(0,0,0,0.2))");

        Span title = new Span("My Bookings");
        title.getStyle()
                .set("color", "white")
                .set("font-size", "32px")
                .set("font-weight", "700")
                .set("text-shadow", "0 2px 8px rgba(0,0,0,0.2)");

        titleSection.add(icon, title);

        // New Reservation button
        Button newBookingBtn = new Button("New booking", VaadinIcon.PLUS_CIRCLE.create());
        newBookingBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBookingBtn.getStyle().set("border-radius", "8px");
        newBookingBtn.addClickListener(e -> {
            RideBookingDto draft = new RideBookingDto();
            if (pendingDriverIdParam != null) {
                draft.setDriverId(pendingDriverIdParam);
            }
            openBookingDialog(draft);
        });

        headerContent.add(titleSection, newBookingBtn);
        header.add(headerContent);

        return header;
    }

    private Div createFilterCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("margin-bottom", "24px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease");

        // Titre de la section
        HorizontalLayout filterHeader = new HorizontalLayout();
        filterHeader.setWidthFull();
        filterHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        filterHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span filterTitle = new Span("🔍 Search bookings");
        filterTitle.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "600")
                .set("color", "#1f2937");

        card.add(filterHeader);

        // Ligne de filtres
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setSpacing(true);
        filterLayout.getStyle().set("margin-top", "16px");

        // Filtre modèle de voiture
        carModelFilter = createModernTextField("Car model", "e.g., Tesla Model 3", VaadinIcon.CAR);
        carModelFilter.addValueChangeListener(e -> applyFilters());

        // Filtre trajet
        trajetFilter = createModernTextField("Route", "e.g., Paris", VaadinIcon.MAP_MARKER);
        trajetFilter.addValueChangeListener(e -> applyFilters());

        // Filtre prix minimum
        minPriceFilter = createModernNumberField("Min price", "0", VaadinIcon.EURO);
        minPriceFilter.addValueChangeListener(e -> applyFilters());

        // Filtre prix maximum
        maxPriceFilter = createModernNumberField("Max price", "∞", VaadinIcon.EURO);
        maxPriceFilter.addValueChangeListener(e -> applyFilters());

        // Bouton réinitialiser
        Button resetBtn = new Button("Reset", VaadinIcon.REFRESH.create());
        resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetBtn.getStyle()
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease");
        resetBtn.addClickListener(e -> {
            carModelFilter.clear();
            trajetFilter.clear();
            minPriceFilter.clear();
            maxPriceFilter.clear();
            refreshGrid();
        });

        filterLayout.add(carModelFilter, trajetFilter, minPriceFilter, maxPriceFilter, resetBtn);
        card.add(filterLayout);

        return card;
    }

    private TextField createModernTextField(String label, String placeholder, VaadinIcon icon) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setPrefixComponent(icon.create());
        field.setClearButtonVisible(true);
        field.getStyle()
                .set("--lumo-border-radius", "8px");
        return field;
    }

    private NumberField createModernNumberField(String label, String placeholder, VaadinIcon icon) {
        NumberField field = new NumberField(label);
        field.setPlaceholder(placeholder);
        field.setPrefixComponent(icon.create());
        field.setClearButtonVisible(true);
        field.setMin(0);
        field.getStyle()
                .set("--lumo-border-radius", "8px");
        return field;
    }

    private Div createGridCard() {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("padding", "24px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.1)")
                .set("flex-grow", "1")
                .set("display", "flex")
                .set("flex-direction", "column");

        // Remplacer le grid par un conteneur de cartes
        Div cardsContainer = createCardsContainer();
        card.add(cardsContainer);

        return card;
    }

    private Div createCardsContainer() {
        Div container = new Div();
        container.addClassName("booking-cards-container");
        container.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(300px, 1fr))")
                .set("gap", "20px")
                .set("width", "100%")
                .set("overflow-y", "auto")
                .set("padding", "8px");

        refreshCards(container);

        return container;
    }

    private void applyFilters() {
        List<RideBookingDto> allBookings = getAllBookings();

        List<RideBookingDto> filteredBookings = allBookings.stream()
                .filter(booking -> {
                    // Filtre modèle de voiture
                    if (carModelFilter.getValue() != null && !carModelFilter.getValue().trim().isEmpty()) {
                        String carModel = getRideById(booking.getRideId())
                                .map(ride -> {
                                    if (ride.getCarId() != null) {
                                        return "Car ID: " + ride.getCarId().toString();
                                    }
                                    return "";
                                })
                                .orElse("");

                        if (!carModel.contains(carModelFilter.getValue().toLowerCase().trim())) {
                            return false;
                        }
                    }

                    // Filtre trajet
                    if (trajetFilter.getValue() != null && !trajetFilter.getValue().trim().isEmpty()) {
                        String trajet = getRideById(booking.getRideId())
                                .map(ride -> (ride.getPickUp() + " " + ride.getDropOff()).toLowerCase(Locale.ENGLISH))
                                .orElse("");

                        if (!trajet.contains(trajetFilter.getValue().toLowerCase(Locale.ENGLISH).trim())) {
                            return false;
                        }
                    }

                    // Filtre prix
                    Double minPrice = minPriceFilter.getValue();
                    Double maxPrice = maxPriceFilter.getValue();

                    if (minPrice != null || maxPrice != null) {
                        Double ridePrice = getRideById(booking.getRideId())
                                .map(RideDto::getPrice)
                                .orElse(null);

                        if (ridePrice != null) {
                            if (minPrice != null && ridePrice < minPrice) {
                                return false;
                            }
                            if (maxPrice != null && ridePrice > maxPrice) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Mettre à jour les cartes au lieu du grid
        refreshCardsWithData(filteredBookings);
    }

    private void refreshCards(Div container) {
        List<RideBookingDto> bookings = getAllBookings();
        refreshCardsWithData(bookings, container);
    }

    private void refreshCardsWithData(List<RideBookingDto> bookings) {
        // Trouver le conteneur de cartes existant et le vider
        Div container = null;
        for (int i = 0; i < getComponentCount(); i++) {
            com.vaadin.flow.component.Component component = getComponentAt(i);
            if (component instanceof Div) {
                Div mainContainer = (Div) component;
                for (int j = 0; j < mainContainer.getComponentCount(); j++) {
                    com.vaadin.flow.component.Component child = mainContainer.getComponentAt(j);
                    if (child instanceof Div
                            && ((Div) child).getElement().getClassList().contains("booking-cards-container")) {
                        container = (Div) child;
                        break;
                    }
                }
            }
            if (container != null) {
                break;
            }
        }

        if (container != null) {
            refreshCardsWithData(bookings, container);
        }
    }

    private void refreshCardsWithData(List<RideBookingDto> bookings, Div container) {
        container.removeAll();

        if (bookings.isEmpty()) {
            Div emptyState = new Div();
            emptyState.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("padding", "40px")
                    .set("text-align", "center")
                    .set("grid-column", "1 / -1");

            Icon emptyIcon = VaadinIcon.CALENDAR_O.create();
            emptyIcon.setSize("64px");
            emptyIcon.getStyle()
                    .set("color", "#667eea")
                    .set("margin-bottom", "16px");

            Span emptyText = new Span("Aucune réservation trouvée");
            emptyText.getStyle()
                    .set("font-size", "18px")
                    .set("font-weight", "500")
                    .set("color", "#6b7280");

            emptyState.add(emptyIcon, emptyText);
            container.add(emptyState);
        } else {
            bookings.forEach(booking -> {
                container.add(createBookingCard(booking));
            });
        }
    }

    private Div createBookingCard(RideBookingDto booking) {
        Div card = new Div();
        card.addClassName("booking-card");
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.08)")
                .set("overflow", "hidden")
                .set("transition", "transform 0.3s ease, box-shadow 0.3s ease")
                .set("display", "flex")
                .set("flex-direction", "column");

        card.getElement().addEventListener("mouseover", e -> {
            card.getStyle()
                    .set("transform", "translateY(-5px)")
                    .set("box-shadow", "0 10px 30px rgba(102, 126, 234, 0.2)");
        });

        card.getElement().addEventListener("mouseout", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 4px 20px rgba(0,0,0,0.08)");
        });

        // En-tête de la carte: Ride ID as title
        String carInfo = booking.getRideId() != null ? booking.getRideId().toString() : "N/A";

        Div cardHeader = new Div();
        cardHeader.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "16px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "12px");

        Icon carIcon = VaadinIcon.CAR.create();
        carIcon.setSize("24px");
        carIcon.getStyle().set("color", "white");

        Span carTitle = new Span(carInfo);
        carTitle.getStyle()
                .set("font-weight", "700")
                .set("font-size", "18px");

        cardHeader.add(carIcon, carTitle);

        // Contenu de la carte
        Div cardContent = new Div();
        cardContent.getStyle()
                .set("padding", "16px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "12px");

        // Trajet
        String trajet = getRideById(booking.getRideId())
                .map(r -> {
                    String from = r.getPickupName() != null && !r.getPickupName().isBlank() ? r.getPickupName() : r.getPickUp();
                    String to = r.getDropoffName() != null && !r.getDropoffName().isBlank() ? r.getDropoffName() : r.getDropOff();
                    return from + " → " + to;
                })
                .orElse("N/A");

        HorizontalLayout trajetLayout = new HorizontalLayout();
        trajetLayout.setSpacing(true);
        trajetLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon locationIcon = VaadinIcon.MAP_MARKER.create();
        locationIcon.setSize("18px");
        locationIcon.getStyle().set("color", "#10b981");

        Span trajetText = new Span(trajet);
        trajetText.getStyle()
                .set("font-weight", "500")
                .set("color", "#374151");

        trajetLayout.add(locationIcon, trajetText);

        // Prix
        String price = getRideById(booking.getRideId())
                .map(r -> String.format("$%.2f", r.getPrice()))
                .orElse("N/A");

        HorizontalLayout priceLayout = new HorizontalLayout();
        priceLayout.setSpacing(true);
        priceLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon priceIcon = VaadinIcon.EURO.create();
        priceIcon.setSize("18px");
        priceIcon.getStyle().set("color", "#667eea");

        Span priceText = new Span(price);
        priceText.getStyle()
                .set("font-weight", "700")
                .set("color", "#667eea")
                .set("font-size", "18px");

        priceLayout.add(priceIcon, priceText);

        // Places
        HorizontalLayout seatsLayout = new HorizontalLayout();
        seatsLayout.setSpacing(true);
        seatsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Icon seatsIcon = VaadinIcon.USERS.create();
        seatsIcon.setSize("18px");
        seatsIcon.getStyle().set("color", "#f59e0b");

        Span seatsText = new Span(booking.getSeatsBooked() + " place(s)");
        seatsText.getStyle()
                .set("font-weight", "500")
                .set("color", "#374151");

        seatsLayout.add(seatsIcon, seatsText);

        // Statut
        StatusBadge statusBadge = new StatusBadge(booking.getBookingStatus());

        cardContent.add(trajetLayout, priceLayout, seatsLayout, statusBadge);

        // Actions
        HorizontalLayout actions = new HorizontalLayout();
        actions.getStyle()
                .set("padding", "0 16px 16px 16px")
                .set("display", "flex")
                .set("justify-content", "space-between")
                .set("margin-top", "auto");

        Button editBtn = new Button("Edit", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.getStyle()
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease");
        editBtn.addClickListener(e -> openBookingDialog(booking));

        // Acceptance is handled by drivers via notifications; users cannot accept here

        Button cancelBtn = new Button("Cancel", VaadinIcon.CLOSE_CIRCLE.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        cancelBtn.getStyle()
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease");
        cancelBtn.addClickListener(e -> ConfirmDialog.show(
                "Cancel booking",
                "Are you sure you want to cancel this booking?",
                "Cancel",
                () -> {
                    deleteBookingById(booking.getId().toString());
                    refreshCardsWithData(getAllBookings());
                    AppNotification.success("Booking cancelled");
                }));

        actions.add(editBtn, cancelBtn);

        card.add(cardHeader, cardContent, actions);
        return card;
    }

    // La méthode createGrid a été remplacée par createBookingCard et
    // createCardsContainer

    private void openBookingDialog(RideBookingDto booking) {
        Dialog dialog = new Dialog();
        dialog.setWidth("480px");
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);

        boolean isUpdate = booking.getId() != null;

        // En-tête du dialogue
        Div dialogHeader = new Div();
        dialogHeader.getStyle()
                .set("padding", "24px 24px 16px 24px")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("margin", "-16px -16px 0 -16px")
                .set("border-radius", "8px 8px 0 0");

        H3 dialogTitle = new H3(isUpdate ? "✏️ Edit booking" : "➕ New booking");
        dialogTitle.getStyle()
                .set("color", "white")
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "700");

        dialogHeader.add(dialogTitle);

        FormLayout formLayout = new FormLayout();
        formLayout.getStyle().set("padding", "24px 0");

        TextField driverIdField = new TextField("Driver ID (optional)");
        String prefillDriver = booking.getDriverId() != null ? booking.getDriverId().toString() : (pendingDriverIdParam != null ? pendingDriverIdParam.toString() : "");
        driverIdField.setValue(prefillDriver);
        driverIdField.setPrefixComponent(VaadinIcon.USER.create());
        driverIdField.getStyle().set("--lumo-border-radius", "8px");

        TextField carIdField = new TextField("Car ID (optional)");
        String prefillCar = pendingCarIdParam != null ? pendingCarIdParam.toString() : "";
        carIdField.setValue(prefillCar);
        carIdField.setPrefixComponent(VaadinIcon.CAR.create());
        carIdField.getStyle().set("--lumo-border-radius", "8px");

		// Hidden fields to carry coordinates and names
		TextField dropOffField = new TextField("Destination (coordinates)");
		dropOffField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
		dropOffField.getStyle().set("--lumo-border-radius", "8px");

		TextField pickUpField = new TextField("Pickup (coordinates)");
		pickUpField.setPrefixComponent(VaadinIcon.LOCATION_ARROW.create());
		pickUpField.getStyle().set("--lumo-border-radius", "8px");
		pickUpField.setReadOnly(true);

		TextField pickupNameField = new TextField("Pickup name");
		pickupNameField.setReadOnly(true);
		pickupNameField.getStyle().set("--lumo-border-radius", "8px");

		TextField dropoffNameField = new TextField("Destination name");
		dropoffNameField.setReadOnly(true);
		dropoffNameField.getStyle().set("--lumo-border-radius", "8px");

		ComboBox<LocationSuggestion> pickupSelect = new ComboBox<>("Pickup");
		pickupSelect.setClearButtonVisible(true);
		pickupSelect.setItemLabelGenerator(LocationSuggestion::getLabel);
		pickupSelect.setWidthFull();
		pickupSelect.getStyle().set("--lumo-border-radius", "8px");

        DateTimePicker departureField = new DateTimePicker("Departure time");
        departureField.getStyle().set("--lumo-border-radius", "8px");

        IntegerField seatsField = new IntegerField("Seats");
        seatsField.setMin(1);
        seatsField.setPrefixComponent(VaadinIcon.USERS.create());
        seatsField.getStyle().set("--lumo-border-radius", "8px");

		// No price estimation on client; price is computed server-side

        ComboBox<LocationSuggestion> destinationSelect = new ComboBox<>("Destination");
        destinationSelect.setClearButtonVisible(true);
        destinationSelect.setItemLabelGenerator(LocationSuggestion::getLabel);
        destinationSelect.setWidthFull();
        destinationSelect.getStyle().set("--lumo-border-radius", "8px");
		DataProvider<LocationSuggestion, String> placesProvider = DataProvider.fromFilteringCallbacks(
            (Query<LocationSuggestion, String> q) -> {
                String filter = q.getFilter().orElse("");
                java.util.List<LocationSuggestion> all = (filter != null && filter.trim().length() >= 3)
                    ? searchPlaces(filter.trim())
                    : java.util.List.of();
                int offset = q.getOffset();
                int limit = q.getLimit();
                return all.stream().skip(offset).limit(limit);
            },
            (Query<LocationSuggestion, String> q) -> {
                String filter = q.getFilter().orElse("");
                if (filter == null || filter.trim().length() < 3) return 0;
                return searchPlaces(filter.trim()).size();
            }
        );
		destinationSelect.setDataProvider(placesProvider, (SerializableFunction<String, String>) f -> f);
		pickupSelect.setDataProvider(placesProvider, (SerializableFunction<String, String>) f -> f);
		pickupSelect.addValueChangeListener(ev -> {
			LocationSuggestion sel = ev.getValue();
			if (sel != null) {
				pickUpField.setValue(sel.getLat() + "," + sel.getLon());
				pickupNameField.setValue(sel.getLabel());
			}
		});
		destinationSelect.addValueChangeListener(ev -> {
			LocationSuggestion sel = ev.getValue();
			if (sel != null) {
				// Store coordinates and name for server-side price computation
				dropOffField.setValue(sel.getLat() + "," + sel.getLon());
				dropoffNameField.setValue(sel.getLabel());
			}
		});

		// No client-side recomputation needed

        // Always creating a new ride in this view

        binder.forField(seatsField)
                .asRequired("Le nombre de places est requis")
                .bind(RideBookingDto::getSeatsBooked, RideBookingDto::setSeatsBooked);

        // No ride selection; booking.rideId will be set after ride creation

        binder.forField(driverIdField)
                .withConverter(
                        s -> s != null && !s.isEmpty() ? UUID.fromString(s) : null,
                        u -> u != null ? u.toString() : "")
                .bind(RideBookingDto::getDriverId, RideBookingDto::setDriverId);

        // Car ID is not part of RideBookingDto, but is part of the Ride being created
        // We'll use it during ride creation below if provided

		binder.readBean(booking);
		// Default seats to 1 if empty
		if (seatsField.isEmpty()) {
			seatsField.setValue(1);
		}

		formLayout.add(
			driverIdField,
			carIdField,
			seatsField,
			pickupSelect,
			pickupNameField,
			pickUpField,
			destinationSelect,
			dropoffNameField,
			dropOffField,
			departureField
		);

		// Resolve current location button for pickup
		Button useCurrentLocation = new Button("Use my location", VaadinIcon.LOCATION_ARROW.create());
		useCurrentLocation.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		useCurrentLocation.getStyle().set("border-radius", "8px");
        useCurrentLocation.addClickListener(evt -> {
            UI.getCurrent().getPage().executeJs(
                "(function(){ if(!navigator.geolocation) return; navigator.geolocation.getCurrentPosition(function(pos){ var v=pos.coords.latitude+','+pos.coords.longitude; $0.value=v; $0.dispatchEvent(new Event('input',{bubbles:true})); }, function(){}, {enableHighAccuracy:true, maximumAge: 1000, timeout: 8000}); })();",
                pickUpField
            );
        });

        // When pickup coordinates change, reverse geocode to fill name if available
        pickUpField.addValueChangeListener(ev -> {
            String v = ev.getValue();
            if (v != null && v.contains(",")) {
                try {
                    String[] p = v.split(",");
                    double lat = Double.parseDouble(p[0].trim());
                    double lon = Double.parseDouble(p[1].trim());
                    String label = reverseGeocodeLabel(lat, lon);
                    if (label != null && !label.isBlank()) {
                        pickupNameField.setValue(label);
                    }
                } catch (Exception ignored) {}
            }
        });

        // Populate drop-off and departure time from rideId
        // No existing ride info loading necessary

        // Fields are always editable for creating a new ride

        ActionButton cancel = ActionButton.createSecondary("Annuler", VaadinIcon.CLOSE);
        cancel.getStyle().set("border-radius", "8px");
        cancel.addClickListener(e -> dialog.close());

        ActionButton save = ActionButton.createPrimary(isUpdate ? "Mettre à jour" : "Créer", VaadinIcon.CHECK);
        save.getStyle().set("border-radius", "8px");
        save.addClickListener(e -> {
            try {
				binder.writeBean(booking);
				String pickupCoord = pickUpField.getValue();
				String dropoffCoord = dropOffField.getValue();
				boolean ok = pickupCoord != null && pickupCoord.contains(",") && dropoffCoord != null && dropoffCoord.contains(",");
				if (!ok) {
					AppNotification.error("Veuillez sélectionner les emplacements de départ et d'arrivée");
					return;
				}
                if (isUpdate) {
                    updateBooking(booking.getId().toString(), booking);
                    AppNotification.success("Booking updated");
                } else {
                    // Always create ride first, then booking
					RideDto newRide = new RideDto();
					// Names and coords
					newRide.setPickupName(pickupNameField.getValue());
					try {
						String pv = pickUpField.getValue();
						if (pv != null && pv.contains(",")) {
							String[] p = pv.split(",");
							newRide.setPickupLat(Double.parseDouble(p[0].trim()));
							newRide.setPickupLon(Double.parseDouble(p[1].trim()));
						}
					} catch (Exception ignored) {}
					newRide.setDropoffName(dropoffNameField.getValue());
					try {
						String dv = dropOffField.getValue();
						if (dv != null && dv.contains(",")) {
							String[] d = dv.split(",");
							newRide.setDropoffLat(Double.parseDouble(d[0].trim()));
							newRide.setDropoffLon(Double.parseDouble(d[1].trim()));
						}
					} catch (Exception ignored) {}
					// Legacy text values also kept for compatibility
					newRide.setPickUp(pickUpField.getValue());
					newRide.setDropOff(dropOffField.getValue());
                    newRide.setDepartureTime(departureField.getValue());
					// Price is computed server-side in RideService
                    try {
                        String carIdStr = carIdField.getValue();
                        if (carIdStr != null && !carIdStr.isBlank()) {
                            newRide.setCarId(UUID.fromString(carIdStr.trim()));
                        } else if (pendingCarIdParam != null) {
                            newRide.setCarId(pendingCarIdParam);
                        }
                    } catch (IllegalArgumentException ignored) {}
                    String token = getAuthToken();
                    RideDto created = webClient.post()
                        .uri(baseUrl + "/api/rides")
                        .header("Authorization", token != null ? "Bearer " + token : "")
                        .bodyValue(newRide)
                        .retrieve()
                        .bodyToMono(RideDto.class)
                        .block();
                    if (created == null || created.getId() == null) throw new RuntimeException("Ride creation failed");
                    booking.setRideId(created.getId());
                    createBooking(booking);
                    AppNotification.success("Booking created");
                    UI.getCurrent().getPage().reload();
                }
                refreshGrid();
                dialog.close();
            } catch (ValidationException ex) {
                AppNotification.error("Please fix the errors");
            } catch (Exception ex) {
                AppNotification.error("Error: " + ex.getMessage());
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancel, save);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        buttons.getStyle().set("padding-top", "16px");

		HorizontalLayout locationActions = new HorizontalLayout(useCurrentLocation);
		locationActions.setWidthFull();
		locationActions.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

		VerticalLayout layout = new VerticalLayout(dialogHeader, formLayout, locationActions, buttons);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle()
                .set("padding", "16px");

        dialog.add(layout);
        dialog.open();

        this.currentDialog = dialog;
        dialog.addDetachListener(e -> this.currentDialog = null);
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        if (this.currentDialog != null && this.currentDialog.isOpened()) {
            this.currentDialog.close();
            this.currentDialog = null;
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var req = VaadinService.getCurrentRequest();
        if (req == null || req.getCookies() == null) {
            event.rerouteTo(com.example.view.auth.LoginView.class);
            return;
        }
        boolean hasAuth = java.util.Arrays.stream(req.getCookies()).anyMatch(c -> "AUTH".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty());
        if (!hasAuth) {
            event.rerouteTo(com.example.view.auth.LoginView.class);
            return;
        }
        var params = event.getLocation().getQueryParameters().getParameters();
        String driverId = params.getOrDefault("driverId", java.util.List.of()).stream().findFirst().orElse(null);
        String carId = params.getOrDefault("carId", java.util.List.of()).stream().findFirst().orElse(null);
        try {
            this.pendingDriverIdParam = driverId != null ? UUID.fromString(driverId) : null;
            this.pendingCarIdParam = carId != null ? UUID.fromString(carId) : null;
        } catch (IllegalArgumentException ex) {
            this.pendingDriverIdParam = null;
            this.pendingCarIdParam = null;
        }
        // If a driverId is present, open the new booking dialog immediately prefilled
        if (this.pendingDriverIdParam != null) {
            this.openDialogOnAttach = true;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (openDialogOnAttach) {
            openDialogOnAttach = false;
            RideBookingDto draft = new RideBookingDto();
            draft.setDriverId(this.pendingDriverIdParam);
            openBookingDialog(draft);
        }
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (bookingUiReg != null) { bookingUiReg.remove(); bookingUiReg = null; }
    }

    private void refreshGrid() {
        if ((carModelFilter.getValue() != null && !carModelFilter.getValue().trim().isEmpty()) ||
                (trajetFilter.getValue() != null && !trajetFilter.getValue().trim().isEmpty()) ||
                minPriceFilter.getValue() != null ||
                maxPriceFilter.getValue() != null) {
            applyFilters();
        } else {
            refreshCardsWithData(getAllBookings());
        }
    }

    private String getAuthToken() {
        var req = VaadinService.getCurrentRequest();
        if (req == null) return null;
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(cookie -> "AUTH".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private List<RideBookingDto> getAllBookings() {
        try {
            String token = getAuthToken();
            Flux<RideBookingDto> bookingsFlux = webClient.get()
                    .uri(baseUrl + "/api/bookings")
                    .header("Authorization", token != null ? "Bearer " + token : "")
                    .retrieve()
                    .bodyToFlux(RideBookingDto.class)
                    .onErrorResume(e -> reactor.core.publisher.Flux.empty());
            return bookingsFlux.collectList().block();
        } catch (Exception e) {
            AppNotification.error("Error loading bookings: " + e.getMessage());
            return List.of();
        }
    }

    private java.util.Optional<RideDto> getRideById(UUID id) {
        try {
            String token = getAuthToken();
            RideDto ride = webClient.get()
                    .uri(baseUrl + "/api/rides/" + id.toString())
                    .header("Authorization", token != null ? "Bearer " + token : "")
                    .retrieve()
                    .bodyToMono(RideDto.class)
                    .block();
            return java.util.Optional.ofNullable(ride);
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    private void createBooking(RideBookingDto booking) {
        try {
            // Validate driver online if specified
            if (booking.getDriverId() != null) {
                var e = webClient.get()
                    .uri(baseUrl + "/api/driver-locations/" + booking.getDriverId())
                    .retrieve()
                    .bodyToMono(com.example.dto.DriverLocationEvent.class)
                    .block();
                if (e == null) {
                    throw new RuntimeException("Selected driver is offline");
                }
            }
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

    private void applyGlobalStyles() {
        getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("min-height", "100vh");
    }
}