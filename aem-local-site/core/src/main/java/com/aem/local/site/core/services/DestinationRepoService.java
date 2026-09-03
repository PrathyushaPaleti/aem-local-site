package com.aem.local.site.core.services;

import java.io.IOException;

public interface DestinationRepoService {
     String fetchDestinationRepoJson() throws IOException, InterruptedException;
}
