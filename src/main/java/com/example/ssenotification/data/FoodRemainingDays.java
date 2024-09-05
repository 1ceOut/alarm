package com.example.ssenotification.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FoodRemainingDays {
    private String food_id;
    private String refrigerator_id;
    private String remainingDay;
}
