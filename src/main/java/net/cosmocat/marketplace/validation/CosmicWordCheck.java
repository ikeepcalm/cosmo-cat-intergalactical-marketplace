package net.cosmocat.marketplace.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CosmicWordCheckValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CosmicWordCheck {

    String message() default "Name must contain at least one cosmic term (star, galaxy, comet, nebula, planet, moon, asteroid, meteor, cosmic, space, orbit, solar, stellar, celestial, universe, supernova, black hole, pulsar, quasar)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean required() default true;

    int minCosmicWords() default 1;
}