package core;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;

/**
 * Minimal logger for request/response traffic.
 *
 * This class intentionally does not perform analysis or detection.
 */
public class SafeLogger {

    private final MontoyaApi api;

    public SafeLogger(MontoyaApi api) {
        this.api = api;
    }

    public void logRequest(HttpRequestToBeSent request, boolean inScope) {
        if (api == null) {
            return;
        }

        if (request == null) {
            api.logging().logToOutput("[LogicHunter][REQ] scope=" + scopeLabel(inScope) + " request=null");
            return;
        }

        String method = safe(request.method());
        String url = safe(request.url());
        int headerCount = request.headers() == null ? 0 : request.headers().size();

        api.logging().logToOutput(
                "[LogicHunter][REQ] scope=" + scopeLabel(inScope)
                        + " method=" + method
                        + " url=" + url
                        + " headers=" + headerCount
        );
    }

    public void logResponse(HttpResponseReceived response, boolean inScope) {
        if (api == null) {
            return;
        }

        if (response == null) {
            api.logging().logToOutput("[LogicHunter][RES] scope=" + scopeLabel(inScope) + " response=null");
            return;
        }

        String method = "";
        String url = "";
        if (response.initiatingRequest() != null) {
            method = safe(response.initiatingRequest().method());
            url = safe(response.initiatingRequest().url());
        }

        String mime = response.mimeType() == null ? "" : response.mimeType().toString();

        api.logging().logToOutput(
                "[LogicHunter][RES] scope=" + scopeLabel(inScope)
                        + " status=" + response.statusCode()
                        + " method=" + method
                        + " url=" + url
                        + " mime=" + mime
        );
    }

    private String scopeLabel(boolean inScope) {
        return inScope ? "in" : "out";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}