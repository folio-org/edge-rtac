package org.folio.edge.rtac.security;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

public class SecureStoreFactoryTest {

  public static final Class<? extends SecureStore> DEFAULT_SS_CLASS = EphemeralStore.class;

  @Test
  public void testGetSecureStoreKnownTypes()
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    Class<?>[] stores = new Class<?>[] {
        AwsParamStore.class,
        EphemeralStore.class,
        VaultStore.class
    };

    SecureStore actual;

    for (Class<?> clazz : stores) {
      actual = SecureStoreFactory.getSecureStore((String) clazz.getField("TYPE").get(null), new Properties());
      assertThat(actual, instanceOf(clazz));
      
      try {
        actual = SecureStoreFactory.getSecureStore((String) clazz.getField("TYPE").get(null), null);
        assertThat(actual, instanceOf(clazz));
      } catch (Exception e) {
        if(clazz.equals(VaultStore.class)) {
          // Expect NPE as VaultStore has required properties
          assertThat(e.getClass(), equalTo(NullPointerException.class));
        } else {
          // Whoops, something went wrong.
          fail(String.format("Unexpected Exception thrown for class: ", clazz.getName(), e.getMessage()));
        }
      }
    }
  }

  @Test
  public void testGetSecureStoreDefaultType()
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    SecureStore actual;

    // unknown type
    actual = SecureStoreFactory.getSecureStore("foo", new Properties());
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));
    actual = SecureStoreFactory.getSecureStore("foo", null);
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));

    // null type
    actual = SecureStoreFactory.getSecureStore(null, new Properties());
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));
    actual = SecureStoreFactory.getSecureStore(null, null);
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));
  }
}
