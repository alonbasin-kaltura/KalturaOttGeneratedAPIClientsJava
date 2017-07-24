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
import java.util.HashMap;
import com.kaltura.client.utils.ParseUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class was generated using clients-generator\exec.php
 * against an XML schema provided by Kaltura.
 * 
 * MANUAL CHANGES TO THIS CLASS WILL BE OVERWRITTEN.
 */

/**  C-DVR Adapter  */
@SuppressWarnings("serial")
public class KalturaCDVRAdapterProfile extends KalturaObjectBase {
	/**  C-DVR adapter identifier  */
    public int id = Integer.MIN_VALUE;
	/**  C-DVR adapter name  */
    public String name;
	/**  C-DVR adapter active status  */
    public boolean isActive;
	/**  C-DVR adapter adapter URL  */
    public String adapterUrl;
	/**  C-DVR adapter extra parameters  */
    public HashMap<String, KalturaStringValue> settings;
	/**  C-DVR adapter external identifier  */
    public String externalIdentifier;
	/**  C-DVR shared secret  */
    public String sharedSecret;
	/**  Indicates whether the C-DVR adapter supports dynamic URLs  */
    public boolean dynamicLinksSupport;

    public KalturaCDVRAdapterProfile() {
    }

    public KalturaCDVRAdapterProfile(Element node) throws KalturaApiException {
        super(node);
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node aNode = childNodes.item(i);
            String nodeName = aNode.getNodeName();
            String txt = aNode.getTextContent();
            if (nodeName.equals("id")) {
                this.id = ParseUtils.parseInt(txt);
                continue;
            } else if (nodeName.equals("name")) {
                this.name = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("isActive")) {
                this.isActive = ParseUtils.parseBool(txt);
                continue;
            } else if (nodeName.equals("adapterUrl")) {
                this.adapterUrl = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("settings")) {
                this.settings = ParseUtils.parseMap(KalturaStringValue.class, aNode);
                continue;
            } else if (nodeName.equals("externalIdentifier")) {
                this.externalIdentifier = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("sharedSecret")) {
                this.sharedSecret = ParseUtils.parseString(txt);
                continue;
            } else if (nodeName.equals("dynamicLinksSupport")) {
                this.dynamicLinksSupport = ParseUtils.parseBool(txt);
                continue;
            } 
        }
    }

    public KalturaParams toParams() throws KalturaApiException {
        KalturaParams kparams = super.toParams();
        kparams.add("objectType", "KalturaCDVRAdapterProfile");
        kparams.add("name", this.name);
        kparams.add("isActive", this.isActive);
        kparams.add("adapterUrl", this.adapterUrl);
        kparams.add("settings", this.settings);
        kparams.add("externalIdentifier", this.externalIdentifier);
        kparams.add("dynamicLinksSupport", this.dynamicLinksSupport);
        return kparams;
    }

}
