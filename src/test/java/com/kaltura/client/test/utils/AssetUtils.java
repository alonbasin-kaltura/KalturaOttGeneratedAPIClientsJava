package com.kaltura.client.test.utils;

import com.kaltura.client.Client;
import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.test.servicesImpl.AssetServiceImpl;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.MediaFile;
import com.kaltura.client.utils.response.base.Response;

import java.util.ArrayList;
import java.util.List;

import static com.kaltura.client.test.tests.BaseTest.SharedHousehold.getsharedMasterUserKs;
import static com.kaltura.client.test.tests.BaseTest.getClient;

public class AssetUtils extends BaseUtils {

    public static List<Integer> getAssetFileIds(String assetId) {
        Client client = getClient(getsharedMasterUserKs());

        AssetReferenceType assetReferenceType = AssetReferenceType.get(AssetReferenceType.MEDIA.getValue());
        Response<Asset> assetResponse = AssetServiceImpl.get(client, assetId, assetReferenceType);
        List<MediaFile> mediafiles = assetResponse.results.getMediaFiles();

        List<Integer> fileIdsList = new ArrayList<>();
        for (MediaFile mediaFile : mediafiles) {
            fileIdsList.add(mediaFile.getId());
        }

        return fileIdsList;
    }
}

