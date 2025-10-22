package com.example.view.components;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public final class AppNotification {
    

    private AppNotification() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    private static final int DEFAULT_DURATION = 3000;
    
    public static void success(String message) {
        show(message, NotificationVariant.LUMO_SUCCESS);
    }
    
    public static void error(String message) {
        show(message, NotificationVariant.LUMO_ERROR);
    }
    
    public static void warning(String message) {
        show(message, NotificationVariant.LUMO_WARNING);
    }
    
    public static void info(String message) {
        show(message, NotificationVariant.LUMO_PRIMARY);
    }
    
    private static void show(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, DEFAULT_DURATION);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_END);
        notification.open();
    }
    
    public static void show(String message, NotificationVariant variant, int duration) {
        Notification notification = new Notification(message, duration);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_END);
        notification.open();
    }
}