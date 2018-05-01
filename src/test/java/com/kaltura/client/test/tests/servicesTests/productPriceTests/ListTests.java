package com.kaltura.client.test.tests.servicesTests.productPriceTests;

import com.kaltura.client.Client;
import com.kaltura.client.enums.*;
import com.kaltura.client.test.servicesImpl.EntitlementServiceImpl;
import com.kaltura.client.test.servicesImpl.ProductPriceServiceImpl;
import com.kaltura.client.test.tests.BaseTest;
import com.kaltura.client.test.utils.HouseholdUtils;
import com.kaltura.client.test.utils.OttUserUtils;
import com.kaltura.client.test.utils.PurchaseUtils;
import com.kaltura.client.types.*;
import com.kaltura.client.utils.response.base.Response;
import io.qameta.allure.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Optional;
import static com.kaltura.client.test.IngestConstants.CURRENCY_EUR;
import static com.kaltura.client.test.servicesImpl.ProductPriceServiceImpl.list;
import static com.kaltura.client.test.tests.BaseTest.SharedHousehold.*;
import static com.kaltura.client.test.utils.BaseUtils.getAPIExceptionFromList;
import static org.assertj.core.api.Assertions.assertThat;

public class ListTests extends BaseTest {

    private Client client;
    private EntitlementFilter entitlementPpvsFilter;
    private EntitlementFilter entitlementSubsFilter;
    private Household household;
    private String classMasterUserKs;

    @BeforeClass
    public void beforeClass() {
        client = getClient(getOperatorKs());

        entitlementPpvsFilter = new EntitlementFilter();
        entitlementPpvsFilter.setOrderBy(EntitlementOrderBy.PURCHASE_DATE_ASC.getValue());
        entitlementPpvsFilter.setProductTypeEqual(TransactionType.PPV);
        entitlementPpvsFilter.setEntityReferenceEqual(EntityReferenceBy.HOUSEHOLD);
        entitlementPpvsFilter.setIsExpiredEqual(false);

        entitlementSubsFilter = new EntitlementFilter();
        entitlementSubsFilter.setOrderBy(EntitlementOrderBy.PURCHASE_DATE_ASC.getValue());
        entitlementSubsFilter.setProductTypeEqual(TransactionType.SUBSCRIPTION);
        entitlementSubsFilter.setEntityReferenceEqual(EntityReferenceBy.HOUSEHOLD);
        entitlementSubsFilter.setIsExpiredEqual(false);

        /*Ppv ppv = IngestUtils.ingestPPV(INGEST_ACTION_INSERT, true, "My ingest PPV", getProperty(FIFTY_PERCENTS_ILS_DISCOUNT_NAME),
                Double.valueOf(getProperty(PRICE_CODE_AMOUNT_4_99)), CURRENCY_EUR, getProperty(DEFAULT_USAGE_MODULE_4_INGEST_PPV), false, false,
                getProperty(DEFAULT_PRODUCT_CODE), getProperty(WEB_FILE_TYPE), getProperty(MOBILE_FILE_TYPE));*/

        /*Response<ListResponse<Asset>> ingestedProgrammes = IngestUtils.ingestEPG("Shmulik_Series_1", Optional.of(2), Optional.empty(), Optional.of(30),
                Optional.of("minutes"), Optional.empty(), Optional.of(1), Optional.empty(), Optional.empty(), Optional.empty());
        System.out.println("ID:" + ingestedProgrammes.results.getObjects().get(0).getId());*/

        int numberOfUsers = 2;
        int numberOfDevices = 1;
        household = HouseholdUtils.createHouseHold(numberOfUsers, numberOfDevices, true);
        classMasterUserKs = HouseholdUtils.getHouseholdUserKs(household, HouseholdUtils.getDevicesListFromHouseHold(household).get(0).getUdid());
    }

    @Description("productPrice/action/list - subscription test by Operator without currency")
    @Test(enabled = false) // as used in feature tests
    public void listSubscription() {
        ProductPriceFilter filter = new ProductPriceFilter();
        filter.setSubscriptionIdIn(get5MinRenewableSubscription().getId());
        Response<ListResponse<ProductPrice>> productPriceList = list(client, filter);
        assertThat(productPriceList.results.getObjects().get(0).getProductId()).isEqualToIgnoringCase(get5MinRenewableSubscription().getId().trim());
        assertThat(productPriceList.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceList.results.getObjects().get(0).getPrice().getAmount()).isGreaterThan(0);
    }

