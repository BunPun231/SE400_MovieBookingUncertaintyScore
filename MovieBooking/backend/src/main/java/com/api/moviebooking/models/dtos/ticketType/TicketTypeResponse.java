package com.api.moviebooking.models.dtos.ticketType;

import java.math.BigDecimal;
import java.util.UUID;

import com.api.moviebooking.models.enums.ModifierType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTypeResponse {

    private UUID ticketTypeId;
    private String code; // adult, student, senior, member, double
    private String label; // Display label: NGƯỜI LỚN, HSSV/U22-GV, etc.
    private ModifierType modifierType; // PERCENTAGE or FIXED_AMOUNT (for admin view)
    private BigDecimal modifierValue; // Modifier value (for admin view)
    private BigDecimal price; // Calculated price (for guest view with showtime context)
    private Boolean active;
    private Integer sortOrder;
}
