package com.glebfox.jmix.autoconfigure.locstr;

import com.glebfox.jmix.locstr.LocstrConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({LocstrConfiguration.class})
public class LocstrAutoConfiguration {
}

