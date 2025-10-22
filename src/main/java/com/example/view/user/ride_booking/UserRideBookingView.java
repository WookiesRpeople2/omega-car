package com.example.view.user.ride_booking;

import com.example.dto.RideBookingDto;
import com.example.dto.RideDto;
import com.example.model.BookingStatus;
import com.example.view.components.ActionButton;
import com.example.view.components.AppNotification;
import com.example.view.components.ConfirmDialog;
import com.example.view.components.StatusBadge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Route("bookings")
public class UserRideBookingView extends VerticalLayout implements BeforeLeaveObserver {

    private final WebClient webClient;
    private final String baseUrl;
    private final Binder<RideBookingDto> binder;
    private Dialog currentDialog = null;

    // Filtres
    private TextField carModelFilter;
    private TextField trajetFilter;
    private NumberField minPriceFilter;
    private NumberField maxPriceFilter;

    @Autowired
    public UserRideBookingView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
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
    }

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

        Span title = new Span("Mes Réservations");
        title.getStyle()
                .set("color", "white")
                .set("font-size", "32px")
                .set("font-weight", "700")
                .set("text-shadow", "0 2px 8px rgba(0,0,0,0.2)");

        titleSection.add(icon, title);

        headerContent.add(titleSection);
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

        Span filterTitle = new Span("🔍 Rechercher une réservation");
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
        carModelFilter = createModernTextField("Modèle de voiture", "Ex: Tesla Model 3", VaadinIcon.CAR);
        carModelFilter.addValueChangeListener(e -> applyFilters());

        // Filtre trajet
        trajetFilter = createModernTextField("Trajet", "Ex: Paris", VaadinIcon.MAP_MARKER);
        trajetFilter.addValueChangeListener(e -> applyFilters());

        // Filtre prix minimum
        minPriceFilter = createModernNumberField("Prix min", "0", VaadinIcon.EURO);
        minPriceFilter.addValueChangeListener(e -> applyFilters());

        // Filtre prix maximum
        maxPriceFilter = createModernNumberField("Prix max", "∞", VaadinIcon.EURO);
        maxPriceFilter.addValueChangeListener(e -> applyFilters());

        // Bouton réinitialiser
        Button resetBtn = new Button("Réinitialiser", VaadinIcon.REFRESH.create());
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

        // En-tête de la carte avec le modèle de voiture
        String carInfo = getRideById(booking.getRideId())
                .map(ride -> {
                    if (ride.getCarId() != null) {
                        return "Car ID: " + ride.getCarId().toString();
                    }
                    return "N/A";
                })
                .orElse("N/A");

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
                .map(r -> r.getPickUp() + " → " + r.getDropOff())
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
                .map(r -> String.format("%.2f €", r.getPrice()))
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

        Button editBtn = new Button("Modifier", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editBtn.getStyle()
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease");
        editBtn.addClickListener(e -> openBookingDialog(booking));

        Button takeBtn = new Button("Valider", VaadinIcon.CHECK_CIRCLE.create());
        takeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        takeBtn.getStyle()
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease");
        takeBtn.addClickListener(e -> {
            try {
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                updateBooking(booking.getId().toString(), booking);
                refreshCardsWithData(getAllBookings());
                AppNotification.success("Réservation validée");
            } catch (Exception ex) {
                AppNotification.error("Erreur: " + ex.getMessage());
            }
        });

        Button cancelBtn = new Button("Annuler", VaadinIcon.CLOSE_CIRCLE.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        cancelBtn.getStyle()
                .set("border-radius", "8px")
                .set("transition", "all 0.3s ease");
        cancelBtn.addClickListener(e -> ConfirmDialog.show(
                "Annuler la réservation",
                "Êtes-vous sûr de vouloir annuler cette réservation ?",
                "Annuler",
                () -> {
                    deleteBookingById(booking.getId().toString());
                    refreshCardsWithData(getAllBookings());
                    AppNotification.success("Réservation annulée");
                }));

        actions.add(editBtn, takeBtn, cancelBtn);

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

        H3 dialogTitle = new H3(isUpdate ? "✏️ Modifier la réservation" : "➕ Nouvelle réservation");
        dialogTitle.getStyle()
                .set("color", "white")
                .set("margin", "0")
                .set("font-size", "22px")
                .set("font-weight", "700");

        dialogHeader.add(dialogTitle);

        FormLayout formLayout = new FormLayout();
        formLayout.getStyle().set("padding", "24px 0");

        TextField rideIdField = new TextField("ID du trajet");
        rideIdField.setValue(booking.getRideId() != null ? booking.getRideId().toString() : "");
        rideIdField.setPrefixComponent(VaadinIcon.ROAD.create());
        rideIdField.getStyle().set("--lumo-border-radius", "8px");

        IntegerField seatsField = new IntegerField("Nombre de places");
        seatsField.setMin(1);
        seatsField.setPrefixComponent(VaadinIcon.USERS.create());
        seatsField.getStyle().set("--lumo-border-radius", "8px");

        binder.forField(seatsField)
                .asRequired("Le nombre de places est requis")
                .bind(RideBookingDto::getSeatsBooked, RideBookingDto::setSeatsBooked);

        binder.forField(rideIdField)
                .asRequired("L'ID du trajet est requis")
                .withConverter(
                        s -> s != null && !s.isEmpty() ? UUID.fromString(s) : null,
                        u -> u != null ? u.toString() : "")
                .bind(RideBookingDto::getRideId, RideBookingDto::setRideId);

        binder.readBean(booking);

        formLayout.add(rideIdField, seatsField);

        ActionButton cancel = ActionButton.createSecondary("Annuler", VaadinIcon.CLOSE);
        cancel.getStyle().set("border-radius", "8px");
        cancel.addClickListener(e -> dialog.close());

        ActionButton save = ActionButton.createPrimary(isUpdate ? "Mettre à jour" : "Créer", VaadinIcon.CHECK);
        save.getStyle().set("border-radius", "8px");
        save.addClickListener(e -> {
            try {
                binder.writeBean(booking);
                if (isUpdate) {
                    updateBooking(booking.getId().toString(), booking);
                    AppNotification.success("Réservation mise à jour");
                } else {
                    createBooking(booking);
                    AppNotification.success("Réservation créée");
                }
                refreshGrid();
                dialog.close();
            } catch (ValidationException ex) {
                AppNotification.error("Veuillez corriger les erreurs");
            } catch (Exception ex) {
                AppNotification.error("Erreur: " + ex.getMessage());
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(cancel, save);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        buttons.getStyle().set("padding-top", "16px");

        VerticalLayout layout = new VerticalLayout(dialogHeader, formLayout, buttons);
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