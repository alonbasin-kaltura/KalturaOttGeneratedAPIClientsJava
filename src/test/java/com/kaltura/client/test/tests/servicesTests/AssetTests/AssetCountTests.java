package com.kaltura.client.test.tests.servicesTests.AssetTests;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.tests.enums.MediaType;
import com.kaltura.client.test.utils.KsqlBuilder;

import com.kaltura.client.test.utils.dbUtils.DBUtils;
import com.kaltura.client.test.utils.ingestUtils.IngestEpgUtils;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


import static com.kaltura.client.services.AssetService.*;
import static com.kaltura.client.test.utils.dbUtils.DBUtils.getMediaTypeId;
import static com.kaltura.client.test.utils.ingestUtils.IngestEpgUtils.*;
import static com.kaltura.client.test.tests.enums.KsqlKey.EPG_ID;
import static com.kaltura.client.test.tests.enums.KsqlKey.MEDIA_ID;
import static com.kaltura.client.test.tests.enums.MediaType.MOVIE;
import static com.kaltura.client.test.utils.BaseUtils.getRandomValue;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodUtils.*;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodUtils.insertVod;
import static org.assertj.core.api.Assertions.assertThat;

public class AssetCountTests extends BaseTest {

    private ProgramAsset program, program2;
    private MediaAsset asset, asset2;

    //TODO - Change all hardcoded values after Alon will add DB meta anf tags utils.

    private final String metaName1 = "synopsis";
    private final String metaValue1 = metaName1 + getRandomValue("_");

    private final String metaName2 = "runtime";
    private final String metaValue2 = metaName2 + getRandomValue("_");
    private final String metaValue3 = metaName2 + getRandomValue("_");

    private final String tagName = "Studio";
    private final String tagValue = tagName +  getRandomValue("_");

    private final String epgMetaName = "Country";
    private final String epgMetaValue = epgMetaName + getRandomValue("_");

    private final String sharedTagName = "Director";
    private final String sharedTagValue = sharedTagName + getRandomValue("_");

    @BeforeClass
    private void asset_count_before_class() {

        // Metas
        HashMap<String, String> stringMetaMap = new HashMap<>();
        stringMetaMap.put(metaName1, metaValue1);
        stringMetaMap.put(metaName2,metaValue2);

        // Tags
        HashMap<String, List<String>> stringTagMap = new HashMap<>();
        stringTagMap.put(sharedTagName, Arrays.asList(sharedTagValue));
        stringTagMap.put(tagName,Arrays.asList(tagValue));

        // ingest asset 1
        VodData vodData1 = new VodData()
                .mediaType(MOVIE)
                .stringsMeta(stringMetaMap)
                .tags(stringTagMap);
        asset = insertVod(vodData1, true);

        HashMap<String, String> stringMetaMap2 = new HashMap<>();
        stringMetaMap2.put(metaName1, metaValue1);
        stringMetaMap2.put(metaName2,metaValue3);
        stringTagMap.put(tagName,Arrays.asList(tagValue));

        // ingest asset 2
        VodData vodData2 = new VodData()
                .mediaType(MOVIE)
                .stringsMeta(stringMetaMap2);
        asset2 = insertVod(vodData2, true);

        HashMap<String, String> epgMetas = new HashMap<>();
        epgMetas.put(epgMetaName, epgMetaValue);

        HashMap<String,String> epgTags = new HashMap<>();
        epgTags.put(sharedTagName, sharedTagValue);
        
        // ingest epg programs
        EpgData epgData = new EpgData(getSharedEpgChannelName());
        epgData.seasonsNum(1);
        epgData.episodesNum(2);
        epgData.metas(epgMetas);
        epgData.tags(epgTags);

        List<ProgramAsset> programsList = IngestEpgUtils.insertEpg(epgData);
        program = programsList.get(0);
        program2 = programsList.get(1);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("2 VOD assets with the same meta (synopsis) value and pass the meta name in the count request")
    @Test
    private void groupByVodMeta() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .closeOr()
                .toString();

        SearchAssetFilter searchAssetFilter = new SearchAssetFilter();
        searchAssetFilter.setKSql(query);
        searchAssetFilter.setTypeIn(String.valueOf(DBUtils.getMediaTypeId(MediaType.MOVIE)));

        ArrayList<AssetGroupBy> arrayList = new ArrayList<>();
        AssetMetaOrTagGroupBy assetMetaOrTagGroupBy = new AssetMetaOrTagGroupBy();
        assetMetaOrTagGroupBy.setValue(metaName1);
        arrayList.add(assetMetaOrTagGroupBy);

        searchAssetFilter.setGroupBy(arrayList);
        AssetService.CountAssetBuilder countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());

        // asset/action/count - 2 VOD assets in count
        Response<AssetCount> assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(2);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("2 VOD assets with the same tag (Genre) value and pass the tag name in the count request")
    @Test
    private void groupByVodTag() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .closeOr()
                .toString();

        SearchAssetFilter searchAssetFilter = new SearchAssetFilter();
        searchAssetFilter.setKSql(query);
        searchAssetFilter.setTypeIn(String.valueOf(DBUtils.getMediaTypeId(MediaType.MOVIE)));

        ArrayList<AssetGroupBy> arrayList = new ArrayList<>();
        AssetMetaOrTagGroupBy assetMetaOrTagGroupBy = new AssetMetaOrTagGroupBy();
        assetMetaOrTagGroupBy.setValue(tagName);
        arrayList.add(assetMetaOrTagGroupBy);

