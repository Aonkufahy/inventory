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

  constructor(private apiService: ApiService, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/products']);
      return;
    }
    this.loadProducts();
  }

  loadProducts(): void {
    this.apiService.getAdminProducts().subscribe(data => this.products = data);
  }

 // Add this property to your class
selectedFile: File | null = null;

onFileSelected(event: any): void {
  this.selectedFile = event.target.files[0];
}

addProduct(): void {
  const productPayload = { 
    ...this.newProduct, 
    categoryId: Number(this.selectedCategoryId) 
  };

  // Pass the selectedFile as the 3rd argument
 // Add 'this.selectedFile' as the 3rd argument
this.apiService.addProduct(this.selectedCategoryId, productPayload).subscribe({

    next: () => {
      this.loadProducts();
      this.newProduct = { name: '', description: '', price: 0, quantity: 0, categoryId: 1 };
      this.selectedFile = null; // Clear file after success
    },
    error: (err) => console.error('Add failed', err)
  });
}



  editProduct(product: Product): void {
    this.editingProduct = { ...product };
    this.selectedCategoryId = product.categoryId;
  }

  saveEdit(): void {
    if (this.editingProduct) {
      this.editingProduct.categoryId = Number(this.selectedCategoryId);
      
      this.apiService.updateProduct(this.editingProduct.id!, this.editingProduct).subscribe({
        next: () => {
          this.loadProducts();
          this.editingProduct = null;
        },
        error: (err) => console.error('Update failed', err)
      });
    }
  }

  cancelEdit(): void {
    this.editingProduct = null;
  }

  deleteProduct(id: number): void {
    if (confirm('Delete this product?')) {
      this.apiService.deleteProduct(id).subscribe(() => this.loadProducts());
    }
  }
}
