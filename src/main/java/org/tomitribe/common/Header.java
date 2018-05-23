/*
 *
 *   Tomitribe Confidential
 *
 *  Copyright Tomitribe Corporation. 2018
 *
 *  The source code for this program is not published or otherwise divested
 *  of its trade secrets, irrespective of what has been deposited with the
 *  U.S. Copyright Office.
 *
 */

package org.tomitribe.common;

public class Header {

    private String name;
    private String description;
    private String rootName;

    public Header(String name, String description, String rootName) {
        this.name = name;
        this.description = description;
        this.rootName = rootName;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description + rootName + ".";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }
}