    @Description("productPrice/action/list - subscription test with currency by Operator")
    @Test()
    public void listSubscriptionWithCurrencyTest() {
        ProductPriceFilter filter = new ProductPriceFilter();
        filter.setSubscriptionIdIn(get5MinRenewableSubscription().getId());
        client.setCurrency(CURRENCY_EUR);
        Response<ListResponse<ProductPrice>> productPriceList = list(client, filter);
        // TODO: should we create ENUMs for currencies? A: Yes if library doesn't contain them
        assertThat(productPriceList.results.getObjects().get(0).getProductId()).isEqualToIgnoringCase(get5MinRenewableSubscription().getId().trim());
        assertThat(productPriceList.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceList.results.getObjects().get(0).getPrice().getAmount()).isGreaterThan(0);
        assertThat(productPriceList.results.getObjects().get(0).getPrice().getCurrency()).isEqualTo(CURRENCY_EUR);
    }

    @Description("productPrice/action/list - without required fields (subscriptionIdIn, collectionIdIn and fileIdIn are empty)")
    @Test()
    public void listWithoutRequiredFields() {
        ProductPriceFilter filter = new ProductPriceFilter();
        Response<ListResponse<ProductPrice>> productPriceResponse = list(client, filter);

        int errorCode = 500056;
        assertThat(productPriceResponse.results).isNull();
        assertThat(productPriceResponse.error.getCode()).isEqualTo(getAPIExceptionFromList(errorCode).getCode());
        assertThat(productPriceResponse.error.getMessage()).isEqualToIgnoringCase(
                "One of the arguments [KalturaProductPriceFilter.subscriptionIdIn, KalturaProductPriceFilter.fileIdIn, KalturaProductPriceFilter.collectionIdIn] must have a value");
    }

    @Description("productPrice/action/list - ppv test")
    @Test()
    public void ppvTest() {
        // TODO: after fix of BEO-4967 change HouseholdDevice.json to have only 1 enum value in objectType
        client.setKs(getSharedMasterUserKs());

        Response<ListResponse<Entitlement>> entitlementListBeforePurchase = EntitlementServiceImpl.list(client, entitlementPpvsFilter, null);
        assertThat(entitlementListBeforePurchase.results.getTotalCount()).isEqualTo(0);

        ProductPriceFilter ppFilter = new ProductPriceFilter();
        int webMediaFileId = getSharedMediaAsset().getMediaFiles().get(0).getId();
        int mobileMediaFileId = getSharedMediaAsset().getMediaFiles().get(1).getId();
        ppFilter.setFileIdIn(String.valueOf(webMediaFileId));
        ppFilter.setIsLowest(false);
        Response<ListResponse<ProductPrice>> productPriceListBeforePurchase = ProductPriceServiceImpl.list(client, ppFilter);
        // TODO: 4/8/2018 talk with Max about the assertions (currently it not asserting nothing as only actual was implemented)
        assertThat(productPriceListBeforePurchase.results.getTotalCount()).isEqualTo(1);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getProductType()).isEqualTo(TransactionType.PPV);
        assertThat(((PpvPrice)productPriceListBeforePurchase.results.getObjects().get(0)).getFileId()).isEqualTo(webMediaFileId);

        PurchaseUtils.purchasePpv(client, Optional.empty(), Optional.of(webMediaFileId));

        Response<ListResponse<Entitlement>> entitlementListAfterPurchase = EntitlementServiceImpl.list(client, entitlementPpvsFilter, null);
        assertThat(entitlementListAfterPurchase.results.getTotalCount()).isEqualTo(1);
        assertThat(((PpvEntitlement) entitlementListAfterPurchase.results.getObjects().get(0)).getMediaFileId()).isEqualTo(webMediaFileId);
        assertThat(((PpvEntitlement) entitlementListAfterPurchase.results.getObjects().get(0)).getMediaId()).isEqualTo(getSharedMediaAsset().getId().intValue());
        assertThat(entitlementListAfterPurchase.results.getObjects().get(0).getEndDate())
                .isGreaterThan(entitlementListAfterPurchase.results.getObjects().get(0).getCurrentDate());

