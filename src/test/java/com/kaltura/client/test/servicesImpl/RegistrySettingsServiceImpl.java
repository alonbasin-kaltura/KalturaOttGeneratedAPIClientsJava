package com.kaltura.client.test.servicesImpl;

import com.kaltura.client.Client;
import com.kaltura.client.services.RegistrySettingsService;
import com.kaltura.client.test.TestAPIOkRequestsExecutor;
import com.kaltura.client.types.ListResponse;
import com.kaltura.client.types.RegistrySettings;
import com.kaltura.client.utils.response.base.ApiCompletion;
import com.kaltura.client.utils.response.base.Response;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.kaltura.client.services.RegistrySettingsService.*;
import static org.awaitility.Awaitility.await;

public class RegistrySettingsServiceImpl {

    private static final AtomicBoolean done = new AtomicBoolean(false);

    private static Response<ListResponse<RegistrySettings>> registrySettingsListResponse;

    // list
    public static Response<ListResponse<RegistrySettings>> list(Client client) {
        ListRegistrySettingsBuilder listRegistrySettingsBuilder = RegistrySettingsService.list()
                .setCompletion((ApiCompletion<ListResponse<RegistrySettings>>) result -> {
                    registrySettingsListResponse = result;
                    done.set(true);
                });

        TestAPIOkRequestsExecutor.getExecutor().queue(listRegistrySettingsBuilder.build(client));
        await().untilTrue(done);
        done.set(false);

        return registrySettingsListResponse;
    }
}