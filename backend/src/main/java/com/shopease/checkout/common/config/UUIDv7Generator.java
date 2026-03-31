package com.shopease.checkout.common.config;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;

import java.util.EnumSet;

/**
 * Custom Hibernate ID generator that produces UUIDv7 (time-ordered) values
 * using the java-uuid-generator library.
 */
public class UUIDv7Generator implements BeforeExecutionGenerator {

    private static final TimeBasedEpochRandomGenerator GENERATOR = Generators.timeBasedEpochRandomGenerator();

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        return GENERATOR.generate();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }
}
