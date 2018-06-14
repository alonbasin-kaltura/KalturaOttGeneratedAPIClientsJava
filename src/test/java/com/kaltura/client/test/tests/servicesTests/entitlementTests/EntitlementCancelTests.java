package com.kaltura.client.test.tests.servicesTests.entitlementTests;

import com.kaltura.client.enums.AssetType;
import com.kaltura.client.enums.BookmarkActionType;
import com.kaltura.client.enums.TransactionType;
import com.kaltura.client.services.*;
import com.kaltura.client.services.ChannelService.AddChannelBuilder;
import com.kaltura.client.services.BookmarkService.AddBookmarkBuilder;
import com.kaltura.client.services.LicensedUrlService.GetLicensedUrlBuilder;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.utils.*;
import com.kaltura.client.test.utils.ingestUtils.IngestUtils;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Optional;
import static com.kaltura.client.services.EntitlementService.*;
import static com.kaltura.client.services.HouseholdService.delete;
import static com.kaltura.client.test.IngestConstants.INGEST_ACTION_DELETE;
import static com.kaltura.client.test.IngestConstants.INGEST_ACTION_INSERT;
import static com.kaltura.client.test.Properties.WEB_FILE_TYPE;
import static com.kaltura.client.test.Properties.getProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class EntitlementCancelTests extends BaseTest {

    private int subscriptionId;

    private Household testSharedHousehold;
    private HouseholdUser testSharedMasterUser;
    private BookmarkPlayerData playerData;
    private Bookmark bookmark;
    private Channel sharedChannel;

    private final int numberOfUsersInHousehold = 2;
    private final int numberOfDevicesInHousehold = 1;

    @BeforeClass
    public void cancelTestBeforeClass() {
        subscriptionId = Integer.valueOf(getSharedCommonSubscription().getId());

        // set household
        testSharedHousehold = HouseholdUtils.createHousehold(numberOfUsersInHousehold, numberOfDevicesInHousehold, false);
        testSharedMasterUser = HouseholdUtils.getMasterUserFromHousehold(testSharedHousehold);

        playerData = new BookmarkPlayerData();
        playerData.setAction(BookmarkActionType.FIRST_PLAY);
        playerData.setAverageBitrate(0);
        playerData.setTotalBitrate(0);
        playerData.setCurrentBitrate(0);

        bookmark = new Bookmark();
        bookmark.setPosition(0);
        bookmark.setType(AssetType.MEDIA);

        sharedChannel = new Channel();
        sharedChannel.setName(BaseUtils.getRandomValue("Channel_", 999999));
        sharedChannel.setDescription("Description of " + sharedChannel.getName());
        sharedChannel.setIsActive(true);
        sharedChannel.setAssetTypes(null);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("entitlement/action/cancel - cancel subscription")
    @Test
    public void cancelSubscription() {
        // set household
        Household household = HouseholdUtils.createHousehold(numberOfUsersInHousehold, numberOfDevicesInHousehold, false);
        HouseholdUser masterUser = HouseholdUtils.getMasterUserFromHousehold(household);
        String userKs = OttUserUtils.getKs(Integer.parseInt(masterUser.getUserId()), null);

        // grant subscription
        GrantEntitlementBuilder grantEntitlementBuilder = grant(subscriptionId, TransactionType.SUBSCRIPTION, true, 0)
                .setKs(getAdministratorKs()).setUserId(Integer.valueOf(masterUser.getUserId()));
        executor.executeSync(grantEntitlementBuilder);

        // set entitlementFilter
        EntitlementFilter filter = new EntitlementFilter();
        filter.setProductTypeEqual(TransactionType.SUBSCRIPTION);

        // assert entitlement list size == 1
        ListEntitlementBuilder listEntitlementBuilder = EntitlementService.list(filter).setKs(userKs);
        List<Entitlement> entitlementList = executor.executeSync(listEntitlementBuilder).results.getObjects();
        assertThat(entitlementList.size()).isEqualTo(1);

        // cancel subscription
        CancelEntitlementBuilder cancelEntitlementBuilder = cancel(subscriptionId, TransactionType.SUBSCRIPTION).setKs(userKs);
        Response<Boolean> booleanResponse = executor.executeSync(cancelEntitlementBuilder);
        assertThat(booleanResponse.results.booleanValue()).isTrue();

        // assert entitlement list size == 0
        listEntitlementBuilder = EntitlementService.list(filter).setKs(userKs);
        entitlementList = executor.executeSync(listEntitlementBuilder).results.getObjects();
        assertThat(entitlementList.size()).isEqualTo(0);

        // delete household for cleanup
        executor.executeSync(delete(Math.toIntExact(household.getId())).setKs(getAdministratorKs()));
    }

    @Severity(SeverityLevel.NORMAL)
    @Description("entitlement/action/cancel - cancel non-purchased subscription - error 3000")
    @Test
    public void cancelWithInvalidSubscription() {
        // cancel subscription
        int invalidSubscriptionId = 1;
        String userKs = OttUserUtils.getKs(Integer.parseInt(testSharedMasterUser.getUserId()), null);

        CancelEntitlementBuilder cancelEntitlementBuilder = cancel(invalidSubscriptionId, TransactionType.SUBSCRIPTION);
        Response<Boolean> booleanResponse = executor.executeSync(cancelEntitlementBuilder.setKs(userKs));
        assertThat(booleanResponse.results).isNull();
        assertThat(booleanResponse.error.getCode()).isEqualTo(BaseUtils.getAPIExceptionFromList(3000).getCode());
    }

    @Severity(SeverityLevel.NORMAL)
    @Description("entitlement/action/cancel - cancel played subscription - error 3005")
    @Test(enabled = false) // TODO: as not completed
    public void cancelPlayedSubscription() {
        // create mpp having at least 1 media on its channel
        sharedChannel.setFilterExpression("name='" + getSharedMediaAsset().getName() + "'");
        AddChannelBuilder addChannelBuilder = ChannelService.add(sharedChannel);
        Response<Channel> channelResponse = executor.executeSync(addChannelBuilder.setKs(getManagerKs()));
        sharedChannel.setId(channelResponse.results.getId());
        Subscription subscription = IngestUtils.ingestMPP(Optional.of(INGEST_ACTION_INSERT), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(sharedChannel.getName()),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        // set household
        Household household = HouseholdUtils.createHousehold(numberOfUsersInHousehold, numberOfDevicesInHousehold, true);
        //HouseholdUser masterUser = HouseholdUtils.getMasterUserFromHousehold(household);
        String masterKs = HouseholdUtils.getHouseholdMasterUserKs(household, HouseholdUtils.getDevicesListFromHouseHold(household).get(0).getUdid());

        PurchaseUtils.purchaseSubscription(masterKs, Integer.valueOf(subscription.getId()), Optional.empty());

        // get CDN code for media
        MediaFile mediaFile = getMediaFileByType(getSharedMediaAsset(), getProperty(WEB_FILE_TYPE));
        String cdnCode = mediaFile.getCdnCode();

        // check license for play
        LicensedUrlMediaRequest licensedUrlRequest = new LicensedUrlMediaRequest();
        licensedUrlRequest.setAssetId(String.valueOf(getSharedMediaAsset().getId()));
        licensedUrlRequest.setContentId(mediaFile.getId());
        licensedUrlRequest.setBaseUrl(cdnCode);
        GetLicensedUrlBuilder licensedUrlBuilder = LicensedUrlService.get(licensedUrlRequest);
        Response<LicensedUrl> urlResponse = executor.executeSync(licensedUrlBuilder.setKs(masterKs));
        assertThat(urlResponse.results).isNotNull();
        // play
        playerData.setFileId(mediaFile.getId().longValue());
        bookmark.setPlayerData(playerData);
        bookmark.setId(String.valueOf(getSharedMediaAsset().getId()));
        AddBookmarkBuilder addBookmarkBuilder = BookmarkService.add(bookmark);
        Response<Boolean> booleanResponse = executor.executeSync(addBookmarkBuilder.setKs(masterKs));
        assertThat(booleanResponse.results.booleanValue()).isTrue();

        // try cancel
        CancelEntitlementBuilder cancelEntitlementBuilder = cancel(Integer.valueOf(subscription.getId()),
                TransactionType.SUBSCRIPTION);
        booleanResponse = executor.executeSync(cancelEntitlementBuilder.setKs(masterKs));
        assertThat(booleanResponse.results).isNull();
        assertThat(booleanResponse.error.getCode()).isEqualTo(BaseUtils.getAPIExceptionFromList(3005).getCode());
        // TODO: 5/23/2018 complete test

        // delete household for cleanup
        executor.executeSync(delete(Math.toIntExact(household.getId())).setKs(getAdministratorKs()));
        //delete subscription
        IngestUtils.ingestMPP(Optional.of(INGEST_ACTION_DELETE), Optional.of(subscription.getName()), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(sharedChannel.getName()),
                Optional.empty(), Optional.of(getProperty(WEB_FILE_TYPE)), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @AfterClass
    public void cancelTestAfterClass() {
        // delete shared household for cleanup
        executor.executeSync(delete(Math.toIntExact(testSharedHousehold.getId())).setKs(getAdministratorKs()));
    }
    // TODO: 5/22/2018 add cancel ppv test with dynamic data
}
