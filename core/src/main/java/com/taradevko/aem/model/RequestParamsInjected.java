package com.taradevko.aem.model;

import com.taradevko.aem.model.injector.annotation.RequestParameter;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;

import java.util.HashMap;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class)
public class RequestParamsInjected {
    private static final Map<Object, String> CONTENT = new HashMap<>();
    static {
        CONTENT.put("param1", "Content 1");
        CONTENT.put(963, "Content 2");
        CONTENT.put("1rp2", "Content for regexp");
    }

    @RequestParameter
    private String stringParam;

    @RequestParameter(optional = true)
    private Integer integerParam;

    @RequestParameter(regexp = "\\d\\w{2}\\d", optional = true)
    private String regexpParam;

    public String getStringContent() {
        return CONTENT.get(stringParam);
    }

    public String getIntegerContent() {
        return CONTENT.get(integerParam);
    }

    public String getRegexpContent() {
        return CONTENT.get(regexpParam);
    }
}
