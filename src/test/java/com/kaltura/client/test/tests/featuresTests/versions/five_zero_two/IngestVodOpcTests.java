package com.kaltura.client.test.tests.featuresTests.versions.five_zero_two;

import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.services.LanguageService;
import com.kaltura.client.services.PpvService;
import com.kaltura.client.services.ProductPriceService;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.utils.BaseUtils;
import com.kaltura.client.test.utils.KsqlBuilder;
import com.kaltura.client.test.utils.dbUtils.DBUtils;
import com.kaltura.client.types.*;
import io.qameta.allure.Issue;
import io.qameta.allure.Link;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.kaltura.client.services.AssetService.*;
import static com.kaltura.client.services.AssetService.get;
import static com.kaltura.client.test.tests.enums.IngestAction.*;
import static com.kaltura.client.test.tests.enums.KsqlKey.MEDIA_ID;
import static com.kaltura.client.test.tests.enums.MediaType.*;
import static com.kaltura.client.test.utils.BaseUtils.*;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodOpcUtils.delayBetweenRetriesInSeconds;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodOpcUtils.*;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodOpcUtils.maxTimeExpectingValidResponseInSeconds;
import static com.kaltura.client.test.utils.ingestUtils.IngestVodUtils.*;
import static io.restassured.path.xml.XmlPath.from;
import static java.util.TimeZone.getTimeZone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

/**
 *
 * Class to test functionality described in https://kaltura.atlassian.net/browse/BEO-5428
 */
@Link(name = "OPC VOD Ingest", url = "BEO-5428")
@Test(groups = { "opc", "OPC VOD Ingest" })
public class IngestVodOpcTests extends BaseTest {
    private int movieType;
    private int episodeType;
    private int seriesType;

