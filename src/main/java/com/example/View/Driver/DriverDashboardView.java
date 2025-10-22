package com.example.View.Driver;

import com.example.Security.SecuredRoles;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route("driver")
@PageTitle("Driver Dashboard")
@RolesAllowed({"Driver"})
public class DriverDashboardView extends VerticalLayout {

    public DriverDashboardView() {
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

        welcomeCard.add(driverIcon, welcomeTitle, description);
        content.add(welcomeCard);

        return content;
    }

    private void applyStyles() {
        getStyle().set("background", "#f9fafb");
    }
}

