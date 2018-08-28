package com.kaltura.client.test.utils.ingestUtils;

import com.kaltura.client.enums.AssetOrderBy;
import com.kaltura.client.test.tests.enums.MediaType;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.kaltura.client.services.AssetService.list;
import static com.kaltura.client.test.tests.BaseTest.*;
import static com.kaltura.client.test.utils.BaseUtils.*;

public class IngestVodOPCUtils extends BaseIngestUtils {

    public static String name;
    public static String description;
    public static String textValue;
    public static String dateValue;
    public static double doubleValue;
    public static boolean booleanValue;
    public static List<String> tagValues;

    static final String defaultThumbUrl = "http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png";

    public static final String INGEST_VOD_XML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\n" +
            "<soapenv:Header/>\n" +
            "<soapenv:Body>\n" +
            "<tem:IngestTvinciData>\n" +
            "<tem:request>\n" +
            "<userName>Test_API_27_03</userName>\n" +
            "<passWord>Test_API_27_03</passWord>\n" +
            "<data>\n" +
            "<![CDATA[\n" +
            "<feed>\n" +
            "<export>\n" +
            "<media action=\"insert\" co_guid=\"180822092522774\" entry_id=\"entry_180822092522774\" erase=\"false\" is_active=\"true\">\n" +
            "<basic>\n" +
            "<name>\n" +
            "<value lang=\"eng\">Movie_Name_1808220925223281</value>\n" +
            "</name>\n" +
            "<thumb ingestUrl=\"http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png\"/>\n" +
            "<description>\n" +
            "<value lang=\"eng\">Movie_Description_1808220925223281</value>\n" +
            "</description>\n" +
            "<dates>\n" +
            "<catalog_start/>\n" +
            "<start/>\n" +
            "<catalog_end/>\n" +
            "<end/>\n" +
            "</dates>\n" +
            "<pic_ratios>\n" +
            "<ratio ratio=\"4:3\" thumb=\"http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png\"/>\n" +
            "<ratio ratio=\"16:9\" thumb=\"http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png\"/>\n" +
            "</pic_ratios>\n" +
            "<media_type>Movie</media_type>\n" +
            "<rules>\n" +
            "<geo_block_rule/>\n" +
            "<watch_per_rule>Parent Allowed</watch_per_rule>\n" +
            "<device_rule/>\n" +
            "</rules>\n" +
            "</basic>\n" +
            "<structure>\n" +
            "<strings>\n" +
            "<meta ml_handling=\"unique\" name=\"BoxOffice\">\n" +
            "<value lang=\"eng\">BoxOfficevalue</value>\n" +
            "</meta>\n" +
            "</strings>\n" +
            "<booleans/>\n" +
            "<doubles>\n" +
            "<meta ml_handling=\"unique\" name=\"Runtime2\">123456</meta>\n" +
            "</doubles>\n" +
            "<dates>\n" +
            "<meta ml_handling=\"unique\" name=\"ReleaseDate\">12/12/2012</meta>\n" +
            "</dates>\n" +
            "<metas>\n" +
            "<meta ml_handling=\"unique\" name=\"Actors\">\n" +
            "<container>\n" +
            "<value lang=\"eng\">Jack Nicholson</value>\n" +
            "</container>\n" +
            "<container>\n" +
            "<value lang=\"eng\">Natalie Portman</value>\n" +
            "</container>\n" +
            "</meta>\n" +
            "</metas>\n" +
            "</structure>\n" +
            "<files>\n" +
            "<file PPV_MODULE=\"Shai_Regression_PPV\" alt_cdn_code=\"http://alt_cdntesting.qa.mkaltura.com/p/231/sp/23100/playManifest/entryId/0_3ugsts44/format/hdnetworkmanifest/tags/mbr/protocol/http/f/a.a4m\" assetDuration=\"1000\" billing_type=\"Tvinci\" cdn_code=\"http://cdntesting.qa.mkaltura.com/p/231/sp/23100/playManifest/entryId/0_3ugsts44/format/hdnetworkmanifest/tags/mbr/protocol/http/f/a.a4m\" cdn_name=\"Default CDN\" co_guid=\"Test130301_1180822092522328\" handling_type=\"CLIP\" product_code=\"productExampleCode\" quality=\"HIGH\" type=\"Test130301\"/>\n" +
            "<file PPV_MODULE=\"Subscription_only_PPV\" alt_cdn_code=\"http://alt_cdntesting.qa.mkaltura.com/p/231/sp/23100/playManifest/entryId/0_3ugsts44/format/hdnetworkmanifest/tags/mbr/protocol/http/f/a.a4m\" assetDuration=\"1000\" billing_type=\"Tvinci\" cdn_code=\"http://cdntesting.qa.mkaltura.com/p/231/sp/23100/playManifest/entryId/0_3ugsts44/format/hdnetworkmanifest/tags/mbr/protocol/http/f/a.a4m\" cdn_name=\"Default CDN\" co_guid=\"new file type1_1180822092522328\" handling_type=\"CLIP\" product_code=\"productExampleCode\" quality=\"HIGH\" type=\"new file type1\"/>\n" +
            "</files>\n" +
            "</media>\n" +
            "</export>\n" +
            "</feed>\n" +
            "]]>\n" +
            "</data>\n" +
            "</tem:request>\n" +
            "</tem:IngestTvinciData>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

