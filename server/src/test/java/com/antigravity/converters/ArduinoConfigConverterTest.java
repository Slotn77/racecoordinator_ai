package com.antigravity.converters;

import static org.junit.Assert.assertEquals;

import com.antigravity.proto.PinBehavior;
import com.antigravity.protocols.arduino.ArduinoConfig;
import java.util.Arrays;
import org.junit.Test;

public class ArduinoConfigConverterTest {

  @Test
  public void testToProtoFiltersNullIds() {
    ArduinoConfig config = new ArduinoConfig();
    config.digitalIds =
        Arrays.asList(
            PinBehavior.BEHAVIOR_UNUSED.getNumber(),
            PinBehavior.BEHAVIOR_UNUSED.getNumber(),
            null,
            PinBehavior.BEHAVIOR_LAP_BASE.getNumber());
    config.analogIds = Arrays.asList(null, PinBehavior.BEHAVIOR_VOLTAGE_LEVEL_BASE.getNumber());

    com.antigravity.proto.ArduinoConfig proto = ArduinoConfigConverter.toProto(config);

    assertEquals(4, proto.getDigitalIdsCount());
    assertEquals(0, proto.getDigitalIds(0));
    assertEquals(0, proto.getDigitalIds(1));
    assertEquals(0, proto.getDigitalIds(2)); // The null should be replaced with 0
    assertEquals(PinBehavior.BEHAVIOR_LAP_BASE.getNumber(), proto.getDigitalIds(3));

    assertEquals(2, proto.getAnalogIdsCount());
    assertEquals(0, proto.getAnalogIds(0)); // The null should be replaced with 0
    assertEquals(PinBehavior.BEHAVIOR_VOLTAGE_LEVEL_BASE.getNumber(), proto.getAnalogIds(1));
  }
}
