/**
 * Basic, multipurpose, thread scoped transaction management. Originally
 * developed as part of the {@link com.acromere.data} package, the logic was
 * eventually separated for other uses. The general intent is that things that
 * should happen together happen as a unit when the transaction is committed.
 */
package com.acromere.transaction;