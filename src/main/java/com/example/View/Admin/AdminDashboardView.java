package com.example.View.Admin;

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
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.RolesAllowed;

@Route("admin")
@PageTitle("Admin Dashboard")
@RolesAllowed({"Admin"})
public class AdminDashboardView extends VerticalLayout {

    public AdminDashboardView() {
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

        Icon adminIcon = VaadinIcon.COG.create();
        adminIcon.setSize("32px");

        H1 title = new H1("Admin Dashboard");
        title.getStyle().set("margin", "0").set("color", "white");

        HorizontalLayout titleSection = new HorizontalLayout(adminIcon, title);
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

        Icon shieldIcon = VaadinIcon.SHIELD.create();
        shieldIcon.setSize("64px");
        shieldIcon.getStyle().set("color", "#f5576c").set("margin-bottom", "20px");

        H2 welcomeTitle = new H2("Welcome to Admin Dashboard!");
        welcomeTitle.getStyle().set("margin", "0 0 16px 0").set("color", "#1f2937");

        Paragraph description = new Paragraph(
            "As an administrator, you have full control over the system. " +
            "Manage users, vehicles, and system settings."
        );
        description.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "16px")
            .set("margin", "0 0 20px 0");

        RouterLink carsLink = new RouterLink("Manage Vehicles", com.example.View.Admin.Cars.CarsView.class);
        carsLink.getStyle()
            .set("display", "inline-block")
            .set("padding", "12px 24px")
            .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            .set("color", "white")
            .set("text-decoration", "none")
            .set("border-radius", "8px")
            .set("font-weight", "600")
            .set("box-shadow", "0 4px 8px rgba(99, 102, 241, 0.3)");

        welcomeCard.add(shieldIcon, welcomeTitle, description, carsLink);
        content.add(welcomeCard);

        return content;
    }

    private void applyStyles() {
        getStyle().set("background", "#f9fafb");
    }
}

