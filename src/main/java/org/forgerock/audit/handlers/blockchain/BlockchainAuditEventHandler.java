/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.audit.handlers.blockchain;

import org.forgerock.audit.events.EventTopicsMetaData;
import org.forgerock.audit.events.handlers.AuditEventHandlerBase;
import org.forgerock.http.Client;
import org.forgerock.http.HttpApplicationException;
import org.forgerock.http.apache.async.AsyncHttpClientProvider;
import org.forgerock.http.handler.HttpClientHandler;
import org.forgerock.http.header.ContentTypeHeader;
import org.forgerock.http.protocol.Request;
import org.forgerock.http.spi.Loader;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.*;
import org.forgerock.services.context.Context;
import org.forgerock.util.Options;
import org.forgerock.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import static org.forgerock.http.handler.HttpClientHandler.OPTION_LOADER;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.resource.Responses.newResourceResponse;

public class BlockchainAuditEventHandler extends AuditEventHandlerBase {
    Logger logger=LoggerFactory.getLogger(BlockchainUtils.class);
    private final Client client;
    private final HttpClientHandler defaultHttpClientHandler;

    public BlockchainAuditEventHandler(final BlockchainAuditEventHandlerConfiguration configuration,
                                       final EventTopicsMetaData eventTopicsMetaData) {

        super(configuration.getName(), eventTopicsMetaData, configuration.getTopics(), configuration.isEnabled());
        this.defaultHttpClientHandler = defaultHttpClientHandler();
        this.client = new Client(defaultHttpClientHandler);
    }

    @Override
    public void startup() throws ResourceException { //if one wanted, a property file could be read in here
        BlockchainUtils http_utils = new BlockchainUtils();
        http_utils.postValue("      IDM + blockchain starting up " ); //rj? + timestamp.toString());
    }

    @Override
    public void shutdown() throws ResourceException {
    }

    @Override
    public Promise<ResourceResponse, ResourceException> publishEvent(Context context, String topic, JsonValue jsonValue) {
        try {
            BlockchainUtils http_utils = new BlockchainUtils();
            http_utils.postValue(" from idm" + jsonValue.toString()); //rj? + timestamp.toString());
        } catch (Exception e) {
            logger.error("blockchain e in promise: " + e.toString());
        }
        return newResourceResponse("1234", "", json(object())).asPromise();
    }

    @Override
    public Promise<ResourceResponse, ResourceException> readEvent(Context context, String topic, String id) {
        logger.info("Read Event %s", id);
        return null;
    }

    @Override
    public Promise<QueryResponse, ResourceException> queryEvents(Context context, String s, QueryRequest
            queryRequest, QueryResourceHandler queryResourceHandler) {
        return null;
    }

    private Request createRequest(final String method, final String uri, final Object payload)
            throws URISyntaxException {
        final Request request = new Request();
        request.setMethod(method);
        request.setUri(uri);
        if (payload != null) {
            request.getHeaders().put(ContentTypeHeader.NAME, "application/json; charset=UTF-8");
            request.setEntity(payload);
        }
//        if (basicAuthHeaderValue != null) {
//            request.getHeaders().put("Authorization", basicAuthHeaderValue);
//        }
        return request;
    }

    private HttpClientHandler defaultHttpClientHandler() {
        try {
            return new HttpClientHandler(
                    Options.defaultOptions()
                            .set(OPTION_LOADER, new Loader() {
                                @Override
                                public <S> S load(Class<S> service, Options options) {
                                    return service.cast(new AsyncHttpClientProvider());
                                }
                            }));
        } catch (HttpApplicationException e) {
            throw new RuntimeException("Error while building default HTTP Client", e);
        }
    }

}
