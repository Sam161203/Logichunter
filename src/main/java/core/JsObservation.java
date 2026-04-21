package core;

import java.util.HashMap;
import java.util.Map;

public class JsObservation {

    // Store the "Identifier" -> "Code Snippet" mapping
    // Example: "isAdmin" -> "if (user.isAdmin) { enableDebug() }"
    public final Map<String, String> interestingSnippets = new HashMap<>();

    public void addSnippet(String key, String snippet) {
        // Keep the shortest, cleanest snippet to save tokens
        if (!interestingSnippets.containsKey(key) || snippet.length() < interestingSnippets.get(key).length()) {
            interestingSnippets.put(key, snippet);
        }
    }
}