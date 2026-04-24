package org.example.core.models.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RatingTriggerType {
   SCHEDULED(1),
    MODERATOR(2);
   private int value;
   RatingTriggerType(int value) {
      this.value = value;
   }

   @JsonValue
   public String getValue() {
      return this.name();
   }

   @JsonCreator
   public static RatingTriggerType fromCode(int code) {
      for (RatingTriggerType type : values()) {
         if (type.value == code) {
            return type;
         }
      }
      throw new IllegalArgumentException("Unknown code: " + code);
   }
}
