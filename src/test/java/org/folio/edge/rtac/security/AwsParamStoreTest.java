package org.folio.edge.rtac.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.log4j.Level;
import org.folio.edge.rtac.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.AWSSimpleSystemsManagementException;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

public class AwsParamStoreTest {

  @Mock
  AWSSimpleSystemsManagement ssm;

  @InjectMocks
  AwsParamStore secureStore;

  @Before
  public void setUp() throws Exception {
    // Either use env vars or system props so that tests
    // can be run in non-ECS container environments.
    //
    // Use system props since they're easier to deal with
    // programmatically.
    System.setProperty("aws.accessKeyId", "bogus");
    System.setProperty("aws.secretKey", "bogus");

    // Use empty properties since the only thing configurable
    // is related to AWS, which is mocked here
    secureStore = new AwsParamStore(new Properties());

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testConstruction() {
    assertEquals(AwsParamStore.DEFAULT_REGION, secureStore.getRegion());

    String euCentral1 = "eu-central-1";

    Properties diffProps = new Properties();
    diffProps.setProperty(AwsParamStore.PROP_REGION, euCentral1);

    secureStore = new AwsParamStore(diffProps);
    assertEquals(euCentral1, secureStore.getRegion());
  }

  @Test
  public void testGetFound() {
    // test data & expected values
    String tenant = "foo";
    String user = "bar";
    String val = "letmein";
    String key = tenant + "_" + user;

    // setup mocks/spys/etc.
    GetParameterRequest req = new GetParameterRequest().withName(key).withWithDecryption(true);
    GetParameterResult resp = new GetParameterResult().withParameter(new Parameter().withName(key).withValue(val));
    when(ssm.getParameter(req)).thenReturn(resp);

    // test & assertions
    assertEquals(val, secureStore.get(tenant, user));
  }

  @Test
  public void testGetNotFound() {
    String exceptionMsg = "Parameter null_null not found. (Service: AWSSimpleSystemsManagement; Status Code: 400; Error Code: ParameterNotFound; Request ID: 25fc4a22-9839-4645-b7b4-ad40aa643821)";
    String logMsg = "Exception retreiving password for null_null: ";
    Throwable exception = new AWSSimpleSystemsManagementException(exceptionMsg);

    when(ssm.getParameter(any())).thenThrow(exception);

    TestUtils.assertLogMessage(AwsParamStore.logger, 1, 1, Level.ERROR, logMsg, exception, () -> {
      String val = secureStore.get(null, null);
      assertNull(val);
    });
  }

}
