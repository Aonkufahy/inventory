import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api';
import { AuthService } from '../../services/auth';
import { Product } from '../../models/product';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.html',
  styleUrls: ['./product-form.css'],
  standalone: false
})
export class ProductFormComponent implements OnInit {
  product: Product = { name: '', description: '', price: 0, quantity: 0, categoryId: 1 };
  isEdit = false;

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
      this.apiService.getProduct(+id).subscribe(data => this.product = data);
    }
  }

  saveProduct(): void {
    if (this.isEdit) {
      this.apiService.updateProduct(this.product.id!, this.product).subscribe(() => this.router.navigate(['/products']));
    } else {
      // Add 'null' as the 3rd argument to satisfy the new Service signature
this.apiService.addProduct(this.product.categoryId, this.product).subscribe(
  () => this.router.navigate(['/products'])
);

    }
  }
}