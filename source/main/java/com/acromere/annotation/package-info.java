/**
 * Source-level annotations for code documentation and static analysis.
 *
 * <p>This package provides reusable annotations intended primarily for
 * source-level use. Annotations in this package are retained only at the
 * source level and are discarded by the compiler, and are consumed by tools
 * such as static analyzers, code generators, or documentation generators
 * that operate on source code.</p>
 *
 * <p>Key annotations in this package include:</p>
 * <ul>
 *   <li>{@link com.acromere.annotation.Note} - A repeatable annotation for
 *       attaching notes or thread-safety hints to code elements</li>
 *   <li>{@link com.acromere.annotation.Notes} - Container annotation for
 *       multiple {@code @Note} annotations on the same element</li>
 * </ul>
 *
 * @see com.acromere.annotation.Note
 * @see com.acromere.annotation.Notes
 */
package com.acromere.annotation;