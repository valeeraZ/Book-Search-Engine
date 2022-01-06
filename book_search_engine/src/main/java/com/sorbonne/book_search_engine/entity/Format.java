package com.sorbonne.book_search_engine.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Sylvain in 2021/12.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Format implements Serializable {
    @JsonProperty(value = "text/plain; charset=utf-8")
    private String text1;
    @JsonProperty(value = "text/plain; charset=us-ascii")
    private String text2;
    @JsonProperty(value = "text/plain")
    private String text3;
    @JsonProperty(value = "image/jpeg")
    private String image;
    @JsonProperty(value = "text/html")
    private String html;
}
