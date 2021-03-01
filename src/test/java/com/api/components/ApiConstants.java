package com.api.components;

public class ApiConstants {

    public enum BaseUris {
        ENV_ENUM("https://api.agify.io");

        private String baseUri;

        BaseUris(String uri) {
            this.baseUri = uri;
        }

        public String baseUri() {
            return baseUri;
        }
    }


    public enum Endpoints {
        NAME("/?name={name}");

        private String endpoint;

        Endpoints(String endpoint) {
            this.endpoint = endpoint;
        }

        public String endpoint() {
            return endpoint;
        }
    }


}
