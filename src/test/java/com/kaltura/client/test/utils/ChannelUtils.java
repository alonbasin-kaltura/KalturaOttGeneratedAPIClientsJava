package com.kaltura.client.test.utils;

import com.kaltura.client.types.*;

import javax.annotation.Nullable;
import java.util.List;

public class ChannelUtils extends BaseUtils {

    public static DynamicChannel addDynamicChannel(String name, @Nullable String description, @Nullable Boolean isActive, @Nullable String ksqlExpression,
                                            @Nullable ChannelOrder channelOrder, @Nullable List<IntegerValue> assetTypes) {
        DynamicChannel channel = new DynamicChannel();
        channel.setMultilingualName(setTranslationToken(name));
        channel.setMultilingualDescription(setTranslationToken(description));
        channel.setIsActive(isActive);
        channel.setAssetTypes(assetTypes);
        channel.setKSql(ksqlExpression);
        channel.setOrderBy(channelOrder);
        channel.setSystemName(channel.getMultilingualName().get(0).getValue());

        return channel;
    }
}
