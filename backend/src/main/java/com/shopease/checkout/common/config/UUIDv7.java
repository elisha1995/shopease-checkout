package com.shopease.checkout.common.config;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity ID field to be generated using UUIDv7 (time-ordered UUID).
 */
@IdGeneratorType(UUIDv7Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface UUIDv7 {
}