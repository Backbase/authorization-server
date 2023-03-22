package com.backbase.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
class AuthServerApplicationTests {

    public static final String MCOB_AIS_CODE = "UKaccountEsbGdTB2a9MbSdt53serRsv0aUK001";
    public static final String MCOB_AIS_STATE = "3713ac23-9600-418f-842a-3df01e93cd4d";

    @Autowired
    private WebClient webClient;

    @Autowired
    private ObjectMapper mapper;

    @Value("${wiremock.server.port}")
    private Integer wiremockPort;

    private String externalLoginUrl;
    private String redirectUri;
    private String authorizationRequest;

    @BeforeEach
    public void setUp() {
        this.webClient.removeRequestHeader(HttpHeaders.AUTHORIZATION);
        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        this.webClient.getOptions().setRedirectEnabled(true);
        this.webClient.getCookieManager().clearCookies(); // log out

        this.externalLoginUrl = String.format("http://127.0.0.1:%d/external-login", wiremockPort);
        this.redirectUri = String.format("http://127.0.0.1:%d/test-client-redirect", wiremockPort);
        this.authorizationRequest = UriComponentsBuilder.fromPath("/oauth2/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", "test-client")
            .queryParam("scope", "openid")
            .queryParam("state", "some-state")
            .queryParam("code_challenge", "6xp5qsjr8GPeF7Eho4hS1BmbRT4dwTn0L9y5uiGbmsI")
            .queryParam("code_challenge_method", "S256")
            .queryParam("redirect_uri", redirectUri)
            .toUriString();
    }

    @Test
    public void whenLoginSuccessfulThenDisplayNotFoundError() throws IOException {
        HtmlPage page = this.webClient.getPage("/");

        assertExternalLoginPage(page);

        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebResponse signInResponse = signIn(page, MCOB_AIS_CODE, MCOB_AIS_STATE).getWebResponse();
        assertThat(signInResponse.getStatusCode()).isEqualTo(
            HttpStatus.NOT_FOUND.value());    // there is no "default" index page
    }

    @Test
    public void whenLoginFailsThenReturnUnauthorized() throws IOException {
        HtmlPage page = this.webClient.getPage("/");

        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebResponse response = signIn(page, MCOB_AIS_CODE, "wrong-value").getWebResponse();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void whenNotLoggedInAndRequestingTokenThenRedirectsToExternalLogin() throws IOException {
        HtmlPage page = this.webClient.getPage(authorizationRequest);

        assertExternalLoginPage(page);
        HtmlPage redirectPage = signIn(page, MCOB_AIS_CODE, MCOB_AIS_STATE);

        assertThat(redirectPage.getUrl().toString()).startsWith(redirectUri);
    }

    @Test
    public void whenRequestingUserInfoThenReturnClaimsAttributes() throws IOException {
        HtmlPage page = this.webClient.getPage(authorizationRequest);
        HtmlPage redirectPage = signIn(page, MCOB_AIS_CODE, MCOB_AIS_STATE);
        HtmlButton tokenRequestButton = redirectPage.querySelector("button");
        WebResponse tokenResponse = tokenRequestButton.click().getWebResponse();

        assertThat(tokenResponse.getStatusCode()).isEqualTo(200);

        Map token = mapper.readValue(tokenResponse.getContentAsString(), Map.class);
        this.webClient.addRequestHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.get("access_token"));
        WebResponse userInfoResponse = this.webClient.getPage("/userinfo").getWebResponse();

        assertThat(userInfoResponse.getStatusCode()).isEqualTo(200);

        Map userInfo = mapper.readValue(userInfoResponse.getContentAsString(), Map.class);
        assertThat(userInfo.get("sub")).isEqualTo("test");
        assertThat(userInfo.get("preferred_username")).isEqualTo("test");
        assertThat(userInfo.get("aspspId")).isEqualTo("420e5cff-0e2a-4156-991a-f6eeef0478cf");
    }

    @Test
    public void whenLoggingInAndRequestingTokenThenRedirectsToClientApplication() throws IOException {
        // Log in
        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        this.webClient.getOptions().setRedirectEnabled(false);
        signIn(this.webClient.getPage(externalLoginUrl), MCOB_AIS_CODE, MCOB_AIS_STATE);

        // Request token
        WebResponse response = this.webClient.getPage(authorizationRequest).getWebResponse();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
        String location = response.getResponseHeaderValue("location");
        assertThat(location).startsWith(redirectUri);
        assertThat(location).contains("code=");
    }

    private static <P extends Page> P signIn(HtmlPage page, String code, String state) throws IOException {
        HtmlInput codeInput = page.querySelector("input[name=\"code\"]");
        HtmlInput stateInput = page.querySelector("input[name=\"state\"]");
        HtmlButton signInButton = page.querySelector("button");

        codeInput.type(code);
        stateInput.type(state);
        return signInButton.click();
    }

    private void assertExternalLoginPage(HtmlPage page) {
        assertThat(page.getUrl().toString()).startsWith(externalLoginUrl);

        HtmlInput codeInput = page.querySelector("input[name=\"code\"]");
        HtmlInput stateInput = page.querySelector("input[name=\"state\"]");
        HtmlButton signInButton = page.querySelector("button");

        assertThat(codeInput).isNotNull();
        assertThat(stateInput).isNotNull();
        assertThat(signInButton.getTextContent()).isEqualTo("Sign in");
    }

}