    @BeforeClass()
    public void ingestVodOpcTests_beforeClass() {
        movieType = DBUtils.getMediaTypeId(MOVIE);
        episodeType = DBUtils.getMediaTypeId(EPISODE);
        seriesType = DBUtils.getMediaTypeId(SERIES);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "ingest VOD with filled base meta fields")
    public void insertVodMediaTagFieldName() {
        VodData vodData = getVodData(MOVIE, INSERT);
        MediaAsset movie = insertVod(vodData, true);

        assertThat(movie.getName()).isEqualTo(vodData.name());
        assertThat(movie.getDescription()).isEqualTo(vodData.description());
        assertThat(((MultilingualStringValue)movie.getMetas().get(mediaTextFieldName)).getValue()).isEqualTo(vodData.stringsMeta().get(mediaTextFieldName));
        assertThat(((DoubleValue)movie.getMetas().get(mediaNumberFieldName)).getValue()).isEqualTo(vodData.numbersMeta().get(mediaNumberFieldName));
        assertThat(getFormattedDate(((LongValue)movie.getMetas().get(mediaDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.datesMeta().get(mediaDateFieldName));
        assertThat(((BooleanValue)movie.getMetas().get(mediaBooleanFieldName)).getValue()).isEqualTo(vodData.booleansMeta().get(mediaBooleanFieldName));
        assertThat(movie.getTags().get(mediaTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(mediaTagFieldName));
        assertFiles(vodData.files(), movie.getId().toString());
        assertThat(movie.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());

        // cleanup
        deleteVod(movie.getExternalId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "update VOD with filled base meta fields and erase = true")
    public void updateVodMediaBaseFieldsWithErase() {
        String coguid = getCoguidOfActiveMediaAsset(movieType);
        VodData vodData = new VodData()
                .name(String.valueOf(getEpochInMillis()))
                .isErase(true);

        MediaAsset movie = updateVod(coguid, vodData);

        assertThat(movie.getName()).isEqualTo(vodData.name());
        assertThat(movie.getDescription()).isEqualTo("");

        fail("ask Shir why some of the old data return while some being delete as expected");

//        assertThat(((MultilingualStringValue)movie.getMetas().get(mediaTextFieldName)).getValue()).isEqualTo(vodData.strings().get(mediaTextFieldName));
//        assertThat(((DoubleValue)movie.getMetas().get(mediaNumberFieldName)).getValue()).isEqualTo(vodData.numbers().get(mediaNumberFieldName));
//        assertThat(getFormattedDate(((LongValue)movie.getMetas().get(mediaDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.dates().get(mediaDateFieldName));
//        assertThat(((BooleanValue)movie.getMetas().get(mediaBooleanFieldName)).getValue()).isEqualTo(vodData.booleans().get(mediaBooleanFieldName));
//        assertThat(movie.getTags().get(mediaTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(mediaTagFieldName));
//        assertFiles(vodData.files(), movie.getId().toString());
//        assertThat(movie.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());

    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "update VOD with filled base meta fields and erase = false")
    public void updateVodMediaBaseFieldsWithoutErase() {
        String coguid = getCoguidOfActiveMediaAsset(movieType);
        VodData vodData = getVodData(MOVIE, UPDATE);

        MediaAsset movie = updateVod(coguid, vodData);

        assertThat(movie.getName()).isEqualTo(vodData.name());
        assertThat(movie.getDescription()).isEqualTo(vodData.description());
        assertThat(((MultilingualStringValue)movie.getMetas().get(mediaTextFieldName)).getValue()).isEqualTo(vodData.stringsMeta().get(mediaTextFieldName));
        assertThat(((DoubleValue)movie.getMetas().get(mediaNumberFieldName)).getValue()).isEqualTo(vodData.numbersMeta().get(mediaNumberFieldName));
        assertThat(getFormattedDate(((LongValue)movie.getMetas().get(mediaDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.datesMeta().get(mediaDateFieldName));
        assertThat(((BooleanValue)movie.getMetas().get(mediaBooleanFieldName)).getValue()).isEqualTo(vodData.booleansMeta().get(mediaBooleanFieldName));
        assertThat(movie.getTags().get(mediaTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(mediaTagFieldName));
        assertFiles(vodData.files(), movie.getId().toString());
        assertThat(movie.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "ingest VOD with filled base meta fields")
    public void insertVodEpisodeBaseFields() {
        VodData vodData = getVodData(EPISODE, INSERT);
        MediaAsset episode = insertVod(vodData, true);

        assertThat(episode.getName()).isEqualTo(vodData.name());
        assertThat(episode.getDescription()).isEqualTo(vodData.description());
        assertThat(((MultilingualStringValue) episode.getMetas().get(episodeTextFieldName)).getValue()).isEqualTo(vodData.stringsMeta().get(episodeTextFieldName));
        assertThat(((DoubleValue) episode.getMetas().get(episodeNumberFieldName)).getValue()).isEqualTo(vodData.numbersMeta().get(episodeNumberFieldName));
        assertThat(getFormattedDate(((LongValue) episode.getMetas().get(episodeDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.datesMeta().get(episodeDateFieldName));
        assertThat(((BooleanValue) episode.getMetas().get(episodeBooleanFieldName)).getValue()).isEqualTo(vodData.booleansMeta().get(episodeBooleanFieldName));
        assertThat(episode.getTags().get(episodeTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(episodeTagFieldName));
        assertFiles(vodData.files(), episode.getId().toString());
        assertThat(episode.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());

        // cleanup
        deleteVod(episode.getExternalId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "ingest VOD with filled base meta fields")
    public void insertVodSeriesBaseFields() {
        VodData vodData = getVodData(SERIES, INSERT);
        MediaAsset series = insertVod(vodData, true);

        assertThat(series.getName()).isEqualTo(vodData.name());
        assertThat(series.getDescription()).isEqualTo(vodData.description());
        assertThat(((StringValue) series.getMetas().get(seriesTextFieldName)).getValue()).isEqualTo(vodData.stringsMeta().get(seriesTextFieldName));
        assertThat(((DoubleValue) series.getMetas().get(seriesNumberFieldName)).getValue()).isEqualTo(vodData.numbersMeta().get(seriesNumberFieldName));
        assertThat(getFormattedDate(((LongValue) series.getMetas().get(seriesDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.datesMeta().get(seriesDateFieldName));
        assertThat(((BooleanValue) series.getMetas().get(seriesBooleanFieldName)).getValue()).isEqualTo(vodData.booleansMeta().get(seriesBooleanFieldName));
        assertThat(series.getTags().get(seriesTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(seriesTagFieldName));
        assertFiles(vodData.files(), series.getId().toString());
        assertThat(series.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());

        // cleanup
        deleteVod(series.getExternalId());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "update VOD episode with filled base meta fields")
    public void updateVodEpisodeBaseFields() {
        VodData vodData = getVodData(EPISODE, UPDATE);

        String coguid = getCoguidOfActiveMediaAsset(episodeType);
        MediaAsset episode = updateVod(coguid, vodData);

        assertThat(episode.getName()).isEqualTo(vodData.name());
        assertThat(episode.getDescription()).isEqualTo(vodData.description());
        assertThat(((MultilingualStringValue) episode.getMetas().get(episodeTextFieldName)).getValue()).isEqualTo(vodData.stringsMeta().get(episodeTextFieldName));
        assertThat(((DoubleValue) episode.getMetas().get(episodeNumberFieldName)).getValue()).isEqualTo(vodData.numbersMeta().get(episodeNumberFieldName));
        assertThat(getFormattedDate(((LongValue) episode.getMetas().get(episodeDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.datesMeta().get(episodeDateFieldName));
        assertThat(((BooleanValue) episode.getMetas().get(episodeBooleanFieldName)).getValue()).isEqualTo(vodData.booleansMeta().get(episodeBooleanFieldName));
        assertThat(episode.getTags().get(episodeTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(episodeTagFieldName));
//        assertFiles(vodData.files(), episode.getId().toString());
        assertThat(episode.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "update VOD series with filled base meta fields")
    public void updateVodSeriesBaseFields() {
        VodData vodData = getVodData(SERIES, UPDATE);

        String coguid = getCoguidOfActiveMediaAsset(seriesType);
        MediaAsset series = updateVod(coguid, vodData);

        assertThat(series.getName()).isEqualTo(vodData.name());
        assertThat(series.getDescription()).isEqualTo(vodData.description());
        assertThat(((StringValue) series.getMetas().get(seriesTextFieldName)).getValue()).isEqualTo(vodData.stringsMeta().get(seriesTextFieldName));
        assertThat(((DoubleValue) series.getMetas().get(seriesNumberFieldName)).getValue()).isEqualTo(vodData.numbersMeta().get(seriesNumberFieldName));
        assertThat(getFormattedDate(((LongValue) series.getMetas().get(seriesDateFieldName)).getValue(), getTimeZone("UTC"), "MM/dd/yyyy")).isEqualTo(vodData.datesMeta().get(seriesDateFieldName));
        assertThat(((BooleanValue) series.getMetas().get(seriesBooleanFieldName)).getValue()).isEqualTo(vodData.booleansMeta().get(seriesBooleanFieldName));
        assertThat(series.getTags().get(seriesTagFieldName).getObjects()).extracting("value").containsExactlyElementsOf(vodData.tags().get(seriesTagFieldName));
//        assertFiles(vodData.files(), series.getId().toString());
        assertThat(series.getImages()).extracting("ratio").containsAll(vodData.thumbRatios());
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "delete movie")
    public void deleteMovie() {
        String coguid = getCoguidOfActiveMediaAsset(movieType);
        assertVodDeletion(coguid);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "delete episode")
    public void deleteEpisode() {
        String coguid = getCoguidOfActiveMediaAsset(episodeType);
        assertVodDeletion(coguid);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "delete series")
    public void deleteSeries() {
        String coguid = getCoguidOfActiveMediaAsset(seriesType);
        assertVodDeletion(coguid);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(description = "try insert without coguid")
    public void insertWithEmptyCoguid() {
        // insert with empty coguid
        VodData vodData = getVodData(MOVIE, INSERT).coguid("");
        String invalidXml = buildIngestVodXml(vodData, INSERT);
        Response resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).contains("External identifier is missing");

        // insert without coguid attribute
        invalidXml = invalidXml.replaceAll("co_guid=\"\"", "");
        resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).contains("External identifier is missing");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(description = "try delete without coguid")
    public void deleteWithEmptyCoguid() {
        // delete with empty coguid
        String invalidXml = buildIngestVodXml(new VodData(), DELETE);
        Response resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).contains("External identifier is missing");

        // delete with missing coguid attribute
        invalidXml = invalidXml.replaceAll("co_guid=\"\"", "");
        resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).contains("External identifier is missing");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(description = "try delete with non-existed coguid")
    public void deleteWithNonExistedCoguid() {
        String invalidCoguid = "123456";
        VodData vodData = new VodData().coguid(invalidCoguid);
        String invalidXml = buildIngestVodXml(vodData, DELETE);
        Response resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusWarningMessagePath)).contains("Media Id not exist");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(description = "try insert with empty entry_id")
    public void insertWithEmptyEntryId() {
        VodData vodData = getVodData(MOVIE, INSERT);
        String ingestInsertXml = buildIngestVodXml(vodData, INSERT);

        // entry_id tag empty
        String invalidXml = ingestInsertXml.replaceAll("entry_id=\"entry_" + vodData.coguid() + "\"", "entry_id=\"\"");
        Response resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusWarningMessagePath)).contains("entry_id is missing");

        // without entry_id tag
        invalidXml = ingestInsertXml.replaceAll("entry_id=\"entry_" + vodData.coguid() + "\"", "");
        resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestAssetStatusWarningMessagePath)).contains("entry_id is missing");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(description = "try insert inactive item")
    public void insertInactiveItem() {
        VodData vodData = getVodData(MOVIE, INSERT).isActive(false);
        String invalidXml = buildIngestVodXml(vodData, INSERT);

        Response resp = executeIngestVodRequest(invalidXml);
        String id = from(resp.asString()).get(ingestAssetIdPath).toString();

        SearchAssetFilter assetFilter = new SearchAssetFilter();
        String query = new KsqlBuilder().equal(MEDIA_ID.getValue(), id).toString();
        assetFilter.setKSql(query);

        ListResponse<Asset> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getAnonymousKs()))
                .results;
        assertThat(assetListResponse.getTotalCount()).isEqualTo(0);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(description = "try insert with empty isActive parameter")
    public void insertEmptyIsActive() {
        String ingestInsertXml = buildIngestVodXml(getVodData(MOVIE, INSERT), INSERT);
        String invalidXml = ingestInsertXml.replaceAll("is_active=\"true\"", "is_active=\"\"");

        Response resp = executeIngestVodRequest(invalidXml);
        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).isEqualTo("media.IsActive cannot be empty");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(description = "try insert with empty name")
    public void insertWithEmptyName() {
        // empty name value tag
        VodData vodData = new VodData().mediaType(MOVIE).coguid(String.valueOf(getEpochInMillis())).name("");
        String invalidXml = buildIngestVodXml(vodData, INSERT);

        Response resp = executeIngestVodRequest(invalidXml);
        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).isEqualTo("media.basic.name.value.text cannot be empty");

        // empty name tag
        VodData vodData1 = new VodData().mediaType(MOVIE).coguid(String.valueOf(getEpochInMillis()));
        invalidXml = buildIngestVodXml(vodData1, INSERT);

        resp = executeIngestVodRequest(invalidXml);
        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath)).isEqualTo("media.basic.name cannot be empty");
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(description = "try insert with invalid credentials")
    public void insertWithInvalidCredentials() {
        String statusMessage = "Invalid credentials";
        String status = "ERROR";
        String ingestInsertXml = buildIngestVodXml(new VodData(), INSERT);

        // TODO: 9/17/2018 fix the test to support dynamic accounts

        // invalid user name
        String invalidXml = ingestInsertXml.replaceAll("Name>Test_API_27_03<", "Name>aTest_API_27_03<");
        Response resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestStatusMessagePath)).isEqualTo(statusMessage);
        assertThat(from(resp.asString()).getString(ingestStatusPath)).isEqualTo(status);

        // invalid password
        invalidXml = ingestInsertXml.replaceAll("passWord>Test_API_27_03<", "passWord>aTest_API_27_03<");
        resp = executeIngestVodRequest(invalidXml);

        assertThat(from(resp.asString()).getString(ingestStatusMessagePath)).isEqualTo(statusMessage);
        assertThat(from(resp.asString()).getString(ingestStatusPath)).isEqualTo(status);
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(description = "try insert with invalid meta or tag field")
    public void insertWithInvalidMetaOrTagField() {
        String suffix = "_" + getEpoch();
        VodData vodData = getVodData(MOVIE, INSERT);

        String updatedField = mediaNumberFieldName + suffix;
        vodData.numbersMeta(Map.of(updatedField, getRandomDouble()));
        assertInvalidMovieField(vodData, updatedField, "meta");
        vodData.numbersMeta(Map.of());

        updatedField = mediaBooleanFieldName + suffix;
        vodData.booleansMeta(Map.of(updatedField, getRandomBoolean()));
        assertInvalidMovieField(vodData, updatedField, "meta");
        vodData.booleansMeta(Map.of());

        updatedField = mediaTagFieldName + suffix;
        vodData.tags(Map.of(updatedField, List.of(String.valueOf(getEpochInMillis()))));
        assertInvalidMovieField(vodData, updatedField, "tag");
        vodData.tags(Map.of());

        updatedField = mediaTextFieldName + suffix;
        vodData.stringsMeta(Map.of(updatedField, getRandomString()));
        assertInvalidMovieField(vodData, updatedField, "meta");
        vodData.stringsMeta(Map.of());

        updatedField = mediaDateFieldName + suffix;
        vodData.datesMeta(Map.of(updatedField, BaseUtils.getCurrentDateInFormat("yyyyMMddHHmmss")));
        assertInvalidMovieField(vodData, updatedField, "meta");
        vodData.datesMeta(Map.of());
    }

    @Severity(SeverityLevel.NORMAL)
    @Test(description = "insert multilingual fields", enabled = true)
    public void insertMultiLingualFields() {
        // TODO: 9/17/2018 complete test
        // set multi languages
        List<Language> languages = executor.executeSync(LanguageService.list(new LanguageFilter())
                .setKs(getOperatorKs()))
                .results
                .getObjects();
        String lang1 = languages.get(0).getCode();
        String lang2 = languages.get(1).getCode();

        // prepare data
        String lang1Name = getRandomString();
        String lang2Name = getRandomString();
        String lang1Description = "description_" + lang1Name;
        String lang2Description = "description_" + lang2Name;
        String lang1Meta = "meta_" + lang1Name;
        String lang2Meta = "meta_" + lang2Name;
        String lang1Tag = "tag_" + lang1Name;
        String lang2Tag = "tag_" + lang2Name;

        // set strings meta
        List<String> metaNames = Arrays.asList("shmulik_str3", "BoxOffice", "alon_test2");
        Map<String, Map<String, String>> metas = new HashMap<>();
        metas.put(metaNames.get(0), Map.of(lang1, lang1Meta, lang2, lang2Meta));
        metas.put(metaNames.get(1), Map.of(lang1, lang1Meta, lang2, lang2Meta));
        metas.put(metaNames.get(2), Map.of(lang1, lang1Meta, lang2, lang2Meta));

        // set tags
        Map<String, List<Map<String, String>>> tags = new HashMap<>();
        tags.put("Actors", Arrays.asList(Map.of(lang1, lang1Tag, lang2, lang2Tag), Map.of(lang1, lang1Tag, lang2, lang2Tag), Map.of(lang1, lang1Tag, lang2, lang2Tag)));
        tags.put("TagsTest", Arrays.asList(Map.of(lang1, lang1Tag, lang2, lang2Tag), Map.of(lang1, lang1Tag, lang2, lang2Tag), Map.of(lang1, lang1Tag, lang2, lang2Tag)));

        // ingest vod
        VodData vodData = getVodData(MOVIE, INSERT)
                .multilingualName(Map.of(lang1, lang1Name, lang2, lang2Name))
                .multilingualDescription(Map.of(lang1, lang1Description, lang2, lang2Description))
                .multilingualStringsMeta(metas)
                .multilingualTags(tags);
        MediaAsset asset = insertVod(vodData, false);

        // assert multilingual data
        // assert lang1 asset
        Asset lang1Asset = executor.executeSync(get(String.valueOf(asset.getId()), AssetReferenceType.MEDIA)
                .setKs(getAnonymousKs())
                .setLanguage(lang1))
                .results;

        // assert name
        assertThat(lang1Asset.getName()).isEqualTo(lang1Name);

        // assert description
        assertThat(lang1Asset.getDescription()).isEqualTo(lang1Description);

        // aasert metas
        assertThat(lang1Asset.getMetas().keySet()).containsAll(metaNames);
        lang1Asset.getMetas().forEach((s, value) -> System.out.println(s + "_" + value.getDescription()));
//        assertThat(((MultilingualStringValue)asset.getMetas().get(mediaTextFieldName)).getValue())
//                .isEqualTo(stringMetaDataValue + JAP);

        // assert tags
//        boolean isTagValueFound = isTagValueFound(tagValue1 + JAP, asset);
//        assertThat(isTagValueFound).isEqualTo(true);

        // lang2
        Asset lang2Asset = executor.executeSync(get(String.valueOf(asset.getId()), AssetReferenceType.MEDIA)
                .setKs(getAnonymousKs())
                .setLanguage(lang2))
                .results;

        assertThat(lang2Asset.getName()).isEqualTo(lang2Name);
        assertThat(lang2Asset.getDescription()).isEqualTo(lang2Description);
//        assertThat(((MultilingualStringValue)asset.getMetas().get(mediaTextFieldName)).getValue()).isEqualTo(stringMetaDataValue);
//        // check tag value
//        isTagValueFound = isTagValueFound(tagValue1, asset);
//        assertThat(isTagValueFound).isEqualTo(true);
    }

    @Severity(SeverityLevel.MINOR)
    @Test(description = "ingest VOD with empty images urls")
    public void insertVodMediaBaseWithEmptyImageUrl() {
        // empty thumb tag
        VodData vodData = getVodData(MOVIE, INSERT).thumbUrl("");
        String ingestXml = buildIngestVodXml(vodData, INSERT);

        Response resp = executeIngestVodRequest(ingestXml);
        assertThat(from(resp.asString()).getString(ingestAssetStatusWarningMessagePath)).contains("InvalidUrlForImageInvalidUrlForImage");
    }

    @Severity(SeverityLevel.MINOR)
    @Test(description = "ingest VOD files with same coguid")
    public void insertVodMediaBaseFilesWithSameCoguid() {
        // empty images tag
        VodData vodData = getVodData(MOVIE, INSERT);
        long epoch = getEpoch();
        vodData.files().get(0).coguid(String.valueOf(epoch));
        vodData.files().get(1).coguid(String.valueOf(epoch));
        String ingestXml = buildIngestVodXml(vodData, INSERT);

        Response resp = executeIngestVodRequest(ingestXml);
        assertThat(from(resp.asString()).getString(ingestAssetStatusWarningMessagePath)).contains("MediaFileExternalIdMustBeUnique");
    }

    @Issue("BEO-5584")
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "ingest VOD with different Ppv", enabled = false)
    public void updateVodMediaPpv() {
        List<String> ppvNames = DBUtils.getPpvNames(4);

        // insert vod
        List<VodFile> files = getDefaultAssetFiles(ppvNames.get(0), ppvNames.get(1));
        VodData vodData = getVodData(MOVIE, INSERT).files(files);
        MediaAsset movie = insertVod(vodData, true);

        // update ppvs
        files.get(0).ppvModule(ppvNames.get(2));
        files.get(1).ppvModule(ppvNames.get(3));
        vodData = new VodData().files(files);
        movie = updateVod(movie.getExternalId(), vodData);

        List<MediaFile> mediaFiles = movie.getMediaFiles();
        assertThat(mediaFiles.size()).isEqualTo(2);

        // assert ppvs update
        ProductPriceFilter filter = new ProductPriceFilter();
        List<String> fileIds = Arrays.asList(String.valueOf(mediaFiles.get(0).getId()), String.valueOf(mediaFiles.get(1).getId()));
        filter.setFileIdIn(getConcatenatedString(fileIds));

        com.kaltura.client.utils.response.base.Response<ListResponse<ProductPrice>> productPriceListResponse = executor.executeSync(ProductPriceService.list(filter).setKs(getAnonymousKs()));
        PpvPrice ppvPrice1 = (PpvPrice) productPriceListResponse.results.getObjects().get(0);

        String ppvName1 = executor.executeSync(PpvService.get(Long.parseLong(ppvPrice1
                .getPpvModuleId()))
                .setKs(getAnonymousKs()))
                .results
                .getName();

        System.out.println(ppvName1);

        // TODO: 9/17/2018 complete the test after the bug will be fixed
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "update VOD images")
    public void updateImages() {
        // insert vod
        VodData vodData = getVodData(MOVIE, INSERT);
        MediaAsset mediaAsset = insertVod(vodData, true);

        // get list of original images
        List<MediaImage> originalImages = mediaAsset.getImages();
        assertThat(originalImages.size()).isEqualTo(3);

        // update vod images - enter 2 new images
        List<String> newRatios = Arrays.asList("2:1", "2:3");
        String fakeImageUrl = "https://picsum.photos/200/300/?random";

        VodData updateVodData = new VodData()
                .thumbUrl(fakeImageUrl)
                .thumbRatios(newRatios);
        mediaAsset = updateVod(mediaAsset.getExternalId(), updateVodData);

        // assert update
        List<MediaImage> images = mediaAsset.getImages();
        assertThat(images.size()).isEqualTo(5);

        List<MediaImage> newImages = images.stream()
                .filter(image -> originalImages.stream()
                        .map(MediaImage::getRatio)
                        .noneMatch(s1 -> s1.equals(image.getRatio())))
                .collect(Collectors.toList());

        newImages.forEach(image -> assertThat(image.getUrl()).isNotEmpty());
        assertThat(newImages).extracting("ratio").containsExactlyInAnyOrderElementsOf(newRatios);
    }

    @Issue("BEO-5536")
    @Severity(SeverityLevel.CRITICAL)
    @Test(description = "update VOD files")
    public void updateFiles() {
        // insert vod
        List<String> fileTypeNames = DBUtils.getMediaFileTypeNames(2);
        List<String> ppvNames = DBUtils.getPpvNames(2);

        VodData vodData = getVodData(MOVIE, INSERT);
        List<VodFile> files = get2AssetFiles(fileTypeNames.get(0), fileTypeNames.get(1), ppvNames.get(0), ppvNames.get(1));
        vodData.files(files);
        MediaAsset mediaAsset = insertVod(vodData, true);

        // update vod images
        long e = getEpoch();
        String r = String.valueOf(getRandomLong());

        String coguid1 = "file_1_" + e + "_" + r;
        String coguid2 = "file_2_" + e + "_" + r;

        files.get(0).coguid(coguid1).assetDuration("5");
        files.get(1).coguid(coguid2).assetDuration("5");

        VodData updateVodData = new VodData()
                .files(files);
        List<MediaFile> mediaFiles = updateVod(mediaAsset.getExternalId(), updateVodData).getMediaFiles();

        // assert update
        assertThat(mediaFiles.size()).isEqualTo(2);
        mediaFiles.forEach(file -> assertThat(file.getDuration()).isEqualTo(5));
        assertThat(mediaFiles).extracting("externalId").containsExactlyInAnyOrder(coguid1, coguid2);

        // cleanup
        deleteVod(mediaAsset.getExternalId());
    }


    // help methods
    void assertInvalidMovieField(VodData vodData, String fieldName, String fieldType) {
        Response resp = executeIngestVodRequest(buildIngestVodXml(vodData, INSERT));

        assertThat(from(resp.asString()).getString(ingestAssetStatusMessagePath))
                .isEqualTo(fieldType + ": " + fieldName + " does not exist for group");
    }

    void assertVodDeletion(String coguid) {
        SearchAssetFilter assetFilter = new SearchAssetFilter();
        assetFilter.setKSql(new KsqlBuilder().equal("externalId", coguid).toString());

        com.kaltura.client.utils.response.base.Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter)
                .setKs(getAnonymousKs()));

        assertThat(assetListResponse.results.getTotalCount()).isEqualTo(1);

        deleteVod(coguid);

        ListAssetBuilder listAssetBuilder = list(assetFilter).setKs(getAnonymousKs());
        await()
                .pollInterval(delayBetweenRetriesInSeconds, TimeUnit.SECONDS)
                .atMost(maxTimeExpectingValidResponseInSeconds, TimeUnit.SECONDS)
                .until(() -> (executor.executeSync(listAssetBuilder).results.getTotalCount() == 0));
    }

    // to check that ingested file data are corresponding to getAsset file data
    private void assertFiles(List<VodFile> ingestAssetFiles, String assetId) {
        boolean isFileWasFound = false;

        GetAssetBuilder assetBuilder = get(assetId, AssetReferenceType.MEDIA).setKs(getAnonymousKs());
        com.kaltura.client.utils.response.base.Response<Asset> assetGetResponse = executor.executeSync(assetBuilder);
        List<MediaFile> getMediaFiles = assetGetResponse.results.getMediaFiles();

        for (VodFile ingestFile: ingestAssetFiles) {
            for (MediaFile getFile: getMediaFiles) {
                if (getFile.getType().equals(ingestFile.type())) {
                    isFileWasFound = true;
                    assertThat(getFile.getDuration().toString()).isEqualTo(ingestFile.assetDuration());
                    assertThat(getFile.getUrl()).isEqualTo(ingestFile.cdn_code());
                    assertThat(getFile.getAltStreamingCode()).isEqualTo(ingestFile.alt_cdn_code());
                    assertThat(getFile.getExternalStoreId()).isEqualTo(ingestFile.product_code());
                    assertThat(getFile.getExternalId()).isEqualTo(ingestFile.coguid());
                }
            }
        }
        assertThat(isFileWasFound).isEqualTo(true);
    }
}