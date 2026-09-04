/**
 * Product, mod, and program metadata management and runtime support.
 * <p>
 * The core interface is {@link com.acromere.product.Product Product}, which represents
 * an installable or runnable product with associated configuration settings and data storage.
 * <p>
 * Metadata and packaging classes include:
 * <ul>
 *   <li>{@link com.acromere.product.ProductCard ProductCard} - Metadata descriptor representing product identity, version, maintainers, and configuration</li>
 *   <li>{@link com.acromere.product.CatalogCard CatalogCard} / {@link com.acromere.product.RepoCard RepoCard} - Descriptors for product catalogs and repositories</li>
 *   <li>{@link com.acromere.product.Version Version} / {@link com.acromere.product.Release Release} - Versioning and release cycle management</li>
 *   <li>{@link com.acromere.product.Program Program} / {@link com.acromere.product.ProgramProduct ProgramProduct} - Interfaces for executable program products</li>
 *   <li>{@link com.acromere.product.ProductClassLoader ProductClassLoader} - Class loader for product resources and dependencies</li>
 * </ul>
 */
package com.acromere.product;
