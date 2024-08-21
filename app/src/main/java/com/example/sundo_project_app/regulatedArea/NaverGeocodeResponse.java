package com.example.sundo_project_app.regulatedArea;

import com.google.gson.annotations.SerializedName;

public class NaverGeocodeResponse {
    @SerializedName("addresses")
    private Address[] addresses;

    public Address[] getAddresses() {
        return addresses;
    }

    public void setAddresses(Address[] addresses) {
        this.addresses = addresses;
    }

    public static class Address {
        @SerializedName("x")
        private String longitude;

        @SerializedName("y")
        private String latitude;

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }
    }
}

