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
package com.kaltura.client.types;

import org.w3c.dom.Element;
import com.kaltura.client.KalturaParams;
import com.kaltura.client.KalturaApiException;
import com.kaltura.client.utils.ParseUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class was generated using clients-generator\exec.php
 * against an XML schema provided by Kaltura.
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  KalturaSubscriptionEntitlement  */
@SuppressWarnings("serial")
public class KalturaSubscriptionEntitlement extends KalturaEntitlement {
	/**  The date of the next renewal (only for subscription)  */
    public long nextRenewalDate = Long.MIN_VALUE;
	/**  Indicates whether the subscription is renewable in this purchase (only for
	  subscription)  */
    public boolean isRenewableForPurchase;
	/**  Indicates whether a subscription is renewable (only for subscription)  */
    public boolean isRenewable;
	/**  Indicates whether the user is currently in his grace period entitlement  */
    public boolean isInGracePeriod;
	/**  Payment Gateway identifier  */
    public int paymentGatewayId = Integer.MIN_VALUE;
	/**  Payment Method identifier  */
    public int paymentMethodId = Integer.MIN_VALUE;

    public KalturaSubscriptionEntitlement() {
    }

    public KalturaSubscriptionEntitlement(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("nextRenewalDate")) {
                this.nextRenewalDate = ParseUtils.parseBigint(txt);
                continue;
            } else if (nodeName.equals("isRenewableForPurchase")) {
                this.isRenewableForPurchase = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isRenewable")) {
                this.isRenewable = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isInGracePeriod")) {
                this.isInGracePeriod = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("paymentGatewayId")) {
                this.paymentGatewayId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("paymentMethodId")) {
                this.paymentMethodId = ParseUtils.parseInt(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() throws KalturaApiException {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaSubscriptionEntitlement");
        kparams.add("paymentGatewayId", this.paymentGatewayId);
        kparams.add("paymentMethodId", this.paymentMethodId);
        return kparams;
    }

}

