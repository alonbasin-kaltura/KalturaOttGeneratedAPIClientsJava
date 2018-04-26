package com.kaltura.client.test.utils;

import com.kaltura.client.types.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.kaltura.client.test.Properties.*;
import static io.restassured.path.xml.XmlPath.from;

public class IngestPPVUtils extends BaseUtils {


    // way to ingest PPV using predefined default values for any of input parameters
    public static Ppv ingestPPVWithDefaultValues(Optional<String> action, Optional<Boolean> isActive, Optional<String> description,
                                                 Optional<String> discount, Optional<Double> price, Optional<String> currency,
                                                 Optional<String> usageModule, Optional<Boolean> isSubscriptionOnly,
                                                 Optional<Boolean> isFirstDeviceLimitation, Optional<String> productCode,
                                                 Optional<String> firstFileType, Optional<String> secondFileType) {
        String actionValue = action.orElse("insert");
        boolean isActiveValue = isActive.isPresent() ? isActive.get() : true;
        String descriptionValue = description.orElse("My ingest PPV");
        String discountValue = discount.orElseGet(() -> getProperty(FIFTY_PERCENTS_ILS_DISCOUNT_NAME));
        double priceValue = price.orElseGet(() -> Double.valueOf(getProperty(AMOUNT_4_99_EUR)));
        String currencyValue = currency.orElseGet(() -> getProperty(CURRENCY_EUR));
        String usageModuleValue = usageModule.orElseGet(() -> getProperty(DEFAULT_USAGE_MODULE_4_INGEST_PPV));
        boolean isSubscriptionOnlyValue = isSubscriptionOnly.isPresent() ? isSubscriptionOnly.get() : false;
        boolean isFirstDeviceLimitationValue = isFirstDeviceLimitation.isPresent() ? isFirstDeviceLimitation.get() : false;
        String productCodeValue = productCode.orElseGet(() -> getProperty(DEFAULT_PRODUCT_CODE));
        String firstFileTypeValue = firstFileType.orElseGet(() -> getProperty(WEB_FILE_TYPE));
        String secondFileTypeValue = secondFileType.orElseGet(() -> getProperty(MOBILE_FILE_TYPE));

        return ingestPPV(actionValue, isActiveValue, descriptionValue, discountValue, priceValue, currencyValue,
                usageModuleValue, isSubscriptionOnlyValue, isFirstDeviceLimitationValue, productCodeValue,
                firstFileTypeValue, secondFileTypeValue);
    }

    // ingest new PPV
    public static Ppv ingestPPV(String action, boolean isActive, String description, String discount,
                                double price, String currency, String usageModule, boolean isSubscriptionOnly,
                                boolean isFirstDeviceLimitation, String productCode, String firstFileType,
                                String secondFileType) {
        String ppvCode = getRandomValue("PPV_", 9999999999L);

        String url = getProperty(INGEST_BASE_URL) + "/Ingest_" + getProperty(API_VERSION) + "/Service.svc?wsdl";
        HashMap headermap = new HashMap<>();
        headermap.put("Content-Type", "text/xml;charset=UTF-8");
        headermap.put("SOAPAction", "http://tempuri.org/IService/IngestBusinessModules");

        String reqBody = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:tem='http://tempuri.org/'>\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <tem:IngestBusinessModules><tem:username>" + getProperty(INGEST_BUSINESS_MODULE_USER_USERNAME) + "</tem:username><tem:password>" +
                        getProperty(INGEST_BUSINESS_MODULE_USER_PASSWORD) + "</tem:password><tem:xml>" +
                "         <![CDATA[" + buildIngestPpvXML(action, ppvCode, isActive, description, discount, price, currency,
                                        usageModule, isSubscriptionOnly, isFirstDeviceLimitation, productCode, firstFileType, secondFileType) +
                "                 ]]></tem:xml></tem:IngestBusinessModules>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        Response resp = RestAssured.given()
                .log().all()
                .headers(headermap)
                .body(reqBody)
                .post(url);

        String reportId = from(resp.asString()).get("Envelope.Body.IngestBusinessModulesResponse.IngestBusinessModulesResult.ReportId").toString();
        //System.out.println("ReportId = " + reportId);

        url = getProperty(INGEST_REPORT_URL) + "/" + getProperty(PARTNER_ID) + "/" + reportId;
        resp = RestAssured.given()
                .log().all()
                .get(url);

        System.out.println(resp.asString());
        System.out.println(resp.asString().split(" = ")[1].replaceAll("\\.", ""));

        String id = resp.asString().split(" = ")[1].replaceAll("\\.", "");

        Ppv ppv = new Ppv();
        ppv.setId(id);
        List<TranslationToken> descriptions = new ArrayList<>();
        TranslationToken translationToken = new TranslationToken();
        translationToken.setValue(description);
        descriptions.add(translationToken);
        ppv.setDescriptions(descriptions);
        PriceDetails priceDetails = new PriceDetails();
        Price priceObj = new Price();
        priceObj.setAmount(price);
        priceObj.setCurrency(currency);
        priceDetails.setPrice(priceObj);
        ppv.setPrice(priceDetails);
        UsageModule usageModuleObj = new UsageModule();
        usageModuleObj.setName(usageModule);
        ppv.setUsageModule(usageModuleObj);
        ppv.setIsSubscriptionOnly(isSubscriptionOnly);
        ppv.setFirstDeviceLimitation(isFirstDeviceLimitation);
        ppv.setProductCode(productCode);

        return ppv;
    }

    private static String buildIngestPpvXML(String action, String ppvCode, boolean isActive, String description, String discount,
                                     double price, String currency, String usageModule, boolean isSubscriptionOnly,
                                     boolean isFirstDeviceLimitation, String productCode, String firstFileType,
                                     String secondFileType) {
        return "<ingest id=\"" + ppvCode + "\">\n" +
                "  <ppvs>\n" +
                "    <ppv code=\"" + ppvCode + "\" action=\"" + action + "\" is_active=\"" + isActive + "\">\n" +
                "      <descriptions>\n" +
                "        <description lang=\"eng\">" + description + "</description>\n" +
                "      </descriptions>\n" +
                "      <price_code>\n" +
                "        <price>" + price + "</price>\n" +
                "        <currency>" + currency + "</currency>\n" +
                "      </price_code>\n" +
                "      <usage_module>" + usageModule + "</usage_module>\n" +
                "      <discount>" + discount + "</discount>\n" +
                "      <coupon_group/>\n" +
                "      <subscription_only>" + isSubscriptionOnly + "</subscription_only>\n" +
                "      <first_device_limitation>" + isFirstDeviceLimitation + "</first_device_limitation>\n" +
                "      <product_code>" + productCode + "</product_code>\n" +
                "      <file_types>\n" +
                "        <file_type>" + firstFileType + "</file_type>\n" +
                "        <file_type>" + secondFileType + "</file_type>\n" +
                "      </file_types>\n" +
                "    </ppv>\n" +
                "  </ppvs>\n" +
                "</ingest>\n";
    }
}
