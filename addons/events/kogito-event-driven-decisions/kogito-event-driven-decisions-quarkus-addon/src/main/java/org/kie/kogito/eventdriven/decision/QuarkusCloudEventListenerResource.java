/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.eventdriven.decision;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.kie.kogito.event.KogitoEventStreams;
import org.kie.kogito.events.knative.ce.Printer;
import org.kie.kogito.events.knative.ce.http.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.JsonFormat;

@Path("/")
@ApplicationScoped()
public class QuarkusCloudEventListenerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuarkusCloudEventListenerResource.class);

    private Map<String, Emitter<String>> emitters;

    @Inject
    ObjectMapper objectMapper;

    @Inject()
    @Named(KogitoEventStreams.PUBLISHER)
    Emitter<String> emitter;

    @PostConstruct
    void setup() {
        objectMapper.registerModule(JsonFormat.getCloudEventJacksonModule());
    }

    @POST()
    @Consumes({ MediaType.APPLICATION_JSON, JsonFormat.CONTENT_TYPE })
    @Produces(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response cloudEventListener(CloudEvent event) {
        try {
            LOGGER.debug("CloudEvent received: {}", Printer.beautify(event));
            // convert CloudEvent to JSON and send to internal channels
            emitter.send(objectMapper.writeValueAsString(event));
            return Response.ok().build();
        } catch (Exception ex) {
            return Responses.errorProcessingCloudEvent(ex);
        }
    }
}
