package com.backbase.authorization.ais.authentication;

import com.backbase.authorization.ais.config.AiConsentsProperties;
import com.backbase.authorization.ais.config.AiConsentsProperties.Aspsp;
import com.mastercard.mcob.ais.ApiException;
import com.mastercard.mcob.ais.api.AiConsentsApi;
import com.mastercard.mcob.ais.model.PostAccountsConsentsOKBody;
import com.mastercard.mcob.ais.model.PostAccountsConsentsParamsBody;
import com.mastercard.mcob.ais.model.PostAccountsConsentsParamsBodyRequestInfo;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class AiConsentRedirectEntryPoint implements AuthenticationEntryPoint {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final AiConsentsApi aiConsentsApi;
    private final AiConsentsProperties properties;
    private String callbackPath = AiConsentAuthenticationConfigurer.DEFAULT_CALLBACK_URL;

    public AiConsentRedirectEntryPoint() {
        this.aiConsentsApi = new AiConsentsApi();
        this.properties = new AiConsentsProperties();
    }

    @Autowired
    public AiConsentRedirectEntryPoint(AiConsentsApi aiConsentsApi, AiConsentsProperties properties) {
        this.aiConsentsApi = aiConsentsApi;
        this.properties = properties;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException {
        log.debug("Requesting AI consent initiation via SCA redirect");
        try {
            Aspsp aspsp = getAspsp(request);
            var callbackQueryParams = Map.of(AiConsentsProperties.ASPSP_ID_KEY, List.of(aspsp.getId()));
            PostAccountsConsentsParamsBody consentRequest = new PostAccountsConsentsParamsBody()
                .permissions(aspsp.getPermissions())
                .validUntilDateTime(OffsetDateTime.now().plus(aspsp.getConsentValidity()))
                .requestInfo(new PostAccountsConsentsParamsBodyRequestInfo()
                    .xRequestId(UUID.randomUUID().toString())
                    .aspspId(aspsp.getId())
                    .tppRedirectURI(buildCallbackUri(request, callbackQueryParams)));
            PostAccountsConsentsOKBody consent = aiConsentsApi.getConsent(consentRequest);
            redirectStrategy.sendRedirect(request, response, consent.getLinks().getScaRedirect());
        } catch (ApiException e) {
            log.error("Failed when requesting SCA redirect link: {}", e.getMessage());
            throw new IOException(e);
        }
    }

    private Aspsp getAspsp(HttpServletRequest request) {
        String aspspId = request.getParameter(AiConsentsProperties.ASPSP_ID_KEY);
        if (ObjectUtils.isEmpty(aspspId)) {
            Aspsp aspsp = properties.getDefaultAspsp();
            log.debug("Fallback to default ASPSP configuration: {}", aspsp.getId());
            return aspsp;
        }
        log.debug("Getting configuration for ASPSP: {}", aspspId);
        return properties.getAspsps().stream().filter(a -> a.getId().equals(aspspId)).findFirst().orElseThrow();
    }

    private String buildCallbackUri(HttpServletRequest request, Map<String, List<String>> callbackQueryParams) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
            .replacePath(this.callbackPath)
            .replaceQueryParams(CollectionUtils.toMultiValueMap(callbackQueryParams))
            .build()
            .toUriString();
    }
}
