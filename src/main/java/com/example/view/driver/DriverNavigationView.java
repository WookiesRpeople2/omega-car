package com.example.view.driver;

import com.example.dto.RideBookingDto;
import com.example.dto.RideDto;
import com.example.view.components.AppNotification;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.UUID;

@Route(value = "driver/navigate/:bookingId", layout = com.example.view.layout.MainLayout.class)
@RolesAllowed({"Driver"})
public class DriverNavigationView extends VerticalLayout implements BeforeEnterObserver {

    private final WebClient webClient;
    private final String baseUrl;
    private UUID bookingId;
    private RideDto ride;
    private Div mapContainer;
    private Span etaLabel;

    public DriverNavigationView(WebClient webClient, @Value("${app.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Navigation");
        add(title);

        etaLabel = new Span("ETA: --");
        etaLabel.getStyle()
            .set("font-weight", "600")
            .set("color", "#374151");
        add(etaLabel);

        mapContainer = new Div();
        mapContainer.setId("driver-nav-map");
        mapContainer.getStyle()
            .set("width", "100%")
            .set("height", "70vh")
            .set("border", "1px solid #e5e7eb")
            .set("border-radius", "8px")
            .set("overflow", "hidden")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
        add(mapContainer);

        HorizontalLayout actions = new HorizontalLayout();
        Button openGmaps = new Button("Open in Google Maps", VaadinIcon.EXTERNAL_LINK.create());
        openGmaps.addClickListener(e -> UI.getCurrent().getPage().executeJs(
            "if(window.__driverNavMap&&window.__driverNavMap.openGMaps){window.__driverNavMap.openGMaps();}else{window.alert('Map not ready');}"
        ));
        Button back = new Button("Back", VaadinIcon.ARROW_LEFT.create());
        back.addClickListener(e -> UI.getCurrent().navigate("driver/bookings"));
        actions.add(openGmaps, back);
        add(actions);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        var param = event.getRouteParameters().get("bookingId");
        if (param.isEmpty()) {
            AppNotification.error("Missing booking id");
            UI.getCurrent().navigate("driver/bookings");
            return;
        }
        try {
            this.bookingId = UUID.fromString(param.get());
        } catch (IllegalArgumentException ex) {
            AppNotification.error("Invalid booking id");
            UI.getCurrent().navigate("driver/bookings");
            return;
        }

        // Fetch booking and ride
        RideBookingDto booking = fetchBooking(this.bookingId);
        if (booking == null) {
            AppNotification.error("Booking not found");
            UI.getCurrent().navigate("driver/bookings");
            return;
        }
        this.ride = fetchRide(booking.getRideId());
        if (this.ride == null) {
            AppNotification.error("Ride not found");
            UI.getCurrent().navigate("driver/bookings");
            return;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (ride != null) {
            initMapWithTarget(ride.getPickupLat(), ride.getPickupLon());
        }
        // Listen for nav target change events to switch to dropoff in realtime
        var rbBroadcaster = com.example.view.SpringContext.getBean(com.example.service.RideBookingUiBroadcaster.class);
        rbBroadcaster.register(evt -> {
            if (evt instanceof com.example.service.RideBookingUiBroadcaster.NavigationTargetChangedEvent e) {
                UI ui = getUI().orElse(null);
                if (ui == null) return;
                ui.access(() -> ui.getPage().executeJs(
                    "(function(){ if(window.__driverNavMap){ window.__driverNavMap.setDropoffTarget($0,$1); } })();",
                    e.getLat(), e.getLon()
                ));
                ui.access(() -> AppNotification.success("Switching to dropoff destination"));
            }
        });
    }

    private void initMapWithTarget(Double lat, Double lon) {
        if (lat == null || lon == null) {
            AppNotification.error("Target location missing");
            return;
        }
        UI.getCurrent().getPage().executeJs(
            "(function(){\n" +
            " const ensureLeaflet=(cb)=>{ if(window.L){cb();return;} const link=document.createElement('link');link.rel='stylesheet';link.href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';document.head.appendChild(link); const s=document.createElement('script');s.src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';s.onload=cb;document.head.appendChild(s); };\n" +
            " ensureLeaflet(()=>{ setTimeout(()=>{ const el=document.getElementById('driver-nav-map'); if(!el) return; const map=L.map(el).setView([$0,$1], 14); L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{ attribution: '&copy; OpenStreetMap'}).addTo(map); const carIcon=L.divIcon({className:'driver-icon',html:'🚗',iconSize:[48,48],iconAnchor:[24,24]}); const userIcon=L.divIcon({className:'user-icon',html:'🧍',iconSize:[48,48],iconAnchor:[24,24]}); const flagIcon=L.divIcon({className:'dropoff-flag',html:'🏁',iconSize:[48,48],iconAnchor:[24,24]}); const driver=L.marker([$0,$1],{icon:carIcon}).addTo(map); const target=L.marker([$0,$1],{icon:userIcon}).addTo(map); let routeLine=null; const pickup=[$0,$1]; let dropoff=null; let mode='TO_PICKUP'; const formatDuration=(s)=>{ s=Math.round(s); const h=Math.floor(s/3600); const m=Math.floor((s%3600)/60); if(h>0){ return h+'h '+m+'m'; } return m+' min'; }; const requestRoute=(fromLa,fromLo,toLa,toLo)=>{ if(!fromLa||!fromLo||!toLa||!toLo) return; const url='https://router.project-osrm.org/route/v1/driving/'+fromLo+','+fromLa+';'+toLo+','+toLa+'?overview=full&geometries=geojson'; fetch(url).then(r=>r.json()).then(data=>{ try{ if(!data||!data.routes||!data.routes[0]) return; const route=data.routes[0]; const coords=route.geometry.coordinates.map(c=>[c[1],c[0]]); if(routeLine){ map.removeLayer(routeLine);} routeLine=L.polyline(coords,{color:'#2563eb',weight:6,opacity:0.95}).addTo(map); const b=L.latLngBounds(coords); map.fitBounds(b,{padding:[30,30]}); if($2){ $2.textContent='ETA: '+formatDuration(route.duration)+' ('+(Math.round(route.distance/100)/10)+' km)'; } }catch(e){} }).catch(()=>{}); }; target.setLatLng([$0,$1]); if(navigator.geolocation){ navigator.geolocation.watchPosition((p)=>{ const {latitude,longitude}=p.coords; driver.setLatLng([latitude,longitude]); if(mode==='TO_PICKUP'){ const t=target.getLatLng(); if(t){ requestRoute(latitude,longitude,t.lat,t.lng); } } },()=>{}, {enableHighAccuracy:true, maximumAge: 1000, timeout: 10000}); } window.__driverNavMap={ setDropoffTarget:(la,lo)=>{ mode='TO_DROPOFF'; dropoff=[la,lo]; const pickupPoint = target.getLatLng(); driver.setIcon(carIcon); if(pickupPoint){ driver.setLatLng(pickupPoint); } target.setIcon(flagIcon); target.setLatLng([la,lo]); requestRoute(pickup[0],pickup[1],la,lo); }, openGMaps:()=>{ let origin=null, dest=null; if(mode==='TO_PICKUP'){ const a=driver.getLatLng(); const b=target.getLatLng(); if(a&&b){ origin=a; dest=b; } } else { const a=L.latLng(pickup[0],pickup[1]); const b=dropoff?L.latLng(dropoff[0],dropoff[1]):null; if(a&&b){ origin=a; dest=b; } } if(origin&&dest){ const url='https://www.google.com/maps/dir/?api=1&origin='+origin.lat+','+origin.lng+'&destination='+dest.lat+','+dest.lng+'&travelmode=driving'; window.open(url,'_blank'); } else { alert('Route not ready'); } } }; }, 0); });\n" +
            "})();",
            lat, lon, etaLabel
        );
    }

    private RideBookingDto fetchBooking(UUID id) {
        try {
            String token = readAuthTokenFromCookie();
            return webClient.get()
                .uri(baseUrl + "/api/bookings/" + id)
                .headers(h -> { if (token != null && !token.isEmpty()) h.set("Authorization", "Bearer " + token); })
                .retrieve()
                .bodyToMono(RideBookingDto.class)
                .block();
        } catch (Exception e) {
            return null;
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
}


