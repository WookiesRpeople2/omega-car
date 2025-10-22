package com.example.view.components;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ConfirmDialog extends Dialog {
    
    private final Runnable onConfirm;
    
    public ConfirmDialog(String title, String message, String confirmButtonText, Runnable onConfirm) {
        this.onConfirm = onConfirm;
        
        setWidth("400px");
        
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        
        H3 titleComponent = new H3(title);
        titleComponent.getStyle()
            .set("margin", "0")
            .set("color", "#1f2937");
        
        Span messageComponent = new Span(message);
        messageComponent.getStyle()
            .set("color", "#6b7280");
        
        HorizontalLayout buttons = createButtons(confirmButtonText);
        
        layout.add(titleComponent, messageComponent, buttons);
        add(layout);
    }
    
    private HorizontalLayout createButtons(String confirmButtonText) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        ActionButton cancelBtn = ActionButton.createSecondary("Cancel", VaadinIcon.CLOSE);
        cancelBtn.addClickListener(e -> close());
        
        ActionButton confirmBtn = ActionButton.createDanger(confirmButtonText, VaadinIcon.TRASH);
        confirmBtn.addClickListener(e -> {
            onConfirm.run();
            close();
        });
        
        buttons.add(cancelBtn, confirmBtn);
        return buttons;
    }
    
    public static void show(String title, String message, String confirmButtonText, Runnable onConfirm) {
        new ConfirmDialog(title, message, confirmButtonText, onConfirm).open();
    }
}