    public static final String UPDATE_VOD_XML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\n" +
            "<soapenv:Header/>\n" +
            "<soapenv:Body>\n" +
            "<tem:IngestTvinciData>\n" +
            "<tem:request>\n" +
            "<userName>Test_API_27_03</userName>\n" +
            "<passWord>Test_API_27_03</passWord>\n" +
            "<data>\n" +
            "<![CDATA[\n" +
            "<feed>\n" +
            "<export>\n" +
            "<media action=\"update\" co_guid=\"180822092522774\" entry_id=\"entry_180822092522774\" erase=\"false\" is_active=\"true\">\n" +
            "<basic>\n" +
            "<name>\n" +
            "<value lang=\"eng\">Movie_Name_1808220925223281</value>\n" +
            "</name>\n" +
            "<thumb ingestUrl=\"http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png\"/>\n" +
            "<description>\n" +
            "<value lang=\"eng\">Movie_Description_1808220925223281</value>\n" +
            "</description>\n" +
            "<dates>\n" +
            "<catalog_start/>\n" +
            "<start/>\n" +
            "<catalog_end/>\n" +
            "<end/>\n" +
            "</dates>\n" +
            "<pic_ratios>\n" +
            "<ratio ratio=\"4:3\" thumb=\"http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png\"/>\n" +
            "<ratio ratio=\"16:9\" thumb=\"http://opengameart.org/sites/default/files/styles/thumbnail/public/pictures/picture-1760-1321510314.png\"/>\n" +
            "</pic_ratios>\n" +
            "<media_type>Movie</media_type>\n" +
            "<rules>\n" +
            "<geo_block_rule/>\n" +
            "<watch_per_rule>Parent Allowed</watch_per_rule>\n" +
            "<device_rule/>\n" +
            "</rules>\n" +
            "</basic>\n" +
            "<structure>\n" +
            "<strings>\n" +
            "<meta ml_handling=\"unique\" name=\"BoxOffice\">\n" +
            "<value lang=\"eng\">BoxOfficevalue</value>\n" +
            "</meta>\n" +
            "</strings>\n" +
            "<booleans/>\n" +
            "<doubles>\n" +
            "<meta ml_handling=\"unique\" name=\"Runtime2\">123456</meta>\n" +
            "</doubles>\n" +
            "<dates>\n" +
            "<meta ml_handling=\"unique\" name=\"ReleaseDate\">12/12/2012</meta>\n" +
            "</dates>\n" +
            "<metas>\n" +
            "<meta ml_handling=\"unique\" name=\"Actors\">\n" +
            "<container>\n" +
            "<value lang=\"eng\">Jack Nicholson</value>\n" +
            "</container>\n" +
            "<container>\n" +
            "<value lang=\"eng\">Natalie Portman</value>\n" +
            "</container>\n" +
            "</meta>\n" +
            "</metas>\n" +
            "</structure>\n" +
            "<files/>\n" +
            "</media>\n" +
            "</export>\n" +
            "</feed>\n" +
            "]]>\n" +
            "</data>\n" +
            "</tem:request>\n" +
            "</tem:IngestTvinciData>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

