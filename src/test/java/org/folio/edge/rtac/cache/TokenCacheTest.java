package org.folio.edge.rtac.cache;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.folio.edge.rtac.cache.TokenCache.CacheValue;
import org.folio.edge.rtac.cache.TokenCache.TokenCacheBuilder;
import org.junit.Before;
import org.junit.Test;

public class TokenCacheTest {

  final int cap = 50;
  final long ttl = 5000;

  TokenCache<Long> cache;

  private final String tenant = "diku";
  private final String user = "diku";
  private final String val = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkaWt1IiwidXNlcl9pZCI6Ijk3ZTNmYzVmLTVjMzMtNGY2Ny1hZmRiLWEzYjI5YTVhYWZjOCIsInRlbmFudCI6ImRpa3UifQ.uda9KgBn82jCR3FXd73CnkmfDDk3OBQI0bjrJ5L7oJ8fS_7-TDNj7UKiFl-YxnqwFGHGACprsG5Bp7kkG8ArZA";

  @Before
  public void setUp() throws Exception {
    cache = new TokenCacheBuilder<Long>()
      .withCapacity(cap)
      .withTTL(ttl)
      .build();
  }

  @Test
  public void testEmpty() throws Exception {
    // empty cache...
    assertNull(cache.get(tenant, user));
  }

  @Test
  public void testGetPutGet() throws Exception {
    // empty cache...
    assertNull(cache.get(tenant, user));

    // basic functionality
    cache.put(tenant, user, 1L);
    assertEquals(1L, cache.get(tenant, user).longValue());
  }

  @Test
  public void testNoOverwrite() throws Exception {
    // make sure we don't overwrite the cached value
    Long val = 1L;

    cache.put(tenant, user, val);
    assertEquals(val.longValue(), cache.get(tenant, user).longValue());

    for (int i = 0; i < 100; i++) {
      cache.put(tenant, user, ++val);
      assertEquals(1L, cache.get(tenant, user).longValue());
    }

    // should expire very soon, if not already.
    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(ttl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cache.get(tenant, user) == null);
  }

  @Test
  public void testPruneExpires() throws Exception {
    CacheValue<Long> cached = cache.put(tenant, user + 0, 0L);
    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(ttl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cached.expired());

    // load capacity + 1 entries triggering eviction of expired
    for (Long i = 1L; i <= cap; i++) {
      cache.put(tenant, user + i, i);
    }

    // should be evicted as it's expired
    assertNull(cache.get(tenant, user + 0));

    // should still be cached
    for (Long i = 1L; i <= cap; i++) {
      assertEquals(i.longValue(), cache.get(tenant, user + i).longValue());
    }
  }

  @Test
  public void testPruneNoExpires() throws Exception {
    // load capacity + 1 entries triggering eviction of the first
    for (Long i = 0L; i <= cap; i++) {
      cache.put(tenant, user + i, i);
    }

    // should be evicted as it's the oldest
    assertNull(cache.get(tenant, user + 0));

    // should still be cached
    for (Long i = 1L; i <= cap; i++) {
      assertEquals(i.longValue(), cache.get(tenant, user + i).longValue());
    }
  }

  @Test
  public void testWithString() throws Exception {
    TokenCache<String> cache = new TokenCacheBuilder<String>()
      .withCapacity(cap)
      .withTTL(ttl)
      .build();

    // empty cache...
    assertNull(cache.get(tenant, user));

    // basic functionality
    CacheValue<String> cached = cache.put(tenant, user, val);
    assertEquals(val, cache.get(tenant, user));

    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(ttl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cached.expired());

    // empty cache...
    assertNull(cache.get(tenant, user));
  }
}
