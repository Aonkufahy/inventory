import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { Product } from '../../models/product';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.html',
  styleUrls: ['./admin.css']
})
export class AdminComponent implements OnInit {
  products: Product[] = [];
  newProduct: Product = { name: '', description: '', price: 0, quantity: 0, categoryId: 1 };
  editingProduct: Product | null = null;
  selectedCategoryId: number = 1;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/products']);
      return;
    }
    this.loadProducts();
  }

  loadProducts(): void {
    this.apiService.getAdminProducts().subscribe(
      data => {
        this.products = data;
      },
      error => {
        console.error('Error loading products:', error);
      }
    );
  }

  addProduct(): void {
    this.apiService.addProduct(this.selectedCategoryId, this.newProduct).subscribe(
      () => {
        this.loadProducts();
        this.newProduct = { name: '', description: '', price: 0, quantity: 0, categoryId: 1 };
      },
      error => {
        console.error('Error adding product:', error);
      }
    );
  }

  editProduct(product: Product): void {
    this.editingProduct = { ...product };
  }

  saveEdit(): void {
    if (this.editingProduct) {
      this.apiService.updateProduct(this.editingProduct.id!, this.editingProduct).subscribe(
        () => {
          this.loadProducts();
          this.editingProduct = null;
        },
        error => {
          console.error('Error updating product:', error);
        }
      );
    }
  }

  cancelEdit(): void {
    this.editingProduct = null;
  }

  deleteProduct(id: number): void {
    this.apiService.deleteProduct(id).subscribe(
      () => {
        this.loadProducts();
      },
      error => {
        console.error('Error deleting product:', error);
      }
    );
  }
}