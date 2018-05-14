package org.folio.edge.rtac.cache;

import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_CAPACITY;
import static org.folio.edge.rtac.Constants.DEFAULT_TOKEN_CACHE_TTL_MS;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

/**
 * A cache for storing (JWT) tokens. For now, cache entries are have a set TTL,
 * but eventually should get their expiration time from the token itself (once
 * OKAPI support expiring JWTs)
 * 
 * @param <T>
 */
public class TokenCache<T> {

  public static final Logger logger = Logger.getLogger(TokenCache.class);

  private LinkedHashMap<String, CacheValue<T>> cache;
  private long ttl;
  private int capacity;

  private TokenCache() {
    ttl = Long.parseLong(DEFAULT_TOKEN_CACHE_TTL_MS);
    capacity = Integer.parseInt(DEFAULT_TOKEN_CACHE_CAPACITY);
  }

  public T get(String tenant, String username) {
    String key = getKey(tenant, username);
    CacheValue<T> cached = cache.get(key);

    if (cached != null) {
      if (cached.expired()) {
        cache.remove(key);
        return null;
      } else {
        T token = cached.value;
        return token;
      }
    } else {
      return null;
    }
  }

  public void put(String tenant, String username, T token) {
    String key = getKey(tenant, username);

    // Double-checked locking...
    CacheValue<T> fromCache = cache.get(key);
    if (fromCache == null || fromCache.expired()) {

      // lock to safeguard against multiple threads
      // trying to cache the same key at the same time
      synchronized (this) {
        fromCache = cache.get(key);
        if (fromCache == null || fromCache.expired()) {
          cache.put(key, new CacheValue<T>(token, System.currentTimeMillis() + ttl));
        }
      }
    }

    if (cache.size() >= capacity) {
      prune();
    }
  }

  private String getKey(String tenant, String username) {
    return String.format("%s:%s", tenant, username);
  }

  private void prune() {
    logger.info("Cache size before pruning: " + cache.size());

    LinkedHashMap<String, CacheValue<T>> updated = new LinkedHashMap<String, CacheValue<T>>(capacity);
    Iterator<String> keyIter = cache.keySet().iterator();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      CacheValue<T> val = cache.get(key);
      if (val != null && !val.expired()) {
        updated.put(key, val);
      } else {
        logger.info("Pruning expired cache entry: " + key);
      }
    }

    if (updated.size() > capacity) {
      // this works because LinkedHashMap maintains order of insertion
      String key = updated.keySet().iterator().next();
      logger.info(String
        .format(
            "Cache is above capacity and doesn't contain expired entries.  Removing oldest entry (%s)",
            key));
      updated.remove(key);
    }

    // atomic swap-in updated cache.
    cache = updated;

    logger.info("Cache size after pruning: " + updated.size());
  }

  /**
   * A Generic, immutable cache entry.
   * 
   * Expiration times are specified in ms since epoch.<br>
   * e.g. <code>System.currentTimeMills() + TTL</code>
   * 
   * @param <T>
   *          The class/type of value being cached
   */
  public static final class CacheValue<T> {
    public final T value;
    public final long expires;

    public CacheValue(T value, long expires) {
      this.value = value;
      this.expires = expires;
    }

    public boolean expired() {
      return expires < System.currentTimeMillis();
    }
  }

  public static class TokenCacheBuilder<T> {
    private TokenCache<T> instance;

    public TokenCacheBuilder() {
      instance = new TokenCache<T>();
    }

    public TokenCacheBuilder<T> withTTL(long ttl) {
      instance.ttl = ttl;
      return this;
    }

    public TokenCacheBuilder<T> withCapacity(int capacity) {
      instance.capacity = capacity;
      return this;
    }

    public TokenCache<T> build() {
      instance.cache = new LinkedHashMap<String, CacheValue<T>>(instance.capacity);
      return instance;
    }
  }
}