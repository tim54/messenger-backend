package com.example.messenger.repo.dynamodb.util;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;

public class InstantAsStringConverter implements AttributeConverter<Instant> {
    public InstantAsStringConverter() {}

    @Override
    public AttributeValue transformFrom(Instant input) {
        return input == null ? AttributeValue.fromNul(true) : AttributeValue.fromS(input.toString());
    }

    @Override
    public Instant transformTo(AttributeValue value) {
        if (value == null || Boolean.TRUE.equals(value.nul())) return null;
        return Instant.parse(value.s());
    }

    @Override
    public EnhancedType<Instant> type() {
        return EnhancedType.of(Instant.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}