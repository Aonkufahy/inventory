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
  selectedFile: File | null = null;

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
    this.apiService.getAdminProducts().subscribe({
      next: (data) => {
        this.products = data;
        console.log('Products loaded:', this.products);
      },
      error: (err) => console.error('Failed to load products:', err)
    });
  }

  onFileSelected(event: any): void {
    console.log('=== onFileSelected ===');
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
      console.log('✅ File selected:', this.selectedFile?.name);
    } else {
      console.log('❌ No file selected');
      this.selectedFile = null;
    }
  }

  addProduct(): void {
    console.log('=== addProduct ===');
    
    const formData = new FormData();
    formData.append('name', this.newProduct.name);
    formData.append('description', this.newProduct.description);
    formData.append('price', this.newProduct.price.toString());
    formData.append('quantity', this.newProduct.quantity.toString());
    formData.append('categoryId', this.selectedCategoryId.toString());

    if (this.selectedFile) {
      formData.append('image', this.selectedFile, this.selectedFile.name);
    }

    this.apiService.addProductWithImage(formData).subscribe({
      next: (response: any) => {
        console.log('✅ Product added successfully!', response);
        this.loadProducts();
        this.resetForm();
        alert('Product added successfully!');
      },
      error: (err) => {
        console.error('❌ Add failed:', err);
        alert('Error adding product: ' + (err.error?.error || err.message));
      }
    });
  }

  resetForm(): void {
    this.newProduct = { name: '', description: '', price: 0, quantity: 0, categoryId: 1 };
    this.selectedCategoryId = 1;
    this.selectedFile = null;
    
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  editProduct(product: Product): void {
    console.log('=== editProduct ===', product);
    this.editingProduct = { ...product };
    this.selectedCategoryId = product.categoryId;
    this.selectedFile = null; // Reset file selection
    
    // Clear file input
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  saveEdit(): void {
    console.log('=== saveEdit ===');
    console.log('Editing product:', this.editingProduct);
    console.log('Selected file:', this.selectedFile);
    
    if (!this.editingProduct || !this.editingProduct.id) {
      console.error('No product being edited');
      return;
    }

    if (this.selectedFile) {
      // If there's a new image, use FormData with image
      const formData = new FormData();
      formData.append('name', this.editingProduct.name);
      formData.append('description', this.editingProduct.description);
      formData.append('price', this.editingProduct.price.toString());
      formData.append('quantity', this.editingProduct.quantity.toString());
      formData.append('categoryId', this.selectedCategoryId.toString());
      formData.append('image', this.selectedFile, this.selectedFile.name);
      
      console.log('Updating with image using:', `/api/admin/products/${this.editingProduct.id}/with-image`);
      
      this.apiService.updateProductWithImage(this.editingProduct.id, formData).subscribe({
        next: (response: any) => {
          console.log('✅ Product updated with image successfully!', response);
          this.loadProducts();
          this.cancelEdit();
          alert('Product updated successfully!');
        },
        error: (err) => {
          console.error('❌ Update with image failed:', err);
          alert('Error updating product: ' + (err.error?.error || err.message));
        }
      });
    } else {
      // No new image, update without image
      const productToUpdate: Product = {
        id: this.editingProduct.id,
        name: this.editingProduct.name,
        description: this.editingProduct.description,
        price: this.editingProduct.price,
        quantity: this.editingProduct.quantity,
        categoryId: Number(this.selectedCategoryId)
      };
      
      console.log('Updating without image using:', `/api/admin/products/${this.editingProduct.id}`);
      
      this.apiService.updateProduct(this.editingProduct.id, productToUpdate).subscribe({
        next: (response) => {
          console.log('✅ Product updated successfully!', response);
          this.loadProducts();
          this.cancelEdit();
          alert('Product updated successfully!');
        },
        error: (err) => {
          console.error('❌ Update failed:', err);
          alert('Error updating product: ' + (err.error?.error || err.message));
        }
      });
    }
  }

  cancelEdit(): void {
    console.log('=== cancelEdit ===');
    this.editingProduct = null;
    this.selectedFile = null;
    
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  deleteProduct(id: number): void {
    if (confirm('Are you sure you want to delete this product?')) {
      console.log('=== deleteProduct ===', id);
      this.apiService.deleteProduct(id).subscribe({
        next: () => {
          console.log('✅ Product deleted successfully');
          this.loadProducts();
          alert('Product deleted successfully!');
        },
        error: (err) => {
          console.error('❌ Delete failed:', err);
          alert('Error deleting product: ' + (err.error?.error || err.message));
        }
      });
    }
  }
}