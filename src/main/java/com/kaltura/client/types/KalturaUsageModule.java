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
import com.kaltura.client.KalturaObjectBase;
import com.kaltura.client.utils.ParseUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class was generated using clients-generator\exec.php
 * against an XML schema provided by Kaltura.
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  Pricing usage module  */
@SuppressWarnings("serial")
public class KalturaUsageModule extends KalturaObjectBase {
	/**  Usage module identifier  */
    public long id = Long.MIN_VALUE;
	/**  Usage module name  */
    public String name;
	/**  The maximum number of times an item in this usage module can be viewed  */
    public int maxViewsNumber = Integer.MIN_VALUE;
	/**  The amount time an item is available for viewing since a user started watching
	  the item  */
    public int viewLifeCycle = Integer.MIN_VALUE;
	/**  The amount time an item is available for viewing  */
    public int fullLifeCycle = Integer.MIN_VALUE;
	/**  Identifies a specific coupon linked to this object  */
    public int couponId = Integer.MIN_VALUE;
	/**  Time period during which the end user can waive his rights to cancel a purchase.
	  When the time period is passed, the purchase can no longer be cancelled  */
    public int waiverPeriod = Integer.MIN_VALUE;
	/**  Indicates whether or not the end user has the right to waive his rights to
	  cancel a purchase  */
    public boolean isWaiverEnabled;
	/**  Indicates that usage is targeted for offline playback  */
    public boolean isOfflinePlayback;

    public KalturaUsageModule() {
    }

    public KalturaUsageModule(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("id")) {
                this.id = ParseUtils.parseBigint(txt);
                continue;
            } else if (nodeName.equals("name")) {
                this.name = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("maxViewsNumber")) {
                this.maxViewsNumber = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("viewLifeCycle")) {
                this.viewLifeCycle = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("fullLifeCycle")) {
                this.fullLifeCycle = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("couponId")) {
                this.couponId = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("waiverPeriod")) {
                this.waiverPeriod = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("isWaiverEnabled")) {
                this.isWaiverEnabled = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("isOfflinePlayback")) {
                this.isOfflinePlayback = ParseUtils.parseBool(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() throws KalturaApiException {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaUsageModule");
        kparams.add("name", this.name);
        kparams.add("maxViewsNumber", this.maxViewsNumber);
        kparams.add("viewLifeCycle", this.viewLifeCycle);
        kparams.add("fullLifeCycle", this.fullLifeCycle);
        kparams.add("couponId", this.couponId);
        kparams.add("waiverPeriod", this.waiverPeriod);
        kparams.add("isWaiverEnabled", this.isWaiverEnabled);
        kparams.add("isOfflinePlayback", this.isOfflinePlayback);
        return kparams;
    }

}

