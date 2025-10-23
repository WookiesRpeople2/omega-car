package com.example.view.driver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.dto.CarDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "driver/cars", layout = com.example.view.layout.MainLayout.class)
@PageTitle("My Cars")
@RolesAllowed({"Driver"})
public class DriverCarsView extends VerticalLayout implements BeforeEnterObserver {

    private final WebClient webClient;
    private final String baseUrl;
    private final Grid<CarDto> grid = new Grid<>(CarDto.class, false);

    public DriverCarsView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;

        setSizeFull();
        setPadding(true);

        H1 title = new H1("My Cars");
        add(title);

        grid.addColumn(CarDto::getMake).setHeader("Make");
        grid.addColumn(CarDto::getModel).setHeader("Model");
        grid.addColumn(CarDto::getLicensePlate).setHeader("License Plate");
        grid.addColumn(c -> c.isCarValidated() ? "Validated" : "Pending").setHeader("Status");
        grid.setHeight("360px");
        add(grid);

        FormLayout form = new FormLayout();
        TextField make = new TextField("Make");
        TextField model = new TextField("Model");
        TextField plate = new TextField("License Plate");
        Button save = new Button("Create", VaadinIcon.CHECK.create());
        save.addClickListener(e -> createCar(make.getValue(), model.getValue(), plate.getValue()));
        form.add(make, model, plate, save);
        add(form);

        refresh();
    }

    private void refresh() {
        try {
            String token = readAuthTokenFromCookie();
            var list = webClient.get()
                .uri(baseUrl + "/api/cars/my")
                .headers(h -> { if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token); })
                .retrieve()
                .bodyToFlux(CarDto.class)
                .collectList()
                .block();
            grid.setItems(list);
        } catch (Exception ex) {
            Notification.show("Failed to load cars", 3000, Notification.Position.TOP_END).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void createCar(String make, String model, String plate) {
        try {
            CarDto dto = new CarDto();
            dto.setMake(make);
            dto.setModel(model);
            dto.setLicensePlate(plate);
            dto.setCarValidated(false);
            String token = readAuthTokenFromCookie();
            webClient.post()
                .uri(baseUrl + "/api/cars/my")
                .headers(h -> { if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token); })
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(CarDto.class)
                .block();
            Notification.show("Car created; awaiting validation", 3000, Notification.Position.TOP_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refresh();
        } catch (Exception ex) {
            Notification.show("Create failed", 3000, Notification.Position.TOP_END).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String readAuthTokenFromCookie() {
        var req = VaadinService.getCurrentRequest();
        if (req == null || req.getCookies() == null) return null;
        return java.util.Arrays.stream(req.getCookies())
            .filter(c -> "AUTH".equals(c.getName()))
            .map(c -> c.getValue())
            .findFirst()
            .orElse(null);
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
        }
    }
}


