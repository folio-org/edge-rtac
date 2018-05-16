package org.folio.edge.rtac.cache;

import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_TTL_MS;

import org.apache.log4j.Logger;
import org.folio.edge.rtac.cache.Cache.Builder;
import org.folio.edge.rtac.cache.Cache.CacheValue;

/**
 * A cache for storing (JWT) tokens. For now, cache entries are have a set TTL,
 * but eventually should get their expiration time from the token itself (once
 * OKAPI support expiring JWTs)
 */
public class TokenCache {

  public static final Logger logger = Logger.getLogger(TokenCache.class);

  private static TokenCache instance;

  private Cache<String> cache;

  private TokenCache(long ttl, int capacity) {
    logger.info("Using TTL: " + ttl);
    logger.info("Using capcity: " + capacity);
    cache = new Builder<String>()
      .withTTL(ttl)
      .withCapacity(capacity)
      .build();
  }

  /**
   * Get the TokenCache singleton, creating one w/ the default TTL and capacity
   * if necessary
   * 
   * @return the TokenCache singleton instance.
   */
  public static TokenCache getInstance() {
    if (instance == null) {
      synchronized (TokenCache.class) {
        if (instance == null) {
          instance = new TokenCache(
              Long.parseLong(DEFAULT_TOKEN_CACHE_TTL_MS),
              Integer.parseInt(DEFAULT_TOKEN_CACHE_CAPACITY));
        }
      }
    }
    return instance;
  }

  /**
   * Creates a new TokenCache instance, replacing the existing one if it already
   * exists; in which case all pre-existing cache entries will be lost.
   * 
   * @param ttl
   *          cache entry time to live in ms
   * @param capacity
   *          maximum number of entries this cache will hold before pruning
   * @return the new TokenCache singleton instance
   */
  public static TokenCache getInstance(long ttl, int capacity) {
    synchronized (TokenCache.class) {
      if(instance != null) {
        logger.warn("Replacing cache.  All cached entries will be lost");
      }
      instance = new TokenCache(ttl, capacity);
    }
    return instance;
  }

  public String get(String tenant, String username) {
    return cache.get(computeKey(tenant, username));
  }

  public CacheValue<String> put(String tenant, String username, String token) {
    return cache.put(computeKey(tenant, username), token);
  }
  
  private String computeKey(String tenant, String username) {
    return String.format("%s:%s", tenant, username);
  }
}