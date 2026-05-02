package org.example.notification_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountMessage {
    @Positive(message = "shopId must be > =0")
    private Long shopId;
    @Positive(message = "categoryId must be > =0")
    private Long categoryId;
    @Positive(message = "tagId must be > =0")
    private Long tagId;

    @NotBlank(message = "topic must be given")
    private String topic;

    @NotBlank(message = "message must be given")
    private String message;

}
