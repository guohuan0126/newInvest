package com.jiuyi.ndr.resource.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by WangGang on 2017/5/22.
 */
@RestController
public class VersionResource {
    private final static Logger logger = LoggerFactory.getLogger(VersionResource.class);
    private static final String VERSION = "20170908 10:00";

    @RequestMapping(path = "/version", method = RequestMethod.GET)
    public String version() {
        logger.info("current version: " + VERSION);
        return VERSION;
    }
}
