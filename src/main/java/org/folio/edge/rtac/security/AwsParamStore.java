package org.folio.edge.rtac.security;

import static org.folio.edge.rtac.Constants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ContainerCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.internal.CredentialsEndpointProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;

public class AwsParamStore extends SecureStore {

  protected static final Logger logger = Logger.getLogger(AwsParamStore.class);

  public static final String TYPE = "AwsSsm";

  public static final String PROP_REGION = "region";
  public static final String PROP_KEY_ID = "keyId";

  public static final String DEFAULT_REGION = "us-east-1";

  private String region;

  private AWSCredentialsProvider credProvider;

  protected AWSSimpleSystemsManagement ssm;

  public AwsParamStore(Properties properties) {
    super(properties);
    logger.info("Initializing...");

    if (properties != null) {
      region = properties.getProperty(PROP_REGION, DEFAULT_REGION);
    } else {
      region = DEFAULT_REGION;
    }

    credProvider = new EnvironmentVariableCredentialsProvider();
    try {
      credProvider.getCredentials();
    } catch (Exception e) {
      try {
        credProvider = new SystemPropertiesCredentialsProvider();
      } catch (Exception e2) {
        try {
          credProvider.getCredentials();
        } catch (Exception e3) {
          credProvider = new ContainerCredentialsProvider(new ECSCredentialsEndpointProvider());
          credProvider.getCredentials();
        }
      }
    }

    ssm = AWSSimpleSystemsManagementClientBuilder.standard()
      .withRegion(region)
      .withCredentials(credProvider)
      .build();
  }

  @Override
  public String get(String tenant, String username) {
    String ret = null;

    String key = String.format("%s_%s", tenant, username);
    GetParameterRequest req = new GetParameterRequest()
      .withName(key)
      .withWithDecryption(true);

    try {
      GetParameterResult res = ssm.getParameter(req);
      if (res != null) {
        ret = res.getParameter().getValue();
      }
    } catch (Exception e) {
      logger.error(String.format(
          "Exception retreiving password for %s: ",
          key),
          e);
    }

    return ret;
  }

  protected static class ECSCredentialsEndpointProvider extends CredentialsEndpointProvider {

    /**
     * Environment variable to get the Amazon ECS credentials resource path.
     */
    public static final String ECS_CONTAINER_CREDENTIALS_PATH = "AWS_CONTAINER_CREDENTIALS_RELATIVE_URI";

    /**
     * Default endpoint to retrieve the Amazon ECS Credentials.
     */
    public static final String ECS_CREDENTIALS_ENDPOINT = System.getProperty(SYS_ECS_CREDENTIALS_ENDPOINT,
        "http://169.254.170.2");

    @Override
    public URI getCredentialsEndpoint() throws URISyntaxException {
      String path = System.getenv(ECS_CONTAINER_CREDENTIALS_PATH);
      if (path == null) {
        throw new SdkClientException(
            "The environment variable " + ECS_CONTAINER_CREDENTIALS_PATH + " is empty");
      }

      return new URI(ECS_CREDENTIALS_ENDPOINT + path);
    }
  }

  public String getRegion() {
    return region;
  }
}
