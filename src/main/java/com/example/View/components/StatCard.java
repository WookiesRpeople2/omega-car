package com.example.View.components;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class StatCard extends VerticalLayout {
    
    public StatCard(String label, String value, VaadinIcon iconType, String color) {
        addClassName("stat-card");
        setPadding(true);
        setSpacing(false);
        
        HorizontalLayout iconContainer = new HorizontalLayout();
        iconContainer.addClassName("icon-container");
        iconContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        iconContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
        Icon cardIcon = iconType.create();
        cardIcon.setSize("24px");
        cardIcon.getStyle().set("color", color);
        iconContainer.add(cardIcon);
        iconContainer.getStyle()
            .set("background", color + "20")
            .set("border-radius", "12px")
            .set("width", "48px")
            .set("height", "48px")
            .set("margin-bottom", "12px");
        
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "32px")
            .set("font-weight", "700")
            .set("color", "#1f2937")
            .set("line-height", "1");
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "14px")
            .set("color", "#6b7280")
            .set("margin-top", "4px");
        
        add(iconContainer, valueSpan, labelSpan);
    }
}