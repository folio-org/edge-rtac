package org.folio.edge.rtac.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

/**
 * A general purpose cache storing entries with a set TTL,
 *
 * @param <T>
 */
public class Cache<T> {

  public static final Logger logger = Logger.getLogger(Cache.class);

  private LinkedHashMap<String, CacheValue<T>> storage;
  private final long ttl;
  private final int capacity;
  
  private Cache(long ttl, int capacity) {
    this.ttl = ttl;
    this.capacity = capacity;
    storage = new LinkedHashMap<>(capacity);
  }

  public T get(String key) {
    CacheValue<T> cached = storage.get(key);

    if (cached != null) {
      if (cached.expired()) {
        storage.remove(key);
        return null;
      } else {
        return cached.value;
      }
    } else {
      return null;
    }
  }

  public CacheValue<T> put(String key, T value) {
    // Double-checked locking...
    CacheValue<T> cached = storage.get(key);
    if (cached == null || cached.expired()) {

      // lock to safeguard against multiple threads
      // trying to cache the same key at the same time
      synchronized (this) {
        cached = storage.get(key);
        if (cached == null || cached.expired()) {
          cached = new CacheValue<>(value, System.currentTimeMillis() + ttl);
          storage.put(key, cached);
        }
      }
    }

    if (storage.size() >= capacity) {
      prune();
    }

    return cached;
  }

  private void prune() {
    logger.info("Cache size before pruning: " + storage.size());

    LinkedHashMap<String, CacheValue<T>> updated = new LinkedHashMap<>(capacity);
    Iterator<String> keyIter = storage.keySet().iterator();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      CacheValue<T> val = storage.get(key);
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
    storage = updated;

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

  public static class Builder<T> {
    private long ttl;
    private int capacity;
    
    public Builder() {
      // nothing to do here...
    }

    public Builder<T> withTTL(long ttl) {
      this.ttl = ttl;
      return this;
    }

    public Builder<T> withCapacity(int capacity) {
      this.capacity = capacity;
      return this;
    }

    public Cache<T> build() {
      return new Cache<>(ttl, capacity);
    }
  }
}