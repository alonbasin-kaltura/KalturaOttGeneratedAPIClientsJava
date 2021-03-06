package com.kaltura.client.test.tests.servicesTests.AssetTests.AssetListTests;

import com.kaltura.client.services.ChannelService;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.tests.enums.MediaType;
import com.kaltura.client.test.utils.KsqlBuilder;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.kaltura.client.services.AssetService.list;
import static com.kaltura.client.test.tests.enums.KsqlKey.EPG_ID;
import static com.kaltura.client.test.tests.enums.KsqlKey.MEDIA_ID;
import static com.kaltura.client.test.utils.AssetUtils.getAssets;
import static com.kaltura.client.test.utils.AssetUtils.getPrograms;
import static com.kaltura.client.test.utils.BaseUtils.getEpoch;
import static com.kaltura.client.test.utils.dbUtils.DBUtils.getAPIExceptionFromList;
import static com.kaltura.client.test.utils.dbUtils.DBUtils.setTranslationToken;
import static org.assertj.core.api.Assertions.assertThat;

public class ChannelFilterTests extends BaseTest {

    private MediaAsset asset1, asset2, asset3;
    private ProgramAsset program1;
    private DynamicChannel channel;


    @BeforeClass
    private void asset_list_channelFilter_before_class() {
        // get movie
        asset1 = getAssets(1, MediaType.MOVIE).get(0);

        // get series
        asset2 = getAssets(1, MediaType.SERIES).get(0);

        // get episode
        asset3 = getAssets(1, MediaType.EPISODE).get(0);

        // get epg
        program1 = getPrograms(1).get(0);

        // add assets to channel query
        String query = new KsqlBuilder()
                .openOr()
                    .equal(MEDIA_ID.getValue(), String.valueOf(asset1.getId()))
                    .equal(MEDIA_ID.getValue(), String.valueOf(asset2.getId()))
                    .equal(MEDIA_ID.getValue(), String.valueOf(asset3.getId()))
                    .equal(EPG_ID.getValue(), String.valueOf(program1.getId()))
                .closeOr()
                .toString();

        // add channel
        channel = new DynamicChannel();
        channel.setMultilingualName(setTranslationToken("channel_" + getEpoch()));
        channel.setMultilingualDescription(setTranslationToken("Description of " + channel.getName()));
        channel.setSystemName(channel.getMultilingualName().get(0).getValue());
        channel.setIsActive(true);
        channel.setKSql(query); // "Free='" + channel.getName() + "'"

        channel = (DynamicChannel) executor.executeSync(ChannelService.add(channel)
                .setKs(getOperatorKs())
                .setLanguage("*"))
                .results;
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - channelFilter - idEqual")
    @Test
    private void list_assets_with_channelFilter_by_channelId() {
        // set channelFilter
        ChannelFilter filter = new ChannelFilter();
        filter.setIdEqual(Math.toIntExact(channel.getId()));

        // get list
        ListResponse<Asset> assetListResponse = executor.executeSync(list(filter)
                .setKs(getAnonymousKs())).results;

        // assert response
        assertThat(assetListResponse.getTotalCount()).isEqualTo(4);
        assertThat(assetListResponse.getObjects()).extracting("id")
                .containsExactlyInAnyOrder(asset1.getId(), asset2.getId(), asset3.getId(), program1.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - channelFilter - idEqual and KSql")
    @Test
    private void list_assets_with_channelFilter_by_channelId_and_ksql() {
        // build query
        String query =  new KsqlBuilder()
                .openOr()
                    .equal(MEDIA_ID.getValue(), String.valueOf(asset1.getId()))
                    .equal(EPG_ID.getValue(), String.valueOf(program1.getId()))
                .closeOr()
                .toString();

        // set channelFilter
        ChannelFilter filter = new ChannelFilter();
        filter.setIdEqual(Math.toIntExact(channel.getId()));
        filter.setKSql(query);

        // get list
        ListResponse<Asset> assetListResponse = executor.executeSync(list(filter)
                .setKs(getAnonymousKs())).results;

        // assert response
        assertThat(assetListResponse.getTotalCount()).isEqualTo(2);
        assertThat(assetListResponse.getObjects()).extracting("id")
                .containsExactlyInAnyOrder(asset1.getId(), program1.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list  - with invalid channelId")
    @Test
    private void list_vod_assets_with_channelFilter_by_channelId_and_ksql() {
        // set channelFilter
        int invalidChannelId = 1;
        ChannelFilter filter = new ChannelFilter();
        filter.setIdEqual(invalidChannelId);

        // get list
        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(filter)
                .setKs(getAnonymousKs()));

        // assert response
        assertThat(assetListResponse.results).isNull();
        assertThat(assetListResponse.error.getCode()).isEqualTo(getAPIExceptionFromList(4018).getCode());
    }

    @AfterClass
    private void asset_list_channelFilter_after_class() {
        // delete channel
        executor.executeSync(ChannelService.delete(Math.toIntExact(channel.getId()))
                .setKs(getOperatorKs()));
    }
}
