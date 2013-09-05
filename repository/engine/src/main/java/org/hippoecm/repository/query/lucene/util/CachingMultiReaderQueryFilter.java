/*
 *  Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.repository.query.lucene.util;

import java.io.IOException;

import org.apache.jackrabbit.core.query.lucene.MultiIndexReader;
import org.apache.jackrabbit.core.query.lucene.hits.AbstractHitCollector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.WeakIdentityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingMultiReaderQueryFilter extends Filter {

    private static final Logger log = LoggerFactory.getLogger(CachingMultiReaderQueryFilter.class);

    private final WeakIdentityMap<Object, ValidityBitSet> cache = WeakIdentityMap.newConcurrentHashMap();

    private final Query query;

    // for some queries, typically the jackrabbit parentAxis / childAxis queries do not support
    // multi index dissection as they require to be able to jump through multiple indexes for a query.
    // these queries need to be done with dissectMultiIndex = false.
    private boolean dissectMultiIndex = true;

    public CachingMultiReaderQueryFilter(final Query query) {
        this.query = query;
    }


    public CachingMultiReaderQueryFilter(final Query query, boolean dissectMultiIndex) {
        this.query = query;
        this.dissectMultiIndex = dissectMultiIndex;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
        if (dissectMultiIndex && reader instanceof MultiIndexReader) {
            MultiIndexReader multiIndexReader = (MultiIndexReader) reader;

            IndexReader[] indexReaders = multiIndexReader.getIndexReaders();
            DocIdSet[] docIdSets = new DocIdSet[indexReaders.length];
            int[] maxDocs = new int[indexReaders.length];
            for (int i = 0; i < indexReaders.length; i++) {
                IndexReader subReader = indexReaders[i];
                docIdSets[i] = getIndexReaderDocIdSet(subReader, subReader);
                maxDocs[i] = subReader.maxDoc();
            }

            return new MultiDocIdSet(docIdSets, maxDocs);
        }
        // Jackrabbit creates a *NEW* JackrabbitIndexReader instance for *EVERY* search. Hence
        // if the reader is a JackrabbitIndexReader, the cache would be pointless.
        // Since all index readers in JR extend from FilterIndexReader, we use
        // reader.getCoreCacheKey() : The FilterIndexReader delegates that call to the
        // wrapped index reader
        // HOWEVER, for other index readers, the CACHEKEY must not be of the wrapped index reader as this one
        // does not contain the DELETED bitset for example that READ-ONLY indexreader has
        //System.out.println(reader.getCoreCacheKey());
        return getIndexReaderDocIdSet(reader, reader.getCoreCacheKey());
    }

    private DocIdSet getIndexReaderDocIdSet(final IndexReader reader, Object cacheKey) throws IOException {

        ValidityBitSet validityBitSet = cache.get(cacheKey);
        if (validityBitSet != null) {
            // unfortunately, Jackrabbit can return a ReadOnlyIndexReader which is the same instance as used previously,
            // but still happened to be changed through it's ***deleted*** bitset : This is a optimisation.
            // See AbstractIndex#getReadOnlyIndexReader. This is why even though we use an IDENTITY as cachekey, we
            // now still need to check whether the cached bit set is really still valid. We can only do this by checking
            // numDocs as when a doc id gets deleted in the ReadOnlyIndexReader, numDocs decreases
            if (reader.numDocs() == validityBitSet.numDocs) {
                log.debug("Return cached bitSet for reader '{}'", reader);
                return validityBitSet.bitSet;
            } else {
                log.debug("ReadOnlyIndexReader '{}' deleted bitset got changed. Cached entry not valid any more", reader);
                cache.remove(cacheKey);
            }
        }
        // no synchronization needed: worst case scenario two concurrent thread do it both
        OpenBitSet docIdSet = createDocIdSet(reader);
        cache.put(cacheKey, new ValidityBitSet(reader.numDocs(), docIdSet));
        return docIdSet;
    }

    private OpenBitSet createDocIdSet(IndexReader reader) throws IOException {
        final OpenBitSet bits = new OpenBitSet(reader.maxDoc());

        long start = System.currentTimeMillis();
        new IndexSearcher(reader).search(query, new AbstractHitCollector() {

            @Override
            public final void collect(int doc, float score) {
                bits.set(doc);  // set bit for hit
            }
        });
        log.info("Creating CachingMultiReaderQueryFilter doc id set took {} ms.", String.valueOf(System.currentTimeMillis() - start));
        return bits;
    }


    private class ValidityBitSet {
        private int numDocs;
        private OpenBitSet bitSet;
        private ValidityBitSet(int numDocs, OpenBitSet bitSet) {
            this.numDocs = numDocs;
            this.bitSet = bitSet;
        }
    }

}
