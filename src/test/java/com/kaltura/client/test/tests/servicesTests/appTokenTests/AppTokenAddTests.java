package com.kaltura.client.test.tests.servicesTests.appTokenTests;

import com.kaltura.client.Client;
import com.kaltura.client.enums.AppTokenHashType;
import com.kaltura.client.test.Properties;
import com.kaltura.client.test.servicesImpl.AppTokenServiceImpl;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.utils.AppTokenUtils;
import com.kaltura.client.test.utils.BaseUtils;
import com.kaltura.client.types.AppToken;
import com.kaltura.client.utils.response.base.Response;
import io.qameta.allure.Description;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.kaltura.client.test.tests.BaseTest.SharedHousehold.*;
import static com.kaltura.client.test.utils.BaseUtils.getAPIExceptionFromList;
import static org.assertj.core.api.Assertions.assertThat;

public class AppTokenAddTests extends BaseTest {

    private AppTokenHashType hashType;
    private String sessionUserId = "1577578";
    private AppToken appToken = new AppToken();
    public static Client client;
    private String sessionPrivileges;

    @BeforeClass
    private void add_tests_before_class() {
        hashType = AppTokenHashType.SHA1;
        appToken = AppTokenUtils.addAppToken(sessionUserId, hashType, null, null);
    }

    @Description("appToken/action/add")
    @Test
    private void addAppToken() {
        client = getClient(getOperatorKs());
        Response<AppToken> appTokenResponse = AppTokenServiceImpl.add(client, appToken);

        // Verify no error returned
        assertThat(appTokenResponse.error).isNull();
        assertThat(appTokenResponse.results.getExpiry()).isNull();
        assertThat(appTokenResponse.results.getId()).isNotEmpty();
        assertThat(appTokenResponse.results.getSessionDuration()).isGreaterThan(0);
        assertThat(appTokenResponse.results.getHashType()).isEqualTo(this.hashType);
        assertThat(appTokenResponse.results.getToken()).isNotEmpty();
        assertThat(appTokenResponse.results.getSessionUserId()).isEqualTo(this.sessionUserId);
        assertThat(appTokenResponse.results.getPartnerId()).isEqualTo(Properties.PARTNER_ID);
        assertThat(appTokenResponse.results.getSessionUserId()).isEqualTo(String.valueOf(this.sessionUserId));
    }

    @Description("appToken/action/add - without hash type")
    @Test
    private void addAppTokenWithDefaultHashType() {
        client = getClient(getOperatorKs());
        appToken = AppTokenUtils.addAppToken(sessionUserId, null, null, null);
        // Invoke AppToken/action/add - with no hash type (will return the default hash type)
        Response<AppToken> appTokenResponse = AppTokenServiceImpl.add(client, appToken);
        // Verify that hashType = SHA256
        assertThat(appTokenResponse.results.getHashType()).isEqualTo(AppTokenHashType.SHA256);
    }

    @Description("appToken/action/add - with privileges")
    @Test
    private void addAppTokenWithPrivileges() {
        client = getClient(getOperatorKs());
        sessionPrivileges = "key1:value1,key2:value2";
        appToken = AppTokenUtils.addAppToken(sessionUserId, null, sessionPrivileges, null);
        Response<AppToken> appTokenResponse = AppTokenServiceImpl.add(client, appToken);

        assertThat(appTokenResponse.results.getSessionPrivileges()).isEqualTo(sessionPrivileges);
    }

    @Description("appToken/action/add - with expiry date")
    @Test(groups = "slow")
    private void addAppTokenWithExpiryDate() {
        client = getClient(getOperatorKs());
        Long expiryDate = BaseUtils.getTimeInEpoch(1);
        appToken = AppTokenUtils.addAppToken(sessionUserId, null, sessionPrivileges, Math.toIntExact(expiryDate));
        Response<AppToken> addAppTokenResponse = AppTokenServiceImpl.add(client, appToken);
        assertThat(addAppTokenResponse.results.getExpiry()).isEqualTo(Math.toIntExact(expiryDate));

        // Wait until token is expired (according to expiry date)
        System.out.println("Waiting 1 minute until token expiry date reached");

        try {
            Thread.sleep(72000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Response<AppToken> getAppTokenResponse = AppTokenServiceImpl.get(client, addAppTokenResponse.results.getId());
        assertThat(getAppTokenResponse.error.getCode()).isEqualTo(getAPIExceptionFromList(500055).getCode());
    }

    @Description("appToken/action/add - with no expiry date (return default expiry date -" +
            "According to app_token_max_expiry_seconds key value in group_203 CB document")
    @Test
    //todo - Add specific mark indicating the version of the feature
    private void addAppTokenWithNoExpiryDate() {
        getSharedHousehold();
        client = getClient(getSharedMasterUserKs());
        int expiryDate = 0;
        //int cbExpiryDateValue = 2592000;
        appToken = AppTokenUtils.addAppToken(null, null, sessionPrivileges, expiryDate);
        Response<AppToken> addAppTokenResponse = AppTokenServiceImpl.add(client, appToken);
        assertThat(addAppTokenResponse.results.getExpiry()).isGreaterThan(Math.toIntExact(BaseUtils.getTimeInEpoch(0)));
    }

    @Description("appToken/action/add - with no specific user id")
    @Test
    private void addAppTokenWithoutSpecificUserId() {
        client = getClient(getOperatorKs());
        appToken = AppTokenUtils.addAppToken(null, null, sessionPrivileges, null);
        Response<AppToken> addAppTokenResponse = AppTokenServiceImpl.add(client, appToken);
        assertThat(addAppTokenResponse.error).isNull();
        assertThat(addAppTokenResponse.results.getExpiry()).isNull();
        assertThat(addAppTokenResponse.results.getId()).isNotEmpty();
        assertThat(addAppTokenResponse.results.getToken()).isNotEmpty();
        assertThat(addAppTokenResponse.results.getSessionUserId()).isNotEqualTo(sessionUserId);
        assertThat(addAppTokenResponse.results.getPartnerId()).isEqualTo(Properties.PARTNER_ID);
    }
}
