package com.aevi.sdk.pos.flow;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.aevi.android.rxmessenger.client.ObservableMessengerClient;
import com.aevi.sdk.flow.constants.FinancialRequestTypes;
import com.aevi.sdk.flow.constants.TransactionTypes;
import com.aevi.sdk.flow.model.AppMessage;
import com.aevi.sdk.flow.model.Request;
import com.aevi.sdk.flow.model.Token;
import com.aevi.sdk.pos.flow.model.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@Config(sdk = Build.VERSION_CODES.LOLLIPOP, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PaymentClientImplTest {

    private PaymentClientImpl paymentClient;

    @Mock
    private ObservableMessengerClient messengerClient;

    @Before
    public void setup() {
        ShadowLog.stream = System.out;
        initMocks(this);
        paymentClient = new PaymentClientImpl(RuntimeEnvironment.application) {
            @Override
            protected ObservableMessengerClient getMessengerClient(ComponentName componentName) {
                return messengerClient;
            }
        };
        when(messengerClient.sendMessage(anyString())).thenReturn(Observable.just("{}"));
    }

    @Test
    public void getPaymentServicesShouldSendAndReceiveDataCorrectly() throws Exception {
        PaymentServiceInfo testInfo = getPaymentServiceInfo();
        when(messengerClient.sendMessage(anyString())).thenReturn(Observable.just(testInfo.toJson()));

        TestObserver<PaymentServices> testObserver = paymentClient.getPaymentServices().test();

        AppMessage appMessage = callSendAndCaptureMessage();
        assertThat(appMessage.getMessageData()).isEqualTo(AppMessage.EMPTY_DATA);
        PaymentServices result = testObserver.values().get(0);
        assertThat(result.getAllPaymentServices()).hasSize(1);
        assertThat(result.getAllPaymentServices().get(0)).isEqualTo(testInfo);
        verify(messengerClient).closeConnection();
    }

    private PaymentServiceInfo getPaymentServiceInfo() throws PackageManager.NameNotFoundException {
        Context context = TestEnvironment.mockContext("com.test", "1.2.3");
        return new PaymentServiceInfoBuilder()
                .withVendor("Test")
                .withDisplayName("PA one")
                .withTerminalId("1234")
                .withMerchantIds("5678")
                .withSupportedRequestTypes("hawk", "snail")
                .withSupportedTransactionTypes("banana", "pear")
                .withSupportedCurrencies("GBP", "AUD")
                .withDefaultCurrency("GBP")
                .build(context);
    }

    @Test
    public void initiatePaymentShouldSendPaymentViaRequestCorrectly() throws Exception {
        Payment payment = new PaymentBuilder().withTransactionType(TransactionTypes.SALE).withAmounts(new Amounts(1000, "GBP")).build();

        paymentClient.initiatePayment(payment).test();

        AppMessage sentAppMessage = callSendAndCaptureMessage();
        Request request = Request.fromJson(sentAppMessage.getMessageData());
        Payment sentPayment = request.getRequestData().getValue(FinancialRequestTypes.PAYMENT, Payment.class);
        assertThat(sentPayment).isEqualTo(payment);
        verify(messengerClient).closeConnection();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentIfTokenAppIdAndPaymentServiceIdMismatch() throws Exception {
        Token token = new Token("123", "card");
        token.setSourceAppId("123");
        Payment payment = new PaymentBuilder().withTransactionType(TransactionTypes.SALE).withAmounts(new Amounts(1000, "GBP"))
                .withCardToken(token).build();

        paymentClient.initiatePayment(payment, "456", "789");
    }

    @Test
    public void subscribeToStatusUpdatesShouldPropagateUpdatesCorrectly() throws Exception {
        paymentClient.subscribeToStatusUpdates("123").test();

        AppMessage sentAppMessage = callSendAndCaptureMessage();
        RequestStatus requestStatus = RequestStatus.fromJson(sentAppMessage.getMessageData());
        assertThat(requestStatus.getStatus()).isEqualTo("123");
        verify(messengerClient).closeConnection();
    }

    private AppMessage callSendAndCaptureMessage() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(messengerClient).sendMessage(captor.capture());
        AppMessage sentAppMessage = AppMessage.fromJson(captor.getValue());
        return sentAppMessage;
    }
}
