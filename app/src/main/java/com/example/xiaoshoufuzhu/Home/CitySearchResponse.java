package com.example.xiaoshoufuzhu.Home;

import java.util.List;

public class CitySearchResponse {
    private String code;
    private List<Location> location;
    private Refer refer;

    public String getCode() {
        return code;
    }

    public List<Location> getLocation() {
        return location;
    }

    public Refer getRefer() {
        return refer;
    }

    public class Location {
        private String name;
        private String id;
        private String lat;
        private String lon;
        private String adm2;
        private String adm1;
        private String country;
        private String tz;
        private String utcOffset;
        private String isDst;
        private String type;
        private String rank;
        private String fxLink;

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getLat() {
            return lat;
        }

        public String getLon() {
            return lon;
        }

        public String getAdm2() {
            return adm2;
        }

        public String getAdm1() {
            return adm1;
        }

        public String getCountry() {
            return country;
        }

        public String getTz() {
            return tz;
        }

        public String getUtcOffset() {
            return utcOffset;
        }

        public String getIsDst() {
            return isDst;
        }

        public String getType() {
            return type;
        }

        public String getRank() {
            return rank;
        }

        public String getFxLink() {
            return fxLink;
        }
    }

    public class Refer {
        private List<String> sources;
        private List<String> license;

        public List<String> getSources() {
            return sources;
        }

        public List<String> getLicense() {
            return license;
        }
    }
}
