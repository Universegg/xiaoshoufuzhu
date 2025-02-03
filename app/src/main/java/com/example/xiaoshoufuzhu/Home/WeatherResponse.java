package com.example.xiaoshoufuzhu.Home;

public class WeatherResponse {
    private Now now;

    public Now getNow() {
        return now;
    }

    public class Now {
        private String obsTime;
        private String temp;
        private String feelsLike;
        private String text;
        private String wind360;
        private String windDir;
        private String windScale;
        private String windSpeed;
        private String humidity;
        private String precip;
        private String pressure;
        private String vis;
        private String cloud;
        private String dew;

        public String getObsTime() {
            return obsTime;
        }

        public String getTemp() {
            return temp;
        }

        public String getFeelsLike() {
            return feelsLike;
        }

        public String getText() {
            return text;
        }

        public String getWind360() {
            return wind360;
        }

        public String getWindDir() {
            return windDir;
        }

        public String getWindScale() {
            return windScale;
        }

        public String getWindSpeed() {
            return windSpeed;
        }

        public String getHumidity() {
            return humidity;
        }

        public String getPrecip() {
            return precip;
        }

        public String getPressure() {
            return pressure;
        }

        public String getVis() {
            return vis;
        }

        public String getCloud() {
            return cloud;
        }

        public String getDew() {
            return dew;
        }
    }
}