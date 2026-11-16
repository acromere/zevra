/**
 * Product, mod, and program metadata management and runtime support.
 *
 * <p>This package provides the model and runtime support for describing,
 * locating, and interacting with installable products. A product is described
 * by a {@link com.acromere.product.ProductCard ProductCard} that captures its
 * identity (group and artifact), release information, maintainers, and
 * configuration. At runtime, a running product is exposed through the
 * {@link com.acromere.product.Product Product} interface, which provides
 * access to the product card, settings, and the shared product data folder.</p>
 *
 * <p>Key classes in this package include:</p>
 * <ul>
 *   <li>{@link com.acromere.product.Product Product} /
 *       {@link com.acromere.product.ProgramProduct ProgramProduct} /
 *       {@link com.acromere.product.Program Program} - Interfaces for running
 *       products and executable program products</li>
 *   <li>{@link com.acromere.product.ProductCard ProductCard} - The metadata
 *       descriptor for a product, including identity, version, maintainers,
 *       and configuration</li>
 *   <li>{@link com.acromere.product.RepoCard RepoCard} /
 *       {@link com.acromere.product.CatalogCard CatalogCard} - Descriptors for
 *       product repositories and repository catalogs</li>
 *   <li>{@link com.acromere.product.Version Version} /
 *       {@link com.acromere.product.Release Release} - Version parsing and
 *       comparison, and artifact release identification</li>
 *   <li>{@link com.acromere.product.Maintainer Maintainer} /
 *       {@link com.acromere.product.Contributor Contributor} - People who
 *       maintain or contribute to a product</li>
 *   <li>{@link com.acromere.product.Rebrand Rebrand} - Settings used to
 *       rebrand a packaged program</li>
 *   <li>{@link com.acromere.product.ProductClassLoader ProductClassLoader} - A
 *       class loader for product classes, resources, and native libraries</li>
 *   <li>{@link com.acromere.product.Rb Rb} - Resource bundle lookup with
 *       fallback to parent products</li>
 *   <li>{@link com.acromere.product.ProductCardComparator ProductCardComparator}
 *       / {@link com.acromere.product.RepoCardComparator RepoCardComparator} -
 *       Comparators for sorting cards by selected fields</li>
 *   <li>{@link com.acromere.product.ProgramFlag ProgramFlag} - Standard
 *       program command-line flags</li>
 * </ul>
 *
 * @see com.acromere.product.Product
 * @see com.acromere.product.ProductCard
 * @see com.acromere.product.Version
 */
package com.acromere.product;
