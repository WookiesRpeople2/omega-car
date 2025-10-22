package com.example.View.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ActionButton extends Button {
    
    public ActionButton(String text, VaadinIcon icon, ButtonVariant... variants) {
        super(text, icon.create());
        
        for (ButtonVariant variant : variants) {
            addThemeVariants(variant);
        }
        
        getStyle()
            .set("transition", "all 0.3s ease");
    }
    
    public static ActionButton createPrimary(String text, VaadinIcon icon) {
        ActionButton button = new ActionButton(text, icon, ButtonVariant.LUMO_PRIMARY);
        button.getStyle()
            .set("background", "linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)")
            .set("border", "none")
            .set("box-shadow", "0 4px 12px rgba(99, 102, 241, 0.3)");
        
        return button;
    }
    
    public static ActionButton createDanger(String text, VaadinIcon icon) {
        ActionButton button = new ActionButton(text, icon, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        return button;
    }
    
    public static ActionButton createSecondary(String text, VaadinIcon icon) {
        return new ActionButton(text, icon, ButtonVariant.LUMO_TERTIARY);
    }
    
    public static ActionButton createIconButton(VaadinIcon icon, ButtonVariant... variants) {
        return new ActionButton("", icon, variants);
    }
}