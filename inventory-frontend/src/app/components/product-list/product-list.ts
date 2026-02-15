import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api';
import { CartService } from '../../services/cart';
import { AuthService } from '../../services/auth';
import { Product } from '../../models/product';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.html',
  styleUrls: ['./product-list.css'],
  standalone: false
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  search = '';
  page = 0;
  size = 10;
  totalElements = 0;
  cartHasItems = false;

  constructor(private apiService: ApiService, private cartService: CartService, public authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.loadProducts();
    this.cartService.cart$.subscribe(cart => {
      this.cartHasItems = cart.items && cart.items.length > 0;
    });
  }

  loadProducts(): void {
    this.apiService.getProducts(this.search, this.page, this.size).subscribe(data => {
      this.products = data.content;
      this.totalElements = data.totalElements;
    });
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product);
  }

  onSearch(): void {
    this.page = 0;
    this.loadProducts();
  }

  nextPage(): void {
    if ((this.page + 1) * this.size < this.totalElements) {
      this.page++;
      this.loadProducts();
    }
  }

  prevPage(): void {
    if (this.page > 0) {
      this.page--;
      this.loadProducts();
    }
  }

  goToCheckout(): void {
    this.router.navigate(['/checkout']);
  }

  deleteProduct(id: number): void {
    if (this.authService.isAdmin()) {
      this.apiService.deleteProduct(id).subscribe(() => this.loadProducts());
    }
  }
}