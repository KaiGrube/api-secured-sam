package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private LambdaLogger logger;

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent requestEvent,
                                                      final Context context) {
        logger = context.getLogger();
        final Map<String, String> headers = new HashMap<>();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        if (!hasValidApiKey(requestEvent)) {
            headers.put("Content-Type", "text/plain");
            return response.withStatusCode(401)
                    .withHeaders(headers)
                    .withBody("No valid API key.");
        } else {
            headers.put("Content-Type", "text/plain");
            return response.withStatusCode(200)
                    .withHeaders(headers)
                    .withBody(getContent());
        }
    }

    private boolean hasValidApiKey(final APIGatewayProxyRequestEvent requestEvent) {
        final Optional<String> requestApiKey = Optional.ofNullable(requestEvent.getQueryStringParameters().get("API_KEY"));
        final Optional<String> secretApiKey = Optional.ofNullable(System.getenv("API_KEY"));

        requestApiKey.ifPresentOrElse(
                key -> logger.log(String.format("requestKey=%s%n", key)),
                () -> logger.log("Could not get API_KEY from query parameters.")
        );

        if (requestApiKey.isEmpty()) { // Todo: Better use ifPresentOrElse instead? See below...
            logger.log("Could not get API_KEY from query parameters.");
            return false;
        }
//        requestApiKey.ifPresentOrElse(
//                key -> logger.log(String.format("requestKey=%s%n", key)),
//                () -> logger.log("Could not get API_KEY from query parameters.") // Todo: How to "return false"? Wrong approach?!
//        );

        logger.log(String.format("requestApiKey=%s%n", requestApiKey.get()));

        if (secretApiKey.isEmpty()) {
            logger.log("Could not get API_KEY from environment.");
            return false;
        }

        if (requestApiKey.equals(secretApiKey)) {
            logger.log("Valid API_KEY.");
            return true;
        }
        logger.log("Missing or invalid API_KEY.");
        return false;
    }

    private String getContent() {
        return "Hello World :-)";
    }
}