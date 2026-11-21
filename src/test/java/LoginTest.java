package ru.netology.tests;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.*;
import static org.graalvm.compiler.nodeinfo.InputType.Condition;

public class LoginTest {
    private DataHelper dataHelper;
    private static final String BASE_URL = "http://localhost:9999"; // порт SUT
    private static final String LOGIN = "user1";

    @BeforeEach
    void setUp() throws Exception {
        dataHelper = new DataHelper();
        dataHelper.clearTables();
        open(BASE_URL + "/login");
    }

    @AfterEach
    void tearDown() throws Exception {
        dataHelper.close();
    }

    @Test
    void successfulLoginWithCodeFromDb() throws Exception {
        // Допустим, SUT сам создал запись в codes при первом запросе или через API
        // Здесь мы берём код из БД
        String code = dataHelper.getCodeForUser(LOGIN);
        if (code == null) {
            throw new IllegalStateException("Не удалось получить код из БД. Проверь, что SUT сгенерировал его.");
        }

        $("#login").setValue(LOGIN);
        $("#code").setValue(code);
        $("#submit").click();

        $("body").shouldHave(Condition.text("Добро пожаловать"));
    }

    @Test
    void loginFailsAfterThreeWrongPasswords() throws Exception {
        for (int i = 0; i < 3; i++) {
            $("#login").setValue(LOGIN);
            $("#code").setValue("wrong" + i);
            $("#submit").click();
            $("body").shouldHave(Condition.textContains("Неверный код"));
        }

        // Четвёртая попытка — должна быть блокировка
        $("#login").setValue(LOGIN);
        $("#code").setValue("any");
        $("#submit").click();

        $("body").shouldHave(Condition.textContains("Аккаунт заблокирован"));

        // Проверка через БД, что флаг блокировки установлен
        boolean blocked = dataHelper.isUserBlocked(LOGIN);
        assert blocked : "В БД не установлен флаг блокировки после 3 ошибок";
    }
}
