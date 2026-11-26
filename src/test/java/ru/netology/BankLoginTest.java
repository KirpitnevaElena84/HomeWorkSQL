package ru.netology;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions.assertEquals;
import static com.codeborne.selenide.Selenide.open;

public class BankLoginTest {

    private LoginPage loginPage;
    private static final DataHelper.AuthInfo AUTH_INFO = DataHelper.getAuthInfoWithTestData();

    @BeforeEach
    void setUp() {
        // только коды — пользователей и карты SUT создаёт сам при старте
        SQLHelper.cleanAuthCodes();
        open("http://localhost:9999");
        loginPage = new LoginPage();
    }

    @AfterEach
    void tearDown() {
        SQLHelper.cleanAuthCodes();
    }

    @Test
    @DisplayName("Успешный вход с корректными логином, паролем и кодом из БД")
    void shouldSuccessfullyLogin() {
        var verificationPage = loginPage.validLogin(AUTH_INFO);
        var verificationCode = SQLHelper.getVerificationCode();
        verificationPage.validVerify(verificationCode.getCode())
                .shouldBeVisible();
    }

    @Test
    @DisplayName("Ошибка при попытке входа несуществующего пользователя")
    void shouldGetErrorNotificationIfLoginWithRandomUserWithoutAddingToBase() {
        var authInfo = DataHelper.generateRandomUser();
        loginPage.login(authInfo);
        loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
    }

    @Test
    @DisplayName("Ошибка при неверном коде подтверждения для существующего пользователя")
    void shouldGetErrorNotificationIfLoginWithExistUserAndRandomVerificationCode() {
        var verificationPage = loginPage.validLogin(AUTH_INFO);
        var randomCode = DataHelper.generateRandomVerificationCode();
        verificationPage.verify(randomCode.getCode());
        verificationPage.verifyErrorNotification("Ошибка! Неверно указан код! Попробуйте ещё раз.");
    }

    @Test
    @DisplayName("Блокировка пользователя после трёх неверных паролей")
    void shouldBlockUserAfterThreeWrongPasswords() {
        for (int i = 0; i < 3; i++) {
            open("http://localhost:9999");
            loginPage.login(DataHelper.withWrongPassword(AUTH_INFO));
            loginPage.verifyErrorNotification("Ошибка! Неверно указан логин или пароль");
        }
        assertEquals("blocked", SQLHelper.getUserStatus(AUTH_INFO.getLogin()));

        // и вход даже с верным паролем больше не проходит
        open("http://localhost:9999");
        loginPage.login(AUTH_INFO);
        loginPage.verifyErrorNotification("Ошибка! Пользователь заблокирован");
    }
}
