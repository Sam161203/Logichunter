import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;
import core.SafeLogger;
import ui.LogicHunterTab;

/**
 * Entry point for the Burp Suite extension.
 *
 * This version only logs HTTP traffic through SafeLogger.
 */
public class Extension implements BurpExtension, HttpHandler {

    private MontoyaApi api;
    private SafeLogger safeLogger;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        this.safeLogger = new SafeLogger(api);

        api.extension().setName("LogicHunter");
        api.userInterface().registerSuiteTab("LogicHunter", new LogicHunterTab());
        api.http().registerHttpHandler(this);
        api.extension().registerUnloadingHandler(() ->
                api.logging().logToOutput("[LogicHunter] Unloaded cleanly."));

        api.logging().logToOutput("[LogicHunter] Loaded in safe logging mode.");
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent request) {
        if (safeLogger != null) {
            safeLogger.logRequest(request, isInScope(request != null ? request.url() : null));
        }
        return RequestToBeSentAction.continueWith(request);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived response) {
        String requestUrl = null;
        if (response != null && response.initiatingRequest() != null) {
            requestUrl = response.initiatingRequest().url();
        }

        if (safeLogger != null) {
            safeLogger.logResponse(response, isInScope(requestUrl));
        }

        return ResponseReceivedAction.continueWith(response);
    }

    private boolean isInScope(String url) {
        if (api == null || url == null) {
            return false;
        }

        try {
            return api.scope().isInScope(url);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}