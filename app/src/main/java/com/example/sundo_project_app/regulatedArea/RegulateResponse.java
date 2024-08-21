package com.example.sundo_project_app.regulatedArea;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "response", strict = false)
public class RegulateResponse {

    @Element(name = "header")
    private Header header;

    @Element(name = "body")
    private Body body;

    public Header getHeader() {
        return header;
    }

    public Body getBody() {
        return body;
    }

    @Root(name = "header", strict = false)
    public static class Header {
        @Element(name = "resultCode")
        private String resultCode;

        @Element(name = "resultMsg")
        private String resultMsg;

        // Getters and setters
    }

    @Root(name = "body", strict = false)
    public static class Body {
        @Element(name = "items")
        private Items items;

        @Element(name = "numOfRows")
        private int numOfRows;

        @Element(name = "pageNo")
        private int pageNo;

        @Element(name = "totalCount")
        private int totalCount;

        public Items getItems() {
            return items;
        }

        public int getNumOfRows() {
            return numOfRows;
        }

        public int getPageNo() {
            return pageNo;
        }

        public int getTotalCount() {
            return totalCount;
        }

        @Root(name = "items", strict = false)
        public static class Items {
            @ElementList(name = "item", inline = true)
            private List<Item> itemList;

            public List<Item> getItemList() {
                return itemList;
            }
        }

        @Root(name = "item", strict = false)
        public static class Item {
            @Element(name = "hmpgAddrLcAddr")
            private String hmpgAddrLcAddr;

            @Element(name = "hmpgNm")
            private String hmpgNm;

            // Other fields if needed

            public String getHmpgAddrLcAddr() {
                return hmpgAddrLcAddr;
            }
            public String gethmpgNm() {return hmpgNm;}
        }
    }

}
