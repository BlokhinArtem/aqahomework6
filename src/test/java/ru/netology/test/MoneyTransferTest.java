package ru.netology.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.data.DataHelper.CardInfo;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DataHelper.*;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach //перед каждым тестом
    void setup() {
        var loginPage = open("http://localhost:9999", LoginPage.class); //открывается страница логина
        var authInfo = DataHelper.getAuthInfo(); //получаем данные логина и пароля
        var verificationPage = loginPage.validLogin(authInfo); //логин в SUT
        var verificationCode = DataHelper.getVerificationCode(); //получаем код верификации
        dashboardPage = verificationPage.validVerify(verificationCode); //вводим код верификации
        firstCardInfo = DataHelper.getFirstCardInfo(); //получаем номер и id первой карты
        secondCardInfo = DataHelper.getSecondCardInfo(); //получаем номер и id второй карты
        firstCardBalance = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber())); //получаем данные о балансе карты по маске карты, которую получаем по номеру карты
        secondCardBalance = dashboardPage.getCardBalance(1); //получаем данные о балансе второй карты по её id
    }

    @Test //тестируем возможность перевода с первой карты на вторую
    void shouldTransferFromFirstToSecond() {
        var amount = generateValidAmount(firstCardBalance); //генерируем случайную валидную сумму перевода
        var expectedBalanceFirstCard = firstCardBalance - amount; //ожидаемый баланс первой карты
        var expectedBalanceSecondCard = secondCardBalance + amount; //ожидаемый баланс второй карты
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo); //нажимаем кнопку пополнить второй карты
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo); //на странице пополнения вводим валидную сумму перевода, вводим данные первой карты
        dashboardPage.reloadDashboardPage(); //нажимаем кнопку обновить
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber())); //получаем актуальные данные о балансе первой карты по маске(которую получаем по номеру карты)
        var actualBalanceSecondCard = dashboardPage.getCardBalance(1); //получаем актуальные данные о балансе второй карты по id
        assertAll(
                () -> assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                () -> assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard)
        ); //сравниваем ожидаемые и актуальные балансы двух карт сразу
    }
    @Test //тестируем возможность перевода суммы превышающей баланс
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var amount = generateInvalidAmount(secondCardBalance); //генерируем сумму выходящую за рамки допустимого лимита перевода
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo); //нажимаем кнопку пополнить первой карты
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo); //на странице пополнения вводим невалидную сумму перевода, вводим данные второй карты
        transferPage.findErrorMessage("Ошибка!"); //ловим сообщение об ошибке по ожидаемому тексту ошибки, в элементах страницы нашёл только notification__title равный значению "Ошибка", по нему ошибку и ищу
        dashboardPage.reloadDashboardPage(); //обновляем страницу
        var actualBalanceFirstCard = dashboardPage.getCardBalance(getMaskedNumber(firstCardInfo.getCardNumber())); //получаем актуальные данные о балансе первой карты по маске(которую получаем по номеру карты)
        var actualBalanceSecondCard = dashboardPage.getCardBalance(getMaskedNumber(secondCardInfo.getCardNumber())); //получаем актуальные данные о балансе второй карты по маске(которую получаем по номеру карты)
        assertAll(
                () -> assertEquals(firstCardBalance, actualBalanceFirstCard),
                () -> assertEquals(secondCardBalance, actualBalanceSecondCard)
        ); //сравниваем ожидаемые и актуальные балансы двух карт сразу, они должны совпадать с балансом карт до попытки перевода
    }



}
