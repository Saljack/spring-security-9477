package com.saljack.springsecurity9477.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .httpBasic().and()
        .authorizeRequests()
        .anyRequest()
        .authenticated();
  }

  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    UserDetails user = User.withDefaultPasswordEncoder()
        .username("user")
        .password("password")
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public WebClient webClient(ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository oauth2AuthorizedClientRepository) {
    final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
        clientRegistrationRepository, oauth2AuthorizedClientRepository);
    oauth2Client.setDefaultClientRegistrationId("test");

    return WebClient.builder()
        .apply(oauth2Client.oauth2Configuration())
        .build();
  }
}
