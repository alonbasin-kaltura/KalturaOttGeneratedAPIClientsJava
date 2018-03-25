package com.kaltura.client.test.servicesImpl;

import com.kaltura.client.APIOkRequestsExecutor;
import com.kaltura.client.types.LoginResponse;
import com.kaltura.client.types.LoginSession;
import com.kaltura.client.types.OTTUser;
import com.kaltura.client.types.StringValue;
import com.kaltura.client.utils.response.base.ApiCompletion;
import com.kaltura.client.utils.response.base.Response;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.kaltura.client.services.OttUserService.*;
import static com.kaltura.client.services.OttUserService.login;
import static com.kaltura.client.test.tests.BaseTest.client;
import static org.awaitility.Awaitility.await;

public class OttUserServiceImpl {

    private static final AtomicBoolean done = new AtomicBoolean(false);

    private static final String LOGIN_RESPONSE_SCHEMA = "KalturaLoginResponse_Schema.json";
    private static final String LOGIN_SESSION_SCHEMA = "KalturaLoginSession_Schema.json";
    private static final String OTT_USER_SCHEMA = "KalturaOttUser_Schema.json";

    private static Response<LoginResponse> loginResponse;
    private static Response<OTTUser> ottUserResponse;
    private static Response<LoginSession> loginSessionResponse;
    private static Response<Boolean> booleanResponse;


    //login
    public static Response<LoginResponse> loginImpl(int partnerId, String username, String password, Map<String, StringValue> extraParams, String udid) {
        LoginOttUserBuilder loginOttUserBuilder = login(partnerId, username, password, extraParams, udid)
                .setCompletion((ApiCompletion<LoginResponse>) result -> {
                    if (result.isSuccess()) {
                        // TODO: 3/22/2018 fix schema assertions
//                        MatcherAssert.assertThat(result.results.toParams().toString(), matchesJsonSchemaInClasspath(LOGIN_RESPONSE_SCHEMA));
                    }
                    loginResponse = result;
                    done.set(true);
                });
        APIOkRequestsExecutor.getExecutor().queue(loginOttUserBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return loginResponse;
    }

    //register
    public static Response<OTTUser> registerImpl(int partnerId, OTTUser user, String password) {
        RegisterOttUserBuilder registerOttUserBuilder = register(partnerId, user, password)
                .setCompletion((ApiCompletion<OTTUser>) result -> {
                    if (result.isSuccess()) {
                        // TODO: 3/22/2018 fix schema assertions
                    }
                    ottUserResponse = result;
                    done.set(true);
                });
        APIOkRequestsExecutor.getExecutor().queue(registerOttUserBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return ottUserResponse;
    }

    //anonymousLogin
    public static Response<LoginSession> anonymousLoginImpl(int partnerId, String udid) {
        AnonymousLoginOttUserBuilder anonymousLoginOttUserBuilder = anonymousLogin(partnerId, udid)
                .setCompletion((ApiCompletion<LoginSession>) result -> {
                    if (result.isSuccess()) {
                        // TODO: 3/22/2018 fix schema assertions
                    }
                    loginSessionResponse = result;
                    done.set(true);
                });
        APIOkRequestsExecutor.getExecutor().queue(anonymousLoginOttUserBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return loginSessionResponse;
    }

    //activate
    public static Response<OTTUser> activateImpl(int partnerId, String username, String activationToken) {
        ActivateOttUserBuilder activateOttUserBuilder = activate(partnerId, username, activationToken)
                .setCompletion((ApiCompletion<OTTUser>) result -> {
                    if (result.isSuccess()) {
                        // TODO: 3/22/2018 fix schema assertions
                    }
                    ottUserResponse = result;
                    done.set(true);
                });
        APIOkRequestsExecutor.getExecutor().queue(activateOttUserBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return ottUserResponse;
    }

    //addRole
    public static Response<Boolean> addRoleImpl(String ks, int roleId) {
        AddRoleOttUserBuilder addRoleOttUserBuilder = addRole(roleId)
                .setCompletion((ApiCompletion<Boolean>) result -> {
                    if (result.isSuccess()) {
                        // TODO: 3/22/2018 fix schema assertions
                    }
                    booleanResponse = result;
                    client.setKs(ks);
                    done.set(true);
                });
        APIOkRequestsExecutor.getExecutor().queue(addRoleOttUserBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return booleanResponse;
    }

    //delete
    public static Response<Boolean> deleteImpl(String ks) {
        DeleteOttUserBuilder deleteOttUserBuilder = delete()
                .setCompletion((ApiCompletion<Boolean>) result -> {
                    if (result.isSuccess()) {
                        // TODO: 3/22/2018 fix schema assertions
                    }
                    booleanResponse = result;
                    client.setKs(ks);
                    done.set(true);
                });
        APIOkRequestsExecutor.getExecutor().queue(deleteOttUserBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return booleanResponse;
    }
//
//    public static <T> T get(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T getEncryptedUserId(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
///*    public static <T> T list(String ks, Optional<OTTUserFilter> ottUserFilter) {
//        String body = listRequestBuilder(ks, ottUserFilter);
//        Response response = setPostRequest(body, SERVICE, LIST_ACTION);
//
//        if (isApiException(response)) {
//            return (T) getApiException(response);
//        } else {
//            try {
//                assertThat(response.asString(), matchesJsonSchemaInClasspath(BOOLEAN_RESPONSE_SCHEMA));
//                return GsonParser.parseListResponse(response.asString(), OTTUser.class);
//            } catch (APIException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        // KalturaOTTUserListResponse
//        // TODO: 3/19/2018 implement function
//        return null;
//    }*/
//
//    public static <T> T loginWithPin(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T logout(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T refreshSession(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T resendActivationToken(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T resetPassword(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T setInitialPassword(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T update(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
//
//    public static <T> T updateLoginData(String ks) {
//        // TODO: 3/19/2018 implement function
//        return null;
//    }
}