        searchAssetFilter.setGroupBy(arrayList);
        AssetService.CountAssetBuilder countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());

        // asset/action/count - 2 VOD assets in count
        Response<AssetCount> assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(2);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("2 VOD assets with different meta (Runtime) value and pass the meta name in the count request")
    @Test
    private void groupByVodMetaWithDifferentValues() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset.getId()))
                .equal(MEDIA_ID.getValue(), Math.toIntExact(asset2.getId()))
                .closeOr()
                .toString();

        SearchAssetFilter searchAssetFilter = new SearchAssetFilter();
        searchAssetFilter.setKSql(query);
        searchAssetFilter.setTypeIn(String.valueOf(DBUtils.getMediaTypeId(MediaType.MOVIE)));

        ArrayList<AssetGroupBy> arrayList = new ArrayList<>();
        AssetMetaOrTagGroupBy assetMetaOrTagGroupBy = new AssetMetaOrTagGroupBy();
        assetMetaOrTagGroupBy.setValue(metaName2);
        arrayList.add(assetMetaOrTagGroupBy);

        searchAssetFilter.setGroupBy(arrayList);
        AssetService.CountAssetBuilder countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());

        // asset/action/count - 2 VOD assets in count
        Response<AssetCount> assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(2);
        assertThat(assetCountResponse.results.getSubs().get(0).getObjects().get(0).getCount()).isEqualTo(1);
        assertThat(assetCountResponse.results.getSubs().get(0).getObjects().get(0).getValue()).isEqualTo(metaValue3);
        assertThat(assetCountResponse.results.getSubs().get(0).getObjects().get(1).getCount()).isEqualTo(1);
        assertThat(assetCountResponse.results.getSubs().get(0).getObjects().get(1).getValue()).isEqualTo(metaValue2);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("2 EPG programs with the same meta (Country) value and pass the meta name in the count request")
    @Test
    private void groupByEPGMeta() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(EPG_ID.getValue(), String.valueOf(program.getId()))
                .equal(EPG_ID.getValue(),String.valueOf(program2.getId()))
                .closeOr()
                .toString();

        SearchAssetFilter searchAssetFilter = new SearchAssetFilter();
        searchAssetFilter.setKSql(query);
        searchAssetFilter.setTypeIn("0");

        ArrayList<AssetGroupBy> arrayList = new ArrayList<>();
        AssetMetaOrTagGroupBy assetMetaOrTagGroupBy = new AssetMetaOrTagGroupBy();
        assetMetaOrTagGroupBy.setValue(epgMetaName);
        arrayList.add(assetMetaOrTagGroupBy);

        searchAssetFilter.setGroupBy(arrayList);
        CountAssetBuilder countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());

        // asset/action/count - 2 EPG in count
        Response<AssetCount> assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(2);
    }

    // TODO
    @Description("2 EPG programs with the same tag (Director) value and pass the tag name in the count request")
    @Test
    private void groupByEPGTag() {
        String query = new KsqlBuilder()
                .openOr()
                .equal(EPG_ID.getValue(), String.valueOf(program.getId()))
                .equal(EPG_ID.getValue(),String.valueOf(program2.getId()))
                .closeOr()
                .toString();

        SearchAssetFilter searchAssetFilter = new SearchAssetFilter();
        searchAssetFilter.setKSql(query);
        searchAssetFilter.setTypeIn("0");

        ArrayList<AssetGroupBy> arrayList = new ArrayList<>();
        AssetMetaOrTagGroupBy assetMetaOrTagGroupBy = new AssetMetaOrTagGroupBy();
        assetMetaOrTagGroupBy.setValue(sharedTagName);
        arrayList.add(assetMetaOrTagGroupBy);

        searchAssetFilter.setGroupBy(arrayList);
        CountAssetBuilder countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());

        // asset/action/count - 2 EPG in count
        Response<AssetCount> assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(2);
    }


    @Description("VOD and EPG program with the same tag value")
    @Test
    private void groupByEPGAndVODTag() {
        String query = new KsqlBuilder()
                .equal(sharedTagName, sharedTagValue)
                .toString();

        SearchAssetFilter searchAssetFilter = new SearchAssetFilter();
        searchAssetFilter.setKSql(query);

        ArrayList<AssetGroupBy> arrayList = new ArrayList<>();
        AssetMetaOrTagGroupBy assetMetaOrTagGroupBy = new AssetMetaOrTagGroupBy();
        assetMetaOrTagGroupBy.setValue(sharedTagName);
        arrayList.add(assetMetaOrTagGroupBy);

        searchAssetFilter.setGroupBy(arrayList);
        CountAssetBuilder countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());

        // asset/action/count - no filtering (1 VOD asset and 2 EPG programs in count)
        Response<AssetCount> assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(3);

        // asset/action/count - filter by movie type id (1 VOD asset in count)
        searchAssetFilter.setTypeIn(String.valueOf(getMediaTypeId(MediaType.MOVIE)));
        countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());
        assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(1);

        // asset/action/count - filter by EPG type id (2 EPG programs in count)
        searchAssetFilter.setTypeIn("0");
        countAssetBuilder = AssetService.count(searchAssetFilter)
                .setKs(BaseTest.getAnonymousKs());
        assetCountResponse = executor.executeSync(countAssetBuilder);
        assertThat(assetCountResponse.results.getCount()).isEqualTo(2);
    }
}
