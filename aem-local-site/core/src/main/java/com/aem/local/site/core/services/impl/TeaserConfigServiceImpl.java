package com.aem.local.site.core.services.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.aem.local.site.core.services.TeaserConfigService;

@Component(
    service = TeaserConfigService.class,
    immediate = true
)
@Designate(ocd = TeaserConfigServiceImpl.Configuration.class)
public class TeaserConfigServiceImpl implements TeaserConfigService {

    private String fallbackImage;
    private String promoUrl;

    // OSGi Configuration Properties (visible in AEM Web Console / system/console/configMgr)
    @ObjectClassDefinition(name = "Teaser Component Configuration Service")
    public @interface Configuration {
        @AttributeDefinition(name = "Fallback Background Image Path")
        String fallback_image() default "https://images.pexels.com/photos/38343759/pexels-photo-38343759.jpeg";

        @AttributeDefinition(name = "Promo Destination URL")
        String promo_url() default "/content/aem-local-site/us/en/products.html?CID=ALL835938503";
    }

    @Activate
    protected void activate(Configuration config) {
        this.fallbackImage = config.fallback_image();
        this.promoUrl = config.promo_url();
    }

    @Override
    public String getFallbackBackgroundImage() {
        return this.fallbackImage;
    }

    @Override
    public String getPromoDestinationUrl() {
        return this.promoUrl;
    }

    
}
