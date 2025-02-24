package org.onosproject.rest;

import org.onlab.rest.BaseResource;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("flows")
@Component(immediate = true)
public class FlowStatsResource extends BaseResource {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @GET
    @Produces("application/json")
    @Path("stats")
    public Response getFlowStats() {
        try {
            List<FlowStat> stats = flowRuleService.getFlowEntries().stream()
                    .map(this::convertToStat)
                    .collect(Collectors.toList());
            return Response.ok(new FlowStatsResponse(stats)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to retrieve flow stats"))
                    .build();
        }
    }

    private FlowStat convertToStat(FlowEntry entry) {
        return new FlowStat(
                entry.id().value(),
                entry.appId(),
                entry.state().toString(),
                entry.bytes(),
                entry.packets(),
                entry.lastSeen()
        );
    }

    private static class FlowStatsResponse {
        public final List<FlowStat> flows;

        FlowStatsResponse(List<FlowStat> flows) {
            this.flows = flows;
        }
    }

    private static class FlowStat {
        public final long id;
        public final String appName;
        public final String state;
        public final long bytes;
        public final long packets;
        public final long lastSeen;

        FlowStat(long id, ApplicationId appId, String state,
                long bytes, long packets, long lastSeen) {
            this.id = id;
            this.appName = appId.name();
            this.state = state;
            this.bytes = bytes;
            this.packets = packets;
            this.lastSeen = lastSeen;
        }
    }

    private static class ErrorResponse {
        public final String error;

        ErrorResponse(String error) {
            this.error = error;
        }
    }
}