    public static final String DELETE_VOD_XML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">\n" +
            "<soapenv:Header/>\n" +
            "<soapenv:Body>\n" +
            "<tem:IngestTvinciData>\n" +
            "<tem:request>\n" +
            "<userName>Test_API_27_03</userName>\n" +
            "<passWord>Test_API_27_03</passWord>\n" +
            "<data>\n" +
            "<![CDATA[\n" +
            "<feed>\n" +
            "<export>\n" +
            "<media action=\"delete\" co_guid=\"180822092522774\" entry_id=\"entry_180822092522774\" erase=\"false\" is_active=\"true\">\n" +
            "</media>\n" +
            "</export>\n" +
            "</feed>\n" +
            "]]>\n" +
            "</data>\n" +
            "</tem:request>\n" +
            "</tem:IngestTvinciData>\n" +
            "</soapenv:Body>\n" +
            "</soapenv:Envelope>";

    // media types
    public static final String EPISODE = "Episode";
    public static final String MOVIE = "Movie";
    public static final String SERIES = "Series";

    // TODO: how to get these data from DB or request?
    //Movie fields
    public static final String mediaTextFieldName = "BoxOffice";
    public static final String mediaDateFieldName = "ReleaseDate";
    public static final String mediaNumberFieldName = "Runtime2";
    public static final String mediaBooleanFieldName = "IsAgeLimited";
    public static final String mediaTagFieldName = "Actors";

    //Episode fields
    public static final String episodeTextFieldName = "TwitterHashtag";
    public static final String episodeDateFieldName = "Date";
    public static final String episodeNumberFieldName = "CommonIpAddress";
    public static final String episodeBooleanFieldName = "CyyNCAh";
    public static final String episodeTagFieldName = "Studio";

    //Series fields
    public static final String seriesTextFieldName = "SeriesID";
    public static final String seriesDateFieldName = "DateField";
    public static final String seriesNumberFieldName = "ReleaseYear";
    public static final String seriesBooleanFieldName = "IsWestern";
    public static final String seriesTagFieldName = "Studio";

    // fields & values
    public static HashMap<String, String> stringMetaMap = new HashMap<>();
    public static HashMap<String, Double> numberMetaMap = new HashMap<>();
    public static HashMap<String, Boolean> booleanHashMap = new HashMap<>();
    public static HashMap<String, String> datesMetaMap = new HashMap<>();
    public static HashMap<String, List<String>> tagsMetaMap = new HashMap<>();
    public static List<IngestVodUtils.VODFile> assetFiles = new ArrayList<>();

    public static IngestVodUtils.VodData getVodData(String mediaType) {
        switch (mediaType) {
            case "Movie":
                return new IngestVodUtils.VodData()
                        .name(name)
                        .description(description)
                        .mediaType(MediaType.MOVIE)
                        .thumbUrl(defaultThumbUrl)
                        .strings(stringMetaMap)
                        .booleans(booleanHashMap)
                        .numbers(numberMetaMap)
                        .dates(datesMetaMap)
                        .tags(tagsMetaMap)
                        .isVirtual(false)
                        .assetFiles(assetFiles);
            case "Episode":
                return new IngestVodUtils.VodData()
                        .name(name)
                        .description(description)
                        .mediaType(MediaType.EPISODE)
                        .thumbUrl(defaultThumbUrl)
                        .strings(stringMetaMap)
                        .booleans(booleanHashMap)
                        .numbers(numberMetaMap)
                        .dates(datesMetaMap)
                        .tags(tagsMetaMap)
                        .isVirtual(false)
                        .assetFiles(assetFiles);
            case "Series":
                return new IngestVodUtils.VodData()
                        .name(name)
                        .description(description)
                        .mediaType(MediaType.SERIES)
                        .thumbUrl(defaultThumbUrl)
                        .strings(stringMetaMap)
                        .booleans(booleanHashMap)
                        .numbers(numberMetaMap)
                        .dates(datesMetaMap)
                        .tags(tagsMetaMap)
                        .isVirtual(true)
                        .assetFiles(assetFiles);
            default:
                return null;
        }
    }

