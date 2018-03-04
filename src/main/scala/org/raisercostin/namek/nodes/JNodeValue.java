package org.raisercostin.namek.nodes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.util.Optional;
import org.joda.time.DateTime;

interface JNodeValue {
  Optional<Boolean> asOptionalBoolean();
  Optional<String> asOptionalString();
  Optional<Number> asOptionalNumber();
  Optional<Integer> asOptionalInteger();
  Optional<Long> asOptionalLong();
  Optional<BigInteger> asOptionalBigInteger();
  Optional<BigDecimal> asOptionalBigDecimal();
  Optional<Double> asOptionalDouble();
  Optional<Object> asOptionalObject();
  Optional<Long> asOptionalBytes();

  Optional<DateTime> asOptionalDateTime();
  Optional<Long> asOptionalMilliseconds();
  Optional<Long> asOptionalNanoseconds();
  Optional<Long> asOptionalDuration(TimeUnit unit);
  Optional<Duration> asOptionalDuration();
  <T extends Enum<T>> Optional<T> asEnum(Class<T> enumClass);
}