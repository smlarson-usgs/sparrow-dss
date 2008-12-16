package gov.usgswim.sparrow.cachefactory;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * The awesome kludge caching factory is responsible for receiving requests to
 * create an entry for the cache that does not exist yet.  It constructs a new
 * {@code AggregateIdLookupKludge} upon request.
 */
public class AggregateIdLookupKludgeFactory implements CacheEntryFactory {
    public Object createEntry(Object aggLevel) throws Exception {
        AggregateIdLookupKludge kludge = new AggregateIdLookupKludge((String) aggLevel);
        return kludge;
    }
}
