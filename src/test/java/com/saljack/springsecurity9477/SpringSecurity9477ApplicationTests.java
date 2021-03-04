package com.saljack.springsecurity9477;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringSecurity9477ApplicationTests {

  private static final String TOKEN_MOCK_BODY_RESPONSE = "{\"token_type\": \"Bearer\",\"access_token\":"
      + "\"{{randomValue length=20 type='ALPHANUMERIC'}}\"}";
  private static final String TOKEN_PATH = "/auth/realms/test/protocol/openid-connect/token";
  private static final String SCENARIO = "scenario";
  private static final String UNAUTHORIZED = "unauthorized";

  WireMockServer wireMockServer = new WireMockServer(18081);

  @Autowired
  WebTestClient webClient;

  @Autowired
  OAuth2AuthorizedClientService oauth2AuthorizedClientService;

  @BeforeEach
  void init() {
    webClient = webClient
        .mutate()
        .responseTimeout(Duration.ofMinutes(15))
        .build();

    wireMockServer.start();
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
    wireMockServer.resetAll();
  }

  private void mockTokenCall() {
    wireMockServer.stubFor(post(TOKEN_PATH).willReturn(okJson(TOKEN_MOCK_BODY_RESPONSE)));
  }

  @Test
  void contextLoads() {
    mockTokenCall();
    wireMockServer.stubFor(get("/")
        .inScenario(SCENARIO)
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(ok("hello"))
        .willSetStateTo(UNAUTHORIZED));

    wireMockServer.stubFor(get("/")
        .inScenario(SCENARIO)
        .whenScenarioStateIs(UNAUTHORIZED)
        .willReturn(unauthorized())
        .willSetStateTo(Scenario.STARTED));

    // First success request and a token is stored
    webClient
        .get()
        .uri("/")
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .isOk();
    OAuth2AuthorizedClient loadAuthorizedClient = oauth2AuthorizedClientService.loadAuthorizedClient("test", "user");
    assertNotNull(loadAuthorizedClient);

    // Second request and resource server returns 401 Unauthorized then the token
    // should be removed
    webClient
        .get()
        .uri("/")
        .headers(headers -> headers.setBasicAuth("user", "password"))
        .exchange()
        .expectStatus()
        .is5xxServerError();
    loadAuthorizedClient = oauth2AuthorizedClientService.loadAuthorizedClient("test", "user");
    assertNull(loadAuthorizedClient, "Token is not removed");

  }

}
