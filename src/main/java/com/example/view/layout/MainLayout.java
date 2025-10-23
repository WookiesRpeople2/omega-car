package com.example.view.layout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.view.admin.cars.CarsView;
import com.example.view.admin.ride_booking.RideBookingView;
import com.example.view.driver.DriverCarsView;
import com.example.view.driver.DriverDashboardView;
import com.example.view.driver.NotificationsView;
import com.example.view.user.UserDashboardView;
import com.example.view.user.ride_booking.UserRideBookingView;
import com.example.dto.UserDto;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinService;

public class MainLayout extends AppLayout {

    private final WebClient webClient;
    private final String baseUrl;

    public MainLayout(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        setPrimarySection(Section.NAVBAR);
        createHeader();
    }

    private void createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        H3 title = new H3("Omega Car");
        title.getStyle().set("margin", "0");
        header.add(title);
        addToNavbar(header);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        buildMenu();
    }

    private void buildMenu() {
        // Clear existing drawer content by recreating menu
        VerticalLayout menu = new VerticalLayout();
        menu.setPadding(true);
        menu.setSpacing(false);
        menu.setWidthFull();

        String role = fetchCurrentUserRole();

        if ("User".equalsIgnoreCase(role)) {
            menu.add(new RouterLink("Dashboard", UserDashboardView.class));
            menu.add(new RouterLink("Bookings", UserRideBookingView.class));
        }
        if ("Driver".equalsIgnoreCase(role)) {
            menu.add(new RouterLink("Driver Dashboard", DriverDashboardView.class));
            menu.add(new RouterLink("My Cars", DriverCarsView.class));
            menu.add(new RouterLink("Notifications", NotificationsView.class));
        }
        if ("Admin".equalsIgnoreCase(role)) {
            menu.add(new RouterLink("Admin Cars", CarsView.class));
            menu.add(new RouterLink("Admin Bookings", RideBookingView.class));
        }

        if (menu.getComponentCount() == 0) {
            menu.add(new Span("No navigation available"));
        }

        setDrawerOpened(true);
        addToDrawer(menu);
    }

    private String fetchCurrentUserRole() {
        try {
            String token = readAuthTokenFromCookie();
            UserDto me = webClient.get()
                .uri(baseUrl + "/api/users/me")
                .headers(h -> { if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token); })
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
            return me != null ? me.getRole() : null;
        } catch (Exception ignored) {
            return null;
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
}