    static void fillMediaMapsWithData() {
        stringMetaMap.put(mediaTextFieldName, textValue);
        numberMetaMap.put(mediaNumberFieldName, doubleValue);
        datesMetaMap.put(mediaDateFieldName, dateValue);
        booleanHashMap.put(mediaBooleanFieldName, booleanValue);
        tagsMetaMap.put(mediaTagFieldName, tagValues);
    }

    static void fillEpisodeMapsWithData() {
        stringMetaMap.put(episodeTextFieldName, textValue);
        numberMetaMap.put(episodeNumberFieldName, doubleValue);
        datesMetaMap.put(episodeDateFieldName, dateValue);
        booleanHashMap.put(episodeBooleanFieldName, booleanValue);
        tagsMetaMap.put(episodeTagFieldName, tagValues);
    }

    static void fillSeriesMapsWithData() {
        stringMetaMap.put(seriesTextFieldName, textValue);
        numberMetaMap.put(seriesNumberFieldName, doubleValue);
        datesMetaMap.put(seriesDateFieldName, dateValue);
        booleanHashMap.put(seriesBooleanFieldName, booleanValue);
        tagsMetaMap.put(seriesTagFieldName, tagValues);
    }

    public static void generateDefaultValues4Update(boolean previousValue, String mediaType) {
        textValue = "textValue" + getCurrentDateInFormat("MM/dd/yyyy") + "_updated";
        dateValue = getOffsetDateInFormat(1,"MM/dd/yyyy");
        // to round up 2 decimal places
        doubleValue = Math.round(getRandomDoubleValue() * 100.0) / 100.0;
        booleanValue = !previousValue;
        tagValues = new ArrayList<>();
        tagValues.add("Jack NicholsonUpd");
        tagValues.add("Natalie PortmanUpd");
        tagValues.add(textValue);

        fillMapsWithData(mediaType);
    }

    public static void generateDefaultValues4Insert(String mediaType) {
        textValue = "textValue" + getCurrentDateInFormat("MM/dd/yyyy");
        dateValue = getCurrentDateInFormat("MM/dd/yyyy");
        // to round up 2 decimal places
        doubleValue = Math.round(getRandomDoubleValue() * 100.0) / 100.0;
        booleanValue = getRandomBooleanValue();
        tagValues = new ArrayList<>();
        tagValues.add("Jack Nicholson");
        tagValues.add("Natalie Portman");
        tagValues.add(textValue);

        fillMapsWithData(mediaType);
    }

    private static void fillMapsWithData(String mediaType) {
        clearMapsBeforeFilling();
        switch (mediaType) {
            case MOVIE:
                fillMediaMapsWithData();
                break;
            case EPISODE:
                fillEpisodeMapsWithData();
                break;
            case SERIES:
                fillSeriesMapsWithData();
                break;
            default:
                break;
        }
    }

    private static void clearMapsBeforeFilling() {
        stringMetaMap = new HashMap<>();
        numberMetaMap = new HashMap<>();
        booleanHashMap = new HashMap<>();
        datesMetaMap = new HashMap<>();
        tagsMetaMap = new HashMap<>();
    }

    public static String getCoguidOfActiveMediaAsset(int assetType) {
        SearchAssetFilter assetFilter = new SearchAssetFilter();
        assetFilter.setOrderBy(AssetOrderBy.CREATE_DATE_DESC.getValue());
        assetFilter.setTypeIn(String.valueOf(assetType));
        FilterPager pager = new FilterPager();
        pager.setPageSize(1);
        pager.setPageIndex(1);

        Response<ListResponse<Asset>> assetListResponse = executor.executeSync(list(assetFilter, pager)
                        .setKs(getAnonymousKs()));
        //assertThat(assetListResponse.results.getObjects().size()).isEqualTo(1);

        return assetListResponse.results.getObjects().get(0).getExternalId();
    }
}
