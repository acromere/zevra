/**
 * A flexible, hierarchical data model system. The classes in this package
 * provide structured data management with support for key-value attributes,
 * modified (dirty) state tracking, parent-child relationships,
 * {@link com.acromere.data.DataNodeEvent events},
 * and integration with {@link com.acromere.transaction.Txn transactions}.
 * <p>
 * The core class is {@link com.acromere.data.DataNode DataNode}, which is
 * designed to be extended to represent specific domain data types with defined
 * primary keys, natural keys, and modifying attributes.
 */
package com.acromere.data;