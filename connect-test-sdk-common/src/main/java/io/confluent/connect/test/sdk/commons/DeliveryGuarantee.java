/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons;

/** This enum is used to specify the delivery guarantee of the connector. */
public enum DeliveryGuarantee {
  AT_LEAST_ONCE,
  EXACTLY_ONCE,
  AT_MOST_ONCE
}