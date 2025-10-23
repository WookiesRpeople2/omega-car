package com.example.view.user;

import java.util.List;
import java.util.UUID;

import com.example.dto.DriverLocationEvent;
import com.example.service.DriverLocationService;
import com.example.service.DriverLocationUiBroadcaster;
import com.example.service.DriverLocationUiBroadcaster.Registration;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "user", layout = com.example.view.layout.MainLayout.class)
@PageTitle("User Dashboard")
@RolesAllowed({"User"})
public class UserDashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final DriverLocationService driverLocationService;
    private final DriverLocationUiBroadcaster broadcaster;

    private Registration registration;
    private final String mapElementId = "map-" + java.util.UUID.randomUUID();
    private Div mapDiv;

    public UserDashboardView(DriverLocationService driverLocationService, DriverLocationUiBroadcaster broadcaster) {
        this.driverLocationService = driverLocationService;
        this.broadcaster = broadcaster;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        add(createHeader());
        add(createContent());
        applyStyles();
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("color", "white")
            .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

        Icon dashIcon = VaadinIcon.DASHBOARD.create();
        dashIcon.setSize("32px");

        H1 title = new H1("User Dashboard");
        title.getStyle().set("margin", "0").set("color", "white");

        HorizontalLayout titleSection = new HorizontalLayout(dashIcon, title);
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setSpacing(true);

        header.add(titleSection);
        return header;
    }

    private VerticalLayout createContent() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setPadding(true);
        wrapper.setSpacing(true);

        HorizontalLayout content = new HorizontalLayout();
        content.setSizeFull();
        content.setSpacing(true);

        // Map container
        mapDiv = new Div();
        mapDiv.setId(mapElementId);
        mapDiv.setWidthFull();
        mapDiv.setHeight("700px");
        mapDiv.getStyle().set("border-radius", "12px").set("overflow", "hidden").set("box-shadow", "0 4px 12px rgba(0,0,0,0.08)");

        // Sidebar with actions
        VerticalLayout sideBar = new VerticalLayout();
        sideBar.setWidth("380px");
        sideBar.setPadding(true);
        sideBar.getStyle().set("background", "white").set("border-radius", "12px").set("box-shadow", "0 4px 12px rgba(0,0,0,0.08)");

        Button openBookings = new Button("Open Rides to Book", VaadinIcon.CALENDAR.create(), e -> UI.getCurrent().navigate("bookings"));
        openBookings.setWidthFull();

        sideBar.add(openBookings);

        content.add(sideBar, mapDiv);
        content.setFlexGrow(1, mapDiv);

        wrapper.add(content);
        wrapper.setFlexGrow(1, content);
        return wrapper;
    }

    private void applyStyles() {
        getStyle().set("background", "#f9fafb");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        initLeaflet();

        List<DriverLocationEvent> actives = driverLocationService.getAllActiveDrivers();
        for (DriverLocationEvent e : actives) {
            upsertMarker(e.getDriverId(), e.getLatitude(), e.getLongitude(), e.getCarId());
        }

        registration = broadcaster.register(evt -> {
            UI ui = getUI().orElse(null);
            if (ui == null) return;
            ui.access(() -> {
                if (evt instanceof com.example.service.DriverLocationUiBroadcaster.DriverOfflineEvent off) {
                    removeMarker(off.getDriverId());
                } else if (evt instanceof com.example.dto.DriverLocationEvent) {
                    com.example.dto.DriverLocationEvent e = (com.example.dto.DriverLocationEvent) evt;
                    upsertMarker(e.getDriverId(), e.getLatitude(), e.getLongitude(), e.getCarId());
                }
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private void initLeaflet() {
        UI ui = UI.getCurrent();
        ui.getPage().addStyleSheet("https://unpkg.com/leaflet@1.9.4/dist/leaflet.css");
        ui.getPage().addJavaScript("https://unpkg.com/leaflet@1.9.4/dist/leaflet.js");
		// Ensure Leaflet (window.L) is available before initializing map and markers.
		ui.getPage().executeJs("window.__omegaEnsureLeafletReady = window.__omegaEnsureLeafletReady || ((cb)=>{if(window.L){cb();}else{let i=0;const t=setInterval(()=>{if(window.L){clearInterval(t);cb();}if(++i>200){clearInterval(t);}},50);}});");
		ui.getPage().executeJs("window.__initLeafletMap = window.__initLeafletMap || ((id) => {\n" +
			" if (!window.__maps) window.__maps = {};\n" +
			" if (window.__maps[id]) return;\n" +
			" const m = L.map(id).setView([48.8566, 2.3522], 12);\n" +
			" L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(m);\n" +
			" const carIcon = L.icon({ iconUrl: 'https://twemoji.maxcdn.com/v/latest/72x72/1f697.png', iconSize: [36,36], iconAnchor:[18,18] });\n" +
			" window.__maps[id] = { map: m, markers: {}, carIcon };\n" +
			"});");
        ui.getPage().executeJs("window.__upsertDriverMarker = window.__upsertDriverMarker || ((id, driverId, lat, lng, carId) => {\n" +
			" const attempt = () => {\n" +
			"   const ctx = window.__maps && window.__maps[id];\n" +
			"   if (!ctx) { setTimeout(attempt, 100); return; }\n" +
			"   const key = String(driverId);\n" +
			"   let mk = ctx.markers[key];\n" +
			"   if (!mk) { mk = L.marker([lat, lng], { icon: ctx.carIcon }).addTo(ctx.map);\n" +
			"     mk.on('click', async () => {\n" +
			"       try {\n" +
			"         const resp = await fetch('/api/driver-locations/' + key, { credentials: 'include' });\n" +
			"         if (resp && resp.ok) { window.location.href = '/bookings?driverId=' + key + (carId ? ('&carId=' + carId) : ''); }\n" +
			"         else { const n=document.createElement('vaadin-notification'); n.renderer = function(root){ root.textContent = 'Driver is offline. Booking disabled.'; }; n.duration=3000; document.body.appendChild(n); n.opened=true; }\n" +
			"       } catch (e) {}\n" +
			"     });\n" +
			"     ctx.markers[key] = mk;\n" +
			"   } else { mk.setLatLng([lat, lng]); }\n" +
			" };\n" +
			" attempt();\n" +
			"});");
		ui.getPage().executeJs("window.__removeDriverMarker = window.__removeDriverMarker || ((id, driverId) => {\n" +
			" const attempt = () => {\n" +
			"   const ctx = window.__maps && window.__maps[id];\n" +
			"   if (!ctx) { setTimeout(attempt, 100); return; }\n" +
			"   const key = String(driverId);\n" +
			"   const mk = ctx.markers[key];\n" +
			"   if (mk) { ctx.map.removeLayer(mk); delete ctx.markers[key]; }\n" +
			" };\n" +
			" attempt();\n" +
			"});");
		ui.getPage().executeJs("window.__omegaEnsureLeafletReady(()=>window.__initLeafletMap($0));", mapElementId);
    }

    private void upsertMarker(UUID driverId, double lat, double lng) {
        UI.getCurrent().getPage().executeJs("window.__upsertDriverMarker($0, $1, $2, $3, $4);", mapElementId, driverId.toString(), lat, lng, null);
    }

    private void upsertMarker(UUID driverId, double lat, double lng, java.util.UUID carId) {
        UI.getCurrent().getPage().executeJs("window.__upsertDriverMarker($0, $1, $2, $3, $4);", mapElementId, driverId.toString(), lat, lng, carId != null ? carId.toString() : null);
    }

	private void removeMarker(UUID driverId) {
		UI.getCurrent().getPage().executeJs("window.__removeDriverMarker($0, $1);", mapElementId, driverId.toString());
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
