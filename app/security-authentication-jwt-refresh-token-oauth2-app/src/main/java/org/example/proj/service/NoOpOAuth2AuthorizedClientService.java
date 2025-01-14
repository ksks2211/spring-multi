package org.example.proj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

/**
 * @author rival
 * @since 2025-01-03
 */


@Slf4j
public class NoOpOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return null;
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        log.info("Authorized client saved: {} for principal: {}",
            authorizedClient.getClientRegistration().getRegistrationId(),
            principal.getName());
        log.info("Access token: {}", authorizedClient.getAccessToken().getTokenValue());


    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {

    }
}
