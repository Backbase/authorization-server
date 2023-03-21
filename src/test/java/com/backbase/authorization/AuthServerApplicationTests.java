package com.backbase.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 9999)
class AuthServerApplicationTests {

    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/test-client-oidc";

    private static final String AUTHORIZATION_REQUEST = UriComponentsBuilder
        .fromPath("/oauth2/authorize")
        .queryParam("response_type", "code")
        .queryParam("client_id", "test-client")
        .queryParam("scope", "openid")
        .queryParam("state", "some-state")
        .queryParam("redirect_uri", REDIRECT_URI)
        .toUriString();

    @Autowired
    private WebClient webClient;

    @BeforeEach
    public void setUp() {
        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        this.webClient.getOptions().setRedirectEnabled(true);
        this.webClient.getCookieManager().clearCookies(); // log out
    }

    @Test
    public void whenLoginSuccessfulThenDisplayNotFoundError() throws IOException {
        HtmlPage page = this.webClient.getPage("/");

        assertExternalLoginPage(page);

        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        WebResponse signInResponse = signIn(page, "UKaccountEsbGdTB2a9MbSdt53serRsv0aUK001",
            "3713ac23-9600-418f-842a-3df01e93cd4d").getWebResponse();
        assertThat(signInResponse.getStatusCode()).isEqualTo(
            HttpStatus.NOT_FOUND.value());    // there is no "default" index page
    }

    @Test
    @Disabled
    public void whenLoginFailsThenDisplayBadCredentials() throws IOException {
        HtmlPage page = this.webClient.getPage("/");

        HtmlPage loginErrorPage = signIn(page, "UKaccountEsbGdTB2a9MbSdt53serRsv0aUK001", "wrong-state");

        HtmlElement alert = loginErrorPage.querySelector("div[role=\"alert\"]");
        assertThat(alert).isNotNull();
        assertThat(alert.getTextContent()).isEqualTo("Bad credentials");
    }

    @Test
    public void whenNotLoggedInAndRequestingTokenThenRedirectsToLogin() throws IOException {
        HtmlPage page = this.webClient.getPage(AUTHORIZATION_REQUEST);

        assertExternalLoginPage(page);

        HtmlPage redirectPage = signIn(page, "UKaccountEsbGdTB2a9MbSdt53serRsv0aUK001",
            "3713ac23-9600-418f-842a-3df01e93cd4d");

        assertThat(redirectPage.getUrl().toString()).startsWith(REDIRECT_URI);
    }

    @Test
    public void whenLoggingInAndRequestingTokenThenRedirectsToClientApplication() throws IOException {
        // Log in
        this.webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        this.webClient.getOptions().setRedirectEnabled(false);
        signIn(this.webClient.getPage("http://127.0.0.1:9999/external-login"), "UKaccountEsbGdTB2a9MbSdt53serRsv0aUK001",
            "3713ac23-9600-418f-842a-3df01e93cd4d");

        // Request token
        WebResponse response = this.webClient.getPage(AUTHORIZATION_REQUEST).getWebResponse();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.MOVED_PERMANENTLY.value());
        String location = response.getResponseHeaderValue("location");
        assertThat(location).startsWith(REDIRECT_URI);
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

    private static void assertExternalLoginPage(HtmlPage page) {
        assertThat(page.getUrl().toString()).startsWith("http://127.0.0.1:9999/external-login");

        HtmlInput codeInput = page.querySelector("input[name=\"code\"]");
        HtmlInput stateInput = page.querySelector("input[name=\"state\"]");
        HtmlButton signInButton = page.querySelector("button");

        assertThat(codeInput).isNotNull();
        assertThat(stateInput).isNotNull();
        assertThat(signInButton.getTextContent()).isEqualTo("Sign in");
    }

}
