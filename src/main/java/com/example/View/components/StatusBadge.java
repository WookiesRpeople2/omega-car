package com.example.View.components;

import com.vaadin.flow.component.html.Span;

public class StatusBadge extends Span {
    
    public StatusBadge(String status) {
        setText(getStatusText(status));
        getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "12px")
            .set("font-size", "13px")
            .set("font-weight", "600");
        
        applyStatusStyle(status);
    }
    
    private String getStatusText(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> "✓ Confirmed";
            case "cancelled" -> "✗ Cancelled";
            case "pending" -> "⏱ Pending";
            default -> status;
        };
    }
    
    private void applyStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "confirmed" -> getStyle()
                    .set("background", "#d1fae5")
                    .set("color", "#065f46");
            case "cancelled" -> getStyle()
                    .set("background", "#fee2e2")
                    .set("color", "#991b1b");
            case "pending" -> getStyle()
                    .set("background", "#fef3c7")
                    .set("color", "#92400e");
            default -> getStyle()
                    .set("background", "#e5e7eb")
                    .set("color", "#374151");
        }
    }
}