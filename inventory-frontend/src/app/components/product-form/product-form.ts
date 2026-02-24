import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { Product } from '../../models/product';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.html',
  styleUrls: ['./product-form.css']
})
export class ProductFormComponent implements OnInit {

  product: Product = {
    name: '',
    description: '',
    price: 0,
    quantity: 0,
    categoryId: 1,
    imageUrl: ''
  };

  isEdit = false;
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  isLoading = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/products']);
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.isEdit = true;

      this.apiService.getProduct(+id).subscribe({
        next: (data) => {
          this.product = data;
          this.previewUrl = data.imageUrl || null;
        },
        error: (err) => console.error('Error loading product:', err)
      });
    }
  }


 onFileSelected(event: Event): void {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files.length > 0) {
    this.selectedFile = input.files[0];
    console.log('✅ File selected:', this.selectedFile.name);
    
    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl = reader.result as string;
    };
    reader.readAsDataURL(this.selectedFile);
  }
}

 saveProduct(): void {
  this.isLoading = true;
  
  const formData = new FormData();
  formData.append('name', this.product.name);
  formData.append('description', this.product.description);
  formData.append('price', this.product.price.toString());
  formData.append('quantity', this.product.quantity.toString());
  
  
  if (this.selectedFile) {
    formData.append('image', this.selectedFile, this.selectedFile.name);
    console.log('✅ Image selected for upload:', this.selectedFile.name);
  } else {
    console.log('ℹ️ No new image selected');
  }

  if (this.isEdit) {
    // For edit - update with image (image may or may not be included)
    this.apiService.updateProductWithImage(this.product.id!, formData)
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          console.log('✅ Update successful:', response);
          alert('Product updated successfully!');
          this.router.navigate(['/products']);
        },
        error: (err) => {
          this.isLoading = false;
          console.error('❌ Error updating product:', err);
          alert('Error updating product: ' + (err.error?.error || err.message));
        }
      });
  } else {
    // For new product - include categoryId
    formData.append('categoryId', this.product.categoryId.toString());
    
    this.apiService.addProductWithImage(formData)
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          console.log('✅ Product created:', response);
          alert('Product created successfully!');
          this.router.navigate(['/products']);
        },
        error: (err) => {
          this.isLoading = false;
          console.error('❌ Error creating product:', err);
          alert('Error creating product: ' + (err.error?.error || err.message));
        }
      });
  }
}
}