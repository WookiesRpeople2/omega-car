package com.example.view.components;

import com.example.model.BookingStatus;
import com.vaadin.flow.component.html.Span;

public class StatusBadge extends Span {
    
    public StatusBadge(BookingStatus status) {
        setText(getStatusText(status));
        getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "12px")
            .set("font-size", "13px")
            .set("font-weight", "600");
        
        applyStatusStyle(status);
    }
    
    private String getStatusText(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "✓ Confirmed";
            case CANCELLED -> "✗ Cancelled";
            case PENDING -> "⏱ Pending";
        };
    }
    
    private void applyStatusStyle(BookingStatus status) {
        switch (status) {
            case CONFIRMED -> getStyle()
                    .set("background", "#d1fae5")
                    .set("color", "#065f46");
            case CANCELLED -> getStyle()
                    .set("background", "#fee2e2")
                    .set("color", "#991b1b");
            case PENDING -> getStyle()
                    .set("background", "#fef3c7")
                    .set("color", "#92400e");
            default -> {
                throw new IllegalArgumentException("Unknown status: " + status);
            }
        }
    }
}