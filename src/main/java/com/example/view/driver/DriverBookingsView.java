package com.example.view.driver;

import com.example.dto.RideBookingDto;
import com.example.dto.RideDto;
import com.example.dto.DriverLocationEvent;
import com.example.view.components.AppNotification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Route(value = "driver/bookings", layout = com.example.view.layout.MainLayout.class)
@RolesAllowed({"Driver"})
public class DriverBookingsView extends VerticalLayout {

    private final WebClient webClient;
    private final String baseUrl;
    private com.example.service.DriverLocationUiBroadcaster.Registration locationReg;
    private com.example.service.RideBookingUiBroadcaster.Registration navReg;
    private Div mapContainer;

    public DriverBookingsView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("My Bookings");
        add(title);

        Div list = new Div();
        list.getStyle().set("display", "grid").set("gap", "12px");
        add(list);

        // Embedded map container
        mapContainer = new Div();
        mapContainer.setId("driver-map");
        mapContainer.getStyle()
            .set("height", "360px")
            .set("border", "1px solid #e5e7eb")
            .set("border-radius", "8px")
            .set("overflow", "hidden")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
        add(mapContainer);

        initMapJs();

        // Live driver location updates: move marker and redraw route
        var locBroadcaster = com.example.view.SpringContext.getBean(com.example.service.DriverLocationUiBroadcaster.class);
        locationReg = locBroadcaster.register(evt -> {
            if (evt instanceof DriverLocationEvent e) {
                UI ui = getUI().orElse(null);
                if (ui == null) return;
                ui.access(() -> ui.getPage().executeJs(
                    "(async ()=>{try{const me=await fetch('/api/users/me',{credentials:'include'}).then(r=>r.ok?r.json():null);if(!me||!me.id)return; if(me.id!==$0)return; if(window.__driverMap){ window.__driverMap.updateDriver($1,$2); if(window.__driverMap.target){ window.__driverMap.drawRoute(); } } }catch(e){}})();",
                    e.getDriverId().toString(), e.getLatitude(), e.getLongitude()
                ));
            }
        });

        // Navigation target change (e.g., to dropoff after picked-up)
        var rbBroadcaster = com.example.view.SpringContext.getBean(com.example.service.RideBookingUiBroadcaster.class);
        navReg = rbBroadcaster.register(evt -> {
            if (evt instanceof com.example.service.RideBookingUiBroadcaster.NavigationTargetChangedEvent e) {
                UI ui = getUI().orElse(null);
                if (ui == null) return;
                ui.access(() -> ui.getPage().executeJs(
                    "(async ()=>{try{const me=await fetch('/api/users/me',{credentials:'include'}).then(r=>r.ok?r.json():null);if(!me||!me.id)return; if(me.id!==$0)return; if(window.__driverMap){ window.__driverMap.setTarget($1,$2); } }catch(e){}})();",
                    e.getDriverId().toString(), e.getLat(), e.getLon()
                ));
            }
        });

