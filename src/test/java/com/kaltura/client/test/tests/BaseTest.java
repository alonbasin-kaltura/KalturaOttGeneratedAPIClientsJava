package com.kaltura.client.test.tests;

import com.kaltura.client.Client;
import com.kaltura.client.Configuration;
import com.kaltura.client.Logger;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import org.testng.annotations.BeforeSuite;

import java.util.concurrent.TimeUnit;

import static com.kaltura.client.test.Properties.*;
import static com.kaltura.client.test.servicesImpl.OttUserServiceImpl.anonymousLogin;
import static com.kaltura.client.test.servicesImpl.OttUserServiceImpl.login;
import static org.awaitility.Awaitility.setDefaultTimeout;

public class BaseTest {

    private static Configuration config;

    // shared ks's
    public static String administratorKs, operatorKs, managerKs, anonymousKs;

    // shared household
    public static Household sharedHousehold;
    public static HouseholdUser sharedMasterUser, sharedUser;
    public static String sharedMasterUserKs, sharedUserKs;

    // shared VOD
    public static MediaAsset mediaAsset;

    @BeforeSuite
    public void base_test_before_suite() {
        Logger.getLogger(BaseTest.class).debug("Start Setup!");

        // set configuration
        config = new Configuration();
        config.setEndpoint(API_BASE_URL + "/" + API_URL_VERSION);
        config.setAcceptGzipEncoding(false);

        // Set default awaitility timeout
        setDefaultTimeout(20, TimeUnit.SECONDS);

        // TODO: 4/17/2018 move init functions to the relevant used places instead calling it in beforeSuite
        initGlobalUsersKs();
//        getSharedHousehold();
//        getSharedMediaAsset();

        Logger.getLogger(BaseTest.class).debug("Finish Setup!");
    }

    public static Client getClient(String ks) {
        Client client = new Client(config);
        client.setApiVersion(API_REQUEST_VERSION);
        client.setKs(ks);
        return client;
    }

    private void initGlobalUsersKs() {
        Client client = getClient(null);
        Response<LoginResponse> loginResponse;
        Response<LoginSession> loginSession;

        loginResponse = login(client, PARTNER_ID, getProperty(ADMINISTRATOR_USERNAME), getProperty(ADMINISTRATOR_PASSWORD), null, null);
        administratorKs = loginResponse.results.getLoginSession().getKs();

        loginResponse = login(client, PARTNER_ID, getProperty(OPERATOR_USERNAME), getProperty(OPERATOR_PASSWORD), null, null);
        operatorKs = loginResponse.results.getLoginSession().getKs();

        loginResponse = login(client, PARTNER_ID, getProperty(MANAGER_USERNAME), getProperty(MANAGER_PASSWORD), null, null);
        managerKs = loginResponse.results.getLoginSession().getKs();

        loginSession = anonymousLogin(client, PARTNER_ID, null);
        anonymousKs = loginSession.results.getKs();
    }
}
