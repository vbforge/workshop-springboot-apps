package com.vbforge.ras.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single reflection question.
 *
 * JUNIOR NOTE:
 * @Data from Lombok auto-generates: getters, setters, toString, equals, hashCode.
 * @NoArgsConstructor generates the no-arg constructor Jackson needs to deserialize JSON.
 * Without @NoArgsConstructor, Jackson can't create an empty object and fill fields — it will throw.
 *
 * This class mirrors the structure of questions.json exactly.
 * Field names match JSON keys — Jackson maps them automatically.
 */
@Data
@NoArgsConstructor
public class Question {

    private int id;
    private String text;
    private String hint;
    private String icon;

    // We add this field at runtime — not in JSON.
    // It tracks the user's answer for this question during a session.
    // @JsonProperty(access = WRITE_ONLY) would hide it in serialization if needed.
    private String answer;
}