        refresh(list);
    }

    private void refresh(Div list) {
        list.removeAll();
        for (RideBookingDto b : fetchMyBookings()) {
            Div card = new Div();
            card.getStyle().set("background", "white").set("padding", "12px").set("border-radius", "8px").set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
            RideDto ride = fetchRide(b.getRideId());
            String from = ride != null && ride.getPickupName() != null ? ride.getPickupName() : (ride != null ? ride.getPickUp() : "");
            String to = ride != null && ride.getDropoffName() != null ? ride.getDropoffName() : (ride != null ? ride.getDropOff() : "");
            Div info = new Div();
            info.setText((ride != null ? ride.getDepartureTime() : "") + " | " + from + " → " + to);
            Button go = new Button("Go", VaadinIcon.ARROW_CIRCLE_RIGHT.create());
            go.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            go.addClickListener(e -> navigateToMap(b.getId()));
            HorizontalLayout row = new HorizontalLayout(info, go);
            row.setWidthFull();
            row.setJustifyContentMode(JustifyContentMode.BETWEEN);
            card.add(row);
            list.add(card);
        }
    }

    private void navigateToMap(java.util.UUID bookingId) {
        if (bookingId == null) return;
        UI.getCurrent().navigate("driver/navigate/" + bookingId.toString());
    }

    private List<RideBookingDto> fetchMyBookings() {
        try {
            String token = readAuthTokenFromCookie();
            Flux<RideBookingDto> flux = webClient.get()
                .uri(baseUrl + "/api/bookings/my")
                .headers(h -> { if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token); })
                .retrieve()
                .bodyToFlux(RideBookingDto.class);
            return flux.collectList().block();
        } catch (Exception e) {
            return List.of();
        }
    }

    private RideDto fetchRide(UUID id) {
        try {
            String token = readAuthTokenFromCookie();
            return webClient.get()
                .uri(baseUrl + "/api/rides/" + id)
                .headers(h -> { if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token); })
                .retrieve()
                .bodyToMono(RideDto.class)
                .block();
        } catch (Exception e) {
            return null;
        }
    }

    private String readAuthTokenFromCookie() {
        var req = VaadinService.getCurrentRequest();
        if (req == null || req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
            .filter(c -> "AUTH".equals(c.getName()))
            .map(c -> c.getValue())
            .findFirst()
            .orElse(null);
    }

    private void initMapJs() {
        UI.getCurrent().getPage().executeJs(
            "(function(){\n" +
            " if(!window.__leaflet_loaded){\n" +
            "  const link=document.createElement('link');link.rel='stylesheet';link.href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';document.head.appendChild(link);\n" +
            "  const s=document.createElement('script');s.src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';s.onload=function(){window.__leaflet_loaded=true; init();};document.head.appendChild(s);\n" +
            " } else { init(); }\n" +
            " function init(){\n" +
            "  const el=document.getElementById('driver-map'); if(!el) return;\n" +
            "  if(window.__driverMap && window.__driverMap.map){ return; }\n" +
            "  const map=L.map(el).setView([0,0], 2);\n" +
            "  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{ attribution: '&copy; OpenStreetMap'}).addTo(map);\n" +
            "  const driverMarker=L.marker([0,0]);\n" +
            "  let targetMarker=null;\n" +
            "  let routeLine=null;\n" +
            "  function updateDriver(lat,lng){ driverMarker.setLatLng([lat,lng]).addTo(map); if(targetMarker){ fit(); } }\n" +
            "  function setTarget(lat,lng){ if(!targetMarker){ targetMarker=L.marker([lat,lng],{opacity:0.8}); targetMarker.addTo(map);} else { targetMarker.setLatLng([lat,lng]); } window.__driverMap.target=[lat,lng]; drawRoute(); fit(); }\n" +
            "  function drawRoute(){ if(!window.__driverMap || !window.__driverMap.target) return; const a=driverMarker.getLatLng(); const b=L.latLng(window.__driverMap.target[0], window.__driverMap.target[1]); if(!a||!b) return; if(routeLine){ map.removeLayer(routeLine);} routeLine=L.polyline([a,b],{color:'#2563eb',weight:4,opacity:0.8}).addTo(map);}\n" +
            "  function fit(){ const pts=[]; const a=driverMarker.getLatLng(); if(a) pts.push(a); if(targetMarker) pts.push(targetMarker.getLatLng()); if(pts.length>=2){ map.fitBounds(L.latLngBounds(pts), {padding:[30,30]}); } }\n" +
            "  window.__driverMap={ map: map, updateDriver: updateDriver, setTarget: setTarget, drawRoute: drawRoute, target: null };\n" +
            " }\n" +
            "})();"
        );
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (locationReg != null) { locationReg.remove(); locationReg = null; }
        if (navReg != null) { navReg.remove(); navReg = null; }
    }
}


