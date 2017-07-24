// ===================================================================================================
//                           _  __     _ _
//                          | |/ /__ _| | |_ _  _ _ _ __ _
//                          | ' </ _` | |  _| || | '_/ _` |
//                          |_|\_\__,_|_|\__|\_,_|_| \__,_|
//
// This file is part of the Kaltura Collaborative Media Suite which allows users
// to do with audio, video, and animation what Wiki platfroms allow them to do with
// text.
//
// Copyright (C) 2006-2017  Kaltura Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// @ignore
// ===================================================================================================
package com.kaltura.client.services;

import com.kaltura.client.KalturaClient;
import com.kaltura.client.KalturaServiceBase;
import com.kaltura.client.types.*;
import org.w3c.dom.Element;
import com.kaltura.client.utils.ParseUtils;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;

/**
 * This class was generated using clients-generator\exec.php
 * against an XML schema provided by Kaltura.
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

@SuppressWarnings("serial")
public class KalturaSubscriptionSetService extends KalturaServiceBase {
    public KalturaSubscriptionSetService(KalturaClient client) {
        this.kalturaClient = client;
    }

	/**  Add a new subscriptionSet  */
    public KalturaSubscriptionSet add(KalturaSubscriptionSet subscriptionSet) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("subscriptionSet", subscriptionSet);
        this.kalturaClient.queueServiceCall("subscriptionset", "add", kparams, KalturaSubscriptionSet.class);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaSubscriptionSet.class, resultXmlElement);
    }

	/**  Delete a subscriptionSet  */
    public boolean delete(long id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("subscriptionset", "delete", kparams);
        if (this.kalturaClient.isMultiRequest())
            return false;
        Element resultXmlElement = this.kalturaClient.doQueue();
        String resultText = resultXmlElement.getTextContent();
        return ParseUtils.parseBool(resultText);
    }

	/**  Get the subscriptionSet according to the Identifier  */
    public KalturaSubscriptionSet get(long id) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        this.kalturaClient.queueServiceCall("subscriptionset", "get", kparams, KalturaSubscriptionSet.class);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaSubscriptionSet.class, resultXmlElement);
    }

    public KalturaSubscriptionSetListResponse list() throws KalturaApiException {
        return this.list(null);
    }

	/**  Returns a list of subscriptionSets requested by ids or subscription ids  */
    public KalturaSubscriptionSetListResponse list(KalturaSubscriptionSetFilter filter) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("filter", filter);
        this.kalturaClient.queueServiceCall("subscriptionset", "list", kparams, KalturaSubscriptionSetListResponse.class);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaSubscriptionSetListResponse.class, resultXmlElement);
    }

	/**  Update the subscriptionSet  */
    public KalturaSubscriptionSet update(long id, KalturaSubscriptionSet subscriptionSet) throws KalturaApiException {
        KalturaParams kparams = new KalturaParams();
        kparams.add("id", id);
        kparams.add("subscriptionSet", subscriptionSet);
        this.kalturaClient.queueServiceCall("subscriptionset", "update", kparams, KalturaSubscriptionSet.class);
        if (this.kalturaClient.isMultiRequest())
            return null;
        Element resultXmlElement = this.kalturaClient.doQueue();
        return ParseUtils.parseObject(KalturaSubscriptionSet.class, resultXmlElement);
    }
}