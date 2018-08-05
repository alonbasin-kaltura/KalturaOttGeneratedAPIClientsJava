package com.kaltura.client.test.tests.servicesTests.AssetTests.AssetListTests;

import com.kaltura.client.enums.AssetOrderBy;
import com.kaltura.client.enums.AssetType;
import com.kaltura.client.enums.MetaTagOrderBy;
import com.kaltura.client.enums.PlaybackContextType;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.tests.enums.KsqlKey;
import com.kaltura.client.test.utils.*;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.json.JSONArray;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.kaltura.client.services.AssetService.ListAssetBuilder;
import static com.kaltura.client.services.AssetService.list;
import static com.kaltura.client.test.tests.BaseTest.SharedHousehold.getSharedMasterUserKs;
import static com.kaltura.client.test.tests.enums.KsqlKey.ENTITLED_ASSETS;
import static com.kaltura.client.test.tests.enums.KsqlKey.MEDIA_ID;
import static com.kaltura.client.test.tests.enums.MediaType.EPISODE;
import static com.kaltura.client.test.tests.enums.MediaType.MOVIE;
import static com.kaltura.client.test.utils.AssetUtils.*;
import static com.kaltura.client.test.utils.BaseUtils.getRandomValue;
import static com.kaltura.client.test.utils.dbUtils.DBUtils.getLinearAssetIdAndEpgChannelNameJsonArray;
import static com.kaltura.client.test.utils.dbUtils.DBUtils.getMediaTypeId;
import static com.kaltura.client.test.utils.ingestUtils.IngestEpgUtils.EpgData;
import static com.kaltura.client.test.utils.ingestUtils.IngestEpgUtils.insertEpg;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SearchAssetFilterTests extends BaseTest {

    private final String tagName = "Genre";
    private final String metaName = "synopsis";
    private final String metaName2 = "Short title";
    private final String metaValue1 = "A" + getRandomValue("_");
    private final String metaValue2 = "B" + getRandomValue("_");

    private MediaAsset asset, asset2, asset3;
    private ProgramAsset program, program2;
    private String tagValue, masterUserKs;
    private AssetFilter assetFilter;


    @BeforeClass
    private void asset_list_searchAssetFilter_before_class() {
        // Get asset from shared asset method
        tagValue = getRandomValue(tagName + "_");

        ArrayList<String> list = new ArrayList<>();
        list.add(tagValue);

        HashMap<String, List<String>> tagMap = new HashMap<>();
        tagMap.put(tagName, list);

        HashMap<String, String> stringMetaMap1 = new HashMap<>();
        stringMetaMap1.put(metaName, metaValue1);
        stringMetaMap1.put(metaName2, metaValue1);

        HashMap<String, String> stringMetaMap2 = new HashMap<>();
        stringMetaMap2.put(metaName, metaValue2);

        JSONArray ja = getLinearAssetIdAndEpgChannelNameJsonArray();
        String epgChannelName = ja.getJSONObject(0).getString("name");
        String epgChannelName2 = ja.getJSONObject(1).getString("name");

        // ingest asset 1
        VodData vodData1 = new VodData()
                .mediaType(MOVIE);
        asset = insertVod(vodData1);

        // ingest asset 2
        VodData vodData2 = new VodData()
                .mediaType(MOVIE)
                .catalogStartDate(getUtcTimeFormatted(-100))
                .tags(tagMap)
                .strings(stringMetaMap1);
        asset2 = insertVod(vodData2);

        // ingest asset 3
        VodData vodData3 = new VodData()
                .mediaType(EPISODE)
                .catalogStartDate(getUtcTimeFormatted(-10))
                .tags(tagMap)
                .strings(stringMetaMap2);
        asset3 = insertVod(vodData3);

        // ingest epg 1
        EpgData epgData1 = new EpgData(epgChannelName).episodesNum(1);
        program = insertEpg(epgData1).get(0);

        // ingest epg 2
        EpgData epgData2 = new EpgData(epgChannelName2).episodesNum(1);
        program2 = insertEpg(epgData2).get(0);

        Household household = HouseholdUtils.createHousehold();
        masterUserKs = HouseholdUtils.getHouseholdMasterUserKs(household);

        PurchaseUtils.purchasePpv(masterUserKs, Optional.of(asset.getId().intValue()), Optional.empty(), Optional.empty());
    }

    // Filter by KSQL
    // *********************

    // TODO: 27/06/2018  - Add test that filter by Geo block after Alon will refactor the ingest util

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD - filter by entitled asset")
    @Test
    private void listVodAssetsByEntitled() {
        String query = new KsqlBuilder()
                .openAnd()
                .openOr()
                    .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                    .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .closeOr()
                .equal(ENTITLED_ASSETS.getValue(), "entitled")
                .closeAnd()
                .toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(masterUserKs));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD - filter by asset name")
    @Test
    private void listVodAssetsByAssetName() {
        String query = new KsqlBuilder().equal("name", asset.getName()).toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("Reserved key: asset/action/list - VOD - filter by media_id")
    @Test
    private void listVodAssetsByMediaId() {
        String query = new KsqlBuilder().equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId())).toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD - filter by meta")
    @Test
    private void listVodAssetsByMeta() {
        String query = new KsqlBuilder().equal(metaName, metaValue1).toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
    }


    @Severity(SeverityLevel.CRITICAL)
    @Description("Logical conjunction: asset/action/list - VOD - OR query")
    @Test
    private void listVodAssetsWithOrQuery() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .closeOr()
                .toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(2);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("Logical conjunction: asset/action/list - VOD - AND query")
    @Test
    private void listVodAssetsWithAndQuery() {
        String query = new KsqlBuilder()
                .openAnd()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .equal(tagName, tagValue)
                .closeAnd()
                .toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset3.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("Alpha numeric field: asset/action/list - VOD - not query")
    @Test
    private void listVodAssetsWithNotKsqlQuery() {
        String query = new KsqlBuilder()
                .openAnd()
                .notEqual(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .equal(tagName, tagValue)
                .closeAnd()
                .toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
    }


    @Severity(SeverityLevel.CRITICAL)
    @Description("Alpha numeric field: asset/action/list - VOD - with existing meta value (+)")
    @Test
    private void listVodAssetsWithExistingMetaValue() {
        String query = new KsqlBuilder()
                .openAnd()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .closeOr()
                .exists(metaName2)
                .closeAnd()
                .toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("Alpha numeric field: asset/action/list - VOD - like query")
    @Test
    private void listVodAssetsWithLikeKsqlQuery() {
        String query = new KsqlBuilder().like(tagName, tagValue).toString();
        assetFilter = getSearchAssetFilter(query);

        List<Asset> assets = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()))
                .results
                .getObjects();

        assertThat(assets).isNotNull();
        assertThat(assets.size()).isEqualTo(2);
        assertThat(assets).extracting("id").contains(asset3.getId(), asset2.getId()).doesNotContain(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("Alpha numeric field: asset/action/list - VOD - start with query")
    @Test
    private void listVodAssetsWithStartWithKsqlQuery() {
        String query = new KsqlBuilder().startsWith(tagName, tagValue).toString();
        assetFilter = getSearchAssetFilter(query);

        List<Asset> assets = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()))
                .results
                .getObjects();

        assertThat(assets).as("assets list").isNotNull();
        assertThat(assets.size()).isEqualTo(2);
        assertThat(assets).extracting("id").contains(asset3.getId(), asset2.getId()).doesNotContain(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD - filtered by type (Movie)")
    @Test
    private void listVodAssetsFilteredByType() {
        String query = new KsqlBuilder()
                .openAnd()
                .equal(tagName, tagValue)
                .equal("asset_type", getMediaTypeId(MOVIE))
                .closeAnd()
                .toString();
        assetFilter = getSearchAssetFilter(query);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results).isNotNull();
        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD - order by VIEWS")
    @Test
    private void orderVodAssetsByViews() {

        int numOfActionsAsset = 3;
        int numOfActionsAsset2 = 2;
        int numOfActionsAsset3 = 1;

        // Add 3 views to asset
        for (int i = 0; i < numOfActionsAsset; i++) {
            addViewToAsset(asset, AssetType.MEDIA);
        }

        // Add 2 views to asset2
        for (int i = 0; i < numOfActionsAsset2; i++) {
            addViewToAsset(asset2, AssetType.MEDIA);
        }

        // Add 1 view to asset3
        for (int i = 0; i < numOfActionsAsset3; i++) {
            addViewToAsset(asset3, AssetType.MEDIA);
        }

        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .closeOr()
                .toString();
        assetFilter = getSearchAssetFilter(query, null, null, null, null, AssetOrderBy.VIEWS_DESC.getValue());

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(3);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset3.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD -  order by NAME (DESC/ASC")
    @Test(enabled = true)
    private void orderVodAssetsByName() {
        VodData vodData = new VodData().name("AAA");
        updateVod(asset.getName(), vodData);

        vodData.name("BBB");
        updateVod(asset2.getName(), vodData);

        vodData.name("CCC");
        updateVod(asset3.getName(), vodData);

        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .closeOr()
                .toString();

        assetFilter = getSearchAssetFilter(query,null, null, null, null, AssetOrderBy.NAME_ASC.getValue());

        ListAssetBuilder listAssetBuilder = list(assetFilter)
                .setKs(getSharedMasterUserKs());
        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(listAssetBuilder);

        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset3.getId());

        assetFilter = getSearchAssetFilter(query,null, null, null, null, AssetOrderBy.NAME_DESC.getValue());

        listAssetBuilder = list(assetFilter).setKs(getSharedMasterUserKs());
        assetListResponse = executor.executeSync(listAssetBuilder);

        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset3.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD -  order by LIKES")
    @Test
    private void orderVodAssetsByLikes() {
        addLikesToAsset(asset3.getId(), 3, AssetType.MEDIA);
        addLikesToAsset(asset2.getId(), 2, AssetType.MEDIA);
        addLikesToAsset(asset.getId(), 1, AssetType.MEDIA);

        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .closeOr()
                .toString();
        assetFilter = getSearchAssetFilter(query,null, null, null, null, AssetOrderBy.LIKES_DESC.getValue());

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(3);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset3.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD -  order by (num of) VOTES and RATING")
    @Test
    private void orderVodAssetsByVotesAndRating() {
        addVotesToAsset(asset2.getId(), 2, AssetType.MEDIA, 1);
        addVotesToAsset(asset3.getId(), 1, AssetType.MEDIA, 5);

        // Order by number of votes (highest to lowest)
        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .closeOr()
                .toString();
        assetFilter = getSearchAssetFilter(query, null, null, null, null, AssetOrderBy.VOTES_DESC.getValue());

        ListAssetBuilder listAssetBuilder = list(assetFilter)
                .setKs(getSharedMasterUserKs());
        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(listAssetBuilder);

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(3);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset3.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset.getId());

        // Order by Ratings (highest to lowest)
        assetFilter = getSearchAssetFilter(query, null, null, null, null, AssetOrderBy.RATINGS_DESC.getValue());

        listAssetBuilder = list(assetFilter).setKs(getSharedMasterUserKs());
        assetListResponse = executor.executeSync(listAssetBuilder);

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(3);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset3.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD -  order by CATALOG START DATE")
    @Test
    private void orderVodAssetsByCatalogStartDate() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .closeOr()
                .toString();
        assetFilter = getSearchAssetFilter(query, null, null, null, null, AssetOrderBy.START_DATE_DESC.getValue());

        ListAssetBuilder listAssetBuilder = list(assetFilter)
                .setKs(getSharedMasterUserKs());
        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(listAssetBuilder);

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(3);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset3.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset.getId());

        assetFilter = getSearchAssetFilter(query, null, null, null, null, AssetOrderBy.START_DATE_ASC.getValue());

        listAssetBuilder = list(assetFilter).setKs(getSharedMasterUserKs());
        assetListResponse = executor.executeSync(listAssetBuilder);

        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(2).getId()).isEqualTo(asset3.getId());
    }

    @Issue("BEO-5254")
    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - VOD -  dynamicOrderBy meta (ASC/DESC")
    @Test(enabled = false)

    private void dynamicOrderByMeta() {
        String query = new KsqlBuilder()
                .openAnd()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset3.getId()))
                .closeOr()
                .equal("asset_type", getMediaTypeId(MOVIE))
                .closeAnd()
                .toString();
        DynamicOrderBy dynamicOrderBy = new DynamicOrderBy();
        dynamicOrderBy.setName(metaName);
        dynamicOrderBy.setOrderBy(MetaTagOrderBy.META_ASC);
        assetFilter = getSearchAssetFilter(query, null, dynamicOrderBy, null, null, null);

        ListAssetBuilder listAssetBuilder = list(assetFilter)
                .setKs(getSharedMasterUserKs());
        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(listAssetBuilder);
        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(2);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(asset2.getId());
        assertThat(assetListResponse.results.getObjects().get(1).getId()).isEqualTo(asset3.getId());
    }


    //TODO - add test for  KalturaPersistedFilter in searchHistory class

    // EPG
    // ***************************************************
    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - EPG - name equal query")
    @Test
    private void listEpgProgramByName() {
        String query = new KsqlBuilder().equal("name", program.getName()).toString();
        assetFilter = getSearchAssetFilter(query, "0", null, null, null, null);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(program.getId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - EPG - epg channel id equal query")
    @Test
    private void listEpgProgramByChannelId() {
        String query = new KsqlBuilder().equal("epg_channel_id", Math.toIntExact(program.getEpgChannelId())).toString();
        assetFilter = getSearchAssetFilter(query);

        List<Asset> epgPrograms = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()))
                .results
                .getObjects();

        assertThat(epgPrograms.size()).isGreaterThan(1);
        assertThat(epgPrograms).extracting("epgChannelId").contains(program.getEpgChannelId());
    }


    @Severity(SeverityLevel.CRITICAL)
    @Description("asset/action/list - EPG - filter by epg channel id")
    @Test
    private void listEpgProgramsFilteredByEpgChannel() {
        String query = new KsqlBuilder()
                .openAnd()
                .openOr()
                .equal("name", program.getName())
                .equal("name", program2.getName())
                .closeOr()
                .equal(KsqlKey.EPG_CHANNEL_ID.getValue(), program2.getEpgChannelId().toString())
                .closeAnd()
                .toString();
        assetFilter = getSearchAssetFilter(query, "0", null, null, null, null);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getSharedMasterUserKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);
        assertThat(assetListResponse.results.getObjects().get(0).getId()).isEqualTo(program2.getId());
    }
}