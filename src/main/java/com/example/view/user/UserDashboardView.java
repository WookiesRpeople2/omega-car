package com.example.view.user;

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

@Route("user")
@PageTitle("User Dashboard")
@RolesAllowed({"User"})
public class UserDashboardView extends VerticalLayout {

    public UserDashboardView() {
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

        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("64px");
        userIcon.getStyle().set("color", "#667eea").set("margin-bottom", "20px");

        H2 welcomeTitle = new H2("Welcome to Your Dashboard!");
        welcomeTitle.getStyle().set("margin", "0 0 16px 0").set("color", "#1f2937");

        Paragraph description = new Paragraph(
            "As a user, you can book rides, view your booking history, and manage your profile. " +
            "Explore the menu to get started!"
        );
        description.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "16px")
            .set("margin", "0");

        welcomeCard.add(userIcon, welcomeTitle, description);
        content.add(welcomeCard);

        return content;
    }

    private void applyStyles() {
        getStyle().set("background", "#f9fafb");
    }
}