        assertThat(entitlementListAfterPurchase.results.getObjects().get(0).getPaymentMethod()).isIn(PaymentMethodType.OFFLINE, PaymentMethodType.UNKNOWN);

        Response<ListResponse<ProductPrice>> productPriceListAfterPurchase = ProductPriceServiceImpl.list(client, ppFilter);
        // only 1 item mention in filter
        assertThat(productPriceListAfterPurchase.results.getTotalCount()).isEqualTo(1);
        assertThat(productPriceListAfterPurchase.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.PPV_PURCHASED);
        assertThat(((PpvPrice) productPriceListAfterPurchase.results.getObjects().get(0)).getFileId()).isEqualTo(webMediaFileId);

        ppFilter.setFileIdIn(String.valueOf(mobileMediaFileId));
        Response<ListResponse<ProductPrice>> productPriceListAfterPurchaseForAnotherFileFromTheSameMedia = ProductPriceServiceImpl.list(client, ppFilter);
        assertThat(productPriceListAfterPurchaseForAnotherFileFromTheSameMedia.results.getTotalCount()).isEqualTo(1);
        assertThat(productPriceListAfterPurchaseForAnotherFileFromTheSameMedia.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.PPV_PURCHASED);
        assertThat(((PpvPrice) productPriceListAfterPurchaseForAnotherFileFromTheSameMedia.results.getObjects().get(0)).getFileId()).isEqualTo(mobileMediaFileId);
    }

    @Description("productPrice/action/list - common test for PPV and subscription to check before purchase")
    @Test()
    public void productPriceSubscriptionAndPpvBeforePurchaseTest() {
        int numberOfUsers = 1;
        int numberOfDevices = 1;
        Household household = HouseholdUtils.createHouseHold(numberOfUsers, numberOfDevices, true);
        HouseholdUser masterUser = HouseholdUtils.getMasterUserFromHousehold(household);
        client.setKs(OttUserUtils.getKs(Integer.parseInt(masterUser.getUserId()), null));

        ProductPriceFilter filter = new ProductPriceFilter();
        filter.setSubscriptionIdIn(get5MinRenewableSubscription().getId());
        filter.setFileIdIn(String.valueOf(getSharedWebMediaFile().getId()));
        filter.setIsLowest(false);
        Response<ListResponse<ProductPrice>> productPriceListBeforePurchase = list(client, filter);
        // should be 2 ss one item is subscription an another is media file
        assertThat(productPriceListBeforePurchase.results.getTotalCount()).isEqualTo(2);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getProductType()).isEqualTo(TransactionType.SUBSCRIPTION);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getProductId()).isEqualToIgnoringCase(get5MinRenewableSubscription().getId().trim());
        assertThat(productPriceListBeforePurchase.results.getObjects().get(1).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(1).getProductType()).isEqualTo(TransactionType.PPV);
        assertThat(((PpvPrice) productPriceListBeforePurchase.results.getObjects().get(1)).getFileId()).isEqualTo(getSharedWebMediaFile().getId());
    }

    @Description("productPrice/action/list - subscription test")
    @Test(enabled = false) // as not completed
    public void subscriptionTest() {
        String sharedWebMediaFileId = String.valueOf(getSharedWebMediaFile().getId());

        // TODO: 3/7/2018 add remarks when possible such as below - show to Shmulik / Michael and see if test is clear
        client = getClient(classMasterUserKs);
        Response<ListResponse<Entitlement>> entitlementListBeforePurchase = EntitlementServiceImpl.list(client, entitlementSubsFilter, null);
        assertThat(entitlementListBeforePurchase.results.getTotalCount()).isEqualTo(0);

        ProductPriceFilter ppFilter = new ProductPriceFilter();
        ppFilter.setSubscriptionIdIn(get5MinRenewableSubscription().getId().trim());
        ppFilter.setIsLowest(false);
        Response<ListResponse<ProductPrice>> productPriceListBeforePurchase = ProductPriceServiceImpl.list(client, ppFilter);
        assertThat(productPriceListBeforePurchase.results.getTotalCount()).isEqualTo(1);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getProductType()).isEqualTo(TransactionType.SUBSCRIPTION);
        assertThat(productPriceListBeforePurchase.results.getObjects().get(0).getProductId()).isEqualTo(get5MinRenewableSubscription().getId().trim());

        Response<ListResponse<ProductPrice>> productPriceListBeforePurchase4Anonymous = ProductPriceServiceImpl.list(client, ppFilter);
        assertThat(productPriceListBeforePurchase4Anonymous.results.getTotalCount()).isEqualTo(1);
        assertThat(productPriceListBeforePurchase4Anonymous.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.FOR_PURCHASE);
        assertThat(productPriceListBeforePurchase4Anonymous.results.getObjects().get(0).getProductType()).isEqualTo(TransactionType.SUBSCRIPTION);
        assertThat(productPriceListBeforePurchase4Anonymous.results.getObjects().get(0).getProductId()).isEqualTo(get5MinRenewableSubscription().getId().trim());

        //PurchaseUtils.purchaseSubscription(client, Integer.valueOf(get5MinRenewableSubscription().getId().trim()));

        Response<ListResponse<Entitlement>> entitlementListAfterPurchase = EntitlementServiceImpl.list(client, entitlementSubsFilter, null);
        assertThat(entitlementListAfterPurchase.results.getTotalCount()).isEqualTo(1);
        assertThat(entitlementListAfterPurchase.results.getObjects().get(0).getProductId()).isEqualToIgnoringCase(get5MinRenewableSubscription().getId().trim());
        assertThat(entitlementListAfterPurchase.results.getObjects().get(0).getEndDate()).isGreaterThan(
                entitlementListAfterPurchase.results.getObjects().get(0).getCurrentDate());
        MatcherAssert.assertThat(entitlementListAfterPurchase.results.getObjects().get(0).getPaymentMethod(),
                Matchers.anyOf(Matchers.is(PaymentMethodType.OFFLINE), Matchers.is(PaymentMethodType.UNKNOWN)));

        ppFilter.setFileIdIn(sharedWebMediaFileId);
        Response<ListResponse<ProductPrice>> productPriceListAfterPurchase = ProductPriceServiceImpl.list(client, ppFilter);
        assertThat(productPriceListAfterPurchase.results.getTotalCount()).isEqualTo(2);
        assertThat(productPriceListAfterPurchase.results.getObjects().get(0).getPurchaseStatus()).isEqualTo(PurchaseStatus.SUBSCRIPTION_PURCHASED);
        assertThat(productPriceListAfterPurchase.results.getObjects().get(1).getPurchaseStatus()).isEqualTo(PurchaseStatus.SUBSCRIPTION_PURCHASED);
        // TODO: should we use ENUM containing subs of KalturaProductPrice class such as: KalturaCollectionPrice, KalturaPpvPrice, KalturaSubscriptionPrice???
        // that logic can't be checked by schema as schema can't check that exactly 1st item is Subscription and 2nd one is PPV
        assertThat(productPriceListAfterPurchase.results.getObjects().get(0).getClass().getSimpleName()).isEqualToIgnoringCase("SubscriptionPrice");
        assertThat(productPriceListAfterPurchase.results.getObjects().get(1).getClass().getSimpleName()).isEqualToIgnoringCase("PpvPrice");
        assertThat(productPriceListAfterPurchase.results.getObjects().get(0).getProductType()).isEqualTo(TransactionType.SUBSCRIPTION);
        assertThat(productPriceListAfterPurchase.results.getObjects().get(1).getProductType()).isEqualTo(TransactionType.PPV);
        assertThat(productPriceListAfterPurchase.results.getObjects().get(0).getProductId()).isEqualToIgnoringCase(get5MinRenewableSubscription().getId().trim());
        assertThat(((PpvPrice) productPriceListAfterPurchase.results.getObjects().get(1)).getFileId()).isEqualTo(sharedWebMediaFileId);
    }
}
