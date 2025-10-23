package com.example.view.driver;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.model.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;

import jakarta.annotation.security.RolesAllowed;

@Route("notifications")
@PageTitle("Notifications")
@RolesAllowed({"Driver", "Admin"})
public class NotificationsView extends VerticalLayout implements BeforeEnterObserver {

    private final WebClient webClient;
    private final String baseUrl;
    private final Grid<Notification> grid = new Grid<>(Notification.class, false);

    public NotificationsView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;

        setSizeFull();

        grid.addColumn(Notification::getTitle).setHeader("Title");
        grid.addColumn(Notification::getMessage).setHeader("Message");
        grid.addColumn(n -> n.getType().name()).setHeader("Type");
        grid.addComponentColumn(n -> new Button("Mark read", e -> markRead(n.getId())));
        grid.setSizeFull();
        add(grid);

        refresh();
    }

    private void refresh() {
        String token = readAuthCookie();
        List<Notification> list = webClient.get()
            .uri(baseUrl + "/api/notifications")
            .header("Authorization", token != null ? "Bearer " + token : "")
            .retrieve()
            .bodyToFlux(Notification.class)
            .collectList()
            .block();
        grid.setItems(list != null ? list : List.of());
    }

    private void markRead(UUID id) {
        String token = readAuthCookie();
        webClient.post()
            .uri(baseUrl + "/api/notifications/" + id + "/read")
            .header("Authorization", token != null ? "Bearer " + token : "")
            .retrieve()
            .toBodilessEntity()
            .block();
        refresh();
    }

    private String readAuthCookie() {
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


