package org.folio.edge.rtac.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class RtacClientResponseInterpreterTests {
  @Test
  void successfulResponseIsMappedToBody() {
    final var responseInterpreter = new RtacOkapiClient.ResponseInterpreter();

    final var response = new RtacOkapiClient.Response(200, "some body");

    assertThat(responseInterpreter.interpretResponse(response), is("some body"));
  }

  @Test
  void failureResponseIsMappedToEmptyJson() {
    final var responseInterpreter = new RtacOkapiClient.ResponseInterpreter();

    final var response = new RtacOkapiClient.Response(400, "irrelevant");

    assertThat(responseInterpreter.interpretResponse(response), is("{}"));
  }
}
