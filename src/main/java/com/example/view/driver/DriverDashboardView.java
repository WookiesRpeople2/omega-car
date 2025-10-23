package com.example.view.driver;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.example.dto.CarDto;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import jakarta.annotation.security.RolesAllowed;
import com.example.service.DriverNotificationBroadcaster;

@Route("driver")
@PageTitle("Driver Dashboard")
@RolesAllowed({"Driver"})
public class DriverDashboardView extends VerticalLayout implements BeforeEnterObserver {

    // Driver location streaming handled via JS Geolocation API; notification subscription below
    private DriverNotificationBroadcaster.Registration bookingReg;
    private ComboBox<CarDto> carSelect;

    public DriverDashboardView(DriverNotificationBroadcaster driverNotificationBroadcaster) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        
        add(createHeader());
        add(createContent());
        applyStyles();

        bookingReg = driverNotificationBroadcaster.register(evt -> {
            UI ui = getUI().orElse(null);
            if (ui == null) return;
            // Only notify if this UI belongs to the driver; check via /api/users/me id matches
            ui.access(() -> ui.getPage().executeJs(
                "(async ()=>{try{const me=await fetch('/api/users/me',{credentials:'include'}).then(r=>r.ok?r.json():null);if(!me||!me.id)return; if(me.id!==$0)return; const n=document.createElement('vaadin-notification'); n.renderer = function(root){ root.textContent = 'New booking assigned (ID: '+$1+')'; }; n.duration=4000; document.body.appendChild(n); n.opened=true;}catch(e){}})();",
                evt.getDriverId().toString(), evt.getBookingId().toString()
            ));
        });
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)")
            .set("color", "white")
            .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)");

        Icon carIcon = VaadinIcon.CAR.create();
        carIcon.setSize("32px");

        H1 title = new H1("Driver Dashboard");
        title.getStyle().set("margin", "0").set("color", "white");

        HorizontalLayout titleSection = new HorizontalLayout(carIcon, title);
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setSpacing(true);

        header.add(titleSection);
        return header;
    }

    private VerticalLayout createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div welcomeCard = new Div();
        welcomeCard.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "40px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            .set("text-align", "center")
            .set("max-width", "600px");

        Icon driverIcon = VaadinIcon.TAXI.create();
        driverIcon.setSize("64px");
        driverIcon.getStyle().set("color", "#f5576c").set("margin-bottom", "20px");

        H2 welcomeTitle = new H2("Welcome to Your Driver Dashboard!");
        welcomeTitle.getStyle().set("margin", "0 0 16px 0").set("color", "#1f2937");

        Paragraph description = new Paragraph(
            "As a driver, you can manage your rides, view your earnings, and accept new ride requests. " +
            "Start providing rides and earn money!"
        );
        description.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "16px")
            .set("margin", "0");

        carSelect = new ComboBox<>("Active Car");
        carSelect.setItemLabelGenerator(c -> c.getMake() + " " + c.getModel() + " (" + c.getLicensePlate() + ")" + (c.isCarValidated()?"":" - pending"));
        carSelect.setWidthFull();
        carSelect.getStyle().set("margin-top", "12px");

        Button toggle = new Button("Start Location Sharing", VaadinIcon.PLAY.create());
        toggle.getStyle().set("margin-top", "16px");
        toggle.addClickListener(e -> {
            String current = toggle.getText();
            if ("Start Location Sharing".equals(current)) {
                if (carSelect.getValue() == null) {
                    UI.getCurrent().getPage().executeJs("window.alert('Please select a car.');");
                    return;
                }
                if (!carSelect.getValue().isCarValidated()) {
                    UI.getCurrent().getPage().executeJs("window.alert('Selected car is not validated yet.');");
                    return;
                }
                startGeoWatchWithCar(carSelect.getValue().getId());
                toggle.setText("Stop Location Sharing");
                toggle.setIcon(VaadinIcon.STOP.create());
            } else {
                stopGeoWatch();
                toggle.setText("Start Location Sharing");
                toggle.setIcon(VaadinIcon.PLAY.create());
                // publish offline to backend so UIs remove marker and disable booking
                UI.getCurrent().getPage().executeJs(
                    "(async ()=>{try{const me=await fetch('/api/users/me',{credentials:'include'}).then(r=>r.ok?r.json():null);if(!me||!me.id)return; await fetch('/api/driver-locations/'+me.id+'/offline',{method:'POST',credentials:'include'});}catch(e){}})();"
                );
            }
        });

        welcomeCard.add(driverIcon, welcomeTitle, description, carSelect, toggle);
        content.add(welcomeCard);

        return content;
    }

    private void applyStyles() {
        getStyle().set("background", "#f9fafb");
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Use authenticated principal email as key; backend maps email->user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            // We don't have the UUID here, so let backend accept email, but our API expects UUID.
            // As a simpler approach, let server accept updates at /me later. For now, we use email string as ID path.
            // Better: query /api/users/me to get UUID and store. We'll do that client-side via fetch.
        }
        // Load my cars into the combobox
        UI.getCurrent().getPage().executeJs(
            "(async (combo)=>{try{const cars=await fetch('/api/cars/my',{credentials:'include'}).then(r=>r.ok?r.json():[]); const items=cars; const map=new Map(items.map(i=>[i.id,i])); combo._extItems = map; const labels = items.map(i=> i.make+' '+i.model+' ('+i.licensePlate+')'+(i.carValidated?'':' - pending')); combo.items = labels; combo.__ids = items.map(i=> i.id); combo.addEventListener('value-changed', (e)=>{ const idx = combo.items.indexOf(combo.value); if (idx>=0) combo.__selected = combo.__ids[idx]; }); }catch(e){}})(this)",
            carSelect
        );
        // Do not auto-start; user controls via toggle
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        stopGeoWatch();
        if (bookingReg != null) { bookingReg.remove(); bookingReg = null; }
    }

    private void startGeoWatchWithCar(java.util.UUID carId) {
        UI ui = UI.getCurrent();
        // Helper JS to resolve current user id and then watch position
        ui.getPage().executeJs(
            "(async () => {\n" +
            "  try {\n" +
            "    const me = await fetch('/api/users/me', {credentials:'include'}).then(r=>r.ok?r.json():null);\n" +
            "    if (!me || !me.id) return;\n" +
            "    const driverId = me.id;\n" +
            "    if (!navigator.geolocation) return;\n" +
            "    const postUpdate = (lat, lng) => {\n" +
            "      fetch(`/api/driver-locations/${driverId}`, {\n" +
            "        method: 'POST', credentials:'include', headers: { 'Content-Type':'application/json' },\n" +
            "        body: JSON.stringify({ latitude: lat, longitude: lng, carId: '$0' })\n" +
            "      }).catch(()=>{});\n" +
            "    };\n" +
            "    let lastSent = 0;\n" +
            "    const throttleMs = 2000;\n" +
            "    const watchId = navigator.geolocation.watchPosition(pos => {\n" +
            "      const now = Date.now();\n" +
            "      if (now - lastSent < throttleMs) return;\n" +
            "      lastSent = now;\n" +
            "      const { latitude, longitude } = pos.coords;\n" +
            "      postUpdate(latitude, longitude);\n" +
            "    }, err => {}, { enableHighAccuracy: true, maximumAge: 1000, timeout: 10000 });\n" +
            "    window.__omega_driver_watch = watchId;\n" +
            "  } catch(e) {}\n" +
            "})();",
            carId.toString()
        );
    }

    private void stopGeoWatch() {
        UI ui = UI.getCurrent();
        ui.getPage().executeJs(
            "if (window.__omega_driver_watch != null && navigator.geolocation) {\n" +
            "  try { navigator.geolocation.clearWatch(window.__omega_driver_watch); } catch(e) {}\n" +
            "  window.__omega_driver_watch = null;\n" +
            "}"
        );
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

