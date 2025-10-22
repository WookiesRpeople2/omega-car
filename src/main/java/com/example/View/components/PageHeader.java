package com.example.View.components;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

public class PageHeader extends HorizontalLayout {
    
    private final H1 title;
    private final Icon icon;
    
    public PageHeader(String titleText, VaadinIcon iconType) {
        addClassName("page-header");
        setWidthFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(true);
        
        icon = iconType.create();
        icon.setSize("32px");
        icon.getStyle().set("color", "#6366f1");
        
        title = new H1(titleText);
        title.getStyle()
            .set("margin", "0")
            .set("font-size", "28px")
            .set("font-weight", "700")
            .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
            .set("color", "white")
            .set("-webkit-background-clip", "text")
            .set("background-clip", "text");
        
        add(icon, title);
    }
    
    public void setTitle(String newTitle) {
        title.setText(newTitle);
    }
    
    public void setIcon(VaadinIcon newIcon) {
        icon.getElement().setAttribute("icon", newIcon.name().toLowerCase().replace('_', '-'));
    }
}