<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Servlet & JSP View - InventoryPro ERP</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <!-- Load custom styles from the static resources mapping -->
    <link href="css/style.css?v=1.2" rel="stylesheet">
</head>
<body>

<div class="app-container">
    <!-- Sidebar Container -->
    <aside id="layoutSidebar"></aside>

    <div class="app-main">
        <!-- Header Container -->
        <div id="layoutHeader"></div>

        <main class="page-content">
            <!-- Info Badge for Servlet & JSP -->
            <div class="alert alert-info d-flex align-items-center justify-content-between mb-4 border-0 shadow-sm" role="alert" style="border-radius: var(--radius-md);">
                <div class="d-flex align-items-center">
                    <i class="fas fa-server fa-lg me-3 text-info"></i>
                    <div>
                        <strong class="d-block">Java Servlet & JSP Mode Activated</strong>
                        <span class="small text-secondary">This page is rendered server-side using <code>ProductServlet.java</code> and <code>products-list.jsp</code> via JSTL.</span>
                    </div>
                </div>
                <span class="badge bg-info text-white py-2 px-3" style="font-size: 0.8rem; border-radius: var(--radius-sm);">SSR</span>
            </div>

            <!-- Page Title & Actions -->
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
                <div>
                    <h3 class="font-weight-800 text-primary mb-1">Product Catalog (JSP Rendered)</h3>
                    <p class="text-muted mb-0 small">Real-time inventory listing dynamically processed on the server.</p>
                </div>
                <a href="products.html" class="btn-primary-custom text-decoration-none d-inline-flex align-items-center gap-2">
                    <i class="fas fa-arrow-left"></i> Back to AJAX Catalog
                </a>
            </div>

            <!-- Products Table Card -->
            <div class="card-custom">
                <div class="card-header-custom d-flex justify-content-between align-items-center">
                    <h5 class="card-title-custom mb-0">
                        <i class="fas fa-box text-primary me-2"></i> Products List
                    </h5>
                    <span class="badge bg-primary-light text-primary font-weight-700 px-3 py-2" style="font-size: 0.85rem; border-radius: var(--radius-sm);">
                        Total Items: <c:out value="${products.size()}"/>
                    </span>
                </div>
                <div class="card-body-custom p-0">
                    <div class="table-custom-wrapper">
                        <table class="table-custom">
                            <thead>
                                <tr>
                                    <th>Code</th>
                                    <th>Product Name</th>
                                    <th>Category</th>
                                    <th>Supplier</th>
                                    <th>Cost Price</th>
                                    <th>Selling Price</th>
                                    <th>Discount (%)</th>
                                    <th>Available Qty</th>
                                    <th>Min Stock</th>
                                    <th>Stock Status</th>
                                    <th class="text-end">Render Type</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty products}">
                                        <tr>
                                            <td colspan="11" class="text-center py-5 text-muted">
                                                <i class="fas fa-box-open fa-3x mb-3 text-light"></i>
                                                <p class="mb-0">No products found in the database.</p>
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="product" items="${products}">
                                            <tr>
                                                <td><span class="text-secondary font-weight-600"><c:out value="${product.productCode}"/></span></td>
                                                <td><strong><c:out value="${product.productName}"/></strong></td>
                                                <td><c:out value="${product.category != null ? product.category.categoryName : 'None'}"/></td>
                                                <td><c:out value="${product.supplier != null ? product.supplier.supplierName : 'None'}"/></td>
                                                <td>₹<c:out value="${String.format('%.2f', product.unitPrice)}"/></td>
                                                <td>₹<c:out value="${String.format('%.2f', product.sellingPrice)}"/></td>
                                                <td><c:out value="${product.discount != null ? product.discount : 0.0}"/>%</td>
                                                <td>
                                                    <span class="${product.quantity <= 0 ? 'text-danger font-weight-700' : ''}">
                                                        <c:out value="${product.quantity}"/>
                                                    </span>
                                                </td>
                                                <td><c:out value="${product.minimumStockLevel}"/></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${product.quantity <= 0}">
                                                            <span class="badge-pill badge-out-of-stock"><i class="fas fa-times-circle"></i> Out of Stock</span>
                                                        </c:when>
                                                        <c:when test="${product.quantity <= product.minimumStockLevel}">
                                                            <span class="badge-pill badge-low-stock"><i class="fas fa-exclamation-triangle"></i> Low Stock</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-pill badge-in-stock"><i class="fas fa-check-circle"></i> In Stock</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <span class="badge bg-success-light text-success border border-success-light px-2 py-1">JSP Engine</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Load Bootstrap JS & Custom Application Scripts -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script src="js/api.js?v=1.2"></script>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        // Render the application layout: sidebar & top header
        // Using 'products.html' as active page to highlight the menu
        renderLayout('products.html', 'JSP Product Catalog', 'JSP Servlet View');
    });
</script>
</body>
</html>
